package com.peertutor.dao;

import com.peertutor.exception.DatabaseException;
import com.peertutor.model.Review;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Data Access Object for the reviews table.
 */
public class ReviewDAO extends BaseDAO {

    private static final Logger LOGGER = Logger.getLogger(ReviewDAO.class.getName());

    /** Submits a review. Returns generated review_id. */
    public int addReview(int sessionId, int rating, String comment) {
        String sql = "INSERT INTO reviews (session_id, rating, comment) VALUES (?, ?, ?)";
        try (PreparedStatement ps = getConnection().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, sessionId);
            ps.setInt(2, rating);
            ps.setString(3, comment);
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) return keys.getInt(1);
            }
        } catch (SQLIntegrityConstraintViolationException e) {
            throw new DatabaseException("You have already submitted a review for this session.", e);
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "addReview failed", e);
            throw new DatabaseException("Failed to submit review.", e);
        }
        throw new DatabaseException("Review created but no ID returned.");
    }

    /** Returns the review for a given session, if one exists. */
    public Optional<Review> findBySession(int sessionId) {
        String sql = "SELECT r.*, u.name AS reviewer_name FROM reviews r " +
                     "JOIN sessions s ON r.session_id = s.session_id " +
                     "JOIN users u ON s.student_id = u.user_id " +
                     "WHERE r.session_id = ?";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setInt(1, sessionId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(mapRow(rs));
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "findBySession failed", e);
            throw new DatabaseException("Failed to load review.", e);
        }
        return Optional.empty();
    }

    /** Returns all reviews for a tutor (from all their completed sessions). */
    public List<Review> findByTutor(int tutorId) {
        List<Review> list = new ArrayList<>();
        String sql = """
            SELECT r.*, u.name AS reviewer_name
            FROM reviews r
            JOIN sessions s ON r.session_id = s.session_id
            JOIN users u ON s.student_id = u.user_id
            WHERE s.tutor_id = ?
            ORDER BY r.created_at DESC
            """;
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setInt(1, tutorId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapRow(rs));
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "findByTutor failed", e);
            throw new DatabaseException("Failed to load reviews.", e);
        }
        return list;
    }

    /** Returns average rating for a tutor, or 0.0 if no reviews. */
    public double getAverageRating(int tutorId) {
        String sql = """
            SELECT AVG(r.rating)
            FROM reviews r
            JOIN sessions s ON r.session_id = s.session_id
            WHERE s.tutor_id = ?
            """;
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setInt(1, tutorId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getDouble(1);
            }
        } catch (SQLException e) {
            LOGGER.log(Level.WARNING, "getAverageRating failed", e);
        }
        return 0.0;
    }

    private Review mapRow(ResultSet rs) throws SQLException {
        Review r = new Review();
        r.setReviewId(rs.getInt("review_id"));
        r.setSessionId(rs.getInt("session_id"));
        r.setRating(rs.getInt("rating"));
        r.setComment(rs.getString("comment"));
        Timestamp ts = rs.getTimestamp("created_at");
        if (ts != null) r.setCreatedAt(ts.toLocalDateTime());
        r.setReviewerName(rs.getString("reviewer_name"));
        return r;
    }
}
