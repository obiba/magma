package org.obiba.magma.datasource.mart.jdbc;

import java.io.File;
import java.io.IOException;

import org.apache.commons.dbcp.BasicDataSource;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.obiba.magma.MagmaEngine;
import org.obiba.magma.datasource.fs.FsDatasource;
import org.obiba.magma.datasource.jdbc.JdbcDatasource;
import org.obiba.magma.support.DatasourceCopier;
import org.obiba.magma.xstream.MagmaXStreamExtension;

public class JdbcDatasourceTest {

  @Before
  public void startYourEngine() {
    new MagmaEngine().extend(new MagmaXStreamExtension());
  }

  @After
  public void shutdown() {
    MagmaEngine.get().shutdown();
  }

  @Ignore
  @Test
  public void testYo() throws IOException {
    BasicDataSource bds = new BasicDataSource();
    bds.setDriverClassName("org.hsqldb.jdbcDriver");
    bds.setUrl("jdbc:hsqldb:file:target/jdbc_mart;shutdown=true");
    bds.setUsername("sa");
    bds.setPassword("");
    JdbcDatasource ds = new JdbcDatasource("test", bds);

    MagmaEngine.get().addDatasource(ds);

    FsDatasource fsds = new FsDatasource("test-source", new File("src/test/resources/test-datasource.zip"));
    MagmaEngine.get().addDatasource(fsds);

    DatasourceCopier copier = DatasourceCopier.Builder.newCopier().withLoggingListener().withThroughtputListener().build();
    copier.copy(fsds, ds);

  }
}
