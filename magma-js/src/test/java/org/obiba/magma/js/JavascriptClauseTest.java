/*
 * Copyright (c) 2017 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.magma.js;

import org.junit.Test;
import org.obiba.magma.ValueSet;
import org.obiba.magma.ValueTable;
import org.obiba.magma.Variable;
import org.obiba.magma.VariableValueSource;
import org.obiba.magma.js.views.JavascriptClause;
import org.obiba.magma.type.TextType;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class JavascriptClauseTest extends AbstractJsTest {

  @Test
  public void testSelectWithScriptThatEvaluatesToTrue() {
    Variable variableMock = mock(Variable.class);

    when(variableMock.getName()).thenReturn("Participant.DO_YOU_SMOKE");

    JavascriptClause javascriptClause = new JavascriptClause("name().matches(/Participant.*/, /DO_YOU_SMOKE/)");
    javascriptClause.initialise();
    assertThat(javascriptClause.select(variableMock)).isTrue();
  }

  @Test
  public void testSelectWithScriptThatEvaluatesToFalse() {
    Variable variableMock = mock(Variable.class);

    when(variableMock.getName()).thenReturn("Participant.DO_YOU_SMOKE");

    JavascriptClause javascriptClause = new JavascriptClause("name().matches(/SomeUnmatchedPattern/)");
    javascriptClause.initialise();

    assertThat(javascriptClause.select(variableMock)).isFalse();
  }

  @Test
  public void testWhereWithScriptThatEvaluatesToTrue() {
    ValueTable valueTableMock = mock(ValueTable.class);
    ValueSet valueSetMock = mock(ValueSet.class);
    VariableValueSource variableValueSourceMock = mock(VariableValueSource.class);
    Variable variableMock = mock(Variable.class);

    when(valueSetMock.getValueTable()).thenReturn(valueTableMock);
    when(valueTableMock.getVariableValueSource("DO_YOU_SMOKE")).thenReturn(variableValueSourceMock);
    when(variableValueSourceMock.getValue(valueSetMock)).thenReturn(TextType.get().valueOf("Yes"));
    when(variableValueSourceMock.getVariable()).thenReturn(variableMock);
    when(variableMock.getUnit()).thenReturn(null);

    JavascriptClause javascriptClause = new JavascriptClause("$('DO_YOU_SMOKE').any('DNK', 'PNA').not()");
    javascriptClause.initialise();

    assertThat(javascriptClause.where(valueSetMock)).isTrue();

    verify(variableValueSourceMock).getVariable();
    verify(variableMock).getUnit();
  }

  @Test
  public void testWhereWithScriptThatEvaluatesToFalse() {
    ValueTable valueTableMock = mock(ValueTable.class);
    ValueSet valueSetMock = mock(ValueSet.class);
    VariableValueSource variableValueSourceMock = mock(VariableValueSource.class);
    Variable variableMock = mock(Variable.class);

    when(valueSetMock.getValueTable()).thenReturn(valueTableMock);
    when(valueTableMock.getVariableValueSource("DO_YOU_SMOKE")).thenReturn(variableValueSourceMock);
    when(variableValueSourceMock.getValue(valueSetMock)).thenReturn(TextType.get().valueOf("DNK"));
    when(variableValueSourceMock.getVariable()).thenReturn(variableMock);
    when(variableMock.getUnit()).thenReturn(null);

    JavascriptClause javascriptClause = new JavascriptClause("$('DO_YOU_SMOKE').any(true, 'DNK', 'PNA').not()");
    javascriptClause.initialise();

    assertThat(javascriptClause.where(valueSetMock)).isFalse();
    verify(variableValueSourceMock).getVariable();
    verify(variableMock).getUnit();
  }
}
