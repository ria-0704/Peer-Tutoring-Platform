package com.peertutor.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

/**
 * Represents a tutoring session booking.
 */
public class Session {

    public enum Status { PENDING, APPROVED, COMPLETED, CANCELLED, REJECTED }

    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("HH:mm");
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd MMM yyyy");

    private int           sessionId;
    private int           studentId;
    private int           tutorId;
    private int           availabilityId;
    private Status        status;
    private LocalDateTime createdAt;

    // Convenience fields from JOINs
    private String    studentName;
    private String    tutorName;
    private LocalDate sessionDate;
    private LocalTime startTime;
    private LocalTime endTime;

    public Session() {}

    public Session(int sessionId, int studentId, int tutorId,
                   int availabilityId, Status status, LocalDateTime createdAt) {
        this.sessionId      = sessionId;
        this.studentId      = studentId;
        this.tutorId        = tutorId;
        this.availabilityId = availabilityId;
        this.status         = status;
        this.createdAt      = createdAt;
    }

    public int           getSessionId()      { return sessionId;      }
    public int           getStudentId()      { return studentId;      }
    public int           getTutorId()        { return tutorId;        }
    public int           getAvailabilityId() { return availabilityId; }
    public Status        getStatus()         { return status;         }
    public LocalDateTime getCreatedAt()      { return createdAt;      }
    public String        getStudentName()    { return studentName;    }
    public String        getTutorName()      { return tutorName;      }
    public LocalDate     getSessionDate()    { return sessionDate;    }
    public LocalTime     getStartTime()      { return startTime;      }
    public LocalTime     getEndTime()        { return endTime;        }

    public void setSessionId(int id)              { this.sessionId      = id;      }
    public void setStudentId(int id)              { this.studentId      = id;      }
    public void setTutorId(int id)                { this.tutorId        = id;      }
    public void setAvailabilityId(int id)         { this.availabilityId = id;      }
    public void setStatus(Status status)          { this.status         = status;  }
    public void setCreatedAt(LocalDateTime dt)    { this.createdAt      = dt;      }
    public void setStudentName(String name)       { this.studentName    = name;    }
    public void setTutorName(String name)         { this.tutorName      = name;    }
    public void setSessionDate(LocalDate date)    { this.sessionDate    = date;    }
    public void setStartTime(LocalTime t)         { this.startTime      = t;       }
    public void setEndTime(LocalTime t)           { this.endTime        = t;       }

    public String getDateTimeLabel() {
        if (sessionDate == null) return "—";
        return sessionDate.format(DATE_FMT) + "  " +
               (startTime != null ? startTime.format(TIME_FMT) : "") + " – " +
               (endTime   != null ? endTime.format(TIME_FMT)   : "");
    }

    @Override
    public String toString() {
        return "Session{id=" + sessionId + ", status=" + status + "}";
    }
}
