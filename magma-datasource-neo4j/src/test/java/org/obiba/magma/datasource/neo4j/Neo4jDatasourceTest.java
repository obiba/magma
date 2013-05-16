package org.obiba.magma.datasource.neo4j;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.obiba.magma.MagmaEngine;
import org.obiba.magma.ValueTableWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import junit.framework.Assert;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("/application-context-test-neo4j.xml")
@Transactional
public class Neo4jDatasourceTest {

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

  @Test
  public void canCreateDatasourceAndTable() {
    String dsName = "testDs";
    String tableName = "testTable";

    Neo4jDatasource ds = new Neo4jDatasource(dsName);
    //TODO replace with @Configurable
    applicationContext.getAutowireCapableBeanFactory().autowireBean(ds);

    MagmaEngine.get().addDatasource(ds);
    ValueTableWriter vtWriter = ds.createWriter(tableName, "Participant");
    vtWriter.close();

    Assert.assertTrue(ds.hasValueTable(tableName));
    Assert.assertNotNull(ds.getValueTable(tableName));
  }

}
