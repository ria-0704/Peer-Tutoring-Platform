package com.peertutor.service;

import com.peertutor.dao.UserDAO;
import com.peertutor.exception.ValidationException;
import com.peertutor.model.User;
import com.peertutor.util.PasswordUtil;

import java.util.Optional;
import java.util.regex.Pattern;

/**
 * Business logic for user registration and authentication.
 */
public class UserService {

    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");

    private final UserDAO userDAO = new UserDAO();

    /**
     * Registers a new student.
     *
     * @return The persisted User with assigned ID.
     * @throws ValidationException if input is invalid or duplicated.
     */
    public User register(String name, String email, String password,
                         String confirmPassword, String studentId)
            throws ValidationException {

        // ── Input validation ──────────────────────────────────────────────
        if (isBlank(name))           throw new ValidationException("Name is required.");
        if (isBlank(email))          throw new ValidationException("Email is required.");
        if (isBlank(password))       throw new ValidationException("Password is required.");
        if (isBlank(studentId))      throw new ValidationException("Student ID is required.");

        if (!EMAIL_PATTERN.matcher(email.trim()).matches())
            throw new ValidationException("Please enter a valid email address.");

        if (password.length() < 6)
            throw new ValidationException("Password must be at least 6 characters.");

        if (!password.equals(confirmPassword))
            throw new ValidationException("Passwords do not match.");

        // ── Duplicate checks ─────────────────────────────────────────────
        if (userDAO.emailExists(email.trim().toLowerCase()))
            throw new ValidationException("An account with this email already exists.");

        if (userDAO.studentIdExists(studentId.trim()))
            throw new ValidationException("An account with this Student ID already exists.");

        // ── Persist ──────────────────────────────────────────────────────
        User user = new User();
        user.setName(name.trim());
        user.setEmail(email.trim().toLowerCase());
        user.setPasswordHash(PasswordUtil.hash(password));
        user.setStudentId(studentId.trim());

        int id = userDAO.createUser(user);
        user.setUserId(id);
        return user;
    }

    /**
     * Authenticates a student.
     *
     * @return The authenticated User.
     * @throws ValidationException on invalid credentials.
     */
    public User login(String email, String password) throws ValidationException {
        if (isBlank(email))    throw new ValidationException("Email is required.");
        if (isBlank(password)) throw new ValidationException("Password is required.");

        Optional<User> opt = userDAO.findByEmail(email.trim().toLowerCase());
        if (opt.isEmpty())
            throw new ValidationException("No account found with this email.");

        User user = opt.get();
        if (!PasswordUtil.verify(password, user.getPasswordHash()))
            throw new ValidationException("Incorrect password. Please try again.");

        return user;
    }

    public int countTutors()   { return userDAO.countTutors();   }

    private boolean isBlank(String s) { return s == null || s.trim().isEmpty(); }
}
