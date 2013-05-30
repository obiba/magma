package org.obiba.magma.type;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.junit.Test;
import org.obiba.magma.Coordinate;
import org.obiba.magma.MagmaRuntimeException;
import org.obiba.magma.ValueType;

import com.google.common.collect.ImmutableList;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

@SuppressWarnings({ "unchecked", "ConstantConditions" })
public class LineTypeTest extends BaseValueTypeTest {
  @Override
  ValueType getValueType() {
    return LineType.get();
  }

  @Override
  Object getObjectForType() {
    Collection<Coordinate> line = new ArrayList<Coordinate>(3);
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
    line = new ArrayList<Coordinate>();
    return ImmutableList.<Class<?>>of(line.getClass());
  }

  @Test
  public void testValidLine() {
    List<Coordinate> result = (List<Coordinate>) getValueType()
        .valueOf("[[100.0, 0.0], [101.0, 0.0], [101.0, 1.0], [100.0, 1.0], [100.0, 0.0]]").getValue();
    assertThat(result.size(), is(5));
    assertThat(result.get(0).getLatitude(), is(0.0));
    assertThat(result.get(0).getLongitude(), is(100.0));
  }

  @Test
  public void testLine2() {
    List<Coordinate> result = (List<Coordinate>) getValueType().valueOf(
        "[{\"lat\" : 41.12,\"lon\" : -72.34}, {\"lat\" : 41.12,\"lon\" :" +
            " -71.34}, {\"lat\" : 41.12,\"lon\" : -70.34}]").getValue();
    assertThat(result.size(), is(3));
    assertThat(result.get(0).getLatitude(), is(41.12));
    assertThat(result.get(0).getLongitude(), is(-72.34));
  }

  @Test(expected = MagmaRuntimeException.class)
  public void testLineMalformedPoint() {
    getValueType().valueOf("[[[0,0],[0,e1],[1,0],[1,1],[0,0]]]").getValue();
  }

  @Test
  public void testAssertObject() {
    Collection<Object> c = new ArrayList<Object>();
    c.add(new Coordinate(0.0, 0.1));
    c.add(new Coordinate(1.0, 1.1));
    assertThat(((List<Coordinate>) getValueType().valueOf(c).getValue()).get(0).getLongitude(), is(0.0));
  }

  @Test(expected = MagmaRuntimeException.class)
  public void testInvalidObject() {
    Collection<Object> c = new ArrayList<Object>();
    c.add("expected ");
    c.add("MagmaRuntime");
    c.add("Exception");
    c.add(new Coordinate(1.0, 1.1));
    assertThat(((List<Coordinate>) getValueType().valueOf(c).getValue()).get(0).getLongitude(), is(0.0));
  }

  @Test(expected = MagmaRuntimeException.class)
  public void testNullObject() {
    Collection<Object> c = new ArrayList<Object>();
    getValueType().valueOf(c).getValue();
  }
}
