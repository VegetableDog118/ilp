package uk.ac.ed.inf;

import com.mapbox.geojson.Feature;
import com.mapbox.geojson.FeatureCollection;
import com.mapbox.geojson.Geometry;
import com.mapbox.geojson.LineString;
import com.mapbox.geojson.Point;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author zhuhaolin s1930356
 * This class is used to generate GeoJson file from our drone's flight data.
 */
public class GeoJsonWriter {

  /**
   *  This method write fly path data into a geo-json file.
   *
   * @param route List of coordinates of the drone's movement
   * @param yyyy year eg 2023, 2022...
   * @param mm month eg 01-12
   * @param dd date eg 01-31
   */
  public void writeFiles(List<LongLat> route, String yyyy,
                                 String mm, String dd) {
    // Write the flight path file
    try {
      java.io.FileWriter writer = new java.io.FileWriter(
          "drone-"+dd+"-"+mm+"-"+yyyy+".geojson");
      List<Point> routes = new ArrayList<>();
      for(LongLat longLat : route){
        routes.add(Point.fromLngLat(longLat.getLongitude(),
            longLat.getLatitude()));
      }
      Feature feature = Feature.fromGeometry((Geometry) LineString.fromLngLats(routes));
      writer.write(FeatureCollection.fromFeature(feature).toJson());
      writer.close();
      System.out.println("Geo json flight pat generated ");
    } catch (IOException e) {
      System.out.println("Fatal error: Readings GeoJson wasn't created.");
      e.printStackTrace();
    }
  }
}
