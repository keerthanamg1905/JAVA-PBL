package com.equishare.service;

import com.equishare.model.User;

/**
 * Manages the current authenticated user session in the application.
 */
public class AuthenticationManager {
    private final UserManager userManager;
    private User currentUser;

    public AuthenticationManager(UserManager userManager) {
        this.userManager = userManager;
        this.currentUser = null;
    }

    public User login(String username, String password) {
        User user = userManager.authenticate(username, password);
        this.currentUser = user;
        return user;
    }

    public void logout() {
        this.currentUser = null;
    }

    public boolean isLoggedIn() {
        return currentUser != null;
    }

    public User getCurrentUser() {
        return currentUser;
    }

    public void setCurrentUser(User user) {
        this.currentUser = user;
    }
}
