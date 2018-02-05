/*
 * Copyright (c) 2017 OBiBa. All rights reserved.
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
public class LineStringTypeTest extends BaseValueTypeTest {
  @Override
  ValueType getValueType() {
    return LineStringType.get();
  }

  @Override
  Object getObjectForType() {
    Collection<Coordinate> line = new ArrayList<>(3);
    line.add(new Coordinate(22.2, 44.1));
    line.add(new Coordinate(33.4, 55.3));
    line.add(new Coordinate(32.12, 44));
    return line;
  }

  @Override
  boolean isNumeric() {
    return false;
  }

  @Override
  boolean isDateTime() {
    return false;
  }

  @Override
  Iterable<Class<?>> validClasses() {
    List<Coordinate> line;
    line = new ArrayList<>();
    return ImmutableList.<Class<?>>of(line.getClass());
  }

  @Test
  public void testValidLine() {
    List<Coordinate> result = (List<Coordinate>) getValueType()
        .valueOf("[[100.0, 0.0], [101.0, 0.0], [101.0, 1.0], [100.0, 1.0], [100.0, 0.0]]").getValue();
    assertThat(result).hasSize(5);
    assertThat(result.get(0).getLatitude()).isEqualTo(0.0);
    assertThat(result.get(0).getLongitude()).isEqualTo(100.0);
  }

  @Test
  public void testValidGeoJSONLine() {
    List<Coordinate> result = (List<Coordinate>) getValueType()
      .valueOf("{'type':'LineString','coordinates': [[100.0, 0.0], [101.0, 0.0], [101.0, 1.0], [100.0, 1.0], [100.0, 0.0]]}").getValue();
    assertThat(result).hasSize(5);
    assertThat(result.get(0).getLatitude()).isEqualTo(0.0);
    assertThat(result.get(0).getLongitude()).isEqualTo(100.0);
  }

  @Test
  public void testLine2() {
    List<Coordinate> result = (List<Coordinate>) getValueType().valueOf(
        "[{\"lat\" : 41.12,\"lon\" : -72.34}, {\"lat\" : 41.12,\"lon\" :" +
            " -71.34}, {\"lat\" : 41.12,\"lon\" : -70.34}]").getValue();
    assertThat(result).hasSize(3);
    assertThat(result.get(0).getLatitude()).isEqualTo(41.12);
    assertThat(result.get(0).getLongitude()).isEqualTo(-72.34);
  }

  @Test(expected = MagmaRuntimeException.class)
  public void testLineMalformedPoint() {
    getValueType().valueOf("[[[0,0],[0,e1],[1,0],[1,1],[0,0]]]").getValue();
  }

  @Test
  public void testAssertObject() {
    Collection<Object> c = new ArrayList<>();
    c.add(new Coordinate(0.0, 0.1));
    c.add(new Coordinate(1.0, 1.1));
    assertThat(((List<Coordinate>) getValueType().valueOf(c).getValue()).get(0).getLongitude()).isEqualTo(0.0);
  }

  @Test(expected = MagmaRuntimeException.class)
  public void testInvalidObject() {
    Collection<Object> c = new ArrayList<>();
    c.add("expected ");
    c.add("MagmaRuntime");
    c.add("Exception");
    c.add(new Coordinate(1.0, 1.1));
    assertThat(((List<Coordinate>) getValueType().valueOf(c).getValue()).get(0).getLongitude()).isEqualTo(0.0);
  }

  @Test(expected = MagmaRuntimeException.class)
  public void testNullObject() {
    Collection<Object> c = new ArrayList<>();
    getValueType().valueOf(c).getValue();
  }
}
