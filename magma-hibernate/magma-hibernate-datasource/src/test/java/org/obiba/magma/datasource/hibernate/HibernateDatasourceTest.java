package org.obiba.magma.datasource.hibernate;

import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.SortedSet;
import java.util.TreeSet;

import org.hibernate.Transaction;
import org.hibernate.cfg.Environment;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.obiba.magma.Category;
import org.obiba.magma.Datasource;
import org.obiba.magma.MagmaEngine;
import org.obiba.magma.Value;
import org.obiba.magma.ValueSet;
import org.obiba.magma.ValueTable;
import org.obiba.magma.ValueTableWriter;
import org.obiba.magma.ValueTableWriter.VariableWriter;
import org.obiba.magma.Variable;
import org.obiba.magma.VariableEntity;
import org.obiba.magma.VariableValueSource;
import org.obiba.magma.VectorSource;
import org.obiba.magma.datasource.generated.GeneratedValueTable;
import org.obiba.magma.datasource.hibernate.support.LocalSessionFactoryProvider;
import org.obiba.magma.support.DatasourceCopier;
import org.obiba.magma.type.DecimalType;
import org.obiba.magma.type.IntegerType;
import org.obiba.magma.type.TextType;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

import junit.framework.Assert;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;

@SuppressWarnings({ "OverlyLongMethod", "MagicNumber", "ReuseOfLocalVariable" })
public class HibernateDatasourceTest {

//  private static final Logger log = LoggerFactory.getLogger(HibernateDatasourceTest.class);

  LocalSessionFactoryProvider provider;

  @Before
  public void startYourEngine() {
    new MagmaEngine();
    provider = newProvider("theTest");
  }

  @After
  public void stopYourEngine() {
    MagmaEngine.get().shutdown();
    provider.getSessionFactory().close();
  }

  @Test
  public void testTransactionalTableCreation() throws Exception {

    final String dsName = "testDs";
    final String tableName = "testTable";

    HibernateDatasource ds = new HibernateDatasource(dsName, provider.getSessionFactory());

    provider.getSessionFactory().getCurrentSession().beginTransaction();
    MagmaEngine.get().addDatasource(ds);
    ValueTableWriter vtWriter = ds.createWriter(tableName, "Participant");
    vtWriter.close();

    Assert.assertTrue(ds.hasValueTable(tableName));
    Assert.assertNotNull(ds.getValueTable(tableName));

    // Make sure the table is not visible outside this transaction.
    new TestThread() {
      @Override
      public void test() {
        // Assert that the datasource does not have the value table
        Assert.assertFalse(MagmaEngine.get().getDatasource(dsName).hasValueTable(tableName));
      }
    }.assertNoException();

    provider.getSessionFactory().getCurrentSession().getTransaction().commit();

    // Make sure the table is visible outside this transaction.
    new TestThread() {
      @Override
      public void test() {
        Assert.assertTrue(MagmaEngine.get().getDatasource(dsName).hasValueTable(tableName));
      }
    }.assertNoException();

    cleanlyRemoveDatasource(dsName);
  }

  @Test
  public void testTableAndVariablesPersisted() throws Exception {

    String dsName = "testDs";
    String tableName = "testTable";

    HibernateDatasource ds = new HibernateDatasource(dsName, provider.getSessionFactory());

    provider.getSessionFactory().getCurrentSession().beginTransaction();
    MagmaEngine.get().addDatasource(ds);

    ValueTableWriter vtWriter = ds.createWriter(tableName, "Participant");

    // Test that the table is visible
    Assert.assertTrue(ds.hasValueTable(tableName));

    // Write variables and assert that they are visible.
    VariableWriter vw = vtWriter.writeVariables();
    vw.writeVariable(Variable.Builder.newVariable("Var1", TextType.get(), "Participant").build());
    Assert.assertNotNull(ds.getValueTable(tableName).getVariable("Var1"));
    vw.writeVariable(Variable.Builder.newVariable("Var2", IntegerType.get(), "Participant").build());
    Assert.assertNotNull(ds.getValueTable(tableName).getVariable("Var2"));
    vw.close();
    vtWriter.close();

    provider.getSessionFactory().getCurrentSession().getTransaction().commit();

    cleanlyRemoveDatasource(dsName);

    // Re-create same datasource and assert that everything is still there.
    ds = new HibernateDatasource(dsName, provider.getSessionFactory());
    provider.getSessionFactory().getCurrentSession().beginTransaction();
    MagmaEngine.get().addDatasource(ds);
    Assert.assertNotNull(ds.getValueTable(tableName));
    Assert.assertNotNull(ds.getValueTable(tableName).getVariable("Var1"));
    Assert.assertNotNull(ds.getValueTable(tableName).getVariable("Var2"));

    cleanlyRemoveDatasource(dsName);
  }

