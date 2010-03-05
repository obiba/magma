package org.obiba.magma.views;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.Test;
import org.obiba.magma.ValueTable;
import org.obiba.magma.Variable;

public class JoinTableTest {
  //
  // Constants
  //

  private static final String PARTICIPANT_ENTITY_TYPE = "Participant";

  //
  // Instance Variables
  //

  //
  // Test Methods
  //

  @Test(expected = IllegalArgumentException.class)
  public void testTableListCannotBeNull() {
    new JoinTable(null);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testTableListCannotBeEmpty() {
    List<ValueTable> tables = new ArrayList<ValueTable>();
    new JoinTable(tables);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testTableListMustContainAtLeastTwoTables() {
    List<ValueTable> tables = new ArrayList<ValueTable>();
    tables.add(createMockTable("table", PARTICIPANT_ENTITY_TYPE));

    new JoinTable(tables);
  }

  @Test
  public void testTableListWithTwoTablesAllowed() {
    List<ValueTable> tables = new ArrayList<ValueTable>();
    tables.add(createMockTable("tableOne", PARTICIPANT_ENTITY_TYPE));
    tables.add(createMockTable("tableTwo", PARTICIPANT_ENTITY_TYPE));

    new JoinTable(tables);
  }

  @Test
  public void testTableListWithMoreThanTwoTablesAllowed() {
    List<ValueTable> tables = new ArrayList<ValueTable>();
    tables.add(createMockTable("tableOne", PARTICIPANT_ENTITY_TYPE));
    tables.add(createMockTable("tableTwo", PARTICIPANT_ENTITY_TYPE));
    tables.add(createMockTable("tableThree", PARTICIPANT_ENTITY_TYPE));

    new JoinTable(tables);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testAllTablesMustHaveTheSameEntityType() {
    List<ValueTable> tables = new ArrayList<ValueTable>();
    tables.add(createMockTable("tableOne", PARTICIPANT_ENTITY_TYPE));
    tables.add(createMockTable("tableTwo", "someOtherType"));

    new JoinTable(tables);
  }

  @Test
  public void testJoinTableHasNoDatasource() {
    List<ValueTable> tables = new ArrayList<ValueTable>();
    tables.add(createMockTable("tableOne", PARTICIPANT_ENTITY_TYPE));
    tables.add(createMockTable("tableTwo", PARTICIPANT_ENTITY_TYPE));

    JoinTable joinTable = new JoinTable(tables);
    assertEquals(null, joinTable.getDatasource());
  }

  @Test
  public void testJoinTableNameIsConcatenationOfIndividualTableNames() {
    List<ValueTable> tables = new ArrayList<ValueTable>();
    tables.add(createMockTable("tableOne", PARTICIPANT_ENTITY_TYPE));
    tables.add(createMockTable("tableTwo", PARTICIPANT_ENTITY_TYPE));

    JoinTable joinTable = new JoinTable(tables);
    assertEquals("tableOne-tableTwo", joinTable.getName());
  }

  @Test
  public void testJoinTableVariablesAreUnionOfIndividualTableVariables() {
    List<ValueTable> tables = new ArrayList<ValueTable>();
    tables.add(createMockTable("tableOne", PARTICIPANT_ENTITY_TYPE, "var1", "var2", "var3"));
    tables.add(createMockTable("tableTwo", PARTICIPANT_ENTITY_TYPE, "var2", "var3", "var4"));
    tables.add(createMockTable("tableThree", PARTICIPANT_ENTITY_TYPE, "var5", "var6"));

    JoinTable joinTable = new JoinTable(tables);
    Iterable<Variable> variables = joinTable.getVariables();
    assertNotNull(variables);

    List<String> variableNameList = new ArrayList<String>();
    for(Variable variable : variables) {
      variableNameList.add(variable.getName());
    }

    assertEquals(6, variableNameList.size());
    for(String variableName : new String[] { "var1", "var2", "var3", "var4", "var5", "var6" }) {
      assertTrue(variableName, variableNameList.contains(variableName));
    }
  }

  //
  // Helper Methods
  //

  private ValueTable createMockTable(String name, String entityType, String... variableNames) {
    ValueTable mockTable = createMock(ValueTable.class);
    expect(mockTable.getName()).andReturn(name).anyTimes();
    expect(mockTable.getEntityType()).andReturn(entityType).anyTimes();

    if(variableNames.length != 0) {
      Set<Variable> tableVariables = new HashSet<Variable>();
      for(String variableName : variableNames) {
        Variable mockVariable = createMock(Variable.class);
        tableVariables.add(mockVariable);
        expect(mockVariable.getName()).andReturn(variableName).anyTimes();
        replay(mockVariable);
      }

      expect(mockTable.getVariables()).andReturn(tableVariables).anyTimes();
    }

    replay(mockTable);

    return mockTable;
  }
}
