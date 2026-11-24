package com.peertutor.dao;

import com.peertutor.exception.DatabaseException;
import com.peertutor.model.User;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Data Access Object for the users table.
 */
public class UserDAO extends BaseDAO {

    private static final Logger LOGGER = Logger.getLogger(UserDAO.class.getName());

    /** Inserts a new user. Returns the generated user_id. */
    public int createUser(User user) {
        String sql = "INSERT INTO users (name, email, password_hash, student_id) VALUES (?, ?, ?, ?)";
        try (PreparedStatement ps = getConnection().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, user.getName());
            ps.setString(2, user.getEmail());
            ps.setString(3, user.getPasswordHash());
            ps.setString(4, user.getStudentId());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) return keys.getInt(1);
            }
        } catch (SQLIntegrityConstraintViolationException e) {
            throw new DatabaseException("Email or Student ID already registered.", e);
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "createUser failed", e);
            throw new DatabaseException("Failed to register user: " + e.getMessage(), e);
        }
        throw new DatabaseException("User created but no ID returned.");
    }

    /** Finds a user by email. Returns Optional.empty() if not found. */
    public Optional<User> findByEmail(String email) {
        String sql = "SELECT * FROM users WHERE email = ?";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(mapRow(rs));
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "findByEmail failed", e);
            throw new DatabaseException("Database error during login.", e);
        }
        return Optional.empty();
    }

    /** Finds a user by their primary key. */
    public Optional<User> findById(int userId) {
        String sql = "SELECT * FROM users WHERE user_id = ?";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(mapRow(rs));
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "findById failed", e);
            throw new DatabaseException("Database error.", e);
        }
        return Optional.empty();
    }

    /** Returns true if the given email is already taken. */
    public boolean emailExists(String email) {
        String sql = "SELECT 1 FROM users WHERE email = ?";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            throw new DatabaseException("Database error checking email.", e);
        }
    }

    /** Returns true if the given student ID is already taken. */
    public boolean studentIdExists(String studentId) {
        String sql = "SELECT 1 FROM users WHERE student_id = ?";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setString(1, studentId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            throw new DatabaseException("Database error checking student ID.", e);
        }
    }

    /** Count of distinct tutors (users who have at least one skill). */
    public int countTutors() {
        String sql = "SELECT COUNT(DISTINCT user_id) FROM skills";
        try (PreparedStatement ps = getConnection().prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            LOGGER.log(Level.WARNING, "countTutors failed", e);
        }
        return 0;
    }

    private User mapRow(ResultSet rs) throws SQLException {
        User u = new User();
        u.setUserId(rs.getInt("user_id"));
        u.setName(rs.getString("name"));
        u.setEmail(rs.getString("email"));
        u.setPasswordHash(rs.getString("password_hash"));
        u.setStudentId(rs.getString("student_id"));
        Timestamp ts = rs.getTimestamp("created_at");
        if (ts != null) u.setCreatedAt(ts.toLocalDateTime());
        return u;
    }
}
