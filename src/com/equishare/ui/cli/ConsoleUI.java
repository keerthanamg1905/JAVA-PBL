package com.equishare.ui.cli;

import com.equishare.exception.EquiShareException;
import com.equishare.model.*;
import com.equishare.service.*;
import com.equishare.util.DateUtils;

import java.nio.file.Path;
import java.util.*;

/**
 * Interactive Console Interface for EquiShare Pro.
 * Provides complete multi-user expense sharing, history filtering,
 * ledger balance sheets, greedy settlements, and invariant audit reports.
 */
public class ConsoleUI {
    private final UserManager userManager;
    private final GroupManager groupManager;
    private final ExpenseManager expenseManager;
    private final AuditService auditService;
    private final Scanner scanner;
    private User currentUser;

    public ConsoleUI(UserManager userManager, GroupManager groupManager,
                     ExpenseManager expenseManager, AuditService auditService) {
        this.userManager = userManager;
        this.groupManager = groupManager;
        this.expenseManager = expenseManager;
        this.auditService = auditService;
        this.scanner = new Scanner(System.in);
        this.currentUser = null;
    }

    public void start() {
        ConsolePrinter.printHeader("WELCOME TO EQUISHARE PRO");
        System.out.println("  An Automated Multi-Participant Expense Settlement & Audit System");
        System.out.println("  Persistent Storage | Multi-User | Real-Time Greedy Routing");

        boolean running = true;
        while (running) {
            if (currentUser == null) {
                running = showAuthMenu();
            } else {
                showDashboardMenu();
            }
        }
        ConsolePrinter.printHeader("THANK YOU FOR USING EQUISHARE PRO");
        System.out.println("Exiting application safely. All changes have been persisted.");
    }

    private boolean showAuthMenu() {
        ConsolePrinter.printSubHeader("AUTHENTICATION GATEWAY");
        System.out.println("1. Login to Existing Account");
        System.out.println("2. Register New Account");
        System.out.println("3. Quick Demo Login (Alice / Bob / Charlie)");
        System.out.println("4. Exit Application");
        System.out.print("Select an option (1-4): ");

        String choice = scanner.nextLine().trim();
        switch (choice) {
            case "1":
                handleLogin();
                break;
            case "2":
                handleRegister();
                break;
            case "3":
                handleQuickDemoLogin();
                break;
            case "4":
                return false;
            default:
                ConsolePrinter.printWarning("Invalid option! Please enter a number between 1 and 4.");
        }
        return true;
    }

    private void handleLogin() {
        ConsolePrinter.printSubHeader("USER LOGIN");
        System.out.print("Enter username: ");
        String username = scanner.nextLine().trim();
        System.out.print("Enter password: ");
        String password = scanner.nextLine().trim();

        try {
            currentUser = userManager.authenticate(username, password);
            ConsolePrinter.printSuccess("Welcome back, " + currentUser.getFullName() + "! (@" + currentUser.getUsername() + ")");
        } catch (EquiShareException e) {
            ConsolePrinter.printError(e.getMessage());
        }
    }

    private void handleRegister() {
        ConsolePrinter.printSubHeader("REGISTER NEW PARTICIPANT");
        System.out.print("Choose a username (lowercase letters/numbers): ");
        String username = scanner.nextLine().trim();
        System.out.print("Enter full name: ");
        String fullName = scanner.nextLine().trim();
        System.out.print("Enter email address: ");
        String email = scanner.nextLine().trim();
        System.out.print("Choose a password (min 4 characters): ");
        String password = scanner.nextLine().trim();

        try {
            currentUser = userManager.registerUser(username, password, fullName, email);
            ConsolePrinter.printSuccess("Account created successfully! Logged in as @" + currentUser.getUsername());
        } catch (EquiShareException e) {
            ConsolePrinter.printError(e.getMessage());
        }
    }

