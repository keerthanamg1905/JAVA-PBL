package com.equishare.ui.swing;

import com.equishare.model.Category;
import com.equishare.model.Expense;
import com.equishare.model.Group;
import com.equishare.model.User;
import com.equishare.service.ExpenseManager;
import com.equishare.service.UserManager;
import com.equishare.util.DateUtils;

import javax.swing.*;
import java.awt.*;
import java.util.*;
import java.util.List;

/**
 * Modal dialog for creating and editing shared expenses.
 */
public class ExpenseDialog extends JDialog {
    private final ExpenseManager expenseManager;
    private final UserManager userManager;
    private final Group group;
    private final String currentUsername;
    private final Expense existingExpense; // null if adding new
    private boolean saved = false;

    private JTextField txtDescription;
    private JTextField txtAmount;
    private JTextField txtDate;
    private JComboBox<Category> cmbCategory;
    private JComboBox<String> cmbPayer;
    private final Map<String, JCheckBox> participantCheckboxes = new LinkedHashMap<>();

    public ExpenseDialog(Frame owner, Group group, String currentUsername,
                         ExpenseManager expenseManager, UserManager userManager,
                         Expense existingExpense) {
        super(owner, existingExpense == null ? "Add New Shared Expense" : "Edit Expense", true);
        this.group = group;
        this.currentUsername = currentUsername;
        this.expenseManager = expenseManager;
        this.userManager = userManager;
        this.existingExpense = existingExpense;

        initComponents();
        setSize(480, 560);
        setLocationRelativeTo(owner);
        setResizable(false);
    }

