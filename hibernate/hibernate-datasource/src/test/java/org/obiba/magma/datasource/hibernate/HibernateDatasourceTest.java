package org.obiba.magma.datasource.hibernate;

import junit.framework.Assert;

import org.hibernate.Transaction;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.obiba.magma.MagmaEngine;
import org.obiba.magma.ValueTableWriter;
import org.obiba.magma.Variable;
import org.obiba.magma.ValueTableWriter.VariableWriter;
import org.obiba.magma.datasource.hibernate.support.LocalSessionFactoryProvider;
import org.obiba.magma.type.IntegerType;
import org.obiba.magma.type.TextType;

public class HibernateDatasourceTest {

  LocalSessionFactoryProvider provider;

  @Before
  public void createMetaEngine() {
    new MagmaEngine();
    provider = newProvider("theTest");
  }

  @After
  public void shutdownMetaEngine() {
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
      public void test() {
        // Assert that the datasource does not have the value table
        Assert.assertFalse(MagmaEngine.get().getDatasource(dsName).hasValueTable(tableName));
      }
    }.assertNoException();

    provider.getSessionFactory().getCurrentSession().getTransaction().commit();

    // Make sure the table is visible outside this transaction.
    new TestThread() {
      public void test() {
        Assert.assertTrue(MagmaEngine.get().getDatasource(dsName).hasValueTable(tableName));
      }
    }.assertNoException();

    cleanlyRemoveDatasource(dsName);
  }

  @Test
  public void testTableAndVariablesPersisted() throws Exception {

    final String dsName = "testDs";
    final String tableName = "testTable";

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

  private void cleanlyRemoveDatasource(String name) {
    Transaction tx = provider.getSessionFactory().getCurrentSession().getTransaction();
    if(tx == null || tx.isActive() == false) {
      provider.getSessionFactory().getCurrentSession().beginTransaction();
    }
    MagmaEngine.get().removeDatasource(MagmaEngine.get().getDatasource(name));
    provider.getSessionFactory().getCurrentSession().getTransaction().commit();
  }

  private LocalSessionFactoryProvider newProvider(String testName) {
    LocalSessionFactoryProvider provider = new LocalSessionFactoryProvider("org.hsqldb.jdbcDriver", "jdbc:hsqldb:mem:" + testName + ";shutdown=true", "sa", "", "org.hibernate.dialect.HSQLDialect");
    provider.initialise();
    return provider;
  }

  private abstract class TestThread extends Thread {

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
