package client;

import shared.ChatService;

import javax.swing.*;
import java.awt.*;
import java.rmi.RemoteException;

public class ChangePasswordController {
    private final JFrame frame;
    private final ChatService chatService;
    private final String username;

    public ChangePasswordController(ChatService chatService, String username) {
        this.chatService = chatService;
        this.username = username;
        this.frame = createFrame();
    }

    public void show() {
        frame.setVisible(true);
    }

    private JFrame createFrame() {
        JFrame frame = new JFrame("Change Password - " + username);
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setSize(420, 320);
        frame.setLocationRelativeTo(null);
        frame.setLayout(new BorderLayout());
        frame.getContentPane().setBackground(ModernUI.BACKGROUND);

        // Title
        JLabel title = new JLabel("Change Password");
        title.setFont(new Font("Segoe UI", Font.BOLD, 22));
        title.setBorder(BorderFactory.createEmptyBorder(25, 30, 10, 30));
        frame.add(title, BorderLayout.NORTH);

        // Form
        JPanel form = new JPanel(new GridLayout(3, 2, 10, 15));
        form.setOpaque(false);
        form.setBorder(BorderFactory.createEmptyBorder(10, 30, 10, 30));

        JPasswordField oldPassField = new JPasswordField();
        JPasswordField newPassField = new JPasswordField();
        JPasswordField confirmPassField = new JPasswordField();

        form.add(new JLabel("Current Password:"));
        form.add(oldPassField);
        form.add(new JLabel("New Password:"));
        form.add(newPassField);
        form.add(new JLabel("Confirm Password:"));
        form.add(confirmPassField);

        frame.add(form, BorderLayout.CENTER);

        // Buttons
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 15));
        btnPanel.setOpaque(false);

        ModernUI.RoundedButton saveBtn = new ModernUI.RoundedButton("Save", ModernUI.SUCCESS, ModernUI.SUCCESS_HOVER);
        ModernUI.RoundedButton cancelBtn = new ModernUI.RoundedButton("Cancel", new Color(107, 114, 128), new Color(75, 85, 99));
        saveBtn.setPreferredSize(new Dimension(100, 38));
        cancelBtn.setPreferredSize(new Dimension(100, 38));

        saveBtn.addActionListener(e -> {
            String oldPass = new String(oldPassField.getPassword()).trim();
            String newPass = new String(newPassField.getPassword()).trim();
            String confirmPass = new String(confirmPassField.getPassword()).trim();

            if (oldPass.isEmpty() || newPass.isEmpty() || confirmPass.isEmpty()) {
                JOptionPane.showMessageDialog(frame, "All fields are required!");
                return;
            }
            if (!newPass.equals(confirmPass)) {
                JOptionPane.showMessageDialog(frame, "New passwords do not match!");
                return;
            }
            if (newPass.length() < 4) {
                JOptionPane.showMessageDialog(frame, "Password must be at least 4 characters!");
                return;
            }
            try {
                boolean success = chatService.changePassword(username, oldPass, newPass);
                if (success) {
                    ModernUI.showToast(frame, "Password changed successfully!", ModernUI.SUCCESS);
                    frame.dispose();
                } else {
                    JOptionPane.showMessageDialog(frame, "Current password is incorrect!");
                }
            } catch (RemoteException ex) {
                ex.printStackTrace();
            }
        });

        cancelBtn.addActionListener(e -> frame.dispose());
        btnPanel.add(cancelBtn);
        btnPanel.add(saveBtn);
        frame.add(btnPanel, BorderLayout.SOUTH);

        return frame;
    }
}