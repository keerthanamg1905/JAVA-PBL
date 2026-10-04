package com.equishare.service;

import com.equishare.exception.ResourceNotFoundException;
import com.equishare.exception.ValidationException;
import com.equishare.model.*;
import com.equishare.storage.StorageManager;

import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Core engine managing expenses, itemized allocations, ledger recalculations,
 * and simplified greedy debt settlements.
 */
public class ExpenseManager {
    private final StorageManager storageManager;
    private final GroupManager groupManager;
    private final UserManager userManager;
    private final AuditService auditService;
    private final SettlementCalculator settlementCalculator;
    private final List<Expense> expenseCache;
    private final AtomicLong idCounter;

    public ExpenseManager(StorageManager storageManager, GroupManager groupManager,
                          UserManager userManager, AuditService auditService,
                          SettlementCalculator settlementCalculator) {
        this.storageManager = storageManager;
        this.groupManager = groupManager;
        this.userManager = userManager;
        this.auditService = auditService;
        this.settlementCalculator = settlementCalculator != null ? settlementCalculator : new GreedySettlementCalculator();
        this.expenseCache = new ArrayList<>(storageManager.loadExpenses());

        long maxId = 100;
        for (Expense e : expenseCache) {
            if (e.getExpenseId() != null && e.getExpenseId().startsWith("EXP-")) {
                try {
                    long val = Long.parseLong(e.getExpenseId().substring(4));
                    if (val > maxId) maxId = val;
                } catch (NumberFormatException ignored) {}
            }
        }
        this.idCounter = new AtomicLong(maxId);
    }

    public synchronized Expense addExpense(String groupId, String description, double amount,
                                          String payerUsername, List<String> involvedUsernames,
                                          String date, Category category, String createdByUsername) {
        Group group = groupManager.getGroupById(groupId);
        if (group == null) {
            throw new ResourceNotFoundException("Group '" + groupId + "' not found.");
        }
        if (description == null || description.trim().isEmpty()) {
            throw new ValidationException("Expense description cannot be empty.");
        }
        if (amount <= 0.0) {
            throw new ValidationException("Amount must be greater than 0.");
        }
        if (payerUsername == null || !group.isMember(payerUsername)) {
            throw new ValidationException("Payer @" + payerUsername + " must be a member of the group.");
        }
        if (involvedUsernames == null || involvedUsernames.isEmpty()) {
            throw new ValidationException("At least one participant must be involved in the expense.");
        }
        for (String member : involvedUsernames) {
            if (!group.isMember(member)) {
                throw new ValidationException("Participant @" + member + " is not a member of the group.");
            }
        }

        String expenseId = "EXP-" + idCounter.incrementAndGet();
        GroupEqualSplit expense = new GroupEqualSplit(
                expenseId, groupId, description, amount, payerUsername,
                involvedUsernames, date, category, createdByUsername
        );

        expenseCache.add(expense);
        storageManager.saveExpenses(expenseCache);

        if (auditService != null) {
            String details = String.format("Amount: ₹%.2f | Payer: @%s | Category: %s | Split among: %s",
                    amount, payerUsername, category.name(), String.join(", ", involvedUsernames));
            auditService.log(AuditLog.EventType.EXPENSE_ADD, createdByUsername, groupId, expenseId,
                    "Added expense: " + description, details);
        }

        return expense;
    }

    public synchronized Expense editExpense(String expenseId, String description, double amount,
                                           String payerUsername, List<String> involvedUsernames,
                                           String date, Category category, String modifiedByUsername) {
        Expense expense = getExpenseById(expenseId);
        if (expense == null) {
            throw new ResourceNotFoundException("Expense '" + expenseId + "' not found.");
        }

        Group group = groupManager.getGroupById(expense.getGroupId());
        if (group == null) {
            throw new ResourceNotFoundException("Associated group not found.");
        }
        if (amount <= 0.0) {
            throw new ValidationException("Amount must be greater than 0.");
        }
        if (payerUsername == null || !group.isMember(payerUsername)) {
            throw new ValidationException("Payer @" + payerUsername + " must be a member of the group.");
        }
        if (involvedUsernames == null || involvedUsernames.isEmpty()) {
            throw new ValidationException("At least one participant must be involved.");
        }

        String oldDetails = String.format("Old: ₹%.2f, Desc: '%s', Payer: @%s",
                expense.getTotalAmount(), expense.getDescription(), expense.getPayerUsername());

        expense.setDescription(description);
        expense.setTotalAmount(amount);
        expense.setPayerUsername(payerUsername);
        expense.setInvolvedUsernames(involvedUsernames);
        expense.setDate(date);
        expense.setCategory(category);
        expense.calculateSplits();

        storageManager.saveExpenses(expenseCache);

        if (auditService != null) {
            String newDetails = String.format("Updated: ₹%.2f, Desc: '%s', Payer: @%s | Previous: [%s]",
                    amount, description, payerUsername, oldDetails);
            auditService.log(AuditLog.EventType.EXPENSE_EDIT, modifiedByUsername, expense.getGroupId(), expenseId,
                    "Modified expense: " + description, newDetails);
        }

        return expense;
    }

