package client;

import shared.ChatService;

import javax.swing.*;
import java.awt.*;
import java.rmi.RemoteException;
import java.util.List;

public class TaskController {
    private final JFrame frame;
    private final ChatService chatService;
    private final String username;
    private final DefaultListModel<String> taskListModel;

    public TaskController(ChatService chatService, String username) {
        this.chatService = chatService;
        this.username = username;
        this.taskListModel = new DefaultListModel<>();
        this.frame = createFrame();
        refreshTasks();
    }

    public void show() {
        frame.setVisible(true);
    }

    private JFrame createFrame() {
        JFrame frame = new JFrame("My Tasks - " + username);
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setSize(800, 600);
        frame.setLocationRelativeTo(null);
        frame.setLayout(new BorderLayout());
        frame.getContentPane().setBackground(ModernUI.BACKGROUND);

        // Title
        JLabel title = new JLabel("Task Manager");
        title.setFont(new Font("Segoe UI", Font.BOLD, 24));
        title.setBorder(BorderFactory.createEmptyBorder(20, 30, 10, 30));
        frame.add(title, BorderLayout.NORTH);

        // Task List
        JList<String> taskList = new JList<>(taskListModel);
        taskList.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        taskList.setFixedCellHeight(50);
        taskList.setCellRenderer(new TaskCellRenderer());
        JScrollPane scroll = new JScrollPane(taskList);
        ModernUI.applyCustomScrollbar(scroll);
        scroll.setBorder(BorderFactory.createEmptyBorder(0, 20, 0, 20));
        frame.add(scroll, BorderLayout.CENTER);

        // Buttons
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 15));
        actions.setOpaque(false);
        actions.setBorder(BorderFactory.createEmptyBorder(0, 20, 10, 20));

        ModernUI.RoundedButton addBtn = new ModernUI.RoundedButton("+ Add Task", ModernUI.PRIMARY, ModernUI.PRIMARY_HOVER);
        ModernUI.RoundedButton editBtn = new ModernUI.RoundedButton("Edit", new Color(107, 114, 128), new Color(75, 85, 99));
        ModernUI.RoundedButton deleteBtn = new ModernUI.RoundedButton("Delete", ModernUI.DANGER, new Color(220, 38, 38));

        addBtn.setPreferredSize(new Dimension(130, 40));
        editBtn.setPreferredSize(new Dimension(100, 40));
        deleteBtn.setPreferredSize(new Dimension(100, 40));

        addBtn.addActionListener(e -> showTaskDialog(null, -1));

        editBtn.addActionListener(e -> {
            String selected = taskList.getSelectedValue();
            if (selected != null) {
                String[] parts = selected.split("\\|");
                int taskId = Integer.parseInt(parts[0]);
                showTaskDialog(parts, taskId);
            } else {
                JOptionPane.showMessageDialog(frame, "Please select a task to edit.");
            }
        });

        deleteBtn.addActionListener(e -> {
            String selected = taskList.getSelectedValue();
            if (selected != null) {
                int confirm = JOptionPane.showConfirmDialog(frame,
                        "Are you sure you want to delete this task?",
                        "Confirm Delete", JOptionPane.YES_NO_OPTION);
                if (confirm == JOptionPane.YES_OPTION) {
                    try {
                        int taskId = Integer.parseInt(selected.split("\\|")[0]);
                        if (chatService.deleteTask(taskId)) {
                            ModernUI.showToast(frame, "Task deleted!", ModernUI.DANGER);
                            refreshTasks();
                        }
                    } catch (RemoteException ex) { ex.printStackTrace(); }
                }
            } else {
                JOptionPane.showMessageDialog(frame, "Please select a task to delete.");
            }
        });

        actions.add(editBtn);
        actions.add(deleteBtn);
        actions.add(addBtn);
        frame.add(actions, BorderLayout.SOUTH);

        return frame;
    }

    private void showTaskDialog(String[] existingParts, int taskId) {
        JDialog dialog = new JDialog(frame, taskId == -1 ? "Add New Task" : "Edit Task", true);
        dialog.setSize(450, 400);
        dialog.setLocationRelativeTo(frame);
        dialog.setLayout(new BorderLayout());
        dialog.getContentPane().setBackground(ModernUI.BACKGROUND);

        JPanel form = new JPanel(new GridLayout(6, 2, 10, 15));
        form.setOpaque(false);
        form.setBorder(BorderFactory.createEmptyBorder(20, 30, 10, 30));

        JTextField titleField = new JTextField(existingParts != null ? existingParts[1] : "");
        JTextField descField = new JTextField(existingParts != null ? existingParts[2] : "");
        JTextField dateField = new JTextField(existingParts != null ? existingParts[3] : "YYYY-MM-DD");
        String[] priorities = {"low", "medium", "high"};
        JComboBox<String> priorityBox = new JComboBox<>(priorities);
        if (existingParts != null) priorityBox.setSelectedItem(existingParts[4]);
        String[] statuses = {"pending", "in_progress", "completed"};
        JComboBox<String> statusBox = new JComboBox<>(statuses);
        if (existingParts != null) statusBox.setSelectedItem(existingParts[5]);

        form.add(new JLabel("Title:")); form.add(titleField);
        form.add(new JLabel("Description:")); form.add(descField);
        form.add(new JLabel("Due Date:")); form.add(dateField);
        form.add(new JLabel("Priority:")); form.add(priorityBox);
        if (taskId != -1) { form.add(new JLabel("Status:")); form.add(statusBox); }

        dialog.add(form, BorderLayout.CENTER);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 15));
        btnPanel.setOpaque(false);
        ModernUI.RoundedButton saveBtn = new ModernUI.RoundedButton("Save", ModernUI.SUCCESS, ModernUI.SUCCESS_HOVER);
        ModernUI.RoundedButton cancelBtn = new ModernUI.RoundedButton("Cancel", new Color(107, 114, 128), new Color(75, 85, 99));
        saveBtn.setPreferredSize(new Dimension(100, 38));
        cancelBtn.setPreferredSize(new Dimension(100, 38));

        saveBtn.addActionListener(e -> {
            String t = titleField.getText().trim();
            String d = descField.getText().trim();
            String date = dateField.getText().trim();
            String prio = (String) priorityBox.getSelectedItem();
            String stat = (String) statusBox.getSelectedItem();

            if (t.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "Title cannot be empty!");
                return;
            }
            try {
                boolean success;
                if (taskId == -1) {
                    success = chatService.createTask(username, t, d, date, prio);
                } else {
                    success = chatService.updateTask(taskId, t, d, date, prio, stat);
                }
                if (success) {
                    ModernUI.showToast(frame, taskId == -1 ? "Task created!" : "Task updated!", ModernUI.SUCCESS);
                    refreshTasks();
                    dialog.dispose();
                }
            } catch (RemoteException ex) { ex.printStackTrace(); }
        });

        cancelBtn.addActionListener(e -> dialog.dispose());
        btnPanel.add(cancelBtn);
        btnPanel.add(saveBtn);
        dialog.add(btnPanel, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }

    private void refreshTasks() {
        new Thread(() -> {
            try {
                List<String> tasks = chatService.getTasks(username);
                SwingUtilities.invokeLater(() -> {
                    taskListModel.clear();
                    if (tasks.isEmpty()) {
                        taskListModel.addElement("No tasks yet. Click '+ Add Task' to create one!");
                    } else {
                        for (String t : tasks) taskListModel.addElement(t);
                    }
                });
            } catch (RemoteException e) { e.printStackTrace(); }
        }).start();
    }

    // Custom cell renderer to display tasks nicely
    static class TaskCellRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value,
                                                      int index, boolean isSelected, boolean cellHasFocus) {
            JLabel label = (JLabel) super.getListCellRendererComponent(
                    list, value, index, isSelected, cellHasFocus);
            String val = value.toString();
            if (val.contains("|")) {
                String[] p = val.split("\\|");
                // p[0]=id, p[1]=title, p[2]=desc, p[3]=date, p[4]=priority, p[5]=status
                String priority = p.length > 4 ? p[4] : "";
                String status = p.length > 5 ? p[5] : "";
                String title = p.length > 1 ? p[1] : val;
                String date = p.length > 3 ? p[3] : "";
                label.setText(String.format("<html><b>%s</b> &nbsp;|&nbsp; Due: %s &nbsp;|&nbsp; Priority: %s &nbsp;|&nbsp; Status: <i>%s</i></html>",
                        title, date, priority, status));
                // Color by status
                if (!isSelected) {
                    if ("completed".equals(status)) label.setBackground(new Color(220, 252, 231));
                    else if ("in_progress".equals(status)) label.setBackground(new Color(254, 249, 195));
                    else label.setBackground(Color.WHITE);
                }
            }
            return label;
        }
    }
}