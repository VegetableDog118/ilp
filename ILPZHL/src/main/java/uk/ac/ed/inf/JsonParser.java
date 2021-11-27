package uk.ac.ed.inf;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import com.mapbox.geojson.Feature;
import com.mapbox.geojson.FeatureCollection;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;


/**
 * @author zhuhaolin s1930356
 * This class can deserializing a JSON list to a Java object using its type.
 */
public class JsonParser {

  //private variables
  //used to request data from the server
  HttpConnection httpConnection;

  private List<Restaurant> restaurantList;

  private List<Feature> noFlyZoneAreasList;

  private List<Feature> landMarksList;



  //Method
  /**
   * This method parse the menus.json to a restaurant Java object.
   */
  public void parseFoodMenu(){
    //produce the correct url
    String url = "http://" + httpConnection.getMachineName()+ ":"+ httpConnection.getPort()+
        "/menus/menus.json";
    this.httpConnection.getConnectToServer(url);
    Type listType =
        new TypeToken<ArrayList<Restaurant>>() {}.getType();
    // assign the value to  list of restaurant object
    this.restaurantList = new Gson().fromJson(this.httpConnection.getJsonContent(), listType);
  }

  /**
   * This is a constructor of the JsonParser class
   * @param httpConnection used to connect to the server
   */
  public JsonParser(HttpConnection httpConnection) {
    this.httpConnection = httpConnection;
  }


  /**
   * Parse words it could parse  an area one the map from three words coordinate
   * @param word1 The first word
   * @param word2 The second word
   * @param word3 The third word
   * @return return three word coordinate object
   */
  public ThreeWordCoordinate parseWords(String word1,String word2,String word3){
    String url = "http://"+httpConnection.getMachineName()+":"+httpConnection.getPort()+ "/words"
        + "/" + word1 +"/" + word2 +"/" + word3 +"/details.json";
    this.httpConnection.getConnectToServer(url);

    ThreeWordCoordinate threeWordCoordinate =
        new Gson().fromJson(this.httpConnection.getJsonContent(),
      ThreeWordCoordinate.class);
    return threeWordCoordinate;
  }

  /**
   * Parse the building of non-fly zone from web server
   */
  public  void parseNoFlyZone(){
    String url  = "http://" + httpConnection.getMachineName()+ ":"+ httpConnection.getPort()+
        "/buildings/no-fly-zones.geojson";
    this.httpConnection.getConnectToServer(url);
    this.noFlyZoneAreasList =
        FeatureCollection.fromJson(this.httpConnection.getJsonContent()).features();
  }

  /**
   * Parse the coordinates of LandMarks from the webserver
   */
  public void parseLandMark(){
    String url  = "http://" + httpConnection.getMachineName()+ ":"+ httpConnection.getPort()+
        "/buildings/landmarks.geojson";
    this.httpConnection.getConnectToServer(url);
    this.landMarksList =
        FeatureCollection.fromJson(this.httpConnection.getJsonContent()).features();
  }

  // getters
  public List<Restaurant> getRestaurantList() {
    return restaurantList;
  }

  public List<Feature> getNoFlyZoneAreasList() {
    return noFlyZoneAreasList;
  }

  public List<Feature> getLandMarksList() {
    return landMarksList;
  }
}
