package org.obiba.magma;

import org.junit.Test;
import org.obiba.magma.test.AbstractMagmaTest;
import org.obiba.magma.type.IntegerType;

public class VariableTest extends AbstractMagmaTest {

  @Test(expected = IllegalArgumentException.class)
  public void testNullName() {
    Variable.Builder.newVariable(null, IntegerType.get(), "entityType");
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNullValueType() {
    Variable.Builder.newVariable("Name", null, "entityType");
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNullEntityType() {
    Variable.Builder.newVariable("Name", IntegerType.get(), null);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testIllegalVariableName() {
    Variable.Builder.newVariable("Name:WithColon", IntegerType.get(), "entityType");
  }

  @Test(expected = IllegalArgumentException.class)
  public void testEmptyVariableName() {
    Variable.Builder.newVariable("", IntegerType.get(), "entityType");
  }
}
