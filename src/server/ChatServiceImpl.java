package server;

import shared.ChatService;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ChatServiceImpl extends UnicastRemoteObject implements ChatService {

    protected ChatServiceImpl() throws RemoteException {
        super();
    }

    @Override
    public String register(String username, String password, String fullName, int age, String phone) throws RemoteException {
        if (username == null || username.isBlank() || password == null || password.isBlank()) {
            return "Username and password cannot be empty.";
        }
        try (Connection conn = DBConnection.getConnection()) {
            String checkSql = "SELECT id FROM users WHERE username = ?";
            try (PreparedStatement checkStmt = conn.prepareStatement(checkSql)) {
                checkStmt.setString(1, username);
                try (ResultSet rs = checkStmt.executeQuery()) {
                    if (rs.next()) return "Username already exists.";
                }
            }
            String insertSql = "INSERT INTO users (username, password, full_name, age, phone, role, reg_status, status) VALUES (?, ?, ?, ?, ?, 'user', 'pending', 'offline')";
            try (PreparedStatement insertStmt = conn.prepareStatement(insertSql)) {
                insertStmt.setString(1, username);
                insertStmt.setString(2, password);
                insertStmt.setString(3, fullName);
                insertStmt.setInt(4, age);
                insertStmt.setString(5, phone);
                insertStmt.executeUpdate();
            }
            return "Registration successful. Please wait for admin approval.";
        } catch (SQLException e) {
            e.printStackTrace();
            return "Database error during registration.";
        }
    }

    @Override
    public String login(String username, String password) throws RemoteException {
        if (username == null || username.isBlank() || password == null || password.isBlank()) {
            return "Username and password cannot be empty.";
        }
        try (Connection conn = DBConnection.getConnection()) {
            String sql = "SELECT password, reg_status FROM users WHERE username = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, username);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        String dbPassword = rs.getString("password");
                        String regStatus = rs.getString("reg_status");
                        if (!dbPassword.equals(password)) return "Invalid password.";
                        if (regStatus.equals("pending")) return "Your account is pending approval.";
                        else if (regStatus.equals("rejected")) return "Your account registration was rejected.";
                        else if (regStatus.equals("blocked")) return "Your account has been blocked.";
                        String updateSql = "UPDATE users SET status = 'online' WHERE username = ?";
                        try (PreparedStatement updateStmt = conn.prepareStatement(updateSql)) {
                            updateStmt.setString(1, username);
                            updateStmt.executeUpdate();
                        }
                        return "Login successful.";
                    } else {
                        return "User not found.";
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return "Database error during login.";
        }
    }

    @Override
    public void logout(String username) throws RemoteException {
        try (Connection conn = DBConnection.getConnection()) {
            String sql = "UPDATE users SET status = 'offline' WHERE username = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, username);
                stmt.executeUpdate();
            }
        } catch (SQLException e) { e.printStackTrace(); }
    }

    @Override
    public String sendMessage(String sender, String receiver, String message) throws RemoteException {
        if (sender == null || sender.isBlank() || receiver == null || receiver.isBlank() || message == null || message.isBlank()) {
            return "Sender, receiver, and message must not be empty.";
        }
        try (Connection conn = DBConnection.getConnection()) {
            String sql = "INSERT INTO messages (sender, receiver, message) VALUES (?, ?, ?)";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, sender);
                stmt.setString(2, receiver);
                stmt.setString(3, message);
                stmt.executeUpdate();
            }
            return "Message sent.";
        } catch (SQLException e) {
            e.printStackTrace();
            return "Database error during sendMessage.";
        }
    }

    @Override
    public List<String> getMessages(String user1, String user2) throws RemoteException {
        List<String> messages = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection()) {
            String sql = "SELECT sender, message, timestamp FROM messages "
                    + "WHERE (sender = ? AND receiver = ?) OR (sender = ? AND receiver = ?) "
                    + "ORDER BY timestamp ASC";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, user1);
                stmt.setString(2, user2);
                stmt.setString(3, user2);
                stmt.setString(4, user1);
                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        messages.add(String.format("[%s] %s: %s",
                                rs.getString("timestamp"),
                                rs.getString("sender"),
                                rs.getString("message")));
                    }
                }
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return messages;
    }

    @Override
    public List<String> getOnlineUsers() throws RemoteException {
        List<String> users = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection()) {
            String sql = "SELECT username FROM users WHERE status = 'online' AND reg_status = 'approved' ORDER BY username";
            try (PreparedStatement stmt = conn.prepareStatement(sql);
                 ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) users.add(rs.getString("username"));
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return users;
    }

    @Override
    public List<String> getAllUsers(String currentUser) throws RemoteException {
        List<String> users = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection()) {
            String sql = "SELECT username FROM users WHERE username <> ? AND reg_status = 'approved' ORDER BY username";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, currentUser);
                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) users.add(rs.getString("username"));
                }
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return users;
    }

    @Override
    public String getUserRole(String username) throws RemoteException {
        try (Connection conn = DBConnection.getConnection()) {
            String sql = "SELECT role FROM users WHERE username = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, username);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) return rs.getString("role");
                }
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return "user";
    }

    @Override
    public List<String> getPendingUsers() throws RemoteException {
        List<String> users = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection()) {
            String sql = "SELECT username FROM users WHERE reg_status = 'pending' ORDER BY id DESC";
            try (PreparedStatement stmt = conn.prepareStatement(sql);
                 ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) users.add(rs.getString("username"));
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return users;
    }

    @Override
    public List<String> getAllUsersAdmin() throws RemoteException {
        List<String> users = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection()) {
            String sql = "SELECT username FROM users ORDER BY username";
            try (PreparedStatement stmt = conn.prepareStatement(sql);
                 ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) users.add(rs.getString("username"));
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return users;
    }

    @Override
    public boolean approveUser(String username) throws RemoteException {
        return updateRegStatus(username, "approved");
    }

    @Override
    public boolean rejectUser(String username) throws RemoteException {
        return updateRegStatus(username, "rejected");
    }

    @Override
    public boolean deleteUser(String username) throws RemoteException {
        try (Connection conn = DBConnection.getConnection()) {
            String sql = "DELETE FROM users WHERE username = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, username);
                return stmt.executeUpdate() > 0;
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return false;
    }

    @Override
    public boolean resetPassword(String username, String newPassword) throws RemoteException {
        try (Connection conn = DBConnection.getConnection()) {
            String sql = "UPDATE users SET password = ? WHERE username = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, newPassword);
                stmt.setString(2, username);
                return stmt.executeUpdate() > 0;
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return false;
    }

    @Override
    public boolean updateUserStatus(String username, String status) throws RemoteException {
        return updateRegStatus(username, status);
    }

    @Override
    public boolean createTask(String username, String title, String description,
                              String dueDate, String priority) throws RemoteException {
        try (Connection conn = DBConnection.getConnection()) {
            String sql = "INSERT INTO tasks (username, title, description, due_date, priority, status) VALUES (?, ?, ?, ?, ?, 'pending')";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, username);
                stmt.setString(2, title);
                stmt.setString(3, description);
                stmt.setString(4, dueDate);
                stmt.setString(5, priority);
                return stmt.executeUpdate() > 0;
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return false;
    }

    @Override
    public List<String> getTasks(String username) throws RemoteException {
        List<String> tasks = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection()) {
            String sql = "SELECT id, title, description, due_date, priority, status FROM tasks WHERE username = ? ORDER BY created_at DESC";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, username);
                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        tasks.add(rs.getInt("id") + "|" + rs.getString("title") + "|" +
                                rs.getString("description") + "|" + rs.getString("due_date") + "|" +
                                rs.getString("priority") + "|" + rs.getString("status"));
                    }
                }
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return tasks;
    }

    @Override
    public boolean updateTask(int taskId, String title, String description,
                              String dueDate, String priority, String status) throws RemoteException {
        try (Connection conn = DBConnection.getConnection()) {
            String sql = "UPDATE tasks SET title=?, description=?, due_date=?, priority=?, status=? WHERE id=?";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, title);
                stmt.setString(2, description);
                stmt.setString(3, dueDate);
                stmt.setString(4, priority);
                stmt.setString(5, status);
                stmt.setInt(6, taskId);
                return stmt.executeUpdate() > 0;
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return false;
    }

    @Override
    public boolean deleteTask(int taskId) throws RemoteException {
        try (Connection conn = DBConnection.getConnection()) {
            String sql = "DELETE FROM tasks WHERE id = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, taskId);
                return stmt.executeUpdate() > 0;
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return false;
    }

    @Override
    public List<String> getAllTasks() throws RemoteException {
        List<String> tasks = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection()) {
            String sql = "SELECT id, username, title, description, due_date, priority, status FROM tasks ORDER BY created_at DESC";
            try (PreparedStatement stmt = conn.prepareStatement(sql);
                 ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    tasks.add(rs.getInt("id") + "|" + rs.getString("username") + "|" +
                            rs.getString("title") + "|" + rs.getString("description") + "|" +
                            rs.getString("due_date") + "|" + rs.getString("priority") + "|" +
                            rs.getString("status"));
                }
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return tasks;
    }

    @Override
    public boolean updateUser(String username, String fullName, int age, String phone) throws RemoteException {
        try (Connection conn = DBConnection.getConnection()) {
            String sql = "UPDATE users SET full_name=?, age=?, phone=? WHERE username=?";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, fullName);
                stmt.setInt(2, age);
                stmt.setString(3, phone);
                stmt.setString(4, username);
                return stmt.executeUpdate() > 0;
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return false;
    }

    @Override
    public boolean changePassword(String username, String oldPassword, String newPassword) throws RemoteException {
        try (Connection conn = DBConnection.getConnection()) {
            String checkSql = "SELECT id FROM users WHERE username=? AND password=?";
            try (PreparedStatement checkStmt = conn.prepareStatement(checkSql)) {
                checkStmt.setString(1, username);
                checkStmt.setString(2, oldPassword);
                try (ResultSet rs = checkStmt.executeQuery()) {
                    if (!rs.next()) return false;
                }
            }
            String updateSql = "UPDATE users SET password=? WHERE username=?";
            try (PreparedStatement updateStmt = conn.prepareStatement(updateSql)) {
                updateStmt.setString(1, newPassword);
                updateStmt.setString(2, username);
                return updateStmt.executeUpdate() > 0;
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return false;
    }

    private boolean updateRegStatus(String username, String status) {
        try (Connection conn = DBConnection.getConnection()) {
            String sql = "UPDATE users SET reg_status = ? WHERE username = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, status);
                stmt.setString(2, username);
                return stmt.executeUpdate() > 0;
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return false;
    }
}