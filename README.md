# Peer Tutoring and Study Session Platform

A complete JavaFX + MySQL desktop application for student-to-student peer tutoring management.

---

## Technology Stack

| Technology | Version |
|---|---|
| Java (JDK) | 21+ (tested on JDK 26) |
| JavaFX | 21.0.4 |
| MySQL | 8.0+ |
| Maven | 3.9+ |
| MySQL JDBC Connector | 8.0.33 |

---

## Prerequisites

1. **JDK 21 or higher** — [Download from Oracle](https://www.oracle.com/java/technologies/downloads/)
2. **Apache Maven 3.9+** — [Download from Apache](https://maven.apache.org/download.cgi)
3. **MySQL Server 8.0+** — [Download from MySQL](https://dev.mysql.com/downloads/)
4. **Git** (optional, for cloning)

---

## Step 1: Create the MySQL Database

1. Open MySQL Workbench or the MySQL command-line client.
2. Connect with your MySQL credentials (usually root).
3. Run the schema script:

```sql
-- From MySQL Workbench: File > Open SQL Script > select the file below
-- Or from command line:
mysql -u root -p < sql/schema.sql
```

This creates the `peer_tutoring` database with all required tables and pre-populates 15 common subjects.

---

## Step 2: Configure Database Credentials

Open the file:
```
src/main/resources/db.properties
```

Update it with your MySQL credentials:

```properties
db.url=jdbc:mysql://localhost:3306/peer_tutoring?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true
db.username=root
db.password=YOUR_MYSQL_PASSWORD_HERE
db.driver=com.mysql.cj.jdbc.Driver
```

---

## Step 3: Build the Project

Open a terminal in the project root directory (`peer-tutoring-platform/`) and run:

```bash
mvn clean compile
```

You should see **BUILD SUCCESS**.

---

## Step 4: Run the Application

**Option A — Using the JavaFX Maven Plugin (recommended):**

```bash
mvn javafx:run
```

**Option B — Run via Java directly after packaging:**

```bash
mvn package -DskipTests
java --module-path target/dependency --add-modules ALL-MODULE-PATH -jar target/peer-tutoring-platform-1.0.0.jar
```

**Option C — From an IDE (IntelliJ IDEA / Eclipse):**
1. Import the project as a Maven project.
2. Set the main class to `com.peertutor.MainApp`.
3. Run directly.

> **Note:** On Windows, if `mvn` is not in your PATH, prepend the Maven bin folder:
> ```powershell
> $env:PATH = "C:\maven\apache-maven-3.9.9\bin;" + $env:PATH
> mvn javafx:run
> ```

---

## Application Features

### Modules Overview

| Module | Description |
|---|---|
| Login & Register | Secure registration and login with salted SHA-256 hashing |
| Dashboard | Welcome screen with stats and upcoming sessions |
| Tutor Profile | Add/remove subjects and descriptions for your tutor profile |
| Availability | Add and manage time slots for tutoring |
| Find Tutors | Search tutors by name or subject, view availability, book sessions |
| Book a Session | View pending booking requests |
| My Sessions | Manage all sessions (as student and tutor) |
| Reviews | View received reviews and submit reviews for completed sessions |

---

## Project Structure

```
peer-tutoring-platform/
├── pom.xml                                    Maven build file
├── README.md                                  This file
├── sql/
│   └── schema.sql                             Database creation script
├── src/main/
│   ├── java/com/peertutor/
│   │   ├── MainApp.java                       Entry point
│   │   ├── model/                             Data model classes
│   │   │   ├── User.java
│   │   │   ├── Subject.java
│   │   │   ├── Skill.java
│   │   │   ├── Availability.java
│   │   │   ├── Session.java
│   │   │   └── Review.java
│   │   ├── dao/                               Database access layer
│   │   │   ├── BaseDAO.java
│   │   │   ├── UserDAO.java
│   │   │   ├── SubjectDAO.java
│   │   │   ├── SkillDAO.java
│   │   │   ├── AvailabilityDAO.java
│   │   │   ├── SessionDAO.java
│   │   │   └── ReviewDAO.java
│   │   ├── service/                           Business logic layer
│   │   │   ├── UserService.java
│   │   │   ├── TutorService.java
│   │   │   ├── AvailabilityService.java
│   │   │   ├── SessionService.java
│   │   │   └── ReviewService.java
│   │   ├── controller/                        JavaFX UI controllers
│   │   │   ├── NavigationManager.java
│   │   │   ├── MainLayoutController.java
│   │   │   ├── LoginController.java
│   │   │   ├── RegisterController.java
│   │   │   ├── DashboardController.java
│   │   │   ├── TutorProfileController.java
│   │   │   ├── AvailabilityController.java
│   │   │   ├── FindTutorsController.java
│   │   │   ├── BookingController.java
│   │   │   ├── MySessionsController.java
│   │   │   └── ReviewController.java
│   │   ├── exception/
│   │   │   ├── DatabaseException.java
│   │   │   └── ValidationException.java
│   │   └── util/
│   │       ├── DBConnection.java
│   │       ├── PasswordUtil.java
│   │       ├── AlertUtil.java
│   │       └── SessionManager.java
│   └── resources/
│       ├── db.properties                      Database configuration
│       └── com/peertutor/css/
│           └── styles.css                     JavaFX stylesheet
```

---

## Testing Guide

### Scenario 1: Student Registration

1. Launch the application.
2. Click "Create Account" on the login screen.
3. Fill in: Name, Email (university format), Student ID, Password (6+ chars), Confirm Password.
4. Click "Create Account".
5. **Expected:** Success message; redirected to login.

### Scenario 2: Duplicate Registration Prevention

1. Try to register again with the same email.
2. **Expected:** Error — "An account with this email already exists."
3. Try with same Student ID but different email.
4. **Expected:** Error — "An account with this Student ID already exists."

### Scenario 3: Login

1. Enter registered email and password.
2. Click "Sign In".
3. **Expected:** Dashboard with welcome message.
4. Try wrong password.
5. **Expected:** Error — "Incorrect password."

### Scenario 4: Create Tutor Profile

1. Log in and click "My Tutor Profile" in the sidebar.
2. Select a subject from the dropdown.
3. Optionally type a description.
4. Click "Add to Profile".
5. **Expected:** Subject appears in the table below.

### Scenario 5: Add a New Subject

1. On the Tutor Profile page, type a new subject name in the text field.
2. Click "Add Subject".
3. **Expected:** New subject appears in the dropdown.

### Scenario 6: Add Availability

1. Click "My Availability" in the sidebar.
2. Set a future date, start time (e.g. 10:00), end time (e.g. 11:00).
3. Click "Add Slot".
4. **Expected:** Slot appears in the table with "Available" status.
5. Try a past date.
6. **Expected:** Error — "Cannot add availability for a past date."

### Scenario 7: Search for Tutors

1. Click "Find Tutors" in the sidebar.
2. All tutors with profiles appear as cards.
3. Type a subject name (e.g. "Mathematics") and click "Search".
4. **Expected:** Only tutors who teach that subject appear.
5. Click "Show All" to reset.

### Scenario 8: Request a Session

1. On "Find Tutors", expand a tutor's availability by clicking "View Availability".
2. Click "Request Session" on a slot.
3. Confirm the booking dialog.
4. **Expected:** Success message; slot status changes in tutor's availability.
5. Go to "Book a Session" to see the pending request.

### Scenario 9: Self-Booking Prevention

1. Try to request a session on your own availability slot.
2. **Expected:** Button is disabled with "Your own slot" text.

### Scenario 10: Approve / Reject Session (as Tutor)

1. Log in with the tutor's account.
2. Click "My Sessions".
3. Under "Sessions I'm Teaching", find the PENDING request.
4. Click "Approve" or "Reject".
5. **Expected:** Status updates immediately.

### Scenario 11: Cancel a Session

1. As a student, go to "My Sessions".
2. Find an APPROVED session.
3. Click "Cancel".
4. **Expected:** Status changes to CANCELLED; slot becomes available again.

### Scenario 12: Complete a Session

1. As the tutor, go to "My Sessions".
2. Find an APPROVED session (past the scheduled time is ideal).
3. Click "Mark Complete".
4. **Expected:** Status changes to COMPLETED.

### Scenario 13: Submit a Review

1. As the student, go to "My Sessions".
2. Find a COMPLETED session.
3. Click "Leave Review".
4. Select a rating (1–5) and optionally write a comment.
5. Click "Submit Review".
6. **Expected:** Success message.
7. Try to review the same session again.
8. **Expected:** Message — "You have already submitted a review."

### Scenario 14: View Reviews

1. Click "Reviews" in the sidebar.
2. **Expected:** Average rating, total count, and all reviews received are displayed.

### Scenario 15: Duplicate Booking Prevention

1. Have two students try to book the same slot.
2. **Expected:** Only one succeeds; the second gets "This slot has already been booked."

---

## Known Limitations

1. No real-time notifications — students must manually check session status updates.
2. Password recovery is not implemented (out of scope for this mini-project).
3. The application uses a single shared database connection (suitable for single-user desktop use).
4. Date/time are stored and shown in the server's local timezone.
5. No export or print functionality for session records.

---

## Troubleshooting

| Problem | Solution |
|---|---|
| "Cannot connect to database" | Verify MySQL is running and credentials in db.properties are correct |
| "db.properties not found" | Ensure it is in src/main/resources/ and the project was compiled |
| JavaFX window doesn't appear | Ensure JDK 21+ is used; check Maven is using the correct JDK |
| "Access denied for user" | Check db.username and db.password in db.properties |

---

*UCSXXX Enterprise Web Application — Academic Mini-Project*
