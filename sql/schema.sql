-- =============================================================
-- Peer Tutoring and Study Session Platform - Database Schema
-- MySQL 8.0+
-- =============================================================

CREATE DATABASE IF NOT EXISTS peer_tutoring
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE peer_tutoring;

-- -------------------------------------------------------------
-- Table: users
-- -------------------------------------------------------------
CREATE TABLE IF NOT EXISTS users (
    user_id       INT          NOT NULL AUTO_INCREMENT,
    name          VARCHAR(100) NOT NULL,
    email         VARCHAR(150) NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    student_id    VARCHAR(50)  NOT NULL,
    created_at    TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT pk_users   PRIMARY KEY (user_id),
    CONSTRAINT uq_email   UNIQUE (email),
    CONSTRAINT uq_sid     UNIQUE (student_id)
) ENGINE=InnoDB;

-- -------------------------------------------------------------
-- Table: subjects
-- -------------------------------------------------------------
CREATE TABLE IF NOT EXISTS subjects (
    subject_id   INT         NOT NULL AUTO_INCREMENT,
    subject_name VARCHAR(100) NOT NULL,
    CONSTRAINT pk_subjects   PRIMARY KEY (subject_id),
    CONSTRAINT uq_subject    UNIQUE (subject_name)
) ENGINE=InnoDB;

-- Pre-populate common subjects
INSERT IGNORE INTO subjects (subject_name) VALUES
    ('Mathematics'),
    ('Physics'),
    ('Chemistry'),
    ('Biology'),
    ('Computer Science'),
    ('Data Structures'),
    ('Algorithms'),
    ('Database Systems'),
    ('Operating Systems'),
    ('Networking'),
    ('English'),
    ('Statistics'),
    ('Calculus'),
    ('Linear Algebra'),
    ('Economics');

-- -------------------------------------------------------------
-- Table: skills  (tutor profile + subjects they can teach)
-- -------------------------------------------------------------
CREATE TABLE IF NOT EXISTS skills (
    skill_id    INT  NOT NULL AUTO_INCREMENT,
    user_id     INT  NOT NULL,
    subject_id  INT  NOT NULL,
    description TEXT,
    CONSTRAINT pk_skills         PRIMARY KEY (skill_id),
    CONSTRAINT fk_skills_user    FOREIGN KEY (user_id)    REFERENCES users    (user_id) ON DELETE CASCADE,
    CONSTRAINT fk_skills_subject FOREIGN KEY (subject_id) REFERENCES subjects (subject_id) ON DELETE CASCADE,
    CONSTRAINT uq_skill          UNIQUE (user_id, subject_id)
) ENGINE=InnoDB;

CREATE INDEX idx_skills_user    ON skills (user_id);
CREATE INDEX idx_skills_subject ON skills (subject_id);

-- -------------------------------------------------------------
-- Table: availability
-- -------------------------------------------------------------
CREATE TABLE IF NOT EXISTS availability (
    availability_id INT      NOT NULL AUTO_INCREMENT,
    tutor_id        INT      NOT NULL,
    avail_date      DATE     NOT NULL,
    start_time      TIME     NOT NULL,
    end_time        TIME     NOT NULL,
    is_booked       TINYINT  NOT NULL DEFAULT 0,
    CONSTRAINT pk_availability       PRIMARY KEY (availability_id),
    CONSTRAINT fk_avail_tutor        FOREIGN KEY (tutor_id) REFERENCES users (user_id) ON DELETE CASCADE,
    CONSTRAINT chk_time_order        CHECK (end_time > start_time)
) ENGINE=InnoDB;

CREATE INDEX idx_avail_tutor ON availability (tutor_id);
CREATE INDEX idx_avail_date  ON availability (avail_date);

-- -------------------------------------------------------------
-- Table: sessions
-- -------------------------------------------------------------
-- NOTE: No UNIQUE constraint on availability_id here.
-- Duplicate booking is prevented by the is_booked flag on the availability
-- table combined with a SELECT ... FOR UPDATE lock in SessionDAO.bookSession().
-- A UNIQUE constraint would permanently block rebooking after a cancellation
-- or rejection, because cancelled/rejected session rows remain in this table.
CREATE TABLE IF NOT EXISTS sessions (
    session_id      INT       NOT NULL AUTO_INCREMENT,
    student_id      INT       NOT NULL,
    tutor_id        INT       NOT NULL,
    availability_id INT       NOT NULL,
    status          ENUM('PENDING','APPROVED','COMPLETED','CANCELLED','REJECTED') NOT NULL DEFAULT 'PENDING',
    created_at      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT pk_sessions        PRIMARY KEY (session_id),
    CONSTRAINT fk_sess_student    FOREIGN KEY (student_id)      REFERENCES users        (user_id)         ON DELETE CASCADE,
    CONSTRAINT fk_sess_tutor      FOREIGN KEY (tutor_id)        REFERENCES users        (user_id)         ON DELETE CASCADE,
    CONSTRAINT fk_sess_avail      FOREIGN KEY (availability_id) REFERENCES availability (availability_id) ON DELETE CASCADE
) ENGINE=InnoDB;

CREATE INDEX idx_sess_student ON sessions (student_id);
CREATE INDEX idx_sess_tutor   ON sessions (tutor_id);
CREATE INDEX idx_sess_status  ON sessions (status);

-- -------------------------------------------------------------
-- Table: reviews
-- -------------------------------------------------------------
CREATE TABLE IF NOT EXISTS reviews (
    review_id  INT       NOT NULL AUTO_INCREMENT,
    session_id INT       NOT NULL,
    rating     TINYINT   NOT NULL,
    comment    TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT pk_reviews        PRIMARY KEY (review_id),
    CONSTRAINT fk_review_session FOREIGN KEY (session_id) REFERENCES sessions (session_id) ON DELETE CASCADE,
    CONSTRAINT uq_review_session UNIQUE (session_id),
    CONSTRAINT chk_rating        CHECK (rating BETWEEN 1 AND 5)
) ENGINE=InnoDB;

-- =============================================================
-- End of schema
-- =============================================================