    public synchronized boolean deleteExpense(String expenseId, String deletedByUsername) {
        Expense expense = getExpenseById(expenseId);
        if (expense == null) {
            return false;
        }

        expenseCache.remove(expense);
        storageManager.saveExpenses(expenseCache);

        if (auditService != null) {
            String details = String.format("Deleted: '%s' of ₹%.2f (Payer: @%s)",
                    expense.getDescription(), expense.getTotalAmount(), expense.getPayerUsername());
            auditService.log(AuditLog.EventType.EXPENSE_DELETE, deletedByUsername, expense.getGroupId(), expenseId,
                    "Deleted expense: " + expense.getDescription(), details);
        }

        return true;
    }

    public synchronized Expense getExpenseById(String expenseId) {
        if (expenseId == null) return null;
        for (Expense e : expenseCache) {
            if (expenseId.equalsIgnoreCase(e.getExpenseId())) {
                return e;
            }
        }
        return null;
    }

    public synchronized List<Expense> getExpensesForGroup(String groupId) {
        List<Expense> list = new ArrayList<>();
        if (groupId == null) return list;
        for (Expense e : expenseCache) {
            if (groupId.equalsIgnoreCase(e.getGroupId())) {
                list.add(e);
            }
        }
        return list;
    }

    /**
     * Filters expenses for a group by participant, category, and keyword search.
     */
    public synchronized List<Expense> filterExpenses(String groupId, String participantUsername,
                                                     Category category, String searchKeyword) {
        List<Expense> all = getExpensesForGroup(groupId);
        List<Expense> filtered = new ArrayList<>();

        for (Expense e : all) {
            boolean matchesParticipant = true;
            if (participantUsername != null && !participantUsername.trim().isEmpty()) {
                String clean = participantUsername.trim().toLowerCase();
                matchesParticipant = e.getPayerUsername().equalsIgnoreCase(clean) || e.isUserInvolved(clean);
            }

            boolean matchesCategory = true;
            if (category != null) {
                matchesCategory = e.getCategory() == category;
            }

            boolean matchesKeyword = true;
            if (searchKeyword != null && !searchKeyword.trim().isEmpty()) {
                String kw = searchKeyword.trim().toLowerCase();
                matchesKeyword = e.getDescription().toLowerCase().contains(kw) ||
                                 e.getPayerUsername().toLowerCase().contains(kw);
            }

            if (matchesParticipant && matchesCategory && matchesKeyword) {
                filtered.add(e);
            }
        }
        return filtered;
    }

    /**
     * Aggregates all expenses for a group into per-participant ledger balances.
     */
    public synchronized List<Participant> calculateGroupLedger(String groupId) {
        Group group = groupManager.getGroupById(groupId);
        if (group == null) {
            return Collections.emptyList();
        }

        Map<String, Participant> participantMap = new LinkedHashMap<>();
        for (String member : group.getMemberUsernames()) {
            User u = userManager.getUserByUsername(member);
            String displayName = (u != null) ? u.getFullName() : member;
            participantMap.put(member, new Participant(member, displayName));
        }

        List<Expense> groupExpenses = getExpensesForGroup(groupId);
        for (Expense exp : groupExpenses) {
            // Payer credit
            Participant payer = participantMap.get(exp.getPayerUsername().toLowerCase());
            if (payer != null) {
                payer.addPayment(exp.getTotalAmount());
            }

            // Split debts
            Map<String, Double> splits = exp.getSplitMap();
            for (Map.Entry<String, Double> entry : splits.entrySet()) {
                Participant debtor = participantMap.get(entry.getKey().toLowerCase());
                if (debtor != null) {
                    debtor.addFairShare(entry.getValue());
                }
            }
        }

        List<Participant> result = new ArrayList<>(participantMap.values());
        Collections.sort(result); // Sorted descending by net balance
        return result;
    }

    public synchronized double getTotalGroupExpenditure(String groupId) {
        double total = 0.0;
        for (Expense e : getExpensesForGroup(groupId)) {
            total += e.getTotalAmount();
        }
        return Math.round(total * 100.0) / 100.0;
    }

    /**
     * Calculates simplified settlement plan using the Greedy Routing Engine.
     */
    public synchronized List<Settlement> calculateGroupSettlements(String groupId) {
        List<Participant> participants = calculateGroupLedger(groupId);
        return settlementCalculator.calculateSettlements(participants);
    }
}
