package com.peertutor.service;

import com.peertutor.dao.AvailabilityDAO;
import com.peertutor.exception.ValidationException;
import com.peertutor.model.Availability;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

/**
 * Business logic for tutor availability management.
 */
public class AvailabilityService {

    private final AvailabilityDAO availabilityDAO = new AvailabilityDAO();

    /**
     * Adds a new availability slot after validation.
     */
    public int addSlot(int tutorId, LocalDate date,
                       LocalTime startTime, LocalTime endTime)
            throws ValidationException {

        if (date == null)      throw new ValidationException("Date is required.");
        if (startTime == null) throw new ValidationException("Start time is required.");
        if (endTime   == null) throw new ValidationException("End time is required.");

        // Cannot add slots in the past
        LocalDate today = LocalDate.now();
        LocalTime now   = LocalTime.now();
        if (date.isBefore(today))
            throw new ValidationException("Cannot add availability for a past date.");
        if (date.isEqual(today) && !startTime.isAfter(now))
            throw new ValidationException("Start time must be in the future.");

        if (!endTime.isAfter(startTime))
            throw new ValidationException("End time must be after start time.");

        // Minimum slot duration: 30 minutes
        if (startTime.plusMinutes(30).isAfter(endTime))
            throw new ValidationException("Session slot must be at least 30 minutes long.");

        return availabilityDAO.addAvailability(tutorId, date, startTime, endTime);
    }

    /** Deletes an unbooked availability slot. Returns false if slot was already booked. */
    public boolean deleteSlot(int availabilityId) throws ValidationException {
        boolean deleted = availabilityDAO.deleteAvailability(availabilityId);
        if (!deleted)
            throw new ValidationException(
                "Cannot delete this slot — it is already booked by a student.");
        return true;
    }

    /** Returns all slots for a tutor (free and booked). */
    public List<Availability> getTutorSlots(int tutorId) {
        return availabilityDAO.findByTutor(tutorId);
    }

    /** Returns only future, unbooked slots for a tutor. */
    public List<Availability> getAvailableSlots(int tutorId) {
        return availabilityDAO.findAvailableByTutor(tutorId);
    }
}
