package com.peertutor.service;

import com.peertutor.dao.AvailabilityDAO;
import com.peertutor.dao.SessionDAO;
import com.peertutor.exception.ValidationException;
import com.peertutor.model.Availability;
import com.peertutor.model.Session;
import com.peertutor.model.Session.Status;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

/**
 * Business logic for session booking and management.
 */
public class SessionService {

    private final SessionDAO      sessionDAO      = new SessionDAO();
    private final AvailabilityDAO availabilityDAO = new AvailabilityDAO();

    /**
     * Books a tutoring session after all business-rule checks pass.
     */
    public int bookSession(int studentId, int tutorId, int availabilityId)
            throws ValidationException {

        // Cannot book your own session
        if (studentId == tutorId)
            throw new ValidationException("You cannot book your own tutoring session.");

        // Check slot still exists and is unbooked and in the future
        Availability slot = availabilityDAO.findById(availabilityId);
        if (slot == null)
            throw new ValidationException("The selected availability slot does not exist.");
        if (slot.isBooked())
            throw new ValidationException("This slot has already been booked.");

        LocalDate today = LocalDate.now();
        LocalTime now   = LocalTime.now();
        if (slot.getDate().isBefore(today) ||
            (slot.getDate().isEqual(today) && !slot.getStartTime().isAfter(now)))
            throw new ValidationException("Cannot book a past time slot.");

        return sessionDAO.bookSession(studentId, tutorId, availabilityId);
    }

    /** Tutor approves a pending session. */
    public void approveSession(int sessionId, int tutorId, List<Session> sessions)
            throws ValidationException {
        Session sess = findById(sessions, sessionId);
        if (sess == null)
            throw new ValidationException("Session not found.");
        if (sess.getTutorId() != tutorId)
            throw new ValidationException("You are not the tutor for this session.");
        if (sess.getStatus() != Status.PENDING)
            throw new ValidationException("Only pending sessions can be approved.");
        sessionDAO.approveSession(sessionId);
    }

    /** Tutor rejects a pending session. */
    public void rejectSession(int sessionId, int tutorId, List<Session> sessions)
            throws ValidationException {
        Session sess = findById(sessions, sessionId);
        if (sess == null)
            throw new ValidationException("Session not found.");
        if (sess.getTutorId() != tutorId)
            throw new ValidationException("You are not the tutor for this session.");
        if (sess.getStatus() != Status.PENDING)
            throw new ValidationException("Only pending sessions can be rejected.");
        sessionDAO.rejectSession(sessionId);
    }

    /** Student or tutor cancels an approved session. */
    public void cancelSession(int sessionId, int userId, List<Session> sessions)
            throws ValidationException {
        Session sess = findById(sessions, sessionId);
        if (sess == null)
            throw new ValidationException("Session not found.");
        if (sess.getStudentId() != userId && sess.getTutorId() != userId)
            throw new ValidationException("You are not part of this session.");
        if (sess.getStatus() != Status.APPROVED)
            throw new ValidationException("Only approved sessions can be cancelled.");
        sessionDAO.cancelSession(sessionId);
    }

    /** Tutor marks an approved session as completed. */
    public void completeSession(int sessionId, int tutorId, List<Session> sessions)
            throws ValidationException {
        Session sess = findById(sessions, sessionId);
        if (sess == null)
            throw new ValidationException("Session not found.");
        if (sess.getTutorId() != tutorId)
            throw new ValidationException("You are not the tutor for this session.");
        if (sess.getStatus() != Status.APPROVED)
            throw new ValidationException("Only approved sessions can be marked as completed.");
        sessionDAO.completeSession(sessionId);
    }

    public List<Session> getStudentSessions(int studentId) {
        return sessionDAO.findByStudent(studentId);
    }

    public List<Session> getTutorSessions(int tutorId) {
        return sessionDAO.findByTutor(tutorId);
    }

    public List<Session> getStudentSessionsByStatus(int studentId, Status status) {
        return sessionDAO.findByStudentAndStatus(studentId, status);
    }

    public List<Session> getTutorSessionsByStatus(int tutorId, Status status) {
        return sessionDAO.findByTutorAndStatus(tutorId, status);
    }

    public int countUpcoming(int studentId) {
        return sessionDAO.countUpcomingForStudent(studentId);
    }

    private Session findById(List<Session> list, int id) {
        return list.stream()
                   .filter(s -> s.getSessionId() == id)
                   .findFirst().orElse(null);
    }
}
