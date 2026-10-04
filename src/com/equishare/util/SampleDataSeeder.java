package com.equishare.util;

import com.equishare.model.Category;
import com.equishare.model.Group;
import com.equishare.service.ExpenseManager;
import com.equishare.service.GroupManager;
import com.equishare.service.UserManager;

import java.time.LocalDate;
import java.util.Arrays;

/**
 * Seeds default sample demonstration data for quick college evaluation
 * and automated test verification.
 */
public class SampleDataSeeder {

    public static void seedIfEmpty(UserManager userManager, GroupManager groupManager, ExpenseManager expenseManager) {
        if (!userManager.getAllUsers().isEmpty()) {
            return; // Data already exists
        }

        System.out.println("[Seeder] Initializing sample demonstration data...");

        // 1. Register 3 Users
        userManager.registerUser("alice", "password123", "Alice Sharma", "alice@example.com");
        userManager.registerUser("bob", "password123", "Bob Verma", "bob@example.com");
        userManager.registerUser("charlie", "password123", "Charlie Singh", "charlie@example.com");

        // 2. Create Group
        Group group = groupManager.createGroup("Goa Vacation 2026", "Beach house trip and water sports", "alice");
        String gId = group.getGroupId();

        // 3. Join Bob and Charlie
        groupManager.joinGroup(gId, "bob");
        groupManager.joinGroup(gId, "charlie");

        // 4. Add Sample Expenses
        String today = LocalDate.now().toString();
        String yesterday = LocalDate.now().minusDays(1).toString();

        // Expense 1: Alice paid ₹1200 for Dinner (split 3 ways = ₹400 each)
        expenseManager.addExpense(gId, "Seafood Dinner at Beachside", 1200.0,
                "alice", Arrays.asList("alice", "bob", "charlie"),
                yesterday, Category.FOOD, "alice");

        // Expense 2: Bob paid ₹600 for Scooters (split 3 ways = ₹200 each)
        expenseManager.addExpense(gId, "Rental Scooters & Fuel", 600.0,
                "bob", Arrays.asList("alice", "bob", "charlie"),
                today, Category.TRAVEL, "bob");

        // Expense 3: Charlie paid ₹300 for Museum Entry (split 3 ways = ₹100 each)
        expenseManager.addExpense(gId, "Fort Aguada Museum Entry Tickets", 300.0,
                "charlie", Arrays.asList("alice", "bob", "charlie"),
                today, Category.ENTERTAINMENT, "charlie");

        System.out.println("[Seeder] Successfully seeded Alice, Bob, Charlie & Goa Vacation 2026.");
    }
}
