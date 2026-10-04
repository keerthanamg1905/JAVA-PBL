package com.equishare.service;

import com.equishare.model.Participant;
import com.equishare.model.Settlement;

import java.util.List;

/**
 * Interface defining settlement calculation strategies.
 * Demonstrates Strategy Pattern and Interface segregation.
 */
public interface SettlementCalculator {
    List<Settlement> calculateSettlements(List<Participant> participants);
}
