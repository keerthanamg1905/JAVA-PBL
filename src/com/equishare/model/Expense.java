package com.equishare.model;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * Abstract Base Class for all Expenses.
 * Demonstrates abstraction and polymorphism. Subclasses define specific
 * split calculation logic (e.g. GroupEqualSplit).
 */
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
    protected Map<String, Double> splitMap; // username -> split amount

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

    /**
     * Polymorphic method implemented by subclasses to calculate
     * exact shares per participant involved in this expense.
     */
    public abstract void calculateSplits();

    /**
     * Returns a human-readable identifier of the split strategy.
     */
    public abstract String getSplitType();

    public String getExpenseId() {
        return expenseId;
    }

    public void setExpenseId(String expenseId) {
        this.expenseId = expenseId;
    }

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public double getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(double totalAmount) {
        this.totalAmount = Math.max(0.0, totalAmount);
        calculateSplits();
    }

    public String getPayerUsername() {
        return payerUsername;
    }

    public void setPayerUsername(String payerUsername) {
        this.payerUsername = payerUsername;
    }

    public List<String> getInvolvedUsernames() {
        return Collections.unmodifiableList(involvedUsernames);
    }

    public void setInvolvedUsernames(List<String> involvedUsernames) {
        this.involvedUsernames = involvedUsernames != null ? new ArrayList<>(involvedUsernames) : new ArrayList<>();
        calculateSplits();
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public Category getCategory() {
        return category;
    }

    public void setCategory(Category category) {
        this.category = category != null ? category : Category.OTHER;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public String getCreatedByUsername() {
        return createdByUsername;
    }

    public void setCreatedByUsername(String createdByUsername) {
        this.createdByUsername = createdByUsername;
    }

    public Map<String, Double> getSplitMap() {
        if (splitMap == null || splitMap.isEmpty()) {
            calculateSplits();
        }
        return Collections.unmodifiableMap(splitMap);
    }

    public double getShareForUser(String username) {
        if (splitMap == null || splitMap.isEmpty()) {
            calculateSplits();
        }
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
