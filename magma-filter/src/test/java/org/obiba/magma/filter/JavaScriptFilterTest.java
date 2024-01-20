/*
 * Copyright (c) 2019 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.magma.filter;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.obiba.magma.MagmaEngine;
import org.obiba.magma.ValueSet;
import org.obiba.magma.ValueTable;
import org.obiba.magma.Variable;
import org.obiba.magma.VariableEntity;
import org.obiba.magma.VariableValueSource;
import org.obiba.magma.js.MagmaJsExtension;
import org.obiba.magma.support.ValueSetBean;
import org.obiba.magma.support.VariableEntityBean;
import org.obiba.magma.type.BooleanType;
import org.obiba.magma.type.TextType;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class JavaScriptFilterTest {

  private ValueSet valueSetMock;

  @Before
  public void setUp() throws Exception {
    new MagmaEngine().extend(new MagmaJsExtension());
    valueSetMock = mock(ValueSet.class);
    when(valueSetMock.getValueTable()).thenReturn(mock(ValueTable.class));
    when(valueSetMock.getVariableEntity()).thenReturn(mock(VariableEntity.class));
  }

  @After
  public void cleanUp() throws Exception {
    MagmaEngine.get().shutdown();
  }

  @SuppressWarnings("ConstantConditions")
  @Test(expected = IllegalArgumentException.class)
  public void testJavaScriptNotAllowed() throws Exception {
    new JavaScriptFilter(null);
  }

  @Test
  public void testAttributeAndValueAllowed() throws Exception {
    new JavaScriptFilter("1;");
  }

  @Test
  public void testSimpleScriptReturnsTrue() throws Exception {
    JavaScriptFilter filter = JavaScriptFilter.Builder.newFilter().javascript("4 > 3;").include().build();
    assertThat(filter.runFilter(valueSetMock)).isTrue();
  }

  @Test
  public void testSimpleScriptReturnsFalse() throws Exception {
    JavaScriptFilter filter = JavaScriptFilter.Builder.newFilter().javascript("2 > 3;").include().build();
    assertThat(filter.runFilter(valueSetMock)).isFalse();
  }

  @Test
  public void testNullReturnValueSameAsFalse() throws Exception {
    JavaScriptFilter filter = JavaScriptFilter.Builder.newFilter().javascript("null;").exclude().build();
    assertThat(filter.runFilter(valueSetMock)).isNull();
  }

  @Test
  public void testScriptAnyReturnsTrue() throws Exception {
    JavaScriptFilter filter = JavaScriptFilter.Builder.newFilter()
        .javascript("$('Admin.Interview.exported').any('TRUE')").include().build();

    Variable variable = Variable.Builder.newVariable("Admin.Interview.exported", BooleanType.get(), "Participant")
        .build();

    VariableValueSource mockSource = mock(VariableValueSource.class);
    when(mockSource.getVariable()).thenReturn(variable);
    when(mockSource.getValue((ValueSet) any())).thenReturn(BooleanType.get().valueOf("TRUE"));

    ValueTable tableMock = mock(ValueTable.class);
    ValueSet valueSet = new ValueSetBean(tableMock, new VariableEntityBean("Participant", "1234"));
    when(tableMock.getName()).thenReturn("collectionName");
    when(tableMock.getVariableValueSource("Admin.Interview.exported")).thenReturn(mockSource);
    when(tableMock.getValueSet((VariableEntity) any())).thenReturn(valueSet);

    assertThat(filter.runFilter(valueSet)).isTrue();
  }

  @Test
  public void testScriptAnyReturnsFalse() throws Exception {
    JavaScriptFilter filter = JavaScriptFilter.Builder.newFilter()
        .javascript("$('Admin.Interview.exported').any('FALSE')").include().build();

    Variable variable = Variable.Builder.newVariable("Admin.Interview.exported", BooleanType.get(), "Participant")
        .build();

    VariableValueSource mockSource = mock(VariableValueSource.class);
    when(mockSource.getVariable()).thenReturn(variable);
    when(mockSource.getValue((ValueSet) any())).thenReturn(BooleanType.get().valueOf("TRUE"));

    ValueTable tableMock = mock(ValueTable.class);
    ValueSet valueSet = new ValueSetBean(tableMock, new VariableEntityBean("Participant", "1234"));
    when(tableMock.getName()).thenReturn("collectionName");
    when(tableMock.getVariableValueSource("Admin.Interview.exported")).thenReturn(mockSource);
    when(tableMock.getValueSet((VariableEntity) any())).thenReturn(valueSet);

    assertThat(filter.runFilter(valueSet)).isFalse();
  }

  @Test
  public void testScriptAnyMultipleReturnsTrue() throws Exception {
    JavaScriptFilter filter = JavaScriptFilter.Builder.newFilter()
        .javascript("$('Participant.Interview.status').any('CANCELED','CLOSED')").include().build();

    Variable variable = Variable.Builder.newVariable("Admin.Interview.exported", BooleanType.get(), "Participant")
        .build();

    VariableValueSource mockSource = mock(VariableValueSource.class);
    when(mockSource.getVariable()).thenReturn(variable);
    when(mockSource.getValue((ValueSet) any())).thenReturn(TextType.get().valueOf("CLOSED"));

    ValueTable tableMock = mock(ValueTable.class);
    ValueSet valueSet = new ValueSetBean(tableMock, new VariableEntityBean("Participant", "1234"));
    when(tableMock.getName()).thenReturn("collectionName");
    when(tableMock.getVariableValueSource("Participant.Interview.status")).thenReturn(mockSource);
    when(tableMock.getValueSet((VariableEntity) any())).thenReturn(valueSet);

    assertThat(filter.runFilter(valueSet)).isTrue();
  }

  @Test
  public void testScriptAnyMultipleReturnsFalse() throws Exception {
    JavaScriptFilter filter = JavaScriptFilter.Builder.newFilter()
        .javascript("$('Participant.Interview.status').any('CANCELED','CLOSED')").include().build();

    Variable variable = Variable.Builder.newVariable("Admin.Interview.exported", BooleanType.get(), "Participant")
        .build();

    VariableValueSource mockSource = mock(VariableValueSource.class);
    when(mockSource.getVariable()).thenReturn(variable);
    when(mockSource.getValue((ValueSet) any())).thenReturn(TextType.get().valueOf("IN_PROGRESS"));

    ValueTable tableMock = mock(ValueTable.class);
    ValueSet valueSet = new ValueSetBean(tableMock, new VariableEntityBean("Participant", "1234"));
    when(tableMock.getName()).thenReturn("collectionName");
    when(tableMock.getVariableValueSource("Participant.Interview.status")).thenReturn(mockSource);
    when(tableMock.getValueSet((VariableEntity) any())).thenReturn(valueSet);

    assertThat(filter.runFilter(valueSet)).isFalse();
  }

  @Test
  public void testScriptNotEqualReturnsTrue() throws Exception {
    JavaScriptFilter filter = JavaScriptFilter.Builder.newFilter()
        .javascript("$('Participant.Interview.status').not('CANCELED')").include().build();

    Variable variable = Variable.Builder.newVariable("Admin.Interview.exported", BooleanType.get(), "Participant")
        .build();

    VariableValueSource mockSource = mock(VariableValueSource.class);
    when(mockSource.getVariable()).thenReturn(variable);
    when(mockSource.getValue((ValueSet) any())).thenReturn(TextType.get().valueOf("IN_PROGRESS"));

    ValueTable tableMock = mock(ValueTable.class);
    ValueSet valueSet = new ValueSetBean(tableMock, new VariableEntityBean("Participant", "1234"));
    when(tableMock.getName()).thenReturn("collectionName");
    when(tableMock.getVariableValueSource("Participant.Interview.status")).thenReturn(mockSource);
    when(tableMock.getValueSet((VariableEntity) any())).thenReturn(valueSet);

    assertThat(filter.runFilter(valueSet)).isTrue();
  }

  @Test
  public void testScriptNotEqualReturnsFalse() throws Exception {
    JavaScriptFilter filter = JavaScriptFilter.Builder.newFilter()
        .javascript("$('Participant.Interview.status').not('IN_PROGRESS')").include().build();

    Variable variable = Variable.Builder.newVariable("Admin.Interview.exported", BooleanType.get(), "Participant")
        .build();

    VariableValueSource mockSource = mock(VariableValueSource.class);
    when(mockSource.getVariable()).thenReturn(variable);
    when(mockSource.getValue((ValueSet) any())).thenReturn(TextType.get().valueOf("IN_PROGRESS"));

    ValueTable tableMock = mock(ValueTable.class);
    ValueSet valueSet = new ValueSetBean(tableMock, new VariableEntityBean("Participant", "1234"));
    when(tableMock.getName()).thenReturn("collectionName");
    when(tableMock.getVariableValueSource("Participant.Interview.status")).thenReturn(mockSource);
    when(tableMock.getValueSet((VariableEntity) any())).thenReturn(valueSet);

    assertThat(filter.runFilter(valueSet)).isFalse();
  }

}
