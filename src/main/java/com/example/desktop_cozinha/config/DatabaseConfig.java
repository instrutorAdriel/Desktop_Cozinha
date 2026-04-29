package com.example.desktop_cozinha.config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConfig {
    public static Connection getConnection() throws SQLException {
        String url = "jdbc:mysql://localhost:3307/desktop_cozinha";
        String user = "root";
        String password = "senac";
        return DriverManager.getConnection(url, user, password);
    }
}

