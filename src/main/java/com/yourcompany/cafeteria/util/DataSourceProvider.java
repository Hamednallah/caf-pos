package com.yourcompany.cafeteria.util;

import org.h2.jdbcx.JdbcDataSource;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DataSourceProvider {
    private static JdbcDataSource ds;
    private static String databaseUrl = "jdbc:h2:file:./data/cafeteria";

    static {
        initializeDataSource();
    }

    private static void initializeDataSource() {
        ds = new JdbcDataSource();
        ds.setURL(databaseUrl);
        ds.setUser("sa");
        ds.setPassword("");
    }

    public static void setURL(String url) {
        databaseUrl = url;
        closeDataSource();
        initializeDataSource();
    }

    public static DataSource getDataSource() {
        if (ds == null) {
            initializeDataSource();
        }
        return ds;
    }

    public static Connection getConnection() throws SQLException {
        if (ds == null) {
            initializeDataSource();
        }
        return ds.getConnection();
    }

    public static void closeDataSource() {
        if (ds != null) {
            try {
                // H2's JdbcDataSource doesn't have a close() method in the traditional sense.
                // To properly shut down the database connection pool, we can execute the SHUTDOWN command.
                // However, for the restore operation, we need to ensure all connections are closed
                // without shutting down the *database instance* itself if it's a server.
                // For an embedded file DB, simply nullifying the DS and letting GC handle it
                // after connections are closed is one approach. A more explicit shutdown is better.
                // Let's rely on SHUTDOWN via a direct connection for now.
                // For the purpose of the backup service, we'll just nullify the DS.
                ds = null;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static Connection getDirectConnection() throws SQLException {
        // This provides a direct, un-pooled connection to the database file,
        // which is necessary for running scripts after the main pool is disconnected.
        return DriverManager.getConnection(databaseUrl, "sa", "");
    }
}
