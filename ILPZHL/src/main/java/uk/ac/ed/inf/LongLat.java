package uk.ac.ed.inf;

import com.mapbox.geojson.Feature;
import com.mapbox.geojson.Point;
import com.mapbox.geojson.Polygon;
import java.awt.geom.Line2D;
import java.util.ArrayList;
import java.util.List;

import static uk.ac.ed.inf.Map.CONFINEMENT_LAT1;
import static uk.ac.ed.inf.Map.CONFINEMENT_LAT2;
import static uk.ac.ed.inf.Map.CONFINEMENT_LONG1;
import static uk.ac.ed.inf.Map.CONFINEMENT_LONG2;

/**
 * @author zhuhaolin
 *a class which represent a point by  longitude and latitude
 */
public class LongLat {

  // public constant
  public static final double MOVE_DISTANCE = 0.00015;
  public static final double DISTANCE_TOLERANCE = 0.00015;

  //private variables
  private double longitude;
  private double latitude;

  /**
   * This method check if the point is in confinement area. It is used to make sure every drone's
   * move is valid.
   * @return return true if the point is in the confinement area, false if not
   */
  public boolean isConfined(){
    return this.latitude > CONFINEMENT_LAT1
        && this.latitude < CONFINEMENT_LAT2
        && this.longitude< CONFINEMENT_LONG1
        && this.longitude> CONFINEMENT_LONG2 ;
  }

  /**
   * This method calculate distance between the coordinate itself and the coordinate given by the
   * parameter
   * @param longLat the coordinate with longitude and latitude
   * @return distance between two points
   */
  public double distanceTo(LongLat longLat){
    double lon2 = Math.pow(longitude-longLat.getLongitude(),2);
    double lat2 = Math.pow(latitude-longLat.getLatitude(),2);
    double ret = Math.sqrt(lon2+lat2);
    return ret;
  }

  /**
   * This method test if two points are closed to each other, if will always be
   * used to check if our drone reach to our target location
   * @param longLat the coordinate with longitude and latitude
   * @return true if two coordinate is less than 0.00015(distance tolerance), return false otherwise
   */
  public boolean closeTo(LongLat longLat){
    return distanceTo(longLat) < DISTANCE_TOLERANCE;
  }

  /**
   * This method find the far coordinate from two coordinates
   * @param l1 the first coordinate with longitude and latitude
   * @param l2 the second coordinate with longitude and latitude
   * @return return the more far away coordinate
   */
  public LongLat farLongLat(LongLat l1,LongLat l2){
    double dis1 = this.distanceTo(l1);
    double dis2 = this.distanceTo(l2);
    return (dis1<dis2)?l2:l1;
  }

  /**
   * This method find the closer coordinate from two coordinates
   * @param l1 the first coordinate with longitude and latitude
   * @param l2 the second coordinate with longitude and latitude
   * @return return the more closer away coordinate
   */
  public LongLat closerLongLat(LongLat l1,LongLat l2) {
    double dis1 = this.distanceTo(l1);
    double dis2 = this.distanceTo(l2);
    return (dis1 > dis2) ? l2 : l1;
  }

  /**
   * This method calculate the angle of the movement between two coordinates.
   * @param destination the second coordinate with longitude and latitude
   * @return angle of the movement this angle is multiple of 10 and it is between 0 - 360
   */
  public  int getAngle(LongLat destination) {
    var yMovement = destination.getLatitude() - this.latitude;
    var xMovement = destination.getLongitude() - this.longitude;
    var angleRadians = Math.atan(yMovement / xMovement);
    var angleDegrees = Math.toDegrees(angleRadians);
    var angleFromEast = 0.0;
    // Calculate the angle
    if (xMovement > 0 && yMovement > 0) {
      angleFromEast = angleDegrees;
    } else if (xMovement < 0 && yMovement > 0) {
      angleFromEast = 180 - Math.abs(angleDegrees);
    }  else if (xMovement > 0 && yMovement < 0) {
      angleFromEast = 360 - (Math.abs(angleDegrees));
    }else if (xMovement < 0 && yMovement < 0) {
      angleFromEast = 180 + angleDegrees;
    }
    // we should make sure the angle is multiple of ten
    var angleDown = (int) (angleFromEast - angleFromEast % 10);
    var angleUp = (int) ((10 - angleFromEast % 10) + angleFromEast);
    if ((angleUp - angleFromEast) >= (angleFromEast - angleDown)) {
      return angleDown;
    } else {
      return angleUp;
    }
  }


  /**
   * This method move the drone's with angle by one unit
   * @param angle The direction the drone's move
   * @return The coordinate after the drone's movement
   */
  public LongLat nextPosition(int angle){
    LongLat nextPosition =
        new LongLat(this.latitude+ Math.sin(Math.toRadians(angle))*MOVE_DISTANCE,
            this.longitude + Math.cos(Math.toRadians(angle))*MOVE_DISTANCE
            );
    return nextPosition;
  }

  /**
   * This method check if the move of drone will cross no-fly zone. It will decide if the
   * line of movement will intersect with the edges of the forbidden area. If two lines are
   * crossed then it will fly to non-fly zone.
   * @param map it contains geographical information help us to check non-fly zone
   * @param initialCoordinate the drone's initial point before move
   * @param targetCoordinate the drone's point after move
   * @return true if the move cross the no-fly-zone, otherwise not.
   */
  public boolean isCrossingNonFLyZone(Map map, LongLat initialCoordinate,
                                      LongLat targetCoordinate) {
    Line2D movementLine = new Line2D.Double(initialCoordinate.latitude,initialCoordinate.longitude,
        targetCoordinate.latitude, targetCoordinate.longitude);
    List<Line2D> noFlyLines = new ArrayList<>();
    for (Feature feature : map.getNoFlyZone()) {
      Polygon polygon = (Polygon) feature.geometry();
      List<List<Point>> lines = polygon.coordinates();
      List<Point> line = lines.get(0);
      for (int i = 0; i < line.size() - 1; i++) {
        int k = (i+1)%line.size();
        noFlyLines.add(new Line2D.Double(line.get(i).latitude(),
            line.get(i).longitude()
            , line.get(k).latitude(),
            line.get(k).longitude()));
      }
    }
    //check if the move cross each line
    for (Line2D noFlyLine : noFlyLines) {
      if (noFlyLine.intersectsLine(movementLine)) {
        return true;
      }
    }
    return false;
  }


  //getters and setters
  public double getLatitude() {
    return latitude;
  }

  public double getLongitude() {
    return longitude;
  }

  /**
   * constructor
   * @param longitude
   * @param latitude
   */
  public LongLat(double latitude,double longitude){
    this.longitude = longitude;
    this.latitude = latitude;
  }

  @Override
  public String toString() {
    return "LongLat{" +
        "longitude=" + longitude +
        ", latitude=" + latitude +
        '}';
  }

  public static void main(String[] args) {
    HttpConnection httpConnection  = new HttpConnection("localhost","9898");
    JsonParser jsonParser = new JsonParser(httpConnection);
    Map map = new Map(jsonParser);
    LongLat ret = new LongLat(55.9440,-3.1885);
    LongLat dest = new LongLat(55.9457,-3.1862);

    LongLat dest2 = new LongLat(55.9454,-3.1884);
    LongLat dest3 = new LongLat(55.9437,-3.1916);
    System.out.println(ret.isCrossingNonFLyZone(map,ret,dest));
    System.out.println(ret.isCrossingNonFLyZone(map,ret,dest2));
    System.out.println(dest2.getAngle(dest3));
  }
}
