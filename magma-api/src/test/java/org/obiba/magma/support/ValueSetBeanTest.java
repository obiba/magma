package org.obiba.magma.support;

import javax.annotation.Nonnull;

import org.junit.Before;
import org.junit.Test;
import org.obiba.magma.Timestamps;
import org.obiba.magma.ValueSet;
import org.obiba.magma.ValueTable;
import org.obiba.magma.VariableEntity;

import static org.easymock.EasyMock.createMock;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class ValueSetBeanTest {

  private static final VariableEntity ENTITY = new VariableEntityBean("type", "1234");

  private ValueTable mockTable;

  @Before
  public void setup() {
    mockTable = createMock(ValueTable.class);
  }

  @SuppressWarnings("ConstantConditions")
  @edu.umd.cs.findbugs.annotations.SuppressWarnings("NP_NONNULL_PARAM_VIOLATION")
  @Test(expected = IllegalArgumentException.class)
  public void test_ctor_preconditions_1() {
    new ValueSetBean(null, ENTITY);
  }

  @SuppressWarnings("ConstantConditions")
  @edu.umd.cs.findbugs.annotations.SuppressWarnings("NP_NONNULL_PARAM_VIOLATION")
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
        return ENTITY;
      }

      @SuppressWarnings("ConstantConditions")
      @edu.umd.cs.findbugs.annotations.SuppressWarnings("NP_NONNULL_PARAM_VIOLATION")
      @Nonnull
      @Override
      public Timestamps getTimestamps() {
        return null;
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

      @SuppressWarnings("ConstantConditions")
      @edu.umd.cs.findbugs.annotations.SuppressWarnings("NP_NONNULL_PARAM_VIOLATION")
      @Nonnull
      @Override
      public Timestamps getTimestamps() {
        return null;
      }
    });
  }

  @Test
  public void test_getters() {
    ValueSet vsb = new ValueSetBean(mockTable, ENTITY);
    assertThat(vsb.getValueTable(), is(mockTable));
    assertThat(vsb.getVariableEntity(), is(ENTITY));
  }

  @Test
  public void test_toString_containsTableAndEntity() {
    ValueSetBean vsb = new ValueSetBean(mockTable, ENTITY);
    String str = vsb.toString();
    // We can't mock toString()
    assertThat(str.contains("EasyMock"), is(true));
    assertThat(str.contains(ENTITY.toString()), is(true));
  }
}
