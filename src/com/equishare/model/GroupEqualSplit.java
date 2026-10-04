package com.equishare.model;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

/**
 * Concrete implementation of Expense performing equal splits.
 * Preserves and extends the GroupEqualSplit class from the original prototype.
 * Accurately allocates currency cents to avoid rounding discrepancies.
 */
public class GroupEqualSplit extends Expense {
    private static final long serialVersionUID = 1L;

    public GroupEqualSplit() {
        super();
    }

    public GroupEqualSplit(String expenseId, String groupId, String description, double totalAmount,
                           String payerUsername, List<String> involvedUsernames,
                           String date, Category category, String createdByUsername) {
        super(expenseId, groupId, description, totalAmount, payerUsername, involvedUsernames, date, category, createdByUsername);
        calculateSplits();
    }

    @Override
    public void calculateSplits() {
        splitMap.clear();
        if (involvedUsernames == null || involvedUsernames.isEmpty() || totalAmount <= 0.0) {
            return;
        }

        int count = involvedUsernames.size();
        // Use BigDecimal for financial precision
        BigDecimal totalBd = BigDecimal.valueOf(totalAmount).setScale(2, RoundingMode.HALF_UP);
        BigDecimal countBd = BigDecimal.valueOf(count);
        BigDecimal baseShare = totalBd.divide(countBd, 2, RoundingMode.DOWN);
        BigDecimal remainder = totalBd.subtract(baseShare.multiply(countBd));

        // Allocate the remainder cent-by-cent to the first few participants to ensure exact sum match
        int remainingCents = remainder.multiply(BigDecimal.valueOf(100)).intValue();

        for (int i = 0; i < count; i++) {
            String user = involvedUsernames.get(i);
            BigDecimal share = baseShare;
            if (i < remainingCents) {
                share = share.add(BigDecimal.valueOf(0.01));
            }
            splitMap.put(user, share.doubleValue());
        }
    }

    @Override
    public String getSplitType() {
        return "Equal Split";
    }
}
