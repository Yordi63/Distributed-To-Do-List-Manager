package client;

import shared.ChatService;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.rmi.RemoteException;
import java.util.function.Consumer;

public class LoginController {
    private final JFrame frame;
    private final ChatService chatService;
    private final Consumer<String> onLoginSuccess;

    public LoginController(ChatService chatService, Consumer<String> onLoginSuccess) {
        this.chatService = chatService;
        this.onLoginSuccess = onLoginSuccess;
        this.frame = createFrame();
    }

    public void show() {
        frame.setVisible(true);
    }

    private JFrame createFrame() {
        JFrame frame = new JFrame("Distributed Chat");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(450, 480);
        frame.setLocationRelativeTo(null);
        frame.getContentPane().setBackground(ModernUI.BACKGROUND);
        
        // Custom background panel with animated blobs
        JPanel bgPanel = new JPanel(new GridBagLayout()) {
            private float angle = 0;
            {
                Timer t = new Timer(50, e -> {
                    angle += 0.02;
                    repaint();
                });
                t.start();
            }
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                // Base Gradient
                GradientPaint gp = new GradientPaint(0, 0, new Color(238, 242, 255), 
                                                   getWidth(), getHeight(), new Color(224, 231, 255));
                g2.setPaint(gp);
                g2.fillRect(0, 0, getWidth(), getHeight());
                
                // Animated Blobs
                g2.setColor(new Color(99, 102, 241, 40));
                int x1 = (int) (Math.cos(angle) * 100 + getWidth() / 2 - 200);
                int y1 = (int) (Math.sin(angle * 0.5) * 100 + getHeight() / 2 - 200);
                g2.fillOval(x1, y1, 400, 400);
                
                g2.setColor(new Color(168, 85, 247, 30));
                int x2 = (int) (Math.sin(angle * 0.8) * 150 + getWidth() / 2 - 150);
                int y2 = (int) (Math.cos(angle * 0.3) * 150 + getHeight() / 2 - 150);
                g2.fillOval(x2, y2, 350, 350);
                
                g2.dispose();
            }
        };
        frame.setContentPane(bgPanel);
        frame.setLayout(new GridBagLayout());

        ModernUI.GlassPanel card = new ModernUI.GlassPanel(220);
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(BorderFactory.createEmptyBorder(40, 40, 40, 40));
        card.setPreferredSize(new Dimension(380, 400));

        JLabel titleLabel = new JLabel("Welcome Back");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 32));
        titleLabel.setForeground(new Color(20, 43, 89));
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel subtitleLabel = new JLabel("Join the conversation");
        subtitleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        subtitleLabel.setForeground(ModernUI.TEXT_MUTED);
        subtitleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        subtitleLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 35, 0));

        JTextField usernameField = ModernUI.createModernField("Username");
        usernameField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 45));

        JPasswordField passwordField = ModernUI.createModernPasswordField("Password");
        passwordField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 45));

        JLabel statusLabel = new JLabel(" ");
        statusLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        statusLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        statusLabel.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));

        ModernUI.GradientButton loginButton = new ModernUI.GradientButton("Sign In", new Color(37, 99, 235), new Color(124, 58, 237));
        loginButton.setMaximumSize(new Dimension(Integer.MAX_VALUE, 45));

        ModernUI.GradientButton registerButton = new ModernUI.GradientButton("Create Account", new Color(16, 185, 129), new Color(5, 150, 105));
        registerButton.setMaximumSize(new Dimension(Integer.MAX_VALUE, 45));

        card.add(titleLabel);
        card.add(subtitleLabel);
        card.add(usernameField);
        card.add(Box.createRigidArea(new Dimension(0, 15)));
        card.add(passwordField);
        card.add(statusLabel);
        card.add(loginButton);
        card.add(Box.createRigidArea(new Dimension(0, 10)));
        card.add(registerButton);

        loginButton.addActionListener(e -> {
            String username = usernameField.getText().trim();
            String password = new String(passwordField.getPassword());
            if (username.equals("Username") || password.equals("Password")) {
                statusLabel.setForeground(ModernUI.DANGER);
                statusLabel.setText("Please fill in all fields.");
                return;
            }
            try {
                String result = chatService.login(username, password);
                if (result.contains("successful")) {
                    String role = chatService.getUserRole(username);
                    frame.dispose();
                    if ("admin".equalsIgnoreCase(role)) {
                        AdminController adminController = new AdminController(chatService, username, () -> {
                            // On logout, show login again
                            new LoginController(chatService, onLoginSuccess).show();
                        });
                        adminController.show();
                    } else {
                        onLoginSuccess.accept(username);
                    }
                } else {
                    statusLabel.setForeground(ModernUI.DANGER);
                    statusLabel.setText(result);
                }
            } catch (RemoteException ex) {
                statusLabel.setForeground(ModernUI.DANGER);
                statusLabel.setText("Connection Error");
            }
        });

        registerButton.addActionListener(e -> {
            JDialog regDialog = new JDialog(frame, "Create New Account", true);
            regDialog.setLayout(new BorderLayout());
            regDialog.setSize(400, 550);
            regDialog.setLocationRelativeTo(frame);

            ModernUI.RoundedPanel content = new ModernUI.RoundedPanel(0);
            content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
            content.setBorder(BorderFactory.createEmptyBorder(30, 40, 30, 40));
            content.setBackground(Color.WHITE);

            JLabel title = new JLabel("Join Us");
            title.setFont(new Font("Segoe UI", Font.BOLD, 24));
            title.setAlignmentX(Component.CENTER_ALIGNMENT);
            
            JTextField uField = ModernUI.createModernField("Username");
            JPasswordField pField = ModernUI.createModernPasswordField("Password");
            JTextField nField = ModernUI.createModernField("Full Name");
            JTextField aField = ModernUI.createModernField("Age");
            JTextField phField = ModernUI.createModernField("Phone Number");

            Dimension fieldDim = new Dimension(Integer.MAX_VALUE, 45);
            uField.setMaximumSize(fieldDim);
            pField.setMaximumSize(fieldDim);
            nField.setMaximumSize(fieldDim);
            aField.setMaximumSize(fieldDim);
            phField.setMaximumSize(fieldDim);

            ModernUI.GradientButton submitBtn = new ModernUI.GradientButton("Register", ModernUI.SUCCESS, new Color(5, 150, 105));
            submitBtn.setMaximumSize(fieldDim);
            submitBtn.setAlignmentX(Component.CENTER_ALIGNMENT);

            content.add(title);
            content.add(Box.createVerticalStrut(25));
            content.add(uField);
            content.add(Box.createVerticalStrut(15));
            content.add(pField);
            content.add(Box.createVerticalStrut(15));
            content.add(nField);
            content.add(Box.createVerticalStrut(15));
            content.add(aField);
            content.add(Box.createVerticalStrut(15));
            content.add(phField);
            content.add(Box.createVerticalStrut(30));
            content.add(submitBtn);

            submitBtn.addActionListener(ev -> {
                try {
                    String user = uField.getText();
                    String pass = new String(pField.getPassword());
                    String name = nField.getText();
                    int age = Integer.parseInt(aField.getText().trim());
                    String phone = phField.getText();

                    String result = chatService.register(user, pass, name, age, phone);
                    JOptionPane.showMessageDialog(regDialog, result);
                    if (result.contains("successful")) regDialog.dispose();
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(regDialog, "Error: Please check your input (Age must be a number)");
                }
            });

            regDialog.add(content);
            regDialog.setVisible(true);
        });

        frame.add(card);
        return frame;
    }
}