    private void handleQuickDemoLogin() {
        ConsolePrinter.printSubHeader("QUICK DEMO ACCOUNTS");
        System.out.println("1. Alice Sharma (@alice) - Group Creator");
        System.out.println("2. Bob Verma (@bob)");
        System.out.println("3. Charlie Singh (@charlie)");
        System.out.print("Pick account (1-3): ");
        String pick = scanner.nextLine().trim();

        String un = "alice";
        if ("2".equals(pick)) un = "bob";
        if ("3".equals(pick)) un = "charlie";

        try {
            currentUser = userManager.authenticate(un, "password123");
            ConsolePrinter.printSuccess("Demo login successful as " + currentUser.getFullName() + " (@" + currentUser.getUsername() + ")");
        } catch (Exception e) {
            ConsolePrinter.printError("Quick login failed: " + e.getMessage());
        }
    }

    private void showDashboardMenu() {
        ConsolePrinter.printHeader("USER DASHBOARD: " + currentUser.getFullName().toUpperCase());
        System.out.println("User ID: @" + currentUser.getUsername() + " | Email: " + currentUser.getEmail());

        List<Group> userGroups = groupManager.getGroupsForUser(currentUser.getUsername());
        System.out.println("\nYour Active Groups (" + userGroups.size() + "):");
        if (userGroups.isEmpty()) {
            System.out.println("  (You have not created or joined any groups yet.)");
        } else {
            for (int i = 0; i < userGroups.size(); i++) {
                Group g = userGroups.get(i);
                double total = expenseManager.getTotalGroupExpenditure(g.getGroupId());
                System.out.printf("  [%d] %-25s (ID: %-8s) | Members: %d | Total Spend: ₹%.2f\n",
                        (i + 1), g.getName(), g.getGroupId(), g.getMemberCount(), total);
            }
        }

        ConsolePrinter.printDivider();
        System.out.println("1. Open / Manage a Group Workspace");
        System.out.println("2. Create a New Group");
        System.out.println("3. Join an Existing Group by Group ID");
        System.out.println("4. View All System Audit Logs");
        System.out.println("5. Logout / Switch User");
        System.out.print("Choose action (1-5): ");

        String choice = scanner.nextLine().trim();
        switch (choice) {
            case "1":
                openGroupWorkspace(userGroups);
                break;
            case "2":
                handleCreateGroup();
                break;
            case "3":
                handleJoinGroup();
                break;
            case "4":
                viewSystemAuditLogs();
                break;
            case "5":
                ConsolePrinter.printInfo("Logged out @" + currentUser.getUsername());
                currentUser = null;
                break;
            default:
                ConsolePrinter.printWarning("Invalid choice. Please choose 1-5.");
        }
    }

    private void handleCreateGroup() {
        ConsolePrinter.printSubHeader("CREATE NEW EXPENSE GROUP");
        System.out.print("Enter Group Name (e.g. Goa Trip, Flat 401): ");
        String name = scanner.nextLine().trim();
        System.out.print("Enter Description / Purpose: ");
        String desc = scanner.nextLine().trim();

        try {
            Group created = groupManager.createGroup(name, desc, currentUser.getUsername());
            ConsolePrinter.printSuccess("Group '" + created.getName() + "' created! Unique Group ID: " + created.getGroupId());
            System.out.println("Share this Group ID (" + created.getGroupId() + ") with your friends so they can join.");
        } catch (EquiShareException e) {
            ConsolePrinter.printError(e.getMessage());
        }
    }

    private void handleJoinGroup() {
        ConsolePrinter.printSubHeader("JOIN EXISTING EXPENSE GROUP");
        System.out.print("Enter Group ID (e.g. GRP-101): ");
        String groupId = scanner.nextLine().trim();

        try {
            boolean joined = groupManager.joinGroup(groupId, currentUser.getUsername());
            if (joined) {
                Group g = groupManager.getGroupById(groupId);
                ConsolePrinter.printSuccess("Successfully joined '" + g.getName() + "'!");
            } else {
                ConsolePrinter.printInfo("You are already a member of this group.");
            }
        } catch (EquiShareException e) {
            ConsolePrinter.printError(e.getMessage());
        }
    }

