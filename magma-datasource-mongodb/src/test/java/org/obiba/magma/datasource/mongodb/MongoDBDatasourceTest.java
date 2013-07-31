package org.obiba.magma.datasource.mongodb;

import java.io.IOException;
import java.net.UnknownHostException;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.obiba.core.util.FileUtil;
import org.obiba.magma.MagmaEngine;
import org.obiba.magma.datasource.fs.FsDatasource;
import org.obiba.magma.support.DatasourceCopier;
import org.obiba.magma.support.Initialisables;
import org.obiba.magma.test.AbstractMagmaTest;
import org.obiba.magma.xstream.MagmaXStreamExtension;

import com.google.common.collect.Iterables;
import com.mongodb.MongoClient;

import junit.framework.Assert;

public class MongoDBDatasourceTest {

  private static final String DB_TEST = "magma-test";

  @Before
  public void startYourEngine() {
    new MagmaEngine().extend(new MagmaXStreamExtension());
  }

  @After
  public void stopYourEngine() {
    MagmaEngine.get().shutdown();
  }

  @Before
  public void before() throws UnknownHostException {
    MongoClient client = new MongoClient();
    client.dropDatabase(DB_TEST);
  }

  @Test
  @Ignore("cannot run without manual intervention")
  public void testWriters() throws IOException {
    FsDatasource onyx = new FsDatasource("onyx", FileUtil.getFileFromResource("20-onyx-data.zip"));
    MongoDBDatasource ds = new MongoDBDatasource("ds-" + DB_TEST, DB_TEST);
    Initialisables.initialise(ds, onyx);

    DatasourceCopier copier = DatasourceCopier.Builder.newCopier().build();
    copier.copy(onyx, ds);

    Assert.assertEquals(20, ds.getValueTable("AnkleBrachial").getVariableEntities().size());
    Assert.assertEquals(21, Iterables.size(ds.getValueTable("AnkleBrachial").getVariables()));
  }
}
