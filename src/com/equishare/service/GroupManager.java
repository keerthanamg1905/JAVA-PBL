package com.equishare.service;

import com.equishare.exception.ResourceNotFoundException;
import com.equishare.exception.ValidationException;
import com.equishare.model.AuditLog;
import com.equishare.model.Group;
import com.equishare.model.User;
import com.equishare.storage.StorageManager;

import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Service managing expense-sharing groups, membership rosters, and join operations.
 */
public class GroupManager {
    private final StorageManager storageManager;
    private final UserManager userManager;
    private final AuditService auditService;
    private final Map<String, Group> groupCache;
    private final AtomicLong idCounter;

    public GroupManager(StorageManager storageManager, UserManager userManager, AuditService auditService) {
        this.storageManager = storageManager;
        this.userManager = userManager;
        this.auditService = auditService;
        this.groupCache = new LinkedHashMap<>(storageManager.loadGroups());

        long maxId = 100;
        for (String id : groupCache.keySet()) {
            if (id.startsWith("GRP-")) {
                try {
                    long val = Long.parseLong(id.substring(4));
                    if (val > maxId) maxId = val;
                } catch (NumberFormatException ignored) {}
            }
        }
        this.idCounter = new AtomicLong(maxId);
    }

    public synchronized Group createGroup(String groupName, String description, String creatorUsername) {
        if (groupName == null || groupName.trim().isEmpty()) {
            throw new ValidationException("Group name cannot be empty.");
        }
        if (creatorUsername == null || !userManager.userExists(creatorUsername)) {
            throw new ValidationException("Invalid creator username: " + creatorUsername);
        }

        String groupId = "GRP-" + idCounter.incrementAndGet();
        Group group = new Group(groupId, groupName, description, creatorUsername);
        groupCache.put(groupId, group);
        storageManager.saveGroups(groupCache);

        // Update user's joined groups
        User creator = userManager.getUserByUsername(creatorUsername);
        if (creator != null) {
            creator.addGroup(groupId);
            userManager.updateUser(creator);
        }

        if (auditService != null) {
            auditService.log(AuditLog.EventType.GROUP_CREATE, creatorUsername, groupId, null,
                    "Group created: " + groupName + " [" + groupId + "]",
                    "Description: " + description);
        }

        return group;
    }

    public synchronized boolean joinGroup(String groupId, String username) {
        if (groupId == null || username == null) {
            throw new ValidationException("Group ID and username are required.");
        }
        String cleanGroupId = groupId.trim().toUpperCase();
        Group group = groupCache.get(cleanGroupId);
        if (group == null) {
            throw new ResourceNotFoundException("Group with ID '" + cleanGroupId + "' not found.");
        }

        User user = userManager.getUserByUsername(username);
        if (user == null) {
            throw new ResourceNotFoundException("User '@" + username + "' not found.");
        }

        if (group.isMember(username)) {
            return false; // Already a member
        }

        group.addMember(username);
        storageManager.saveGroups(groupCache);

        user.addGroup(cleanGroupId);
        userManager.updateUser(user);

        if (auditService != null) {
            auditService.log(AuditLog.EventType.GROUP_JOIN, username, cleanGroupId, null,
                    "User @" + username + " joined group: " + group.getName(),
                    "Group ID: " + cleanGroupId);
        }

        return true;
    }

    public synchronized Group getGroupById(String groupId) {
        if (groupId == null) return null;
        return groupCache.get(groupId.trim().toUpperCase());
    }

    public synchronized List<Group> getGroupsForUser(String username) {
        List<Group> list = new ArrayList<>();
        if (username == null) return list;
        String clean = username.trim().toLowerCase();
        for (Group g : groupCache.values()) {
            if (g.isMember(clean)) {
                list.add(g);
            }
        }
        return list;
    }

    public synchronized Collection<Group> getAllGroups() {
        return Collections.unmodifiableCollection(groupCache.values());
    }
}
