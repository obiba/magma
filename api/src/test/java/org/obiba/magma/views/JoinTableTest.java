package org.obiba.magma.views;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.Test;
import org.obiba.magma.ValueSet;
import org.obiba.magma.ValueTable;
import org.obiba.magma.Variable;
import org.obiba.magma.VariableEntity;

public class JoinTableTest {
  //
  // Constants
  //

  private static final String PARTICIPANT_ENTITY_TYPE = "Participant";

  //
  // Instance Variables
  //

  private Map<String, VariableEntity> mockEntityMap = new HashMap<String, VariableEntity>();

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
  public void testGetEntityType() {
    List<ValueTable> tables = new ArrayList<ValueTable>();
    tables.add(createMockTable("tableOne", PARTICIPANT_ENTITY_TYPE));
    tables.add(createMockTable("tableTwo", PARTICIPANT_ENTITY_TYPE));

    JoinTable joinTable = new JoinTable(tables);
    assertEquals(PARTICIPANT_ENTITY_TYPE, joinTable.getEntityType());
  }

  @Test
  public void testIsForEntityType() {
    List<ValueTable> tables = new ArrayList<ValueTable>();
    tables.add(createMockTable("tableOne", PARTICIPANT_ENTITY_TYPE));
    tables.add(createMockTable("tableTwo", PARTICIPANT_ENTITY_TYPE));

    JoinTable joinTable = new JoinTable(tables);
    assertTrue(joinTable.isForEntityType(PARTICIPANT_ENTITY_TYPE));
    assertFalse(joinTable.isForEntityType("someOtherType"));
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

  @Test
  public void testJoinTableValueSets() {
    List<ValueTable> tables = new ArrayList<ValueTable>();
    tables.add(createMockTableNoReplay("tableOne", PARTICIPANT_ENTITY_TYPE, "var1", "var2", "var3"));
    tables.add(createMockTableNoReplay("tableTwo", PARTICIPANT_ENTITY_TYPE, "var2", "var3", "var4"));

    ValueTable tableOne = tables.get(0);
    ValueTable tableTwo = tables.get(1);

    List<ValueSet> tableOneValueSets = new ArrayList<ValueSet>();
    List<ValueSet> tableTwoValueSets = new ArrayList<ValueSet>();

    tableOneValueSets.add(createMockValueSet(tableOne, PARTICIPANT_ENTITY_TYPE, "1111111", "var1", "11", "var2", "12", "var3", "13"));
    tableOneValueSets.add(createMockValueSet(tableOne, PARTICIPANT_ENTITY_TYPE, "2222222", "var1", "21", "var2", "22", "var3", "23"));
    tableTwoValueSets.add(createMockValueSet(tableTwo, PARTICIPANT_ENTITY_TYPE, "1111111", "var2", "12", "var3", "13", "var4", "14"));
    tableTwoValueSets.add(createMockValueSet(tableTwo, PARTICIPANT_ENTITY_TYPE, "3333333", "var2", "32", "var3", "33", "var4", "34"));

    expect(tableOne.getValueSets()).andReturn(tableOneValueSets).anyTimes();
    expect(tableOne.hasValueSet(tableOneValueSets.get(0).getVariableEntity())).andReturn(true).anyTimes();
    expect(tableOne.hasValueSet(tableOneValueSets.get(1).getVariableEntity())).andReturn(true).anyTimes();
    expect(tableOne.hasValueSet(tableTwoValueSets.get(1).getVariableEntity())).andReturn(false).anyTimes();

    expect(tableTwo.getValueSets()).andReturn(tableTwoValueSets).anyTimes();
    expect(tableTwo.hasValueSet(tableTwoValueSets.get(0).getVariableEntity())).andReturn(true).anyTimes();
    expect(tableTwo.hasValueSet(tableTwoValueSets.get(1).getVariableEntity())).andReturn(true).anyTimes();
    expect(tableTwo.hasValueSet(tableOneValueSets.get(1).getVariableEntity())).andReturn(false).anyTimes();

    replay(tableOne, tableTwo);

    JoinTable joinTable = new JoinTable(tables);
    Iterable<ValueSet> valueSets = joinTable.getValueSets();

    List<ValueSet> valueSetList = new ArrayList<ValueSet>();
    for(ValueSet valueSet : valueSets) {
      valueSetList.add(valueSet);
    }
    assertEquals(1, valueSetList.size());
  }

  //
  // Helper Methods
  //

  private ValueTable createMockTableNoReplay(String name, String entityType, String... variableNames) {
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

    return mockTable;
  }

  private ValueTable createMockTable(String name, String entityType, String... variableNames) {
    ValueTable mockTable = createMockTableNoReplay(name, entityType, variableNames);

    replay(mockTable);

    return mockTable;
  }

  private ValueSet createMockValueSet(ValueTable mockTable, String entityType, String entityIdentifier, String... variablesAndValues) {
    VariableEntity mockEntity = mockEntityMap.get(entityIdentifier);
    if(mockEntity == null) {
      mockEntity = createMock(VariableEntity.class);
      mockEntityMap.put(entityIdentifier, mockEntity);
      expect(mockEntity.getType()).andReturn(entityType).anyTimes();
      expect(mockEntity.getIdentifier()).andReturn(entityIdentifier).anyTimes();
      replay(mockEntity);
    }

    ValueSet mockValueSet = createMock(ValueSet.class);
    expect(mockValueSet.getValueTable()).andReturn(mockTable).anyTimes();
    expect(mockValueSet.getVariableEntity()).andReturn(mockEntity).anyTimes();
    replay(mockValueSet);

    return mockValueSet;
  }
}
