package client;

import shared.ChatService;

import javax.swing.*;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class ClientApp {

    private static String serverHost = "";

    public static void main(String[] args) {
        // ── Ask for the Server IP via a popup dialog ────────────────────────
        String input = JOptionPane.showInputDialog(
                null,
                "Enter the Server IP address (PC3):",
                "Connect to Server",
                JOptionPane.QUESTION_MESSAGE
        );

        if (input == null) {
            // User pressed Cancel
            System.out.println("Client startup cancelled.");
            System.exit(0);
        }

        input = input.trim();
        if (input.isEmpty()) {
            JOptionPane.showMessageDialog(
                    null,
                    "Server IP cannot be empty. Please restart and enter a valid IP.",
                    "Invalid Input",
                    JOptionPane.ERROR_MESSAGE
            );
            System.exit(1);
        }

        serverHost = input;

        // If passed as command-line argument, it overrides the dialog
        if (args.length > 0) {
            serverHost = args[0];
        }

        SwingUtilities.invokeLater(ClientApp::createAndShowLogin);
    }

    private static void createAndShowLogin() {
        try {
            Registry registry = LocateRegistry.getRegistry(serverHost, 1099); // Default RMI port
            ChatService chatService = (ChatService) registry.lookup("ChatService");
            LoginController loginController = new LoginController(chatService, username -> {
                ChatController chatController = new ChatController(chatService, username, ClientApp::createAndShowLogin);
                chatController.show();
            });
            loginController.show();
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(
                    null,
                    "❌ Unable to connect to server at:\n"
                    + serverHost + ":1099\n\n"
                    + "Make sure:\n"
                    + "• The server (PC3) is running\n"
                    + "• The IP address is correct\n"
                    + "• Port 1099 is not blocked by firewall\n\n"
                    + "Error: " + e.getMessage(),
                    "Connection Error",
                    JOptionPane.ERROR_MESSAGE
            );
            System.exit(1);
        }
    }
}
