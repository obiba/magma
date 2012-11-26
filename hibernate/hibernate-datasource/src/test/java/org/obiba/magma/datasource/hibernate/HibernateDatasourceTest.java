package org.obiba.magma.datasource.hibernate;

import java.util.List;
import java.util.Properties;
import java.util.SortedSet;
import java.util.TreeSet;

import junit.framework.Assert;

import org.hibernate.Transaction;
import org.hibernate.cfg.Environment;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.obiba.magma.Category;
import org.obiba.magma.Datasource;
import org.obiba.magma.MagmaEngine;
import org.obiba.magma.Value;
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

@SuppressWarnings("OverlyLongMethod")
public class HibernateDatasourceTest {

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

    ImmutableSet<Variable> variables = ImmutableSet.of(//
        Variable.Builder.newVariable("Test Variable", IntegerType.get(), "Participant").build(), //
        Variable.Builder.newVariable("Other Variable", DecimalType.get(), "Participant").build());

    GeneratedValueTable generatedValueTable = new GeneratedValueTable(ds, variables, 300);
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

    GeneratedValueTable generatedValueTable = new GeneratedValueTable(ds, variables, 300);
    provider.getSessionFactory().getCurrentSession().beginTransaction();
    MagmaEngine.get().addDatasource(ds);
    DatasourceCopier.Builder.newCopier().build().copy(generatedValueTable, "NewTable", ds);
    provider.getSessionFactory().getCurrentSession().getTransaction().commit();

    provider.getSessionFactory().getCurrentSession().beginTransaction();
    VariableValueSource vvs = ds.getValueTable("NewTable").getVariableValueSource("Test Variable");
    Assert.assertNotNull(vvs);
    Assert.assertNotNull(vvs.asVectorSource());
    VectorSource vs = vvs.asVectorSource();
    SortedSet<VariableEntity> entities = new TreeSet<VariableEntity>(
        ds.getValueTable("NewTable").getVariableEntities());
    Iterable<Value> values = vs.getValues(entities);
    Assert.assertNotNull(values);
    Assert.assertEquals(entities.size(), Iterables.size(values));
    cleanlyRemoveDatasource(ds);
  }

  private void assertSameCategories(Variable expected, Variable actual) {
    List<Category> expectedCategories = Lists.newArrayList(expected.getCategories());
    List<Category> actualCategories = Lists.newArrayList(actual.getCategories());
    Assert.assertEquals("Category count mismatch", expectedCategories.size(), actualCategories.size());
    for(int i = 0; i < expectedCategories.size(); i++) {
      Assert.assertEquals("Category name mismatch at index " + i, expectedCategories.get(i).getName(),
          actualCategories.get(i).getName());
      Assert.assertEquals("Category code mismatch at index " + i, expectedCategories.get(i).getCode(),
          actualCategories.get(i).getCode());
    }

  }

  private void cleanlyRemoveDatasource(Datasource ds) {
    Transaction tx = provider.getSessionFactory().getCurrentSession().getTransaction();
    if(tx == null || tx.isActive() == false) {
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
