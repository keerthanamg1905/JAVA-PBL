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

/**
 * Main Application Launcher for EquiShare Pro.
 * Preserves the original driver signature while introducing
 * dual-mode runtime (GUI by default, CLI fallback with --cli).
 */
public class Main {

    /**
     * Generic Method replacing C++ template functionality,
     * preserved from the original prototype driver class.
     */
    public static <T> void printHeader(T text) {
        ConsolePrinter.printHeader(text);
    }

    public static void main(String[] args) {
        // Check for CLI flag
        boolean forceCli = false;
        for (String arg : args) {
            if ("--cli".equalsIgnoreCase(arg) || "-c".equalsIgnoreCase(arg) || "--console".equalsIgnoreCase(arg)) {
                forceCli = true;
                break;
            }
        }

        // Initialize Core Services & Persistence
        StorageManager storageManager = new FileStorageManager("data");
        AuditService auditService = new AuditService(storageManager);
        UserManager userManager = new UserManager(storageManager, auditService);
        GroupManager groupManager = new GroupManager(storageManager, userManager, auditService);
        SettlementCalculator settlementCalculator = new GreedySettlementCalculator();
        ExpenseManager expenseManager = new ExpenseManager(storageManager, groupManager, userManager, auditService, settlementCalculator);

        // Seed demonstration data if repository is brand new
        SampleDataSeeder.seedIfEmpty(userManager, groupManager, expenseManager);

        boolean isHeadless = GraphicsEnvironment.isHeadless();

        if (forceCli || isHeadless) {
            if (isHeadless && !forceCli) {
                System.out.println("[INFO] Running in headless environment. Starting Console CLI mode...");
            }
            ConsoleUI cli = new ConsoleUI(userManager, groupManager, expenseManager, auditService);
            cli.start();
        } else {
            // Launch Modern Swing GUI
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
