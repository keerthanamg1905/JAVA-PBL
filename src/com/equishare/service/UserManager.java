package com.equishare.service;

import com.equishare.exception.AuthenticationException;
import com.equishare.exception.ValidationException;
import com.equishare.model.AuditLog;
import com.equishare.model.User;
import com.equishare.storage.StorageManager;
import com.equishare.util.SecurityUtils;

import java.util.*;

/**
 * Manages user accounts, credential verification, and user profiles.
 */
public class UserManager {
    private final StorageManager storageManager;
    private final AuditService auditService;
    private final Map<String, User> userCache;

    public UserManager(StorageManager storageManager, AuditService auditService) {
        this.storageManager = storageManager;
        this.auditService = auditService;
        this.userCache = new LinkedHashMap<>(storageManager.loadUsers());
    }

    public synchronized User registerUser(String username, String plainPassword, String fullName, String email) {
        if (username == null || username.trim().isEmpty()) {
            throw new ValidationException("Username cannot be empty.");
        }
        String cleanUsername = username.trim().toLowerCase();
        if (cleanUsername.length() < 3) {
            throw new ValidationException("Username must be at least 3 characters long.");
        }
        if (!cleanUsername.matches("^[a-z0-9_.]+$")) {
            throw new ValidationException("Username can only contain letters, numbers, dots, and underscores.");
        }
        if (userCache.containsKey(cleanUsername)) {
            throw new ValidationException("Username '" + cleanUsername + "' is already registered.");
        }
        if (plainPassword == null || plainPassword.length() < 4) {
            throw new ValidationException("Password must be at least 4 characters long.");
        }

        String passwordHash = SecurityUtils.hashPassword(plainPassword);
        User user = new User(cleanUsername, passwordHash, fullName, email);
        userCache.put(cleanUsername, user);
        storageManager.saveUsers(userCache);

        if (auditService != null) {
            auditService.log(AuditLog.EventType.USER_REGISTER, cleanUsername, null, null,
                    "User registered: @" + cleanUsername, "Full Name: " + fullName);
        }

        return user;
    }

    public synchronized User authenticate(String username, String plainPassword) {
        if (username == null || username.trim().isEmpty()) {
            throw new AuthenticationException("Username is required.");
        }
        String cleanUsername = username.trim().toLowerCase();
        User user = userCache.get(cleanUsername);
        if (user == null) {
            throw new AuthenticationException("User '@" + cleanUsername + "' not found.");
        }
        if (!SecurityUtils.verifyPassword(plainPassword, user.getPasswordHash())) {
            throw new AuthenticationException("Incorrect password for @" + cleanUsername + ".");
        }

        if (auditService != null) {
            auditService.log(AuditLog.EventType.USER_LOGIN, cleanUsername, null, null,
                    "User logged in: @" + cleanUsername, "Success");
        }

        return user;
    }

    public synchronized User getUserByUsername(String username) {
        if (username == null) return null;
        return userCache.get(username.trim().toLowerCase());
    }

    public synchronized boolean userExists(String username) {
        if (username == null) return false;
        return userCache.containsKey(username.trim().toLowerCase());
    }

    public synchronized Collection<User> getAllUsers() {
        return Collections.unmodifiableCollection(userCache.values());
    }

    public synchronized void updateUser(User user) {
        if (user != null && user.getUsername() != null) {
            userCache.put(user.getUsername().toLowerCase(), user);
            storageManager.saveUsers(userCache);
        }
    }
}
