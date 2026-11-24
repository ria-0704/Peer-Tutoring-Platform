package com.peertutor.util;

import com.peertutor.model.User;

/**
 * Holds the currently logged-in user for the application session.
 * Acts as a simple in-memory singleton (no persistence needed for desktop app).
 */
public final class SessionManager {

    private static User currentUser;

    private SessionManager() {}

    public static void login(User user)   { currentUser = user; }
    public static void logout()           { currentUser = null; }
    public static User getCurrentUser()   { return currentUser; }
    public static boolean isLoggedIn()    { return currentUser != null; }
}
