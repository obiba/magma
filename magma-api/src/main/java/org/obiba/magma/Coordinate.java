/*
 * Copyright (c) 2013 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.magma;

import java.io.Serializable;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import static java.lang.Double.isNaN;

public class Coordinate implements Comparable<Coordinate>, Serializable {

  private static final long serialVersionUID = 8139103526460838188L;

  private static final String[] LATITUDES_KEYS = new String[] { "lat", "latitude", "lt" };

  private static final String[] LONGITUDES_KEYS = new String[] { "lon", "lng", "longitude", "lg" };

  private final double longitude;

  private final double latitude;

  public Coordinate(double longitude, double latitude) {
    this.longitude = longitude;
    this.latitude = latitude;
  }

  public double getLongitude() {
    return longitude;
  }

  public double getLatitude() {
    return latitude;
  }

  @Override
  public int compareTo(Coordinate o) {
    if(longitude < o.getLongitude()) return -1;
    if(longitude > o.getLongitude()) return 1;
    if(latitude < o.getLatitude()) return -1;
    if(latitude > o.getLatitude()) return 1;
    return 0;
  }

  @Override
  public boolean equals(Object obj) {
    if(this == obj) return true;
    if(obj == null) return false;

    if(obj instanceof Coordinate) {
      Coordinate o = (Coordinate) obj;
      return getLatitude() == o.getLatitude() && getLongitude() == o.getLongitude();
    }
    return false;
  }

  @Override
  public int hashCode() {

    int result = 17;
    result = 37 * result + Double.valueOf(longitude).hashCode();
    result = 37 * result + Double.valueOf(latitude).hashCode();
    return result;
  }

  @Override
  public String toString() {
    return "[" + longitude + "," + latitude + "]";
  }

  public static Coordinate valueOf(String string) {

    double[] coordinate;

    // GeoJSON coordinate
    if(string.trim().startsWith("[")) {
      coordinate = parseGeoJson(string);
    }
    // JSON coordinate
    else if(string.trim().startsWith("{")) {
      coordinate = parseJSON(string);
    }
    // Google coordinate  lat,long
    else {
      String stringToParse = "[" + string.trim() + "]";
      coordinate = parseGoogleMap(stringToParse);
    }

    // parse s to Coordinate
    return new Coordinate(coordinate[0], coordinate[1]);
  }

  /**
   * Parse a string representing a GeoJSON coordinate
   *
   * @param s
   * @return [longitude, latitude]
   */
  private static double[] parseGeoJson(String s) {
    double lng;
    double lat;
    try {
      JSONArray array = new JSONArray(s);
      lng = array.getDouble(0);
      lat = array.getDouble(1);
    } catch(JSONException e) {
      throw new MagmaRuntimeException("Not a coordinate point", e);
    }
    return new double[] { lng, lat };
  }

  /**
   * Parse a string representing a Google Map coordinate
   *
   * @param s
   * @return [longitude, latitude]
   */
  private static double[] parseGoogleMap(String s) {
    double lng;
    double lat;
    try {
      JSONArray array = new JSONArray(s);
      lng = array.getDouble(1);
      lat = array.getDouble(0);
    } catch(JSONException e) {
      throw new MagmaRuntimeException("Not a coordinate point", e);
    }
    return new double[] { lng, lat };
  }

  /**
   * Parse a string representing a JSON coordinate
   *
   * @param s
   * @return [longitude, latitude]
   */
  private static double[] parseJSON(String s) {

    double lng;
    double lat;

    try {
      JSONObject object = new JSONObject(s);

      lat = getKeyValue(LATITUDES_KEYS, object);
      lng = getKeyValue(LONGITUDES_KEYS, object);

    } catch(JSONException e) {
      throw new MagmaRuntimeException("Not a coordinate point");
    }
    return new double[] { lng, lat };
  }

  private static double getKeyValue(String[] keyList, JSONObject object) {
    double value = 0;

    for(String key : keyList) {
      value = object.optDouble(key);
      if(!isNaN(value)) {
        break;
      }
    }
    if(isNaN(value)) {
      throw new MagmaRuntimeException("The latitude or the longitude is not defined");
    }
    return value;
  }
}
