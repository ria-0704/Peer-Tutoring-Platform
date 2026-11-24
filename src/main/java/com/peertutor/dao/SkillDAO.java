package com.peertutor.dao;

import com.peertutor.exception.DatabaseException;
import com.peertutor.model.Skill;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Data Access Object for the skills table (tutor profiles).
 */
public class SkillDAO extends BaseDAO {

    private static final Logger LOGGER = Logger.getLogger(SkillDAO.class.getName());

    /** Adds a skill (subject) to a tutor's profile. Returns generated ID. */
    public int addSkill(int userId, int subjectId, String description) {
        String sql = "INSERT INTO skills (user_id, subject_id, description) VALUES (?, ?, ?)";
        try (PreparedStatement ps = getConnection().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, userId);
            ps.setInt(2, subjectId);
            ps.setString(3, description);
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) return keys.getInt(1);
            }
        } catch (SQLIntegrityConstraintViolationException e) {
            throw new DatabaseException("You already have this subject in your profile.", e);
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "addSkill failed", e);
            throw new DatabaseException("Failed to add skill.", e);
        }
        throw new DatabaseException("Skill created but no ID returned.");
    }

    /** Updates the description of an existing skill. */
    public void updateSkill(int skillId, String description) {
        String sql = "UPDATE skills SET description = ? WHERE skill_id = ?";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setString(1, description);
            ps.setInt(2, skillId);
            ps.executeUpdate();
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "updateSkill failed", e);
            throw new DatabaseException("Failed to update skill.", e);
        }
    }

    /** Deletes a skill from a tutor's profile. */
    public void deleteSkill(int skillId) {
        String sql = "DELETE FROM skills WHERE skill_id = ?";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setInt(1, skillId);
            ps.executeUpdate();
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "deleteSkill failed", e);
            throw new DatabaseException("Failed to remove skill.", e);
        }
    }

    /** Returns all skills for a given tutor (with subject name). */
    public List<Skill> findByTutor(int userId) {
        List<Skill> list = new ArrayList<>();
        String sql = """
            SELECT sk.*, s.subject_name
            FROM skills sk
            JOIN subjects s ON sk.subject_id = s.subject_id
            WHERE sk.user_id = ?
            ORDER BY s.subject_name
            """;
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapRow(rs));
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "findByTutor failed", e);
            throw new DatabaseException("Failed to load tutor skills.", e);
        }
        return list;
    }

    /**
     * Returns tutor profiles matching a name or subject search query.
     * Each row represents one skill of one tutor.
     */
    public List<Skill> searchTutors(String query) {
        List<Skill> list = new ArrayList<>();
        String like = "%" + query.trim() + "%";
        String sql = """
            SELECT sk.*, s.subject_name, u.name AS tutor_name
            FROM skills sk
            JOIN subjects s ON sk.subject_id = s.subject_id
            JOIN users    u ON sk.user_id    = u.user_id
            WHERE u.name LIKE ? OR s.subject_name LIKE ?
            ORDER BY u.name, s.subject_name
            """;
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setString(1, like);
            ps.setString(2, like);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Skill sk = mapRow(rs);
                    sk.setTutorName(rs.getString("tutor_name"));
                    list.add(sk);
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "searchTutors failed", e);
            throw new DatabaseException("Failed to search tutors.", e);
        }
        return list;
    }

    /** Returns all tutor profiles (all skills with tutor and subject info). */
    public List<Skill> findAllTutorProfiles() {
        return searchTutors("");
    }

    private Skill mapRow(ResultSet rs) throws SQLException {
        Skill sk = new Skill();
        sk.setSkillId(rs.getInt("skill_id"));
        sk.setUserId(rs.getInt("user_id"));
        sk.setSubjectId(rs.getInt("subject_id"));
        sk.setDescription(rs.getString("description"));
        sk.setSubjectName(rs.getString("subject_name"));
        return sk;
    }
}