  @Test
  public void testVariableStateChangeIsPersisted() throws Exception {
    Variable initialState = Variable.Builder.newVariable("Var1", TextType.get(), "Participant").addCategory("C1", "1")
        .addCategory("C2", "2").addCategory("C3", "3").build();
    Variable changedState = Variable.Builder.newVariable("Var1", TextType.get(), "Participant").addCategory("C3", "3")
        .addCategory("C1", "1").addCategory("C4", "4").addCategory("C2", "2").build();

    String dsName = "testDs";
    String tableName = "testTable";

    HibernateDatasource ds = new HibernateDatasource(dsName, provider.getSessionFactory());

    provider.getSessionFactory().getCurrentSession().beginTransaction();
    MagmaEngine.get().addDatasource(ds);

    ValueTableWriter vtWriter = ds.createWriter(tableName, "Participant");

    // Write variables and assert that they are visible.
    VariableWriter vw = vtWriter.writeVariables();
    vw.writeVariable(initialState);
    vw.close();
    vtWriter.close();

    provider.getSessionFactory().getCurrentSession().getTransaction().commit();

    provider.getSessionFactory().getCurrentSession().beginTransaction();

    Variable v = ds.getValueTable(tableName).getVariable("Var1");
    assertSameCategories(initialState, v);

    vtWriter = ds.createWriter(tableName, "Participant");
    // Write variables and assert that they are visible.
    vw = vtWriter.writeVariables();
    vw.writeVariable(changedState);
    vw.close();
    vtWriter.close();

    provider.getSessionFactory().getCurrentSession().getTransaction().commit();

    provider.getSessionFactory().getCurrentSession().beginTransaction();
    v = ds.getValueTable(tableName).getVariable("Var1");
    assertSameCategories(changedState, v);

    cleanlyRemoveDatasource(dsName);
  }

  @Test
  public void testWrite() throws Exception {
    HibernateDatasource ds = new HibernateDatasource("test", provider.getSessionFactory());

    ImmutableSet<Variable> variables = ImmutableSet.of( //
        Variable.Builder.newVariable("Test Variable", IntegerType.get(), "Participant").build(), //
        Variable.Builder.newVariable("Other Variable", DecimalType.get(), "Participant").build());

    ValueTable generatedValueTable = new GeneratedValueTable(ds, variables, 300);
    provider.getSessionFactory().getCurrentSession().beginTransaction();
    MagmaEngine.get().addDatasource(ds);
    DatasourceCopier.Builder.newCopier().build().copy(generatedValueTable, ds);
    cleanlyRemoveDatasource(ds);
  }

  @Test
  public void testVectorSource() throws Exception {
    HibernateDatasource ds = new HibernateDatasource("vectorSourceTest", provider.getSessionFactory());

    ImmutableSet<Variable> variables = ImmutableSet.of(//
        Variable.Builder.newVariable("Test Variable", IntegerType.get(), "Participant").build(), //
        Variable.Builder.newVariable("Other Variable", DecimalType.get(), "Participant").build());

    ValueTable generatedValueTable = new GeneratedValueTable(ds, variables, 300);
    provider.getSessionFactory().getCurrentSession().beginTransaction();
    MagmaEngine.get().addDatasource(ds);
    DatasourceCopier.Builder.newCopier().build().copy(generatedValueTable, "NewTable", ds);
    provider.getSessionFactory().getCurrentSession().getTransaction().commit();

    provider.getSessionFactory().getCurrentSession().beginTransaction();
    VariableValueSource vvs = ds.getValueTable("NewTable").getVariableValueSource("Test Variable");
    assertThat(vvs, notNullValue());
    assertThat(vvs.asVectorSource(), notNullValue());

    VectorSource vs = vvs.asVectorSource();
    SortedSet<VariableEntity> entities = new TreeSet<VariableEntity>(
        ds.getValueTable("NewTable").getVariableEntities());
    Iterable<Value> values = vs.getValues(entities);
    assertThat(values, notNullValue());
    assertThat(Iterables.size(values), is(entities.size()));
    cleanlyRemoveDatasource(ds);
  }

  @Test
  public void testTimestamps() throws Exception {
    HibernateDatasource ds = new HibernateDatasource("testTimestamps", provider.getSessionFactory());
    ImmutableSet<Variable> variables = ImmutableSet.of(//
        Variable.Builder.newVariable("Test Variable", IntegerType.get(), "Participant").build(), //
        Variable.Builder.newVariable("Other Variable", DecimalType.get(), "Participant").build());

    ValueTable generatedValueTable = new GeneratedValueTable(ds, variables, 300);
    provider.getSessionFactory().getCurrentSession().beginTransaction();
    MagmaEngine.get().addDatasource(ds);
    DatasourceCopier.Builder.newCopier().build().copy(generatedValueTable, "NewTable", ds);
    provider.getSessionFactory().getCurrentSession().getTransaction().commit();

    provider.getSessionFactory().getCurrentSession().beginTransaction();
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
    //noinspection ConstantConditions
    long delta = tableLastUpdate.getTime() - lastValueSetUpdate.getTime();
    assertThat(
        "Table lastUpdate (" + tableLastUpdate + ") is older than its last valueSet lastUpdate (" + lastValueSetUpdate +
            ")", delta < 5, is(true));
    assertThat("Table created date (" + tableCreated + ") is older than last update (" + tableLastUpdate +
        ")", tableLastUpdate.after(tableCreated), is(true));

    cleanlyRemoveDatasource(ds);
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

  private void cleanlyRemoveDatasource(Datasource ds) {
    Transaction tx = provider.getSessionFactory().getCurrentSession().getTransaction();
    if(tx == null || !tx.isActive()) {
      provider.getSessionFactory().getCurrentSession().beginTransaction();
    }
    MagmaEngine.get().removeDatasource(ds);
    provider.getSessionFactory().getCurrentSession().getTransaction().commit();
  }

  private void cleanlyRemoveDatasource(String name) {
    cleanlyRemoveDatasource(MagmaEngine.get().getDatasource(name));
  }

  private LocalSessionFactoryProvider newProvider(String testName) {
    LocalSessionFactoryProvider newProvider = new LocalSessionFactoryProvider("org.hsqldb.jdbcDriver",
        "jdbc:hsqldb:mem:" + testName + ";shutdown=true", "sa", "", "org.hibernate.dialect.HSQLDialect");
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
