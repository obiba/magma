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

import org.easymock.EasyMock;
import org.easymock.IArgumentMatcher;
import org.junit.Test;
import org.obiba.magma.NoSuchValueSetException;
import org.obiba.magma.NoSuchVariableException;
import org.obiba.magma.Value;
import org.obiba.magma.ValueSet;
import org.obiba.magma.ValueTable;
import org.obiba.magma.Variable;
import org.obiba.magma.VariableEntity;
import org.obiba.magma.VariableValueSource;
import org.obiba.magma.test.AbstractMagmaTest;
import org.obiba.magma.type.TextType;

public class JoinTableTest extends AbstractMagmaTest {
  //
  // Constants
  //

  private static final String PARTICIPANT_ENTITY_TYPE = "Participant";

  //
  // Instance Variables
  //

  private Map<String, VariableEntity> mockEntityMap = new HashMap<String, VariableEntity>();

  private Map<String, Variable> mockVariableMap = new HashMap<String, Variable>();

  private Map<String, ValueSet> mockValueSetMap = new HashMap<String, ValueSet>();

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
  public void testGetVariable() {
    List<ValueTable> tables = new ArrayList<ValueTable>();
    tables.add(createMockTable("tableOne", PARTICIPANT_ENTITY_TYPE, "var1", "var2", "var3"));
    tables.add(createMockTable("tableTwo", PARTICIPANT_ENTITY_TYPE, "var2", "var3", "var4"));
    tables.add(createMockTable("tableThree", PARTICIPANT_ENTITY_TYPE, "var5", "var6"));

    JoinTable joinTable = new JoinTable(tables);

    assertEquals("var1", joinTable.getVariable("var1").getName());
    assertEquals("var2", joinTable.getVariable("var2").getName());
    assertEquals("var3", joinTable.getVariable("var3").getName());
    assertEquals("var4", joinTable.getVariable("var4").getName());
    assertEquals("var5", joinTable.getVariable("var5").getName());
    assertEquals("var6", joinTable.getVariable("var6").getName());
  }

  @Test(expected = NoSuchVariableException.class)
  public void testGetBogusVariableThrowsException() {
    List<ValueTable> tables = new ArrayList<ValueTable>();
    tables.add(createMockTable("tableOne", PARTICIPANT_ENTITY_TYPE, "var1", "var2", "var3"));
    tables.add(createMockTable("tableTwo", PARTICIPANT_ENTITY_TYPE, "var2", "var3", "var4"));

    JoinTable joinTable = new JoinTable(tables);
    joinTable.getVariable("bogus").getName();
  }

  @Test
  public void testJoinTableValueSetsAreValueSetsCommonToAllIndividualTables() {
    JoinTable joinTable = createTestJoinTable();
    Iterable<ValueSet> valueSets = joinTable.getValueSets();

    List<ValueSet> valueSetList = new ArrayList<ValueSet>();
    for(ValueSet valueSet : valueSets) {
      valueSetList.add(valueSet);
    }
    assertEquals(1, valueSetList.size());
  }

  @Test
  public void testGetValueSet() {
    JoinTable joinTable = createTestJoinTable();

    joinTable.getValueSet(mockEntityMap.get("1111111"));
  }

  @Test(expected = NoSuchValueSetException.class)
  public void testGetBogusValueSet() {
    JoinTable joinTable = createTestJoinTable();

    joinTable.getValueSet(createMockVariableEntity(PARTICIPANT_ENTITY_TYPE, "bogus"));
  }

  @Test
  public void testHasValueSet() {
    JoinTable joinTable = createTestJoinTable();

    // Entity "1111111" is in the JoinTable.
    assertEquals(true, joinTable.hasValueSet(mockEntityMap.get("1111111")));

    // Entity "2222222" is not in the JoinTable, but is in one of the tables that were joined.
    assertEquals(false, joinTable.hasValueSet(mockEntityMap.get("2222222")));

    // Entity "bogus" does not exist in any table.
    assertEquals(false, joinTable.hasValueSet(createMockVariableEntity(PARTICIPANT_ENTITY_TYPE, "bogus")));
  }

  @Test
  public void testGetValue() {
    JoinTable joinTable = createTestJoinTable();

    ValueSet valueSet = joinTable.getValueSet(mockEntityMap.get("1111111"));

    Value varOneValue = joinTable.getValue(joinTable.getVariable("var1"), valueSet);
    Value varTwoValue = joinTable.getValue(joinTable.getVariable("var2"), valueSet);
    Value varThreeValue = joinTable.getValue(joinTable.getVariable("var3"), valueSet);
    Value varFourValue = joinTable.getValue(joinTable.getVariable("var4"), valueSet);

    assertEquals("11", varOneValue.toString());
    assertEquals("12", varTwoValue.toString());
    assertEquals("13", varThreeValue.toString());
    assertEquals("14", varFourValue.toString());
  }

  //
  // Helper Methods
  //

