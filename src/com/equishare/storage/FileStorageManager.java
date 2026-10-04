package com.equishare.storage;

import com.equishare.model.*;
import com.equishare.util.SimpleJson;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

/**
 * File-based implementation of StorageManager using human-readable JSON files.
 * Provides reliable offline persistence with zero external dependencies.
 */
public class FileStorageManager implements StorageManager {
    private final Path dataDir;
    private final Path usersFile;
    private final Path groupsFile;
    private final Path expensesFile;
    private final Path auditFile;

    public FileStorageManager() {
        this("data");
    }

    public FileStorageManager(String directoryPath) {
        this.dataDir = Paths.get(directoryPath);
        this.usersFile = dataDir.resolve("users.json");
        this.groupsFile = dataDir.resolve("groups.json");
        this.expensesFile = dataDir.resolve("expenses.json");
        this.auditFile = dataDir.resolve("audit_logs.json");
        ensureDirectoryExists();
    }

    private void ensureDirectoryExists() {
        try {
            if (!Files.exists(dataDir)) {
                Files.createDirectories(dataDir);
            }
        } catch (IOException e) {
            System.err.println("Warning: Could not create data directory: " + e.getMessage());
        }
    }

    private void writeString(Path path, String content) {
        try {
            ensureDirectoryExists();
            Files.writeString(path, content, StandardCharsets.UTF_8);
        } catch (IOException e) {
            System.err.println("Error saving to " + path + ": " + e.getMessage());
        }
    }

    private String readString(Path path) {
        try {
            if (!Files.exists(path)) {
                return null;
            }
            return Files.readString(path, StandardCharsets.UTF_8);
        } catch (IOException e) {
            System.err.println("Error reading from " + path + ": " + e.getMessage());
            return null;
        }
    }

    // ==========================================
    // USERS
    // ==========================================
    @Override
    public void saveUsers(Map<String, User> users) {
        List<Map<String, Object>> list = new ArrayList<>();
        for (User u : users.values()) {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("username", u.getUsername());
            map.put("passwordHash", u.getPasswordHash());
            map.put("fullName", u.getFullName());
            map.put("email", u.getEmail());
            map.put("createdAt", u.getCreatedAt());
            map.put("joinedGroupIds", new ArrayList<>(u.getJoinedGroupIds()));
            list.add(map);
        }
        writeString(usersFile, SimpleJson.stringify(list));
    }

