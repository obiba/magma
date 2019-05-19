/*
 * Copyright (c) 2019 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.magma.views;

import javax.validation.constraints.NotNull;

import org.easymock.EasyMock;
import org.junit.Test;
import org.obiba.magma.*;
import org.obiba.magma.support.VariableEntityBean;
import org.obiba.magma.type.DateTimeType;

import static org.fest.assertions.api.Assertions.assertThat;

/**
 *
 */
public class IncrementalWhereClauseTest extends MagmaTest {

  private final VariableEntity entity = new VariableEntityBean("P", "123");

  @Test
  public void test_where_includesMoreRecent() {
    assertThat(test("2011-11-25T12:30Z", "2011-11-24T12:30Z")).isTrue();
  }

  @Test
  public void test_where_excludesLessRecent() {
    assertThat(test("2011-11-25T12:30Z", "2011-11-26T12:30Z")).isFalse();
  }

  @Test
  public void test_where_includesNoDestinationValueSet() {
    assertThat(test("2011-11-25T12:30Z", null)).isTrue();
  }

  private boolean test(String source, String other) {
    ValueTable destination = EasyMock.createMock(ValueTable.class);
    EasyMock.expect(destination.hasValueSet(entity)).andReturn(other != null).anyTimes();
    if(other != null) {
      EasyMock.expect(destination.getValueSet(entity)).andReturn(mockValueSet(other)).anyTimes();
    }

    EasyMock.replay(destination);

    WhereClause clause = new IncrementalWhereClause(destination);

    boolean include = clause.where(mockValueSet(source));

    EasyMock.verify(destination);

    return include;

  }

  private ValueSet mockValueSet(String updated) {
    ValueSet valueSet = EasyMock.createMock(ValueSet.class);
    EasyMock.expect(valueSet.getVariableEntity()).andReturn(entity).anyTimes();
    EasyMock.expect(valueSet.getTimestamps()).andReturn(mockTimestamps(updated)).anyTimes();
    EasyMock.replay(valueSet);
    return valueSet;
  }

  private Timestamps mockTimestamps(final String updated) {
    return new Timestamps() {

      @NotNull
      @Override
      public Value getLastUpdate() {
        return DateTimeType.get().valueOf(updated);
      }

      @NotNull
      @Override
      public Value getCreated() {
        return DateTimeType.get().nullValue();
      }

    };
  }
}
