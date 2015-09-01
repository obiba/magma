/*
 * Copyright (c) 2013 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.magma.type;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.junit.Test;
import org.obiba.magma.Coordinate;
import org.obiba.magma.MagmaRuntimeException;
import org.obiba.magma.ValueType;

import com.google.common.collect.ImmutableList;

import static org.fest.assertions.api.Assertions.assertThat;

@SuppressWarnings({ "unchecked", "ConstantConditions" })
public class PolygonTypeTest extends BaseValueTypeTest {

  @Override
  ValueType getValueType() {
    return PolygonType.get();
  }

  @Override
  Object getObjectForType() {
    Collection<List<Coordinate>> polygon = new ArrayList<>(1);
    List<Coordinate> points = new ArrayList<>(4);
    points.add(new Coordinate(0, 0));
    points.add(new Coordinate(0, 1));
    points.add(new Coordinate(1, 0));
    points.add(new Coordinate(1, 1));
    points.add(new Coordinate(0, 0));
    polygon.add(points);
    return polygon;
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
    List<List<Coordinate>> polygon;
    polygon = new ArrayList<>();
    return ImmutableList.<Class<?>>of(polygon.getClass());
  }

  @Test
  public void testParseOneShapePolygonGeoJSONPoints() {
    List<List<Coordinate>> result = (List<List<Coordinate>>) getValueType()
        .valueOf("[[[100.0, 0.0], [101.0, 0.0], [101.0, 1.0], [100.0, 1.0], [100.0, 0.0]  ]]").getValue();
    assertThat(result).hasSize(1);
    assertThat(result.get(0)).hasSize(5);
  }

  @Test
  public void testParseTwoShapePolygonGeoJSONPoints() {
    List<List<Coordinate>> result = (List<List<Coordinate>>) getValueType()
        .valueOf("[[ {\"lat\" : 100.0,\"lon\" : 0.0 } , {\"lat\" : 101.0,\"lon\" : 0.0 }, " +
            "{\"lat\" : 101.0,\"lon\" : 1.0 }, {\"lat\" : 100.0,\"lon\" : 1.0 }, {\"lat\" : 100.0,\"lon\" : 0.0 } ]," +
            " [ {\"lat\" : 100.2,\"lon\" : 0.2 }, {\"lat\" : 100.8,\"lon\" : 0.2 }, {\"lat\" : 100.8,\"lon\" : 0.8 }," +
            " {\"lat\" : 100.2,\"lon\" : 0.8 }, {\"lat\" : 100.2,\"lon\" : 0.2 } ]]").getValue();
    assertThat(result).hasSize(2);
    assertThat(result.get(0)).hasSize(5);
    assertThat(result.get(0)).hasSize(5);
  }

  @Test
  public void testParseOneShapePolygonJSONPoints() {
    List<List<Coordinate>> result = (List<List<Coordinate>>) getValueType()
        .valueOf("[[[100.0, 0.0], [101.0, 0.0], [101.0, 1.0], [100.0, 1.0], [100.0, 0.0]  ]]").getValue();
    assertThat(result).hasSize(1);
    assertThat(result.get(0)).hasSize(5);
  }

  @Test
  public void testAssertObjectOfPolygon() {
    List<List<Coordinate>> polygon = new ArrayList<>(1);
    List<Coordinate> points = new ArrayList<>(4);
    points.add(new Coordinate(100.0, 0.0));
    points.add(new Coordinate(101.0, 0.0));
    points.add(new Coordinate(101.0, 1.0));
    points.add(new Coordinate(100.0, 1.0));
    points.add(new Coordinate(100.0, 0.0));
    polygon.add(points);

    List<List<Coordinate>> result = (List<List<Coordinate>>) getValueType().valueOf(polygon).getValue();

    assertThat(polygon).hasSize(1);
    assertThat(polygon.get(0)).hasSize(5);
    assertThat(result.get(0).get(0).getLongitude()).isEqualTo(100.0);
    assertThat(result.get(0).get(1).getLongitude()).isEqualTo(101.0);
    assertThat(result.get(0).get(2).getLongitude()).isEqualTo(101.0);
  }

  @Test
  public void testAssertObjectOfPolygon1() {
    List<ArrayList<Coordinate>> polygon = new ArrayList<>(1);
    ArrayList<Coordinate> points = new ArrayList<>(4);
    points.add(new Coordinate(100.0, 0.0));
    points.add(new Coordinate(101.0, 0.0));
    points.add(new Coordinate(101.0, 1.0));
    points.add(new Coordinate(100.0, 1.0));
    points.add(new Coordinate(100.0, 0.0));
    polygon.add(points);

    List<List<Coordinate>> result = (List<List<Coordinate>>) getValueType().valueOf(polygon).getValue();

    assertThat(polygon).hasSize(1);
    assertThat(polygon.get(0)).hasSize(5);
    assertThat(result.get(0).get(0).getLongitude()).isEqualTo(100.0);
    assertThat(result.get(0).get(1).getLongitude()).isEqualTo(101.0);
    assertThat(result.get(0).get(2).getLongitude()).isEqualTo(101.0);
    assertThat(result.get(0).get(1).getLatitude()).isEqualTo(0.0);
    assertThat(result.get(0).get(2).getLatitude()).isEqualTo(1.0);
  }

  @Test
  public void testAssertObjectOfPolygon2() {
    Collection<List<String>> polygon = new ArrayList<>(1);

    List<String> points = new ArrayList<>(4);
    points.add("[100.0,0.0]");
    points.add("[101.0,0.0]");
    points.add("[101.0,1.0]");
    points.add("[100.0,1.0]");
    points.add("[100.0,0.0]");
    polygon.add(points);

    List<List<Coordinate>> result = (List<List<Coordinate>>) getValueType().valueOf(polygon).getValue();

    assertThat(result).hasSize(1);
    assertThat(result.get(0)).hasSize(5);
    assertThat(result.get(0).get(0).getLongitude()).isEqualTo(100.0);
  }

  @Test
  public void testAssertObjectOfPolygon3() {
    String s = "[[ {\"lat\" : 100.0,\"lon\" : 0.0 } , {\"lat\" : 101.0,\"lon\" : 0.0 }, " +
        "{\"lat\" : 101.0,\"lon\" : 1.0 }, {\"lat\" : 100.0,\"lon\" : 1.0 }, {\"lat\" : 100.0,\"lon\" : 0.0 } ]," +
        " [ {\"lat\" : 100.2,\"lon\" : 0.2 }, {\"lat\" : 100.8,\"lon\" : 0.2 }, {\"lat\" : 100.8,\"lon\" : 0.8 }," +
        " {\"lat\" : 100.2,\"lon\" : 0.8 }, {\"lat\" : 100.2,\"lon\" : 0.2 } ]]";

    List<List<Coordinate>> result = (List<List<Coordinate>>) getValueType().valueOf(s).getValue();

    assert result != null;
    assertThat(result).hasSize(2);
    assertThat(result.get(0)).hasSize(5);
    assertThat(result.get(1)).hasSize(5);
    assertThat(result.get(0).get(0).getLatitude()).isEqualTo(100.0);
  }

  @Test(expected = MagmaRuntimeException.class)
  public void testAssertObjectPolygonMalformed() {
    String s = "";
    getValueType().valueOf(s).getValue();
  }

  @Test(expected = MagmaRuntimeException.class)
  public void testAssertObjectNullPointerException() {
    ArrayList<Object> c = new ArrayList<>();
    getValueType().valueOf(c).getValue();
  }

  @Test(expected = MagmaRuntimeException.class)
  public void testAssertObjectNullPointerException2() {
    List<List<Object>> c = new ArrayList<>();
    getValueType().valueOf(c).getValue();
  }

  @Test(expected = MagmaRuntimeException.class)
  public void testAssertObjectPolygonMalformed3() {
    Collection<Object> c = new ArrayList<>();
    Coordinate coordinate = new Coordinate(0.0, 0.1);
    c.add(coordinate);
    getValueType().valueOf(c).getValue();
  }

  @Test(expected = MagmaRuntimeException.class)
  public void testNullPolygon() {
    assertThat(getValueType().valueOf("[[]]").isNull()).isTrue();
  }

  @Test(expected = MagmaRuntimeException.class)
  public void testNullPolygon2() {
    List<List<Coordinate>> result = new ArrayList<>();
    assertThat(getValueType().valueOf(result).isNull()).isTrue();
  }

  @Test(expected = MagmaRuntimeException.class)
  public void testParsePolygonGoogleMapPoints() {
    getValueType().valueOf("[[100.0, 0.0, 101.0, 0.0, 101.0, 1.0, 100.0, 1.0, 100.0, 0.0  ]]").getValue();
  }

  @Test(expected = MagmaRuntimeException.class)
  public void testParseDifferentFirstLastPoint() {
    getValueType().valueOf("[[[0,0],[0,1],[1,0],[1,1],[8,0]]]").getValue();
  }

  @Test(expected = MagmaRuntimeException.class)
  public void testPolygonMalformedPoint() {
    getValueType().valueOf("[[[0,0],[0,e1],[1,0],[1,1],[0,0]]]").getValue();
  }

  @Test
  public void testCompareIdenticalPolygons() {
    String s1 = "[[[100.0, 0.0], [101.0, 0.0], [101.0, 1.0], [100.0, 1.0], [100.0, 0.0]  ]]";
    String s2 = "[[[100.0, 0.0], [101.0, 0.0], [101.0, 1.0], [100.0, 1.0], [100.0, 0.0]  ]]";
    assertThat(getValueType().compare(getValueType().valueOf(s1), getValueType().valueOf(s2))).isEqualTo(0);
  }

  @Test
  public void testCompareInvertedShapesPolygons() {
    String s1 = "[ [[100.0, 0.0], [101.0, 0.0], [101.0, 1.0], [100.0, 1.0], [100.0, 0.0]]," +
        "[[0.0,0.0] , [0.1,0.0] , [0.1,0.1] , [0.0,0.1] , [0.0,0.0] ]]";
    String s2 = "[[[0.0,0.0] , [0.1,0.0] , [0.1,0.1] , [0.0,0.1] , [0.0,0.0]] , " +
        "[[100.0, 0.0], [101.0, 0.0], [101.0, 1.0], [100.0, 1.0], [100.0, 0.0]]]";

    assertThat(getValueType().compare(getValueType().valueOf(s1), getValueType().valueOf(s2))).isEqualTo(0);
  }

  @Test
  public void testComparePolygons() {
    String s1 = "[[[100.0, 0.0], [101.0, 0.0], [101.0, 1.0], [100.0, 1.0], [100.0, 0.0]  ]]";
    String s2 = "[[[100.0, 0.0], [101.0, 1.0], [101.0, 0.0], [100.0, 1.0], [100.0, 0.0]  ]]";
    assertThat(getValueType().compare(getValueType().valueOf(s1), getValueType().valueOf(s2))).isEqualTo(-1);
  }

  @Test
  public void testComparePolygons3() {
    String s1 = "[ [[100.0, 0.0], [101.0, 0.0], [101.0, 1.0], [100.0, 1.0], [100.0, 0.0]]," +
        "[[0.0,0.0] , [0.1,0.0] , [0.1,0.1] , [0.0,0.1] , [0.0,0.0] ]]";
    String s2 = "[[[100.0, 0.0], [101.0, 1.0], [101.0, 0.0], [100.0, 1.0], [100.0, 0.0]]]";

    assertThat(getValueType().compare(getValueType().valueOf(s1), getValueType().valueOf(s2))).isEqualTo(1);
  }
}
