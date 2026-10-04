package com.equishare.model;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Group represents a collective expense-sharing group (e.g. Trip, Flatmates, Event).
 */
public class Group implements Serializable {
    private static final long serialVersionUID = 1L;

    private String groupId;
    private String name;
    private String description;
    private String creatorUsername;
    private Set<String> memberUsernames;
    private String createdAt;

    public Group() {
        this.memberUsernames = new LinkedHashSet<>();
        this.createdAt = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
    }

    public Group(String groupId, String name, String description, String creatorUsername) {
        this.groupId = groupId != null ? groupId.trim().toUpperCase() : "";
        this.name = name != null ? name.trim() : "";
        this.description = description != null ? description.trim() : "";
        this.creatorUsername = creatorUsername != null ? creatorUsername.trim().toLowerCase() : "";
        this.memberUsernames = new LinkedHashSet<>();
        if (!this.creatorUsername.isEmpty()) {
            this.memberUsernames.add(this.creatorUsername);
        }
        this.createdAt = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
    }

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId != null ? groupId.trim().toUpperCase() : "";
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCreatorUsername() {
        return creatorUsername;
    }

    public void setCreatorUsername(String creatorUsername) {
        this.creatorUsername = creatorUsername;
    }

    public Set<String> getMemberUsernames() {
        return Collections.unmodifiableSet(memberUsernames);
    }

    public void setMemberUsernames(Set<String> memberUsernames) {
        this.memberUsernames = memberUsernames != null ? new LinkedHashSet<>(memberUsernames) : new LinkedHashSet<>();
    }

    public boolean addMember(String username) {
        if (username != null && !username.trim().isEmpty()) {
            return this.memberUsernames.add(username.trim().toLowerCase());
        }
        return false;
    }

    public boolean removeMember(String username) {
        if (username != null && !username.equalsIgnoreCase(creatorUsername)) {
            return this.memberUsernames.remove(username.trim().toLowerCase());
        }
        return false;
    }

    public boolean isMember(String username) {
        return username != null && this.memberUsernames.contains(username.trim().toLowerCase());
    }

    public int getMemberCount() {
        return memberUsernames.size();
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Group group = (Group) o;
        return groupId != null && groupId.equals(group.groupId);
    }

    @Override
    public int hashCode() {
        return groupId != null ? groupId.hashCode() : 0;
    }

    @Override
    public String toString() {
        return name + " [" + groupId + "] (" + memberUsernames.size() + " members)";
    }
}
