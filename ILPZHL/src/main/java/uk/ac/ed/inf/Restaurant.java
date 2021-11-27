package uk.ac.ed.inf;

import java.util.List;

/**
 * @author zhuhaolin s1930356
 *
 * This Json object for a restaurant
 *
 */
public class Restaurant {
  String name;
  String location;
  List<Food> menu;
  public static class Food {
    String item;
    int pence;
  }

  @Override
  public String toString() {
    return "Restaurant{" +
        "name='" + name + '\'' +
        ", location='" + location + '\'' +
        ", menu=" + menu +
        '}';
  }
}
