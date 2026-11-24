package com.peertutor.dao;

import com.peertutor.exception.DatabaseException;
import com.peertutor.model.Subject;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Data Access Object for the subjects table.
 */
public class SubjectDAO extends BaseDAO {

    private static final Logger LOGGER = Logger.getLogger(SubjectDAO.class.getName());

    /** Returns all subjects ordered by name. */
    public List<Subject> findAll() {
        List<Subject> list = new ArrayList<>();
        String sql = "SELECT * FROM subjects ORDER BY subject_name";
        try (PreparedStatement ps = getConnection().prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "findAll subjects failed", e);
            throw new DatabaseException("Failed to load subjects.", e);
        }
        return list;
    }

    /** Inserts a new subject. Returns generated ID. */
    public int createSubject(String name) {
        String sql = "INSERT INTO subjects (subject_name) VALUES (?)";
        try (PreparedStatement ps = getConnection().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, name.trim());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) return keys.getInt(1);
            }
        } catch (SQLIntegrityConstraintViolationException e) {
            throw new DatabaseException("Subject '" + name + "' already exists.", e);
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "createSubject failed", e);
            throw new DatabaseException("Failed to create subject.", e);
        }
        throw new DatabaseException("Subject created but no ID returned.");
    }

    /** Count of distinct subjects. */
    public int countSubjects() {
        String sql = "SELECT COUNT(*) FROM subjects";
        try (PreparedStatement ps = getConnection().prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            LOGGER.log(Level.WARNING, "countSubjects failed", e);
        }
        return 0;
    }

    private Subject mapRow(ResultSet rs) throws SQLException {
        return new Subject(rs.getInt("subject_id"), rs.getString("subject_name"));
    }
}
