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
import org.obiba.magma.type.PointType;

import static org.fest.assertions.api.Assertions.assertThat;

public class GeoMethodsTest extends AbstractJsTest {

  @Test
  public void testLongitude() {
    Value value = PointType.get().valueOf(new Coordinate(-112.6185, 49.7167));
    ScriptableValue scriptableValue = newValue(value);

    ScriptableValue longitude = GeoMethods
        .longitude(scriptableValue, new Object[] { });
    assertThat(longitude.getValue().isNull()).isFalse();
    assertThat((DecimalType) longitude.getValue().getValueType()).isEqualTo(DecimalType.get());
    assertThat((Double) longitude.getValue().getValue()).isEqualTo(-112.6185);
  }

  @Test
  public void testLatitude() {
    Value value = PointType.get().valueOf(new Coordinate(-112.6185, 49.7167));
    ScriptableValue scriptableValue = newValue(value);

    ScriptableValue latitude = GeoMethods
        .latitude(scriptableValue, new Object[] { });
    assertThat(latitude.getValue().isNull()).isFalse();
    assertThat((DecimalType) latitude.getValue().getValueType()).isEqualTo(DecimalType.get());
    assertThat((Double) latitude.getValue().getValue()).isEqualTo(49.7167);
  }

  @Test(expected = MagmaRuntimeException.class)
  public void testStringLongitude() {
    ScriptableValue scriptableValue = newValue(ValueType.Factory.newValue("test"));
    GeoMethods.longitude(scriptableValue, new Object[] { });
  }

  @Test
  public void testPointSequenceLongitude() {
    Collection<Value> values = new ArrayList<>();
    for(int i = 0; i < 12; i++) {
      Coordinate coordinate = new Coordinate(10.25, i);
      values.add(PointType.get().valueOf(coordinate));
    }
    ScriptableValue result = evaluate("longitude()", DecimalType.get().sequenceOf(values));
    assertThat(result.getValue().isSequence()).isTrue();
  }

}
