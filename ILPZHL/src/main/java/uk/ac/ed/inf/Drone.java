package uk.ac.ed.inf;

import com.mapbox.geojson.Feature;
import com.mapbox.geojson.Point;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Queue;
import java.util.TreeMap;

/**
 * @author zhuhaolin s1930356 Class drone class control the flight of the drone
 */
public class Drone {

  //public constants
  public static final int BATTERY = 1500;

  //private fields:
  private LongLat currentCoordinate;// this is the up-to-date position of the drone,it will
  // update after each move
  private final LongLat startingCoordinate;// appleton tower
  private uk.ac.ed.inf.Map map;
  private List<LongLat> route;
  private Queue<Order> sortedOrders;
  private JDBCDao jdbcDao;
  private int deliveryMonetaryValue;
  private int batteryRemaining;  // how many batteries left can not be zero


  /**
   * Constructor
   *
   * @param startingCoordinate AppletonTower
   * @param map                map class contains geographical information which could help drone to
   *                           generate valid route
   * @param orders             List of unsorted orders to delivery
   * @param jdbcDao            used to insert order  information to database
   */
  public Drone(LongLat startingCoordinate, uk.ac.ed.inf.Map map,
               List<Order> orders, JDBCDao jdbcDao) {
    this.map = map;
    this.startingCoordinate = startingCoordinate;
    this.currentCoordinate = startingCoordinate;
    this.route = new ArrayList<>();
    this.batteryRemaining = BATTERY;
    this.jdbcDao = jdbcDao;
    //add list of order into our  order queue
    Queue<Order> sortedOrder = new LinkedList<>();
    System.out.println();
    for (Order order : orders) {
      sortedOrder.add(order);
    }
    this.sortedOrders = sortedOrder;
    System.out.println("now order size" + sortedOrder.size());
  }


  /**
   * This method will send one order  from the order queue. It will let drone fly with precalculated
   * coordinates. It will store move information into our database server when drone make a small
   * valid move. It will store order information into our database server when we reach  to our
   * client.
   *
   * @param order the current order we have polled from the queue
   */
  public void sendAnOrder(Order order) {
    List<LongLat> route = order.getRoutes();
    System.out.println(route.size());
    for (int i = 1; i < route.size(); i++) {
      LongLat targetLocation = route.get(i);
      while (!this.currentCoordinate.closeTo(targetLocation)) {
        int angle = this.currentCoordinate.getAngle(targetLocation);
        LongLat prevCoordinate = this.currentCoordinate;
        this.currentCoordinate = this.currentCoordinate.nextPosition(angle);
        this.route.add(this.currentCoordinate);
        //insert movement data
        jdbcDao.insertFlightData(order.getOrderNo(), prevCoordinate, this.currentCoordinate,
            angle);
        if (!this.currentCoordinate.isConfined()) {
          System.exit(0);
        }
        //after each move the battery -1
        this.batteryRemaining--;
      }
    }
    // for each hovering  our battery also need -1
    this.batteryRemaining -= (order.getDeliveryTo().length() + order.getShopsCoordinates().size());
    this.deliveryMonetaryValue += order.getMonetaryValue();
    //insert delivery data
    jdbcDao.insertOrderData(order.getOrderNo(), order.getDeliveryTo(), order.getMonetaryValue());
  }


