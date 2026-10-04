package com.equishare.model;

import java.io.Serializable;

/**
 * Participant represents a group member's consolidated financial balance in a ledger.
 * Upgraded from the original prototype to support fair share calculation,
 * status checks, and formatted ledger reporting.
 */
public class Participant implements Serializable, Comparable<Participant> {
    private static final long serialVersionUID = 1L;

    private String username;
    private String name;
    private double amountPaid;
    private double fairShare;
    private double netBalance;

    public Participant(String name) {
        this(name, name);
    }

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

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getAmountPaid() {
        return amountPaid;
    }

    public void setAmountPaid(double amountPaid) {
        this.amountPaid = amountPaid;
        recalculateNet();
    }

    public double getFairShare() {
        return fairShare;
    }

    public void setFairShare(double fairShare) {
        this.fairShare = fairShare;
        recalculateNet();
    }

    public double getNetBalance() {
        return netBalance;
    }

    public void setNetBalance(double netBalance) {
        this.netBalance = netBalance;
    }

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

    public boolean isCreditor() {
        return netBalance > 0.009;
    }

    public boolean isDebtor() {
        return netBalance < -0.009;
    }

    public boolean isSettled() {
        return !isCreditor() && !isDebtor();
    }

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
        return Double.compare(other.netBalance, this.netBalance); // Descending by net balance
    }

    @Override
    public String toString() {
        return String.format("%-15s | Paid: ₹%-8.2f | Share: ₹%-8.2f | %s",
                name, amountPaid, fairShare, getStatusDescription());
    }
}
