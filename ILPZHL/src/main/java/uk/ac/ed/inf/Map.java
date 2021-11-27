package uk.ac.ed.inf;
import com.mapbox.geojson.Feature;

import java.awt.geom.Line2D;
import java.util.List;

/**
 * @author zhuhaolin  s1930356
 * Map class store the geographical information of the GS Square
 * The information includes :
 * 1. Confinement area : drone could  only flay within  this area.
 * 2. Non-fly Zone : drone must not fly across this area.
 * 3. Landmarks: 2 coordinates used to guide the  drone.
 * 4. Starting point : AppleTon.
 */
public class Map {

  //public constants
  public static final double CONFINEMENT_LAT1 = 55.942617;
  public static final double CONFINEMENT_LAT2 = 55.946233;
  public static final double CONFINEMENT_LONG1 = -3.184319;
  public static final double CONFINEMENT_LONG2 = -3.192473;
  public static final double APPLETON_LONG = -3.186874;
  public static final double APPLETON_LAT = 55.944494;
  private List<Line2D> confinementArea;

  //private fields
  private  List<Feature> landmarks;
  private List<Feature> noFlyZone;
  private JsonParser jsonParser;

  /**
   * Constructor in the constructor we will use json parser  to get information about non-fly
   * zone and landmarks from the webserver.
   * @param jsonParser use to request data
   */
  public Map(JsonParser jsonParser){
    jsonParser.parseNoFlyZone();
    jsonParser.parseLandMark();
    this.noFlyZone = jsonParser.getNoFlyZoneAreasList();
    this.landmarks = jsonParser.getLandMarksList();
    //x1y1 x2y2
    //lat1 long1 -> lat1 long2   lat1 long2 -> lat2 long1 la
    confinementArea.add(new Line2D.Double());
  }

  //getters
  public List<Feature> getLandmarks() {
    return landmarks;
  }

  public List<Feature> getNoFlyZone() {
    return noFlyZone;
  }

}
