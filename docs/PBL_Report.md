# A PROJECT BASED LEARNING (PBL) REPORT
## on
# EQUISHARE PRO: AN AUTOMATED MULTI-PARTICIPANT EXPENSE SETTLEMENT & AUDIT SYSTEM

**Submitted in partial fulfilment of the requirements for the Project-Based Learning component of Java Programming**
**BACHELOR OF ENGINEERING IN COMPUTER SCIENCE AND ENGINEERING**

**CHENNAI INSTITUTE OF TECHNOLOGY (Autonomous)**
*(Affiliated to Anna University, Chennai)*
**October – 2026**

---

## ABSTRACT

Manual calculation of shared expenses in collective social environments such as student roommates, group travel, and collaborative events frequently leads to computational errors, social friction, and a lack of auditability. Existing solutions either require full-time internet connectivity with complex cloud overhead or behave as transient single-session calculators that discard state upon exit. 

This project presents **EquiShare Pro**, a robust, standalone multi-participant financial utility application engineered in Java using Object-Oriented Programming (OOP) paradigms and pure Java zero-dependency persistence. The system models users, groups, shared itemized expenses, and debt relationships through a modular package architecture (`model`, `service`, `storage`, `ui`, `util`). Key features include secure SHA-256 user authentication, dynamic group workspaces, real-time shared expense visibility, an optimized **Two-Pointer Greedy Settlement Routing Engine** that reduces debt clearance transactions from $O(N^2)$ to at most $N-1$, and an **Audit & Invariant Engine** that mathematically verifies ledger integrity through the Zero-Sum conservation property ($\sum \text{Net Balances} = 0.00$). The application features both a modern Java Swing GUI and an interactive terminal CLI. Experimental testing across 12 rigorous test suites demonstrated 100% test pass rate, exact sub-penny mathematical accuracy, and seamless state persistence across restarts.

**Keywords:** Object-Oriented Programming, Greedy Settlement Routing, Expense Sharing, Zero-Sum Invariant, Java Swing GUI.

---

## TABLE OF CONTENTS

1. **INTRODUCTION**
   - 1.1 Background
   - 1.2 Driving Question
   - 1.3 Objectives
   - 1.4 Scope and Limitations
2. **CONCEPT EXPLORATION**
   - 2.1 Related Approaches
   - 2.2 Summary Table
   - 2.3 What This Told Us
3. **PROJECT PLANNING AND TEAM ORGANISATION**
   - 3.1 Weekly PBL Progress Log
   - 3.2 Requirements
   - 3.3 Feasibility
4. **ITERATIVE DESIGN AND DEVELOPMENT**
   - 4.1 System Architecture
   - 4.2 Baseline (Iteration 1)
   - 4.3 Project Refinement (Iteration 2)
   - 4.4 Final Approach (Iteration 3)
   - 4.5 Testing and Execution
5. **IMPLEMENTATION**
   - 5.1 Module Description
   - 5.2 Key Code Snippets
   - 5.3 User Interface / Demo
6. **RESULTS AND DISCUSSION**
   - 6.1 Evaluation Metrics
   - 6.2 Results Across Iterations
   - 6.3 Discussion
   - 6.4 Limitations
7. **TEAM REFLECTION AND LEARNING OUTCOMES**
   - 7.1 Individual Reflections
   - 7.2 Team Learning
   - 7.3 Course Outcomes — Evidence Summary
8. **CONCLUSION AND FUTURE SCOPE**
   - 8.1 Conclusion
   - 8.2 Future Scope
9. **REFERENCES**

---

*(For full expanded report sections, please refer to the project documentation within the college archive)*