    private void openGroupWorkspace(List<Group> userGroups) {
        if (userGroups.isEmpty()) {
            ConsolePrinter.printWarning("You must create or join a group first.");
            return;
        }

        System.out.print("Enter group number (1-" + userGroups.size() + ") or Group ID: ");
        String input = scanner.nextLine().trim();
        Group selectedGroup = null;

        try {
            int index = Integer.parseInt(input) - 1;
            if (index >= 0 && index < userGroups.size()) {
                selectedGroup = userGroups.get(index);
            }
        } catch (NumberFormatException e) {
            selectedGroup = groupManager.getGroupById(input);
        }

        if (selectedGroup == null || !selectedGroup.isMember(currentUser.getUsername())) {
            ConsolePrinter.printError("Invalid group selection or you are not a member.");
            return;
        }

        manageGroupSession(selectedGroup);
    }

    private void manageGroupSession(Group group) {
        boolean inGroup = true;
        while (inGroup) {
            ConsolePrinter.printHeader("GROUP WORKSPACE: " + group.getName().toUpperCase());
            System.out.printf("Group ID: %-10s | Creator: @%-10s | Members: %s\n",
                    group.getGroupId(), group.getCreatorUsername(), String.join(", ", group.getMemberUsernames()));
            double totalExpenditure = expenseManager.getTotalGroupExpenditure(group.getGroupId());
            System.out.printf("💰 Total Group Expenditure: ₹%.2f\n", totalExpenditure);
            ConsolePrinter.printDivider();

            System.out.println("1. View All Group Expenses (Shared Visibility)");
            System.out.println("2. Filter / Search Expenses (by Category / Member / Keyword)");
            System.out.println("3. Add New Shared Expense");
            System.out.println("4. Edit an Existing Expense");
            System.out.println("5. Delete an Expense");
            System.out.println("6. View Participant Ledger & Fair Shares");
            System.out.println("7. View Simplified Settlement Routing (Greedy Transfers)");
            System.out.println("8. Run Mathematical Audit & Invariant Verification");
            System.out.println("9. Add Another Member to this Group");
            System.out.println("10. Return to Dashboard");
            System.out.print("Select an option (1-10): ");

            String opt = scanner.nextLine().trim();
            switch (opt) {
                case "1":
                    displayExpenses(expenseManager.getExpensesForGroup(group.getGroupId()), group);
                    break;
                case "2":
                    handleFilterExpenses(group);
                    break;
                case "3":
                    handleAddExpense(group);
                    break;
                case "4":
                    handleEditExpense(group);
                    break;
                case "5":
                    handleDeleteExpense(group);
                    break;
                case "6":
                    displayLedgerBalances(group);
                    break;
                case "7":
                    displaySettlementReport(group);
                    break;
                case "8":
                    handleAuditVerification(group);
                    break;
                case "9":
                    handleAddMemberToGroup(group);
                    break;
                case "10":
                    inGroup = false;
                    break;
                default:
                    ConsolePrinter.printWarning("Invalid option! Please enter 1-10.");
            }
        }
    }

    private void displayExpenses(List<Expense> expenses, Group group) {
        ConsolePrinter.printSubHeader("SHARED EXPENSES LEDGER - " + group.getName());
        if (expenses.isEmpty()) {
            System.out.println("  No expenses recorded yet in this group.");
            return;
        }

        System.out.printf("%-10s | %-12s | %-14s | %-24s | %-10s | %-10s | %s\n",
                "ID", "Date", "Category", "Description", "Amount", "Paid By", "Split Among");
        ConsolePrinter.printDivider();

        for (Expense e : expenses) {
            String involvedStr = String.join(",", e.getInvolvedUsernames());
            if (involvedStr.length() > 20) {
                involvedStr = involvedStr.substring(0, 17) + "...";
            }
            System.out.printf("%-10s | %-12s | %-14s | %-24s | ₹%-9.2f | @%-9s | %s\n",
                    e.getExpenseId(), e.getDate(), e.getCategory().getDisplayName(),
                    e.getDescription(), e.getTotalAmount(), e.getPayerUsername(), involvedStr);
        }
        ConsolePrinter.printDivider();
        double sum = expenses.stream().mapToDouble(Expense::getTotalAmount).sum();
        System.out.printf("Total displayed expenses: %d items | Sum: ₹%.2f\n", expenses.size(), sum);
    }

