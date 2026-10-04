package com.equishare.model;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Represents an actionable settlement instruction between a debtor and a creditor.
 * Replaces unstructured console loops with an encapsulated domain entity.
 */
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

    public String getDebtorUsername() {
        return debtorUsername;
    }

    public String getDebtorName() {
        return debtorName;
    }

    public String getCreditorUsername() {
        return creditorUsername;
    }

    public String getCreditorName() {
        return creditorName;
    }

    public double getAmount() {
        return amount;
    }

    public boolean isSettled() {
        return settled;
    }

    public void setSettled(boolean settled) {
        this.settled = settled;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public String getInstruction() {
        return String.format("%s transfers ₹%.2f to %s", debtorName, amount, creditorName);
    }

    @Override
    public String toString() {
        return String.format("➔ %-12s shall transfer ₹%-8.2f to %-12s %s",
                debtorName, amount, creditorName, settled ? "[PAID]" : "[PENDING]");
    }
}
