package uk.ac.ed.inf;

/**
 * @author zhuhaolin s1930356
 * This is a corresponding Json object for words coordinate
 */
public class ThreeWordCoordinate {
  String country;
  Square square;
  String nearestPlace;
  public Coordinate coordinates;
  String words;
  String language;
  String map;

  public class Square{
    Southwest southwest;
    Northwest northwest;
    public class Southwest{
      double lng;
      double lat;

      @Override
      public String toString() {
        return "Southwest{" +
            "lng=" + lng +
            ", lat=" + lat +
            '}';
      }
    }

    public class Northwest{
      double lng;
      double lat;

      @Override
      public String toString() {
        return "Northwest{" +
            "lng=" + lng +
            ", lat=" + lat +
            '}';
      }
    }
  }

  public class Coordinate{
    public  double lng;
    public double lat;

    @Override
    public String toString() {
      return "Coordinate{" +
          "lng=" + lng +
          ", lat=" + lat +
          '}';
    }
  }

  @Override
  public String toString() {
    return "ThreeWordCoordinate{" +
        "country='" + country + '\'' +
        ", square=" + square +
        ", nearestPlace='" + nearestPlace + '\'' +
        ", coordinates=" + coordinates +
        ", words='" + words + '\'' +
        ", language='" + language + '\'' +
        ", map='" + map + '\'' +
        '}';
  }
}
