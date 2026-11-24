package com.peertutor.model;

import java.time.LocalDateTime;

/**
 * Represents a registered student/user in the platform.
 */
public class User {

    private int    userId;
    private String name;
    private String email;
    private String passwordHash;
    private String studentId;
    private LocalDateTime createdAt;

    public User() {}

    public User(int userId, String name, String email, String passwordHash,
                String studentId, LocalDateTime createdAt) {
        this.userId       = userId;
        this.name         = name;
        this.email        = email;
        this.passwordHash = passwordHash;
        this.studentId    = studentId;
        this.createdAt    = createdAt;
    }

    // ── Getters ──────────────────────────────────────────────────────────────

    public int           getUserId()       { return userId;       }
    public String        getName()         { return name;         }
    public String        getEmail()        { return email;        }
    public String        getPasswordHash() { return passwordHash; }
    public String        getStudentId()    { return studentId;    }
    public LocalDateTime getCreatedAt()    { return createdAt;    }

    // ── Setters ──────────────────────────────────────────────────────────────

    public void setUserId(int userId)             { this.userId       = userId;       }
    public void setName(String name)              { this.name         = name;         }
    public void setEmail(String email)            { this.email        = email;        }
    public void setPasswordHash(String hash)      { this.passwordHash = hash;         }
    public void setStudentId(String studentId)    { this.studentId    = studentId;    }
    public void setCreatedAt(LocalDateTime dt)    { this.createdAt    = dt;           }

    @Override
    public String toString() {
        return "User{id=" + userId + ", name='" + name + "', email='" + email + "'}";
    }
}
