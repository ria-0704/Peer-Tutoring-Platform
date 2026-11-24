package com.peertutor.service;

import com.peertutor.dao.SkillDAO;
import com.peertutor.dao.SubjectDAO;
import com.peertutor.exception.ValidationException;
import com.peertutor.model.Skill;
import com.peertutor.model.Subject;

import java.util.List;

/**
 * Business logic for tutor profiles and subject management.
 */
public class TutorService {

    private final SkillDAO   skillDAO   = new SkillDAO();
    private final SubjectDAO subjectDAO = new SubjectDAO();

    /** Returns all subjects available in the system. */
    public List<Subject> getAllSubjects() {
        return subjectDAO.findAll();
    }

    /** Creates a new subject if the name is valid. */
    public Subject addSubject(String name) throws ValidationException {
        if (name == null || name.trim().isEmpty())
            throw new ValidationException("Subject name cannot be empty.");
        if (name.trim().length() > 100)
            throw new ValidationException("Subject name is too long (max 100 characters).");

        int id = subjectDAO.createSubject(name.trim());
        return new Subject(id, name.trim());
    }

    /**
     * Adds a subject to the tutor's profile.
     * @return generated skill ID
     */
    public int addSkillToProfile(int userId, int subjectId, String description)
            throws ValidationException {
        if (subjectId <= 0)
            throw new ValidationException("Please select a subject.");
        if (description != null && description.trim().length() > 500)
            throw new ValidationException("Description is too long (max 500 characters).");

        return skillDAO.addSkill(userId, subjectId,
                description == null ? "" : description.trim());
    }

    /** Updates the description for an existing skill in the tutor's profile. */
    public void updateSkill(int skillId, String description) throws ValidationException {
        if (description != null && description.trim().length() > 500)
            throw new ValidationException("Description is too long (max 500 characters).");
        skillDAO.updateSkill(skillId, description == null ? "" : description.trim());
    }

    /** Removes a subject from the tutor's profile. */
    public void removeSkill(int skillId) {
        skillDAO.deleteSkill(skillId);
    }

    /** Returns all skills for a given tutor. */
    public List<Skill> getTutorSkills(int userId) {
        return skillDAO.findByTutor(userId);
    }

    /** Returns tutor profiles matching a name or subject query. */
    public List<Skill> searchTutors(String query) {
        if (query == null || query.trim().isEmpty())
            return skillDAO.findAllTutorProfiles();
        return skillDAO.searchTutors(query.trim());
    }

    public int countSubjects() {
        return subjectDAO.countSubjects();
    }
}
