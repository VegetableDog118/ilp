package uk.ac.ed.inf;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author zhuhaolin s1930356
 * class represents the order the drone needs to send
 */
public class Order {

  //private variable
  private int monetaryValue;//total money of an order
  private String orderNo;
  private String deliveryDate;
  private String customer;
  private String deliveryTo;
  private String item;
  private Menus menus;
  private Set<String> deliveryFrom;
  private List<LongLat> shopsCoordinates;
  private LongLat destinationCoordinate;
  //a list of coordinates the drone will move according to this routes
  private List<LongLat> routes;
  // an estimation of how many moves a order needed before sending
  private int batteriesNeeded;

  /**
   * This method converts the data from we get using JDBCDao into an Order bean object.
   * It will match each column name with the  attribute name and use setter method the set the
   * field.
   * @param column Column name of the database
   * @param value value from the result set
   * @param order Order Object we need to return
   * @param menus Menus class used to find the restaurant according to food
   */
  public void convertToOrderBeanFromDB(String column, String value, Order order, Menus menus){

    switch (column){
      case "ORDERNO":
        order.setOrderNo(value);
        break;
      case "DELIVERYDATE":
        order.setDeliveryDate(value);
        break;
      case "CUSTOMER":
        order.setCustomer(value);
        break;
      case "DELIVERTO":
        order.setDeliveryTo(value);
        break;
      case "ITEM":
        order.setItem(value);
        findDeliveryFrom(value,menus);
        break;
    }
  }

  /**
   * This method could find the restaurants of the order(from the Orders table there are no
   * restaurant information)
   * @param food food items of this order
   * @param menus Menus class which has a food-restaurant map which could find restaurant from food
   */
  private void findDeliveryFrom(String food, Menus menus) {
    Map<String, String> map = menus.getFoodRestaurantMap();
    Set<String> deliveryFrom = new HashSet<>();
    for (String foods : map.keySet()) {
      if (foods.contains(food)) {
        deliveryFrom.add(map.get(foods));
      }
    }
    // it will assign the restaurants to Order object
    this.deliveryFrom = deliveryFrom;
  }

  // getters and setters
  public List<LongLat> getRoutes() {
    return routes;
  }

  public Set<String> getDeliveryFrom() {
    return deliveryFrom;
  }

  public void setDeliveryFrom(Set<String> deliveryFrom) {
    this.deliveryFrom = deliveryFrom;
  }

  public void setOrderNo(String orderNo) {
    this.orderNo = orderNo;
  }

  public void setDeliveryDate(String deliveryDate) {
    this.deliveryDate = deliveryDate;
  }

  public void setCustomer(String customer) {
    this.customer = customer;
  }

  public void setDeliveryTo(String deliveryTo) {
    this.deliveryTo = deliveryTo;
  }

  public void setItem(String item) {
    this.item = item;
  }

  public void setShopsCoordinates(List<LongLat> shopsCoordinates) {
    this.shopsCoordinates = shopsCoordinates;
  }

  public void setDestinationCoordinate(LongLat destinationCoordinate) {
    this.destinationCoordinate = destinationCoordinate;
  }

  public int getMonetaryValue() {
    return monetaryValue;
  }

  public void setMonetaryValue(int monetaryValue) {
    this.monetaryValue = monetaryValue;
  }

  public String getOrderNo() {
    return orderNo;
  }

  public String getItem() {
    return item;
  }

  public String getDeliveryTo() {
    return deliveryTo;
  }

  public int getBatteriesNeeded() {
    return batteriesNeeded;
  }

  public List<LongLat> getShopsCoordinates() {
    return shopsCoordinates;
  }

  public LongLat getDestinationCoordinate() {
    return destinationCoordinate;
  }

  public void setRoutes(List<LongLat> routes) {
    this.routes = routes;
  }

  public void setBatteriesNeeded(int batteriesNeeded) {
    this.batteriesNeeded = batteriesNeeded;
  }


  @Override
  public String toString() {
    return "Order{" +
        "monetaryValue=" + monetaryValue +
        ", orderNo='" + orderNo + '\'' +
        ", deliveryDate='" + deliveryDate + '\'' +
        ", customer='" + customer + '\'' +
        ", deliveryTo='" + deliveryTo + '\'' +
        ", item='" + item + '\'' +
        ", menus=" + menus +
        ", deliveryFrom=" + deliveryFrom +
        ", shopsCoordinates=" + shopsCoordinates +
        ", destinationCoordinate=" + destinationCoordinate +
        ", routes=" + routes +
        ", batteriesNeeded=" + batteriesNeeded +
        '}';
  }
}
