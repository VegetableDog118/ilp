package uk.ac.ed.inf;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author zhuhaolin s1930356
 * Menus class download menu from the server ,and it provides method to calculate food price
 */
public class Menus {

  // constant
  public static final int STANDARD_DELIVERY_CHARGE = 50;

  // private variable
  private String machineName;
  private String port;
  private Map<String,Integer> itemPriceMap;
  private Map<String, String> foodRestaurantMap;

  /**
   * Constructor of Map
   * In the constructor we should construct the item-price map and food-restaurant map.
   * We get the food menu information from the webserver and store the data into map.
   * @param machineName IP of the machine
   * @param port port number
   */
  public Menus(String machineName,String port){
    this.machineName = machineName;
    this.port = port;
    this.itemPriceMap = new HashMap<>();
    this.foodRestaurantMap = new HashMap<>();
    //get the menu items
    HttpConnection httpConnection = new HttpConnection(machineName,port);
    JsonParser jsonParser = new JsonParser(httpConnection);
    jsonParser.parseFoodMenu();
    //store the food and it's price into a map
    for(Restaurant restaurant:jsonParser.getRestaurantList()){
      StringBuilder sb = new StringBuilder();
      for(Restaurant.Food food : restaurant.menu){
        sb.append(food.item);
      }
      foodRestaurantMap.put(sb.toString(),restaurant.location);
      for(Restaurant.Food food : restaurant.menu){
        itemPriceMap.put(food.item,food.pence);
      }
    }
  }


  /**
   *This method will calculate the value of a list of orders
   * @param orders orders list that each order do not have monetary value information
   * @return orders list that each order as monetary value information
   */
  public List<Order> calculatePriceOfEachOrder(List<Order> orders){
    for(Order order : orders){
      String items = order.getItem();
      String[] itemsArr = items.split(";");
      int monetaryValuePerOrder = calculateValue(itemsArr);
      order.setMonetaryValue(monetaryValuePerOrder);
    }
    return orders;
  }


  /*
   * This method take many foods of a single order as argument and calculate the total value of all
   * the food
   * we should also and the 50p for cost of delivery
   * @param string name of the food
   * @return total price
   */
  private int calculateValue(String ...string){
    int price = STANDARD_DELIVERY_CHARGE;
    for(String item : string){
      price += itemPriceMap.get(item);
    }
    return price;
  }

  // getter
  public Map<String, String> getFoodRestaurantMap() {
    return foodRestaurantMap;
  }

  // unit testing
  public static void main(String[] args) {
    Menus menus = new Menus("localhost","9898");
    HttpConnection httpConnection = new HttpConnection("localhost","9898");
    JsonParser jsonParser = new JsonParser(httpConnection);
    jsonParser.parseFoodMenu();
    for(Restaurant restaurant:jsonParser.getRestaurantList()){
      System.out.println(restaurant.location);
    }
  }
}