    @Override
    @SuppressWarnings("unchecked")
    public Map<String, User> loadUsers() {
        Map<String, User> map = new LinkedHashMap<>();
        String json = readString(usersFile);
        if (json == null || json.trim().isEmpty()) return map;

        try {
            Object parsed = SimpleJson.parse(json);
            if (parsed instanceof List) {
                List<?> list = (List<?>) parsed;
                for (Object item : list) {
                    if (item instanceof Map) {
                        Map<String, Object> m = (Map<String, Object>) item;
                        User u = new User();
                        u.setUsername((String) m.get("username"));
                        u.setPasswordHash((String) m.get("passwordHash"));
                        u.setFullName((String) m.get("fullName"));
                        u.setEmail((String) m.get("email"));
                        u.setCreatedAt((String) m.get("createdAt"));
                        Object groupsObj = m.get("joinedGroupIds");
                        if (groupsObj instanceof List) {
                            for (Object g : (List<?>) groupsObj) {
                                if (g != null) u.addGroup(g.toString());
                            }
                        }
                        if (u.getUsername() != null && !u.getUsername().isEmpty()) {
                            map.put(u.getUsername(), u);
                        }
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Error parsing users file: " + e.getMessage());
        }
        return map;
    }

    // ==========================================
    // GROUPS
    // ==========================================
    @Override
    public void saveGroups(Map<String, Group> groups) {
        List<Map<String, Object>> list = new ArrayList<>();
        for (Group g : groups.values()) {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("groupId", g.getGroupId());
            map.put("name", g.getName());
            map.put("description", g.getDescription());
            map.put("creatorUsername", g.getCreatorUsername());
            map.put("createdAt", g.getCreatedAt());
            map.put("memberUsernames", new ArrayList<>(g.getMemberUsernames()));
            list.add(map);
        }
        writeString(groupsFile, SimpleJson.stringify(list));
    }

    @Override
    @SuppressWarnings("unchecked")
    public Map<String, Group> loadGroups() {
        Map<String, Group> map = new LinkedHashMap<>();
        String json = readString(groupsFile);
        if (json == null || json.trim().isEmpty()) return map;

        try {
            Object parsed = SimpleJson.parse(json);
            if (parsed instanceof List) {
                for (Object item : (List<?>) parsed) {
                    if (item instanceof Map) {
                        Map<String, Object> m = (Map<String, Object>) item;
                        Group g = new Group();
                        g.setGroupId((String) m.get("groupId"));
                        g.setName((String) m.get("name"));
                        g.setDescription((String) m.get("description"));
                        g.setCreatorUsername((String) m.get("creatorUsername"));
                        g.setCreatedAt((String) m.get("createdAt"));
                        Object membersObj = m.get("memberUsernames");
                        if (membersObj instanceof List) {
                            for (Object member : (List<?>) membersObj) {
                                if (member != null) g.addMember(member.toString());
                            }
                        }
                        if (g.getGroupId() != null && !g.getGroupId().isEmpty()) {
                            map.put(g.getGroupId(), g);
                        }
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Error parsing groups file: " + e.getMessage());
        }
        return map;
    }

    // ==========================================
    // EXPENSES
    // ==========================================
    @Override
    public void saveExpenses(List<Expense> expenses) {
        List<Map<String, Object>> list = new ArrayList<>();
        for (Expense e : expenses) {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("expenseId", e.getExpenseId());
            map.put("groupId", e.getGroupId());
            map.put("description", e.getDescription());
            map.put("totalAmount", e.getTotalAmount());
            map.put("payerUsername", e.getPayerUsername());
            map.put("involvedUsernames", new ArrayList<>(e.getInvolvedUsernames()));
            map.put("date", e.getDate());
            map.put("category", e.getCategory().name());
            map.put("createdAt", e.getCreatedAt());
            map.put("createdByUsername", e.getCreatedByUsername());
            map.put("splitType", e.getSplitType());
            list.add(map);
        }
        writeString(expensesFile, SimpleJson.stringify(list));
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<Expense> loadExpenses() {
        List<Expense> list = new ArrayList<>();
        String json = readString(expensesFile);
        if (json == null || json.trim().isEmpty()) return list;

        try {
            Object parsed = SimpleJson.parse(json);
            if (parsed instanceof List) {
                for (Object item : (List<?>) parsed) {
                    if (item instanceof Map) {
                        Map<String, Object> m = (Map<String, Object>) item;
                        String expenseId = (String) m.get("expenseId");
                        String groupId = (String) m.get("groupId");
                        String desc = (String) m.get("description");
                        double amount = 0.0;
                        if (m.get("totalAmount") instanceof Number) {
                            amount = ((Number) m.get("totalAmount")).doubleValue();
                        }
                        String payer = (String) m.get("payerUsername");
                        List<String> involved = new ArrayList<>();
                        Object involvedObj = m.get("involvedUsernames");
                        if (involvedObj instanceof List) {
                            for (Object u : (List<?>) involvedObj) {
                                if (u != null) involved.add(u.toString());
                            }
                        }
                        String date = (String) m.get("date");
                        Category cat = Category.fromString((String) m.get("category"));
                        String createdBy = (String) m.get("createdByUsername");
                        String createdAt = (String) m.get("createdAt");

                        // Create concrete GroupEqualSplit instance
                        GroupEqualSplit expense = new GroupEqualSplit(
                                expenseId, groupId, desc, amount, payer, involved, date, cat, createdBy
                        );
                        if (createdAt != null) {
                            expense.setCreatedAt(createdAt);
                        }
                        list.add(expense);
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Error parsing expenses file: " + e.getMessage());
        }
        return list;
    }

    // ==========================================
    // AUDIT LOGS
    // ==========================================
    @Override
    public void saveAuditLogs(List<AuditLog> auditLogs) {
        List<Map<String, Object>> list = new ArrayList<>();
        for (AuditLog a : auditLogs) {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("logId", a.getLogId());
            map.put("timestamp", a.getTimestamp());
            map.put("eventType", a.getEventType() != null ? a.getEventType().name() : "");
            map.put("actorUsername", a.getActorUsername());
            map.put("targetGroupId", a.getTargetGroupId());
            map.put("targetExpenseId", a.getTargetExpenseId());
            map.put("summary", a.getSummary());
            map.put("details", a.getDetails());
            list.add(map);
        }
        writeString(auditFile, SimpleJson.stringify(list));
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<AuditLog> loadAuditLogs() {
        List<AuditLog> list = new ArrayList<>();
        String json = readString(auditFile);
        if (json == null || json.trim().isEmpty()) return list;

        try {
            Object parsed = SimpleJson.parse(json);
            if (parsed instanceof List) {
                for (Object item : (List<?>) parsed) {
                    if (item instanceof Map) {
                        Map<String, Object> m = (Map<String, Object>) item;
                        AuditLog log = new AuditLog();
                        log.setLogId((String) m.get("logId"));
                        log.setTimestamp((String) m.get("timestamp"));
                        String evTypeStr = (String) m.get("eventType");
                        if (evTypeStr != null) {
                            try {
                                log.setEventType(AuditLog.EventType.valueOf(evTypeStr));
                            } catch (Exception ignored) {}
                        }
                        log.setActorUsername((String) m.get("actorUsername"));
                        log.setTargetGroupId((String) m.get("targetGroupId"));
                        log.setTargetExpenseId((String) m.get("targetExpenseId"));
                        log.setSummary((String) m.get("summary"));
                        log.setDetails((String) m.get("details"));
                        list.add(log);
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Error parsing audit logs file: " + e.getMessage());
        }
        return list;
    }

    @Override
    public void clearAll() {
        try {
            Files.deleteIfExists(usersFile);
            Files.deleteIfExists(groupsFile);
            Files.deleteIfExists(expensesFile);
            Files.deleteIfExists(auditFile);
        } catch (IOException e) {
            System.err.println("Error clearing storage: " + e.getMessage());
        }
    }
}
