/*
 * Copyright (c) 2017 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.magma.datasource.hibernate;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.obiba.magma.Category;
import org.obiba.magma.MagmaEngine;
import org.obiba.magma.NoSuchValueSetException;
import org.obiba.magma.NoSuchValueTableException;
import org.obiba.magma.NoSuchVariableException;
import org.obiba.magma.Value;
import org.obiba.magma.ValueSet;
import org.obiba.magma.ValueTable;
import org.obiba.magma.ValueTableWriter;
import org.obiba.magma.ValueTableWriter.VariableWriter;
import org.obiba.magma.Variable;
import org.obiba.magma.VariableEntity;
import org.obiba.magma.VariableValueSource;
import org.obiba.magma.VectorSource;
import org.obiba.magma.datasource.generated.BinaryValueGenerator;
import org.obiba.magma.datasource.generated.GeneratedValueTable;
import org.obiba.magma.datasource.hibernate.domain.CategoryState;
import org.obiba.magma.datasource.hibernate.domain.DatasourceState;
import org.obiba.magma.datasource.hibernate.domain.ValueSetState;
import org.obiba.magma.datasource.hibernate.domain.ValueSetValue;
import org.obiba.magma.datasource.hibernate.domain.ValueTableState;
import org.obiba.magma.datasource.hibernate.domain.VariableState;
import org.obiba.magma.support.DatasourceCopier;
import org.obiba.magma.support.Initialisables;
import org.obiba.magma.support.VariableEntityBean;
import org.obiba.magma.type.BinaryType;
import org.obiba.magma.type.DateTimeType;
import org.obiba.magma.type.DateType;
import org.obiba.magma.type.DecimalType;
import org.obiba.magma.type.IntegerType;
import org.obiba.magma.type.TextType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.junit.Assert.fail;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("/test-context.xml")
@SuppressWarnings({ "OverlyLongMethod", "MagicNumber", "PMD.NcssMethodCount" })
public class HibernateDatasourceTest {

  private static final Logger log = LoggerFactory.getLogger(HibernateDatasourceTest.class);

  private static final String PARTICIPANT = "Participant";

  private static final String DATASOURCE = "testDs";

  private static final String TABLE = "testTable";

  @Autowired
  private SessionFactory sessionFactory;

  @Autowired
  private TransactionTemplate transactionTemplate;

  public HibernateDatasourceTest() {
    System.setProperty("net.sf.ehcache.skipUpdateCheck", "true");
  }

  @Before
  public void setup() {
    MagmaEngine.get();
    cleanlyRemoveDatasource(true);
  }

  @After
  public void shutdown() {
    transactionTemplate.execute(new TransactionCallbackWithoutResult() {
      @Override
      protected void doInTransactionWithoutResult(TransactionStatus status) {
        MagmaEngine.get().shutdown();
      }
    });
  }

  @Test
  public void test_transactional_table_creation() {

    transactionTemplate.execute(new TransactionCallbackRuntimeExceptions() {
      @Override
      protected void doAction(TransactionStatus status) throws Exception {

        HibernateDatasource ds = createDatasource();

        MagmaEngine.get().addDatasource(ds);
        ds.createWriter(TABLE, PARTICIPANT).close();

        assertThat(ds.hasValueTable(TABLE)).isTrue();
        assertThat(ds.getValueTable(TABLE)).isNotNull();

        // Make sure the table is not visible outside this transaction.
        new TestThread() {
          @Override
          public void test() {
            // Assert that the datasource does not have the value table
            assertThat(MagmaEngine.get().getDatasource(DATASOURCE).hasValueTable(TABLE)).isFalse();
          }
        }.assertNoException();
      }
    });

    // Make sure the table is visible outside this transaction.
    transactionTemplate.execute(new TransactionCallbackRuntimeExceptions() {
      @Override
      protected void doAction(TransactionStatus status) throws Exception {
        new TestThread() {
          @Override
          public void test() {
            assertThat(MagmaEngine.get().getDatasource(DATASOURCE).hasValueTable(TABLE)).isTrue();
          }
        }.assertNoException();
      }
    });
  }

  @Test
  public void test_table_and_variables_persisted() {

    transactionTemplate.execute(new TransactionCallbackRuntimeExceptions() {
      @Override
      protected void doAction(TransactionStatus status) throws Exception {
        HibernateDatasource ds = createDatasource();

        MagmaEngine.get().addDatasource(ds);

        try(ValueTableWriter tableWriter = ds.createWriter(TABLE, PARTICIPANT)) {

          // Test that the table is visible
          assertThat(ds.hasValueTable(TABLE)).isTrue();

          // Write variables and assert that they are visible.
          try(VariableWriter variableWriter = tableWriter.writeVariables()) {
            variableWriter.writeVariable(Variable.Builder.newVariable("Var1", TextType.get(), PARTICIPANT).build());
            assertThat(ds.getValueTable(TABLE).getVariable("Var1")).isNotNull();
            variableWriter.writeVariable(Variable.Builder.newVariable("Var2", IntegerType.get(), PARTICIPANT).build());
            assertThat(ds.getValueTable(TABLE).getVariable("Var2")).isNotNull();
          }
        }
      }
    });

    cleanlyRemoveDatasource(false);

    // Re-create same datasource and assert that everything is still there.
    transactionTemplate.execute(new TransactionCallbackRuntimeExceptions() {
      @Override
      protected void doAction(TransactionStatus status) throws Exception {
        HibernateDatasource ds = createDatasource();
        MagmaEngine.get().addDatasource(ds);
        assertThat(ds.getValueTable(TABLE)).isNotNull();
        assertThat(ds.getValueTable(TABLE).getVariable("Var1")).isNotNull();
        assertThat(ds.getValueTable(TABLE).getVariable("Var2")).isNotNull();
      }
    });

  }

  @Test
  public void test_variable_state_change_is_persisted() {
    final Variable initialState = Variable.Builder.newVariable("Var1", TextType.get(), PARTICIPANT)
        .addCategory("C1", "1").addCategory("C2", "2").addCategory("C3", "3").build();
    final Variable changedState = Variable.Builder.newVariable("Var1", TextType.get(), PARTICIPANT)
        .addCategory("C3", "3").addCategory("C1", "1").addCategory("C4", "4").addCategory("C2", "2").build();

    transactionTemplate.execute(new TransactionCallbackRuntimeExceptions() {
      @Override
      protected void doAction(TransactionStatus status) throws Exception {
        HibernateDatasource ds = createDatasource();

        MagmaEngine.get().addDatasource(ds);

        try(ValueTableWriter tableWriter = ds.createWriter(TABLE, PARTICIPANT)) {
          // Write variables and assert that they are visible.
          try(VariableWriter variableWriter = tableWriter.writeVariables()) {
            variableWriter.writeVariable(initialState);
          }
        }
      }
    });

    transactionTemplate.execute(new TransactionCallbackRuntimeExceptions() {
      @Override
      protected void doAction(TransactionStatus status) throws Exception {
        HibernateDatasource ds = getDatasource();

        Variable variable = ds.getValueTable(TABLE).getVariable("Var1");
        assertSameCategories(initialState, variable);

        try(ValueTableWriter tableWriter = ds.createWriter(TABLE, PARTICIPANT);
            VariableWriter variableWriter = tableWriter.writeVariables()) {
          variableWriter.writeVariable(changedState);
        }
      }
    });

    transactionTemplate.execute(new TransactionCallbackRuntimeExceptions() {
      @Override
      protected void doAction(TransactionStatus status) throws Exception {
        HibernateDatasource ds = getDatasource();
        Variable v = ds.getValueTable(TABLE).getVariable("Var1");
        assertSameCategories(changedState, v);
      }
    });

  }

  @Test
  public void test_write() {

    transactionTemplate.execute(new TransactionCallbackRuntimeExceptions() {
      @Override
      protected void doAction(TransactionStatus status) throws Exception {
        HibernateDatasource ds = createDatasource();

        ImmutableSet<Variable> variables = ImmutableSet.of( //
            Variable.Builder.newVariable("Test Variable", IntegerType.get(), PARTICIPANT).build(), //
            Variable.Builder.newVariable("Other Variable", DecimalType.get(), PARTICIPANT).build());

        ValueTable generatedValueTable = new GeneratedValueTable(ds, variables, 50);
        MagmaEngine.get().addDatasource(ds);
        DatasourceCopier.Builder.newCopier().build().copy(generatedValueTable, ds);
      }
    });

  }

  @Test
  public void test_vector_source() {

    final ImmutableSet<Variable> variables = ImmutableSet.of(//
        Variable.Builder.newVariable("Test Variable", IntegerType.get(), PARTICIPANT).build(), //
        Variable.Builder.newVariable("Test Repeatable", TextType.get(), PARTICIPANT).repeatable().build(), //
        Variable.Builder.newVariable("Test Date", DateType.get(), PARTICIPANT).build(), //
        Variable.Builder.newVariable("Test DateTime", DateTimeType.get(), PARTICIPANT).build(), //
        Variable.Builder.newVariable("Other Variable", DecimalType.get(), PARTICIPANT).build());

    transactionTemplate.execute(new TransactionCallbackRuntimeExceptions() {
      @Override
      protected void doAction(TransactionStatus status) throws Exception {
        HibernateDatasource ds = createDatasource();
        ValueTable generatedValueTable = new GeneratedValueTable(ds, variables, 50);
        MagmaEngine.get().addDatasource(ds);
        DatasourceCopier.Builder.newCopier().build().copy(generatedValueTable, "NewTable", ds);
      }
    });

    transactionTemplate.execute(new TransactionCallbackRuntimeExceptions() {
      @Override
      protected void doAction(TransactionStatus status) throws Exception {
        HibernateDatasource ds = getDatasource();
        ValueTable valueTable = ds.getValueTable("NewTable");

        for(Variable variable : variables) {
          VariableValueSource valueSource = valueTable.getVariableValueSource(variable.getName());
          assertThat(valueSource).isNotNull();
          assertThat(valueSource.supportVectorSource()).isTrue();
          VectorSource vectorSource = valueSource.asVectorSource();
          assertThat(vectorSource).isNotNull();

          SortedSet<VariableEntity> entities = Sets.newTreeSet(valueTable.getVariableEntities());
          Iterable<Value> values = vectorSource.getValues(entities);
          assertThat(values).isNotNull();
          assertThat(values).hasSize(entities.size());
          Iterator<Value> valuesIter = values.iterator();
          for(VariableEntity entity : entities) {
            Value value = valuesIter.next();
            assertThat(value).isNotNull();
            assertThat(value.isSequence()).isEqualTo(variable.isRepeatable());
            assertThat(value).isEqualTo(valueTable.getValue(variable, valueTable.getValueSet(entity)));
          }
        }
      }
    });
  }

  @Test
  public void test_vector_source_with_additional_entity() {

    final ImmutableSet<Variable> variables = ImmutableSet.of(//
        Variable.Builder.newVariable("Test Variable", IntegerType.get(), PARTICIPANT).build(), //
        Variable.Builder.newVariable("Test Repeatable", TextType.get(), PARTICIPANT).repeatable().build(), //
        Variable.Builder.newVariable("Test Date", DateType.get(), PARTICIPANT).build(), //
        Variable.Builder.newVariable("Test DateTime", DateTimeType.get(), PARTICIPANT).build(), //
        Variable.Builder.newVariable("Other Variable", DecimalType.get(), PARTICIPANT).build());

    transactionTemplate.execute(new TransactionCallbackRuntimeExceptions() {
      @Override
      protected void doAction(TransactionStatus status) throws Exception {
        HibernateDatasource ds = createDatasource();
        ValueTable generatedValueTable = new GeneratedValueTable(ds, variables, 50);
        MagmaEngine.get().addDatasource(ds);
        DatasourceCopier.Builder.newCopier().build().copy(generatedValueTable, "NewTable", ds);
      }
    });

    transactionTemplate.execute(new TransactionCallbackRuntimeExceptions() {
      @Override
      protected void doAction(TransactionStatus status) throws Exception {
        HibernateDatasource ds = getDatasource();
        ValueTable valueTable = ds.getValueTable("NewTable");

        for(Variable variable : variables) {
          VariableValueSource valueSource = valueTable.getVariableValueSource(variable.getName());
          assertThat(valueSource).isNotNull();
          assertThat(valueSource.supportVectorSource()).isTrue();
          VectorSource vectorSource = valueSource.asVectorSource();
          assertThat(vectorSource).isNotNull();

          VariableEntity unexpected = new VariableEntityBean(valueTable.getEntityType(), "0000000");

          SortedSet<VariableEntity> entities = Sets
              .newTreeSet(Iterables.concat(Sets.newHashSet(unexpected), valueTable.getVariableEntities()));
          Iterable<Value> values = vectorSource.getValues(entities);
          assertThat(values).isNotNull();
          assertThat(values).hasSize(entities.size());
          Iterator<Value> valuesIter = values.iterator();
          for(VariableEntity entity : entities) {

            Value value = valuesIter.next();
            log.info("Entity={} Value={}", entity, value);
            assertThat(value).isNotNull();
            assertThat(value.isSequence()).isEqualTo(variable.isRepeatable());
            if(valueTable.hasValueSet(entity)) {
              assertThat(value).isEqualTo(valueTable.getValue(variable, valueTable.getValueSet(entity)));
            } else {
              assertThat(value.isNull()).isTrue();
            }
          }
        }
      }
    });
  }

  @SuppressWarnings("ConstantConditions")
  @Test
  public void test_binary_vector_source() {

    final ImmutableSet<Variable> variables = ImmutableSet.of( //
        Variable.Builder.newVariable("Test Binary", BinaryType.get(), PARTICIPANT).build(),
        Variable.Builder.newVariable("Test Repeatable Binary", BinaryType.get(), PARTICIPANT).repeatable().build());

    transactionTemplate.execute(new TransactionCallbackRuntimeExceptions() {
      @Override
      protected void doAction(TransactionStatus status) throws Exception {
        HibernateDatasource ds = createDatasource();
        ValueTable generatedValueTable = new GeneratedValueTable(ds, variables, 1);
        MagmaEngine.get().addDatasource(ds);
        DatasourceCopier.Builder.newCopier().build().copy(generatedValueTable, "NewTable", ds);
      }
    });

    transactionTemplate.execute(new TransactionCallbackRuntimeExceptions() {
      @Override
      protected void doAction(TransactionStatus status) throws Exception {
        HibernateDatasource ds = getDatasource();
        ValueTable valueTable = ds.getValueTable("NewTable");

        for(Variable variable : variables) {

          VariableValueSource valueSource = valueTable.getVariableValueSource(variable.getName());
          assertThat(valueSource).isNotNull();
          assertThat(valueSource.supportVectorSource()).isTrue();
          VectorSource vectorSource = valueSource.asVectorSource();
          assertThat(vectorSource).isNotNull();

          SortedSet<VariableEntity> entities = Sets.newTreeSet(valueTable.getVariableEntities());
          Iterable<Value> values = vectorSource.getValues(entities);
          assertThat(values).isNotNull();
          assertThat(values).hasSize(entities.size());
          long length = BinaryValueGenerator.getLength();
          for(Value value : values) {
            assertThat(value.isNull()).isFalse();
            assertThat(value.isSequence()).isEqualTo(variable.isRepeatable());
            if(value.isSequence()) {
              for(Value v : value.asSequence().getValue()) {
                if (!v.isNull()) {
                  assertThat(v.getValue()).isInstanceOf(byte[].class);
                  assertThat(v.getLength()).isEqualTo(length);
                }
              }
            } else {
              assertThat(value.getValue()).isInstanceOf(byte[].class);
              assertThat(value.getLength()).isEqualTo(length);
            }
          }
        }
      }
    });

  }

  @Test
  public void test_timestamps_adding_data() throws Exception {

    final ImmutableSet<Variable> variables = ImmutableSet.of( //
        Variable.Builder.newVariable("Test Variable", IntegerType.get(), PARTICIPANT).build(), //
        Variable.Builder.newVariable("Other Variable", DecimalType.get(), PARTICIPANT).build());

    transactionTemplate.execute(new TransactionCallbackRuntimeExceptions() {
      @Override
      protected void doAction(TransactionStatus status) throws Exception {
        HibernateDatasource ds = createDatasource();
        ValueTable generatedValueTable = new GeneratedValueTable(ds, variables, 5);
        MagmaEngine.get().addDatasource(ds);
        DatasourceCopier.Builder.newCopier().build().copy(generatedValueTable, "NewTable", ds);
      }
    });

    Thread.sleep(1000);

    transactionTemplate.execute(new TransactionCallbackRuntimeExceptions() {
      @Override
      protected void doAction(TransactionStatus status) throws Exception {
        HibernateDatasource ds = getDatasource();
        ValueTable generatedValueTable = new GeneratedValueTable(ds, variables, 50);
        MagmaEngine.get().addDatasource(ds);
        DatasourceCopier.Builder.newCopier().build().copy(generatedValueTable, "NewTable", ds);
      }
    });

    Thread.sleep(1000);

    transactionTemplate.execute(new TransactionCallbackRuntimeExceptions() {
      @Override
      protected void doAction(TransactionStatus status) throws Exception {
        HibernateDatasource ds = getDatasource();
        ValueTable table = ds.getValueTable("NewTable");
        Date lastValueSetUpdate = null;
        for(ValueSet valueSet : table.getValueSets()) {
          Date lastUpdate = (Date) valueSet.getTimestamps().getLastUpdate().getValue();
          if(lastValueSetUpdate == null || lastValueSetUpdate.before(lastUpdate)) {
            lastValueSetUpdate = lastUpdate;
          }
        }
        Date tableCreated = (Date) table.getTimestamps().getCreated().getValue();
        Date tableLastUpdate = (Date) table.getTimestamps().getLastUpdate().getValue();

        assertThat(lastValueSetUpdate).isBeforeOrEqualsTo(tableLastUpdate);

        // Table create date should be before last update
        assertThat(tableCreated).isBefore(tableLastUpdate);
      }
    });

  }

  @Test
  public void test_timestamps_adding_variable() throws Exception {

    transactionTemplate.execute(new TransactionCallbackRuntimeExceptions() {
      @Override
      protected void doAction(TransactionStatus status) throws Exception {
        HibernateDatasource ds = createDatasource();
        ValueTable generatedValueTable = new GeneratedValueTable(ds,
            ImmutableSet.of(Variable.Builder.newVariable("Test Variable", IntegerType.get(), PARTICIPANT).build()), 1);
        MagmaEngine.get().addDatasource(ds);
        DatasourceCopier.Builder.newCopier().build().copy(generatedValueTable, TABLE, ds);
      }
    });

    Thread.sleep(1000);

    Date datasourceLastUpdate = getDatasourceStateLastUpdate();
    Date tableLastUpdate = getTableLastUpdate(TABLE);

    // add new variable
    transactionTemplate.execute(new TransactionCallbackRuntimeExceptions() {
      @Override
      protected void doAction(TransactionStatus status) throws Exception {
        HibernateDatasource ds = getDatasource();

        try(ValueTableWriter tableWriter = ds.createWriter(TABLE, PARTICIPANT);
            VariableWriter variableWriter = tableWriter.writeVariables()) {
          variableWriter
              .writeVariable(Variable.Builder.newVariable("New Variable", TextType.get(), PARTICIPANT).build());
        }
      }
    });

    assertThat(datasourceLastUpdate).isEqualTo(getDatasourceStateLastUpdate());
    assertThat(datasourceLastUpdate).isBefore(getDatasourceLastUpdate());
    assertThat(tableLastUpdate).isBefore(getTableLastUpdate(TABLE));
  }

  @Test
  public void test_timestamps_removing_variable() throws Exception {

    final Variable variable = Variable.Builder.newVariable("Test Variable", IntegerType.get(), PARTICIPANT).build();

    transactionTemplate.execute(new TransactionCallbackRuntimeExceptions() {
      @Override
      protected void doAction(TransactionStatus status) throws Exception {
        HibernateDatasource ds = createDatasource();
        ValueTable generatedValueTable = new GeneratedValueTable(ds, ImmutableSet.of(variable), 1);
        MagmaEngine.get().addDatasource(ds);
        DatasourceCopier.Builder.newCopier().build().copy(generatedValueTable, TABLE, ds);
      }
    });

    Thread.sleep(1000);

    Date datasourceLastUpdate = getDatasourceStateLastUpdate();
    Date tableLastUpdate = getTableLastUpdate(TABLE);

    // remove variable
    transactionTemplate.execute(new TransactionCallbackRuntimeExceptions() {
      @Override
      protected void doAction(TransactionStatus status) throws Exception {
        HibernateDatasource ds = getDatasource();
        try(ValueTableWriter tableWriter = ds.createWriter(TABLE, PARTICIPANT);
            VariableWriter variableWriter = tableWriter.writeVariables()) {
          variableWriter.removeVariable(variable);
        }
      }
    });

    assertThat(datasourceLastUpdate).isEqualTo(getDatasourceStateLastUpdate());
    assertThat(datasourceLastUpdate).isBefore(getDatasourceLastUpdate());
    assertThat(tableLastUpdate).isBefore(getTableLastUpdate(TABLE));
  }

  @Test
  public void test_timestamps_removing_table() throws Exception {

    final Variable variable = Variable.Builder.newVariable("Test Variable", IntegerType.get(), PARTICIPANT).build();

    transactionTemplate.execute(new TransactionCallbackRuntimeExceptions() {
      @Override
      protected void doAction(TransactionStatus status) throws Exception {
        HibernateDatasource ds = createDatasource();
        ValueTable generatedValueTable = new GeneratedValueTable(ds, ImmutableSet.of(variable), 1);
        MagmaEngine.get().addDatasource(ds);
        DatasourceCopier.Builder.newCopier().build().copy(generatedValueTable, TABLE, ds);
      }
    });

    Thread.sleep(1000);

    Date datasourceLastUpdate = getDatasourceStateLastUpdate();

    // drop table
    transactionTemplate.execute(new TransactionCallbackRuntimeExceptions() {
      @Override
      protected void doAction(TransactionStatus status) throws Exception {
        HibernateDatasource ds = getDatasource();
        ds.dropTable(TABLE);
      }
    });

    assertThat(datasourceLastUpdate).isBefore(getDatasourceStateLastUpdate());
    assertThat(datasourceLastUpdate).isBefore(getDatasourceLastUpdate());
  }

  @Test
  public void test_timestamps_removing_table_valuesets() throws Exception {

    final Variable variable = Variable.Builder.newVariable("Test Variable", IntegerType.get(), PARTICIPANT).build();

    transactionTemplate.execute(new TransactionCallbackRuntimeExceptions() {
      @Override
      protected void doAction(TransactionStatus status) throws Exception {
        HibernateDatasource ds = createDatasource();
        ValueTable generatedValueTable = new GeneratedValueTable(ds, ImmutableSet.of(variable), 1);
        MagmaEngine.get().addDatasource(ds);
        DatasourceCopier.Builder.newCopier().build().copy(generatedValueTable, TABLE, ds);
      }
    });

    Thread.sleep(1000);

    Date datasourceLastUpdate = getDatasourceStateLastUpdate();
    Date tableLastUpdate = getTableLastUpdate(TABLE);

    // drop table value sets
    transactionTemplate.execute(new TransactionCallbackRuntimeExceptions() {
      @Override
      protected void doAction(TransactionStatus status) throws Exception {
        HibernateDatasource ds = getDatasource();
        ds.getValueTable(TABLE).dropValueSets();
      }
    });

    assertThat(tableLastUpdate).isBefore(getTableLastUpdate(TABLE));
    assertThat(datasourceLastUpdate).isBefore(getDatasourceLastUpdate());
  }

  private Date getDatasourceStateLastUpdate() {
    return transactionTemplate.execute(new TransactionCallback<Date>() {
      @Override
      public Date doInTransaction(TransactionStatus status) {
        return getDatasource().getDatasourceState().getUpdated();
      }
    });
  }

  private Date getDatasourceCreated() {
    return transactionTemplate.execute(new TransactionCallback<Date>() {
      @Override
      public Date doInTransaction(TransactionStatus status) {
        return (Date) getDatasource().getTimestamps().getCreated().getValue();
      }
    });
  }

  private Date getDatasourceLastUpdate() {
    return transactionTemplate.execute(new TransactionCallback<Date>() {
      @Override
      public Date doInTransaction(TransactionStatus status) {
        return (Date) getDatasource().getTimestamps().getLastUpdate().getValue();
      }
    });
  }

  private Date getTableCreated(final String tableName) {
    return transactionTemplate.execute(new TransactionCallback<Date>() {
      @Override
      public Date doInTransaction(TransactionStatus status) {
        return (Date) getDatasource().getValueTable(tableName).getTimestamps().getCreated().getValue();
      }
    });
  }

  private Date getTableLastUpdate(final String tableName) {
    return transactionTemplate.execute(new TransactionCallback<Date>() {
      @Override
      public Date doInTransaction(TransactionStatus status) {
        return (Date) getDatasource().getValueTable(tableName).getTimestamps().getLastUpdate().getValue();
      }
    });
  }

  @Test
  public void test_drop_datasource() {

    transactionTemplate.execute(new TransactionCallbackRuntimeExceptions() {
      @Override
      protected void doAction(TransactionStatus status) throws Exception {
        HibernateDatasource ds = createDatasource();
        ds.setAttributeValue("test", TextType.get().valueOf("att1"));
        ImmutableSet<Variable> variables = ImmutableSet.of(//
            Variable.Builder.newVariable("Test Variable", IntegerType.get(), PARTICIPANT).build(), //
            Variable.Builder.newVariable("Other Variable", DecimalType.get(), PARTICIPANT).build());

        ValueTable generatedValueTable = new GeneratedValueTable(ds, variables, 10);
        MagmaEngine.get().addDatasource(ds);
        DatasourceCopier.Builder.newCopier().build().copy(generatedValueTable, TABLE, ds);
      }
    });

    transactionTemplate.execute(new TransactionCallbackRuntimeExceptions() {
      @Override
      protected void doAction(TransactionStatus status) throws Exception {
        HibernateDatasource ds = getDatasource();
        assertThat(ds.getAttributes()).hasSize(1);
        assertThat(ds.canDrop()).isTrue();

        Session session = ds.getSessionFactory().getCurrentSession();
        assertJpaEntitiesHasSize(session, DatasourceState.class, 1);
        assertJpaEntitiesHasSize(session, ValueTableState.class, 1);
        assertJpaEntitiesHasSize(session, VariableState.class, 2);
        assertJpaEntitiesHasSize(session, ValueSetState.class, 10);
        assertJpaEntitiesHasSize(session, ValueSetValue.class, 20);

        ds.drop();
      }
    });

    transactionTemplate.execute(new TransactionCallbackRuntimeExceptions() {
      @Override
      protected void doAction(TransactionStatus status) throws Exception {
        Session session = getDatasource().getSessionFactory().getCurrentSession();
        assertEmptyJpaEntities(session, DatasourceState.class);
        assertEmptyJpaEntities(session, ValueTableState.class);
        assertEmptyJpaEntities(session, VariableState.class);
        assertEmptyJpaEntities(session, ValueSetState.class);
        assertEmptyJpaEntities(session, ValueSetValue.class);
      }
    });
  }

  @Test
  public void test_drop_table() {

    transactionTemplate.execute(new TransactionCallbackRuntimeExceptions() {
      @Override
      protected void doAction(TransactionStatus status) throws Exception {
        HibernateDatasource ds = createDatasource();
        ImmutableSet<Variable> variables = ImmutableSet.of(//
            Variable.Builder.newVariable("Test Variable", IntegerType.get(), PARTICIPANT).build(), //
            Variable.Builder.newVariable("Other Variable", DecimalType.get(), PARTICIPANT).build());

        ValueTable generatedValueTable = new GeneratedValueTable(ds, variables, 10);
        MagmaEngine.get().addDatasource(ds);
        DatasourceCopier.Builder.newCopier().build().copy(generatedValueTable, TABLE, ds);
      }
    });

    transactionTemplate.execute(new TransactionCallbackRuntimeExceptions() {
      @Override
      protected void doAction(TransactionStatus status) throws Exception {
        HibernateDatasource ds = getDatasource();
        assertThat(ds.canDropTable(TABLE)).isTrue();

        Session session = ds.getSessionFactory().getCurrentSession();
        assertJpaEntitiesHasSize(session, DatasourceState.class, 1);
        assertJpaEntitiesHasSize(session, ValueTableState.class, 1);
        assertJpaEntitiesHasSize(session, VariableState.class, 2);
        assertJpaEntitiesHasSize(session, ValueSetState.class, 10);
        assertJpaEntitiesHasSize(session, ValueSetValue.class, 20);

        ds.dropTable(TABLE);
      }
    });

    transactionTemplate.execute(new TransactionCallbackRuntimeExceptions() {
      @Override
      protected void doAction(TransactionStatus status) throws Exception {
        HibernateDatasource ds = getDatasource();
        try {
          ds.getValueTable(TABLE);
          fail("Should throw NoSuchValueTableException");
        } catch(NoSuchValueTableException ignored) {
        }

        Session session = ds.getSessionFactory().getCurrentSession();
        assertJpaEntitiesHasSize(session, DatasourceState.class, 1);
        assertEmptyJpaEntities(session, ValueTableState.class);
        assertEmptyJpaEntities(session, VariableState.class);
        assertEmptyJpaEntities(session, ValueSetState.class);
        assertEmptyJpaEntities(session, ValueSetValue.class);
      }
    });
  }

  @Test
  public void test_drop_table_valuesets() {

    transactionTemplate.execute(new TransactionCallbackRuntimeExceptions() {
      @Override
      protected void doAction(TransactionStatus status) throws Exception {
        HibernateDatasource ds = createDatasource();
        ImmutableSet<Variable> variables = ImmutableSet.of(//
            Variable.Builder.newVariable("Test Variable", IntegerType.get(), PARTICIPANT).build(), //
            Variable.Builder.newVariable("Other Variable", DecimalType.get(), PARTICIPANT).build());

        ValueTable generatedValueTable = new GeneratedValueTable(ds, variables, 10);
        MagmaEngine.get().addDatasource(ds);
        DatasourceCopier.Builder.newCopier().build().copy(generatedValueTable, TABLE, ds);
      }
    });

    transactionTemplate.execute(new TransactionCallbackRuntimeExceptions() {
      @Override
      protected void doAction(TransactionStatus status) throws Exception {
        HibernateDatasource ds = getDatasource();
        assertThat(ds.getValueTable(TABLE).canDropValueSets()).isTrue();

        Session session = ds.getSessionFactory().getCurrentSession();
        assertJpaEntitiesHasSize(session, DatasourceState.class, 1);
        assertJpaEntitiesHasSize(session, ValueTableState.class, 1);
        assertJpaEntitiesHasSize(session, VariableState.class, 2);
        assertJpaEntitiesHasSize(session, ValueSetState.class, 10);
        assertJpaEntitiesHasSize(session, ValueSetValue.class, 20);

        ds.getValueTable(TABLE).dropValueSets();
      }
    });

    transactionTemplate.execute(new TransactionCallbackRuntimeExceptions() {
      @Override
      protected void doAction(TransactionStatus status) throws Exception {
        HibernateDatasource ds = getDatasource();

        assertThat(ds.getValueTable(TABLE).getVariableEntityCount()).isEqualTo(0);
        assertThat(ds.getValueTable(TABLE).getVariableEntities()).hasSize(0);

        Session session = ds.getSessionFactory().getCurrentSession();
        assertJpaEntitiesHasSize(session, DatasourceState.class, 1);
        assertJpaEntitiesHasSize(session, ValueTableState.class, 1);
        assertJpaEntitiesHasSize(session, VariableState.class, 2);
        assertEmptyJpaEntities(session, ValueSetState.class);
        assertEmptyJpaEntities(session, ValueSetValue.class);
      }
    });
  }

  @Test
  public void test_remove_variable() {

    final ImmutableSet<Variable> variables = ImmutableSet.of(//
        Variable.Builder.newVariable("Variable to delete", IntegerType.get(), PARTICIPANT) //
            .addCategory("1", "One", false) //
            .build(), //
        Variable.Builder.newVariable("Other Variable", IntegerType.get(), PARTICIPANT) //
            .addCategory("2", "Two", false) //
            .build());

    transactionTemplate.execute(new TransactionCallbackRuntimeExceptions() {
      @Override
      protected void doAction(TransactionStatus status) throws Exception {
        HibernateDatasource ds = createDatasource();
        ValueTable generatedValueTable = new GeneratedValueTable(ds, variables, 10);
        MagmaEngine.get().addDatasource(ds);
        DatasourceCopier.Builder.newCopier().build().copy(generatedValueTable, TABLE, ds);
      }
    });

    transactionTemplate.execute(new TransactionCallbackRuntimeExceptions() {
      @Override
      protected void doAction(TransactionStatus status) throws Exception {
        HibernateDatasource ds = getDatasource();
        ValueTable table = ds.getValueTable(TABLE);

        assertThat(table.getVariables()).hasSize(2);

        Session session = ds.getSessionFactory().getCurrentSession();
        assertJpaEntitiesHasSize(session, VariableState.class, 2);
        assertJpaEntitiesHasSize(session, ValueSetState.class, 10);
        assertJpaEntitiesHasSize(session, ValueSetValue.class, 20);
        assertJpaEntitiesHasSize(session, CategoryState.class, 2);

        ds.createWriter(TABLE, PARTICIPANT).writeVariables().removeVariable(table.getVariable("Variable to delete"));
      }
    });

    transactionTemplate.execute(new TransactionCallbackRuntimeExceptions() {
      @Override
      protected void doAction(TransactionStatus status) throws Exception {
        HibernateDatasource ds = getDatasource();
        try {
          ds.getValueTable(TABLE).getVariable("Variable to delete");
          fail("Should throw NoSuchVariableException");
        } catch(NoSuchVariableException ignored) {
        }

        Variable found = ds.getValueTable(TABLE).getVariable("Other Variable");

        assertThat(found).isNotNull();
        assertThat(found.getCategories()).hasSize(1);

        Session session = ds.getSessionFactory().getCurrentSession();
        assertJpaEntitiesHasSize(session, VariableState.class, 1);
        assertJpaEntitiesHasSize(session, ValueSetState.class, 10);
        assertJpaEntitiesHasSize(session, ValueSetValue.class, 10);
        assertJpaEntitiesHasSize(session, CategoryState.class, 1);

      }
    });
  }

  @Test
  public void test_remove_valueset() {

    final ImmutableSet<Variable> variables = ImmutableSet.of(//
        Variable.Builder.newVariable("Variable to delete", IntegerType.get(), PARTICIPANT) //
            .addCategory("1", "One", false) //
            .build(), //
        Variable.Builder.newVariable("Other Variable", IntegerType.get(), PARTICIPANT) //
            .addCategory("2", "Two", false) //
            .build());

    final VariableEntity[] entity = new VariableEntity[1];

    transactionTemplate.execute(new TransactionCallbackRuntimeExceptions() {
      @Override
      protected void doAction(TransactionStatus status) throws Exception {
        HibernateDatasource ds = createDatasource();
        ValueTable generatedValueTable = new GeneratedValueTable(ds, variables, 10);
        MagmaEngine.get().addDatasource(ds);
        DatasourceCopier.Builder.newCopier().build().copy(generatedValueTable, TABLE, ds);
      }
    });

    transactionTemplate.execute(new TransactionCallbackRuntimeExceptions() {
      @Override
      protected void doAction(TransactionStatus status) throws Exception {
        HibernateDatasource ds = getDatasource();
        ValueTable table = ds.getValueTable(TABLE);

        assertThat(table.getVariableEntities()).hasSize(10);

        Session session = ds.getSessionFactory().getCurrentSession();
        assertJpaEntitiesHasSize(session, VariableState.class, 2);
        assertJpaEntitiesHasSize(session, ValueSetState.class, 10);
        assertJpaEntitiesHasSize(session, ValueSetValue.class, 20);

        entity[0] = table.getVariableEntities().iterator().next();
        ds.createWriter(TABLE, PARTICIPANT).writeValueSet(entity[0]).remove();
      }
    });

    transactionTemplate.execute(new TransactionCallbackRuntimeExceptions() {
      @Override
      protected void doAction(TransactionStatus status) throws Exception {
        HibernateDatasource ds = getDatasource();
        ValueTable table = ds.getValueTable(TABLE);
        try {
          table.getValueSet(entity[0]);
          fail("Should throw NoSuchValueSetException");
        } catch(NoSuchValueSetException ignored) {
        }

        assertThat(table.getVariableEntities()).hasSize(9);

        Session session = ds.getSessionFactory().getCurrentSession();
        assertJpaEntitiesHasSize(session, VariableState.class, 2);
        assertJpaEntitiesHasSize(session, ValueSetState.class, 9);
        assertJpaEntitiesHasSize(session, ValueSetValue.class, 18);
      }
    });
  }

  @Test
  public void test_remove_last_variable() {

    final Variable variable = Variable.Builder.newVariable("Variable 1", IntegerType.get(), PARTICIPANT).addCategory(
        "1", "One", false) //
        .addCategory("2", "Two", false) //
        .addAttribute("att1", "1") //
        .build();

    transactionTemplate.execute(new TransactionCallbackRuntimeExceptions() {
      @Override
      protected void doAction(TransactionStatus status) throws Exception {
        HibernateDatasource ds = createDatasource();
        ImmutableSet<Variable> variables = ImmutableSet.of(variable);

        ValueTable generatedValueTable = new GeneratedValueTable(ds, variables, 10);
        MagmaEngine.get().addDatasource(ds);
        DatasourceCopier.Builder.newCopier().build().copy(generatedValueTable, TABLE, ds);
      }
    });

    transactionTemplate.execute(new TransactionCallbackRuntimeExceptions() {
      @Override
      protected void doAction(TransactionStatus status) throws Exception {
        HibernateDatasource ds = getDatasource();
        ValueTable table = ds.getValueTable(TABLE);
        Variable found = table.getVariable(variable.getName());

        assertThat(table.getVariables()).hasSize(1);

        assertThat(found).isNotNull();

        Session session = ds.getSessionFactory().getCurrentSession();
        assertJpaEntitiesHasSize(session, VariableState.class, 1);
        assertJpaEntitiesHasSize(session, ValueSetState.class, 10);
        assertJpaEntitiesHasSize(session, ValueSetValue.class, 10);
        assertJpaEntitiesHasSize(session, CategoryState.class, 2);

        ds.createWriter(TABLE, PARTICIPANT).writeVariables().removeVariable(found);
      }
    });

    transactionTemplate.execute(new TransactionCallbackRuntimeExceptions() {
      @Override
      protected void doAction(TransactionStatus status) throws Exception {
        HibernateDatasource ds = getDatasource();
        try {
          ds.getValueTable(TABLE).getVariable(variable.getName());
          fail("Should throw NoSuchVariableException");
        } catch(NoSuchVariableException ignored) {
        }

        Session session = ds.getSessionFactory().getCurrentSession();
        assertEmptyJpaEntities(session, ValueSetValue.class);
        assertEmptyJpaEntities(session, ValueSetState.class);
        assertEmptyJpaEntities(session, CategoryState.class);
        assertEmptyJpaEntities(session, VariableState.class);
      }
    });
  }

  @Test
  public void test_initialise_datasource() {
    transactionTemplate.execute(new TransactionCallbackRuntimeExceptions() {
      @Override
      protected void doAction(TransactionStatus status) throws Exception {
        HibernateDatasource ds = createDatasource();
        ImmutableSet<Variable> variables = ImmutableSet.of(//
            Variable.Builder.newVariable("Test Variable", IntegerType.get(), PARTICIPANT).build(), //
            Variable.Builder.newVariable("Other Variable", DecimalType.get(), PARTICIPANT).build());

        ValueTable generatedValueTable = new GeneratedValueTable(ds, variables, 50);
        MagmaEngine.get().addDatasource(ds);
        DatasourceCopier.Builder.newCopier().build().copy(generatedValueTable, TABLE, ds);
      }
    });

    transactionTemplate.execute(new TransactionCallbackRuntimeExceptions() {
      @Override
      protected void doAction(TransactionStatus status) throws Exception {
        HibernateDatasource ds = getDatasource();
        assertThat(ds.getValueTables()).hasSize(1);

        Session session = ds.getSessionFactory().getCurrentSession();
        assertJpaEntitiesHasSize(session, DatasourceState.class, 1);
        assertJpaEntitiesHasSize(session, ValueTableState.class, 1);
        assertJpaEntitiesHasSize(session, VariableState.class, 2);
        assertJpaEntitiesHasSize(session, ValueSetState.class, 50);
        assertJpaEntitiesHasSize(session, ValueSetValue.class, 100);
      }
    });
  }

  @Test
  public void test_rename_table() throws InterruptedException {

    transactionTemplate.execute(new TransactionCallbackRuntimeExceptions() {
      @Override
      protected void doAction(TransactionStatus status) throws Exception {
        HibernateDatasource ds = createDatasource();
        ImmutableSet<Variable> variables = ImmutableSet.of(//
            Variable.Builder.newVariable("Test Variable", IntegerType.get(), PARTICIPANT).build(), //
            Variable.Builder.newVariable("Other Variable", DecimalType.get(), PARTICIPANT).build());

        ValueTable generatedValueTable = new GeneratedValueTable(ds, variables, 50);
        MagmaEngine.get().addDatasource(ds);
        DatasourceCopier.Builder.newCopier().build().copy(generatedValueTable, TABLE, ds);
      }
    });

    final String NEW_NAME = "new_table";
    final Date[] created = new Date[3];
    final Date[] updated = new Date[3];

    Thread.sleep(500);

    transactionTemplate.execute(new TransactionCallbackRuntimeExceptions() {
      @Override
      protected void doAction(TransactionStatus status) throws Exception {
        HibernateDatasource ds = getDatasource();
        created[0] = getDatasourceCreated();
        created[1] = getTableCreated(TABLE);

        updated[0] = getDatasourceLastUpdate();
        updated[1] = getTableLastUpdate(TABLE);

        ds.renameTable(TABLE, NEW_NAME);
      }
    });

    transactionTemplate.execute(new TransactionCallbackRuntimeExceptions() {
      @Override
      protected void doAction(TransactionStatus status) throws Exception {
        HibernateDatasource ds = getDatasource();
        ds.getValueTable(NEW_NAME).getVariable("Test Variable");

        // Created timestamps have not changed
        assertThat(created[0]).isEqualTo(getDatasourceCreated());
        assertThat(created[1]).isEqualTo(getTableCreated(NEW_NAME));

        // LastUpdated timestamps have changed
        assertThat(updated[0]).isBefore(getDatasourceLastUpdate());
        assertThat(updated[1]).isBefore(getTableLastUpdate(NEW_NAME));
      }
    });

    transactionTemplate.execute(new TransactionCallbackRuntimeExceptions() {
      @Override
      protected void doAction(TransactionStatus status) throws Exception {
        try {
          HibernateDatasource ds = getDatasource();
          ds.getValueTable(TABLE);
          fail("Should fail with NoSuchValueTableException");
        } catch(NoSuchValueTableException ignored) {
        }
      }
    });
  }

  @Test
  public void test_update_variable() throws IOException {

    Variable variable1 = Variable.Builder.newVariable("Variable to update", IntegerType.get(), PARTICIPANT) //
        .unit("kg").addCategory("1", "One", false) //
        .build();
    final Variable variable2 = Variable.Builder.newVariable("Other Variable", IntegerType.get(), PARTICIPANT) //
        .addCategory("2", "Two", false) //
        .build();
    final ImmutableSet<Variable> variables = ImmutableSet.of(variable1, variable2);

    transactionTemplate.execute(new TransactionCallbackRuntimeExceptions() {
      @Override
      protected void doAction(TransactionStatus status) throws Exception {
        HibernateDatasource ds = createDatasource();
        ValueTable generatedValueTable = new GeneratedValueTable(ds, variables, 5);
        MagmaEngine.get().addDatasource(ds);
        DatasourceCopier.Builder.newCopier().build().copy(generatedValueTable, TABLE, ds);
      }
    });

    final Variable newVariable = Variable.Builder.newVariable("Variable to update", IntegerType.get(), PARTICIPANT) //
        .unit("g").addCategory("1", "One", false) //
        .addCategory("2", "Two", false) //
        .build();

    transactionTemplate.execute(new TransactionCallbackRuntimeExceptions() {
      @Override
      protected void doAction(TransactionStatus status) throws Exception {
        HibernateDatasource ds = getDatasource();
        try(ValueTableWriter tableWriter = ds.createWriter(TABLE, PARTICIPANT);
            ValueTableWriter.VariableWriter variableWriter = tableWriter.writeVariables()) {
          variableWriter.writeVariable(newVariable);
        }
      }
    });

    transactionTemplate.execute(new TransactionCallbackRuntimeExceptions() {
      @Override
      protected void doAction(TransactionStatus status) throws Exception {
        HibernateDatasource ds = getDatasource();
        ValueTable table = ds.getValueTable(TABLE);
        assertThat(table.getVariables()).hasSize(2);

        Variable variable = table.getVariable("Variable to update");
        assertThat(variable.getUnit()).isEqualTo(newVariable.getUnit());
        assertThat(variable.getCategories()).hasSize(newVariable.getCategories().size());

        List<Variable> foundVariables = Lists.newArrayList(table.getVariables());
        assertThat(foundVariables.indexOf(newVariable)).isEqualTo(0);
        assertThat(foundVariables.indexOf(variable2)).isEqualTo(1);
      }
    });
  }

  @Test
  public void test_count_variables() {

    final List<Variable> variables = new ArrayList<>();
    for(int i = 0; i < 100; i++) {
      variables.add(Variable.Builder.newVariable("Variable " + i, IntegerType.get(), PARTICIPANT).build());
    }

    transactionTemplate.execute(new TransactionCallbackRuntimeExceptions() {
      @Override
      protected void doAction(TransactionStatus status) throws Exception {
        HibernateDatasource ds = createDatasource();
        ValueTable generatedValueTable = new GeneratedValueTable(ds, variables, 50);
        MagmaEngine.get().addDatasource(ds);
        DatasourceCopier.Builder.newCopier().build().copy(generatedValueTable, TABLE, ds);
      }
    });

    transactionTemplate.execute(new TransactionCallbackRuntimeExceptions() {
      @Override
      protected void doAction(TransactionStatus status) throws Exception {
        HibernateDatasource ds = getDatasource();
        assertThat(ds.getValueTable(TABLE).getVariableCount()).isEqualTo(100);
      }
    });
  }

  @Test
  public void test_count_valueSets() {

    final ImmutableSet<Variable> variables = ImmutableSet.of(//
        Variable.Builder.newVariable("Test Variable", IntegerType.get(), PARTICIPANT).build(), //
        Variable.Builder.newVariable("Other Variable", DecimalType.get(), PARTICIPANT).build());

    transactionTemplate.execute(new TransactionCallbackRuntimeExceptions() {
      @Override
      protected void doAction(TransactionStatus status) throws Exception {
        HibernateDatasource ds = createDatasource();
        ValueTable generatedValueTable = new GeneratedValueTable(ds, variables, 100);
        MagmaEngine.get().addDatasource(ds);
        DatasourceCopier.Builder.newCopier().build().copy(generatedValueTable, TABLE, ds);
      }
    });

    transactionTemplate.execute(new TransactionCallbackRuntimeExceptions() {
      @Override
      protected void doAction(TransactionStatus status) throws Exception {
        HibernateDatasource ds = getDatasource();
        assertThat(ds.getValueTable(TABLE).getValueSetCount()).isEqualTo(100);
      }
    });
  }

  @Test
  // See http://jira.obiba.org/jira/browse/OPAL-2423
  public void test_get_binary_values_as_vector() throws IOException {
    final ImmutableSet<Variable> variables = ImmutableSet.of(//
        Variable.Builder.newVariable("V1", BinaryType.get(), PARTICIPANT).build()); //

    transactionTemplate.execute(new TransactionCallbackRuntimeExceptions() {
      @Override
      protected void doAction(TransactionStatus status) throws Exception {
        HibernateDatasource ds = createDatasource();
        ValueTable generatedValueTable = new GeneratedValueTable(ds, variables, 1);
        MagmaEngine.get().addDatasource(ds);
        DatasourceCopier.Builder.newCopier().build().copy(generatedValueTable, TABLE, ds);
      }
    });

    transactionTemplate.execute(new TransactionCallbackRuntimeExceptions() {
      @Override
      protected void doAction(TransactionStatus status) throws Exception {
        HibernateDatasource ds = getDatasource();

        VariableValueSource variableValueSource = ds.getValueTable(TABLE).getVariableValueSource("V1");
        assertThat(variableValueSource.getValueType().getName().equals(BinaryType.get().getName()));

        TreeSet<VariableEntity> entities = Sets.newTreeSet(ds.getValueTable(TABLE).getVariableEntities());
        Iterable<Value> values = variableValueSource.asVectorSource().getValues(entities);
        for(Value value : values) {
          assertThat(value.getValue()).isNotNull();
          assertThat(value.getValueType().getName()).isEqualTo(BinaryType.get().getName());
        }
      }
    });
  }

  private void assertSameCategories(Variable expected, Variable actual) {
    List<Category> expectedCategories = Lists.newArrayList(expected.getCategories());
    List<Category> actualCategories = Lists.newArrayList(actual.getCategories());
    assertThat(actual.getCategories()).hasSize(expected.getCategories().size());
    for(int i = 0; i < expectedCategories.size(); i++) {
      assertThat(actualCategories.get(i).getName()).isEqualTo(expectedCategories.get(i).getName());
      assertThat(actualCategories.get(i).getCode()).isEqualTo(expectedCategories.get(i).getCode());
    }
  }

  private HibernateDatasource createDatasource() {
    HibernateDatasource datasource = new HibernateDatasource(DATASOURCE, sessionFactory);
    Initialisables.initialise(datasource);
    return datasource;
  }

  private HibernateDatasource getDatasource() {
    return (HibernateDatasource) MagmaEngine.get().getDatasource(DATASOURCE);
  }

  private void assertEmptyJpaEntities(@SuppressWarnings("TypeMayBeWeakened") Session session, Class<?> entityClass) {
    assertThat((List<?>) session.createCriteria(entityClass).list()).isEmpty();
  }

  private void assertJpaEntitiesHasSize(@SuppressWarnings("TypeMayBeWeakened") Session session, Class<?> entityClass,
      int expectedSize) {
    assertThat((List<?>) session.createCriteria(entityClass).list()).hasSize(expectedSize);
  }

  private void cleanlyRemoveDatasource(final boolean drop) {

    transactionTemplate.execute(new TransactionCallbackWithoutResult() {
      @Override
      protected void doInTransactionWithoutResult(TransactionStatus status) {
        try {
          if(drop) {
            HibernateDatasource datasource = createDatasource();
            MagmaEngine.get().addDatasource(datasource);
            datasource.drop();
          }
          MagmaEngine.get().removeDatasource(MagmaEngine.get().getDatasource(DATASOURCE));
        } catch(Throwable e) {
          log.warn("Cannot remove datasource", e);
        }
      }
    });
  }

  private abstract static class TestThread extends Thread {

    private Throwable threadException;

    @Override
    public void run() {
      try {
        test();
      } catch(Throwable t) {
        threadException = t;
      }
    }

    public void assertNoException() throws InterruptedException {
      start();
      join();
      assertThat(threadException).isNull();
    }

    abstract protected void test();

  }

}
