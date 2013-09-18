package org.obiba.magma.datasource.hibernate;

import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.SortedSet;

import org.hamcrest.core.Is;
import org.hamcrest.core.IsNull;
import org.hibernate.Transaction;
import org.hibernate.cfg.Environment;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.obiba.magma.Category;
import org.obiba.magma.MagmaEngine;
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
import org.obiba.magma.datasource.hibernate.support.LocalSessionFactoryProvider;
import org.obiba.magma.support.DatasourceCopier;
import org.obiba.magma.support.Initialisables;
import org.obiba.magma.type.BinaryType;
import org.obiba.magma.type.DateTimeType;
import org.obiba.magma.type.DateType;
import org.obiba.magma.type.DecimalType;
import org.obiba.magma.type.IntegerType;
import org.obiba.magma.type.TextType;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.core.IsInstanceOf.instanceOf;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

@SuppressWarnings({ "OverlyLongMethod", "ReuseOfLocalVariable" })
public class HibernateDatasourceTest {

//  private static final Logger log = LoggerFactory.getLogger(HibernateDatasourceTest.class);

  private static final String PARTICIPANT = "Participant";

  private static final String DATASOURCE = "testDs";

  private static final String TABLE = "testTable";

  private LocalSessionFactoryProvider provider;

  @Before
  public void setup() {
    MagmaEngine.get();
    provider = newHsqlProvider();
//    provider = newMysqlProvider();
//    provider = newPostgreSqlProvider();
    cleanlyRemoveDatasource(true);
  }

  @After
  public void shutdown() {
    beginTransaction();
    MagmaEngine.get().shutdown();
    commitTransaction();
    provider.getSessionFactory().close();
  }

  @Test
  public void testTransactionalTableCreation() throws Exception {

    HibernateDatasource ds = createDatasource();

    beginTransaction();
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

    commitTransaction();

    // Make sure the table is visible outside this transaction.
    new TestThread() {
      @Override
      public void test() {
        assertThat(MagmaEngine.get().getDatasource(DATASOURCE).hasValueTable(TABLE), is(true));
      }
    }.assertNoException();

  }

  @Test
  public void testTableAndVariablesPersisted() throws Exception {

    HibernateDatasource ds = createDatasource();

    beginTransaction();
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

    commitTransaction();

    cleanlyRemoveDatasource(false);

    // Re-create same datasource and assert that everything is still there.
    beginTransaction();
    ds = createDatasource();
    MagmaEngine.get().addDatasource(ds);
    assertNotNull(ds.getValueTable(TABLE));
    assertNotNull(ds.getValueTable(TABLE).getVariable("Var1"));
    assertNotNull(ds.getValueTable(TABLE).getVariable("Var2"));
  }

  @Test
  public void testVariableStateChangeIsPersisted() throws Exception {
    Variable initialState = Variable.Builder.newVariable("Var1", TextType.get(), PARTICIPANT).addCategory("C1", "1")
        .addCategory("C2", "2").addCategory("C3", "3").build();
    Variable changedState = Variable.Builder.newVariable("Var1", TextType.get(), PARTICIPANT).addCategory("C3", "3")
        .addCategory("C1", "1").addCategory("C4", "4").addCategory("C2", "2").build();

    HibernateDatasource ds = createDatasource();

    beginTransaction();
    MagmaEngine.get().addDatasource(ds);

    ValueTableWriter vtWriter = ds.createWriter(TABLE, PARTICIPANT);

    // Write variables and assert that they are visible.
    VariableWriter vw = vtWriter.writeVariables();
    vw.writeVariable(initialState);
    vw.close();
    vtWriter.close();

    commitTransaction();

    beginTransaction();

    Variable v = ds.getValueTable(TABLE).getVariable("Var1");
    assertSameCategories(initialState, v);

    vtWriter = ds.createWriter(TABLE, PARTICIPANT);
    // Write variables and assert that they are visible.
    vw = vtWriter.writeVariables();
    vw.writeVariable(changedState);
    vw.close();
    vtWriter.close();

    commitTransaction();

    beginTransaction();
    v = ds.getValueTable(TABLE).getVariable("Var1");
    assertSameCategories(changedState, v);

  }

