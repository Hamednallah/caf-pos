package com.yourcompany.cafeteria.util;
import org.h2.jdbcx.JdbcDataSource;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
public class DataSourceProvider {
  private static final JdbcDataSource ds = new JdbcDataSource();
  static { ds.setURL("jdbc:h2:file:./data/cafeteria"); ds.setUser("sa"); ds.setPassword(""); }
  public static void setURL(String url) { ds.setURL(url); }
  public static DataSource getDataSource(){ return ds; }
  public static Connection getConnection() throws SQLException { return ds.getConnection(); }
}
