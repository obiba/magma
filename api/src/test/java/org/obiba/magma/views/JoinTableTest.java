package org.obiba.magma.views;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.easymock.EasyMock;
import org.junit.Test;
import org.obiba.magma.NoSuchValueSetException;
import org.obiba.magma.NoSuchVariableException;
import org.obiba.magma.Value;
import org.obiba.magma.ValueSet;
import org.obiba.magma.ValueTable;
import org.obiba.magma.Variable;
import org.obiba.magma.VariableEntity;
import org.obiba.magma.support.VariableEntityBean;
import org.obiba.magma.test.AbstractMagmaTest;
import org.obiba.magma.type.TextType;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;

public class JoinTableTest extends AbstractMagmaTest {
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
    tables.add(MockValueTableBuilder.newMock(PARTICIPANT_ENTITY_TYPE).build());
    new JoinTable(tables);
  }

  @Test
  public void testTableListWithTwoTablesAllowed() {
    List<ValueTable> tables = new ArrayList<ValueTable>();
    tables.add(MockValueTableBuilder.newMock(PARTICIPANT_ENTITY_TYPE).build());
    tables.add(MockValueTableBuilder.newMock(PARTICIPANT_ENTITY_TYPE).build());

    new JoinTable(tables);
  }

  @Test
  public void testTableListWithMoreThanTwoTablesAllowed() {
    List<ValueTable> tables = new ArrayList<ValueTable>();
    tables.add(MockValueTableBuilder.newMock(PARTICIPANT_ENTITY_TYPE).build());
    tables.add(MockValueTableBuilder.newMock(PARTICIPANT_ENTITY_TYPE).build());
    tables.add(MockValueTableBuilder.newMock(PARTICIPANT_ENTITY_TYPE).build());

    new JoinTable(tables);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testAllTablesMustHaveTheSameEntityType() {
    List<ValueTable> tables = new ArrayList<ValueTable>();
    tables.add(MockValueTableBuilder.newMock(PARTICIPANT_ENTITY_TYPE).build());

    ValueTable mock = EasyMock.createMock(ValueTable.class);
    expect(mock.isForEntityType(PARTICIPANT_ENTITY_TYPE)).andReturn(false).once();
    tables.add(mock);

    new JoinTable(tables);
  }

  @Test
  public void testJoinTableHasNoDatasource() {
    JoinTable joinTable = JoinTableBuilder.newBuilder() //
    .withMockTable(MockValueTableBuilder.newMock(PARTICIPANT_ENTITY_TYPE))//
    .withMockTable(MockValueTableBuilder.newMock(PARTICIPANT_ENTITY_TYPE)).build();

    assertEquals(null, joinTable.getDatasource());
  }

  @Test
  public void testGetEntityType() {
    JoinTable joinTable = JoinTableBuilder.newBuilder() //
    .withMockTable(MockValueTableBuilder.newMock(PARTICIPANT_ENTITY_TYPE))//
    .withMockTable(MockValueTableBuilder.newMock(PARTICIPANT_ENTITY_TYPE)).build();

    assertEquals(PARTICIPANT_ENTITY_TYPE, joinTable.getEntityType());
  }

  @Test
  public void testIsForEntityType() {
    JoinTable joinTable = JoinTableBuilder.newBuilder() //
    .withMockTable(MockValueTableBuilder.newMock(PARTICIPANT_ENTITY_TYPE))//
    .withMockTable(MockValueTableBuilder.newMock(PARTICIPANT_ENTITY_TYPE)).build();

    assertTrue(joinTable.isForEntityType(PARTICIPANT_ENTITY_TYPE));
    assertFalse(joinTable.isForEntityType("someOtherType"));
  }

  @Test
  public void testJoinTableNameIsConcatenationOfIndividualTableNames() {
    JoinTable joinTable = JoinTableBuilder.newBuilder() //
    .withMockTable(MockValueTableBuilder.newMock(PARTICIPANT_ENTITY_TYPE).withName("first"))//
    .withMockTable(MockValueTableBuilder.newMock(PARTICIPANT_ENTITY_TYPE).withName("second")).build();

    assertEquals("first-second", joinTable.getName());
  }

  @Test
  public void testJoinTableVariablesAreUnionOfIndividualTableVariables() {
    JoinTable joinTable = JoinTableBuilder.newBuilder() //
    .withMockTable(MockValueTableBuilder.newMock(PARTICIPANT_ENTITY_TYPE).withVariables("var1", "var2", "var3"))//
    .withMockTable(MockValueTableBuilder.newMock(PARTICIPANT_ENTITY_TYPE).withVariables("var2", "var3", "var4"))//
    .withMockTable(MockValueTableBuilder.newMock(PARTICIPANT_ENTITY_TYPE).withVariables("var5", "var6")).build();

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
    JoinTable joinTable = JoinTableBuilder.newBuilder() //
    .withMockTable(MockValueTableBuilder.newMock(PARTICIPANT_ENTITY_TYPE).withVariables("var1", "var2", "var3"))//
    .withMockTable(MockValueTableBuilder.newMock(PARTICIPANT_ENTITY_TYPE).withVariables("var2", "var3", "var4"))//
    .withMockTable(MockValueTableBuilder.newMock(PARTICIPANT_ENTITY_TYPE).withVariables("var5", "var6")).build();

    assertEquals("var1", joinTable.getVariable("var1").getName());
    assertEquals("var2", joinTable.getVariable("var2").getName());
    assertEquals("var3", joinTable.getVariable("var3").getName());
    assertEquals("var4", joinTable.getVariable("var4").getName());
    assertEquals("var5", joinTable.getVariable("var5").getName());
    assertEquals("var6", joinTable.getVariable("var6").getName());
  }

  @Test(expected = NoSuchVariableException.class)
  public void testGetBogusVariableThrowsException() {
    JoinTable joinTable = JoinTableBuilder.newBuilder() //
    .withMockTable(MockValueTableBuilder.newMock(PARTICIPANT_ENTITY_TYPE).withVariables("var1", "var2", "var3"))//
    .withMockTable(MockValueTableBuilder.newMock(PARTICIPANT_ENTITY_TYPE).withVariables("var2", "var3", "var4")).build();

    joinTable.getVariable("bogus").getName();
  }

  @Test
  public void testJoinTableValueSetExistsForValueSetInAnyValueTable() {
    JoinTable joinTable = JoinTableBuilder.newBuilder() //
    .withMockTable(MockValueTableBuilder.newMock(PARTICIPANT_ENTITY_TYPE).withEntities("1")) //
    .withMockTable(MockValueTableBuilder.newMock(PARTICIPANT_ENTITY_TYPE).withEntities("1", "2")).build();

    Iterable<ValueSet> valueSets = joinTable.getValueSets();
    assertEquals(2, Iterables.size(valueSets));
  }

  @Test
  public void testGetValueSet() {
    JoinTable joinTable = JoinTableBuilder.newBuilder() //
    .withMockTable(MockValueTableBuilder.newMock(PARTICIPANT_ENTITY_TYPE).withEntities("1")) //
    .withMockTable(MockValueTableBuilder.newMock(PARTICIPANT_ENTITY_TYPE).withEntities("2")).build();

    assertNotNull(joinTable.getValueSet(new VariableEntityBean(PARTICIPANT_ENTITY_TYPE, "1")));
    assertNotNull(joinTable.getValueSet(new VariableEntityBean(PARTICIPANT_ENTITY_TYPE, "2")));
  }

  @Test(expected = NoSuchValueSetException.class)
  public void testGetNonExistentValueSetThrowsNoSuchValueSetException() {
    JoinTable joinTable = JoinTableBuilder.newBuilder() //
    .withMockTable(MockValueTableBuilder.newMock(PARTICIPANT_ENTITY_TYPE).withEntities("1").withName("a")) //
    .withMockTable(MockValueTableBuilder.newMock(PARTICIPANT_ENTITY_TYPE).withEntities("2").withName("b")).build();

    joinTable.getValueSet(new VariableEntityBean("test", "test"));
  }

  @Test
  public void testHasValueSet() {

    JoinTable joinTable = JoinTableBuilder.newBuilder() //
    .withMockTable(MockValueTableBuilder.newMock(PARTICIPANT_ENTITY_TYPE).withEntities("1")) //
    .withMockTable(MockValueTableBuilder.newMock(PARTICIPANT_ENTITY_TYPE).withEntities("2")).build();

    assertEquals(true, joinTable.hasValueSet(new VariableEntityBean(PARTICIPANT_ENTITY_TYPE, "1")));
    assertEquals(true, joinTable.hasValueSet(new VariableEntityBean(PARTICIPANT_ENTITY_TYPE, "2")));

    assertEquals(false, joinTable.hasValueSet(new VariableEntityBean(PARTICIPANT_ENTITY_TYPE, "3")));
    assertEquals(false, joinTable.hasValueSet(new VariableEntityBean("test", "test")));
  }

  @Test
  public void testGetValue() {
    JoinTable joinTable = JoinTableBuilder.newBuilder() //
    .withMockTable(MockValueTableBuilder.newMock(PARTICIPANT_ENTITY_TYPE).withEntities("1").withGetValue("1", "var1", "1-1")) //
    .withMockTable(MockValueTableBuilder.newMock(PARTICIPANT_ENTITY_TYPE).withEntities("2")).build();

    ValueSet valueSet = EasyMock.createMock(ValueSet.class);
    Variable variable = EasyMock.createMock(Variable.class);
    expect(variable.getName()).andReturn("var1").anyTimes();
    EasyMock.replay(valueSet, variable);

    Value value = joinTable.getValue(variable, valueSet);
    assertNotNull(value);
    assertEquals("1-1", value.toString());
  }

  //
  // Helper Methods
  //

  private static class JoinTableBuilder {

    List<ValueTable> mocks = new ArrayList<ValueTable>();

    public static JoinTableBuilder newBuilder() {
      return new JoinTableBuilder();
    }

    public JoinTableBuilder withMockTable(MockValueTableBuilder builder) {
      mocks.add(builder.build());
      return this;
    }

    public JoinTable build() {
      return new JoinTable(mocks);
    }
  }

  private static class MockValueTableBuilder {

    ValueTable mock = EasyMock.createMock(ValueTable.class);

    String entityType;

    static MockValueTableBuilder newMock(String entityType) {
      MockValueTableBuilder builder = new MockValueTableBuilder();
      builder.entityType = entityType;
      expect(builder.mock.getEntityType()).andReturn(entityType).anyTimes();
      expect(builder.mock.isForEntityType(entityType)).andReturn(true).anyTimes();
      return builder;
    }

    public MockValueTableBuilder withName(String name) {
      expect(mock.getName()).andReturn(name).anyTimes();
      return this;
    }

    public MockValueTableBuilder withGetValue(String entity, String variable, String value) {
      expect(mock.hasVariable(variable)).andReturn(true);
      expect(mock.getValue((Variable) EasyMock.anyObject(), (ValueSet) EasyMock.anyObject())).andReturn(TextType.get().valueOf(value)).anyTimes();
      return this;
    }

    public MockValueTableBuilder withVariables(String... names) {
      Set<Variable> tableVariables = new HashSet<Variable>();
      for(String variableName : names) {
        Variable mockVariable = createMockVariable(variableName);
        expect(mock.getVariable(variableName)).andReturn(mockVariable).anyTimes();
        tableVariables.add(mockVariable);
      }

      expect(mock.getVariables()).andReturn(tableVariables).anyTimes();

      return this;
    }

    MockValueTableBuilder withEntities(String... identifiers) {
      expect(mock.getVariableEntities()).andReturn(createEntitySet(entityType, identifiers)).anyTimes();
      return this;
    }

    ValueTable build() {
      EasyMock.replay(mock);
      return this.mock;
    }
  }

  private static Set<VariableEntity> createEntitySet(String entityType, String... identifiers) {
    ImmutableSet.Builder<VariableEntity> set = ImmutableSet.builder();
    for(String identifier : identifiers) {
      set.add(new VariableEntityBean(entityType, identifier));
    }
    return set.build();
  }

  private static Variable createMockVariable(String variableName) {
    Variable mockVariable = createMock(Variable.class);

    expect(mockVariable.getName()).andReturn(variableName).anyTimes();
    replay(mockVariable);

    return mockVariable;
  }
}
