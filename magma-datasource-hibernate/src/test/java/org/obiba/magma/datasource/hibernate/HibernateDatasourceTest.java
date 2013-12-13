package org.obiba.magma.datasource.hibernate;

import java.util.Date;
import java.util.List;
import java.util.SortedSet;

import org.hamcrest.core.Is;
import org.hamcrest.core.IsNull;
import org.hibernate.SessionFactory;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.obiba.magma.Category;
import org.obiba.magma.MagmaEngine;
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
import org.obiba.magma.support.DatasourceCopier;
import org.obiba.magma.support.Initialisables;
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

import uk.co.it.modular.hamcrest.date.DateMatchers;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.core.IsInstanceOf.instanceOf;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
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

    transactionTemplate.execute(new TransactionCallbackWithoutResult() {
      @Override
      protected void doInTransactionWithoutResult(TransactionStatus status) {
        try {
          HibernateDatasource ds = createDatasource();

          MagmaEngine.get().addDatasource(ds);
          ValueTableWriter vtWriter = ds.createWriter(TABLE, PARTICIPANT);
          vtWriter.close();

          assertThat(ds.hasValueTable(TABLE), is(true));
          assertThat(ds.getValueTable(TABLE), notNullValue());

          // Make sure the table is not visible outside this transaction.
          new TestThread() {
            @Override
            public void test() {
              // Assert that the datasource does not have the value table
              assertThat(MagmaEngine.get().getDatasource(DATASOURCE).hasValueTable(TABLE), is(false));
            }
          }.assertNoException();

        } catch(Exception e) {
          fail(e.getMessage());
          log.error("Exception", e);
          throw new RuntimeException(e);
        }
      }
    });

    // Make sure the table is visible outside this transaction.
    transactionTemplate.execute(new TransactionCallbackWithoutResult() {
      @Override
      protected void doInTransactionWithoutResult(TransactionStatus status) {
        try {
          new TestThread() {
            @Override
            public void test() {
              assertThat(MagmaEngine.get().getDatasource(DATASOURCE).hasValueTable(TABLE), is(true));
            }
          }.assertNoException();
        } catch(InterruptedException e) {
          fail(e.getMessage());
          throw new RuntimeException(e);
        }
      }
    });
  }

  @Test
  public void test_table_and_variables_persisted() {

    transactionTemplate.execute(new TransactionCallbackWithoutResult() {
      @Override
      protected void doInTransactionWithoutResult(TransactionStatus status) {
        try {
          HibernateDatasource ds = createDatasource();

          MagmaEngine.get().addDatasource(ds);

          ValueTableWriter tableWriter = ds.createWriter(TABLE, PARTICIPANT);

          // Test that the table is visible
          Assert.assertTrue(ds.hasValueTable(TABLE));

          // Write variables and assert that they are visible.
          VariableWriter variableWriter = tableWriter.writeVariables();
          variableWriter.writeVariable(Variable.Builder.newVariable("Var1", TextType.get(), PARTICIPANT).build());
          assertNotNull(ds.getValueTable(TABLE).getVariable("Var1"));
          variableWriter.writeVariable(Variable.Builder.newVariable("Var2", IntegerType.get(), PARTICIPANT).build());
          assertNotNull(ds.getValueTable(TABLE).getVariable("Var2"));
          variableWriter.close();
          tableWriter.close();
        } catch(Exception e) {
          fail(e.getMessage());
          throw new RuntimeException(e);
        }
      }
    });

    cleanlyRemoveDatasource(false);

    // Re-create same datasource and assert that everything is still there.
    transactionTemplate.execute(new TransactionCallbackWithoutResult() {
      @Override
      protected void doInTransactionWithoutResult(TransactionStatus status) {
        try {
          HibernateDatasource ds = createDatasource();
          MagmaEngine.get().addDatasource(ds);
          assertNotNull(ds.getValueTable(TABLE));
          assertNotNull(ds.getValueTable(TABLE).getVariable("Var1"));
          assertNotNull(ds.getValueTable(TABLE).getVariable("Var2"));
        } catch(Exception e) {
          fail(e.getMessage());
          throw new RuntimeException(e);
        }
      }
    });

  }

  @Test
  public void test_variable_state_change_is_persisted() {
    final Variable initialState = Variable.Builder.newVariable("Var1", TextType.get(), PARTICIPANT)
        .addCategory("C1", "1").addCategory("C2", "2").addCategory("C3", "3").build();
    final Variable changedState = Variable.Builder.newVariable("Var1", TextType.get(), PARTICIPANT)
        .addCategory("C3", "3").addCategory("C1", "1").addCategory("C4", "4").addCategory("C2", "2").build();

    transactionTemplate.execute(new TransactionCallbackWithoutResult() {
      @Override
      protected void doInTransactionWithoutResult(TransactionStatus status) {
        try {
          HibernateDatasource ds = createDatasource();

          MagmaEngine.get().addDatasource(ds);

          ValueTableWriter vtWriter = ds.createWriter(TABLE, PARTICIPANT);

          // Write variables and assert that they are visible.
          VariableWriter vw = vtWriter.writeVariables();
          vw.writeVariable(initialState);
          vw.close();
          vtWriter.close();
        } catch(Exception e) {
          fail(e.getMessage());
          throw new RuntimeException(e);
        }
      }
    });

    transactionTemplate.execute(new TransactionCallbackWithoutResult() {
      @Override
      protected void doInTransactionWithoutResult(TransactionStatus status) {
        try {

          HibernateDatasource ds = getDatasource();

          Variable v = ds.getValueTable(TABLE).getVariable("Var1");
          assertSameCategories(initialState, v);

          ValueTableWriter vtWriter = ds.createWriter(TABLE, PARTICIPANT);
          // Write variables and assert that they are visible.
          VariableWriter vw = vtWriter.writeVariables();
          vw.writeVariable(changedState);
          vw.close();
          vtWriter.close();
        } catch(Exception e) {
          fail(e.getMessage());
          throw new RuntimeException(e);
        }
      }
    });

    transactionTemplate.execute(new TransactionCallbackWithoutResult() {
      @Override
      protected void doInTransactionWithoutResult(TransactionStatus status) {
        try {
          HibernateDatasource ds = getDatasource();
          Variable v = ds.getValueTable(TABLE).getVariable("Var1");
          assertSameCategories(changedState, v);
        } catch(Exception e) {
          fail(e.getMessage());
          throw new RuntimeException(e);
        }
      }
    });

  }

  @Test
  public void test_write() {

    transactionTemplate.execute(new TransactionCallbackWithoutResult() {
      @Override
      protected void doInTransactionWithoutResult(TransactionStatus status) {
        try {
          HibernateDatasource ds = createDatasource();

          ImmutableSet<Variable> variables = ImmutableSet.of( //
              Variable.Builder.newVariable("Test Variable", IntegerType.get(), PARTICIPANT).build(), //
              Variable.Builder.newVariable("Other Variable", DecimalType.get(), PARTICIPANT).build());

          ValueTable generatedValueTable = new GeneratedValueTable(ds, variables, 50);
          MagmaEngine.get().addDatasource(ds);
          DatasourceCopier.Builder.newCopier().build().copy(generatedValueTable, ds);
        } catch(Exception e) {
          fail(e.getMessage());
          throw new RuntimeException(e);
        }
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

    transactionTemplate.execute(new TransactionCallbackWithoutResult() {
      @Override
      protected void doInTransactionWithoutResult(TransactionStatus status) {
        try {
          HibernateDatasource ds = createDatasource();

          ValueTable generatedValueTable = new GeneratedValueTable(ds, variables, 50);
          MagmaEngine.get().addDatasource(ds);
          DatasourceCopier.Builder.newCopier().build().copy(generatedValueTable, "NewTable", ds);
        } catch(Exception e) {
          fail(e.getMessage());
          throw new RuntimeException(e);
        }
      }
    });

    transactionTemplate.execute(new TransactionCallbackWithoutResult() {
      @Override
      protected void doInTransactionWithoutResult(TransactionStatus status) {
        try {
          HibernateDatasource ds = getDatasource();
          ValueTable valueTable = ds.getValueTable("NewTable");

          for(Variable variable : variables) {
            VariableValueSource valueSource = valueTable.getVariableValueSource(variable.getName());
            assertThat(valueSource, notNullValue());
            assertThat(valueSource.asVectorSource(), notNullValue());

            SortedSet<VariableEntity> entities = Sets.newTreeSet(valueTable.getVariableEntities());
            VectorSource vectorSource = valueSource.asVectorSource();
            assertThat(vectorSource, notNullValue());
            //noinspection ConstantConditions
            Iterable<Value> values = vectorSource.getValues(entities);
            assertThat(values, notNullValue());
            assertThat(Iterables.size(values), is(entities.size()));
            for(Value value : values) {
              assertThat(value, notNullValue());
              assertThat(value.isSequence(), is(variable.isRepeatable()));
            }
          }
        } catch(Exception e) {
          fail(e.getMessage());
          throw new RuntimeException(e);
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

    transactionTemplate.execute(new TransactionCallbackWithoutResult() {
      @Override
      protected void doInTransactionWithoutResult(TransactionStatus status) {
        try {
          HibernateDatasource ds = createDatasource();

          ValueTable generatedValueTable = new GeneratedValueTable(ds, variables, 1);
          MagmaEngine.get().addDatasource(ds);
          DatasourceCopier.Builder.newCopier().build().copy(generatedValueTable, "NewTable", ds);
        } catch(Exception e) {
          fail(e.getMessage());
          throw new RuntimeException(e);
        }
      }
    });

    transactionTemplate.execute(new TransactionCallbackWithoutResult() {
      @Override
      protected void doInTransactionWithoutResult(TransactionStatus status) {
        try {
          HibernateDatasource ds = getDatasource();
          ValueTable valueTable = ds.getValueTable("NewTable");

          for(Variable variable : variables) {

            VariableValueSource valueSource = valueTable.getVariableValueSource(variable.getName());
            assertThat(valueSource, notNullValue());
            assertThat(valueSource.asVectorSource(), notNullValue());

            SortedSet<VariableEntity> entities = Sets.newTreeSet(valueTable.getVariableEntities());
            VectorSource vectorSource = valueSource.asVectorSource();
            assertThat(vectorSource, notNullValue());
            Iterable<Value> values = vectorSource.getValues(entities);
            assertThat(values, notNullValue());
            assertThat(Iterables.size(values), is(entities.size()));
            long length = BinaryValueGenerator.getLength();
            for(Value value : values) {
              assertThat(value.isNull(), is(false));
              assertThat(value.isSequence(), is(variable.isRepeatable()));
              if(value.isSequence()) {
                for(Value v : value.asSequence().getValue()) {
                  assertThat(v.getValue(), instanceOf(byte[].class));
                  assertThat(v.getLength(), is(length));
                }
              } else {
                assertThat(value.getValue(), instanceOf(byte[].class));
                assertThat(value.getLength(), is(length));
              }
            }
          }
        } catch(RuntimeException e) {
          fail(e.getMessage());
          throw e;
        } catch(Exception e) {
          fail(e.getMessage());
          throw new RuntimeException(e);
        }
      }
    });

  }

  @SuppressWarnings("ConstantConditions")
  @Test
  public void test_timestamps_adding_data() throws Exception {

    final ImmutableSet<Variable> variables = ImmutableSet.of( //
        Variable.Builder.newVariable("Test Variable", IntegerType.get(), PARTICIPANT).build(), //
        Variable.Builder.newVariable("Other Variable", DecimalType.get(), PARTICIPANT).build());

    transactionTemplate.execute(new TransactionCallbackWithoutResult() {
      @Override
      protected void doInTransactionWithoutResult(TransactionStatus status) {
        try {
          HibernateDatasource ds = createDatasource();
          ValueTable generatedValueTable = new GeneratedValueTable(ds, variables, 5);
          MagmaEngine.get().addDatasource(ds);
          DatasourceCopier.Builder.newCopier().build().copy(generatedValueTable, "NewTable", ds);
        } catch(Exception e) {
          fail(e.getMessage());
          throw new RuntimeException(e);
        }
      }
    });

    Thread.sleep(1000);

    transactionTemplate.execute(new TransactionCallbackWithoutResult() {
      @Override
      protected void doInTransactionWithoutResult(TransactionStatus status) {
        try {
          HibernateDatasource ds = getDatasource();
          ValueTable generatedValueTable = new GeneratedValueTable(ds, variables, 50);
          MagmaEngine.get().addDatasource(ds);
          DatasourceCopier.Builder.newCopier().build().copy(generatedValueTable, "NewTable", ds);
        } catch(Exception e) {
          fail(e.getMessage());
          throw new RuntimeException(e);
        }
      }
    });

    Thread.sleep(1000);

    transactionTemplate.execute(new TransactionCallbackWithoutResult() {
      @Override
      protected void doInTransactionWithoutResult(TransactionStatus status) {
        try {
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

          assertThat(lastValueSetUpdate, DateMatchers.sameOrBefore(tableLastUpdate));

          assertThat("Table create date should be before last update", tableCreated,
              DateMatchers.before(tableLastUpdate));
        } catch(Exception e) {
          fail(e.getMessage());
          throw new RuntimeException(e);
        }
      }
    });

  }

  @Test
  public void test_timestamps_adding_variable() throws Exception {

    transactionTemplate.execute(new TransactionCallbackWithoutResult() {
      @Override
      protected void doInTransactionWithoutResult(TransactionStatus status) {
        try {
          HibernateDatasource ds = createDatasource();
          ValueTable generatedValueTable = new GeneratedValueTable(ds,
              ImmutableSet.of(Variable.Builder.newVariable("Test Variable", IntegerType.get(), PARTICIPANT).build()),
              1);
          MagmaEngine.get().addDatasource(ds);
          DatasourceCopier.Builder.newCopier().build().copy(generatedValueTable, TABLE, ds);
        } catch(Exception e) {
          fail(e.getMessage());
          throw new RuntimeException(e);
        }
      }
    });

    Thread.sleep(1000);

    Date datasourceLastUpdate = getDatasourceStateLastUpdate();
    Date tableLastUpdate = getTableLastUpdate(TABLE);

    // add new variable
    transactionTemplate.execute(new TransactionCallbackWithoutResult() {
      @Override
      protected void doInTransactionWithoutResult(TransactionStatus status) {
        try {
          HibernateDatasource ds = getDatasource();

          ValueTableWriter tableWriter = ds.createWriter(TABLE, PARTICIPANT);
          VariableWriter variableWriter = tableWriter.writeVariables();
          variableWriter
              .writeVariable(Variable.Builder.newVariable("New Variable", TextType.get(), PARTICIPANT).build());
          variableWriter.close();
          tableWriter.close();

        } catch(Exception e) {
          fail(e.getMessage());
          throw new RuntimeException(e);
        }
      }
    });

    assertThat(datasourceLastUpdate, DateMatchers.sameMillisecond(getDatasourceStateLastUpdate()));
    assertThat(datasourceLastUpdate, DateMatchers.before(getDatasourceLastUpdate()));
    assertThat(tableLastUpdate, DateMatchers.before(getTableLastUpdate(TABLE)));
  }

  @Test
  public void test_timestamps_removing_variable() throws Exception {

    final Variable variable = Variable.Builder.newVariable("Test Variable", IntegerType.get(), PARTICIPANT).build();

    transactionTemplate.execute(new TransactionCallbackWithoutResult() {
      @Override
      protected void doInTransactionWithoutResult(TransactionStatus status) {
        try {
          HibernateDatasource ds = createDatasource();
          ValueTable generatedValueTable = new GeneratedValueTable(ds, ImmutableSet.of(variable), 1);
          MagmaEngine.get().addDatasource(ds);
          DatasourceCopier.Builder.newCopier().build().copy(generatedValueTable, TABLE, ds);
        } catch(Exception e) {
          fail(e.getMessage());
          throw new RuntimeException(e);
        }
      }
    });

    Thread.sleep(1000);

    Date datasourceLastUpdate = getDatasourceStateLastUpdate();
    Date tableLastUpdate = getTableLastUpdate(TABLE);

    // remove variable
    transactionTemplate.execute(new TransactionCallbackWithoutResult() {
      @Override
      protected void doInTransactionWithoutResult(TransactionStatus status) {
        try {
          HibernateDatasource ds = getDatasource();

          ValueTableWriter tableWriter = ds.createWriter(TABLE, PARTICIPANT);
          VariableWriter variableWriter = tableWriter.writeVariables();
          variableWriter.removeVariable(variable);
          variableWriter.close();
          tableWriter.close();

        } catch(Exception e) {
          fail(e.getMessage());
          throw new RuntimeException(e);
        }
      }
    });

    assertThat(datasourceLastUpdate, DateMatchers.sameMillisecond(getDatasourceStateLastUpdate()));
    assertThat(datasourceLastUpdate, DateMatchers.before(getDatasourceLastUpdate()));
    assertThat(tableLastUpdate, DateMatchers.before(getTableLastUpdate(TABLE)));
  }

  @Test
  public void test_timestamps_removing_table() throws Exception {

    final Variable variable = Variable.Builder.newVariable("Test Variable", IntegerType.get(), PARTICIPANT).build();

    transactionTemplate.execute(new TransactionCallbackWithoutResult() {
      @Override
      protected void doInTransactionWithoutResult(TransactionStatus status) {
        try {
          HibernateDatasource ds = createDatasource();
          ValueTable generatedValueTable = new GeneratedValueTable(ds, ImmutableSet.of(variable), 1);
          MagmaEngine.get().addDatasource(ds);
          DatasourceCopier.Builder.newCopier().build().copy(generatedValueTable, TABLE, ds);
        } catch(Exception e) {
          fail(e.getMessage());
          throw new RuntimeException(e);
        }
      }
    });

    Thread.sleep(1000);

    Date datasourceLastUpdate = getDatasourceStateLastUpdate();

    // drop table
    transactionTemplate.execute(new TransactionCallbackWithoutResult() {
      @Override
      protected void doInTransactionWithoutResult(TransactionStatus status) {
        try {
          HibernateDatasource ds = getDatasource();
          ds.dropTable(TABLE);
        } catch(Exception e) {
          fail(e.getMessage());
          throw new RuntimeException(e);
        }
      }
    });

    assertThat(datasourceLastUpdate, DateMatchers.before(getDatasourceStateLastUpdate()));
    assertThat(datasourceLastUpdate, DateMatchers.before(getDatasourceLastUpdate()));
  }

  private Date getDatasourceStateLastUpdate() {
    return transactionTemplate.execute(new TransactionCallback<Date>() {
      @Override
      public Date doInTransaction(TransactionStatus status) {
        return getDatasource().getDatasourceState().getUpdated();
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

  private Date getTableLastUpdate(final String tableName) {
    return transactionTemplate.execute(new TransactionCallback<Date>() {
      @Override
      public Date doInTransaction(TransactionStatus status) {
        return (Date) getDatasource().getValueTable(tableName).getTimestamps().getLastUpdate().getValue();
      }
    });
  }

  @Test
  public void test_remove_variable() {

    transactionTemplate.execute(new TransactionCallbackWithoutResult() {
      @Override
      protected void doInTransactionWithoutResult(TransactionStatus status) {
        try {
          HibernateDatasource ds = createDatasource();
          ImmutableSet<Variable> variables = ImmutableSet.of(//
              Variable.Builder.newVariable("Test Variable", IntegerType.get(), PARTICIPANT).build(), //
              Variable.Builder.newVariable("Other Variable", DecimalType.get(), PARTICIPANT).build());

          ValueTable generatedValueTable = new GeneratedValueTable(ds, variables, 50);
          MagmaEngine.get().addDatasource(ds);
          DatasourceCopier.Builder.newCopier().build().copy(generatedValueTable, TABLE, ds);
        } catch(Exception e) {
          fail(e.getMessage());
          throw new RuntimeException(e);
        }
      }
    });

    transactionTemplate.execute(new TransactionCallbackWithoutResult() {
      @Override
      protected void doInTransactionWithoutResult(TransactionStatus status) {
        try {
          HibernateDatasource ds = getDatasource();
          ValueTable table = ds.getValueTable(TABLE);
          Variable variable = table.getVariable("Test Variable");

          assertThat(Iterables.size(table.getVariables()), Is.is(2));
          assertThat(variable, IsNull.notNullValue());
          assertThat(table.getVariable("Other Variable"), IsNull.notNullValue());

          ValueTableWriter.VariableWriter variableWriter = ds.createWriter(TABLE, PARTICIPANT).writeVariables();
          variableWriter.removeVariable(variable);
        } catch(Exception e) {
          fail(e.getMessage());
          throw new RuntimeException(e);
        }
      }
    });

    transactionTemplate.execute(new TransactionCallbackWithoutResult() {
      @Override
      protected void doInTransactionWithoutResult(TransactionStatus status) {
        try {
          try {
            HibernateDatasource ds = getDatasource();
            ds.getValueTable(TABLE).getVariable("Test Variable");
            fail("Should throw NoSuchVariableException");
          } catch(NoSuchVariableException ignored) {
          }
        } catch(Exception e) {
          fail(e.getMessage());
          throw new RuntimeException(e);
        }
      }
    });

    //TODO check in database that values were removed

  }

  @Test
  public void test_rename_table() {

    transactionTemplate.execute(new TransactionCallbackWithoutResult() {
      @Override
      protected void doInTransactionWithoutResult(TransactionStatus status) {
        try {
          HibernateDatasource ds = createDatasource();
          ImmutableSet<Variable> variables = ImmutableSet.of(//
              Variable.Builder.newVariable("Test Variable", IntegerType.get(), PARTICIPANT).build(), //
              Variable.Builder.newVariable("Other Variable", DecimalType.get(), PARTICIPANT).build());

          ValueTable generatedValueTable = new GeneratedValueTable(ds, variables, 50);
          MagmaEngine.get().addDatasource(ds);
          DatasourceCopier.Builder.newCopier().build().copy(generatedValueTable, TABLE, ds);
        } catch(Exception e) {
          fail(e.getMessage());
          throw new RuntimeException(e);
        }
      }
    });

    final String NEW_NAME = "new_table";

    transactionTemplate.execute(new TransactionCallbackWithoutResult() {
      @Override
      protected void doInTransactionWithoutResult(TransactionStatus status) {
        try {
          HibernateDatasource ds = getDatasource();
          ds.renameTable(TABLE, NEW_NAME);
        } catch(Exception e) {
          fail(e.getMessage());
          throw new RuntimeException(e);
        }
      }
    });

    transactionTemplate.execute(new TransactionCallbackWithoutResult() {
      @Override
      protected void doInTransactionWithoutResult(TransactionStatus status) {
        try {
          HibernateDatasource ds = getDatasource();
          ds.getValueTable(NEW_NAME);
          ds.getValueTable(NEW_NAME).getVariable("Test Variable");
        } catch(Exception e) {
          fail(e.getMessage());
          throw new RuntimeException(e);
        }
      }
    });

    transactionTemplate.execute(new TransactionCallbackWithoutResult() {
      @Override
      protected void doInTransactionWithoutResult(TransactionStatus status) {
        try {
          try {
            HibernateDatasource ds = getDatasource();
            ds.getValueTable(TABLE);
            fail("Should fail with NoSuchValueTableException");
          } catch(NoSuchValueTableException ignored) {
          }
        } catch(Exception e) {
          fail(e.getMessage());
          throw new RuntimeException(e);
        }
      }
    });
  }

  private void assertSameCategories(Variable expected, Variable actual) {
    List<Category> expectedCategories = Lists.newArrayList(expected.getCategories());
    List<Category> actualCategories = Lists.newArrayList(actual.getCategories());
    Assert.assertEquals("Category count mismatch", expectedCategories.size(), actualCategories.size());
    for(int i = 0; i < expectedCategories.size(); i++) {
      assertThat("Category name mismatch at index " + i, actualCategories.get(i).getName(),
          is(expectedCategories.get(i).getName()));
      assertThat("Category code mismatch at index " + i, actualCategories.get(i).getCode(),
          is(expectedCategories.get(i).getCode()));
    }
  }

  private HibernateDatasource createDatasource() {
    HibernateDatasource datasource = new HibernateDatasource(DATASOURCE, sessionFactory);
    Initialisables.initialise(datasource);
    return datasource;
  }

  private HibernateDatasource getDatasource() {
    HibernateDatasource datasource = (HibernateDatasource) MagmaEngine.get().getDatasource(DATASOURCE);
    Initialisables.initialise(datasource);
    return datasource;
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
      Assert.assertNull(threadException);
    }

    abstract protected void test() throws Throwable;

  }

}
