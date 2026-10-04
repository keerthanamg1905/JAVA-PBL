package com.equishare.ui.cli;

/**
 * Generic printing utility for the Console Interface.
 * Retains and honors the generic printHeader<T> method from the original prototype.
 */
public class ConsolePrinter {

    /**
     * Generic Method replacing C++ template functionality,
     * preserved from the original EquiShare prototype.
     */
    public static <T> void printHeader(T text) {
        System.out.println("\n========================================================");
        System.out.println("  " + text);
        System.out.println("========================================================");
    }

    public static <T> void printSubHeader(T text) {
        System.out.println("\n--- " + text + " ---");
    }

    public static void printDivider() {
        System.out.println("--------------------------------------------------------");
    }

    public static void printDoubleDivider() {
        System.out.println("========================================================");
    }

    public static void printSuccess(String message) {
        System.out.println(" ✅ " + message);
    }

    public static void printWarning(String message) {
        System.out.println(" ⚠️  " + message);
    }

    public static void printError(String message) {
        System.out.println(" ❌ " + message);
    }

    public static void printInfo(String message) {
        System.out.println(" ℹ️  " + message);
    }
}
