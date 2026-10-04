package com.equishare.ui.swing;

import com.equishare.model.*;
import com.equishare.service.*;
import com.equishare.util.DateUtils;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Modern Swing Desktop Application Interface for EquiShare Pro.
 * Provides rich dashboard visualization, interactive expense management,
 * greedy settlement rendering, and system audit compliance verification.
 */
public class MainFrame extends JFrame {
    private final UserManager userManager;
    private final GroupManager groupManager;
    private final ExpenseManager expenseManager;
    private final AuditService auditService;
    private User currentUser;
    private Group currentGroup;

    // Top Bar Components
    private JComboBox<GroupItem> cmbGroups;
    private JLabel lblUserBadge;

    // Dashboard Components
    private JLabel lblCardTotalGroup;
    private JLabel lblCardMyPaid;
    private JLabel lblCardMyShare;
    private JLabel lblCardMyNet;
    private DefaultListModel<String> memberListModel;
    private DefaultListModel<String> recentActivityModel;

    // Expense Tab Components
    private JTable tblExpenses;
    private DefaultTableModel expenseTableModel;
    private JTextField txtSearch;
    private JComboBox<String> cmbFilterCategory;
    private JComboBox<String> cmbFilterMember;

    // Ledger & Settlement Components
    private JTable tblLedger;
    private DefaultTableModel ledgerTableModel;
    private DefaultListModel<String> settlementListModel;

    // Audit Tab Components
    private JTable tblAudit;
    private DefaultTableModel auditTableModel;

    // Status Bar
    private JLabel lblStatusBar;

    private static class GroupItem {
        final Group group;
        GroupItem(Group group) { this.group = group; }
        @Override
        public String toString() {
            return group.getName() + " (" + group.getGroupId() + ")";
        }
    }

    public MainFrame(UserManager userManager, GroupManager groupManager,
                     ExpenseManager expenseManager, AuditService auditService,
                     User initialUser) {
        super("EquiShare Pro – Multi-Participant Expense Settlement & Audit System");
        this.userManager = userManager;
        this.groupManager = groupManager;
        this.expenseManager = expenseManager;
        this.auditService = auditService;
        this.currentUser = initialUser;

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1100, 720);
        setMinimumSize(new Dimension(950, 600));
        setLocationRelativeTo(null);

