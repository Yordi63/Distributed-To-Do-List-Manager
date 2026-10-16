package client;

import shared.ChatService;

import javax.swing.*;
import java.awt.*;
import java.rmi.RemoteException;
import java.util.List;

public class UserManagementController {
    private final JFrame frame;
    private final ChatService chatService;
    private final String adminUsername;
    private final DefaultListModel<String> userListModel;

    public UserManagementController(ChatService chatService, String adminUsername) {
        this.chatService = chatService;
        this.adminUsername = adminUsername;
        this.userListModel = new DefaultListModel<>();
        this.frame = createFrame();
        refreshUsers();
    }

    public void show() {
        frame.setVisible(true);
    }

    private JFrame createFrame() {
        JFrame frame = new JFrame("User Management - " + adminUsername);
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setSize(700, 550);
        frame.setLocationRelativeTo(null);
        frame.setLayout(new BorderLayout());
        frame.getContentPane().setBackground(ModernUI.BACKGROUND);

        // Title
        JLabel title = new JLabel("User Management");
        title.setFont(new Font("Segoe UI", Font.BOLD, 24));
        title.setBorder(BorderFactory.createEmptyBorder(20, 30, 10, 30));
        frame.add(title, BorderLayout.NORTH);

        // User List
        JList<String> userList = new JList<>(userListModel);
        userList.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        userList.setFixedCellHeight(50);
        JScrollPane scroll = new JScrollPane(userList);
        ModernUI.applyCustomScrollbar(scroll);
        scroll.setBorder(BorderFactory.createEmptyBorder(0, 20, 0, 20));
        frame.add(scroll, BorderLayout.CENTER);

        // Buttons
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 15));
        actions.setOpaque(false);
        actions.setBorder(BorderFactory.createEmptyBorder(0, 20, 10, 20));

        ModernUI.RoundedButton editBtn = new ModernUI.RoundedButton("Edit Info", ModernUI.PRIMARY, ModernUI.PRIMARY_HOVER);
        ModernUI.RoundedButton resetBtn = new ModernUI.RoundedButton("Reset Password", new Color(107, 114, 128), new Color(75, 85, 99));
        ModernUI.RoundedButton deleteBtn = new ModernUI.RoundedButton("Delete", ModernUI.DANGER, new Color(220, 38, 38));

        editBtn.setPreferredSize(new Dimension(120, 40));
        resetBtn.setPreferredSize(new Dimension(150, 40));
        deleteBtn.setPreferredSize(new Dimension(100, 40));

        editBtn.addActionListener(e -> {
            String selected = userList.getSelectedValue();
            if (selected != null) {
                showEditDialog(selected);
            } else {
                JOptionPane.showMessageDialog(frame, "Please select a user to edit.");
            }
        });

        resetBtn.addActionListener(e -> {
            String selected = userList.getSelectedValue();
            if (selected != null) {
                String newPass = JOptionPane.showInputDialog(frame,
                        "Enter new password for " + selected + ":");
                if (newPass != null && !newPass.isBlank()) {
                    try {
                        if (chatService.resetPassword(selected, newPass)) {
                            ModernUI.showToast(frame, "Password reset successfully!", ModernUI.PRIMARY);
                        }
                    } catch (RemoteException ex) { ex.printStackTrace(); }
                }
            } else {
                JOptionPane.showMessageDialog(frame, "Please select a user.");
            }
        });

        deleteBtn.addActionListener(e -> {
            String selected = userList.getSelectedValue();
            if (selected != null) {
                if (selected.equals(adminUsername)) {
                    JOptionPane.showMessageDialog(frame, "You cannot delete yourself!");
                    return;
                }
                int confirm = JOptionPane.showConfirmDialog(frame,
                        "Are you sure you want to delete user " + selected + "?",
                        "Confirm Delete", JOptionPane.YES_NO_OPTION);
                if (confirm == JOptionPane.YES_OPTION) {
                    try {
                        if (chatService.deleteUser(selected)) {
                            ModernUI.showToast(frame, "User deleted!", ModernUI.DANGER);
                            refreshUsers();
                        }
                    } catch (RemoteException ex) { ex.printStackTrace(); }
                }
            } else {
                JOptionPane.showMessageDialog(frame, "Please select a user to delete.");
            }
        });

        actions.add(editBtn);
        actions.add(resetBtn);
        actions.add(deleteBtn);
        frame.add(actions, BorderLayout.SOUTH);

        return frame;
    }

    private void showEditDialog(String username) {
        JDialog dialog = new JDialog(frame, "Edit User - " + username, true);
        dialog.setSize(400, 280);
        dialog.setLocationRelativeTo(frame);
        dialog.setLayout(new BorderLayout());
        dialog.getContentPane().setBackground(ModernUI.BACKGROUND);

        JPanel form = new JPanel(new GridLayout(3, 2, 10, 15));
        form.setOpaque(false);
        form.setBorder(BorderFactory.createEmptyBorder(20, 30, 10, 30));

        JTextField fullNameField = new JTextField();
        JTextField ageField = new JTextField();
        JTextField phoneField = new JTextField();

        form.add(new JLabel("Full Name:"));
        form.add(fullNameField);
        form.add(new JLabel("Age:"));
        form.add(ageField);
        form.add(new JLabel("Phone:"));
        form.add(phoneField);

        dialog.add(form, BorderLayout.CENTER);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 15));
        btnPanel.setOpaque(false);

        ModernUI.RoundedButton saveBtn = new ModernUI.RoundedButton("Save", ModernUI.SUCCESS, ModernUI.SUCCESS_HOVER);
        ModernUI.RoundedButton cancelBtn = new ModernUI.RoundedButton("Cancel", new Color(107, 114, 128), new Color(75, 85, 99));
        saveBtn.setPreferredSize(new Dimension(100, 38));
        cancelBtn.setPreferredSize(new Dimension(100, 38));

        saveBtn.addActionListener(e -> {
            String fullName = fullNameField.getText().trim();
            String ageStr = ageField.getText().trim();
            String phone = phoneField.getText().trim();

            if (fullName.isEmpty() || ageStr.isEmpty() || phone.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "All fields are required!");
                return;
            }
            try {
                int age = Integer.parseInt(ageStr);
                if (chatService.updateUser(username, fullName, age, phone)) {
                    ModernUI.showToast(frame, "User updated successfully!", ModernUI.SUCCESS);
                    refreshUsers();
                    dialog.dispose();
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(dialog, "Age must be a number!");
            } catch (RemoteException ex) {
                ex.printStackTrace();
            }
        });

        cancelBtn.addActionListener(e -> dialog.dispose());
        btnPanel.add(cancelBtn);
        btnPanel.add(saveBtn);
        dialog.add(btnPanel, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }

    private void refreshUsers() {
        new Thread(() -> {
            try {
                List<String> users = chatService.getAllUsersAdmin();
                SwingUtilities.invokeLater(() -> {
                    userListModel.clear();
                    for (String u : users) userListModel.addElement(u);
                });
            } catch (RemoteException e) { e.printStackTrace(); }
        }).start();
    }
}