package com.equishare.model;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Immutable audit record capturing all state mutations, actor identities,
 * timestamps, and verification events.
 */
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

        EventType(String title) {
            this.title = title;
        }

        public String getTitle() {
            return title;
        }
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

    public String getLogId() {
        return logId;
    }

    public void setLogId(String logId) {
        this.logId = logId;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public EventType getEventType() {
        return eventType;
    }

    public void setEventType(EventType eventType) {
        this.eventType = eventType;
    }

    public String getActorUsername() {
        return actorUsername;
    }

    public void setActorUsername(String actorUsername) {
        this.actorUsername = actorUsername;
    }

    public String getTargetGroupId() {
        return targetGroupId;
    }

    public void setTargetGroupId(String targetGroupId) {
        this.targetGroupId = targetGroupId;
    }

    public String getTargetExpenseId() {
        return targetExpenseId;
    }

    public void setTargetExpenseId(String targetExpenseId) {
        this.targetExpenseId = targetExpenseId;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public String getDetails() {
        return details;
    }

    public void setDetails(String details) {
        this.details = details;
    }

    @Override
    public String toString() {
        return String.format("[%s] %-15s | By: @%-10s | %s | %s",
                timestamp, eventType.name(), actorUsername, summary, details);
    }
}
