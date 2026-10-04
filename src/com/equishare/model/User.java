package com.equishare.model;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * User represents a registered participant in EquiShare Pro.
 * Adheres to encapsulation with private fields, validation, and defensive copies.
 */
public class User implements Serializable {
    private static final long serialVersionUID = 1L;

    private String username;
    private String passwordHash;
    private String fullName;
    private String email;
    private String createdAt;
    private Set<String> joinedGroupIds;

    public User() {
        this.joinedGroupIds = new LinkedHashSet<>();
        this.createdAt = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
    }

    public User(String username, String passwordHash, String fullName, String email) {
        this.username = username != null ? username.trim().toLowerCase() : "";
        this.passwordHash = passwordHash != null ? passwordHash : "";
        this.fullName = fullName != null ? fullName.trim() : "";
        this.email = email != null ? email.trim().toLowerCase() : "";
        this.joinedGroupIds = new LinkedHashSet<>();
        this.createdAt = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username != null ? username.trim().toLowerCase() : "";
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName != null ? fullName.trim() : "";
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email != null ? email.trim().toLowerCase() : "";
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public Set<String> getJoinedGroupIds() {
        return Collections.unmodifiableSet(joinedGroupIds);
    }

    public void addGroup(String groupId) {
        if (groupId != null && !groupId.trim().isEmpty()) {
            this.joinedGroupIds.add(groupId.trim());
        }
    }

    public void removeGroup(String groupId) {
        if (groupId != null) {
            this.joinedGroupIds.remove(groupId.trim());
        }
    }

    public void setJoinedGroupIds(Set<String> groupIds) {
        this.joinedGroupIds = groupIds != null ? new LinkedHashSet<>(groupIds) : new LinkedHashSet<>();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return username != null && username.equals(user.username);
    }

    @Override
    public int hashCode() {
        return username != null ? username.hashCode() : 0;
    }

    @Override
    public String toString() {
        return fullName + " (@" + username + ")";
    }
}
