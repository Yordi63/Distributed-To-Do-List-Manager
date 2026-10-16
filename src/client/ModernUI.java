package client;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;

public class ModernUI {
    public static final Color PRIMARY = new Color(59, 130, 246);
    public static final Color PRIMARY_HOVER = new Color(37, 99, 235);
    public static final Color SUCCESS = new Color(34, 197, 94);
    public static final Color SUCCESS_HOVER = new Color(22, 163, 74);
    public static final Color DANGER = new Color(239, 68, 68);
    public static final Color BACKGROUND = new Color(243, 244, 246);
    public static final Color CARD_BG = Color.WHITE;
    public static final Color TEXT_MAIN = new Color(31, 41, 55);
    public static final Color TEXT_MUTED = new Color(107, 114, 128);
    public static final Color ACCENT = new Color(139, 92, 246);
    public static final Color ACCENT_HOVER = new Color(124, 58, 237);

    public static class RoundedButton extends JButton {
        private Color normalColor;
        private Color hoverColor;
        protected boolean isHovered = false;

        public RoundedButton(String text, Color normal, Color hover) {
            super(text);
            this.normalColor = normal;
            this.hoverColor = hover;
            setContentAreaFilled(false);
            setFocusPainted(false);
            setBorderPainted(false);
            setForeground(Color.WHITE);
            setFont(new Font("Segoe UI", Font.BOLD, 14));
            setCursor(new Cursor(Cursor.HAND_CURSOR));

            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    isHovered = true;
                    repaint();
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    isHovered = false;
                    repaint();
                }
            });
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            if (isHovered) {
                g2.setColor(hoverColor);
            } else {
                g2.setColor(normalColor);
            }
            g2.fill(new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), 12, 12));
            g2.dispose();
            super.paintComponent(g);
        }
    }

    public static class RoundedPanel extends JPanel {
        private int radius;
        private boolean hasShadow = false;

        public RoundedPanel(int radius) {
            this.radius = radius;
            setOpaque(false);
        }

        public RoundedPanel(int radius, boolean hasShadow) {
            this.radius = radius;
            this.hasShadow = hasShadow;
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            
            if (hasShadow) {
                g2.setColor(new Color(0, 0, 0, 15));
                g2.fill(new RoundRectangle2D.Double(2, 2, getWidth() - 4, getHeight() - 4, radius, radius));
            }
            
            g2.setColor(getBackground());
            g2.fill(new RoundRectangle2D.Double(0, 0, getWidth() - (hasShadow ? 2 : 0), getHeight() - (hasShadow ? 2 : 0), radius, radius));
            g2.dispose();
            super.paintComponent(g);
        }
    }

    public static class AvatarCircle extends JPanel {
        private String name;
        private Color bgColor;

        public AvatarCircle(String name, int size) {
            this.name = name;
            setPreferredSize(new Dimension(size, size));
            setOpaque(false);
            // Generate a deterministic color based on name
            int hash = name.hashCode();
            this.bgColor = new Color((hash & 0xFF0000) >> 16, (hash & 0x00FF00) >> 8, hash & 0x0000FF).brighter();
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            
            g2.setColor(bgColor);
            g2.fillOval(0, 0, getWidth(), getHeight());
            
            g2.setColor(Color.WHITE);
            g2.setFont(new Font("Segoe UI", Font.BOLD, getWidth() / 2));
            String initial = name.substring(0, 1).toUpperCase();
            FontMetrics fm = g2.getFontMetrics();
            int x = (getWidth() - fm.stringWidth(initial)) / 2;
            int y = ((getHeight() - fm.getHeight()) / 2) + fm.getAscent();
            g2.drawString(initial, x, y);
            
            g2.dispose();
        }
    }

    public static class GradientButton extends RoundedButton {
        private Color gradientStart;
        private Color gradientEnd;

        public GradientButton(String text, Color start, Color end) {
            super(text, start, end);
            this.gradientStart = start;
            this.gradientEnd = end;
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            
            GradientPaint gp = new GradientPaint(0, 0, isHovered ? gradientStart.brighter() : gradientStart, 
                                               getWidth(), getHeight(), isHovered ? gradientEnd.brighter() : gradientEnd);
            g2.setPaint(gp);
            g2.fill(new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), 12, 12));
            g2.dispose();
            
            // Draw text manually to ensure it's on top of gradient
            FontMetrics fm = g.getFontMetrics(getFont());
            Rectangle2D r = fm.getStringBounds(getText(), g);
            int x = (getWidth() - (int) r.getWidth()) / 2;
            int y = (getHeight() - (int) r.getHeight()) / 2 + fm.getAscent();
            g.setColor(getForeground());
            g.setFont(getFont());
            g.drawString(getText(), x, y);
        }
    }

    public static class GlassPanel extends JPanel {
        private int alpha = 180;

        public GlassPanel(int alpha) {
            this.alpha = alpha;
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(new Color(255, 255, 255, alpha));
            g2.fill(new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), 20, 20));
            
            // Subtle white border for glass effect
            g2.setColor(new Color(255, 255, 255, 100));
            g2.setStroke(new BasicStroke(1.5f));
            g2.draw(new RoundRectangle2D.Double(0, 0, getWidth() - 1, getHeight() - 1, 20, 20));
            
            g2.dispose();
            super.paintComponent(g);
        }
    }

    public static class ModernTabUI extends javax.swing.plaf.basic.BasicTabbedPaneUI {
        @Override
        protected void installDefaults() {
            super.installDefaults();
            highlight = new Color(0,0,0,0);
            lightHighlight = new Color(0,0,0,0);
            shadow = new Color(0,0,0,0);
            darkShadow = new Color(0,0,0,0);
            focus = new Color(0,0,0,0);
        }

        @Override
        protected void paintTabBackground(Graphics g, int tabPlacement, int tabIndex, int x, int y, int w, int h, boolean isSelected) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            if (isSelected) {
                g2.setColor(new Color(255, 255, 255, 40));
                g2.fillRoundRect(x + 2, y + 2, w - 4, h - 4, 10, 10);
                g2.setColor(PRIMARY);
                g2.fillRect(x + (w/4), y + h - 3, w/2, 3);
            }
            g2.dispose();
        }

        @Override
        protected void paintTabBorder(Graphics g, int tabPlacement, int tabIndex, int x, int y, int w, int h, boolean isSelected) {
        }

        @Override
        protected void paintContentBorder(Graphics g, int tabPlacement, int selectedIndex) {
        }
    }

    public static class SearchField extends JTextField {
        public SearchField(String placeholder) {
            setPreferredSize(new Dimension(0, 40));
            setBorder(BorderFactory.createCompoundBorder(
                    new RoundedBorder(new Color(226, 232, 240), 12),
                    new javax.swing.border.EmptyBorder(5, 35, 5, 15)
            ));
            setFont(new Font("Segoe UI", Font.PLAIN, 14));
            setText(placeholder);
            setForeground(TEXT_MUTED);

            addFocusListener(new java.awt.event.FocusAdapter() {
                @Override
                public void focusGained(java.awt.event.FocusEvent e) {
                    if (getText().equals(placeholder)) {
                        setText("");
                        setForeground(TEXT_MAIN);
                    }
                }
                @Override
                public void focusLost(java.awt.event.FocusEvent e) {
                    if (getText().isEmpty()) {
                        setText(placeholder);
                        setForeground(TEXT_MUTED);
                    }
                }
            });
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(TEXT_MUTED);
            g2.setStroke(new BasicStroke(1.5f));
            int size = 12;
            int x = 12;
            int y = (getHeight() - size) / 2;
            g2.drawOval(x, y, size - 3, size - 3);
            g2.drawLine(x + size - 4, y + size - 4, x + size, y + size);
            g2.dispose();
        }
    }

    public static void showToast(JFrame parent, String message, Color color) {
        JDialog toast = new JDialog(parent);
        toast.setUndecorated(true);
        toast.setAlwaysOnTop(true);
        toast.setBackground(new Color(0,0,0,0));
        
        RoundedPanel panel = new RoundedPanel(15, true);
        panel.setBackground(color);
        panel.setLayout(new BorderLayout());
        panel.setBorder(new javax.swing.border.EmptyBorder(10, 20, 10, 20));
        
        JLabel label = new JLabel(message);
        label.setFont(new Font("Segoe UI", Font.BOLD, 13));
        label.setForeground(Color.WHITE);
        panel.add(label, BorderLayout.CENTER);
        
        toast.add(panel);
        toast.pack();
        
        Point p = parent.getLocation();
        toast.setLocation(p.x + (parent.getWidth() - toast.getWidth()) / 2, p.y + 50);
        
        toast.setVisible(true);
        
        new Timer(2500, e -> {
            toast.dispose();
        }).start();
    }

    public static class IconDrawer {
        public static void drawSendIcon(Graphics2D g2, int x, int y, int size, Color color) {
            g2.setColor(color);
            g2.setStroke(new BasicStroke(2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            int[] px = {x, x + size, x, x + size / 3};
            int[] py = {y, y + size / 2, y + size, y + size / 2};
            g2.drawPolygon(px, py, 4);
        }

        public static void drawLogoutIcon(Graphics2D g2, int x, int y, int size, Color color) {
            g2.setColor(color);
            g2.setStroke(new BasicStroke(2f));
            g2.drawArc(x, y, size, size, 45, 270);
            g2.drawLine(x + size / 2, y, x + size / 2, y + size / 2);
        }
    }

    public static void applyCustomScrollbar(JScrollPane scrollPane) {
        scrollPane.getVerticalScrollBar().setUI(new javax.swing.plaf.basic.BasicScrollBarUI() {
            @Override
            protected void configureScrollBarColors() {
                this.thumbColor = new Color(209, 213, 219);
                this.trackColor = new Color(249, 250, 251);
            }

            @Override
            protected JButton createDecreaseButton(int orientation) {
                return createZeroButton();
            }

            @Override
            protected JButton createIncreaseButton(int orientation) {
                return createZeroButton();
            }

            private JButton createZeroButton() {
                JButton jbutton = new JButton();
                jbutton.setPreferredSize(new Dimension(0, 0));
                jbutton.setMinimumSize(new Dimension(0, 0));
                jbutton.setMaximumSize(new Dimension(0, 0));
                return jbutton;
            }
            
            @Override
            protected void paintThumb(Graphics g, JComponent c, Rectangle thumbBounds) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(thumbColor);
                g2.fillRoundRect(thumbBounds.x + 4, thumbBounds.y + 2, thumbBounds.width - 8, thumbBounds.height - 4, 8, 8);
                g2.dispose();
            }
        });
        scrollPane.setBorder(null);
    }

    public static JTextField createModernField(String placeholder) {
        JTextField field = new JTextField();
        field.setPreferredSize(new Dimension(0, 40));
        field.setBorder(BorderFactory.createCompoundBorder(
                new RoundedBorder(new Color(209, 213, 219), 8),
                new javax.swing.border.EmptyBorder(5, 10, 5, 10)
        ));
        field.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        
        field.setText(placeholder);
        field.setForeground(TEXT_MUTED);
        
        field.addFocusListener(new java.awt.event.FocusAdapter() {
            @Override
            public void focusGained(java.awt.event.FocusEvent e) {
                if (field.getText().equals(placeholder)) {
                    field.setText("");
                    field.setForeground(TEXT_MAIN);
                }
            }
            @Override
            public void focusLost(java.awt.event.FocusEvent e) {
                if (field.getText().isEmpty()) {
                    field.setText(placeholder);
                    field.setForeground(TEXT_MUTED);
                }
            }
        });
        
        return field;
    }

    public static JPasswordField createModernPasswordField(String placeholder) {
        JPasswordField field = new JPasswordField();
        field.setPreferredSize(new Dimension(0, 40));
        field.setBorder(BorderFactory.createCompoundBorder(
                new RoundedBorder(new Color(209, 213, 219), 8),
                new javax.swing.border.EmptyBorder(5, 10, 5, 10)
        ));
        field.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        field.setEchoChar((char) 0);
        field.setText(placeholder);
        field.setForeground(TEXT_MUTED);

        field.addFocusListener(new java.awt.event.FocusAdapter() {
            @Override
            public void focusGained(java.awt.event.FocusEvent e) {
                if (new String(field.getPassword()).equals(placeholder)) {
                    field.setText("");
                    field.setEchoChar('•');
                    field.setForeground(TEXT_MAIN);
                }
            }
            @Override
            public void focusLost(java.awt.event.FocusEvent e) {
                if (new String(field.getPassword()).isEmpty()) {
                    field.setText(placeholder);
                    field.setEchoChar((char) 0);
                    field.setForeground(TEXT_MUTED);
                }
            }
        });

        return field;
    }

    public static class RoundedBorder extends javax.swing.border.AbstractBorder {
        private Color color;
        private int radius;

        public RoundedBorder(Color color, int radius) {
            this.color = color;
            this.radius = radius;
        }

        @Override
        public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(color);
            g2.draw(new RoundRectangle2D.Double(x, y, width - 1, height - 1, radius, radius));
            g2.dispose();
        }

        @Override
        public Insets getBorderInsets(Component c) {
            return new Insets(radius / 2, radius / 2, radius / 2, radius / 2);
        }
    }
}
