package uk.ac.ed.inf;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * @author zhuhaolin s1930356
 * This class is used to acess the database
 */
public class JDBCDao {

  //private variables
  private static String IP;
  private static String port;

  /**
   * This method will retrieve all the order information from database server on a given date
   * @param date date is in format yyyy-mm-dd
   * @param menus menus class help us to convert out data from database to Order bean
   * @return Return a List of Order object
   * @throws SQLException
   */
  public List<Order> getOneDayOrder(String date, Menus menus) {

    try {
      String str = "jdbc:derby://" + IP + ":" + port +"/derbyDB";
      Connection conn = null;
      conn = DriverManager.getConnection(str);
      String sql  ="select o.orderNo, o.deliveryDate,o.customer,o.deliverTo,od.item "
          + "from "
          + "orders o join "
          + "orderDetails od on o.orderNo = od.orderNo "
          + "where o.deliveryDate = ?";
      PreparedStatement preparedStatement = conn.prepareStatement(sql);
      preparedStatement.setDate(1,java.sql.Date.valueOf(date));
      ResultSet resultSet = preparedStatement.executeQuery();
      ResultSetMetaData rsmd = resultSet.getMetaData();
      int columnsNumber = rsmd.getColumnCount();
      List<Order> orderList = new ArrayList<>();

      //convert the result set to an object
      while (resultSet.next()) {
        Order order = new Order();
        for (int i = 1; i <= columnsNumber; i++) {
          String value = resultSet.getString(i);
          String column = rsmd.getColumnName(i);
          order.convertToOrderBeanFromDB(column,value,order,menus);
        }
        orderList.add(order);
      }
      //merge the orders with same orderId
      String orderId = orderList.get(0).getOrderNo();
      for(int i = 1; i < orderList.size();i++){
        if(orderList.get(i).getOrderNo().equals(orderId)){
          orderList.get(i-1).setItem(orderList.get(i-1).getItem()+";"+orderList.get(i).getItem());
          Set<String> deliveryFrom = orderList.get(i-1).getDeliveryFrom();
          for(String location : orderList.get(i).getDeliveryFrom()){
            deliveryFrom.add(location);
          }
          orderList.get(i-1).setDeliveryFrom(deliveryFrom);
          orderList.remove(i);
          i--;
        }else{
          orderId = orderList.get(i).getOrderNo();
        }
      }
      return orderList;
    } catch (SQLException e) {
      e.printStackTrace();
    }
    return null;
  }

  /**
   * this method will save data of each move of the drone into a table named flightPath
   *
   * @param orderNo the eight-character hexadecimal string assigned to order
   * @param from the starting Coordinate of the move(LongLat object)
   * @param to the destination Coordinate of the move(LongLat object)
   * @param angle the angle of the move
   */
  public void insertFlightData(String orderNo,LongLat from,LongLat to,int angle)  {
    String str = "jdbc:derby://" + IP + ":" + port +"/derbyDB";
    Connection conn = null;
    try {
      conn = DriverManager.getConnection(str);
      String insert = "insert into flightpath (orderNo,fromLongitude,fromLatitude,angle,toLongitude,"
          + "toLatitude) values(?,?,?,?,?,?) ";
      PreparedStatement preparedStatement = conn.prepareStatement(insert);
      preparedStatement.setString(1,orderNo);
      preparedStatement.setDouble(2,from.getLongitude());
      preparedStatement.setDouble(3,from.getLatitude());
      preparedStatement.setInt(4,angle);
      preparedStatement.setDouble(5,to.getLongitude());
      preparedStatement.setDouble(6,to.getLatitude());
      int row = preparedStatement.executeUpdate();

    } catch (SQLException e) {
      System.err.format("SQL State: %s\n%s", e.getSQLState(), e.getMessage());
    }
  }

  /**
   * This method saves ech order details into deliveries table when we finsh sending that order
   * @param orderNo the eight-character hexadecimal string assigned to order
   * @param deliveryTo the What three word of the three word adress
   * @param cost  the total cost of the order in pence
   */
  public void insertOrderData(String orderNo,String deliveryTo,int cost)  {
    String str = "jdbc:derby://" + IP + ":" + port +"/derbyDB";
    Connection conn = null;
    try {
      conn = DriverManager.getConnection(str);
      String insert = "insert into deliveries (orderNo,deliveredTo,costInPence) values(?,?,?)";
      PreparedStatement preparedStatement = conn.prepareStatement(insert);
      preparedStatement.setString(1,orderNo);
      preparedStatement.setString(2,deliveryTo);
      preparedStatement.setInt(3,cost);
      System.out.println("Inserting into deliveries table is successful");
    } catch (SQLException e) {
      System.err.format("SQL State: %s\n%s", e.getSQLState(), e.getMessage());
    }
  }


  /**
   * Constructor
   * @param IP the IP address of the machine
   * @param port the port which the database server run
   */
  public JDBCDao(String IP,String port){
    this.IP = IP;
    this.port = port;
  }

  }

