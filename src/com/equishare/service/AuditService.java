package com.equishare.service;

import com.equishare.model.*;
import com.equishare.storage.StorageManager;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Enterprise-grade Audit Engine upgrading the original AuditUtility.
 * Maintains immutable logs of all mutations, runs mathematical zero-sum
 * invariant verifications, and generates audit reports.
 */
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

        public boolean isPassed() {
            return passed;
        }

        public double getTotalExpenses() {
            return totalExpenses;
        }

        public double getTotalPaid() {
            return totalPaid;
        }

        public double getTotalFairShares() {
            return totalFairShares;
        }

        public double getNetBalanceSum() {
            return netBalanceSum;
        }

        public List<String> getDetails() {
            return details;
        }

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

    /**
     * Preserves legacy AuditUtility capability from the original prototype.
     */
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

    public synchronized List<AuditLog> getAllLogs() {
        return new ArrayList<>(auditLogs);
    }

    public synchronized List<AuditLog> getLogsForGroup(String groupId) {
        List<AuditLog> list = new ArrayList<>();
        for (AuditLog l : auditLogs) {
            if (groupId != null && groupId.equalsIgnoreCase(l.getTargetGroupId())) {
                list.add(l);
            }
        }
        return list;
    }

    /**
     * Performs strict mathematical verification checks:
     * 1. Total expenses == Total amounts paid
     * 2. Total expenses == Total allocated shares
     * 3. Sum of participant net balances == 0.00 (Zero-Sum Invariant)
     */
    public IntegrityResult verifyLedgerIntegrity(String groupId, List<Expense> expenses, List<Participant> participants) {
        double totalExpense = 0.0;
        for (Expense e : expenses) {
            totalExpense += e.getTotalAmount();
        }

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

        // Log this audit verification action
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