    private void handleFilterExpenses(Group group) {
        ConsolePrinter.printSubHeader("FILTER EXPENSES");
        System.out.print("Filter by Participant username (or leave blank for all): ");
        String part = scanner.nextLine().trim();
        if (part.isEmpty()) part = null;

        System.out.println("Categories: FOOD, TRAVEL, ACCOMMODATION, ENTERTAINMENT, UTILITIES, GROCERIES, SHOPPING, OTHER");
        System.out.print("Filter by Category (or leave blank for all): ");
        String catStr = scanner.nextLine().trim();
        Category cat = catStr.isEmpty() ? null : Category.fromString(catStr);

        System.out.print("Keyword search (or leave blank): ");
        String kw = scanner.nextLine().trim();
        if (kw.isEmpty()) kw = null;

        List<Expense> filtered = expenseManager.filterExpenses(group.getGroupId(), part, cat, kw);
        displayExpenses(filtered, group);
    }

    private void handleAddExpense(Group group) {
        ConsolePrinter.printSubHeader("ADD NEW SHARED EXPENSE");
        System.out.print("Enter Expense Description (e.g. Dinner, Fuel, Hotel): ");
        String desc = scanner.nextLine().trim();

        double amount = 0.0;
        while (amount <= 0.0) {
            System.out.print("Enter Total Amount (₹): ");
            try {
                amount = Double.parseDouble(scanner.nextLine().trim());
                if (amount <= 0) ConsolePrinter.printWarning("Amount must be positive.");
            } catch (NumberFormatException e) {
                ConsolePrinter.printError("Invalid amount format. Please enter a valid number.");
            }
        }

        System.out.print("Enter Date (YYYY-MM-DD, or press Enter for today): ");
        String dateStr = scanner.nextLine().trim();
        if (dateStr.isEmpty() || !DateUtils.isValidDate(dateStr)) {
            dateStr = DateUtils.todayString();
        }

        System.out.println("Select Category:");
        Category[] cats = Category.values();
        for (int i = 0; i < cats.length; i++) {
            System.out.println("  " + (i + 1) + ". " + cats[i].toString());
        }
        System.out.print("Choose Category (1-" + cats.length + ", default 1): ");
        String catPick = scanner.nextLine().trim();
        Category selectedCat = Category.FOOD;
        try {
            int idx = Integer.parseInt(catPick) - 1;
            if (idx >= 0 && idx < cats.length) {
                selectedCat = cats[idx];
            }
        } catch (Exception ignored) {}

        List<String> members = new ArrayList<>(group.getMemberUsernames());
        System.out.println("\nWho paid for this expense?");
        for (int i = 0; i < members.size(); i++) {
            System.out.println("  " + (i + 1) + ". @" + members.get(i) + (members.get(i).equals(currentUser.getUsername()) ? " (You)" : ""));
        }
        System.out.print("Select Payer (1-" + members.size() + ", default You): ");
        String payerPick = scanner.nextLine().trim();
        String payerUsername = currentUser.getUsername();
        try {
            int idx = Integer.parseInt(payerPick) - 1;
            if (idx >= 0 && idx < members.size()) {
                payerUsername = members.get(idx);
            }
        } catch (Exception ignored) {}

        System.out.println("\nWho shares this expense?");
        System.out.println("  1. All Group Members (" + members.size() + " people - Default Equal Split)");
        System.out.println("  2. Select Custom Subset of Members");
        System.out.print("Choice (1 or 2): ");
        String splitPick = scanner.nextLine().trim();

        List<String> involved = new ArrayList<>(members);
        if ("2".equals(splitPick)) {
            involved.clear();
            System.out.println("Enter participant numbers separated by comma (e.g. 1, 2):");
            for (int i = 0; i < members.size(); i++) {
                System.out.println("  " + (i + 1) + ". @" + members.get(i));
            }
            System.out.print("Participants: ");
            String[] parts = scanner.nextLine().split(",");
            for (String p : parts) {
                try {
                    int pIdx = Integer.parseInt(p.trim()) - 1;
                    if (pIdx >= 0 && pIdx < members.size() && !involved.contains(members.get(pIdx))) {
                        involved.add(members.get(pIdx));
                    }
                } catch (Exception ignored) {}
            }
            if (involved.isEmpty()) {
                ConsolePrinter.printWarning("No participants chosen! Defaulting to all members.");
                involved = new ArrayList<>(members);
            }
        }

        try {
            Expense added = expenseManager.addExpense(
                    group.getGroupId(), desc, amount, payerUsername, involved,
                    dateStr, selectedCat, currentUser.getUsername()
            );
            ConsolePrinter.printSuccess("Expense successfully recorded! [ID: " + added.getExpenseId() + "]");
            System.out.printf("  Each person's equal share: ₹%.2f across %d participants.\n",
                    (amount / involved.size()), involved.size());
        } catch (EquiShareException e) {
            ConsolePrinter.printError(e.getMessage());
        }
    }

