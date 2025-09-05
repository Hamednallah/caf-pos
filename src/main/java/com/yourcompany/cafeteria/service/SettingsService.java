package com.yourcompany.cafeteria.service;
import com.yourcompany.cafeteria.dao.SettingsDAO;
import java.sql.Connection;
import java.sql.Statement;

public class SettingsService {
    private final SettingsDAO dao;
    private final Connection connection;
    public SettingsService(Connection c){
        this.dao=new SettingsDAO(c);
        this.connection = c;
    }
    public String getSetting(String key) throws Exception { return dao.get(key); }
    public void saveSetting(String key, String value) throws Exception { dao.set(key, value); }

    public void backupDatabase(String toPath) throws Exception {
        try (Statement stmt = connection.createStatement()) {
            String backupSql = String.format("SCRIPT TO '%s'", toPath);
            stmt.execute(backupSql);
        }
    }
}