  /**
   * This private method  will choose a landmark to guide drone's route when drone is going to have
   * an invalid move. It will check if the landmark is valid after inserting it, and it will choose
   * the closer one if both landmarks are valid.
   *
   * @param position1 the initial coordinate of the  invalid move
   * @param position2 the final coordinate of the invalid move
   * @return return the suitable landmark's coordinate
   */
  private LongLat chooseALandMark(LongLat position1, LongLat position2) {
    //get landmark information from map
    List<Feature> landMarks = map.getLandmarks();
    Point point1 = (Point) landMarks.get(0).geometry();
    LongLat landmark1 = new LongLat(point1.latitude(), point1.longitude());
    Point point2 = (Point) landMarks.get(1).geometry();
    LongLat landmark2 = new LongLat(point2.latitude(), point2.longitude());
    // check if the landmarks are valid
    boolean landmark1Available =
        !landmark1.isCrossingNonFLyZone(map, position1, landmark1) && !landmark1
            .isCrossingNonFLyZone(map, landmark1, position2);
    boolean landmark2Available =
        !landmark2.isCrossingNonFLyZone(map, position1, landmark2) && !landmark2.isCrossingNonFLyZone(map, landmark2, position2);
    if (landmark1Available && landmark2Available) {
      //always choose closer one when both  are available
      return position1.closerLongLat(landmark1, landmark2);
    } else if (landmark1Available) {
      return landmark1;
    } else if (landmark2Available) {
      return landmark2;
    } else {
      System.out.println("both are not work");
      //we need to fly to the destination without the help of the current  two landmarks
      //Here we are going to generate our own landmark according to landmark infomation from
      // webserver to cover all the points.
      //generateGuidingPoint(l)
    }
    return null;
  }

  private LongLat generateGuidingPoint(LongLat landMark1, LongLat landMark2, LongLat position1,
                                       LongLat position2) {
    LongLat midPoint = new LongLat((landMark1.getLatitude() + landMark2.getLatitude()) / 2,
         (landMark1.getLongitude() + landMark2.getLongitude()) / 2);
    int angle = landMark1.getAngle(landMark2);
    LongLat guidingPoint1 = null;
    LongLat guidingPoint2 = null;
    LongLat point = midPoint;
    while (!point.isConfined()) {
      guidingPoint1 = midPoint;
      point =
          new LongLat(midPoint.getLatitude() + Math.sin(Math.toRadians(angle - 90)) * LongLat.MOVE_DISTANCE * 2,
              midPoint.getLongitude() + Math.cos(Math.toRadians(angle - 90)) * LongLat.MOVE_DISTANCE * 2
          );
    }
    point = midPoint;
    while (!point.isConfined()) {
      guidingPoint2 = midPoint;
      point =
          new LongLat(midPoint.getLatitude() + Math.sin(Math.toRadians(angle + 90)) * LongLat.MOVE_DISTANCE * 2,
              midPoint.getLongitude() + Math.cos(Math.toRadians(angle + 90)) * LongLat.MOVE_DISTANCE * 2
          );
    }
    boolean guidingPoint1Validity =
        !position1.isCrossingNonFLyZone(map, position1, guidingPoint1) && !guidingPoint1.isCrossingNonFLyZone(map, guidingPoint1, position2);

    boolean guidingPoint2Validity =
        !position1.isCrossingNonFLyZone(map, position1, guidingPoint2) && !guidingPoint2.isCrossingNonFLyZone(map, guidingPoint2, position2);

    if (guidingPoint1Validity && guidingPoint2Validity) {
      //always choose closer one when both  are available
      return position1.closerLongLat(guidingPoint1, guidingPoint2);
    } else if (guidingPoint1Validity) {
      return guidingPoint1;
    } else if (guidingPoint2Validity) {
      return landMark2;

    }
    return null;
  }

