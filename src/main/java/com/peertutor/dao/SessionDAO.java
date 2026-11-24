package com.peertutor.dao;

import com.peertutor.exception.DatabaseException;
import com.peertutor.model.Session;
import com.peertutor.model.Session.Status;
import com.peertutor.util.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Data Access Object for the sessions table.
 * Booking creation and status updates use transactions.
 */
public class SessionDAO extends BaseDAO {

    private static final Logger LOGGER = Logger.getLogger(SessionDAO.class.getName());

    private final AvailabilityDAO availabilityDAO = new AvailabilityDAO();

    /**
     * Creates a new booking in a transaction:
     *   1. Locks the availability row.
     *   2. Verifies it is still unbooked.
     *   3. Inserts the session row.
     *   4. Marks the availability slot as booked.
     *
     * @return generated session_id
     */
    public int bookSession(int studentId, int tutorId, int availabilityId) {
        Connection conn = DBConnection.getConnection();
        try {
            conn.setAutoCommit(false);

            // 1. Lock the availability row
            String lock = "SELECT is_booked FROM availability WHERE availability_id = ? FOR UPDATE";
            try (PreparedStatement ps = conn.prepareStatement(lock)) {
                ps.setInt(1, availabilityId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) {
                        conn.rollback();
                        throw new DatabaseException("Availability slot not found.");
                    }
                    if (rs.getInt("is_booked") == 1) {
                        conn.rollback();
                        throw new DatabaseException("This slot has already been booked by another student.");
                    }
                }
            }

            // 2. Insert session
            String insertSql = "INSERT INTO sessions (student_id, tutor_id, availability_id, status)" +
                               " VALUES (?, ?, ?, 'PENDING')";
            int sessionId;
            try (PreparedStatement ps = conn.prepareStatement(insertSql, Statement.RETURN_GENERATED_KEYS)) {
                ps.setInt(1, studentId);
                ps.setInt(2, tutorId);
                ps.setInt(3, availabilityId);
                ps.executeUpdate();
                try (ResultSet keys = ps.getGeneratedKeys()) {
                    if (!keys.next()) throw new DatabaseException("Session created but no ID returned.");
                    sessionId = keys.getInt(1);
                }
            }

            // 3. Mark slot as booked
            availabilityDAO.markBooked(availabilityId, conn);

            conn.commit();
            return sessionId;

        } catch (DatabaseException e) {
            safeRollback(conn);
            throw e;
        } catch (SQLException e) {
            safeRollback(conn);
            LOGGER.log(Level.SEVERE, "bookSession failed", e);
            throw new DatabaseException("Failed to book session: " + e.getMessage(), e);
        } finally {
            try { conn.setAutoCommit(true); } catch (SQLException ignored) {}
        }
    }

    /**
     * Approves a pending booking in a transaction.
     */
    public void approveSession(int sessionId) {
        updateStatus(sessionId, Status.PENDING, Status.APPROVED);
    }

    /**
     * Rejects a pending booking; frees the availability slot in a transaction.
     */
    public void rejectSession(int sessionId) {
        changeStatusAndFreeSlot(sessionId, Status.PENDING, Status.REJECTED);
    }

    /**
     * Cancels an approved session; frees the availability slot in a transaction.
     */
    public void cancelSession(int sessionId) {
        changeStatusAndFreeSlot(sessionId, Status.APPROVED, Status.CANCELLED);
    }

    /**
     * Marks a session as COMPLETED (tutor action).
     */
    public void completeSession(int sessionId) {
        updateStatus(sessionId, Status.APPROVED, Status.COMPLETED);
    }

    // ── Queries ──────────────────────────────────────────────────────────────

    /** Returns all sessions for a student. */
    public List<Session> findByStudent(int studentId) {
        return findByColumn("s.student_id", studentId);
    }

    /** Returns all incoming booking requests for a tutor. */
    public List<Session> findByTutor(int tutorId) {
        return findByColumn("s.tutor_id", tutorId);
    }

    /** Returns sessions for a student filtered by status. */
    public List<Session> findByStudentAndStatus(int studentId, Status status) {
        return findFiltered("s.student_id = ? AND s.status = ?", studentId, status.name());
    }

    /** Returns sessions for a tutor filtered by status. */
    public List<Session> findByTutorAndStatus(int tutorId, Status status) {
        return findFiltered("s.tutor_id = ? AND s.status = ?", tutorId, status.name());
    }

    /** Count of upcoming (approved) sessions for the logged-in student. */
    public int countUpcomingForStudent(int studentId) {
        String sql = """
            SELECT COUNT(*) FROM sessions s
            JOIN availability a ON s.availability_id = a.availability_id
            WHERE s.student_id = ? AND s.status = 'APPROVED'
              AND (a.avail_date > CURDATE()
                   OR (a.avail_date = CURDATE() AND a.start_time > CURTIME()))
            """;
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setInt(1, studentId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }
        } catch (SQLException e) {
            LOGGER.log(Level.WARNING, "countUpcomingForStudent failed", e);
        }
        return 0;
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    private void updateStatus(int sessionId, Status expected, Status newStatus) {
        String sql = "UPDATE sessions SET status = ? WHERE session_id = ? AND status = ?";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setString(1, newStatus.name());
            ps.setInt(2, sessionId);
            ps.setString(3, expected.name());
            int rows = ps.executeUpdate();
            if (rows == 0) {
                throw new DatabaseException(
                    "Cannot change status: session not found or already in a different state.");
            }
        } catch (DatabaseException e) {
            throw e;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "updateStatus failed", e);
            throw new DatabaseException("Failed to update session status.", e);
        }
    }

    private void changeStatusAndFreeSlot(int sessionId, Status expected, Status newStatus) {
        Connection conn = DBConnection.getConnection();
        try {
            conn.setAutoCommit(false);

            // Get the availability_id
            int availabilityId = getAvailabilityId(sessionId, conn);

            // Update status
            String sql = "UPDATE sessions SET status = ? WHERE session_id = ? AND status = ?";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, newStatus.name());
                ps.setInt(2, sessionId);
                ps.setString(3, expected.name());
                int rows = ps.executeUpdate();
                if (rows == 0) {
                    conn.rollback();
                    throw new DatabaseException(
                        "Cannot change status: session not found or already in a different state.");
                }
            }

            // Free the slot
            availabilityDAO.markFree(availabilityId, conn);

            conn.commit();
        } catch (DatabaseException e) {
            safeRollback(conn);
            throw e;
        } catch (SQLException e) {
            safeRollback(conn);
            LOGGER.log(Level.SEVERE, "changeStatusAndFreeSlot failed", e);
            throw new DatabaseException("Failed to update session: " + e.getMessage(), e);
        } finally {
            try { conn.setAutoCommit(true); } catch (SQLException ignored) {}
        }
    }

    private int getAvailabilityId(int sessionId, Connection conn) throws SQLException {
        String sql = "SELECT availability_id FROM sessions WHERE session_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, sessionId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }
        }
        throw new DatabaseException("Session #" + sessionId + " not found.");
    }

    private List<Session> findByColumn(String column, int value) {
        List<Session> list = new ArrayList<>();
        String sql = buildSelectSQL("WHERE " + column + " = ? ORDER BY a.avail_date DESC, a.start_time DESC");
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setInt(1, value);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapRow(rs));
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "findByColumn failed", e);
            throw new DatabaseException("Failed to load sessions.", e);
        }
        return list;
    }

    private List<Session> findFiltered(String whereClause, int intParam, String strParam) {
        List<Session> list = new ArrayList<>();
        String sql = buildSelectSQL("WHERE " + whereClause + " ORDER BY a.avail_date DESC, a.start_time DESC");
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setInt(1, intParam);
            ps.setString(2, strParam);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapRow(rs));
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "findFiltered failed", e);
            throw new DatabaseException("Failed to load sessions.", e);
        }
        return list;
    }

    private String buildSelectSQL(String whereOrder) {
        return """
            SELECT s.*,
                   st.name  AS student_name,
                   tu.name  AS tutor_name,
                   a.avail_date,
                   a.start_time,
                   a.end_time
            FROM sessions s
            JOIN users st ON s.student_id = st.user_id
            JOIN users tu ON s.tutor_id   = tu.user_id
            JOIN availability a ON s.availability_id = a.availability_id
            """ + whereOrder;
    }

    private Session mapRow(ResultSet rs) throws SQLException {
        Session sess = new Session();
        sess.setSessionId(rs.getInt("session_id"));
        sess.setStudentId(rs.getInt("student_id"));
        sess.setTutorId(rs.getInt("tutor_id"));
        sess.setAvailabilityId(rs.getInt("availability_id"));
        sess.setStatus(Status.valueOf(rs.getString("status")));
        Timestamp ts = rs.getTimestamp("created_at");
        if (ts != null) sess.setCreatedAt(ts.toLocalDateTime());
        sess.setStudentName(rs.getString("student_name"));
        sess.setTutorName(rs.getString("tutor_name"));
        Date d = rs.getDate("avail_date");
        if (d != null) sess.setSessionDate(d.toLocalDate());
        Time st = rs.getTime("start_time");
        if (st != null) sess.setStartTime(st.toLocalTime());
        Time et = rs.getTime("end_time");
        if (et != null) sess.setEndTime(et.toLocalTime());
        return sess;
    }

    private void safeRollback(Connection conn) {
        try { conn.rollback(); } catch (SQLException ignored) {}
    }
}
