package com.yourcompany.cafeteria.service;

import com.yourcompany.cafeteria.util.DataSourceProvider;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class BackupService {

    public void backupDatabase(String filePath) throws SQLException {
        try (Connection conn = DataSourceProvider.getConnection();
             Statement stmt = conn.createStatement()) {
            String sql = String.format("SCRIPT TO '%s'", filePath);
            stmt.execute(sql);
        }
    }

    public void restoreDatabase(String filePath) throws SQLException {
        // First, close all existing connections to the database.
        DataSourceProvider.closeDataSource();

        // Now, connect directly to the database file to run the restore script.
        // This is a simplified approach. A real-world app might need a more
        // robust mechanism, possibly involving restarting the application.
        try (Connection conn = DataSourceProvider.getDirectConnection();
             Statement stmt = conn.createStatement()) {
            String sql = String.format("RUNSCRIPT FROM '%s'", filePath);
            stmt.execute(sql);
        }
        // The data source will be re-initialized on the next getConnection() call,
        // which will happen after the user restarts the application.
    }
}
