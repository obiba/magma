package org.obiba.magma.support;

import static org.easymock.EasyMock.createMock;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import org.junit.Before;
import org.junit.Test;
import org.obiba.magma.ValueSet;
import org.obiba.magma.ValueTable;
import org.obiba.magma.VariableEntity;

public class ValueSetBeanTest {

  private final VariableEntity entity = new VariableEntityBean("type", "1234");

  private ValueTable mockTable;

  @Before
  public void setup() {
    mockTable = createMock(ValueTable.class);
  }

  @Test(expected = IllegalArgumentException.class)
  public void test_ctor_preconditions_1() {
    new ValueSetBean(null, entity);
  }

  @Test(expected = IllegalArgumentException.class)
  public void test_ctor_preconditions_2() {
    new ValueSetBean(mockTable, null);
  }

  @Test(expected = IllegalArgumentException.class)
  public void test_ctor_preconditions_3() {
    new ValueSetBean(new ValueSet() {

      @Override
      public ValueTable getValueTable() {
        return null;
      }

      @Override
      public VariableEntity getVariableEntity() {
        return entity;
      }

    });
  }

  @Test(expected = IllegalArgumentException.class)
  public void test_ctor_preconditions_4() {
    new ValueSetBean(new ValueSet() {

      @Override
      public ValueTable getValueTable() {
        return mockTable;
      }

      @Override
      public VariableEntity getVariableEntity() {
        return null;
      }

    });
  }

  @Test
  public void test_getters() {
    ValueSetBean vsb = new ValueSetBean(mockTable, entity);
    assertThat(vsb.getValueTable(), is(mockTable));
    assertThat(vsb.getVariableEntity(), is(entity));
  }

  @Test
  public void test_toString_containsTableAndEntity() {
    ValueSetBean vsb = new ValueSetBean(mockTable, entity);
    String str = vsb.toString();
    // We can't mock toString()
    assertThat(str.contains("EasyMock"), is(true));
    assertThat(str.contains(entity.toString()), is(true));
  }
}
