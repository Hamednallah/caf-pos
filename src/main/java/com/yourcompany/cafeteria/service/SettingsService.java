package com.yourcompany.cafeteria.service;
import com.yourcompany.cafeteria.dao.SettingsDAO; import java.sql.Connection;
public class SettingsService { private final SettingsDAO dao; public SettingsService(Connection c){ this.dao=new SettingsDAO(c);} public String get(String key) throws Exception { return dao.get(key); } public void set(String key, String value) throws Exception { dao.set(key, value); } }
