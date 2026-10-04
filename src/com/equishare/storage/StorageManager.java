package com.equishare.storage;

import com.equishare.model.AuditLog;
import com.equishare.model.Expense;
import com.equishare.model.Group;
import com.equishare.model.User;

import java.util.List;
import java.util.Map;

/**
 * StorageManager Interface defining data persistence contracts.
 * Enables separation of concerns and future extensibility (e.g. SQL, NoSQL).
 */
public interface StorageManager {
    void saveUsers(Map<String, User> users);
    Map<String, User> loadUsers();

    void saveGroups(Map<String, Group> groups);
    Map<String, Group> loadGroups();

    void saveExpenses(List<Expense> expenses);
    List<Expense> loadExpenses();

    void saveAuditLogs(List<AuditLog> auditLogs);
    List<AuditLog> loadAuditLogs();

    void clearAll();
}
