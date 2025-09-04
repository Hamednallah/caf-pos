package com.yourcompany.cafeteria.dao;
import java.sql.*; import com.yourcompany.cafeteria.model.Order; import com.yourcompany.cafeteria.model.OrderItem; import java.util.List;
public class OrdersDAO {
  private final Connection conn; public OrdersDAO(Connection c){ this.conn=c; }
  public int createOrderTransactional(Order order) throws SQLException {
    conn.setAutoCommit(false); try { int orderId; try (PreparedStatement ps = conn.prepareStatement("INSERT INTO \"order\"(cashier_id,shift_id,total_amount,discount_amount,status,payment_method,payment_confirmed) VALUES (?,?,?,?,?,?,?)", Statement.RETURN_GENERATED_KEYS)){ ps.setInt(1, order.cashierId==null?1:order.cashierId); if(order.shiftId==null) ps.setNull(2, Types.INTEGER); else ps.setInt(2, order.shiftId); ps.setBigDecimal(3, order.totalAmount); ps.setBigDecimal(4, order.discountAmount==null?java.math.BigDecimal.ZERO:order.discountAmount); ps.setString(5, order.status); ps.setString(6, order.paymentMethod); ps.setBoolean(7, order.paymentConfirmed); ps.executeUpdate(); try (ResultSet rs = ps.getGeneratedKeys()){ rs.next(); orderId = rs.getInt(1); } } if(order.items != null && !order.items.isEmpty()){ try (PreparedStatement ips = conn.prepareStatement("INSERT INTO order_item(order_id,item_id,quantity,line_total) VALUES (?,?,?,?)")){ for(OrderItem oi : order.items){ ips.setInt(1, orderId); ips.setInt(2, oi.getItemId()); ips.setInt(3, oi.getQuantity()); ips.setBigDecimal(4, oi.getLineTotal()); ips.addBatch(); } ips.executeBatch(); } } conn.commit(); conn.setAutoCommit(true); return orderId; } catch(SQLException ex){ conn.rollback(); conn.setAutoCommit(true); throw ex; } }
  public ResultSet findOrdersBetween(Timestamp from, Timestamp to) throws SQLException { PreparedStatement ps = conn.prepareStatement("SELECT * FROM \"order\" WHERE created_at BETWEEN ? AND ?"); ps.setTimestamp(1, from); ps.setTimestamp(2, to); return ps.executeQuery(); }
  public ResultSet getOrdersByShift(int shiftId) throws SQLException {
    PreparedStatement ps = conn.prepareStatement("SELECT * FROM \"order\" WHERE shift_id = ?");
    ps.setInt(1, shiftId);
    return ps.executeQuery();
  }

  public Order findOrderById(int orderId) throws SQLException {
      String sql = "SELECT * FROM \"order\" WHERE id = ?";
      try (PreparedStatement ps = conn.prepareStatement(sql)) {
          ps.setInt(1, orderId);
          try (ResultSet rs = ps.executeQuery()) {
              if (rs.next()) {
                  return mapResultSetToOrder(rs);
              }
          }
      }
      return null;
  }

  public Order findOrderByIdAndDate(int orderId, java.time.LocalDate date) throws SQLException {
      String sql = "SELECT * FROM \"order\" WHERE id = ? AND CAST(created_at AS DATE) = ?";
      try (PreparedStatement ps = conn.prepareStatement(sql)) {
          ps.setInt(1, orderId);
          ps.setDate(2, java.sql.Date.valueOf(date));
          try (ResultSet rs = ps.executeQuery()) {
              if (rs.next()) {
                  return mapResultSetToOrder(rs);
              }
          }
      }
      return null;
  }

    private Order mapResultSetToOrder(ResultSet rs) throws SQLException {
        Order order = new Order();
        order.id = rs.getInt("id");
        order.cashierId = rs.getInt("cashier_id");
        order.shiftId = rs.getInt("shift_id");
        order.totalAmount = rs.getBigDecimal("total_amount");
        order.discountAmount = rs.getBigDecimal("discount_amount");
        order.status = rs.getString("status");
        order.paymentMethod = rs.getString("payment_method");
        order.paymentConfirmed = rs.getBoolean("payment_confirmed");
        order.createdAt = rs.getTimestamp("created_at").toLocalDateTime();
        return order;
    }

    public void updateOrderTransactional(Order order) throws SQLException {
        conn.setAutoCommit(false);
        try {
            // Update the order itself
            String updateOrderSql = "UPDATE \"order\" SET total_amount = ?, discount_amount = ?, status = ?, payment_method = ?, payment_confirmed = ? WHERE id = ?";
            try (PreparedStatement ps = conn.prepareStatement(updateOrderSql)) {
                ps.setBigDecimal(1, order.totalAmount);
                ps.setBigDecimal(2, order.discountAmount);
                ps.setString(3, order.status);
                ps.setString(4, order.paymentMethod);
                ps.setBoolean(5, order.paymentConfirmed);
                ps.setInt(6, order.id);
                ps.executeUpdate();
            }

            // Delete old order items
            String deleteItemsSql = "DELETE FROM order_item WHERE order_id = ?";
            try (PreparedStatement ps = conn.prepareStatement(deleteItemsSql)) {
                ps.setInt(1, order.id);
                ps.executeUpdate();
            }

            // Insert new order items
            if (order.items != null && !order.items.isEmpty()) {
                String insertItemsSql = "INSERT INTO order_item(order_id,item_id,quantity,line_total) VALUES (?,?,?,?)";
                try (PreparedStatement ips = conn.prepareStatement(insertItemsSql)) {
                    for (OrderItem oi : order.items) {
                        ips.setInt(1, order.id);
                        ips.setInt(2, oi.getItemId());
                        ips.setInt(3, oi.getQuantity());
                        ips.setBigDecimal(4, oi.getLineTotal());
                        ips.addBatch();
                    }
                    ips.executeBatch();
                }
            }

            conn.commit();
        } catch (SQLException ex) {
            conn.rollback();
            throw ex;
        } finally {
            conn.setAutoCommit(true);
        }
    }

    public List<Order> getRecentOrders(int limit) throws SQLException {
        List<Order> orders = new java.util.ArrayList<>();
        String sql = "SELECT * FROM \"order\" ORDER BY created_at DESC LIMIT ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, limit);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    orders.add(mapResultSetToOrder(rs));
                }
            }
        }
        return orders;
    }
}
