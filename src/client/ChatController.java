package client;

import shared.ChatService;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.rmi.RemoteException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.GradientPaint;
import java.awt.RenderingHints;

public class ChatController {
    private final JFrame frame;
    private final ChatService chatService;
    private final String username;
    private final Runnable onLogout;
    private final DefaultListModel<String> userListModel;
    private JList<String> usersListView;
    private final JPanel chatPanel;
    private final JScrollPane chatScrollPane;
    private final JTextField messageField;
    private final JLabel currentChatLabel;
    private String chatPartner;
    private final Timer poller;

    public ChatController(ChatService chatService, String username, Runnable onLogout) {
        this.chatService = chatService;
        this.username = username;
        this.onLogout = onLogout;
        this.userListModel = new DefaultListModel<>();
        this.chatPanel = new JPanel() {
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                GradientPaint gp = new GradientPaint(0, 0, new Color(241, 245, 249), getWidth(), getHeight(), new Color(226, 232, 240));
                g2.setPaint(gp);
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.setColor(new Color(255, 255, 255, 40));
                g2.fillOval(getWidth() - 100, 50, 200, 200);
                g2.fillOval(-50, getHeight() - 150, 150, 150);
                g2.dispose();
            }
        };
        this.chatPanel.setLayout(new BoxLayout(this.chatPanel, BoxLayout.Y_AXIS));
        this.chatScrollPane = new JScrollPane(this.chatPanel);
        this.chatScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        this.chatScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        this.chatScrollPane.setBorder(BorderFactory.createEmptyBorder());
        this.messageField = new JTextField();
        this.currentChatLabel = new JLabel("Select a user to start chat");
        this.frame = createFrame();
        this.poller = new Timer(2000, e -> refreshUserList());
        this.refreshUserList();
        this.poller.start();
    }

    public ChatController(ChatService chatService, String username) {
        this(chatService, username, () -> {});
    }

    public void show() {
        frame.setVisible(true);
    }

    private JFrame createFrame() {
        JFrame frame = new JFrame("Distributed Chat - " + username);
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
        profile.add(new ModernUI.AvatarCircle(username, 45), BorderLayout.WEST);

        JPanel profileText = new JPanel(new GridLayout(2, 1));
        profileText.setOpaque(false);
        JLabel nameLabel = new JLabel(username);
        nameLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        JLabel statusLabel = new JLabel("● Online");
        statusLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        statusLabel.setForeground(ModernUI.SUCCESS);
        profileText.add(nameLabel);
        profileText.add(statusLabel);
        profile.add(profileText, BorderLayout.CENTER);
        sidebar.add(profile, BorderLayout.NORTH);

        JPanel userListPanel = new JPanel(new BorderLayout(0, 15));
        userListPanel.setOpaque(false);
        userListPanel.setBorder(BorderFactory.createEmptyBorder(0, 20, 20, 20));
        ModernUI.SearchField searchField = new ModernUI.SearchField("Search users...");
        userListPanel.add(searchField, BorderLayout.NORTH);

        usersListView = new JList<>(userListModel);
        usersListView.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        usersListView.setBackground(new Color(249, 250, 251));
        usersListView.setCellRenderer(new UserListRenderer());
        usersListView.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
        usersListView.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                String selected = usersListView.getSelectedValue();
                if (selected != null) {
                    chatPartner = selected;
                    currentChatLabel.setText(chatPartner);
                    loadChatHistory();
                }
            }
        });

        JScrollPane userScroll = new JScrollPane(usersListView);
        ModernUI.applyCustomScrollbar(userScroll);
        userListPanel.add(userScroll, BorderLayout.CENTER);
        sidebar.add(userListPanel, BorderLayout.CENTER);

        // Header
        JPanel header = new JPanel(new BorderLayout()) {
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setPaint(new GradientPaint(0, 0, new Color(255, 255, 255), getWidth(), 0, new Color(243, 244, 246)));
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.dispose();
            }
        };
        header.setPreferredSize(new Dimension(0, 85));
        header.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(229, 231, 235)));

        JPanel chatLabelPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 30, 20));
        chatLabelPanel.setOpaque(false);
        currentChatLabel.setFont(new Font("Segoe UI", Font.BOLD, 22));
        currentChatLabel.setForeground(new Color(15, 23, 42));
        chatLabelPanel.add(currentChatLabel);
        header.add(chatLabelPanel, BorderLayout.WEST);

        // Header Buttons
        JPanel headerButtons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 22));
        headerButtons.setOpaque(false);

        ModernUI.GradientButton tasksBtn = new ModernUI.GradientButton("My Tasks", ModernUI.SUCCESS, new Color(4, 120, 87));
        tasksBtn.setPreferredSize(new Dimension(110, 40));
        tasksBtn.addActionListener(e -> {
            TaskController taskController = new TaskController(chatService, username);
            taskController.show();
        });

        ModernUI.GradientButton changePwdBtn = new ModernUI.GradientButton("Password", new Color(107, 114, 128), new Color(75, 85, 99));
        changePwdBtn.setPreferredSize(new Dimension(110, 40));
        changePwdBtn.addActionListener(e -> {
            ChangePasswordController cpController = new ChangePasswordController(chatService, username);
            cpController.show();
        });

        ModernUI.GradientButton logoutBtn = new ModernUI.GradientButton("Logout", ModernUI.DANGER, new Color(185, 28, 28));
        logoutBtn.setPreferredSize(new Dimension(110, 40));
        logoutBtn.addActionListener(e -> logout());

        headerButtons.add(tasksBtn);
        headerButtons.add(changePwdBtn);
        headerButtons.add(logoutBtn);
        header.add(headerButtons, BorderLayout.EAST);

        // Chat Area
        JPanel chatArea = new JPanel(new BorderLayout());
        chatArea.setBackground(new Color(241, 245, 249));
        ModernUI.applyCustomScrollbar(chatScrollPane);
        chatArea.add(chatScrollPane, BorderLayout.CENTER);

        // Message Input
        JPanel inputPanel = new JPanel(new BorderLayout(20, 0));
        inputPanel.setBackground(Color.WHITE);
        inputPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(229, 231, 235)),
                BorderFactory.createEmptyBorder(20, 30, 20, 30)));
        messageField.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        messageField.setBorder(BorderFactory.createCompoundBorder(
                new ModernUI.RoundedBorder(new Color(226, 232, 240), 15),
                BorderFactory.createEmptyBorder(12, 20, 12, 20)));
        ModernUI.GradientButton sendBtn = new ModernUI.GradientButton("Send", ModernUI.PRIMARY, new Color(29, 78, 216));
        sendBtn.setPreferredSize(new Dimension(100, 45));
        sendBtn.addActionListener(e -> sendMessage());
        messageField.addActionListener(e -> sendMessage());
        inputPanel.add(messageField, BorderLayout.CENTER);
        inputPanel.add(sendBtn, BorderLayout.EAST);

        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.add(header, BorderLayout.NORTH);
        mainPanel.add(chatArea, BorderLayout.CENTER);
        mainPanel.add(inputPanel, BorderLayout.SOUTH);

        frame.add(sidebar, BorderLayout.WEST);
        frame.add(mainPanel, BorderLayout.CENTER);

        return frame;
    }

    private void refreshUserList() {
        new Thread(() -> {
            try {
                List<String> users = chatService.getAllUsers(username);
                SwingUtilities.invokeLater(() -> {
                    userListModel.clear();
                    for (String u : users) userListModel.addElement(u);
                });
            } catch (RemoteException e) {
                SwingUtilities.invokeLater(() -> addMessageBubble("Unable to refresh user list.", false));
                e.printStackTrace();
            }
        }).start();
    }

    private void loadChatHistory() {
        if (chatPartner != null) {
            new Thread(() -> {
                try {
                    List<String> msgs = chatService.getMessages(username, chatPartner);
                    SwingUtilities.invokeLater(() -> {
                        chatPanel.removeAll();
                        for (String msg : msgs) {
                            String formatted = formatMessage(msg);
                            boolean isMine = formatted.contains("You:");
                            addMessageBubble(formatted.substring(formatted.indexOf(":") + 1).trim(), isMine);
                        }
                        chatPanel.revalidate();
                        chatPanel.repaint();
                    });
                } catch (RemoteException e) {
                    SwingUtilities.invokeLater(() -> addMessageBubble("Unable to load chat history.", false));
                    e.printStackTrace();
                }
            }).start();
        }
    }

    private String formatMessage(String msg) {
        if (msg == null || msg.isBlank()) return msg;
        int idx = msg.indexOf("] ");
        if (idx < 0) return msg;
        int colonIdx = msg.indexOf(":", idx + 2);
        if (colonIdx < 0) return msg;
        String sender = msg.substring(idx + 2, colonIdx).trim();
        if (sender.equals(username)) {
            return msg.substring(0, idx + 2) + "You" + msg.substring(colonIdx);
        }
        return msg;
    }

    private void sendMessage() {
        if (chatPartner == null || chatPartner.isEmpty()) {
            JOptionPane.showMessageDialog(frame, "Please select a user before sending a message.", "No user selected", JOptionPane.WARNING_MESSAGE);
            return;
        }
        String text = messageField.getText().trim();
        if (!text.isEmpty()) {
            messageField.setText("");
            new Thread(() -> {
                try {
                    String result = chatService.sendMessage(username, chatPartner, text);
                    if (result.contains("sent")) {
                        SwingUtilities.invokeLater(() -> addMessageBubble(text, true));
                    } else {
                        SwingUtilities.invokeLater(() -> addMessageBubble(result, false));
                    }
                } catch (RemoteException e) {
                    SwingUtilities.invokeLater(() -> addMessageBubble("Unable to send message.", false));
                    e.printStackTrace();
                }
            }).start();
        }
    }

    private void addMessageBubble(String text, final boolean isMine) {
        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setOpaque(false);
        wrapper.setBorder(BorderFactory.createEmptyBorder(10, 30, 10, 30));
        final int[] alpha = {0};
        JPanel bubble = new JPanel(new BorderLayout()) {
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha[0] / 100f));
                g2.setColor(new Color(0, 0, 0, 15));
                g2.fillRoundRect(3, 3, getWidth() - 6, getHeight() - 6, 22, 22);
                if (isMine) {
                    g2.setPaint(new GradientPaint(0, 0, new Color(59, 130, 246), getWidth(), getHeight(), new Color(139, 92, 246)));
                } else {
                    g2.setColor(Color.WHITE);
                }
                g2.fillRoundRect(0, 0, getWidth() - 3, getHeight() - 3, 22, 22);
                if (!isMine) {
                    g2.setColor(new Color(226, 232, 240));
                    g2.setStroke(new BasicStroke(1.2f));
                    g2.drawRoundRect(0, 0, getWidth() - 4, getHeight() - 4, 22, 22);
                }
                g2.dispose();
            }
        };
        bubble.setOpaque(false);
        bubble.setBorder(BorderFactory.createEmptyBorder(15, 20, 10, 20));
        JLabel msgLabel = new JLabel("<html><p style='width: 320px; color: " + (isMine ? "white" : "#334155") + "; font-size: 11px;'>" + text.replace("\n", "<br>") + "</p></html>");
        msgLabel.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        bubble.add(msgLabel, BorderLayout.CENTER);
        String time = new SimpleDateFormat("HH:mm").format(new Date());
        JLabel timeLabel = new JLabel(time + (isMine ? " ✓" : ""));
        timeLabel.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        timeLabel.setForeground(isMine ? new Color(224, 231, 255) : new Color(148, 163, 184));
        timeLabel.setHorizontalAlignment(isMine ? SwingConstants.RIGHT : SwingConstants.LEFT);
        bubble.add(timeLabel, BorderLayout.SOUTH);
        wrapper.add(bubble, isMine ? BorderLayout.EAST : BorderLayout.WEST);
        chatPanel.add(wrapper);
        chatPanel.revalidate();
        Timer fadeTimer = new Timer(15, null);
        fadeTimer.addActionListener(e -> {
            alpha[0] += 10;
            bubble.repaint();
            if (alpha[0] >= 100) { alpha[0] = 100; fadeTimer.stop(); }
        });
        fadeTimer.start();
        SwingUtilities.invokeLater(() -> {
            JScrollBar sb = chatScrollPane.getVerticalScrollBar();
            sb.setValue(sb.getMaximum());
        });
    }

    private void logout() {
        poller.stop();
        new Thread(() -> {
            try { chatService.logout(username); } catch (RemoteException e) { e.printStackTrace(); }
            SwingUtilities.invokeLater(() -> {
                frame.dispose();
                JOptionPane.showMessageDialog(null, "You have been logged out.", "Logged out", JOptionPane.INFORMATION_MESSAGE);
                onLogout.run();
            });
        }).start();
    }

    private class UserListRenderer extends DefaultListCellRenderer {
        public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            String username = (String) value;
            JPanel panel = new JPanel(new BorderLayout(15, 0));
            panel.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));
            panel.setOpaque(true);
            ModernUI.AvatarCircle avatar = new ModernUI.AvatarCircle(username, 35);
            panel.add(avatar, BorderLayout.WEST);
            JLabel label = new JLabel(username);
            label.setFont(new Font("Segoe UI", isSelected ? Font.BOLD : Font.PLAIN, 14));
            panel.add(label, BorderLayout.CENTER);
            if (isSelected) {
                panel.setBackground(new Color(239, 246, 255));
                label.setForeground(ModernUI.PRIMARY);
            } else {
                panel.setBackground(new Color(249, 250, 251));
                label.setForeground(ModernUI.TEXT_MAIN);
            }
            return panel;
        }
    }
}