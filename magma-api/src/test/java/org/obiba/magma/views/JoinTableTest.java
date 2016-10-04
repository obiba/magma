package org.obiba.magma.views;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.google.common.collect.ImmutableList;
import org.junit.Test;
import org.obiba.magma.NoSuchValueSetException;
import org.obiba.magma.NoSuchVariableException;
import org.obiba.magma.ValueSet;
import org.obiba.magma.ValueTable;
import org.obiba.magma.Variable;
import org.obiba.magma.VariableEntity;
import org.obiba.magma.VariableValueSource;
import org.obiba.magma.support.VariableEntityBean;
import org.obiba.magma.test.AbstractMagmaTest;
import org.obiba.magma.type.TextType;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.fest.assertions.api.Assertions.assertThat;
import static org.obiba.magma.views.JoinTableTest.MockValueTableBuilder.newTableMock;
import static org.obiba.magma.views.JoinTableTest.MockVariableBuilder.newVariableMock;

@SuppressWarnings("ReuseOfLocalVariable")
public class JoinTableTest extends AbstractMagmaTest {

  private static final String PARTICIPANT_ENTITY_TYPE = "Participant";

  @SuppressWarnings("ConstantConditions")
  @edu.umd.cs.findbugs.annotations.SuppressWarnings("NP_NONNULL_PARAM_VIOLATION")
  @Test(expected = IllegalArgumentException.class)
  public void testTableListCannotBeNull() {
    new JoinTable(null);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testTableListCannotBeEmpty() {
    List<ValueTable> tables = new ArrayList<>();
    new JoinTable(tables);
  }

  @Test
  public void testTableListWithTwoTablesAllowed() {
    List<ValueTable> tables = new ArrayList<>();
    tables.add(newTableMock().build());
    tables.add(newTableMock().build());

    new JoinTable(tables);
  }

  @Test
  public void testTableListWithMoreThanTwoTablesAllowed() {
    List<ValueTable> tables = new ArrayList<>();
    tables.add(newTableMock().build());
    tables.add(newTableMock().build());
    tables.add(newTableMock().build());

    new JoinTable(tables);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testAllTablesMustHaveTheSameEntityType() {
    List<ValueTable> tables = new ArrayList<>();
    tables.add(newTableMock().build());

    ValueTable mock = createMock(ValueTable.class);
    expect(mock.isForEntityType(PARTICIPANT_ENTITY_TYPE)).andReturn(false).once();
    tables.add(mock);

    new JoinTable(tables);
  }

  @Test
  public void testJoinTableHasNoDatasource() {
    JoinTable joinTable = JoinTableBuilder.newBuilder() //
        .withMockTable(newTableMock())//
        .withMockTable(newTableMock()).build();

    assertThat(joinTable.getDatasource()).isNull();
  }

  @Test
  public void testGetEntityType() {
    JoinTable joinTable = JoinTableBuilder.newBuilder() //
        .withMockTable(newTableMock())//
        .withMockTable(newTableMock()).build();

    assertThat(joinTable.getEntityType()).isEqualTo(PARTICIPANT_ENTITY_TYPE);
  }

  @Test
  public void testIsForEntityType() {
    JoinTable joinTable = JoinTableBuilder.newBuilder() //
        .withMockTable(newTableMock())//
        .withMockTable(newTableMock()).build();

    assertThat(joinTable.isForEntityType(PARTICIPANT_ENTITY_TYPE)).isTrue();
    assertThat(joinTable.isForEntityType("someOtherType")).isFalse();
  }

  @Test
  public void testJoinTableNameIsConcatenationOfIndividualTableNames() {
    JoinTable joinTable = JoinTableBuilder.newBuilder() //
        .withMockTable(newTableMock().withName("first"))//
        .withMockTable(newTableMock().withName("second")).build();

    assertThat(joinTable.getName()).isEqualTo("first-second");
  }

  @Test
  public void testJoinTableVariablesAreUnionOfIndividualTableVariables() {
    JoinTable joinTable = JoinTableBuilder.newBuilder() //
        .withMockTable(newTableMock().withVariables("var1", "var2", "var3"))//
        .withMockTable(newTableMock().withVariables("var2", "var3", "var4"))//
        .withMockTable(newTableMock().withVariables("var5", "var6")).build();

    Iterable<Variable> variables = joinTable.getVariables();
    assertThat(variables).isNotNull();

    Collection<String> variableNameList = new ArrayList<>();
    for(Variable variable : variables) {
      variableNameList.add(variable.getName());
    }

    assertThat(variableNameList).hasSize(6);
    for(String variableName : new String[] { "var1", "var2", "var3", "var4", "var5", "var6" }) {
      assertThat(variableNameList).contains(variableName);
    }
  }

  @Test
  public void testGetVariable() {
    JoinTable joinTable = JoinTableBuilder.newBuilder() //
        .withMockTable(newTableMock().withVariables("var1", "var2", "var3"))//
        .withMockTable(newTableMock().withVariables("var2", "var3", "var4"))//
        .withMockTable(newTableMock().withVariables("var5", "var6")).build();

    assertThat(joinTable.getVariable("var1").getName()).isEqualTo("var1");
    assertThat(joinTable.getVariable("var2").getName()).isEqualTo("var2");
    assertThat(joinTable.getVariable("var3").getName()).isEqualTo("var3");
    assertThat(joinTable.getVariable("var4").getName()).isEqualTo("var4");
    assertThat(joinTable.getVariable("var5").getName()).isEqualTo("var5");
    assertThat(joinTable.getVariable("var6").getName()).isEqualTo("var6");
  }

  @Test(expected = NoSuchVariableException.class)
  public void testGetBogusVariableThrowsException() {
    JoinTable joinTable = JoinTableBuilder.newBuilder() //
        .withMockTable(newTableMock().withVariables("var1", "var2", "var3"))//
        .withMockTable(newTableMock().withVariables("var2", "var3", "var4")).build();

    joinTable.getVariable("bogus").getName();
  }

  @Test
  public void testJoinTableValueSetExistsForValueSetInAnyValueTable() {
    JoinTable joinTable = JoinTableBuilder.newBuilder() //
        .withMockTable(newTableMock().expectHasValueSet("1", true).expectHasValueSet("2", false) //
            .expectGetValueSets("1").withEntities("1")) //
        .withMockTable(newTableMock().expectHasValueSet("1", true).expectHasValueSet("2", true) //
            .expectGetValueSets("1","2").withEntities("1", "2")).build();

    Iterable<ValueSet> valueSets = joinTable.getValueSets();
    assertThat(valueSets).hasSize(2);
  }

  @Test
  public void test_hasValueSet() {
    JoinTable joinTable = JoinTableBuilder.newBuilder() //
        .withMockTable(newTableMock().expectHasValueSet("1", true)) //
        .withMockTable(newTableMock()).build();

    assertThat(joinTable.hasValueSet(newEntity("1"))).isTrue();

    joinTable = JoinTableBuilder.newBuilder() //
        .withMockTable(newTableMock().expectHasValueSet("1", false)) //
        .withMockTable(newTableMock().expectHasValueSet("1", true)).build();

    assertThat(joinTable.hasValueSet(newEntity("1"))).isTrue();

    joinTable = JoinTableBuilder.newBuilder() //
        .withMockTable(newTableMock().expectHasValueSet("3", false)) //
        .withMockTable(newTableMock().expectHasValueSet("3", false)).build();

    assertThat(joinTable.hasValueSet(newEntity("3"))).isFalse();
  }

  @Test(expected = NoSuchValueSetException.class)
  public void test_getValueSet_ThrowsNoSuchValueSetExceptionWhenValueSetDoesNotExist() {
    JoinTable joinTable = JoinTableBuilder.newBuilder() //
        .withMockTable(newTableMock().expectHasValueSet("test", false).withName("a")) //
        .withMockTable(newTableMock().expectHasValueSet("test", false).withName("a")).build();

    joinTable.getValueSet(newEntity("test"));
  }

  @Test
  public void test_getValueSet() {
    JoinTable joinTable = JoinTableBuilder.newBuilder() //
        .withMockTable(newTableMock().expectHasValueSet("1", true)) //
        .withMockTable(newTableMock()).build();

    assertThat(joinTable.getValueSet(newEntity("1"))).isNotNull();

    joinTable = JoinTableBuilder.newBuilder() //
        .withMockTable(newTableMock().expectHasValueSet("2", false)) //
        .withMockTable(newTableMock().expectHasValueSet("2", true)).build();
    assertThat(joinTable.getValueSet(newEntity("2"))).isNotNull();
  }

  //TODO testGetValue but hard to test with mockups
  // @Test
//  public void testGetValue() {
//
//    VariableEntityBean entity1 = newEntity("1");
//    VariableEntityBean entity2 = newEntity("2");
//
//    Variable var1 = newVariableMock("var1").build();
//
//    ValueTable table1 = newTableMock().withName("table1").withEntities(entity1).withVariables(var1)
//        .withGetValue(var1, "1-1").withGetValueSet("1").build();
//
//    ValueTable table2 = newTableMock().withName("table2").withEntities(entity2).build();
//
//    JoinTable joinTable = JoinTableBuilder.newBuilder().withMockTable(table1).withMockTable(table2).build();
//
//    Value value = joinTable.getValue(var1, new JoinTable.JoinValueSet(table1, entity1));
//    assertNotNull(value);
//    assertThat("1-1", value.toString());
//  }

  private static VariableEntity newEntity(String entityIdentifier) {
    return new VariableEntityBean(PARTICIPANT_ENTITY_TYPE, entityIdentifier);
  }

  //
  // Helper Methods
  //

  static class JoinTableBuilder {

    private final List<ValueTable> mocks = new ArrayList<>();

    static JoinTableBuilder newBuilder() {
      return new JoinTableBuilder();
    }

    JoinTableBuilder withMockTable(MockValueTableBuilder builder) {
      return withMockTable(builder.build());
    }

    JoinTableBuilder withMockTable(ValueTable table) {
      mocks.add(table);
      return this;
    }

    JoinTable build() {
      return new JoinTable(mocks);
    }
  }

  @SuppressWarnings("StaticMethodOnlyUsedInOneClass")
  static class MockVariableBuilder {

    Variable mock = createMock(Variable.class);

    static MockVariableBuilder newVariableMock(String variableName) {
      MockVariableBuilder builder = new MockVariableBuilder();
      expect(builder.mock.getName()).andReturn(variableName).anyTimes();
      expect(builder.mock.getValueType()).andReturn(TextType.get()).anyTimes();
      expect(builder.mock.isRepeatable()).andReturn(false).anyTimes();
      return builder;
    }

    Variable build() {
      replay(mock);
      return mock;
    }
  }

  @SuppressWarnings("StaticMethodOnlyUsedInOneClass")
  static class MockValueTableBuilder {

    private final ValueTable mock = createMock(ValueTable.class);

    private String entityType;

    private final Collection<Variable> variables = new HashSet<>();

    static MockValueTableBuilder newTableMock() {
      return newTableMock(PARTICIPANT_ENTITY_TYPE);
    }

    static MockValueTableBuilder newTableMock(String entityType) {
      MockValueTableBuilder builder = new MockValueTableBuilder();
      builder.entityType = entityType;
      expect(builder.mock.getEntityType()).andReturn(entityType).anyTimes();
      expect(builder.mock.isForEntityType(entityType)).andReturn(true).anyTimes();
      expect(builder.mock.getVariables()).andReturn(builder.variables).anyTimes();
      return builder;
    }

    MockValueTableBuilder withName(String name) {
      expect(mock.getName()).andReturn(name).anyTimes();
      return this;
    }

    MockValueTableBuilder expectHasValueSet(String identifier, boolean expect) {
      expect(mock.hasValueSet(new VariableEntityBean(entityType, identifier))).andReturn(expect).anyTimes();
      return this;
    }

    MockValueTableBuilder expectGetValueSets(String... identifiers) {
      List<VariableEntity> entities = Lists.newArrayList(createEntitySet(entityType, identifiers));
      expect(mock.getValueSets(entities)).andReturn(new ArrayList<>()).anyTimes();
      return this;
    }

    MockValueTableBuilder withVariables(String... names) {
      Collection<Variable> list = Lists.newArrayList();
      for(String variableName : names) {
        list.add(newVariableMock(variableName).build());
      }
      return withVariables(Iterables.toArray(list, Variable.class));
    }

    MockValueTableBuilder withVariables(@SuppressWarnings("ParameterHidesMemberVariable") Variable... variables) {
      for(Variable variable : variables) {
        expect(mock.hasVariable(variable.getName())).andReturn(true).anyTimes();
        expect(mock.getVariable(variable.getName())).andReturn(variable).anyTimes();

        VariableValueSource valueSource = createMock(VariableValueSource.class);
        expect(valueSource.getVariable()).andReturn(variable).anyTimes();
        expect(mock.getVariableValueSource(variable.getName())).andReturn(valueSource).anyTimes();
        this.variables.add(variable);
      }
      return this;
    }

    MockValueTableBuilder withEntities(String... identifiers) {
      expect(mock.getVariableEntities()).andReturn(createEntitySet(entityType, identifiers)).anyTimes();
      return this;
    }

    ValueTable build() {
      replay(mock);
      return mock;
    }

    private static Set<VariableEntity> createEntitySet(String entityType, String... identifiers) {
      ImmutableSet.Builder<VariableEntity> set = ImmutableSet.builder();
      for(String identifier : identifiers) {
        set.add(new VariableEntityBean(entityType, identifier));
      }
      return set.build();
    }
  }

}
