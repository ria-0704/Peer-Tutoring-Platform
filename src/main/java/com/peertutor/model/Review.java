package com.peertutor.model;

import java.time.LocalDateTime;

/**
 * Represents a review submitted after a completed session.
 */
public class Review {

    private int           reviewId;
    private int           sessionId;
    private int           rating;
    private String        comment;
    private LocalDateTime createdAt;

    // Convenience fields
    private String reviewerName;
    private String tutorName;

    public Review() {}

    public Review(int reviewId, int sessionId, int rating,
                  String comment, LocalDateTime createdAt) {
        this.reviewId  = reviewId;
        this.sessionId = sessionId;
        this.rating    = rating;
        this.comment   = comment;
        this.createdAt = createdAt;
    }

    public int           getReviewId()     { return reviewId;     }
    public int           getSessionId()    { return sessionId;    }
    public int           getRating()       { return rating;       }
    public String        getComment()      { return comment;      }
    public LocalDateTime getCreatedAt()    { return createdAt;    }
    public String        getReviewerName() { return reviewerName; }
    public String        getTutorName()    { return tutorName;    }

    public void setReviewId(int id)             { this.reviewId     = id;    }
    public void setSessionId(int id)            { this.sessionId    = id;    }
    public void setRating(int rating)           { this.rating       = rating; }
    public void setComment(String comment)      { this.comment      = comment; }
    public void setCreatedAt(LocalDateTime dt)  { this.createdAt    = dt;    }
    public void setReviewerName(String name)    { this.reviewerName = name;  }
    public void setTutorName(String name)       { this.tutorName    = name;  }

    /** Returns a star string like "★★★★☆" for the rating. */
    public String getStars() {
        StringBuilder sb = new StringBuilder();
        for (int i = 1; i <= 5; i++) {
            sb.append(i <= rating ? "★" : "☆");
        }
        return sb.toString();
    }

    @Override
    public String toString() {
        return "Review{id=" + reviewId + ", rating=" + rating + "}";
    }
}