  @Test
  public void testWrite() throws Exception {
    HibernateDatasource ds = createDatasource();

    ImmutableSet<Variable> variables = ImmutableSet.of( //
        Variable.Builder.newVariable("Test Variable", IntegerType.get(), PARTICIPANT).build(), //
        Variable.Builder.newVariable("Other Variable", DecimalType.get(), PARTICIPANT).build());

    ValueTable generatedValueTable = new GeneratedValueTable(ds, variables, 50);
    beginTransaction();
    MagmaEngine.get().addDatasource(ds);
    DatasourceCopier.Builder.newCopier().build().copy(generatedValueTable, ds);
  }

  @Test
  public void testVectorSource() throws Exception {
    HibernateDatasource ds = createDatasource();

    ImmutableSet<Variable> variables = ImmutableSet.of(//
        Variable.Builder.newVariable("Test Variable", IntegerType.get(), PARTICIPANT).build(), //
        Variable.Builder.newVariable("Test Repeatable", TextType.get(), PARTICIPANT).repeatable().build(), //
        Variable.Builder.newVariable("Test Date", DateType.get(), PARTICIPANT).build(), //
        Variable.Builder.newVariable("Test DateTime", DateTimeType.get(), PARTICIPANT).build(), //
        Variable.Builder.newVariable("Other Variable", DecimalType.get(), PARTICIPANT).build());

    ValueTable generatedValueTable = new GeneratedValueTable(ds, variables, 50);
    beginTransaction();
    MagmaEngine.get().addDatasource(ds);
    DatasourceCopier.Builder.newCopier().build().copy(generatedValueTable, "NewTable", ds);
    commitTransaction();

    beginTransaction();
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
  }

  @SuppressWarnings("ConstantConditions")
  @Test
  public void testBinaryVectorSource() throws Exception {
    HibernateDatasource ds = createDatasource();

    ImmutableSet<Variable> variables = ImmutableSet.of( //
        Variable.Builder.newVariable("Test Binary", BinaryType.get(), PARTICIPANT).build(),
        Variable.Builder.newVariable("Test Repeatable Binary", BinaryType.get(), PARTICIPANT).repeatable().build());

    ValueTable generatedValueTable = new GeneratedValueTable(ds, variables, 1);
    beginTransaction();
    MagmaEngine.get().addDatasource(ds);
    DatasourceCopier.Builder.newCopier().build().copy(generatedValueTable, "NewTable", ds);
    commitTransaction();

    beginTransaction();
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
  }

  @SuppressWarnings("ConstantConditions")
  @Test
  public void testTimestamps() throws Exception {

    beginTransaction();
    HibernateDatasource ds = createDatasource();
    ds.createWriter("NewTable", PARTICIPANT).close();
    commitTransaction();

    Thread.sleep(2000);

    ImmutableSet<Variable> variables = ImmutableSet.of(//
        Variable.Builder.newVariable("Test Variable", IntegerType.get(), PARTICIPANT).build(), //
        Variable.Builder.newVariable("Other Variable", DecimalType.get(), PARTICIPANT).build());

    ValueTable generatedValueTable = new GeneratedValueTable(ds, variables, 50);

    beginTransaction();
    MagmaEngine.get().addDatasource(ds);
    DatasourceCopier.Builder.newCopier().build().copy(generatedValueTable, "NewTable", ds);
    commitTransaction();

    beginTransaction();
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

    // allow max 5ms of delay between last valueSet update and table last update
    long delta = tableLastUpdate.getTime() - lastValueSetUpdate.getTime();
    assertThat("Last valueSet lastUpdate (" + lastValueSetUpdate + ") should be older than table lastUpdate (" +
        tableLastUpdate +
        ")", delta < 5, is(true));
    assertThat("Table created date (" + tableCreated + ") should be older than last update (" + tableLastUpdate +
        ")", tableLastUpdate.after(tableCreated), is(true));

  }

