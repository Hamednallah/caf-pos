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

  public Order findById(int orderId) throws SQLException {
      Order order = null;
      String orderSql = "SELECT * FROM \"order\" WHERE id = ?";
      try (PreparedStatement ps = conn.prepareStatement(orderSql)) {
          ps.setInt(1, orderId);
          try (ResultSet rs = ps.executeQuery()) {
              if (rs.next()) {
                  order = new Order();
                  order.setId(rs.getInt("id"));
                  order.setCashierId(rs.getInt("cashier_id"));
                  order.setShiftId(rs.getInt("shift_id"));
                  order.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
                  order.setTotalAmount(rs.getBigDecimal("total_amount"));
                  order.setDiscountAmount(rs.getBigDecimal("discount_amount"));
                  order.setStatus(rs.getString("status"));
                  order.setPaymentMethod(rs.getString("payment_method"));
                  order.setPaymentConfirmed(rs.getBoolean("payment_confirmed"));
              }
          }
      }

      if (order != null) {
          String itemsSql = "SELECT oi.*, i.name as item_name FROM order_item oi JOIN item i ON oi.item_id = i.id WHERE oi.order_id = ?";
          try (PreparedStatement ps = conn.prepareStatement(itemsSql)) {
              ps.setInt(1, orderId);
              java.util.List<OrderItem> items = new java.util.ArrayList<>();
              try (ResultSet rs = ps.executeQuery()) {
                  while (rs.next()) {
                      OrderItem item = new OrderItem();
                      item.setId(rs.getInt("id"));
                      item.setOrderId(rs.getInt("order_id"));
                      item.setItemId(rs.getInt("item_id"));
                      item.setQuantity(rs.getInt("quantity"));
                      item.setLineTotal(rs.getBigDecimal("line_total"));
                      item.setItemName(rs.getString("item_name"));
                      items.add(item);
                  }
              }
              order.setItems(items);
          }
      }
      return order;
  }
}
