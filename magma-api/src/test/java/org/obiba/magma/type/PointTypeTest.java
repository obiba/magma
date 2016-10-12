/*
 * Copyright (c) 2016 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.magma.type;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Test;
import org.obiba.magma.Coordinate;
import org.obiba.magma.MagmaRuntimeException;
import org.obiba.magma.ValueType;

import com.google.common.collect.ImmutableList;

import static org.fest.assertions.api.Assertions.assertThat;

@SuppressWarnings("ConstantConditions")
public class PointTypeTest extends BaseValueTypeTest {

  @Override
  ValueType getValueType() {
    return PointType.get();
  }

  @Override
  Object getObjectForType() {
    return new Coordinate(42, 34);
  }

  @Override
  boolean isDateTime() {
    return false;
  }

  @Override
  boolean isNumeric() {
    return false;
  }

  @Override
  Iterable<Class<?>> validClasses() {
    return ImmutableList.<Class<?>>of(Coordinate.class);
  }

  @Test
  public void testParseGoogleMapCoordinates() {
    Coordinate result = (Coordinate) getValueType().valueOf("41.12,-71.34").getValue();
    assertThat(result.getLatitude()).isEqualTo(41.12);
    assertThat(result.getLongitude()).isEqualTo(-71.34);
  }

  @Test
  public void testParseGoogleMapCoordinates2() {
    Coordinate result = (Coordinate) getValueType().valueOf(" 41.12 , -71.34 ").getValue();
    assertThat(result.getLatitude()).isEqualTo(41.12);
    assertThat(result.getLongitude()).isEqualTo(-71.34);
  }

  @Test
  public void testParseGeoJSONCoordinates() {
    Coordinate result = (Coordinate) getValueType().valueOf("[-71.34,41.12]").getValue();
    assertThat(result.getLatitude()).isEqualTo(41.12);
    assertThat(result.getLongitude()).isEqualTo(-71.34);
  }

  @Test
  public void testJSONCoordinates() {
    Coordinate result1 = (Coordinate) getValueType().valueOf("{\"lat\" : 41.12,\"lon\" : -71.34 }").getValue();
    assertThat(result1.getLatitude()).isEqualTo(41.12);
    assertThat(result1.getLongitude()).isEqualTo(-71.34);
  }

  @Test
  public void testJSONCoordinates2() {
    Coordinate result2 = (Coordinate) getValueType().valueOf("{\"latitude\" : 41.12,\"longitude\" : -71.34 }")
        .getValue();
    assertThat(result2.getLatitude()).isEqualTo(41.12);
    assertThat(result2.getLongitude()).isEqualTo(-71.34);
  }

  @Test
  public void testJSONCoordinates3() {
    Coordinate result3 = (Coordinate) getValueType().valueOf("{\"lt\" : 41.12,\"lg\" : -71.34 }").getValue();
    assertThat(result3.getLatitude()).isEqualTo(41.12);
    assertThat(result3.getLongitude()).isEqualTo(-71.34);
  }

  @Test
  public void testJSONCoordinates4() {
    Coordinate result4 = (Coordinate) getValueType().valueOf("{\"lat\" : 41.12,\"lng\" : -71.34 }").getValue();
    assertThat(result4.getLatitude()).isEqualTo(41.12);
    assertThat(result4.getLongitude()).isEqualTo(-71.34);

  }

  @Test
  public void testValueOfCoordinateInstance() {
    Coordinate coordinate = new Coordinate(-71.34, 41.12);
    Coordinate result = (Coordinate) getValueType().valueOf(coordinate).getValue();
    assertThat(result.getLatitude()).isEqualTo(41.12);
    assertThat(result.getLongitude()).isEqualTo(-71.34);
  }

  @Test(expected = MagmaRuntimeException.class)
  public void testValueOfInvalidCoordinate() {
    Object o = DateType.get();
    getValueType().valueOf(o).getValue();
  }

  @Test(expected = MagmaRuntimeException.class)
  public void testParseGeoJSONCoordinatesMissingLatitude() {
    getValueType().valueOf("[-71.34]").getValue();
  }

  @Test(expected = MagmaRuntimeException.class)
  public void testParseGeoJSONCoordinatesMalformed() {
    getValueType().valueOf("[-71.34,-71").getValue();
  }

  //this kind of points is accepted by JSON
  @Test//(expected = MagmaRuntimeException.class)
  public void testParseGeoJSONCoordinatesMalformed2() {
    getValueType().valueOf("71.31 ,21]").getValue();
  }

  @Test(expected = MagmaRuntimeException.class)
  public void testParseJSONCoordinatesMalformed1() {
    getValueType().valueOf("{\"lat\" : 41.u12,\"lng\" : -71.34 }").getValue();
  }

  @Test(expected = MagmaRuntimeException.class)
  public void testParseJSONCoordinatesMalformed2() {
    getValueType().valueOf("{\"lat\" : ,\"lng\" : -71.34 }").getValue();
  }

  @Test(expected = MagmaRuntimeException.class)
  public void testParseJSONCoordinatesMalformed3() {
    getValueType().valueOf("{\"lat\" : 41.12,\"lion\" : -71.34 }").getValue();
  }

  @Test
  public void testJSONArray() throws JSONException {
    JSONArray array = new JSONArray("[-71.34,41.12]");
    Coordinate result = (Coordinate) getValueType().valueOf(array).getValue();
    assertThat(result.getLatitude()).isEqualTo(41.12);
    assertThat(result.getLongitude()).isEqualTo(-71.34);
  }

  @Test
  public void testJSONObject() throws JSONException {
    JSONObject o = new JSONObject("{\"lat\" : 41.12,\"lon\" : -71.34 }");
    Coordinate result = (Coordinate) getValueType().valueOf(o).getValue();
    assertThat(result.getLatitude()).isEqualTo(41.12);
    assertThat(result.getLongitude()).isEqualTo(-71.34);
  }

}
