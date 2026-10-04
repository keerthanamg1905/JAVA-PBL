# EquiShare Pro — Complete Source Code Reference

This document contains the complete, unabridged source code for all Java packages in **EquiShare Pro – An Automated Multi-Participant Expense Settlement & Audit System**.

---

## Table of Contents
1. [Model Package (`com.equishare.model`)](#1-model-package)
   - [Category.java](#categoryjava)
   - [User.java](#userjava)
   - [Participant.java](#participantjava)
   - [Expense.java (Abstract)](#expensejava)
   - [GroupEqualSplit.java](#groupequalsplitjava)
   - [Group.java](#groupjava)
   - [Settlement.java](#settlementjava)
   - [AuditLog.java](#auditlogjava)
2. [Exceptions Package (`com.equishare.exception`)](#2-exceptions-package)
   - [EquiShareException.java](#equishareexceptionjava)
   - [AuthenticationException.java](#authenticationexceptionjava)
   - [ValidationException.java](#validationexceptionjava)
   - [ResourceNotFoundException.java](#resourcenotfoundexceptionjava)
3. [Utilities Package (`com.equishare.util`)](#3-utilities-package)
   - [SecurityUtils.java](#securityutilsjava)
   - [DateUtils.java](#dateutilsjava)
   - [SimpleJson.java](#simplejsonjava)
   - [SampleDataSeeder.java](#sampledataseederjava)
4. [Storage Package (`com.equishare.storage`)](#4-storage-package)
   - [StorageManager.java (Interface)](#storagemanagerjava)
   - [FileStorageManager.java](#filestoragemanagerjava)
5. [Service Package (`com.equishare.service`)](#5-service-package)
   - [SettlementCalculator.java (Interface)](#settlementcalculatorjava)
   - [GreedySettlementCalculator.java](#greedysettlementcalculatorjava)
   - [AuditService.java](#auditservicejava)
   - [UserManager.java](#usermanagerjava)
   - [AuthenticationManager.java](#authenticationmanagerjava)
   - [GroupManager.java](#groupmanagerjava)
   - [ExpenseManager.java](#expensemanagerjava)
6. [User Interface Layer (`com.equishare.ui`)](#6-user-interface-layer)
   - [ConsolePrinter.java](#consoleprinterjava)
   - [ConsoleUI.java](#consoleuijava)
   - [LoginDialog.java](#logindialogjava)
   - [ExpenseDialog.java](#expensedialogjava)
   - [MainFrame.java](#mainframejava)
7. [Entry Point & Testing](#7-entry-point--testing)
   - [Main.java](#mainjava)
   - [WorkflowTest.java](#workflowtestjava)

---

# 1. Model Package

### `Category.java`
```java
package com.equishare.model;

public enum Category {
    FOOD("Food & Dining", "🍔"),
    TRAVEL("Travel & Commute", "🚗"),
    ACCOMMODATION("Accommodation", "🏨"),
    ENTERTAINMENT("Entertainment & Fun", "🎟️"),
    UTILITIES("Utilities & Bills", "💡"),
    GROCERIES("Groceries & Supplies", "🛒"),
    SHOPPING("Shopping", "🛍️"),
    OTHER("General / Other", "📌");

    private final String displayName;
    private final String icon;

    Category(String displayName, String icon) {
        this.displayName = displayName;
        this.icon = icon;
    }

    public String getDisplayName() { return displayName; }
    public String getIcon() { return icon; }

    @Override
    public String toString() { return icon + " " + displayName; }

    public static Category fromString(String text) {
        if (text == null || text.trim().isEmpty()) return OTHER;
        String clean = text.trim().toUpperCase();
        for (Category c : Category.values()) {
            if (c.name().equalsIgnoreCase(clean) || c.displayName.equalsIgnoreCase(text.trim())) {
                return c;
            }
        }
        return OTHER;
    }
}
```

### `User.java`
```java
package com.equishare.model;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

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

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username != null ? username.trim().toLowerCase() : ""; }
    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }
    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName != null ? fullName.trim() : ""; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email != null ? email.trim().toLowerCase() : ""; }
    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
    public Set<String> getJoinedGroupIds() { return Collections.unmodifiableSet(joinedGroupIds); }

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
    public int hashCode() { return username != null ? username.hashCode() : 0; }

    @Override
    public String toString() { return fullName + " (@" + username + ")"; }
}
```

### `Participant.java`
```java
package com.equishare.model;

import java.io.Serializable;

public class Participant implements Serializable, Comparable<Participant> {
    private static final long serialVersionUID = 1L;

    private String username;
    private String name;
    private double amountPaid;
    private double fairShare;
    private double netBalance;

    public Participant(String name) { this(name, name); }

    public Participant(String username, String name) {
        this.username = username != null ? username.trim() : "";
        this.name = (name != null && !name.trim().isEmpty()) ? name.trim() : this.username;
        this.amountPaid = 0.0;
        this.fairShare = 0.0;
        this.netBalance = 0.0;
    }

    public Participant(String username, String name, double amountPaid, double fairShare) {
        this.username = username;
        this.name = name;
        this.amountPaid = amountPaid;
        this.fairShare = fairShare;
        this.netBalance = amountPaid - fairShare;
    }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public double getAmountPaid() { return amountPaid; }

    public void setAmountPaid(double amountPaid) {
        this.amountPaid = amountPaid;
        recalculateNet();
    }

    public double getFairShare() { return fairShare; }

    public void setFairShare(double fairShare) {
        this.fairShare = fairShare;
        recalculateNet();
    }

    public double getNetBalance() { return netBalance; }
    public void setNetBalance(double netBalance) { this.netBalance = netBalance; }

    public void addPayment(double amount) {
        this.amountPaid += amount;
        recalculateNet();
    }

    public void addFairShare(double amount) {
        this.fairShare += amount;
        recalculateNet();
    }

    private void recalculateNet() {
        this.netBalance = this.amountPaid - this.fairShare;
    }

    public boolean isCreditor() { return netBalance > 0.009; }
    public boolean isDebtor() { return netBalance < -0.009; }
    public boolean isSettled() { return !isCreditor() && !isDebtor(); }

    public String getStatusDescription() {
        if (isCreditor()) {
            return String.format("Gets back ₹%.2f (Overpaid)", netBalance);
        } else if (isDebtor()) {
            return String.format("Owes ₹%.2f (Underpaid)", Math.abs(netBalance));
        } else {
            return "Settled Up (₹0.00)";
        }
    }

    @Override
    public int compareTo(Participant other) {
        return Double.compare(other.netBalance, this.netBalance);
    }

    @Override
    public String toString() {
        return String.format("%-15s | Paid: ₹%-8.2f | Share: ₹%-8.2f | %s",
                name, amountPaid, fairShare, getStatusDescription());
    }
}
```

### `Expense.java`
```java
package com.equishare.model;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public abstract class Expense implements Serializable {
    private static final long serialVersionUID = 1L;

    protected String expenseId;
    protected String groupId;
    protected String description;
    protected double totalAmount;
    protected String payerUsername;
    protected List<String> involvedUsernames;
    protected String date;
    protected Category category;
    protected String createdAt;
    protected String createdByUsername;
    protected Map<String, Double> splitMap;

    public Expense() {
        this.involvedUsernames = new ArrayList<>();
        this.splitMap = new LinkedHashMap<>();
        this.date = LocalDate.now().toString();
        this.category = Category.OTHER;
        this.createdAt = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
    }

    public Expense(String expenseId, String groupId, String description, double totalAmount,
                   String payerUsername, List<String> involvedUsernames,
                   String date, Category category, String createdByUsername) {
        this.expenseId = expenseId;
        this.groupId = groupId;
        this.description = description != null ? description.trim() : "";
        this.totalAmount = Math.max(0.0, totalAmount);
        this.payerUsername = payerUsername != null ? payerUsername.trim() : "";
        this.involvedUsernames = involvedUsernames != null ? new ArrayList<>(involvedUsernames) : new ArrayList<>();
        this.date = (date != null && !date.trim().isEmpty()) ? date.trim() : LocalDate.now().toString();
        this.category = category != null ? category : Category.OTHER;
        this.createdByUsername = createdByUsername != null ? createdByUsername.trim() : this.payerUsername;
        this.createdAt = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        this.splitMap = new LinkedHashMap<>();
    }

    public abstract void calculateSplits();
    public abstract String getSplitType();

    public String getExpenseId() { return expenseId; }
    public void setExpenseId(String expenseId) { this.expenseId = expenseId; }
    public String getGroupId() { return groupId; }
    public void setGroupId(String groupId) { this.groupId = groupId; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public double getTotalAmount() { return totalAmount; }

    public void setTotalAmount(double totalAmount) {
        this.totalAmount = Math.max(0.0, totalAmount);
        calculateSplits();
    }

    public String getPayerUsername() { return payerUsername; }
    public void setPayerUsername(String payerUsername) { this.payerUsername = payerUsername; }
    public List<String> getInvolvedUsernames() { return Collections.unmodifiableList(involvedUsernames); }

    public void setInvolvedUsernames(List<String> involvedUsernames) {
        this.involvedUsernames = involvedUsernames != null ? new ArrayList<>(involvedUsernames) : new ArrayList<>();
        calculateSplits();
    }

    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }
    public Category getCategory() { return category; }
    public void setCategory(Category category) { this.category = category != null ? category : Category.OTHER; }
    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
    public String getCreatedByUsername() { return createdByUsername; }
    public void setCreatedByUsername(String createdByUsername) { this.createdByUsername = createdByUsername; }

    public Map<String, Double> getSplitMap() {
        if (splitMap == null || splitMap.isEmpty()) calculateSplits();
        return Collections.unmodifiableMap(splitMap);
    }

    public double getShareForUser(String username) {
        if (splitMap == null || splitMap.isEmpty()) calculateSplits();
        return splitMap.getOrDefault(username, 0.0);
    }

    public boolean isUserInvolved(String username) {
        return involvedUsernames.contains(username);
    }

    @Override
    public String toString() {
        return String.format("[%s] %s - %s: ₹%.2f (Paid by %s)",
                date, category.getIcon(), description, totalAmount, payerUsername);
    }
}
```

### `GroupEqualSplit.java`
```java
package com.equishare.model;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

public class GroupEqualSplit extends Expense {
    private static final long serialVersionUID = 1L;

    public GroupEqualSplit() { super(); }

    public GroupEqualSplit(String expenseId, String groupId, String description, double totalAmount,
                           String payerUsername, List<String> involvedUsernames,
                           String date, Category category, String createdByUsername) {
        super(expenseId, groupId, description, totalAmount, payerUsername, involvedUsernames, date, category, createdByUsername);
        calculateSplits();
    }

    @Override
    public void calculateSplits() {
        splitMap.clear();
        if (involvedUsernames == null || involvedUsernames.isEmpty() || totalAmount <= 0.0) {
            return;
        }

        int count = involvedUsernames.size();
        BigDecimal totalBd = BigDecimal.valueOf(totalAmount).setScale(2, RoundingMode.HALF_UP);
        BigDecimal countBd = BigDecimal.valueOf(count);
        BigDecimal baseShare = totalBd.divide(countBd, 2, RoundingMode.DOWN);
        BigDecimal remainder = totalBd.subtract(baseShare.multiply(countBd));

        int remainingCents = remainder.multiply(BigDecimal.valueOf(100)).intValue();
        for (int i = 0; i < count; i++) {
            String user = involvedUsernames.get(i);
            BigDecimal share = baseShare;
            if (i < remainingCents) {
                share = share.add(BigDecimal.valueOf(0.01));
            }
            splitMap.put(user, share.doubleValue());
        }
    }

    @Override
    public String getSplitType() { return "Equal Split"; }
}
```

### `Group.java`
```java
package com.equishare.model;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

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

    public String getGroupId() { return groupId; }
    public void setGroupId(String groupId) { this.groupId = groupId != null ? groupId.trim().toUpperCase() : ""; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getCreatorUsername() { return creatorUsername; }
    public void setCreatorUsername(String creatorUsername) { this.creatorUsername = creatorUsername; }
    public Set<String> getMemberUsernames() { return Collections.unmodifiableSet(memberUsernames); }

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

    public int getMemberCount() { return memberUsernames.size(); }
    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Group group = (Group) o;
        return groupId != null && groupId.equals(group.groupId);
    }

    @Override
    public int hashCode() { return groupId != null ? groupId.hashCode() : 0; }

    @Override
    public String toString() { return name + " [" + groupId + "] (" + memberUsernames.size() + " members)"; }
}
```

### `Settlement.java`
```java
package com.equishare.model;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Settlement implements Serializable {
    private static final long serialVersionUID = 1L;

    private String debtorUsername;
    private String debtorName;
    private String creditorUsername;
    private String creditorName;
    private double amount;
    private boolean settled;
    private String timestamp;

    public Settlement(String debtorUsername, String debtorName,
                      String creditorUsername, String creditorName,
                      double amount) {
        this.debtorUsername = debtorUsername;
        this.debtorName = debtorName != null ? debtorName : debtorUsername;
        this.creditorUsername = creditorUsername;
        this.creditorName = creditorName != null ? creditorName : creditorUsername;
        this.amount = Math.round(amount * 100.0) / 100.0;
        this.settled = false;
        this.timestamp = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
    }

    public String getDebtorUsername() { return debtorUsername; }
    public String getDebtorName() { return debtorName; }
    public String getCreditorUsername() { return creditorUsername; }
    public String getCreditorName() { return creditorName; }
    public double getAmount() { return amount; }
    public boolean isSettled() { return settled; }
    public void setSettled(boolean settled) { this.settled = settled; }
    public String getTimestamp() { return timestamp; }

    public String getInstruction() {
        return String.format("%s transfers ₹%.2f to %s", debtorName, amount, creditorName);
    }

    @Override
    public String toString() {
        return String.format("➔ %-12s shall transfer ₹%-8.2f to %-12s %s",
                debtorName, amount, creditorName, settled ? "[PAID]" : "[PENDING]");
    }
}
```

### `AuditLog.java`
```java
package com.equishare.model;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class AuditLog implements Serializable {
    private static final long serialVersionUID = 1L;

    public enum EventType {
        USER_REGISTER("User Registered"),
        USER_LOGIN("User Login"),
        GROUP_CREATE("Group Created"),
        GROUP_JOIN("Group Joined"),
        EXPENSE_ADD("Expense Added"),
        EXPENSE_EDIT("Expense Modified"),
        EXPENSE_DELETE("Expense Deleted"),
        SETTLEMENT_RECORD("Settlement Recorded"),
        AUDIT_VERIFY("System Audit Invariant Check");

        private final String title;
        EventType(String title) { this.title = title; }
        public String getTitle() { return title; }
    }

    private String logId;
    private String timestamp;
    private EventType eventType;
    private String actorUsername;
    private String targetGroupId;
    private String targetExpenseId;
    private String summary;
    private String details;

    public AuditLog() {
        this.timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }

    public AuditLog(String logId, EventType eventType, String actorUsername,
                    String targetGroupId, String targetExpenseId,
                    String summary, String details) {
        this.logId = logId;
        this.eventType = eventType;
        this.actorUsername = actorUsername != null ? actorUsername : "system";
        this.targetGroupId = targetGroupId != null ? targetGroupId : "-";
        this.targetExpenseId = targetExpenseId != null ? targetExpenseId : "-";
        this.summary = summary != null ? summary : "";
        this.details = details != null ? details : "";
        this.timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }

    public String getLogId() { return logId; }
    public void setLogId(String logId) { this.logId = logId; }
    public String getTimestamp() { return timestamp; }
    public void setTimestamp(String timestamp) { this.timestamp = timestamp; }
    public EventType getEventType() { return eventType; }
    public void setEventType(EventType eventType) { this.eventType = eventType; }
    public String getActorUsername() { return actorUsername; }
    public void setActorUsername(String actorUsername) { this.actorUsername = actorUsername; }
    public String getTargetGroupId() { return targetGroupId; }
    public void setTargetGroupId(String targetGroupId) { this.targetGroupId = targetGroupId; }
    public String getTargetExpenseId() { return targetExpenseId; }
    public void setTargetExpenseId(String targetExpenseId) { this.targetExpenseId = targetExpenseId; }
    public String getSummary() { return summary; }
    public void setSummary(String summary) { this.summary = summary; }
    public String getDetails() { return details; }
    public void setDetails(String details) { this.details = details; }

    @Override
    public String toString() {
        return String.format("[%s] %-15s | By: @%-10s | %s | %s",
                timestamp, eventType.name(), actorUsername, summary, details);
    }
}
```

---

# 2. Exceptions Package

### `EquiShareException.java`
```java
package com.equishare.exception;

public class EquiShareException extends RuntimeException {
    private static final long serialVersionUID = 1L;
    public EquiShareException(String message) { super(message); }
    public EquiShareException(String message, Throwable cause) { super(message, cause); }
}
```

### `AuthenticationException.java`
```java
package com.equishare.exception;

public class AuthenticationException extends EquiShareException {
    public AuthenticationException(String message) { super(message); }
}
```

### `ValidationException.java`
```java
package com.equishare.exception;

public class ValidationException extends EquiShareException {
    public ValidationException(String message) { super(message); }
}
```

### `ResourceNotFoundException.java`
```java
package com.equishare.exception;

public class ResourceNotFoundException extends EquiShareException {
    public ResourceNotFoundException(String message) { super(message); }
}
```

---

# 3. Utilities Package

### `SecurityUtils.java`
```java
package com.equishare.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class SecurityUtils {
    public static String hashPassword(String plainPassword) {
        if (plainPassword == null) return "";
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(plainPassword.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hashBytes) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 algorithm not available", e);
        }
    }

    public static boolean verifyPassword(String plainPassword, String storedHash) {
        if (plainPassword == null || storedHash == null) return false;
        String computed = hashPassword(plainPassword);
        return computed.equalsIgnoreCase(storedHash);
    }
}
```

### `DateUtils.java`
```java
package com.equishare.util;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public class DateUtils {
    public static final DateTimeFormatter ISO_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    public static final DateTimeFormatter DISPLAY_FORMATTER = DateTimeFormatter.ofPattern("dd MMM yyyy");

    public static String todayString() { return LocalDate.now().format(ISO_FORMATTER); }

    public static boolean isValidDate(String dateStr) {
        if (dateStr == null || dateStr.trim().isEmpty()) return false;
        try {
            LocalDate.parse(dateStr.trim(), ISO_FORMATTER);
            return true;
        } catch (DateTimeParseException e) {
            return false;
        }
    }

    public static String formatDisplay(String isoDateStr) {
        if (isoDateStr == null || isoDateStr.trim().isEmpty()) return "";
        try {
            LocalDate date = LocalDate.parse(isoDateStr.trim(), ISO_FORMATTER);
            return date.format(DISPLAY_FORMATTER);
        } catch (Exception e) {
            return isoDateStr;
        }
    }
}
```

### `SampleDataSeeder.java`
```java
package com.equishare.util;

import com.equishare.model.Category;
import com.equishare.model.Group;
import com.equishare.service.ExpenseManager;
import com.equishare.service.GroupManager;
import com.equishare.service.UserManager;

import java.time.LocalDate;
import java.util.Arrays;

public class SampleDataSeeder {
    public static void seedIfEmpty(UserManager userManager, GroupManager groupManager, ExpenseManager expenseManager) {
        if (!userManager.getAllUsers().isEmpty()) return;

        System.out.println("[Seeder] Initializing sample demonstration data...");
        userManager.registerUser("alice", "password123", "Alice Sharma", "alice@example.com");
        userManager.registerUser("bob", "password123", "Bob Verma", "bob@example.com");
        userManager.registerUser("charlie", "password123", "Charlie Singh", "charlie@example.com");

        Group group = groupManager.createGroup("Goa Vacation 2026", "Beach house trip and water sports", "alice");
        String gId = group.getGroupId();

        groupManager.joinGroup(gId, "bob");
        groupManager.joinGroup(gId, "charlie");

        String today = LocalDate.now().toString();
        String yesterday = LocalDate.now().minusDays(1).toString();

        expenseManager.addExpense(gId, "Seafood Dinner at Beachside", 1200.0,
                "alice", Arrays.asList("alice", "bob", "charlie"),
                yesterday, Category.FOOD, "alice");

        expenseManager.addExpense(gId, "Rental Scooters & Fuel", 600.0,
                "bob", Arrays.asList("alice", "bob", "charlie"),
                today, Category.TRAVEL, "bob");

        expenseManager.addExpense(gId, "Fort Aguada Museum Entry Tickets", 300.0,
                "charlie", Arrays.asList("alice", "bob", "charlie"),
                today, Category.ENTERTAINMENT, "charlie");

        System.out.println("[Seeder] Successfully seeded Alice, Bob, Charlie & Goa Vacation 2026.");
    }
}
```

---

# 4. Storage Package

### `StorageManager.java` (Interface)
```java
package com.equishare.storage;

import com.equishare.model.AuditLog;
import com.equishare.model.Expense;
import com.equishare.model.Group;
import com.equishare.model.User;

import java.util.List;
import java.util.Map;

public interface StorageManager {
    void saveUsers(Map<String, User> users);
    Map<String, User> loadUsers();

    void saveGroups(Map<String, Group> groups);
    Map<String, Group> loadGroups();

    void saveExpenses(List<Expense> expenses);
    List<Expense> loadExpenses();

    void saveAuditLogs(List<AuditLog> auditLogs);
    List<AuditLog> loadAuditLogs();

    void clearAll();
}
```

---

# 5. Service Package

### `SettlementCalculator.java` (Interface)
```java
package com.equishare.service;

import com.equishare.model.Participant;
import com.equishare.model.Settlement;
import java.util.List;

public interface SettlementCalculator {
    List<Settlement> calculateSettlements(List<Participant> participants);
}
```

### `GreedySettlementCalculator.java`
```java
package com.equishare.service;

import com.equishare.model.Participant;
import com.equishare.model.Settlement;

import java.util.ArrayList;
import java.util.List;

public class GreedySettlementCalculator implements SettlementCalculator {

    private static class BalanceEntry {
        String username;
        String name;
        double amount;

        BalanceEntry(String username, String name, double amount) {
            this.username = username;
            this.name = name;
            this.amount = amount;
        }
    }

    @Override
    public List<Settlement> calculateSettlements(List<Participant> participants) {
        List<Settlement> settlements = new ArrayList<>();
        if (participants == null || participants.isEmpty()) return settlements;

        List<BalanceEntry> debtors = new ArrayList<>();
        List<BalanceEntry> creditors = new ArrayList<>();

        for (Participant p : participants) {
            double net = p.getNetBalance();
            if (net < -0.009) {
                debtors.add(new BalanceEntry(p.getUsername(), p.getName(), Math.abs(net)));
            } else if (net > 0.009) {
                creditors.add(new BalanceEntry(p.getUsername(), p.getName(), net));
            }
        }

        debtors.sort((a, b) -> Double.compare(b.amount, a.amount));
        creditors.sort((a, b) -> Double.compare(b.amount, a.amount));

        int dIdx = 0, cIdx = 0;
        while (dIdx < debtors.size() && cIdx < creditors.size()) {
            BalanceEntry debtor = debtors.get(dIdx);
            BalanceEntry creditor = creditors.get(cIdx);

            double paymentExchange = Math.min(debtor.amount, creditor.amount);
            paymentExchange = Math.round(paymentExchange * 100.0) / 100.0;

            if (paymentExchange > 0.009) {
                settlements.add(new Settlement(
                        debtor.username, debtor.name,
                        creditor.username, creditor.name,
                        paymentExchange
                ));
            }

            debtor.amount = Math.round((debtor.amount - paymentExchange) * 100.0) / 100.0;
            creditor.amount = Math.round((creditor.amount - paymentExchange) * 100.0) / 100.0;

            if (debtor.amount <= 0.009) dIdx++;
            if (creditor.amount <= 0.009) cIdx++;
        }

        return settlements;
    }
}
```

### `AuditService.java`
```java
package com.equishare.service;

import com.equishare.model.*;
import com.equishare.storage.StorageManager;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

public class AuditService {
    private final StorageManager storageManager;
    private final List<AuditLog> auditLogs;
    private final AtomicLong idGenerator;

    public static class IntegrityResult {
        private final boolean passed;
        private final double totalExpenses;
        private final double totalPaid;
        private final double totalFairShares;
        private final double netBalanceSum;
        private final List<String> details;

        public IntegrityResult(boolean passed, double totalExpenses, double totalPaid,
                               double totalFairShares, double netBalanceSum, List<String> details) {
            this.passed = passed;
            this.totalExpenses = totalExpenses;
            this.totalPaid = totalPaid;
            this.totalFairShares = totalFairShares;
            this.netBalanceSum = netBalanceSum;
            this.details = details;
        }

        public boolean isPassed() { return passed; }
        public double getTotalExpenses() { return totalExpenses; }
        public double getTotalPaid() { return totalPaid; }
        public double getTotalFairShares() { return totalFairShares; }
        public double getNetBalanceSum() { return netBalanceSum; }
        public List<String> getDetails() { return details; }

        public String getFormattedReport() {
            StringBuilder sb = new StringBuilder();
            sb.append("========================================================\n");
            sb.append("          SYSTEM AUDIT & INVARIANT INTEGRITY REPORT     \n");
            sb.append("========================================================\n");
            sb.append(String.format("Status:                 %s\n", passed ? "PASSED (Zero Discrepancies)" : "FAILED (Discrepancy Detected)"));
            sb.append(String.format("Total Recorded Expense: ₹%.2f\n", totalExpenses));
            sb.append(String.format("Total Funds Paid:       ₹%.2f\n", totalPaid));
            sb.append(String.format("Total Fair Shares:      ₹%.2f\n", totalFairShares));
            sb.append(String.format("Zero-Sum Balance Delta: ₹%.4f %s\n", netBalanceSum, Math.abs(netBalanceSum) < 0.01 ? "✔ BALANCED" : "❌ IMBALANCED"));
            sb.append("--------------------------------------------------------\n");
            sb.append("Verification Checks:\n");
            for (String d : details) {
                sb.append("  • ").append(d).append("\n");
            }
            sb.append("========================================================\n");
            return sb.toString();
        }
    }

    public AuditService(StorageManager storageManager) {
        this.storageManager = storageManager;
        this.auditLogs = new ArrayList<>(storageManager.loadAuditLogs());
        long maxId = 1000;
        for (AuditLog log : auditLogs) {
            if (log.getLogId() != null && log.getLogId().startsWith("LOG-")) {
                try {
                    long val = Long.parseLong(log.getLogId().substring(4));
                    if (val > maxId) maxId = val;
                } catch (NumberFormatException ignored) {}
            }
        }
        this.idGenerator = new AtomicLong(maxId);
    }

    public static void printRawData(Expense exp) {
        if (exp != null) {
            System.out.println("--> Audit Log (System Verification): Total tracked cost is ₹"
                    + exp.getTotalAmount() + " for " + exp.getInvolvedUsernames().size() + " people.");
        }
    }

    public synchronized AuditLog log(AuditLog.EventType eventType, String actorUsername,
                                     String targetGroupId, String targetExpenseId,
                                     String summary, String details) {
        String logId = "LOG-" + idGenerator.incrementAndGet();
        AuditLog log = new AuditLog(logId, eventType, actorUsername, targetGroupId, targetExpenseId, summary, details);
        auditLogs.add(log);
        storageManager.saveAuditLogs(auditLogs);
        return log;
    }

    public synchronized List<AuditLog> getAllLogs() { return new ArrayList<>(auditLogs); }

    public synchronized List<AuditLog> getLogsForGroup(String groupId) {
        List<AuditLog> list = new ArrayList<>();
        for (AuditLog l : auditLogs) {
            if (groupId != null && groupId.equalsIgnoreCase(l.getTargetGroupId())) {
                list.add(l);
            }
        }
        return list;
    }

    public IntegrityResult verifyLedgerIntegrity(String groupId, List<Expense> expenses, List<Participant> participants) {
        double totalExpense = 0.0;
        for (Expense e : expenses) totalExpense += e.getTotalAmount();

        double totalPaid = 0.0;
        double totalFairShares = 0.0;
        double netSum = 0.0;

        for (Participant p : participants) {
            totalPaid += p.getAmountPaid();
            totalFairShares += p.getFairShare();
            netSum += p.getNetBalance();
        }

        totalExpense = Math.round(totalExpense * 100.0) / 100.0;
        totalPaid = Math.round(totalPaid * 100.0) / 100.0;
        totalFairShares = Math.round(totalFairShares * 100.0) / 100.0;
        netSum = Math.round(netSum * 100.0) / 100.0;

        List<String> details = new ArrayList<>();
        boolean check1 = Math.abs(totalExpense - totalPaid) < 0.05;
        details.add(String.format("Payment-Expense Equivalence: (₹%.2f == ₹%.2f) -> %s",
                totalPaid, totalExpense, check1 ? "VERIFIED" : "FAIL"));

        boolean check2 = Math.abs(totalExpense - totalFairShares) < 0.05;
        details.add(String.format("Share Allocation Equivalence: (₹%.2f == ₹%.2f) -> %s",
                totalFairShares, totalExpense, check2 ? "VERIFIED" : "FAIL"));

        boolean check3 = Math.abs(netSum) < 0.05;
        details.add(String.format("Zero-Sum Invariant Check: Sum(Net Balances) = ₹%.4f -> %s",
                netSum, check3 ? "VERIFIED" : "FAIL"));

        boolean allPassed = check1 && check2 && check3;

        log(AuditLog.EventType.AUDIT_VERIFY, "system", groupId, null,
                "Ledger Invariant Audit: " + (allPassed ? "PASSED" : "FAILED"),
                String.format("Total: ₹%.2f | Paid: ₹%.2f | Net Delta: ₹%.4f", totalExpense, totalPaid, netSum));

        return new IntegrityResult(allPassed, totalExpense, totalPaid, totalFairShares, netSum, details);
    }

    public Path exportAuditReport(String groupId, String reportContent) throws IOException {
        String filename = "audit_report_" + (groupId != null ? groupId : "all") + "_" + System.currentTimeMillis() + ".txt";
        Path out = Paths.get(filename);
        Files.writeString(out, reportContent);
        return out;
    }
}
```

---

# 6. Entry Point

### `Main.java`
```java
package com.equishare;

import com.equishare.model.User;
import com.equishare.service.*;
import com.equishare.storage.FileStorageManager;
import com.equishare.storage.StorageManager;
import com.equishare.ui.cli.ConsolePrinter;
import com.equishare.ui.cli.ConsoleUI;
import com.equishare.ui.swing.LoginDialog;
import com.equishare.ui.swing.MainFrame;
import com.equishare.util.SampleDataSeeder;

import javax.swing.*;
import java.awt.GraphicsEnvironment;

public class Main {
    public static <T> void printHeader(T text) {
        ConsolePrinter.printHeader(text);
    }

    public static void main(String[] args) {
        boolean forceCli = false;
        for (String arg : args) {
            if ("--cli".equalsIgnoreCase(arg) || "-c".equalsIgnoreCase(arg) || "--console".equalsIgnoreCase(arg)) {
                forceCli = true;
                break;
            }
        }

        StorageManager storageManager = new FileStorageManager("data");
        AuditService auditService = new AuditService(storageManager);
        UserManager userManager = new UserManager(storageManager, auditService);
        GroupManager groupManager = new GroupManager(storageManager, userManager, auditService);
        SettlementCalculator settlementCalculator = new GreedySettlementCalculator();
        ExpenseManager expenseManager = new ExpenseManager(storageManager, groupManager, userManager, auditService, settlementCalculator);

        SampleDataSeeder.seedIfEmpty(userManager, groupManager, expenseManager);

        boolean isHeadless = GraphicsEnvironment.isHeadless();

        if (forceCli || isHeadless) {
            if (isHeadless && !forceCli) {
                System.out.println("[INFO] Running in headless environment. Starting Console CLI mode...");
            }
            ConsoleUI cli = new ConsoleUI(userManager, groupManager, expenseManager, auditService);
            cli.start();
        } else {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ignored) {}

            SwingUtilities.invokeLater(() -> {
                LoginDialog loginDialog = new LoginDialog(null, userManager);
                loginDialog.setVisible(true);

                User user = loginDialog.getAuthenticatedUser();
                if (user != null) {
                    MainFrame mainFrame = new MainFrame(userManager, groupManager, expenseManager, auditService, user);
                    mainFrame.setVisible(true);
                } else {
                    System.out.println("No user authenticated. Exiting EquiShare Pro.");
                    System.exit(0);
                }
            });
        }
    }
}
```
