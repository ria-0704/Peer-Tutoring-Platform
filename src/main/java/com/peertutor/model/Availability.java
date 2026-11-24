package com.peertutor.model;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

/**
 * Represents a time slot that a tutor has made available for tutoring.
 */
public class Availability {

    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("HH:mm");
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd MMM yyyy");

    private int       availabilityId;
    private int       tutorId;
    private LocalDate date;
    private LocalTime startTime;
    private LocalTime endTime;
    private boolean   isBooked;

    // Convenience field from JOIN
    private String tutorName;

    public Availability() {}

    public Availability(int availabilityId, int tutorId, LocalDate date,
                        LocalTime startTime, LocalTime endTime, boolean isBooked) {
        this.availabilityId = availabilityId;
        this.tutorId        = tutorId;
        this.date           = date;
        this.startTime      = startTime;
        this.endTime        = endTime;
        this.isBooked       = isBooked;
    }

    public int       getAvailabilityId() { return availabilityId; }
    public int       getTutorId()        { return tutorId;        }
    public LocalDate getDate()           { return date;           }
    public LocalTime getStartTime()      { return startTime;      }
    public LocalTime getEndTime()        { return endTime;        }
    public boolean   isBooked()          { return isBooked;       }
    public String    getTutorName()      { return tutorName;      }

    public void setAvailabilityId(int id)     { this.availabilityId = id;      }
    public void setTutorId(int tutorId)       { this.tutorId        = tutorId; }
    public void setDate(LocalDate date)       { this.date           = date;    }
    public void setStartTime(LocalTime t)     { this.startTime      = t;       }
    public void setEndTime(LocalTime t)       { this.endTime        = t;       }
    public void setBooked(boolean booked)     { this.isBooked       = booked;  }
    public void setTutorName(String name)     { this.tutorName      = name;    }

    /** Human-readable slot label used in list/table cells. */
    public String getSlotLabel() {
        return date.format(DATE_FMT) + "  " +
               startTime.format(TIME_FMT) + " – " +
               endTime.format(TIME_FMT);
    }

    @Override
    public String toString() { return getSlotLabel(); }
}
