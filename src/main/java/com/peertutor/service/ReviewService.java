package com.peertutor.service;

import com.peertutor.dao.ReviewDAO;
import com.peertutor.exception.ValidationException;
import com.peertutor.model.Review;
import com.peertutor.model.Session;

import java.util.List;
import java.util.Optional;

/**
 * Business logic for session reviews.
 */
public class ReviewService {

    private final ReviewDAO reviewDAO = new ReviewDAO();

    /**
     * Submits a review for a completed session.
     */
    public Review submitReview(Session session, int studentId,
                               int rating, String comment)
            throws ValidationException {

        if (session == null)
            throw new ValidationException("Session not found.");
        if (session.getStudentId() != studentId)
            throw new ValidationException("You can only review sessions you attended.");
        if (session.getStatus() != Session.Status.COMPLETED)
            throw new ValidationException("You can only review completed sessions.");

        // Check for duplicate
        Optional<Review> existing = reviewDAO.findBySession(session.getSessionId());
        if (existing.isPresent())
            throw new ValidationException("You have already submitted a review for this session.");

        if (rating < 1 || rating > 5)
            throw new ValidationException("Rating must be between 1 and 5.");

        if (comment != null && comment.trim().length() > 1000)
            throw new ValidationException("Comment is too long (max 1000 characters).");

        int id = reviewDAO.addReview(session.getSessionId(), rating,
                comment == null ? "" : comment.trim());

        Review r = new Review();
        r.setReviewId(id);
        r.setSessionId(session.getSessionId());
        r.setRating(rating);
        r.setComment(comment);
        return r;
    }

    /** Returns the review for a session, if any. */
    public Optional<Review> getReviewForSession(int sessionId) {
        return reviewDAO.findBySession(sessionId);
    }

    /** Returns all reviews for a tutor. */
    public List<Review> getTutorReviews(int tutorId) {
        return reviewDAO.findByTutor(tutorId);
    }

    /** Returns average rating for a tutor. */
    public double getAverageRating(int tutorId) {
        return reviewDAO.getAverageRating(tutorId);
    }
}
