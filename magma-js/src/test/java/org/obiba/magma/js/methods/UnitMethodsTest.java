/*
 * Copyright (c) 2019 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.magma.js.methods;

import java.util.ResourceBundle;

import org.jscience.physics.unit.PhysicsUnit;
import org.junit.Test;
import org.obiba.magma.js.AbstractJsTest;
import org.obiba.magma.js.ScriptableValue;
import org.obiba.magma.type.DecimalType;
import org.obiba.magma.type.IntegerType;
import org.obiba.magma.type.TextType;

import static org.fest.assertions.api.Assertions.assertThat;

public class UnitMethodsTest extends AbstractJsTest {

  @Test
  public void test_unit_assignsUnitWithOneArgument() {
    ScriptableValue value = evaluate("unit('cm')", IntegerType.get().valueOf(150));
    assertThat(value.getUnit()).isEqualTo("cm");
  }

  @Test
  public void test_unit_acceptsScriptableValue() {
    ScriptableValue value = evaluate("unit(newValue('cm'))", IntegerType.get().valueOf(150));
    assertThat(value.getUnit()).isEqualTo("cm");
  }

  @Test
  public void test_unit_returnsUnitWhenNoArgument() {
    ScriptableValue value = evaluate("unit()", IntegerType.get().valueOf(150), "cm");
    assertThat(value.getUnit()).isNull();
    assertThat(value.getValue()).isEqualTo(TextType.get().valueOf("cm"));
  }

  @Test
  public void test_unit_returnsNullValueWhenNoUnitIsDefined() {
    ScriptableValue value = evaluate("unit()", IntegerType.get().valueOf(150));
    assertThat(value.getValue().isNull()).isTrue();
  }

  @Test
  public void test_toUnit_convertsUnits() {
    ScriptableValue value = evaluate("toUnit('cm')", IntegerType.get().valueOf(1), "in");
    assertThat(value.getUnit()).isEqualTo("cm");
    assertThat(value.getValue()).isEqualTo(DecimalType.get().valueOf(2.54));
  }

  @Test
  public void test_conflicting_units() {
    ResourceBundle units = ResourceBundle.getBundle(UnitMethods.class.getName() + "_CS");
    for(String key : units.keySet()) {
      String unit = units.getString(key);
      try {
        PhysicsUnit<?> conflict = PhysicsUnit.valueOf(unit);
        assertThat(conflict.isSystemUnit()).isFalse()
            .overridingErrorMessage("Unit " + unit + " is conflicting with a system unit.");
      } catch(IllegalArgumentException e) {
        // normal
      }
    }
  }

  @Test
  public void test_toUnit_acceptsNull() {
    ScriptableValue value = evaluate("toUnit('s')", IntegerType.get().nullValue(), "ms");
    assertThat(value.getUnit()).isEqualTo("s");
    assertThat(value.getValue()).isEqualTo(IntegerType.get().nullValue());
  }
}
