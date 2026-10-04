package com.equishare.service;

import com.equishare.model.Participant;
import com.equishare.model.Settlement;

import java.util.ArrayList;
import java.util.List;

/**
 * Enhanced implementation of the Greedy Settlement Routing algorithm
 * from the original prototype.
 * Matches highest debtors with highest creditors using a two-pointer approach,
 * minimizing the total number of cash transfer transactions required.
 */
public class GreedySettlementCalculator implements SettlementCalculator {

    private static class BalanceEntry {
        String username;
        String name;
        double amount;

        BalanceEntry(String username, String name, double amount) {
            this.username = username;
            this.name = name;
            this.amount = amount;
        }
    }

    @Override
    public List<Settlement> calculateSettlements(List<Participant> participants) {
        List<Settlement> settlements = new ArrayList<>();
        if (participants == null || participants.isEmpty()) {
            return settlements;
        }

        List<BalanceEntry> debtors = new ArrayList<>();
        List<BalanceEntry> creditors = new ArrayList<>();

        for (Participant p : participants) {
            double net = p.getNetBalance();
            if (net < -0.009) {
                debtors.add(new BalanceEntry(p.getUsername(), p.getName(), Math.abs(net)));
            } else if (net > 0.009) {
                creditors.add(new BalanceEntry(p.getUsername(), p.getName(), net));
            }
        }

        // Sort descending to settle largest amounts first (greedy optimization)
        debtors.sort((a, b) -> Double.compare(b.amount, a.amount));
        creditors.sort((a, b) -> Double.compare(b.amount, a.amount));

        int dIdx = 0;
        int cIdx = 0;

        while (dIdx < debtors.size() && cIdx < creditors.size()) {
            BalanceEntry debtor = debtors.get(dIdx);
            BalanceEntry creditor = creditors.get(cIdx);

            double paymentExchange = Math.min(debtor.amount, creditor.amount);
            paymentExchange = Math.round(paymentExchange * 100.0) / 100.0;

            if (paymentExchange > 0.009) {
                settlements.add(new Settlement(
                        debtor.username, debtor.name,
                        creditor.username, creditor.name,
                        paymentExchange
                ));
            }

            debtor.amount = Math.round((debtor.amount - paymentExchange) * 100.0) / 100.0;
            creditor.amount = Math.round((creditor.amount - paymentExchange) * 100.0) / 100.0;

            if (debtor.amount <= 0.009) {
                dIdx++;
            }
            if (creditor.amount <= 0.009) {
                cIdx++;
            }
        }

        return settlements;
    }
}