        initUI();
        loadUserGroups();
        refreshAllViews();
    }

    private void initUI() {
        setLayout(new BorderLayout());

        // 1. TOP TOOLBAR / HEADER
        add(createTopBar(), BorderLayout.NORTH);

        // 2. MAIN TABBED VIEW
        JTabbedPane tabs = new JTabbedPane();
        tabs.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        tabs.addTab("📊 Dashboard", createDashboardPanel());
        tabs.addTab("💰 Expenses Ledger", createExpensesPanel());
        tabs.addTab("⚖️ Balances & Settlements", createSettlementPanel());
        tabs.addTab("🛡️ Audit Trail & Verification", createAuditPanel());
        add(tabs, BorderLayout.CENTER);

        // 3. BOTTOM STATUS BAR
        lblStatusBar = new JLabel(" Ready | Logged in as @" + currentUser.getUsername());
        lblStatusBar.setBorder(new EmptyBorder(6, 12, 6, 12));
        lblStatusBar.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        add(lblStatusBar, BorderLayout.SOUTH);
    }

    private JPanel createTopBar() {
        JPanel bar = new JPanel(new BorderLayout(15, 0));
        bar.setBackground(new Color(245, 247, 250));
        bar.setBorder(new CompoundBorder(new LineBorder(new Color(222, 226, 230), 1), new EmptyBorder(10, 15, 10, 15)));

        // Brand & Title
        JPanel brandPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        brandPanel.setOpaque(false);
        JLabel logo = new JLabel("EquiShare Pro");
        logo.setFont(new Font("Segoe UI", Font.BOLD, 18));
        logo.setForeground(new Color(13, 110, 253));
        brandPanel.add(logo);

        // Group Switcher
        brandPanel.add(new JLabel(" | Active Group:"));
        cmbGroups = new JComboBox<>();
        cmbGroups.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        cmbGroups.setPreferredSize(new Dimension(220, 30));
        cmbGroups.addActionListener(e -> onGroupChanged());
        brandPanel.add(cmbGroups);

        JButton btnNewGroup = new JButton("➕ New Group");
        btnNewGroup.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        btnNewGroup.addActionListener(e -> promptCreateGroup());
        brandPanel.add(btnNewGroup);

        JButton btnJoinGroup = new JButton("🔗 Join Group");
        btnJoinGroup.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        btnJoinGroup.addActionListener(e -> promptJoinGroup());
        brandPanel.add(btnJoinGroup);

        bar.add(brandPanel, BorderLayout.WEST);

        // User profile & Logout
        JPanel userPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        userPanel.setOpaque(false);
        lblUserBadge = new JLabel("👤 " + currentUser.getFullName() + " (@" + currentUser.getUsername() + ")");
        lblUserBadge.setFont(new Font("Segoe UI", Font.BOLD, 13));
        userPanel.add(lblUserBadge);

        JButton btnSwitchUser = new JButton("Switch User");
        btnSwitchUser.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        btnSwitchUser.addActionListener(e -> switchUser());
        userPanel.add(btnSwitchUser);

        bar.add(userPanel, BorderLayout.EAST);
        return bar;
    }

    // ==========================================
    // TAB 1: DASHBOARD
    // ==========================================
    private JPanel createDashboardPanel() {
        JPanel panel = new JPanel(new BorderLayout(15, 15));
        panel.setBorder(new EmptyBorder(15, 15, 15, 15));

        // 4 KPI Summary Cards
        JPanel cardsGrid = new JPanel(new GridLayout(1, 4, 15, 0));
        lblCardTotalGroup = new JLabel("₹0.00", SwingConstants.CENTER);
        lblCardMyPaid = new JLabel("₹0.00", SwingConstants.CENTER);
        lblCardMyShare = new JLabel("₹0.00", SwingConstants.CENTER);
        lblCardMyNet = new JLabel("₹0.00", SwingConstants.CENTER);

        cardsGrid.add(createKpiCard("💰 Total Group Spend", lblCardTotalGroup, new Color(240, 244, 248), new Color(33, 37, 41)));
        cardsGrid.add(createKpiCard("💳 You Paid", lblCardMyPaid, new Color(230, 244, 234), new Color(25, 135, 84)));
        cardsGrid.add(createKpiCard("⚖️ Your Fair Share", lblCardMyShare, new Color(255, 243, 205), new Color(102, 77, 3)));
        cardsGrid.add(createKpiCard("📊 Net Position", lblCardMyNet, new Color(235, 240, 255), new Color(13, 110, 253)));

        panel.add(cardsGrid, BorderLayout.NORTH);

        // Center split: Left = Group Members, Right = Recent Transactions
        JPanel centerSplit = new JPanel(new GridLayout(1, 2, 15, 0));

        // Members Panel
        JPanel membersPanel = new JPanel(new BorderLayout(5, 5));
        membersPanel.setBorder(BorderFactory.createTitledBorder("👥 Group Members & Live Net Balances"));
        memberListModel = new DefaultListModel<>();
        JList<String> memberList = new JList<>(memberListModel);
        memberList.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        membersPanel.add(new JScrollPane(memberList), BorderLayout.CENTER);

        JButton btnAddMember = new JButton("➕ Add Member by Username");
        btnAddMember.addActionListener(e -> promptAddMember());
        membersPanel.add(btnAddMember, BorderLayout.SOUTH);
        centerSplit.add(membersPanel);

        // Recent Activity Panel
        JPanel activityPanel = new JPanel(new BorderLayout(5, 5));
        activityPanel.setBorder(BorderFactory.createTitledBorder("🕒 Recent Group Activity"));
        recentActivityModel = new DefaultListModel<>();
        JList<String> activityList = new JList<>(recentActivityModel);
        activityList.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        activityPanel.add(new JScrollPane(activityList), BorderLayout.CENTER);
        centerSplit.add(activityPanel);

        panel.add(centerSplit, BorderLayout.CENTER);

        // Dashboard Quick Actions Bar
        JPanel quickActions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 5));
        JButton btnDashAddExp = new JButton("➕ Add New Shared Expense");
        btnDashAddExp.setBackground(new Color(25, 135, 84));
        btnDashAddExp.setForeground(Color.WHITE);
        btnDashAddExp.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnDashAddExp.setFocusPainted(false);
        btnDashAddExp.addActionListener(e -> onAddExpense());
        quickActions.add(btnDashAddExp);

        JButton btnDashNewGrp = new JButton("➕ Create Group");
        btnDashNewGrp.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        btnDashNewGrp.addActionListener(e -> promptCreateGroup());
        quickActions.add(btnDashNewGrp);

        JButton btnDashJoinGrp = new JButton("🔗 Join Group");
        btnDashJoinGrp.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        btnDashJoinGrp.addActionListener(e -> promptJoinGroup());
        quickActions.add(btnDashJoinGrp);

        panel.add(quickActions, BorderLayout.SOUTH);
        return panel;
    }

    private JPanel createKpiCard(String title, JLabel valueLabel, Color bg, Color fg) {
        JPanel card = new JPanel(new BorderLayout(5, 5));
        card.setBackground(bg);
        card.setBorder(new CompoundBorder(new LineBorder(new Color(220, 225, 230), 1), new EmptyBorder(12, 12, 12, 12)));

        JLabel titleLbl = new JLabel(title, SwingConstants.CENTER);
        titleLbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
        titleLbl.setForeground(new Color(108, 117, 125));

        valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 22));
        valueLabel.setForeground(fg);

        card.add(titleLbl, BorderLayout.NORTH);
        card.add(valueLabel, BorderLayout.CENTER);
        return card;
    }

    // ==========================================
    // TAB 2: EXPENSES LEDGER
    // ==========================================
    private JPanel createExpensesPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(new EmptyBorder(15, 15, 15, 15));

        // Filter Bar
        JPanel filterBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        filterBar.add(new JLabel("Search:"));
        txtSearch = new JTextField(12);
        filterBar.add(txtSearch);

        filterBar.add(new JLabel("Category:"));
        cmbFilterCategory = new JComboBox<>();
        cmbFilterCategory.addItem("All Categories");
        for (Category c : Category.values()) {
            cmbFilterCategory.addItem(c.name());
        }
        filterBar.add(cmbFilterCategory);

        filterBar.add(new JLabel("Participant:"));
        cmbFilterMember = new JComboBox<>();
        cmbFilterMember.addItem("All Members");
        filterBar.add(cmbFilterMember);

        JButton btnApplyFilter = new JButton("🔍 Filter");
        btnApplyFilter.addActionListener(e -> refreshExpensesTable());
        filterBar.add(btnApplyFilter);

        JButton btnResetFilter = new JButton("Reset");
        btnResetFilter.addActionListener(e -> {
            txtSearch.setText("");
            cmbFilterCategory.setSelectedIndex(0);
            cmbFilterMember.setSelectedIndex(0);
            refreshExpensesTable();
        });
        filterBar.add(btnResetFilter);

        panel.add(filterBar, BorderLayout.NORTH);

        // Table
        String[] cols = {"Expense ID", "Date", "Category", "Description", "Paid By", "Amount (₹)", "Split Among"};
        expenseTableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        tblExpenses = new JTable(expenseTableModel);
        tblExpenses.setRowHeight(26);
        tblExpenses.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        tblExpenses.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));
        tblExpenses.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        DefaultTableCellRenderer rightRenderer = new DefaultTableCellRenderer();
        rightRenderer.setHorizontalAlignment(SwingConstants.RIGHT);
        tblExpenses.getColumnModel().getColumn(5).setCellRenderer(rightRenderer);

        panel.add(new JScrollPane(tblExpenses), BorderLayout.CENTER);

        // Action Toolbar
        JPanel actionToolbar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 5));
        JButton btnAddExp = new JButton("➕ Add Expense");
        btnAddExp.setBackground(new Color(25, 135, 84));
        btnAddExp.setForeground(Color.WHITE);
        btnAddExp.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnAddExp.addActionListener(e -> onAddExpense());
        actionToolbar.add(btnAddExp);

        JButton btnEditExp = new JButton("✏️ Edit Selected");
        btnEditExp.addActionListener(e -> onEditExpense());
        actionToolbar.add(btnEditExp);

        JButton btnDeleteExp = new JButton("🗑️ Delete Selected");
        btnDeleteExp.setForeground(new Color(220, 53, 69));
        btnDeleteExp.addActionListener(e -> onDeleteExpense());
        actionToolbar.add(btnDeleteExp);

        JButton btnRefresh = new JButton("🔄 Refresh");
        btnRefresh.addActionListener(e -> refreshAllViews());
        actionToolbar.add(btnRefresh);

        panel.add(actionToolbar, BorderLayout.SOUTH);
        return panel;
    }

    // ==========================================
    // TAB 3: BALANCES & SETTLEMENTS
    // ==========================================
    private JPanel createSettlementPanel() {
        JPanel panel = new JPanel(new GridLayout(1, 2, 15, 0));
        panel.setBorder(new EmptyBorder(15, 15, 15, 15));

        // Left: Ledger Table
        JPanel leftPanel = new JPanel(new BorderLayout(8, 8));
        leftPanel.setBorder(BorderFactory.createTitledBorder("📊 Individual Participant Balances"));

        String[] ledgerCols = {"Participant", "Total Paid (₹)", "Fair Share (₹)", "Net Balance (₹)", "Status"};
        ledgerTableModel = new DefaultTableModel(ledgerCols, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        tblLedger = new JTable(ledgerTableModel);
        tblLedger.setRowHeight(26);
        tblLedger.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        tblLedger.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));
        leftPanel.add(new JScrollPane(tblLedger), BorderLayout.CENTER);
        panel.add(leftPanel);

        // Right: Greedy Settlement Routing
        JPanel rightPanel = new JPanel(new BorderLayout(8, 8));
        rightPanel.setBorder(BorderFactory.createTitledBorder("🚀 Simplified Debt Clearance Routing (Greedy Optimization)"));

        settlementListModel = new DefaultListModel<>();
        JList<String> settlementList = new JList<>(settlementListModel);
        settlementList.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        rightPanel.add(new JScrollPane(settlementList), BorderLayout.CENTER);

        JLabel infoLabel = new JLabel("<html><i>Greedy routing matches highest debtors directly with highest creditors to minimize transactions.</i></html>");
        infoLabel.setBorder(new EmptyBorder(5, 5, 5, 5));
        rightPanel.add(infoLabel, BorderLayout.SOUTH);
        panel.add(rightPanel);

        return panel;
    }

    // ==========================================
    // TAB 4: AUDIT TRAIL & VERIFICATION
    // ==========================================
    private JPanel createAuditPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(new EmptyBorder(15, 15, 15, 15));

        // Audit Table
        String[] auditCols = {"Log ID", "Timestamp", "Event Type", "Actor", "Summary", "Details"};
        auditTableModel = new DefaultTableModel(auditCols, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        tblAudit = new JTable(auditTableModel);
        tblAudit.setRowHeight(24);
        tblAudit.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        tblAudit.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        panel.add(new JScrollPane(tblAudit), BorderLayout.CENTER);

        // Bottom Actions
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 5));

        JButton btnVerify = new JButton("🔍 Run Mathematical Invariant Check");
        btnVerify.setBackground(new Color(13, 110, 253));
        btnVerify.setForeground(Color.WHITE);
        btnVerify.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnVerify.addActionListener(e -> runInvariantAudit());
        btnPanel.add(btnVerify);

        JButton btnExport = new JButton("📥 Export Audit Report");
        btnExport.addActionListener(e -> exportAuditReport());
        btnPanel.add(btnExport);

        panel.add(btnPanel, BorderLayout.SOUTH);
        return panel;
    }

    // ==========================================
    // ACTIONS & DATA BINDINGS
    // ==========================================
    private void loadUserGroups() {
        cmbGroups.removeAllItems();
        List<Group> userGroups = groupManager.getGroupsForUser(currentUser.getUsername());
        for (Group g : userGroups) {
            cmbGroups.addItem(new GroupItem(g));
        }
        if (!userGroups.isEmpty()) {
            currentGroup = userGroups.get(0);
        } else {
            currentGroup = null;
        }
    }

    private void onGroupChanged() {
        GroupItem selected = (GroupItem) cmbGroups.getSelectedItem();
        if (selected != null) {
            currentGroup = selected.group;
            refreshAllViews();
        }
    }

    private void refreshAllViews() {
        if (currentGroup == null) {
            lblStatusBar.setText(" No active group selected.");
            return;
        }

        refreshDashboard();
        refreshExpensesTable();
        refreshLedgerAndSettlements();
        refreshAuditTable();
        updateMemberFilterDropdown();

        lblStatusBar.setText(String.format(" Active Group: %s [%s] | Total Members: %d",
                currentGroup.getName(), currentGroup.getGroupId(), currentGroup.getMemberCount()));
    }

    private void refreshDashboard() {
        if (currentGroup == null) return;

        double groupTotal = expenseManager.getTotalGroupExpenditure(currentGroup.getGroupId());
        List<Participant> ledger = expenseManager.calculateGroupLedger(currentGroup.getGroupId());

        double myPaid = 0.0;
        double myShare = 0.0;
        double myNet = 0.0;

        memberListModel.clear();
        for (Participant p : ledger) {
            if (p.getUsername().equalsIgnoreCase(currentUser.getUsername())) {
                myPaid = p.getAmountPaid();
                myShare = p.getFairShare();
                myNet = p.getNetBalance();
            }
            memberListModel.addElement(String.format("• %-16s | Paid: ₹%-8.2f | Share: ₹%-8.2f | %s",
                    p.getName(), p.getAmountPaid(), p.getFairShare(), p.getStatusDescription()));
        }

        lblCardTotalGroup.setText(String.format("₹%.2f", groupTotal));
        lblCardMyPaid.setText(String.format("₹%.2f", myPaid));
        lblCardMyShare.setText(String.format("₹%.2f", myShare));

        if (myNet > 0.01) {
            lblCardMyNet.setText(String.format("+₹%.2f (Receivable)", myNet));
            lblCardMyNet.setForeground(new Color(25, 135, 84)); // Green
        } else if (myNet < -0.01) {
            lblCardMyNet.setText(String.format("-₹%.2f (Owes)", Math.abs(myNet)));
            lblCardMyNet.setForeground(new Color(220, 53, 69)); // Red
        } else {
            lblCardMyNet.setText("₹0.00 (Settled)");
            lblCardMyNet.setForeground(new Color(13, 110, 253));
        }

        // Recent activity
        recentActivityModel.clear();
        List<AuditLog> groupLogs = auditService.getLogsForGroup(currentGroup.getGroupId());
        int start = Math.max(0, groupLogs.size() - 8);
        for (int i = groupLogs.size() - 1; i >= start; i--) {
            AuditLog l = groupLogs.get(i);
            recentActivityModel.addElement(String.format("[%s] @%s: %s", l.getTimestamp().substring(11), l.getActorUsername(), l.getSummary()));
        }
    }

    private void updateMemberFilterDropdown() {
        if (currentGroup == null) return;
        cmbFilterMember.removeAllItems();
        cmbFilterMember.addItem("All Members");
        for (String m : currentGroup.getMemberUsernames()) {
            cmbFilterMember.addItem(m);
        }
    }

    private void refreshExpensesTable() {
        if (currentGroup == null) return;
        expenseTableModel.setRowCount(0);

        String kw = txtSearch.getText().trim();
        if (kw.isEmpty()) kw = null;

        Category cat = null;
        if (cmbFilterCategory.getSelectedIndex() > 0) {
            cat = Category.valueOf((String) cmbFilterCategory.getSelectedItem());
        }

        String member = null;
        if (cmbFilterMember.getSelectedIndex() > 0) {
            member = (String) cmbFilterMember.getSelectedItem();
        }

        List<Expense> expenses = expenseManager.filterExpenses(currentGroup.getGroupId(), member, cat, kw);
        for (Expense e : expenses) {
            expenseTableModel.addRow(new Object[]{
                    e.getExpenseId(),
                    DateUtils.formatDisplay(e.getDate()),
                    e.getCategory().getDisplayName(),
                    e.getDescription(),
                    "@" + e.getPayerUsername(),
                    String.format("%.2f", e.getTotalAmount()),
                    String.join(", ", e.getInvolvedUsernames())
            });
        }
    }

    private void refreshLedgerAndSettlements() {
        if (currentGroup == null) return;
        ledgerTableModel.setRowCount(0);
        settlementListModel.clear();

        List<Participant> participants = expenseManager.calculateGroupLedger(currentGroup.getGroupId());
        for (Participant p : participants) {
            ledgerTableModel.addRow(new Object[]{
                    p.getName() + " (@" + p.getUsername() + ")",
                    String.format("%.2f", p.getAmountPaid()),
                    String.format("%.2f", p.getFairShare()),
                    String.format("%.2f", p.getNetBalance()),
                    p.getStatusDescription()
            });
        }

        List<Settlement> settlements = expenseManager.calculateGroupSettlements(currentGroup.getGroupId());
        if (settlements.isEmpty()) {
            settlementListModel.addElement("✅ Everyone is completely settled up! No transfers needed.");
        } else {
            for (Settlement s : settlements) {
                settlementListModel.addElement("➔ " + s.getDebtorName() + " shall pay ₹" + String.format("%.2f", s.getAmount()) + " to " + s.getCreditorName());
            }
        }
    }

    private void refreshAuditTable() {
        auditTableModel.setRowCount(0);
        List<AuditLog> logs = auditService.getAllLogs();
        for (int i = logs.size() - 1; i >= 0; i--) {
            AuditLog l = logs.get(i);
            auditTableModel.addRow(new Object[]{
                    l.getLogId(),
                    l.getTimestamp(),
                    l.getEventType().name(),
                    "@" + l.getActorUsername(),
                    l.getSummary(),
                    l.getDetails()
            });
        }
    }

    private void onAddExpense() {
        if (currentGroup == null) {
            JOptionPane.showMessageDialog(this,
                    "No group is selected! Please create a new group or join an existing group first.",
                    "No Active Group", JOptionPane.WARNING_MESSAGE);
            return;
        }
        ExpenseDialog dlg = new ExpenseDialog(this, currentGroup, currentUser.getUsername(), expenseManager, userManager, null);
        dlg.setVisible(true);
        if (dlg.isSaved()) {
            refreshAllViews();
        }
    }

    private void onEditExpense() {
        if (currentGroup == null) {
            JOptionPane.showMessageDialog(this, "Please select a group first.", "No Active Group", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int row = tblExpenses.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Please select an expense from the table to edit.", "Selection Required", JOptionPane.WARNING_MESSAGE);
            return;
        }
        String expId = (String) expenseTableModel.getValueAt(row, 0);
        Expense exp = expenseManager.getExpenseById(expId);
        if (exp != null) {
            ExpenseDialog dlg = new ExpenseDialog(this, currentGroup, currentUser.getUsername(), expenseManager, userManager, exp);
            dlg.setVisible(true);
            if (dlg.isSaved()) {
                refreshAllViews();
            }
        }
    }

    private void onDeleteExpense() {
        if (currentGroup == null) {
            JOptionPane.showMessageDialog(this, "Please select a group first.", "No Active Group", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int row = tblExpenses.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Please select an expense from the table to delete.", "Selection Required", JOptionPane.WARNING_MESSAGE);
            return;
        }
        String expId = (String) expenseTableModel.getValueAt(row, 0);
        String desc = (String) expenseTableModel.getValueAt(row, 3);
        int confirm = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to delete expense '" + desc + "' [" + expId + "]?",
                "Confirm Deletion", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            expenseManager.deleteExpense(expId, currentUser.getUsername());
            refreshAllViews();
        }
    }

    private void promptCreateGroup() {
        JTextField nameField = new JTextField(25);
        nameField.setPreferredSize(new Dimension(320, 32));
        JTextField descField = new JTextField(25);
        descField.setPreferredSize(new Dimension(320, 32));
        Object[] fields = {
                "Group Name (e.g. Manali Trip, Flat 204):", nameField,
                "Description / Purpose:", descField
        };
        int result = JOptionPane.showConfirmDialog(this, fields, "Create New Expense Group", JOptionPane.OK_CANCEL_OPTION);
        if (result == JOptionPane.OK_OPTION) {
            String name = nameField.getText().trim();
            if (!name.isEmpty()) {
                Group g = groupManager.createGroup(name, descField.getText().trim(), currentUser.getUsername());
                loadUserGroups();
                // Select newly created group
                for (int i = 0; i < cmbGroups.getItemCount(); i++) {
                    if (cmbGroups.getItemAt(i).group.getGroupId().equalsIgnoreCase(g.getGroupId())) {
                        cmbGroups.setSelectedIndex(i);
                        break;
                    }
                }
                JOptionPane.showMessageDialog(this, "Group created successfully!\nGroup ID: " + g.getGroupId(), "Success", JOptionPane.INFORMATION_MESSAGE);
            }
        }
    }

    private void promptJoinGroup() {
        String groupId = JOptionPane.showInputDialog(this, "Enter Group ID (e.g. GRP-101):", "Join Existing Group", JOptionPane.QUESTION_MESSAGE);
        if (groupId != null && !groupId.trim().isEmpty()) {
            try {
                boolean joined = groupManager.joinGroup(groupId.trim(), currentUser.getUsername());
                if (joined) {
                    loadUserGroups();
                    JOptionPane.showMessageDialog(this, "Successfully joined group!", "Joined", JOptionPane.INFORMATION_MESSAGE);
                } else {
                    JOptionPane.showMessageDialog(this, "You are already a member of this group.", "Info", JOptionPane.INFORMATION_MESSAGE);
                }
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void promptAddMember() {
        if (currentGroup == null) return;
        String username = JOptionPane.showInputDialog(this, "Enter username of the user to add to " + currentGroup.getName() + ":", "Add Member", JOptionPane.QUESTION_MESSAGE);
        if (username != null && !username.trim().isEmpty()) {
            try {
                boolean joined = groupManager.joinGroup(currentGroup.getGroupId(), username.trim().toLowerCase());
                if (joined) {
                    refreshAllViews();
                    JOptionPane.showMessageDialog(this, "User @" + username.trim().toLowerCase() + " added to group!", "Success", JOptionPane.INFORMATION_MESSAGE);
                } else {
                    JOptionPane.showMessageDialog(this, "User is already in this group.", "Info", JOptionPane.INFORMATION_MESSAGE);
                }
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void runInvariantAudit() {
        if (currentGroup == null) return;
        List<Expense> expenses = expenseManager.getExpensesForGroup(currentGroup.getGroupId());
        List<Participant> participants = expenseManager.calculateGroupLedger(currentGroup.getGroupId());

        AuditService.IntegrityResult res = auditService.verifyLedgerIntegrity(currentGroup.getGroupId(), expenses, participants);
        JTextArea txt = new JTextArea(res.getFormattedReport());
        txt.setEditable(false);
        txt.setFont(new Font("Monospaced", Font.PLAIN, 12));
        JScrollPane scroll = new JScrollPane(txt);
        scroll.setPreferredSize(new Dimension(550, 320));

        JOptionPane.showMessageDialog(this, scroll, "System Invariant Audit Report",
                res.isPassed() ? JOptionPane.INFORMATION_MESSAGE : JOptionPane.WARNING_MESSAGE);
        refreshAuditTable();
    }

    private void exportAuditReport() {
        if (currentGroup == null) return;
        List<Expense> expenses = expenseManager.getExpensesForGroup(currentGroup.getGroupId());
        List<Participant> participants = expenseManager.calculateGroupLedger(currentGroup.getGroupId());
        AuditService.IntegrityResult res = auditService.verifyLedgerIntegrity(currentGroup.getGroupId(), expenses, participants);

        try {
            Path path = auditService.exportAuditReport(currentGroup.getGroupId(), res.getFormattedReport());
            JOptionPane.showMessageDialog(this, "Audit report exported successfully to:\n" + path.toAbsolutePath(), "Export Successful", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Export failed: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void switchUser() {
        dispose();
        LoginDialog loginDlg = new LoginDialog(null, userManager);
        loginDlg.setVisible(true);
        User user = loginDlg.getAuthenticatedUser();
        if (user != null) {
            SwingUtilities.invokeLater(() -> new MainFrame(userManager, groupManager, expenseManager, auditService, user).setVisible(true));
        } else {
            System.exit(0);
        }
    }
}
