package server;

import shared.ChatService;

import javax.swing.*;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class ServerMain {

    public static void main(String[] args) {
        // ── Ask for the Database IP via a popup dialog ──────────────────────
        String dbIp = JOptionPane.showInputDialog(
                null,
                "Enter the Database IP address (PC1):\n(Leave blank for localhost)",
                "Database IP",
                JOptionPane.QUESTION_MESSAGE
        );

        if (dbIp == null) {
            // User pressed Cancel
            System.out.println("Server startup cancelled.");
            System.exit(0);
        }

        dbIp = dbIp.trim();
        if (dbIp.isEmpty()) {
            dbIp = "localhost";
        }

        // ── Ask for this Server's own IP ────────────────────────────────────
        String serverIp = JOptionPane.showInputDialog(
                null,
                "Enter this Server's own IP address (PC3):\n(Clients will use this IP to connect)",
                "Server IP",
                JOptionPane.QUESTION_MESSAGE
        );

        if (serverIp == null) {
            System.out.println("Server startup cancelled.");
            System.exit(0);
        }

        serverIp = serverIp.trim();
        if (serverIp.isEmpty()) {
            serverIp = "localhost";
        }

        // ── Configure and Start RMI Server ──────────────────────────────────
        DBConnection.setDbIp(dbIp);

        try {
            System.setProperty("java.rmi.server.hostname", serverIp);
            ChatService service = new ChatServiceImpl();
            Registry registry = LocateRegistry.createRegistry(1099); // Default RMI port
            registry.rebind("ChatService", service);

            System.out.println("===========================================");
            System.out.println("  ChatService bound on port 1099 (RMI).");
            System.out.println("  Server  IP : " + serverIp);
            System.out.println("  Database IP: " + dbIp);
            System.out.println("  Server is ready and waiting for clients.");
            System.out.println("===========================================");

            JOptionPane.showMessageDialog(
                    null,
                    "✅ Server started successfully!\n\n"
                    + "Server IP   : " + serverIp + "\n"
                    + "Database IP : " + dbIp + "\n"
                    + "RMI Port    : 1099\n\n"
                    + "Share the Server IP [" + serverIp + "] with PC2 (clients).\n"
                    + "Do NOT close this window.",
                    "Server Running",
                    JOptionPane.INFORMATION_MESSAGE
            );

        } catch (Exception e) {
            System.err.println("Server exception:");
            e.printStackTrace();
            JOptionPane.showMessageDialog(
                    null,
                    "❌ Failed to start server:\n" + e.getMessage(),
                    "Server Error",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }
}
