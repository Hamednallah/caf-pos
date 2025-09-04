package com.yourcompany.cafeteria.dao;
import java.sql.*; import com.yourcompany.cafeteria.model.User;
public class UsersDAO {
  private final Connection conn;
  public UsersDAO(Connection c){ this.conn=c; }
  public User findByUsername(String username) throws SQLException {
    try (PreparedStatement ps = conn.prepareStatement("SELECT id,username,password_hash,full_name,role_id,active FROM \"user\" WHERE username=?")){
      ps.setString(1, username); try (ResultSet rs = ps.executeQuery()){ if(rs.next()){ User u = new User(); u.setId(rs.getInt("id")); u.setUsername(rs.getString("username")); u.setPasswordHash(rs.getString("password_hash")); u.setFullName(rs.getString("full_name")); u.setRoleId(rs.getInt("role_id")); u.setActive(rs.getBoolean("active")); return u; } }
    } return null;
  }

  public User findById(int id) throws SQLException {
    try (PreparedStatement ps = conn.prepareStatement("SELECT id,username,password_hash,full_name,role_id,active FROM \"user\" WHERE id=?")){
      ps.setInt(1, id);
      try (ResultSet rs = ps.executeQuery()){
        if(rs.next()){
          User u = new User();
          u.setId(rs.getInt("id"));
          u.setUsername(rs.getString("username"));
          u.setPasswordHash(rs.getString("password_hash"));
          u.setFullName(rs.getString("full_name"));
          u.setRoleId(rs.getInt("role_id"));
          u.setActive(rs.getBoolean("active"));
          return u;
        }
      }
    }
    return null;
  }

  public int createUser(String username, String passwordHash, String fullName, int roleId) throws SQLException {
    try (PreparedStatement ps = conn.prepareStatement("INSERT INTO \"user\"(username,password_hash,full_name,role_id) VALUES (?,?,?,?)", Statement.RETURN_GENERATED_KEYS)){
      ps.setString(1, username); ps.setString(2, passwordHash); ps.setString(3, fullName); ps.setInt(4, roleId); ps.executeUpdate(); try (ResultSet rs = ps.getGeneratedKeys()){ rs.next(); return rs.getInt(1); }
    }
  }

  public java.util.List<User> listAll() throws SQLException {
    java.util.List<User> users = new java.util.ArrayList<>();
    try (PreparedStatement ps = conn.prepareStatement("SELECT id,username,password_hash,full_name,role_id,active FROM \"user\" ORDER BY username")) {
        try (ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                User u = new User();
                u.setId(rs.getInt("id"));
                u.setUsername(rs.getString("username"));
                u.setPasswordHash(rs.getString("password_hash"));
                u.setFullName(rs.getString("full_name"));
                u.setRoleId(rs.getInt("role_id"));
                u.setActive(rs.getBoolean("active"));
                users.add(u);
            }
        }
    }
    return users;
  }

  public void updateUser(User user) throws SQLException {
      String sql = "UPDATE \"user\" SET username = ?, full_name = ?, role_id = ?, active = ?" +
              (user.getPasswordHash() != null && !user.getPasswordHash().isEmpty() ? ", password_hash = ?" : "") +
              " WHERE id = ?";
      try (PreparedStatement ps = conn.prepareStatement(sql)) {
          ps.setString(1, user.getUsername());
          ps.setString(2, user.getFullName());
          ps.setInt(3, user.getRoleId());
          ps.setBoolean(4, user.isActive());
          int lastIndex = 5;
          if (user.getPasswordHash() != null && !user.getPasswordHash().isEmpty()) {
              ps.setString(lastIndex++, user.getPasswordHash());
          }
          ps.setInt(lastIndex, user.getId());
          ps.executeUpdate();
      }
  }

  public void updateUserStatus(int userId, boolean isActive) throws SQLException {
      try (PreparedStatement ps = conn.prepareStatement("UPDATE \"user\" SET active = ? WHERE id = ?")) {
          ps.setBoolean(1, isActive);
          ps.setInt(2, userId);
          ps.executeUpdate();
      }
  }

    public java.util.Map<String, Integer> countUsersByRole() throws SQLException {
        java.util.Map<String, Integer> counts = new java.util.HashMap<>();
        String sql = "SELECT r.name, COUNT(u.id) as user_count FROM role r LEFT JOIN \"user\" u ON r.id = u.role_id GROUP BY r.name";
        try (Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                counts.put(rs.getString("name"), rs.getInt("user_count"));
            }
        }
        return counts;
    }
}
