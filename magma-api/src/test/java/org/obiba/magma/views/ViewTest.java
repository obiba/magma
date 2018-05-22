/*
 * Copyright (c) 2017 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.magma.views;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.fest.util.Sets;
import org.junit.Test;
import org.obiba.magma.*;
import org.obiba.magma.support.NullTimestamps;
import org.obiba.magma.support.ValueSetBean;
import org.obiba.magma.support.VariableEntityBean;
import org.obiba.magma.type.TextType;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.anyObject;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@SuppressWarnings({ "PMD.NcssMethodCount", "OverlyLongMethod", "OverlyCoupledClass" })
public class ViewTest extends MagmaTest {

  @Test
  public void testHasValueSetWithDefaultWhereClause() {
    ValueTable valueTableMock = mock(ValueTable.class);
    VariableEntity variableEntity = new VariableEntityBean("type", "id1");

    when(valueTableMock.hasValueSet(variableEntity)).thenReturn(true);
    when(valueTableMock.getVariableEntities()).thenReturn(Sets.newLinkedHashSet(variableEntity));
    when(valueTableMock.getTimestamps()).thenReturn(NullTimestamps.get());

    View view = View.Builder.newView("view", valueTableMock).build();
    assertThat(view.hasValueSet(variableEntity)).isTrue();
  }

  @Test
  public void testHasValueSetWithIncludingWhereClause() {
    ValueTable valueTableMock = mock(ValueTable.class);
    WhereClause whereClauseMock = mock(WhereClause.class);
    VariableEntity variableEntity = new VariableEntityBean("type", "id1");
    ValueSet valueSet = new ValueSetBean(valueTableMock, variableEntity);

    when(valueTableMock.hasValueSet(variableEntity)).thenReturn(true);
    when(valueTableMock.getValueSet(variableEntity)).thenReturn(valueSet);
    when(valueTableMock.getVariableEntities()).thenReturn(Sets.newLinkedHashSet(variableEntity));
    when(valueTableMock.getTimestamps()).thenReturn(NullTimestamps.get());

    View view = View.Builder.newView("view", valueTableMock).where(whereClauseMock).build();
    when(whereClauseMock.where(valueSet, view)).thenReturn(true);
    assertThat(view.hasValueSet(variableEntity)).isTrue();
  }

  @Test
  public void testHasValueSetWithExcludingWhereClause() {
    ValueTable valueTableMock = mock(ValueTable.class);
    WhereClause whereClauseMock = mock(WhereClause.class);
    VariableEntity variableEntity = new VariableEntityBean("type", "id1");
    ValueSet valueSet = new ValueSetBean(valueTableMock, variableEntity);

    when(valueTableMock.hasValueSet(variableEntity)).thenReturn(true);
    when(valueTableMock.getValueSet(variableEntity)).thenReturn(valueSet);
    when(valueTableMock.getTimestamps()).thenReturn(NullTimestamps.get());
    when(whereClauseMock.where(valueSet)).thenReturn(false);

    View view = View.Builder.newView("view", valueTableMock).where(whereClauseMock).build();
    assertThat(view.hasValueSet(variableEntity)).isFalse();
  }

  @Test
  public void testGetValueSetReturnsValueSetThatRefersToView() {
    ValueTable valueTableMock = mock(ValueTable.class);
    VariableEntity variableEntity = new VariableEntityBean("type", "id1");
    ValueSet valueSet = new ValueSetBean(valueTableMock, variableEntity);

    when(valueTableMock.getName()).thenReturn("wrappedTable");
    when(valueTableMock.getValueSet(variableEntity)).thenReturn(valueSet);

    View view = View.Builder.newView("view", valueTableMock).build();
    ValueSet viewValueSet = view.getValueSet(variableEntity);

    // Verify state.
    assertThat(viewValueSet).isNotNull();
    assertThat(viewValueSet.getValueTable()).isNotNull();
    assertThat(viewValueSet.getValueTable().getName()).isEqualTo("view");
  }

  @Test
  public void testGetValueSetWithDefaultWhereClause() {
    ValueTable valueTableMock = mock(ValueTable.class);
    VariableEntity variableEntity = new VariableEntityBean("type", "id1");
    ValueSet valueSet = new ValueSetBean(valueTableMock, variableEntity);

    when(valueTableMock.getName()).thenReturn("wrappedTable");
    when(valueTableMock.getValueSet(variableEntity)).thenReturn(valueSet);

    View view = View.Builder.newView("view", valueTableMock).build();
    assertThat(view.getValueSet(variableEntity)).isNotNull();
  }

  @Test
  public void testGetValueSetWithIncludingWhereClause() {
    ValueTable valueTableMock = mock(ValueTable.class);
    WhereClause whereClauseMock = mock(WhereClause.class);
    VariableEntity variableEntity = new VariableEntityBean("type", "id1");
    ValueSet valueSet = new ValueSetBean(valueTableMock, variableEntity);

    when(valueTableMock.getValueSet(variableEntity)).thenReturn(valueSet);

    View view = View.Builder.newView("view", valueTableMock).where(whereClauseMock).build();
    when(whereClauseMock.where(valueSet, view)).thenReturn(true);
    assertThat(view.getValueSet(variableEntity)).isNotNull();
  }

  @Test(expected = NoSuchValueSetException.class)
  public void testGetValueSetWithExcludingWhereClause() {
    ValueTable valueTableMock = mock(ValueTable.class);
    WhereClause whereClauseMock = mock(WhereClause.class);
    VariableEntity variableEntity = new VariableEntityBean("type", "id1");
    ValueSet valueSet = new ValueSetBean(valueTableMock, variableEntity);

    when(valueTableMock.getValueSet(variableEntity)).thenReturn(valueSet);
    when(whereClauseMock.where(valueSet)).thenReturn(false);

    View view = View.Builder.newView("view", valueTableMock).where(whereClauseMock).build();
    view.getValueSet(variableEntity);
  }

  @Test
  public void testGetValueSetsWithDefaultWhereClause() {
    ValueTable valueTableMock = mock(ValueTable.class);

    Collection<ValueSet> valueSets = new ArrayList<>();
    VariableEntity variableEntityFoo = new VariableEntityBean("type", "foo");
    VariableEntity variableEntityBar = new VariableEntityBean("type", "bar");
    ValueSet valueSetFoo = new ValueSetBean(valueTableMock, variableEntityFoo);
    ValueSet valueSetBar = new ValueSetBean(valueTableMock, variableEntityBar);
    valueSets.add(valueSetFoo);
    valueSets.add(valueSetBar);
    Timestamps timestamps = NullTimestamps.get();

    when(valueTableMock.getValueSets(any(Iterable.class))).thenReturn(valueSets);
    when(valueTableMock.getTimestamps()).thenReturn(timestamps);

    View view = View.Builder.newView("view", valueTableMock).build();
    Iterable<ValueSet> result = view.getValueSets();

    // Verify state.
    assertThat(result).isNotNull();
    assertThat(result).hasSize(2);
    assertThat(containsValueSet(result, valueSetFoo)).isTrue();
    assertThat(containsValueSet(result, valueSetBar)).isTrue();
  }

  @Test
  public void testGetValueSetsWithIncludingWhereClause() {
    ValueTable valueTableMock = mock(ValueTable.class);
    WhereClause whereClauseMock = mock(WhereClause.class);

    Collection<ValueSet> valueSets = new ArrayList<>();
    VariableEntity variableEntityFoo = new VariableEntityBean("type", "foo");
    VariableEntity variableEntityBar = new VariableEntityBean("type", "bar");
    ValueSet valueSetFoo = new ValueSetBean(valueTableMock, variableEntityFoo);
    ValueSet valueSetBar = new ValueSetBean(valueTableMock, variableEntityBar);
    valueSets.add(valueSetFoo);
    valueSets.add(valueSetBar);

    Timestamps timestamps = NullTimestamps.get();

    when(valueTableMock.getValueSets(any(Iterable.class))).thenReturn(valueSets);
    when(valueTableMock.getTimestamps()).thenReturn(timestamps);
    when(whereClauseMock.where((ValueSet) anyObject(), (View) anyObject())).thenReturn(true);

    View view = View.Builder.newView("view", valueTableMock).where(whereClauseMock).build();
    Iterable<ValueSet> result = view.getValueSets();

    // Verify state.
    assertThat(result).isNotNull();
    assertThat(result).hasSize(2);
    assertThat(containsValueSet(result, valueSetFoo)).isTrue();
    assertThat(containsValueSet(result, valueSetBar)).isTrue();
  }

  @Test
  public void testGetValueSetsWithExcludingWhereClause() {
    ValueTable valueTableMock = mock(ValueTable.class);
    WhereClause whereClauseMock = mock(WhereClause.class);

    VariableEntity variableEntityInclude = new VariableEntityBean("type", "include");
    VariableEntity variableEntityExclude = new VariableEntityBean("type", "exclude");
    Set<VariableEntity> entities = Sets.newLinkedHashSet(variableEntityInclude, variableEntityExclude);

    Collection<ValueSet> valueSets = new ArrayList<>();
    Collection<ValueSet> valueSetsIncluded = new ArrayList<>();
    ValueSet valueSetInclude = new ValueSetBean(valueTableMock, variableEntityInclude);
    ValueSet valueSetExclude = new ValueSetBean(valueTableMock, variableEntityExclude);
    valueSets.add(valueSetInclude);
    valueSets.add(valueSetExclude);
    valueSetsIncluded.add(valueSetInclude);

    when(valueTableMock.getValueSets()).thenReturn(valueSets);
    when(valueTableMock.getValueSets(any(Iterable.class))).thenReturn(valueSetsIncluded);
    when(valueTableMock.getVariableEntities()).thenReturn(entities);
    when(valueTableMock.hasValueSet(any(VariableEntity.class))).thenReturn(true);
    when(valueTableMock.getValueSet(variableEntityInclude)).thenReturn(valueSetInclude);
    when(valueTableMock.getValueSet(variableEntityExclude)).thenReturn(valueSetExclude);
    when(valueTableMock.getTimestamps()).thenReturn(NullTimestamps.get());

    View view = View.Builder.newView("view", valueTableMock).where(whereClauseMock).build();
    when(whereClauseMock.where(valueSetInclude, view)).thenReturn(true);
    when(whereClauseMock.where(valueSetExclude, view)).thenReturn(false);

    Iterable<ValueSet> result = view.getValueSets();

    // Verify state.
    assertThat(result).isNotNull();
    assertThat(result).hasSize(1);
    assertThat(containsValueSet(result, valueSetInclude)).isTrue();
  }

  @Test
  public void testGetVariableWithDefaultSelectClause() {
    ValueTable valueTableMock = mock(ValueTable.class);
    Variable variable = new Variable.Builder("someVariable", TextType.get(), "type").build();

    when(valueTableMock.getVariable(variable.getName())).thenReturn(variable);

    View view = View.Builder.newView("view", valueTableMock).build();
    Variable foundVariable = view.getVariable(variable.getName());
    assertThat(foundVariable).isNotNull();
    assertThat(variable.getName()).isEqualTo(foundVariable.getName());
  }

  @Test
  public void testGetVariableWithIncludingSelectClause() {
    ValueTable valueTableMock = mock(ValueTable.class);
    SelectClause selectClauseMock = mock(SelectClause.class);
    Variable variable = new Variable.Builder("someVariable", TextType.get(), "type").build();

    when(valueTableMock.getVariable(variable.getName())).thenReturn(variable);
    when(selectClauseMock.select(variable)).thenReturn(true);

    View view = View.Builder.newView("view", valueTableMock).select(selectClauseMock).build();
    Variable foundVariable = view.getVariable(variable.getName());
    assertThat(foundVariable).isNotNull();
    assertThat(variable.getName()).isEqualTo(foundVariable.getName());
  }

  @Test(expected = NoSuchVariableException.class)
  public void testGetVariableWithExcludingSelectClause() {
    ValueTable valueTableMock = mock(ValueTable.class);
    SelectClause selectClauseMock = mock(SelectClause.class);
    Variable variable = new Variable.Builder("someVariable", TextType.get(), "type").build();

    when(valueTableMock.getVariable(variable.getName())).thenReturn(variable);
    when(selectClauseMock.select(variable)).thenReturn(false);

    View view = View.Builder.newView("view", valueTableMock).select(selectClauseMock).build();
    view.getVariable(variable.getName());
  }

  @Test
  public void testGetVariablesWithDefaultSelectClause() {
    ValueTable valueTableMock = mock(ValueTable.class);

    Collection<Variable> variables = new ArrayList<>();
    Variable variableFoo = new Variable.Builder("foo", TextType.get(), "type").build();
    Variable variableBar = new Variable.Builder("bar", TextType.get(), "type").build();
    variables.add(variableFoo);
    variables.add(variableBar);

    when(valueTableMock.getVariables()).thenReturn(variables);

    View view = View.Builder.newView("view", valueTableMock).build();
    Iterable<Variable> result = view.getVariables();

    // Verify state.
    assertThat(result).isNotNull();
    assertThat(result).hasSize(2);
    assertThat(containsVariable(result, variableFoo)).isTrue();
    assertThat(containsVariable(result, variableBar)).isTrue();
  }

  @Test
  public void testGetVariablesWithIncludingSelectClause() {
    ValueTable valueTableMock = mock(ValueTable.class);
    SelectClause selectClauseMock = mock(SelectClause.class);

    Collection<Variable> variables = new ArrayList<>();
    Variable variableFoo = new Variable.Builder("foo", TextType.get(), "type").build();
    Variable variableBar = new Variable.Builder("bar", TextType.get(), "type").build();
    variables.add(variableFoo);
    variables.add(variableBar);

    when(valueTableMock.getVariables()).thenReturn(variables);
    when(selectClauseMock.select((Variable) anyObject())).thenReturn(true);

    View view = View.Builder.newView("view", valueTableMock).select(selectClauseMock).build();
    Iterable<Variable> result = view.getVariables();

    // Verify state.
    assertThat(result).isNotNull();
    assertThat(result).hasSize(2);
    assertThat(containsVariable(result, variableFoo)).isTrue();
    assertThat(containsVariable(result, variableBar)).isTrue();
  }

  @Test
  public void testGetVariablesWithExcludingSelectClause() {
    ValueTable valueTableMock = mock(ValueTable.class);
    SelectClause selectClauseMock = mock(SelectClause.class);

    Collection<Variable> variables = new ArrayList<>();
    Variable variableInclude = new Variable.Builder("include", TextType.get(), "type").build();
    Variable variableExclude = new Variable.Builder("exclude", TextType.get(), "type").build();
    variables.add(variableInclude);
    variables.add(variableExclude);

    when(valueTableMock.getVariables()).thenReturn(variables);
    when(selectClauseMock.select(variableInclude)).thenReturn(true);
    when(selectClauseMock.select(variableExclude)).thenReturn(false);

    View view = View.Builder.newView("view", valueTableMock).select(selectClauseMock).build();
    Iterable<Variable> result = view.getVariables();

    // Verify state.
    assertThat(result).isNotNull();
    assertThat(result).hasSize(1);
    assertThat(containsVariable(result, variableInclude)).isTrue();
  }

  @Test
  public void testGetValueWithDefaultWhereClause() {
    ValueTable valueTableMock = mock(ValueTable.class);
    VariableEntity variableEntity = new VariableEntityBean("type", "id1");
    Variable variable = new Variable.Builder("someVariable", TextType.get(), "type").build();
    ValueSet valueSet = new ValueSetBean(valueTableMock, variableEntity);
    Value value = TextType.get().valueOf("someValue");

    when(valueTableMock.getValue((Variable) anyObject(), (ValueSet) anyObject())).thenReturn(value);

    View view = View.Builder.newView("view", valueTableMock).build();
    Value result = view.getValue(variable, new ValueSetWrapper(view, valueSet));
    assertThat(result).isNotNull();
    assertThat(result.getValue().toString()).isEqualTo("someValue");
  }

  @Test
  public void testGetValueWithIncludingWhereClause() {
    ValueTable valueTableMock = mock(ValueTable.class);
    WhereClause whereClauseMock = mock(WhereClause.class);
    VariableEntity variableEntity = new VariableEntityBean("type", "id1");
    Variable variable = new Variable.Builder("someVariable", TextType.get(), "type").build();
    ValueSet valueSet = new ValueSetBean(valueTableMock, variableEntity);
    Value value = TextType.get().valueOf("someValue");

    when(valueTableMock.getValue((Variable) anyObject(), (ValueSet) anyObject())).thenReturn(value);
    when(whereClauseMock.where((ValueSet) anyObject(), (View) anyObject())).thenReturn(true);

    View view = View.Builder.newView("view", valueTableMock).where(whereClauseMock).build();
    Value result = view.getValue(variable, new ValueSetWrapper(view, valueSet));
    assertThat(result).isNotNull();
    assertThat(result.getValue().toString()).isEqualTo("someValue");
  }

  @Test(expected = NoSuchValueSetException.class)
  public void testGetValueWithExcludingWhereClause() {
    ValueTable valueTableMock = mock(ValueTable.class);
    WhereClause whereClauseMock = mock(WhereClause.class);
    VariableEntity variableEntity = new VariableEntityBean("type", "id1");
    Variable variable = new Variable.Builder("someVariable", TextType.get(), "type").build();
    ValueSet valueSet = new ValueSetBean(valueTableMock, variableEntity);
    Value value = TextType.get().valueOf("someValue");

    when(valueTableMock.getValue(variable, valueSet)).thenReturn(value);
    when(whereClauseMock.where(valueSet)).thenReturn(false);

    View view = View.Builder.newView("view", valueTableMock).where(whereClauseMock).build();
    view.getValue(variable, valueSet);

  }

  @Test
  public void testGetVariablesWithListClause() {
    ValueTable valueTableMock = mock(ValueTable.class);
    ListClause listClauseMock = mock(ListClause.class);

    VariableValueSource variableValueSourceMock = mock(VariableValueSource.class);
    Collection<VariableValueSource> variableValueSourceList = new ArrayList<>();
    variableValueSourceList.add(variableValueSourceMock);

    when(listClauseMock.getVariableValueSources()).thenReturn(variableValueSourceList);

    View view = View.Builder.newView("view", valueTableMock).list(listClauseMock).build();
    Iterable<Variable> result = view.getVariables();

    // Verify state.
    assertThat(result).isNotNull();
    assertThat(result).hasSize(1);
  }

  @Test
  public void testGetVariableValueSourceWithListClause() {
    ValueTable valueTableMock = mock(ValueTable.class);
    ListClause listClauseMock = mock(ListClause.class);

    VariableValueSource variableValueSourceMock = mock(VariableValueSource.class);

    when(listClauseMock.getVariableValueSource("variable-name")).thenReturn(variableValueSourceMock);

    View view = View.Builder.newView("view", valueTableMock).list(listClauseMock).build();
    assertThat(view.getVariableValueSource("variable-name")).isNotNull();
  }

  @Test(expected = IncompatibleEntityTypeException.class)
  public void testCreateViewDifferentEntityType() {
    ValueTable valueTableMock = mock(ValueTable.class);
    ListClause listClauseMock = mock(ListClause.class);

    ViewPersistenceStrategy viewPersistenceMock = mock(ViewPersistenceStrategy.class);
    Datasource datasourceMock = mock(Datasource.class);

    ViewManager manager = new DefaultViewManagerImpl(viewPersistenceMock);

    Set<View> views = new HashSet<>();
    View view = View.Builder.newView("view", valueTableMock).list(listClauseMock).build();
    views.add(view);

    listClauseMock.setValueTable(view);

    Variable variable = Variable.Builder.newVariable("variable", ValueType.Factory.forName("text"), "Martian").build();

    VariableValueSource vSourceMock = mock(VariableValueSource.class);

    Collection<VariableValueSource> variablesValueSource = new HashSet<>();
    variablesValueSource.add(vSourceMock);

    when(viewPersistenceMock.readViews("datasource")).thenReturn(views);
    when(datasourceMock.getName()).thenReturn("datasource");
    when(listClauseMock.getVariableValueSources()).thenReturn(variablesValueSource);
    when(vSourceMock.getVariable()).thenReturn(variable);
    when(valueTableMock.getEntityType()).thenReturn("NotMartian");

    manager.decorate(datasourceMock);
    manager.addView("datasource", view, null, null);
  }

  //
  // Helper Methods
  //

  private boolean containsVariable(Iterable<Variable> iterable, final Variable variable) {
    return Iterables.any(iterable, new Predicate<Variable>() {
      @Override
      public boolean apply(Variable input) {
        return input.getName().equals(variable.getName());
      }
    });
  }

  private boolean containsValueSet(Iterable<ValueSet> iterable, final ValueSet valueSet) {
    return Iterables.any(iterable, new Predicate<ValueSet>() {
      @Override
      public boolean apply(ValueSet input) {
        return input.getVariableEntity().getIdentifier().equals(valueSet.getVariableEntity().getIdentifier());
      }
    });
  }
}
