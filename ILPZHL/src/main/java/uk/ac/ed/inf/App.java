package uk.ac.ed.inf;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;


/**
 * Application for starting the  drone to send order
 *
 * @author Haolin Zhu s1930356
 *
 */
public class App {

    //constant IP address
    private static final String IP = "localhost";

    /**
     * Main function
     *
     * @param args
     *             <ul>
     *             <li>[0] - Flight day</li>
     *             <li>[1] - Flight month</li>
     *             <li>[2] - Flight year</li>
     *             <li>[3] - Webserver port</li>
     *             <li>[4] - Database port</li>
     *
     *             </ul>
     */
    public static void main( String[] args ){

        //Get the arguments
        int day = Integer.parseInt(args[0]);
        int month = Integer.parseInt(args[1]);
        int year = Integer.parseInt(args[2]);
        int webserverPort = Integer.parseInt(args[3]);
        int databasePort = Integer.parseInt(args[4]);

        //build a connection to webserver
        HttpConnection httpConnection = new HttpConnection(IP,String.valueOf(webserverPort));
        JsonParser jsonParser = new JsonParser(httpConnection);
        //build a connection to database server
        JDBCDao jdbcDao = new JDBCDao(IP,String.valueOf(databasePort));
        // Initializing menus map
        Menus menus = new Menus(IP,String.valueOf(webserverPort));

        //query orders from database on a specific date
        List<Order> orders =
            jdbcDao.getOneDayOrder(String.valueOf(year)+"-"+String.valueOf(month)+"-"+String.valueOf(day),menus);
        // calculate price of each order
        orders = menus.calculatePriceOfEachOrder(orders);
        // convert three word coordinate to Longitude and latitude
        orders = convertThreeWordCoordinateToLongLat(orders,jsonParser);
        int tmoney = 0;
        for(Order order : orders){
            tmoney+=order.getMonetaryValue();
            System.out.println(order);
        }

        LongLat startingPoint = new LongLat(Map.APPLETON_LAT,Map.APPLETON_LONG);
        Map map = new Map(jsonParser);
        Drone drone = new Drone(startingPoint,map,orders,jdbcDao);


        //Drone start to fly
        int money = 0;
        System.out.println("start flying-------------------");
        while(drone.getBatteryRemaining()>0){
            System.out.println("battery remaining:"+drone.getBatteryRemaining());
            System.out.println("current location: " + drone.getCurrentCoordinate());
            System.out.println("Generating route");
            //Drone will generate the order sequence  and the route in each iteration
            drone.generateRoute();
            for (Order order : drone.getSortedOrders()){
                System.out.println(order);
            }
            System.out.println("order sequence has been generated");
            Order order = null;

            if(drone.getSortedOrders().isEmpty()){
                // Drone will fly back to appleton tower if the drone has sent all the orders
                System.out.println("flying back home");
                drone.flyingBack();
                break;
            }else{
                // poll an order from the order queue
                order = drone.getSortedOrders().poll();
                System.out.println("order size after polling: "+ drone.getSortedOrders().size());
                System.out.println(order);
            }
            if(order.getBatteriesNeeded()*1.5> drone.getBatteryRemaining()){
                //if this order needs batteries more than the drone's remained, we will go back home
                //1.5 is an augmentation factor to make sure we could send this order
                System.out.println("Battery is not enough,flying back home");
                drone.flyingBack();
                break;
            }else{
                // we have enough battery to send this order
                drone.sendAnOrder(order);
                money+=order.getMonetaryValue();
                System.out.println("finish sending order: "+order.getOrderNo());
                System.out.println("queue size :"+drone.getSortedOrders().size());
            }
        }
        System.out.println("battery:"+drone.getBatteryRemaining());
        java.text.DecimalFormat df=new java.text.DecimalFormat("##.##%");
        System.out.println((float)money/(float)tmoney
        );
        //write the GeoJson file
        GeoJsonWriter geoJsonWriter = new GeoJsonWriter();
        geoJsonWriter.writeFiles(drone.getRoute(),args[0],args[1],args[2]);
    }

    /**
     * This method will convert each order's three word coordinate to latitude and longitude
     * @param orders orders the drone need to sent on that date with three word coordinate
     * @param jsonParser Json parser could parse data from webserver we will use this class to
     *                   parse three word coordinate
     *
     *
     * @return return a list of orders with longitude and latitude coordinates
     */
    private static List<Order> convertThreeWordCoordinateToLongLat(List<Order> orders,
                                                          JsonParser jsonParser) {
        for(Order order: orders){
            //get the three word coordinate of each order
            //parse order's destination
            String[] words1 = order.getDeliveryTo().split("\\.");
            ThreeWordCoordinate threeWordCoordinateDest = jsonParser.parseWords(words1[0],words1[1],
                words1[2]);
            order.setDestinationCoordinate(new LongLat(threeWordCoordinateDest.coordinates.lat,
                threeWordCoordinateDest.coordinates.lng));
            // parse order's shop
            List<LongLat> shopList = new ArrayList<>();
            for(String string :order.getDeliveryFrom()){
                String[] words2 = string.split("\\.");
                ThreeWordCoordinate threeWordCoordinateShop = jsonParser.parseWords(words2[0],
                    words2[1],words2[2]);
                shopList.add(new LongLat(threeWordCoordinateShop.coordinates
                    .lat,threeWordCoordinateShop.coordinates.lng));
            }
            order.setShopsCoordinates(shopList);
        }
        return orders;
    }
}