  private VariableEntity createMockVariableEntity(String entityType, String entityIdentifier) {
    VariableEntity mockEntity = createMock(VariableEntity.class);
    mockEntityMap.put(entityIdentifier, mockEntity);

    expect(mockEntity.getType()).andReturn(entityType).anyTimes();
    expect(mockEntity.getIdentifier()).andReturn(entityIdentifier).anyTimes();
    replay(mockEntity);

    return mockEntity;
  }

  private Variable createMockVariable(String variableName) {
    Variable mockVariable = createMock(Variable.class);
    mockVariableMap.put(variableName, mockVariable);

    expect(mockVariable.getName()).andReturn(variableName).anyTimes();
    replay(mockVariable);

    return mockVariable;
  }

  private ValueTable createMockTableNoReplay(String name, String entityType, String... variableNames) {
    ValueTable mockTable = createMock(ValueTable.class);
    expect(mockTable.getName()).andReturn(name).anyTimes();
    expect(mockTable.getEntityType()).andReturn(entityType).anyTimes();

    if(variableNames.length != 0) {
      Set<Variable> tableVariables = new HashSet<Variable>();
      for(String variableName : variableNames) {
        Variable mockVariable = mockVariableMap.get(variableName);
        if(mockVariable == null) {
          mockVariable = createMockVariable(variableName);
        }
        expect(mockTable.getVariable(variableName)).andReturn(mockVariable).anyTimes();
        tableVariables.add(mockVariable);
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

  private ValueSet createMockValueSet(ValueTable mockTable, String mockTableName, String entityType, String entityIdentifier, String... variablesAndValues) {
    VariableEntity mockEntity = mockEntityMap.get(entityIdentifier);
    if(mockEntity == null) {
      mockEntity = createMockVariableEntity(entityType, entityIdentifier);
    }

    ValueSet mockValueSet = createMock(ValueSet.class);
    expect(mockValueSet.getValueTable()).andReturn(mockTable).anyTimes();
    expect(mockValueSet.getVariableEntity()).andReturn(mockEntity).anyTimes();
    replay(mockValueSet);

    mockValueSetMap.put(entityIdentifier, mockValueSet);

    if(variablesAndValues.length != 0) {
      for(int variableIndex = 0; variableIndex < variablesAndValues.length - 1; variableIndex += 2) {
        String variableName = variablesAndValues[variableIndex];
        String variableValue = variablesAndValues[variableIndex + 1];

        Variable mockVariable = mockVariableMap.get(variableName);
        if(mockVariable == null) {
          mockVariable = createMockVariable(variableName);
        }

        ValueSet mockValueSetFirstOccurrence = mockValueSetMap.get(entityIdentifier);
        expect(mockTable.getValue(mockVariable, mockValueSetFirstOccurrence)).andReturn(TextType.get().valueOf(variableValue)).anyTimes();
      }
    }

    return mockValueSet;
  }

  private JoinTable createTestJoinTable() {
    List<ValueTable> tables = new ArrayList<ValueTable>();
    tables.add(createMockTableNoReplay("tableOne", PARTICIPANT_ENTITY_TYPE, "var1", "var2", "var3"));
    tables.add(createMockTableNoReplay("tableTwo", PARTICIPANT_ENTITY_TYPE, "var2", "var3", "var4"));

    ValueTable tableOne = tables.get(0);
    ValueTable tableTwo = tables.get(1);

    List<ValueSet> tableOneValueSets = new ArrayList<ValueSet>();
    List<ValueSet> tableTwoValueSets = new ArrayList<ValueSet>();

    tableOneValueSets.add(createMockValueSet(tableOne, "tableOne", PARTICIPANT_ENTITY_TYPE, "1111111", "var1", "11", "var2", "12", "var3", "13"));
    tableOneValueSets.add(createMockValueSet(tableOne, "tableOne", PARTICIPANT_ENTITY_TYPE, "2222222", "var1", "21", "var2", "22", "var3", "23"));
    tableTwoValueSets.add(createMockValueSet(tableTwo, "tableTwo", PARTICIPANT_ENTITY_TYPE, "1111111", "var2", "12", "var3", "13", "var4", "14"));
    tableTwoValueSets.add(createMockValueSet(tableTwo, "tableTwo", PARTICIPANT_ENTITY_TYPE, "3333333", "var2", "32", "var3", "33", "var4", "34"));

    VariableValueSource var1VariableValueSource = createMockVariableValueSource("var1");
    expect(var1VariableValueSource.getValue(eqValueSet(mockValueSetMap.get("1111111")))).andReturn(TextType.get().valueOf("11")).anyTimes();
    expect(var1VariableValueSource.getValue(eqValueSet(mockValueSetMap.get("2222222")))).andReturn(TextType.get().valueOf("21")).anyTimes();

    VariableValueSource var2VariableValueSource = createMockVariableValueSource("var2");
    expect(var2VariableValueSource.getValue(eqValueSet(mockValueSetMap.get("1111111")))).andReturn(TextType.get().valueOf("12")).anyTimes();
    expect(var2VariableValueSource.getValue(eqValueSet(mockValueSetMap.get("2222222")))).andReturn(TextType.get().valueOf("22")).anyTimes();
    expect(var2VariableValueSource.getValue(eqValueSet(mockValueSetMap.get("3333333")))).andReturn(TextType.get().valueOf("32")).anyTimes();

    VariableValueSource var3VariableValueSource = createMockVariableValueSource("var3");
    expect(var3VariableValueSource.getValue(eqValueSet(mockValueSetMap.get("1111111")))).andReturn(TextType.get().valueOf("13")).anyTimes();
    expect(var3VariableValueSource.getValue(eqValueSet(mockValueSetMap.get("2222222")))).andReturn(TextType.get().valueOf("23")).anyTimes();
    expect(var3VariableValueSource.getValue(eqValueSet(mockValueSetMap.get("3333333")))).andReturn(TextType.get().valueOf("33")).anyTimes();

    VariableValueSource var4VariableValueSource = createMockVariableValueSource("var4");
    expect(var4VariableValueSource.getValue(eqValueSet(mockValueSetMap.get("1111111")))).andReturn(TextType.get().valueOf("14")).anyTimes();
    expect(var4VariableValueSource.getValue(eqValueSet(mockValueSetMap.get("3333333")))).andReturn(TextType.get().valueOf("34")).anyTimes();

    expect(tableOne.getValueSets()).andReturn(tableOneValueSets).anyTimes();
    expect(tableOne.hasValueSet(tableOneValueSets.get(0).getVariableEntity())).andReturn(true).anyTimes();
    expect(tableOne.hasValueSet(tableOneValueSets.get(1).getVariableEntity())).andReturn(true).anyTimes();
    expect(tableOne.hasValueSet(tableTwoValueSets.get(1).getVariableEntity())).andReturn(false).anyTimes();
    expect(tableOne.getValueSet(tableOneValueSets.get(0).getVariableEntity())).andReturn(tableOneValueSets.get(0)).anyTimes();
    expect(tableOne.getValueSet(tableOneValueSets.get(1).getVariableEntity())).andReturn(tableOneValueSets.get(1)).anyTimes();
    expect(tableOne.getVariableValueSource("var1")).andReturn(var1VariableValueSource).anyTimes();
    expect(tableOne.getVariableValueSource("var2")).andReturn(var2VariableValueSource).anyTimes();
    expect(tableOne.getVariableValueSource("var3")).andReturn(var3VariableValueSource).anyTimes();

    expect(tableTwo.getValueSets()).andReturn(tableTwoValueSets).anyTimes();
    expect(tableTwo.hasValueSet(tableTwoValueSets.get(0).getVariableEntity())).andReturn(true).anyTimes();
    expect(tableTwo.hasValueSet(tableTwoValueSets.get(1).getVariableEntity())).andReturn(true).anyTimes();
    expect(tableTwo.hasValueSet(tableOneValueSets.get(1).getVariableEntity())).andReturn(false).anyTimes();
    expect(tableTwo.getValueSet(tableTwoValueSets.get(0).getVariableEntity())).andReturn(tableTwoValueSets.get(0)).anyTimes();
    expect(tableTwo.getValueSet(tableTwoValueSets.get(1).getVariableEntity())).andReturn(tableTwoValueSets.get(1)).anyTimes();
    expect(tableTwo.getVariableValueSource("var2")).andReturn(var2VariableValueSource).anyTimes();
    expect(tableTwo.getVariableValueSource("var3")).andReturn(var3VariableValueSource).anyTimes();
    expect(tableTwo.getVariableValueSource("var4")).andReturn(var4VariableValueSource).anyTimes();

    replay(tableOne, tableTwo, var1VariableValueSource, var2VariableValueSource, var3VariableValueSource, var4VariableValueSource);

    return new JoinTable(tables);
  }

  private VariableValueSource createMockVariableValueSource(String variableName) {
    VariableValueSource mockVariableValueSource = createMock(VariableValueSource.class);

    expect(mockVariableValueSource.getVariable()).andReturn(mockVariableMap.get(variableName)).anyTimes();

    return mockVariableValueSource;
  }

  //
  // Inner Classes
  //

  static class ValueSetMatcher implements IArgumentMatcher {

    private ValueSet expected;

    public ValueSetMatcher(ValueSet expected) {
      this.expected = expected;
    }

    @Override
    public boolean matches(Object actual) {
      if(actual instanceof ValueSet) {
        return ((ValueSet) actual).getVariableEntity().getIdentifier().equals(expected.getVariableEntity().getIdentifier());
      } else {
        return false;
      }
    }

    @Override
    public void appendTo(StringBuffer buffer) {
      buffer.append("eqValueSet(");
      buffer.append(expected.getClass().getName());
      buffer.append(" with entity identifier \"");
      buffer.append(expected.getVariableEntity().getIdentifier());
      buffer.append("\")");
    }

  }

  static ValueSet eqValueSet(ValueSet in) {
    EasyMock.reportMatcher(new ValueSetMatcher(in));
    return null;
  }
}
