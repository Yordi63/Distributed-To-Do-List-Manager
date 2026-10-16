package shared;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

public interface ChatService extends Remote {
    String register(String username, String password, String fullName, int age, String phone) throws RemoteException;
    String login(String username, String password) throws RemoteException;
    void logout(String username) throws RemoteException;
    String sendMessage(String sender, String receiver, String message) throws RemoteException;
    List<String> getMessages(String user1, String user2) throws RemoteException;
    List<String> getOnlineUsers() throws RemoteException;
    List<String> getAllUsers(String currentUser) throws RemoteException;
    
    // Admin Methods
    String getUserRole(String username) throws RemoteException;
    List<String> getPendingUsers() throws RemoteException;
    List<String> getAllUsersAdmin() throws RemoteException;
    boolean approveUser(String username) throws RemoteException;
    boolean rejectUser(String username) throws RemoteException;
    boolean deleteUser(String username) throws RemoteException;
    boolean resetPassword(String username, String newPassword) throws RemoteException;
    boolean updateUserStatus(String username, String status) throws RemoteException;

// Task Methods

boolean createTask(String username, String title, String description,
                   String dueDate, String priority) throws RemoteException;
List<String> getTasks(String username) throws RemoteException;
boolean updateTask(int taskId, String title, String description,
                   String dueDate, String priority, String status) throws RemoteException;
boolean deleteTask(int taskId) throws RemoteException;
List<String> getAllTasks() throws RemoteException; // Admin only

// User Management Methods
boolean updateUser(String username, String fullName, int age, String phone) throws RemoteException;
boolean changePassword(String username, String oldPassword, String newPassword) throws RemoteException;
}

