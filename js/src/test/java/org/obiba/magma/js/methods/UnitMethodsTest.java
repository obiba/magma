package org.obiba.magma.js.methods;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;

import org.junit.Test;
import org.obiba.magma.js.AbstractJsTest;
import org.obiba.magma.js.ScriptableValue;
import org.obiba.magma.type.DecimalType;
import org.obiba.magma.type.IntegerType;
import org.obiba.magma.type.TextType;

public class UnitMethodsTest extends AbstractJsTest {

  @Test
  public void test_unit_assignsUnitWithOneArgument() {
    ScriptableValue value = evaluate("unit('cm')", IntegerType.get().valueOf(150));
    assertThat(value.getUnit(), is("cm"));
  }

  @Test
  public void test_unit_acceptsScriptableValue() {
    ScriptableValue value = evaluate("unit(newValue('cm'))", IntegerType.get().valueOf(150));
    assertThat(value.getUnit(), is("cm"));
  }

  @Test
  public void test_unit_returnsUnitWhenNoArgument() {
    ScriptableValue value = evaluate("unit()", IntegerType.get().valueOf(150), "cm");
    assertThat(value.getUnit(), nullValue());
    assertThat(value.getValue(), is(TextType.get().valueOf("cm")));
  }

  @Test
  public void test_unit_returnsNullValueWhenNoUnitIsDefined() {
    ScriptableValue value = evaluate("unit()", IntegerType.get().valueOf(150));
    assertThat(value.getValue().isNull(), is(true));
  }

  @Test
  public void test_toUnit_convertsUnits() {
    ScriptableValue value = evaluate("toUnit('cm')", IntegerType.get().valueOf(1), "in");
    assertThat(value.getUnit(), is("cm"));
    assertThat(value.getValue(), is(DecimalType.get().valueOf(2.54)));
  }
}
