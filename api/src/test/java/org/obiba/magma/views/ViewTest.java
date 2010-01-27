package org.obiba.magma.views;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.Test;
import org.obiba.magma.NoSuchVariableException;
import org.obiba.magma.ValueSet;
import org.obiba.magma.ValueTable;
import org.obiba.magma.Variable;
import org.obiba.magma.VariableEntity;
import org.obiba.magma.support.ValueSetBean;
import org.obiba.magma.support.VariableEntityBean;
import org.obiba.magma.test.AbstractMagmaTest;
import org.obiba.magma.type.TextType;

public class ViewTest extends AbstractMagmaTest {
  //
  // Test Methods
  //

  @Test
  public void testHasValueSetWithNoWhereClause() {
    ValueTable valueTableMock = createMock(ValueTable.class);
    VariableEntity variableEntity = new VariableEntityBean("type", "id1");

    expect(valueTableMock.hasValueSet(variableEntity)).andReturn(true);
    replay(valueTableMock);

    View view = View.Builder.newView("view", valueTableMock).build();
    boolean result = view.hasValueSet(variableEntity);

    // Verify behaviour.
    verify(valueTableMock);

    // Verify state.
    assertTrue(result);
  }

  @Test
  public void testHasValueSetWithIncludingWhereClause() {
    ValueTable valueTableMock = createMock(ValueTable.class);
    WhereClause whereClauseMock = createMock(WhereClause.class);
    VariableEntity variableEntity = new VariableEntityBean("type", "id1");
    ValueSet valueSet = new ValueSetBean(valueTableMock, variableEntity);

    expect(valueTableMock.hasValueSet(variableEntity)).andReturn(true);
    expect(valueTableMock.getValueSet(variableEntity)).andReturn(valueSet);
    expect(whereClauseMock.where(valueSet)).andReturn(true);
    replay(valueTableMock, whereClauseMock);

    View view = View.Builder.newView("view", valueTableMock).where(whereClauseMock).build();
    boolean result = view.hasValueSet(variableEntity);

    // Verify behaviour.
    verify(valueTableMock, whereClauseMock);

    // Verify state.
    assertTrue(result);
  }

  @Test
  public void testHasValueSetWithExcludingWhereClause() {
    ValueTable valueTableMock = createMock(ValueTable.class);
    WhereClause whereClauseMock = createMock(WhereClause.class);
    VariableEntity variableEntity = new VariableEntityBean("type", "id1");
    ValueSet valueSet = new ValueSetBean(valueTableMock, variableEntity);

    expect(valueTableMock.hasValueSet(variableEntity)).andReturn(true);
    expect(valueTableMock.getValueSet(variableEntity)).andReturn(valueSet);
    expect(whereClauseMock.where(valueSet)).andReturn(false);
    replay(valueTableMock, whereClauseMock);

    View view = View.Builder.newView("view", valueTableMock).where(whereClauseMock).build();
    boolean result = view.hasValueSet(variableEntity);

    // Verify behaviour.
    verify(valueTableMock, whereClauseMock);

    // Verify state.
    assertFalse(result);
  }

  @Test
  public void testGetValueSetReturnsValueSetThatRefersToView() {
    ValueTable valueTableMock = createMock(ValueTable.class);
    VariableEntity variableEntity = new VariableEntityBean("type", "id1");
    ValueSet valueSet = new ValueSetBean(valueTableMock, variableEntity);

    expect(valueTableMock.getName()).andReturn("wrappedTable").anyTimes();
    expect(valueTableMock.getValueSet(variableEntity)).andReturn(valueSet);
    replay(valueTableMock);

    View view = View.Builder.newView("view", valueTableMock).build();
    ValueSet viewValueSet = view.getValueSet(variableEntity);

    // Verify behaviour.
    verify(valueTableMock);

    // Verify state.
    assertNotNull(viewValueSet);
    assertNotNull(viewValueSet.getValueTable());
    assertEquals("view", viewValueSet.getValueTable().getName());
  }

  @Test
  public void testGetVariableWithNoSelectClause() {
    ValueTable valueTableMock = createMock(ValueTable.class);
    Variable variable = new Variable.Builder("someVariable", TextType.get(), "type").build();

    expect(valueTableMock.getVariable(variable.getName())).andReturn(variable);
    replay(valueTableMock);

    View view = View.Builder.newView("view", valueTableMock).build();
    Variable result = null;
    try {
      result = view.getVariable(variable.getName());
    } catch(NoSuchVariableException ex) {
      fail("Variable not selected");
    }

    // Verify behaviour.
    verify(valueTableMock);

    // Verify state.
    assertNotNull(result);
    assertEquals(variable.getName(), result.getName());
  }

  @Test
  public void testGetVariableWithIncludingSelectClause() {
    ValueTable valueTableMock = createMock(ValueTable.class);
    SelectClause selectClauseMock = createMock(SelectClause.class);
    Variable variable = new Variable.Builder("someVariable", TextType.get(), "type").build();

    expect(valueTableMock.getVariable(variable.getName())).andReturn(variable);
    expect(selectClauseMock.select(variable)).andReturn(true);
    replay(valueTableMock, selectClauseMock);

    View view = View.Builder.newView("view", valueTableMock).select(selectClauseMock).build();
    Variable result = null;
    try {
      result = view.getVariable(variable.getName());
    } catch(NoSuchVariableException ex) {
      fail("Variable not selected");
    }

    // Verify behaviour.
    verify(valueTableMock, selectClauseMock);

    // Verify state.
    assertNotNull(result);
    assertEquals(variable.getName(), result.getName());
  }

  @Test(expected = NoSuchVariableException.class)
  public void testGetVariableWithExcludingSelectClause() {
    ValueTable valueTableMock = createMock(ValueTable.class);
    SelectClause selectClauseMock = createMock(SelectClause.class);
    Variable variable = new Variable.Builder("someVariable", TextType.get(), "type").build();

    expect(valueTableMock.getVariable(variable.getName())).andReturn(variable);
    expect(selectClauseMock.select(variable)).andReturn(false);
    replay(valueTableMock, selectClauseMock);

    View view = View.Builder.newView("view", valueTableMock).select(selectClauseMock).build();
    Variable result = null;
    result = view.getVariable(variable.getName());

    // Verify behaviour.
    verify(valueTableMock, selectClauseMock);
  }
}
