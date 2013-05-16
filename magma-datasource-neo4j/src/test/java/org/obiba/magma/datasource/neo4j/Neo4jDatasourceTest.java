package org.obiba.magma.datasource.neo4j;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.obiba.magma.MagmaEngine;
import org.obiba.magma.ValueTable;
import org.obiba.magma.ValueTableWriter;
import org.obiba.magma.Variable;
import org.obiba.magma.type.IntegerType;
import org.obiba.magma.type.TextType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import junit.framework.Assert;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("/application-context-test-neo4j.xml")
@Transactional
public class Neo4jDatasourceTest {

  public static final String DS_NAME = "testDs";

  public static final String TABLE_NAME = "testTable";

  public static final String PARTICIPANT = "Participant";

  @Autowired
  private ApplicationContext applicationContext;

  @Before
  public void startYourEngine() {
    MagmaEngine.get();
  }

  @After
  public void stopYourEngine() {
    MagmaEngine.get().shutdown();
  }

  private Neo4jDatasource createDatasource() {
    Neo4jDatasource datasource = new Neo4jDatasource(DS_NAME);

    //TODO replace with @Configurable
    applicationContext.getAutowireCapableBeanFactory().autowireBean(datasource);

    return datasource;
  }

  @Test
  public void canPersistDatasourceAndTable() throws Exception {

    Neo4jDatasource datasource = createDatasource();

    MagmaEngine.get().addDatasource(datasource);
    assertThat(datasource.getNode().getName(), is(DS_NAME));

    ValueTableWriter tableWriter = datasource.createWriter(TABLE_NAME, PARTICIPANT);
    tableWriter.close();

    assertThat(datasource.hasValueTable(TABLE_NAME), is(true));

    ValueTable valueTable = datasource.getValueTable(TABLE_NAME);
    assertThat(valueTable, notNullValue());
    assertThat(valueTable.getName(), is(TABLE_NAME));

    // Make sure the table is not visible outside this transaction.
    new TestThread() {
      @Override
      public void test() {
        // Assert that the datasource does not have the value table
        try {
          assertThat(MagmaEngine.get().getDatasource(DS_NAME).hasValueTable(TABLE_NAME), is(false));
          fail("Should throw NoSuchDatasourceException");
        } catch(Exception ignored) {
        }
      }
    };

  }

  @Test
  public void canPersistVariables() throws Exception {

    Neo4jDatasource datasource = createDatasource();
    MagmaEngine.get().addDatasource(datasource);
    ValueTableWriter tableWriter = datasource.createWriter(TABLE_NAME, PARTICIPANT);
    assertThat(datasource.hasValueTable(TABLE_NAME), is(true));

    ValueTableWriter.VariableWriter variableWriter = tableWriter.writeVariables();
    variableWriter.writeVariable(Variable.Builder.newVariable("Var1", TextType.get(), PARTICIPANT).build());
    assertThat(datasource.getValueTable(TABLE_NAME).getVariable("Var1"), notNullValue());
    variableWriter.writeVariable(Variable.Builder.newVariable("Var2", IntegerType.get(), PARTICIPANT).build());
    assertThat(datasource.getValueTable(TABLE_NAME).getVariable("Var2"), notNullValue());
    variableWriter.close();
    tableWriter.close();

//
//    // Re-create same datasource and assert that everything is still there.
//    ds = new HibernateDatasource(dsName, provider.getSessionFactory());
//    provider.getSessionFactory().getCurrentSession().beginTransaction();
//    MagmaEngine.get().addDatasource(ds);
//    Assert.assertNotNull(ds.getValueTable(tableName));
//    Assert.assertNotNull(ds.getValueTable(tableName).getVariable("Var1"));
//    Assert.assertNotNull(ds.getValueTable(tableName).getVariable("Var2"));
//
//    cleanlyRemoveDatasource(dsName);
  }

  private abstract static class TestThread extends Thread {

    private final Logger log = LoggerFactory.getLogger(getClass());

    private Throwable threadException;

    @Override
    public void run() {
      try {
        test();
      } catch(Throwable t) {
        log.debug("Throwable", t);
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
