package com.peertutor.dao;

import com.peertutor.exception.DatabaseException;
import com.peertutor.model.Availability;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Data Access Object for the availability table.
 */
public class AvailabilityDAO extends BaseDAO {

    private static final Logger LOGGER = Logger.getLogger(AvailabilityDAO.class.getName());

    /** Inserts a new availability slot. Returns generated ID. */
    public int addAvailability(int tutorId, LocalDate date,
                               LocalTime startTime, LocalTime endTime) {
        String sql = "INSERT INTO availability (tutor_id, avail_date, start_time, end_time, is_booked)" +
                     " VALUES (?, ?, ?, ?, 0)";
        try (PreparedStatement ps = getConnection().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, tutorId);
            ps.setDate(2, Date.valueOf(date));
            ps.setTime(3, Time.valueOf(startTime));
            ps.setTime(4, Time.valueOf(endTime));
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) return keys.getInt(1);
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "addAvailability failed", e);
            throw new DatabaseException("Failed to add availability slot.", e);
        }
        throw new DatabaseException("Availability created but no ID returned.");
    }

    /** Soft-deletes (physically deletes) an unbooked availability slot. */
    public boolean deleteAvailability(int availabilityId) {
        String sql = "DELETE FROM availability WHERE availability_id = ? AND is_booked = 0";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setInt(1, availabilityId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "deleteAvailability failed", e);
            throw new DatabaseException("Failed to delete availability slot.", e);
        }
    }

    /** Marks a slot as booked (called inside a transaction from SessionDAO). */
    public void markBooked(int availabilityId, Connection conn) throws SQLException {
        String sql = "UPDATE availability SET is_booked = 1 WHERE availability_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, availabilityId);
            ps.executeUpdate();
        }
    }

    /** Marks a slot as free again (called on cancellation/rejection). */
    public void markFree(int availabilityId, Connection conn) throws SQLException {
        String sql = "UPDATE availability SET is_booked = 0 WHERE availability_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, availabilityId);
            ps.executeUpdate();
        }
    }

    /** Returns all availability slots for a tutor (both free and booked). */
    public List<Availability> findByTutor(int tutorId) {
        List<Availability> list = new ArrayList<>();
        String sql = """
            SELECT a.*, u.name AS tutor_name
            FROM availability a
            JOIN users u ON a.tutor_id = u.user_id
            WHERE a.tutor_id = ?
            ORDER BY a.avail_date, a.start_time
            """;
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setInt(1, tutorId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapRow(rs));
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "findByTutor failed", e);
            throw new DatabaseException("Failed to load availability.", e);
        }
        return list;
    }

    /** Returns all unbooked future slots for a specific tutor. */
    public List<Availability> findAvailableByTutor(int tutorId) {
        List<Availability> list = new ArrayList<>();
        String sql = """
            SELECT a.*, u.name AS tutor_name
            FROM availability a
            JOIN users u ON a.tutor_id = u.user_id
            WHERE a.tutor_id = ? AND a.is_booked = 0
              AND (a.avail_date > CURDATE()
                   OR (a.avail_date = CURDATE() AND a.start_time > CURTIME()))
            ORDER BY a.avail_date, a.start_time
            """;
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setInt(1, tutorId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapRow(rs));
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "findAvailableByTutor failed", e);
            throw new DatabaseException("Failed to load available slots.", e);
        }
        return list;
    }

    /** Returns one availability row by ID. */
    public Availability findById(int availabilityId) {
        String sql = "SELECT a.*, u.name AS tutor_name FROM availability a " +
                     "JOIN users u ON a.tutor_id = u.user_id " +
                     "WHERE a.availability_id = ?";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setInt(1, availabilityId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapRow(rs);
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "findById failed", e);
            throw new DatabaseException("Failed to load availability.", e);
        }
        return null;
    }

    private Availability mapRow(ResultSet rs) throws SQLException {
        Availability a = new Availability();
        a.setAvailabilityId(rs.getInt("availability_id"));
        a.setTutorId(rs.getInt("tutor_id"));
        a.setDate(rs.getDate("avail_date").toLocalDate());
        a.setStartTime(rs.getTime("start_time").toLocalTime());
        a.setEndTime(rs.getTime("end_time").toLocalTime());
        a.setBooked(rs.getInt("is_booked") == 1);
        a.setTutorName(rs.getString("tutor_name"));
        return a;
    }
}
