package org.obiba.magma.datasource.neo4j;

import java.io.IOException;

import org.hamcrest.core.IsNull;
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

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

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
  @Transactional
  public void canCreateDatasourceAndTable() throws IOException {
    String dsName = "testDs";
    String tableName = "testTable";

    Neo4jDatasource datasource = new Neo4jDatasource(dsName);

    //TODO replace with @Configurable
    applicationContext.getAutowireCapableBeanFactory().autowireBean(datasource);

    MagmaEngine.get().addDatasource(datasource);
    assertThat(datasource.getNode().getName(), is(dsName));

    ValueTableWriter tableWriter = datasource.createWriter(tableName, "Participant");
    tableWriter.close();

    assertThat(datasource.hasValueTable(tableName), is(true));
    assertThat(datasource.getValueTable(tableName), IsNull.notNullValue());
  }

}
