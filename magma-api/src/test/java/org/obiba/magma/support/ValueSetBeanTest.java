/*
 * Copyright (c) 2019 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.magma.support;

import javax.validation.constraints.NotNull;

import org.junit.Before;
import org.junit.Test;
import org.obiba.magma.Timestamps;
import org.obiba.magma.ValueSet;
import org.obiba.magma.ValueTable;
import org.obiba.magma.VariableEntity;

import static org.easymock.EasyMock.createMock;
import static org.fest.assertions.api.Assertions.assertThat;

public class ValueSetBeanTest {

  private static final VariableEntity ENTITY = new VariableEntityBean("type", "1234");

  private ValueTable mockTable;

  @Before
  public void setup() {
    mockTable = createMock(ValueTable.class);
  }

  @SuppressWarnings("ConstantConditions")
  @Test(expected = IllegalArgumentException.class)
  public void test_ctor_preconditions_1() {
    new ValueSetBean(null, ENTITY);
  }

  @SuppressWarnings("ConstantConditions")
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

      @NotNull
      @Override
      public Timestamps getTimestamps() {
        return createMock(Timestamps.class);
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

      @NotNull
      @Override
      public Timestamps getTimestamps() {
        return createMock(Timestamps.class);
      }
    });
  }

  @Test
  public void test_getters() {
    ValueSet vsb = new ValueSetBean(mockTable, ENTITY);
    assertThat(vsb.getValueTable()).isEqualTo(mockTable);
    assertThat(vsb.getVariableEntity()).isEqualTo(ENTITY);
  }

  @Test
  public void test_toString_containsTableAndEntity() {
    ValueSetBean vsb = new ValueSetBean(mockTable, ENTITY);
    String str = vsb.toString();
    // We can't mock toString()
    assertThat(str.contains("EasyMock")).isTrue();
    assertThat(str.contains(ENTITY.toString())).isTrue();
  }
}
