package com.equishare.model;

/**
 * Category enum representing the various types of shared expenses.
 */
public enum Category {
    FOOD("Food & Dining", "🍔"),
    TRAVEL("Travel & Commute", "🚗"),
    ACCOMMODATION("Accommodation", "🏨"),
    ENTERTAINMENT("Entertainment & Fun", "🎟️"),
    UTILITIES("Utilities & Bills", "💡"),
    GROCERIES("Groceries & Supplies", "🛒"),
    SHOPPING("Shopping", "🛍️"),
    OTHER("General / Other", "📌");

    private final String displayName;
    private final String icon;

    Category(String displayName, String icon) {
        this.displayName = displayName;
        this.icon = icon;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getIcon() {
        return icon;
    }

    @Override
    public String toString() {
        return icon + " " + displayName;
    }

    public static Category fromString(String text) {
        if (text == null || text.trim().isEmpty()) {
            return OTHER;
        }
        String clean = text.trim().toUpperCase();
        for (Category c : Category.values()) {
            if (c.name().equalsIgnoreCase(clean) || c.displayName.equalsIgnoreCase(text.trim())) {
                return c;
            }
        }
        return OTHER;
    }
}
