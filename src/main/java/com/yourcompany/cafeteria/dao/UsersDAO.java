package com.yourcompany.cafeteria.dao;
import java.sql.*; import com.yourcompany.cafeteria.model.User;
public class UsersDAO {
  private final Connection conn;
  public UsersDAO(Connection c){ this.conn=c; }
  public User findByUsername(String username) throws SQLException {
    try (PreparedStatement ps = conn.prepareStatement("SELECT id,username,password_hash,full_name,role_id FROM \"user\" WHERE username=?")){
      ps.setString(1, username); try (ResultSet rs = ps.executeQuery()){ if(rs.next()){ User u = new User(); u.setId(rs.getInt(1)); u.setUsername(rs.getString(2)); u.setPasswordHash(rs.getString(3)); u.setFullName(rs.getString(4)); u.setRoleId(rs.getInt(5)); return u; } }
    } return null;
  }
  public int createUser(String username, String passwordHash, String fullName, int roleId) throws SQLException {
    try (PreparedStatement ps = conn.prepareStatement("INSERT INTO \"user\"(username,password_hash,full_name,role_id) VALUES (?,?,?,?)", Statement.RETURN_GENERATED_KEYS)){
      ps.setString(1, username); ps.setString(2, passwordHash); ps.setString(3, fullName); ps.setInt(4, roleId); ps.executeUpdate(); try (ResultSet rs = ps.getGeneratedKeys()){ rs.next(); return rs.getInt(1); }
    }
  }
}
