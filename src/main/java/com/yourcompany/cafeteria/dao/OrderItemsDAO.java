package com.yourcompany.cafeteria.dao;
import java.sql.*; import java.util.*; import com.yourcompany.cafeteria.model.OrderItem;
public class OrderItemsDAO {
  private final Connection conn; public OrderItemsDAO(Connection c){ this.conn=c; }
  public List<OrderItem> listByOrderId(int orderId) throws SQLException { List<OrderItem> out = new ArrayList<>(); try (PreparedStatement ps = conn.prepareStatement("SELECT id,order_id,item_id,quantity,line_total FROM order_item WHERE order_id=?")){ ps.setInt(1, orderId); try(ResultSet rs=ps.executeQuery()){ while(rs.next()){ OrderItem oi = new OrderItem(); oi.setId(rs.getInt(1)); oi.setOrderId(rs.getInt(2)); oi.setItemId(rs.getInt(3)); oi.setQuantity(rs.getInt(4)); oi.setLineTotal(rs.getBigDecimal(5)); out.add(oi);} } } return out; }
}
