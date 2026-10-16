CREATE DATABASE IF NOT EXISTS chatdb;
USE chatdb;

CREATE TABLE IF NOT EXISTS users (
                                     id INT AUTO_INCREMENT PRIMARY KEY,
                                     username VARCHAR(100) UNIQUE,
    password VARCHAR(100),
    full_name VARCHAR(100),
    age INT,
    phone VARCHAR(20),
    role VARCHAR(10) DEFAULT 'user',
    reg_status VARCHAR(10) DEFAULT 'pending',
    status VARCHAR(10) DEFAULT 'offline'
    );

CREATE TABLE IF NOT EXISTS messages (
                                        id INT AUTO_INCREMENT PRIMARY KEY,
                                        sender VARCHAR(100),
    receiver VARCHAR(100),
    message TEXT,
    timestamp DATETIME DEFAULT CURRENT_TIMESTAMP
    );

CREATE TABLE IF NOT EXISTS tasks (
                                     id INT AUTO_INCREMENT PRIMARY KEY,
                                     username VARCHAR(100),
    title VARCHAR(200),
    description TEXT,
    due_date DATE,
    priority VARCHAR(20) DEFAULT 'medium',
    status VARCHAR(20) DEFAULT 'pending',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (username) REFERENCES users(username)
    );

INSERT INTO users (username, password, full_name, age, phone, role, reg_status, status)
VALUES ('admin', 'admin123', 'Administrator', 30, '0911000000', 'admin', 'approved', 'offline')
    ON DUPLICATE KEY UPDATE role='admin', reg_status='approved';