    private void handleEditExpense(Group group) {
        ConsolePrinter.printSubHeader("EDIT EXISTING EXPENSE");
        System.out.print("Enter Expense ID to edit (e.g. EXP-101): ");
        String expId = scanner.nextLine().trim();

        Expense exp = expenseManager.getExpenseById(expId);
        if (exp == null || !exp.getGroupId().equalsIgnoreCase(group.getGroupId())) {
            ConsolePrinter.printError("Expense not found in this group.");
            return;
        }

        System.out.println("Editing: " + exp.getDescription() + " (Current Amount: ₹" + exp.getTotalAmount() + ")");
        System.out.print("New Description (press Enter to keep '" + exp.getDescription() + "'): ");
        String newDesc = scanner.nextLine().trim();
        if (newDesc.isEmpty()) newDesc = exp.getDescription();

        System.out.print("New Amount (press Enter to keep ₹" + exp.getTotalAmount() + "): ");
        String amountStr = scanner.nextLine().trim();
        double newAmount = exp.getTotalAmount();
        if (!amountStr.isEmpty()) {
            try {
                newAmount = Double.parseDouble(amountStr);
            } catch (Exception ignored) {}
        }

        try {
            expenseManager.editExpense(
                    exp.getExpenseId(), newDesc, newAmount, exp.getPayerUsername(),
                    new ArrayList<>(exp.getInvolvedUsernames()), exp.getDate(), exp.getCategory(), currentUser.getUsername()
            );
            ConsolePrinter.printSuccess("Expense updated successfully!");
        } catch (EquiShareException e) {
            ConsolePrinter.printError(e.getMessage());
        }
    }

    private void handleDeleteExpense(Group group) {
        ConsolePrinter.printSubHeader("DELETE EXPENSE");
        System.out.print("Enter Expense ID to delete: ");
        String expId = scanner.nextLine().trim();

        Expense exp = expenseManager.getExpenseById(expId);
        if (exp == null || !exp.getGroupId().equalsIgnoreCase(group.getGroupId())) {
            ConsolePrinter.printError("Expense not found.");
            return;
        }

        System.out.print("Are you sure you want to delete '" + exp.getDescription() + "' (₹" + exp.getTotalAmount() + ")? (y/N): ");
        String confirm = scanner.nextLine().trim();
        if ("y".equalsIgnoreCase(confirm)) {
            boolean deleted = expenseManager.deleteExpense(expId, currentUser.getUsername());
            if (deleted) {
                ConsolePrinter.printSuccess("Expense deleted successfully. Balances recalculated.");
            }
        } else {
            ConsolePrinter.printInfo("Deletion cancelled.");
        }
    }

