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

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import org.fest.util.Strings;
import org.junit.Test;
import org.obiba.magma.*;
import org.obiba.magma.support.VariableEntityBean;
import org.obiba.magma.type.TextType;

import java.util.*;

import static org.easymock.EasyMock.*;
import static org.fest.assertions.api.Assertions.assertThat;
import static org.obiba.magma.views.JoinTableTest.MockValueTableBuilder.newTableMock;
import static org.obiba.magma.views.JoinTableTest.MockVariableBuilder.newVariableMock;

@SuppressWarnings("ReuseOfLocalVariable")
public class JoinTableTest extends MagmaTest {

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
        .withMockTable(newTableMock("T1").expectHasValueSet("1", true).expectHasValueSet("2", false) //
            .expectGetVariableEntityBatchSize()
            .expectGetValueSets("1").withEntities("1")) //
        .withMockTable(newTableMock("T2").expectHasValueSet("1", true).expectHasValueSet("2", true) //
            .expectGetVariableEntityBatchSize()
            .expectGetValueSets("1","2").withEntities("1", "2")).build();

    Iterable<ValueSet> valueSets = joinTable.getValueSets();
    assertThat(valueSets).hasSize(2);
  }

  @Test
  public void test_hasValueSet() {
    JoinTable joinTable = JoinTableBuilder.newBuilder() //
        .withMockTable(newTableMock("T1").expectHasValueSet("1", true)) //
        .withMockTable(newTableMock("T2")).build();

    assertThat(joinTable.hasValueSet(newEntity("1"))).isTrue();

    joinTable = JoinTableBuilder.newBuilder() //
        .withMockTable(newTableMock("T1").expectHasValueSet("1", false)) //
        .withMockTable(newTableMock("T2").expectHasValueSet("1", true)).build();

    assertThat(joinTable.hasValueSet(newEntity("1"))).isTrue();

    joinTable = JoinTableBuilder.newBuilder() //
        .withMockTable(newTableMock("T1").expectHasValueSet("3", false)) //
        .withMockTable(newTableMock("T2").expectHasValueSet("3", false)).build();

    assertThat(joinTable.hasValueSet(newEntity("3"))).isFalse();
  }

  @Test
  public void test_hasValueSet_withInner() {
      JoinTable joinTable = JoinTableBuilder.newBuilder() //
        .withMockTable(newTableMock("T1").expectHasValueSet("1", true).expectHasValueSet("2", false)) //
        .withMockTable(newTableMock("T2").expectHasValueSet("2", true))
        .withInnerTable("T2")
          .build();

    assertThat(joinTable.hasValueSet(newEntity("1"))).isTrue();
    assertThat(joinTable.hasValueSet(newEntity("2"))).isFalse();
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
        .withMockTable(newTableMock("T1").expectHasValueSet("1", true)) //
        .withMockTable(newTableMock("T2")).build();

    assertThat(joinTable.getValueSet(newEntity("1"))).isNotNull();

    joinTable = JoinTableBuilder.newBuilder() //
        .withMockTable(newTableMock("T1").expectHasValueSet("2", false)) //
        .withMockTable(newTableMock("T2").expectHasValueSet("2", true)).build();
    assertThat(joinTable.getValueSet(newEntity("2"))).isNotNull();
  }


  @Test(expected = NoSuchValueSetException.class)
  public void test_getValueSet_withInner() {
    JoinTable joinTable = JoinTableBuilder.newBuilder() //
        .withMockTable(newTableMock("T1").expectHasValueSet("1", true).expectHasValueSet("2", false)) //
        .withMockTable(newTableMock("T2").expectHasValueSet("2", true))
        .withInnerTable("T2")
        .build();

    joinTable.getValueSet(newEntity("2"));
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

    private final List<ValueTable> mocks = Lists.newArrayList();

    private final List<String> inners = Lists.newArrayList();

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

    JoinTableBuilder withInnerTable(String name) {
      inners.add(name);
      return this;
    }

    JoinTable build() {
      return new JoinTable(mocks, inners);
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
      expect(builder.mock.getOccurrenceGroup()).andReturn(null).anyTimes();
      expect(builder.mock.getReferencedEntityType()).andReturn(null).anyTimes();
      expect(builder.mock.getMimeType()).andReturn(null).anyTimes();
      expect(builder.mock.getUnit()).andReturn(null).anyTimes();
      expect(builder.mock.getIndex()).andReturn(0).anyTimes();
      expect(builder.mock.getEntityType()).andReturn("Participant").anyTimes();
      expect(builder.mock.hasAttributes()).andReturn(false).anyTimes();
      expect(builder.mock.hasCategories()).andReturn(false).anyTimes();
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
      return newTableMock(null, PARTICIPANT_ENTITY_TYPE);
    }

    static MockValueTableBuilder newTableMock(String name) {
      return newTableMock(name, PARTICIPANT_ENTITY_TYPE);
    }

    static MockValueTableBuilder newTableMock(String name, String entityType) {
      MockValueTableBuilder builder = new MockValueTableBuilder();
      builder.entityType = entityType;
      expect(builder.mock.getEntityType()).andReturn(entityType).anyTimes();
      expect(builder.mock.isForEntityType(entityType)).andReturn(true).anyTimes();
      expect(builder.mock.getVariables()).andReturn(builder.variables).anyTimes();
      if (!Strings.isNullOrEmpty(name)) builder.withName(name);
      return builder;
    }

    MockValueTableBuilder withName(String name) {
      expect(mock.getName()).andReturn(name).anyTimes();
      expect(mock.getTableReference()).andReturn(name).anyTimes();
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

    MockValueTableBuilder expectGetVariableEntityBatchSize() {
      expect(mock.getVariableEntityBatchSize()).andReturn(100).anyTimes();
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
