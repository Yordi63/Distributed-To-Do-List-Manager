package client;

import shared.ChatService;

import javax.swing.*;
import java.awt.*;
import java.rmi.RemoteException;
import java.util.List;

public class AdminController {
    private final JFrame frame;
    private final ChatService chatService;
    private final String adminUsername;
    private final Runnable onLogout;
    private final DefaultListModel<String> pendingListModel;
    private final DefaultListModel<String> allUsersListModel;

    public AdminController(ChatService chatService, String adminUsername, Runnable onLogout) {
        this.chatService = chatService;
        this.adminUsername = adminUsername;
        this.onLogout = onLogout;
        this.pendingListModel = new DefaultListModel<>();
        this.allUsersListModel = new DefaultListModel<>();
        this.frame = createFrame();
        refreshData();
    }

    public void show() {
        frame.setVisible(true);
    }

    private JFrame createFrame() {
        JFrame frame = new JFrame("Admin Dashboard - " + adminUsername);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(1000, 700);
        frame.setLocationRelativeTo(null);
        frame.setLayout(new BorderLayout());
        frame.getContentPane().setBackground(ModernUI.BACKGROUND);

        // Sidebar
        JPanel sidebar = new JPanel(new BorderLayout());
        sidebar.setPreferredSize(new Dimension(280, 0));
        sidebar.setBackground(new Color(249, 250, 251));
        sidebar.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, new Color(229, 231, 235)));

        JPanel profile = new JPanel(new BorderLayout(15, 0));
        profile.setBackground(new Color(249, 250, 251));
        profile.setBorder(BorderFactory.createEmptyBorder(25, 20, 25, 20));
        profile.add(new ModernUI.AvatarCircle(adminUsername, 45), BorderLayout.WEST);
        
        JPanel profileText = new JPanel(new GridLayout(2, 1));
        profileText.setOpaque(false);
        JLabel nameLabel = new JLabel(adminUsername);
        nameLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        JLabel statusDot = new JLabel("● Admin");
        statusDot.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        statusDot.setForeground(ModernUI.PRIMARY);
        profileText.add(nameLabel);
        profileText.add(statusDot);
        profile.add(profileText, BorderLayout.CENTER);
        sidebar.add(profile, BorderLayout.NORTH);

        ModernUI.RoundedButton logoutBtn = new ModernUI.RoundedButton("Logout", ModernUI.DANGER, new Color(220, 38, 38));
        JPanel logoutWrapper = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 20));
        logoutWrapper.setOpaque(false);
        logoutWrapper.add(logoutBtn);
        sidebar.add(logoutWrapper, BorderLayout.SOUTH);
        logoutBtn.addActionListener(e -> {
            frame.dispose();
            onLogout.run();
        });

        // Main Content
        JTabbedPane tabs = new JTabbedPane();
        tabs.setUI(new ModernUI.ModernTabUI());
        tabs.setFont(new Font("Segoe UI", Font.BOLD, 14));
        tabs.setBackground(ModernUI.BACKGROUND);
        
        tabs.addTab("Pending Approvals", createPendingPanel());
        tabs.addTab("Manage Users", createManageUsersPanel());

        frame.add(sidebar, BorderLayout.WEST);
        frame.add(tabs, BorderLayout.CENTER);

        return frame;
    }

    private JPanel createPendingPanel() {
        JPanel panel = new JPanel(new BorderLayout(20, 20));
        panel.setBackground(ModernUI.BACKGROUND);
        panel.setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));

        JLabel title = new JLabel("Pending User Registrations");
        title.setFont(new Font("Segoe UI", Font.BOLD, 22));
        panel.add(title, BorderLayout.NORTH);

        JList<String> pendingList = new JList<>(pendingListModel);
        pendingList.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        pendingList.setFixedCellHeight(50);
        JScrollPane scroll = new JScrollPane(pendingList);
        ModernUI.applyCustomScrollbar(scroll);
        panel.add(scroll, BorderLayout.CENTER);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0));
        actions.setOpaque(false);
        
        ModernUI.RoundedButton approveBtn = new ModernUI.RoundedButton("Approve", ModernUI.SUCCESS, ModernUI.SUCCESS_HOVER);
        ModernUI.RoundedButton rejectBtn = new ModernUI.RoundedButton("Reject", ModernUI.DANGER, new Color(220, 38, 38));
        
        approveBtn.setPreferredSize(new Dimension(120, 40));
        rejectBtn.setPreferredSize(new Dimension(120, 40));

        approveBtn.addActionListener(e -> {
            String selected = pendingList.getSelectedValue();
            if (selected != null) {
                try {
                    if (chatService.approveUser(selected)) {
                        ModernUI.showToast(frame, "User " + selected + " approved!", ModernUI.SUCCESS);
                        refreshData();
                    }
                } catch (RemoteException ex) { ex.printStackTrace(); }
            }
        });

        rejectBtn.addActionListener(e -> {
            String selected = pendingList.getSelectedValue();
            if (selected != null) {
                try {
                    if (chatService.rejectUser(selected)) {
                        ModernUI.showToast(frame, "User " + selected + " rejected.", ModernUI.DANGER);
                        refreshData();
                    }
                } catch (RemoteException ex) { ex.printStackTrace(); }
            }
        });

        actions.add(rejectBtn);
        actions.add(approveBtn);
        panel.add(actions, BorderLayout.SOUTH);

        return panel;
    }

    private JPanel createManageUsersPanel() {
        JPanel panel = new JPanel(new BorderLayout(20, 20));
        panel.setBackground(ModernUI.BACKGROUND);
        panel.setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));

        JLabel title = new JLabel("Manage All Users");
        title.setFont(new Font("Segoe UI", Font.BOLD, 22));
        panel.add(title, BorderLayout.NORTH);

        JList<String> userList = new JList<>(allUsersListModel);
        userList.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        userList.setFixedCellHeight(50);
        JScrollPane scroll = new JScrollPane(userList);
        ModernUI.applyCustomScrollbar(scroll);
        
        // Detail Label
        JLabel detailLabel = new JLabel("Select a user to see details");
        detailLabel.setFont(new Font("Segoe UI", Font.ITALIC, 14));
        detailLabel.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
        
        userList.addListSelectionListener(e -> {
            String selected = userList.getSelectedValue();
            if (selected != null) {
                // In a real app, we'd fetch profile from server. 
                // For this demo, we'll just show the username in a detail string.
                detailLabel.setText("Editing: " + selected);
            }
        });

        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.setOpaque(false);
        centerPanel.add(scroll, BorderLayout.CENTER);
        centerPanel.add(detailLabel, BorderLayout.SOUTH);
        panel.add(centerPanel, BorderLayout.CENTER);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0));
        actions.setOpaque(false);

        ModernUI.RoundedButton resetBtn = new ModernUI.RoundedButton("Reset Password", ModernUI.PRIMARY, ModernUI.PRIMARY_HOVER);
        ModernUI.RoundedButton blockBtn = new ModernUI.RoundedButton("Block/Unblock", new Color(107, 114, 128), new Color(75, 85, 99));
        ModernUI.RoundedButton deleteBtn = new ModernUI.RoundedButton("Delete", ModernUI.DANGER, new Color(220, 38, 38));

        resetBtn.setPreferredSize(new Dimension(150, 40));
        blockBtn.setPreferredSize(new Dimension(150, 40));
        deleteBtn.setPreferredSize(new Dimension(120, 40));

        resetBtn.addActionListener(e -> {
            String selected = userList.getSelectedValue();
            if (selected != null) {
                String newPass = JOptionPane.showInputDialog(frame, "Enter new password for " + selected + ":");
                if (newPass != null && !newPass.isBlank()) {
                    try {
                        if (chatService.resetPassword(selected, newPass)) {
                            ModernUI.showToast(frame, "Password reset successfully!", ModernUI.PRIMARY);
                        }
                    } catch (RemoteException ex) { ex.printStackTrace(); }
                }
            }
        });

        blockBtn.addActionListener(e -> {
            String selected = userList.getSelectedValue();
            if (selected != null) {
                try {
                    // Simple toggle: for demo, we'll just ask or use a more complex logic.
                    // Here we'll just set it to 'blocked' for simplicity in this demo.
                    if (chatService.updateUserStatus(selected, "blocked")) {
                        JOptionPane.showMessageDialog(frame, "User " + selected + " blocked!");
                    }
                } catch (RemoteException ex) { ex.printStackTrace(); }
            }
        });

        deleteBtn.addActionListener(e -> {
            String selected = userList.getSelectedValue();
            if (selected != null) {
                int confirm = JOptionPane.showConfirmDialog(frame, "Are you sure you want to delete user " + selected + "?", "Confirm Delete", JOptionPane.YES_NO_OPTION);
                if (confirm == JOptionPane.YES_OPTION) {
                    try {
                        if (chatService.deleteUser(selected)) {
                            JOptionPane.showMessageDialog(frame, "User deleted.");
                            refreshData();
                        }
                    } catch (RemoteException ex) { ex.printStackTrace(); }
                }
            }
        });

        actions.add(resetBtn);
        actions.add(blockBtn);
        actions.add(deleteBtn);
        panel.add(actions, BorderLayout.SOUTH);

        return panel;
    }

    private void refreshData() {
        new Thread(() -> {
            try {
                List<String> pending = chatService.getPendingUsers();
                List<String> all = chatService.getAllUsersAdmin();
                SwingUtilities.invokeLater(() -> {
                    pendingListModel.clear();
                    for (String s : pending) pendingListModel.addElement(s);
                    allUsersListModel.clear();
                    for (String s : all) allUsersListModel.addElement(s);
                });
            } catch (RemoteException e) { e.printStackTrace(); }
        }).start();
    }
}