  @Test
  public void testRemoveVariable() throws IOException {
    HibernateDatasource ds = createDatasource();
    ImmutableSet<Variable> variables = ImmutableSet.of(//
        Variable.Builder.newVariable("Test Variable", IntegerType.get(), PARTICIPANT).build(), //
        Variable.Builder.newVariable("Other Variable", DecimalType.get(), PARTICIPANT).build());

    ValueTable generatedValueTable = new GeneratedValueTable(ds, variables, 50);
    beginTransaction();
    MagmaEngine.get().addDatasource(ds);
    DatasourceCopier.Builder.newCopier().build().copy(generatedValueTable, TABLE, ds);
    commitTransaction();

    beginTransaction();

    ValueTable table = ds.getValueTable(TABLE);
    Variable variable = table.getVariable("Test Variable");

    assertThat(Iterables.size(table.getVariables()), Is.is(2));
    assertThat(variable, IsNull.notNullValue());
    assertThat(table.getVariable("Other Variable"), IsNull.notNullValue());

    ValueTableWriter.VariableWriter variableWriter = ds.createWriter(TABLE, PARTICIPANT).writeVariables();
    variableWriter.removeVariable(variable);

    commitTransaction();

    beginTransaction();
    try {
      table.getVariable("Test Variable");
      fail("Should throw NoSuchVariableException");
    } catch(NoSuchVariableException e) {
    }

    //TODO check in database that values were removed

  }

  private void beginTransaction() {
    provider.getSessionFactory().getCurrentSession().beginTransaction();
  }

  private void commitTransaction() {
    provider.getSessionFactory().getCurrentSession().getTransaction().commit();
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
    HibernateDatasource datasource = new HibernateDatasource(DATASOURCE, provider.getSessionFactory());
    Initialisables.initialise(datasource);
    return datasource;
  }

  private void cleanlyRemoveDatasource(boolean drop) {
    try {
      Transaction tx = provider.getSessionFactory().getCurrentSession().getTransaction();
      if(tx == null || !tx.isActive()) {
        tx = provider.getSessionFactory().getCurrentSession().beginTransaction();
      }
      if(drop) {
        HibernateDatasource datasource = createDatasource();
        MagmaEngine.get().addDatasource(datasource);
        datasource.drop();
      }
      MagmaEngine.get().removeDatasource(MagmaEngine.get().getDatasource(DATASOURCE));
      tx.commit();
    } catch(Exception e) {
    }
  }

  @SuppressWarnings("UnusedDeclaration")
  private LocalSessionFactoryProvider newHsqlProvider() {
    LocalSessionFactoryProvider newProvider = new LocalSessionFactoryProvider("org.hsqldb.jdbcDriver",
        "jdbc:hsqldb:mem:magma_test;shutdown=true", "sa", "", "org.hibernate.dialect.HSQLDialect");
    Properties p = new Properties();
    p.setProperty(Environment.CACHE_PROVIDER, "org.hibernate.cache.HashtableCacheProvider");
    newProvider.setProperties(p);
    newProvider.initialise();
    return newProvider;
  }

  @SuppressWarnings("UnusedDeclaration")
  private LocalSessionFactoryProvider newMysqlProvider() {
    LocalSessionFactoryProvider newProvider = new LocalSessionFactoryProvider("com.mysql.jdbc.Driver",
        "jdbc:mysql://localhost:3306/magma_test?characterEncoding=UTF-8", "root", "1234",
        "org.hibernate.dialect.MySQL5InnoDBDialect");
    Properties p = new Properties();
    p.setProperty(Environment.CACHE_PROVIDER, "org.hibernate.cache.HashtableCacheProvider");
    newProvider.setProperties(p);
    newProvider.initialise();
    return newProvider;
  }

  @SuppressWarnings("UnusedDeclaration")
  private LocalSessionFactoryProvider newPostgreSqlProvider() {
    LocalSessionFactoryProvider newProvider = new LocalSessionFactoryProvider("org.postgresql.Driver",
        "jdbc:postgresql://localhost:5432/magma_test", "postgres", "password",
        "org.hibernate.dialect.PostgreSQLDialect");
    Properties p = new Properties();
    p.setProperty(Environment.CACHE_PROVIDER, "org.hibernate.cache.HashtableCacheProvider");
    newProvider.setProperties(p);
    newProvider.initialise();
    return newProvider;
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
