package com.yourcompany.cafeteria.util;
import org.h2.jdbcx.JdbcDataSource;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
public class DataSourceProvider {
  private static final JdbcDataSource ds = new JdbcDataSource();
  static { ds.setURL("jdbc:h2:file:./data/cafeteria;AUTO_SERVER=TRUE;DB_CLOSE_ON_EXIT=FALSE"); ds.setUser("sa"); ds.setPassword(""); }
  public static DataSource getDataSource(){ return ds; }
  public static Connection getConnection() throws SQLException { return ds.getConnection(); }
}
