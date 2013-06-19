/*******************************************************************************
 * Copyright (c) 2013 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.magma.js.methods;

import java.util.ArrayList;
import java.util.Collection;

import org.junit.Test;
import org.mozilla.javascript.Context;
import org.obiba.magma.Coordinate;
import org.obiba.magma.MagmaRuntimeException;
import org.obiba.magma.Value;
import org.obiba.magma.ValueType;
import org.obiba.magma.js.AbstractJsTest;
import org.obiba.magma.js.ScriptableValue;
import org.obiba.magma.type.DecimalType;
import org.obiba.magma.type.IntegerType;
import org.obiba.magma.type.PointType;
import org.obiba.magma.type.PolygonType;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class GeoMethodsTest extends AbstractJsTest {

  @Test
  public void testLongitude() {
    Value value = PointType.get().valueOf(new Coordinate(-112.6185, 49.7167));
    ScriptableValue scriptableValue = newValue(value);

    ScriptableValue longitude = GeoMethods
        .longitude(Context.getCurrentContext(), scriptableValue, new Object[] { }, null);
    assertThat(longitude.getValue().isNull(), is(false));
    assertThat((DecimalType) longitude.getValue().getValueType(), is(DecimalType.get()));
    assertThat((Double) longitude.getValue().getValue(), is(-112.6185));
  }

  @Test
  public void testLatitude() {
    Value value = PointType.get().valueOf(new Coordinate(-112.6185, 49.7167));
    ScriptableValue scriptableValue = newValue(value);

    ScriptableValue latitude = GeoMethods
        .latitude(Context.getCurrentContext(), scriptableValue, new Object[] { }, null);
    assertThat(latitude.getValue().isNull(), is(false));
    assertThat((DecimalType) latitude.getValue().getValueType(), is(DecimalType.get()));
    assertThat((Double) latitude.getValue().getValue(), is(49.7167));
  }

  @Test(expected = MagmaRuntimeException.class)
  public void testStringLongitude() {
    ScriptableValue scriptableValue = newValue(ValueType.Factory.newValue("test"));
    ScriptableValue longitude = GeoMethods
        .longitude(Context.getCurrentContext(), scriptableValue, new Object[] { }, null);
  }

  @Test
  public void testPointSequenceLongitude() {
    Collection<Value> values = new ArrayList<Value>();
    for(int i = 0; i < 12; i++) {
      Coordinate coordinate = new Coordinate(10.25, i);
      values.add(PointType.get().valueOf(coordinate));
    }
    ScriptableValue result = evaluate("longitude()", DecimalType.get().sequenceOf(values));
    assertThat(result.getValue().isSequence(), is(true));
  }

}
