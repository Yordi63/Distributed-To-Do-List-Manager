package server;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnection {
    // Default is localhost, but can be changed via setDbIp()
    private static String dbIp = "10.156.142.123";
    private static String dbUrl = "jdbc:mysql://" + dbIp + ":3306/chatdb?useSSL=false&serverTimezone=UTC";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "1234";

    public static void setDbIp(String ip) {
        dbIp = ip;
        dbUrl = "jdbc:mysql://" + dbIp + ":3306/chatdb?useSSL=false&serverTimezone=UTC";
    }

    public static Connection getConnection() throws SQLException {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            throw new SQLException("MySQL JDBC driver not found.", e);
        }
        return DriverManager.getConnection(dbUrl, DB_USER, DB_PASSWORD);
    }
}