  /**
   * This method will generate route of the drone. It will generate route of each order and it
   * will
   * sort the orders according to their values; It will be called after the drone has sent an
   * order.
   */
  public void generateRoute() {
    Queue<Order> orderSorted = this.sortedOrders;
    for (Order order : orderSorted) {
      // generate route within an order
      System.out.println("Generate each order" + order.getOrderNo());
      List<LongLat> route = new ArrayList<>();
      route.add(this.currentCoordinate);
      List<LongLat> shops = order.getShopsCoordinates();
      LongLat destination = order.getDestinationCoordinate();
      System.out.println("add route");
      if (shops.size() == 2) {
        route.add(destination.farLongLat(shops.get(0), shops.get(1)));
        route.add(destination.closerLongLat(shops.get(0), shops.get(1)));
      } else {
        route.add(shops.get(0));
      }
      route.add(destination);
      //checking non-fly zone
      System.out.println("checking non-fly-zone");
      System.out.println("checking" + order.getOrderNo());
      for (int i = 1; i < route.size(); i++) {
        System.out.println(route.size());
        LongLat l1 = route.get(i - 1);
        LongLat l2 = route.get(i);
        System.out.println(l1);
        System.out.println(l2);
        if (l1.isCrossingNonFLyZone(map, l1, l2)) {
          //when the drone will cross no-fly zone,it will insert a valid landmark between points
          System.out.println("inserting landMark");
          int insertIndex = i;
          LongLat landMark = chooseALandMark(l1, l2);
          System.out.println("lm:" + landMark);
          route.add(insertIndex, landMark);
          if (landMark.isCrossingNonFLyZone(map, landMark, route.get(insertIndex + 1))) {
            System.out.println("landmark right" + route.get(insertIndex + 1));
            System.out.println("crossing again......");
            System.exit(0);
          }
        }
      }
      //order's route has generated
      order.setRoutes(route);
      //calculate the estimated batteries needed for an order
      order.setBatteriesNeeded(calculateMoves(route));
      int hoveringTimes = order.getShopsCoordinates().size() + 1;
      order.setBatteriesNeeded(calculateMoves(route) + hoveringTimes);
    }

    //now sort the order according to their value-battery/10
    //we will consider two factors money of the order and distance of the order
    Map<Integer, Order> map = new TreeMap<Integer, Order>(new Comparator<Integer>() {
      @Override
      public int compare(Integer o1, Integer o2) {
        return (int) (o2 - o1);
      }
    });
    for (Order order : orderSorted) {
      map.put(order.getMonetaryValue() - order.getBatteriesNeeded() / 10, order);
    }
    Queue<Order> sortedOrders = new LinkedList<>();
    Iterator<Map.Entry<Integer, Order>> iterator = map.entrySet().iterator();
    while (iterator.hasNext()) {
      Map.Entry<Integer, Order> me = iterator.next();
      Order order = me.getValue();
      sortedOrders.add(order);
    }
    this.sortedOrders = sortedOrders;
    System.out.println("finish sorting...");
  }


  /**
   * This is private helper function used to calculate an estimated moves of an order. This is
   * estimated  because when the drone actually fly it may use extra move to change direction and
   * fly back if drone fly passed by.
   *
   * @param route list of coordinates a drone need to fly
   * @return the estimated moves
   */
  private int calculateMoves(List<LongLat> route) {
    int moves = 0;
    for (int i = 1; i < route.size(); i++) {
      moves += route.get(i - 1).distanceTo(route.get(i)) / LongLat.MOVE_DISTANCE;
    }
    return moves;
  }

  /*
  This function will let drone fly back to Appleton tower when:
  1. Drone has sent all the orders
  2. Drone's battery is not enough
  It will also generate a valid move by checking if the drone can fly directly to home. If not,
  it will add a valid landmark to guide drone.
   */
  public void flyingBack() {
    LongLat home = this.startingCoordinate;
    //if drone cannot fly directly to home , it needs to find a landmark
    if (this.currentCoordinate.isCrossingNonFLyZone(map, this.currentCoordinate, home)) {
      LongLat landMark = chooseALandMark(this.currentCoordinate, home);
      //fly to the landmark
      while (!this.currentCoordinate.closeTo(landMark) && this.batteryRemaining > 0) {
        int angle1 = this.currentCoordinate.getAngle(landMark);
        this.currentCoordinate = this.currentCoordinate.nextPosition(angle1);
        route.add(this.currentCoordinate);
        this.batteryRemaining--;
        if (batteryRemaining <= 0) {
          break;
        }
      }
    }
    //fly back to home from landmark or other point
    while (!this.currentCoordinate.closeTo(home) && this.batteryRemaining > 0) {
      int angle2 = this.currentCoordinate.getAngle(home);
      this.currentCoordinate = this.currentCoordinate.nextPosition(angle2);
      route.add(this.currentCoordinate);
      this.batteryRemaining--;
      if (batteryRemaining <= 0) {
        break;
      }
    }
    System.out.println("flying back successful");
  }

  //getters and setters
  public LongLat getCurrentCoordinate() {
    return currentCoordinate;
  }

  public List<LongLat> getRoute() {
    return route;
  }

  public Queue<Order> getSortedOrders() {
    return sortedOrders;
  }

  public int getBatteryRemaining() {
    return batteryRemaining;
  }
}