    private void displayLedgerBalances(Group group) {
        ConsolePrinter.printHeader("INDIVIDUAL LEDGER BALANCES: " + group.getName());
        List<Participant> participants = expenseManager.calculateGroupLedger(group.getGroupId());
        double totalExpense = expenseManager.getTotalGroupExpenditure(group.getGroupId());

        System.out.printf("💰 Total Group Expenditure : ₹%.2f\n", totalExpense);
        System.out.printf("👥 Total Group Members     : %d\n", participants.size());
        ConsolePrinter.printDivider();
        System.out.printf("%-18s | %-12s | %-12s | %s\n", "Participant", "Total Paid", "Fair Share", "Net Balance Status");
        ConsolePrinter.printDivider();

        for (Participant p : participants) {
            System.out.printf("%-18s | ₹%-11.2f | ₹%-11.2f | %s\n",
                    p.getName() + " (@" + p.getUsername() + ")",
                    p.getAmountPaid(),
                    p.getFairShare(),
                    p.getStatusDescription());
        }
        ConsolePrinter.printDivider();
    }

    private void displaySettlementReport(Group group) {
        ConsolePrinter.printHeader("OPTIMIZED SETTLEMENT INSTRUCTIONS: " + group.getName());
        System.out.println("  Greedy routing calculates the absolute fewest cash transfers needed.");
        ConsolePrinter.printDivider();

        List<Settlement> settlements = expenseManager.calculateGroupSettlements(group.getGroupId());
        if (settlements.isEmpty()) {
            System.out.println("  ✅ Everyone is completely settled! No money needs to change hands.");
        } else {
            System.out.println("  Recommended Settlement Transfers:");
            for (int i = 0; i < settlements.size(); i++) {
                Settlement s = settlements.get(i);
                System.out.printf("  %d. %s shall transfer ₹%.2f to %s\n",
                        (i + 1), s.getDebtorName(), s.getAmount(), s.getCreditorName());
            }
        }
        ConsolePrinter.printDivider();
    }

    private void handleAuditVerification(Group group) {
        ConsolePrinter.printHeader("FINANCIAL AUDIT & INVARIANT CHECK: " + group.getName());
        List<Expense> expenses = expenseManager.getExpensesForGroup(group.getGroupId());
        List<Participant> participants = expenseManager.calculateGroupLedger(group.getGroupId());

        AuditService.IntegrityResult result = auditService.verifyLedgerIntegrity(group.getGroupId(), expenses, participants);
        System.out.println(result.getFormattedReport());

        System.out.print("Would you like to export this audit report to a text file? (y/N): ");
        String ans = scanner.nextLine().trim();
        if ("y".equalsIgnoreCase(ans)) {
            try {
                Path exported = auditService.exportAuditReport(group.getGroupId(), result.getFormattedReport());
                ConsolePrinter.printSuccess("Audit report saved to: " + exported.toAbsolutePath());
            } catch (Exception e) {
                ConsolePrinter.printError("Failed to export report: " + e.getMessage());
            }
        }
    }

    private void handleAddMemberToGroup(Group group) {
        ConsolePrinter.printSubHeader("ADD MEMBER TO GROUP");
        System.out.print("Enter username of the user to add: ");
        String username = scanner.nextLine().trim().toLowerCase();

        try {
            boolean joined = groupManager.joinGroup(group.getGroupId(), username);
            if (joined) {
                ConsolePrinter.printSuccess("User @" + username + " added to " + group.getName() + "!");
            } else {
                ConsolePrinter.printInfo("User @" + username + " is already in this group.");
            }
        } catch (EquiShareException e) {
            ConsolePrinter.printError(e.getMessage());
        }
    }

    private void viewSystemAuditLogs() {
        ConsolePrinter.printHeader("SYSTEM-WIDE AUDIT TRAIL");
        List<AuditLog> logs = auditService.getAllLogs();
        if (logs.isEmpty()) {
            System.out.println("  No audit entries logged yet.");
            return;
        }

        System.out.printf("%-10s | %-20s | %-16s | %-10s | %s\n",
                "Log ID", "Timestamp", "Event Type", "Actor", "Summary");
        ConsolePrinter.printDivider();
        for (AuditLog l : logs) {
            System.out.printf("%-10s | %-20s | %-16s | @%-9s | %s\n",
                    l.getLogId(), l.getTimestamp(), l.getEventType().name(), l.getActorUsername(), l.getSummary());
        }
        ConsolePrinter.printDivider();
        System.out.println("Total audit records: " + logs.size());
    }
}
