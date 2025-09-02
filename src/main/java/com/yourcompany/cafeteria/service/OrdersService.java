package com.yourcompany.cafeteria.service;
import com.yourcompany.cafeteria.dao.OrdersDAO;
import com.yourcompany.cafeteria.model.Order;
import java.sql.Connection;
public class OrdersService { private final OrdersDAO dao; public OrdersService(Connection c){ this.dao=new OrdersDAO(c); } public int create(Order o) throws Exception { return dao.createOrderTransactional(o); } }