    private void initComponents() {
        setLayout(new BorderLayout(10, 10));

        JPanel formPanel = new JPanel();
        formPanel.setLayout(new BoxLayout(formPanel, BoxLayout.Y_AXIS));
        formPanel.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));

        // Description
        JLabel lblDesc = new JLabel("Expense Description (e.g. Dinner, Fuel, Hotel):");
        lblDesc.setAlignmentX(Component.LEFT_ALIGNMENT);
        formPanel.add(lblDesc);
        formPanel.add(Box.createVerticalStrut(4));

        txtDescription = new JTextField(25);
        txtDescription.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        txtDescription.setMaximumSize(new Dimension(Integer.MAX_VALUE, 32));
        txtDescription.setPreferredSize(new Dimension(420, 32));
        txtDescription.setAlignmentX(Component.LEFT_ALIGNMENT);
        formPanel.add(txtDescription);
        formPanel.add(Box.createVerticalStrut(10));

        // Amount
        JLabel lblAmount = new JLabel("Amount (₹):");
        lblAmount.setAlignmentX(Component.LEFT_ALIGNMENT);
        formPanel.add(lblAmount);
        formPanel.add(Box.createVerticalStrut(4));

        txtAmount = new JTextField(25);
        txtAmount.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        txtAmount.setMaximumSize(new Dimension(Integer.MAX_VALUE, 32));
        txtAmount.setPreferredSize(new Dimension(420, 32));
        txtAmount.setAlignmentX(Component.LEFT_ALIGNMENT);
        formPanel.add(txtAmount);
        formPanel.add(Box.createVerticalStrut(10));

        // Date
        JLabel lblDate = new JLabel("Date (YYYY-MM-DD):");
        lblDate.setAlignmentX(Component.LEFT_ALIGNMENT);
        formPanel.add(lblDate);
        formPanel.add(Box.createVerticalStrut(4));

        txtDate = new JTextField(DateUtils.todayString(), 25);
        txtDate.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        txtDate.setMaximumSize(new Dimension(Integer.MAX_VALUE, 32));
        txtDate.setPreferredSize(new Dimension(420, 32));
        txtDate.setAlignmentX(Component.LEFT_ALIGNMENT);
        formPanel.add(txtDate);
        formPanel.add(Box.createVerticalStrut(10));

        // Category
        JLabel lblCat = new JLabel("Category:");
        lblCat.setAlignmentX(Component.LEFT_ALIGNMENT);
        formPanel.add(lblCat);
        formPanel.add(Box.createVerticalStrut(4));

        cmbCategory = new JComboBox<>(Category.values());
        cmbCategory.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        cmbCategory.setMaximumSize(new Dimension(Integer.MAX_VALUE, 32));
        cmbCategory.setPreferredSize(new Dimension(420, 32));
        cmbCategory.setAlignmentX(Component.LEFT_ALIGNMENT);
        formPanel.add(cmbCategory);
        formPanel.add(Box.createVerticalStrut(10));

        // Payer
        JLabel lblPayer = new JLabel("Paid By:");
        lblPayer.setAlignmentX(Component.LEFT_ALIGNMENT);
        formPanel.add(lblPayer);
        formPanel.add(Box.createVerticalStrut(4));

        cmbPayer = new JComboBox<>();
        cmbPayer.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        cmbPayer.setMaximumSize(new Dimension(Integer.MAX_VALUE, 32));
        cmbPayer.setPreferredSize(new Dimension(420, 32));
        cmbPayer.setAlignmentX(Component.LEFT_ALIGNMENT);
        List<String> members = new ArrayList<>(group.getMemberUsernames());
        for (String m : members) {
            User u = userManager.getUserByUsername(m);
            String label = (u != null ? u.getFullName() : m) + " (@" + m + ")";
            cmbPayer.addItem(m);
        }
        cmbPayer.setSelectedItem(currentUsername);
        formPanel.add(cmbPayer);
        formPanel.add(Box.createVerticalStrut(12));

        // Involved Participants
        JLabel lblSplit = new JLabel("Split Equally Among (select participants):");
        lblSplit.setAlignmentX(Component.LEFT_ALIGNMENT);
        formPanel.add(lblSplit);
        formPanel.add(Box.createVerticalStrut(4));

        JPanel checkPanel = new JPanel(new GridLayout(0, 1, 4, 4));
        checkPanel.setBorder(BorderFactory.createTitledBorder("Involved Members"));

        for (String m : members) {
            User u = userManager.getUserByUsername(m);
            String label = (u != null ? u.getFullName() : m) + " (@" + m + ")";
            JCheckBox cb = new JCheckBox(label, true);
            participantCheckboxes.put(m, cb);
            checkPanel.add(cb);
        }

        JScrollPane scrollCheck = new JScrollPane(checkPanel);
        scrollCheck.setPreferredSize(new Dimension(420, 110));
        scrollCheck.setAlignmentX(Component.LEFT_ALIGNMENT);
        formPanel.add(scrollCheck);

        // Populate fields if editing
        if (existingExpense != null) {
            txtDescription.setText(existingExpense.getDescription());
            txtAmount.setText(String.valueOf(existingExpense.getTotalAmount()));
            txtDate.setText(existingExpense.getDate());
            cmbCategory.setSelectedItem(existingExpense.getCategory());
            cmbPayer.setSelectedItem(existingExpense.getPayerUsername());

            for (Map.Entry<String, JCheckBox> entry : participantCheckboxes.entrySet()) {
                entry.getValue().setSelected(existingExpense.isUserInvolved(entry.getKey()));
            }
        }

        add(formPanel, BorderLayout.CENTER);

        // Buttons
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        JButton btnCancel = new JButton("Cancel");
        btnCancel.addActionListener(e -> dispose());

        JButton btnSave = new JButton(existingExpense == null ? "Save Expense" : "Update Expense");
        btnSave.setBackground(new Color(25, 135, 84));
        btnSave.setForeground(Color.WHITE);
        btnSave.setFocusPainted(false);
        btnSave.addActionListener(e -> onSave());

        btnPanel.add(btnCancel);
        btnPanel.add(btnSave);
        add(btnPanel, BorderLayout.SOUTH);
    }

    private void onSave() {
        String desc = txtDescription.getText().trim();
        if (desc.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter an expense description.", "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        double amount;
        try {
            amount = Double.parseDouble(txtAmount.getText().trim());
            if (amount <= 0) throw new NumberFormatException();
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Please enter a valid positive amount.", "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String dateStr = txtDate.getText().trim();
        if (!DateUtils.isValidDate(dateStr)) {
            JOptionPane.showMessageDialog(this, "Please enter a valid date in YYYY-MM-DD format.", "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        Category category = (Category) cmbCategory.getSelectedItem();
        String payer = (String) cmbPayer.getSelectedItem();

        List<String> involved = new ArrayList<>();
        for (Map.Entry<String, JCheckBox> entry : participantCheckboxes.entrySet()) {
            if (entry.getValue().isSelected()) {
                involved.add(entry.getKey());
            }
        }

        if (involved.isEmpty()) {
            JOptionPane.showMessageDialog(this, "At least one participant must be involved in the split.", "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            if (existingExpense == null) {
                expenseManager.addExpense(group.getGroupId(), desc, amount, payer, involved, dateStr, category, currentUsername);
            } else {
                expenseManager.editExpense(existingExpense.getExpenseId(), desc, amount, payer, involved, dateStr, category, currentUsername);
            }
            saved = true;
            dispose();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error saving expense: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public boolean isSaved() {
        return saved;
    }
}
