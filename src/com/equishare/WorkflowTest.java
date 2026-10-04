package com.equishare;

import com.equishare.model.*;
import com.equishare.service.*;
import com.equishare.storage.FileStorageManager;

import java.io.File;
import java.util.Arrays;
import java.util.List;

/**
 * Automated End-to-End Workflow & Mathematical Verification Test for EquiShare Pro.
 * Validates multi-user expense sharing, greedy settlement routing,
 * persistence, and zero-sum invariant integrity.
 */
public class WorkflowTest {

    private static void assertTrue(boolean condition, String message) {
        if (!condition) {
            System.err.println("❌ FAILED: " + message);
            throw new AssertionError("Test assertion failed: " + message);
        } else {
            System.out.println("  ✔ PASS: " + message);
        }
    }

    private static void deleteDir(File dir) {
        File[] files = dir.listFiles();
        if (files != null) {
            for (File f : files) {
                if (f.isDirectory()) deleteDir(f);
                else f.delete();
            }
        }
        dir.delete();
    }

    public static void main(String[] args) {
        System.out.println("========================================================");
        System.out.println("     EQUISHARE PRO: AUTOMATED WORKFLOW TEST RUNNER     ");
        System.out.println("========================================================");

        String testDataDir = "data_test_" + System.currentTimeMillis();
        File testDirFile = new File(testDataDir);

        try {
            // Step 1: Initialize Storage & Services
            System.out.println("\n[1] Initializing Test Storage & Services...");
            FileStorageManager storage = new FileStorageManager(testDataDir);
            AuditService auditService = new AuditService(storage);
            UserManager userManager = new UserManager(storage, auditService);
            GroupManager groupManager = new GroupManager(storage, userManager, auditService);
            SettlementCalculator settlementCalc = new GreedySettlementCalculator();
            ExpenseManager expenseManager = new ExpenseManager(storage, groupManager, userManager, auditService, settlementCalc);

            // Step 2: Register 3 Users
            System.out.println("\n[2] Registering 3 Users: Alice, Bob, Charlie...");
            User alice = userManager.registerUser("alice", "pass123", "Alice Sharma", "alice@test.com");
            User bob = userManager.registerUser("bob", "pass123", "Bob Verma", "bob@test.com");
            User charlie = userManager.registerUser("charlie", "pass123", "Charlie Singh", "charlie@test.com");

            assertTrue(userManager.getAllUsers().size() == 3, "All 3 users registered");
            assertTrue(userManager.authenticate("alice", "pass123") != null, "Alice authentication successful");

            // Step 3: Alice creates a Group and Bob/Charlie join
            System.out.println("\n[3] Alice creates 'Goa Trip 2026' Group...");
            Group group = groupManager.createGroup("Goa Trip 2026", "Beach holiday", "alice");
            String gId = group.getGroupId();
            assertTrue(group.isMember("alice"), "Alice is member of created group");

            System.out.println("    Bob and Charlie join group " + gId + "...");
            groupManager.joinGroup(gId, "bob");
            groupManager.joinGroup(gId, "charlie");
            assertTrue(group.getMemberCount() == 3, "Group has 3 active participants");

            // Step 4: Alice enters an Expense: ₹1200 for Dinner (split 3 ways = ₹400 each)
            System.out.println("\n[4] Alice adds Expense: ₹1200 for Seafood Dinner...");
            Expense exp1 = expenseManager.addExpense(gId, "Seafood Dinner", 1200.0, "alice",
                    Arrays.asList("alice", "bob", "charlie"), "2026-09-14", Category.FOOD, "alice");
            assertTrue(exp1 != null, "Expense 1 recorded");

            // Step 5: Bob logs in and views Alice's expense (Shared Visibility!)
            System.out.println("\n[5] Verifying Shared Visibility: Bob logs in and views expenses...");
            List<Expense> bobsView = expenseManager.getExpensesForGroup(gId);
            assertTrue(bobsView.size() == 1, "Bob sees exactly 1 expense added by Alice");
            assertTrue(bobsView.get(0).getPayerUsername().equals("alice"), "Bob sees Alice was the payer");
            assertTrue(bobsView.get(0).getTotalAmount() == 1200.0, "Bob sees ₹1200 amount");

            // Step 6: Bob adds another Expense: ₹600 for Rental Scooters (split 3 ways = ₹200 each)
            System.out.println("\n[6] Bob adds Expense: ₹600 for Rental Scooters...");
            Expense exp2 = expenseManager.addExpense(gId, "Rental Scooters", 600.0, "bob",
                    Arrays.asList("alice", "bob", "charlie"), "2026-09-14", Category.TRAVEL, "bob");
            assertTrue(exp2 != null, "Expense 2 recorded");

            // Step 7: Charlie logs in and views expense history
            System.out.println("\n[7] Charlie logs in and inspects history (2 shared expenses)...");
            List<Expense> charliesView = expenseManager.getExpensesForGroup(gId);
            assertTrue(charliesView.size() == 2, "Charlie sees both Alice's and Bob's expenses");

            // Step 8: Verify Automated Expense & Balance Calculations
            System.out.println("\n[8] Verifying Individual Ledger Balances...");
            List<Participant> ledger = expenseManager.calculateGroupLedger(gId);
            double totalExpenditure = expenseManager.getTotalGroupExpenditure(gId);
            assertTrue(Math.abs(totalExpenditure - 1800.0) < 0.01, "Total group spend is ₹1800.00");

            Participant pAlice = null, pBob = null, pCharlie = null;
            for (Participant p : ledger) {
                if (p.getUsername().equals("alice")) pAlice = p;
                if (p.getUsername().equals("bob")) pBob = p;
                if (p.getUsername().equals("charlie")) pCharlie = p;
            }

            assertTrue(pAlice != null && Math.abs(pAlice.getAmountPaid() - 1200.0) < 0.01, "Alice paid ₹1200");
            assertTrue(pAlice != null && Math.abs(pAlice.getFairShare() - 600.0) < 0.01, "Alice fair share is ₹600");
            assertTrue(pAlice != null && Math.abs(pAlice.getNetBalance() - 600.0) < 0.01, "Alice net balance is +₹600 (Gets back ₹600)");

            assertTrue(pBob != null && Math.abs(pBob.getAmountPaid() - 600.0) < 0.01, "Bob paid ₹600");
            assertTrue(pBob != null && Math.abs(pBob.getFairShare() - 600.0) < 0.01, "Bob fair share is ₹600");
            assertTrue(pBob != null && Math.abs(pBob.getNetBalance() - 0.0) < 0.01, "Bob is Settled (₹0.00 net)");

            assertTrue(pCharlie != null && Math.abs(pCharlie.getAmountPaid() - 0.0) < 0.01, "Charlie paid ₹0");
            assertTrue(pCharlie != null && Math.abs(pCharlie.getFairShare() - 600.0) < 0.01, "Charlie fair share is ₹600");
            assertTrue(pCharlie != null && Math.abs(pCharlie.getNetBalance() - (-600.0)) < 0.01, "Charlie net balance is -₹600 (Owes ₹600)");

            // Step 9: Verify Greedy Settlement Routing
            System.out.println("\n[9] Verifying Greedy Debt Settlement Calculation...");
            List<Settlement> settlements = expenseManager.calculateGroupSettlements(gId);
            assertTrue(settlements.size() == 1, "Optimized to exactly 1 transfer");
            Settlement s = settlements.get(0);
            assertTrue(s.getDebtorUsername().equals("charlie"), "Debtor is Charlie");
            assertTrue(s.getCreditorUsername().equals("alice"), "Creditor is Alice");
            assertTrue(Math.abs(s.getAmount() - 600.0) < 0.01, "Transfer amount is ₹600.00");
            System.out.println("    Instruction: " + s.getInstruction());

            // Step 10: Financial Audit & Zero-Sum Invariant Check
            System.out.println("\n[10] Running Financial Audit & Zero-Sum Invariant Check...");
            List<Expense> allGroupExpenses = expenseManager.getExpensesForGroup(gId);
            AuditService.IntegrityResult integrity = auditService.verifyLedgerIntegrity(gId, allGroupExpenses, ledger);
            assertTrue(integrity.isPassed(), "Ledger integrity check PASSED with 0 discrepancies");
            assertTrue(Math.abs(integrity.getNetBalanceSum()) < 0.01, "Sum of net balances is 0.00 (Zero-Sum Invariant)");

            // Step 11: Expense Editing & Automatic Recalculation
            System.out.println("\n[11] Testing Expense Editing & Recalculation...");
            expenseManager.editExpense(exp1.getExpenseId(), "Luxury Seafood Buffet", 1500.0, "alice",
                    Arrays.asList("alice", "bob", "charlie"), "2026-09-14", Category.FOOD, "alice");
            double newTotal = expenseManager.getTotalGroupExpenditure(gId);
            assertTrue(Math.abs(newTotal - 2100.0) < 0.01, "Total updated to ₹2100.00");

            // Step 12: Persistence Verification across application reload
            System.out.println("\n[12] Testing Persistence across application restart...");
            FileStorageManager reloadedStorage = new FileStorageManager(testDataDir);
            AuditService reloadedAudit = new AuditService(reloadedStorage);
            UserManager reloadedUserMgr = new UserManager(reloadedStorage, reloadedAudit);
            GroupManager reloadedGroupMgr = new GroupManager(reloadedStorage, reloadedUserMgr, reloadedAudit);
            ExpenseManager reloadedExpenseMgr = new ExpenseManager(reloadedStorage, reloadedGroupMgr, reloadedUserMgr, reloadedAudit, settlementCalc);

            assertTrue(reloadedUserMgr.userExists("alice"), "Persisted user Alice reloaded");
            assertTrue(reloadedUserMgr.userExists("bob"), "Persisted user Bob reloaded");
            assertTrue(reloadedGroupMgr.getGroupById(gId) != null, "Persisted group reloaded");
            assertTrue(reloadedExpenseMgr.getExpensesForGroup(gId).size() == 2, "Persisted expenses reloaded");
            assertTrue(Math.abs(reloadedExpenseMgr.getTotalGroupExpenditure(gId) - 2100.0) < 0.01, "Persisted total expenditure accurate");
            assertTrue(!reloadedAudit.getAllLogs().isEmpty(), "Persisted audit trail reloaded");

            System.out.println("\n========================================================");
            System.out.println("  🎉 ALL 12 VERIFICATION SUITES PASSED SUCCESSFULLY!    ");
            System.out.println("========================================================");

        } finally {
            // Clean up test data directory
            deleteDir(testDirFile);
        }
    }
}
