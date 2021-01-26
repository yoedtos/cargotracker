package org.eclipse.cargotracker.application.util;

import org.eclipse.cargotracker.domain.model.location.Location;

public class LocationUtil {

  public static String getLocationName(String location) {
    // Helsinki (FIHEL)
    return location.substring(0, location.indexOf("("));
  }

  public static String getLocationCode(String location) {
    return location.substring(location.indexOf("(") + 1, location.indexOf(")"));
  }

  public static String asString(Location location) {
    return location.getName() + " (" + location.getUnLocode() + ")";
  }
}
