# Distributed Chat Application (Java RMI + JavaFX)

## Overview
This project is a distributed chat application using Java RMI for server communication and JavaFX for the client UI.
It uses a MySQL database in XAMPP to store users and messages.

## Project Structure
- `src/shared/ChatService.java`
- `src/server/DBConnection.java`
- `src/server/ChatServiceImpl.java`
- `src/server/ServerMain.java`
- `src/client/ClientApp.java`
- `src/client/LoginController.java`
- `src/client/ChatController.java`

## Database Setup
1. Start XAMPP and MySQL.
2. Run `db/setup.sql` in your MySQL client or phpMyAdmin.
3. Confirm the `chatdb` database has `users` and `messages` tables.

## Required Libraries
- Java 11+ JDK
- JavaFX SDK
- MySQL JDBC Driver `.jar`

## Running Locally (One PC)
1. Add `mysql-connector-java.jar` to your classpath.
2. Set `DB_URL`, `DB_USER`, `DB_PASSWORD` in `src/server/DBConnection.java`.
3. Start the RMI server:
   - `javac -d out src/shared/ChatService.java src/server/*.java`
   - `java -classpath out;path\to\mysql-connector-java.jar server.ServerMain`
4. Start the client:
   - `javac -d out src/shared/ChatService.java src/client/*.java`
   - `java --module-path "path\to\javafx-sdk\lib" --add-modules javafx.controls,javafx.fxml -classpath out;path\to\mysql-connector-java.jar server.ServerMain.ClientApp`

## Notes
- For one-PC testing, use `localhost` as the server address.
- For distribution, point the client to the server IP and ensure port `1099` is reachable.
