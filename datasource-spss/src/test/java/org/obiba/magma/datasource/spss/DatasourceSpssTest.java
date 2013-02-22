/*
 * Copyright (c) 2012 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.magma.datasource.spss;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.obiba.magma.Datasource;
import org.obiba.magma.MagmaRuntimeException;
import org.obiba.magma.NoSuchVariableException;
import org.obiba.magma.datasource.spss.support.SpssDatasourceFactory;
import org.obiba.magma.type.IntegerType;

import junit.framework.Assert;

public class DatasourceSpssTest {

  private SpssDatasourceFactory dsFactory;

  @Before
  public void setUp() {
    dsFactory = new SpssDatasourceFactory();
    dsFactory.setName(SpssDatasourceFactory.DEFAULT_DATASOURCE_NAME);
  }

  @Test
  public void createDatasourceWithValidFile() {
    dsFactory.setFile("src/test/resources/org/obiba/magma/datasource/spss/DatabaseTest.sav");
    Datasource ds = dsFactory.create();
    Assert.assertEquals(SpssDatasourceFactory.DEFAULT_DATASOURCE_NAME, ds.getName());
  }

  @Test
  public void createDatasourceWithInvalidFile() {
    dsFactory.setFile("src/test/resources/org/obiba/magma/datasource/spss/DatabaseTest.sav");
    Datasource ds = dsFactory.create();
    Assert.assertEquals(SpssDatasourceFactory.DEFAULT_DATASOURCE_NAME, ds.getName());
  }

  @Test
  public void createDatasourceWithTwoFiles() {
    File file1 = new File("src/test/resources/org/obiba/magma/datasource/spss/DatabaseTest.sav");
    File file2 = new File("src/test/resources/org/obiba/magma/datasource/spss/HOP phase1d LifeLines.sav");

    dsFactory.addFile(file1);
    dsFactory.addFile(file2);

    Datasource ds = dsFactory.create();
    Assert.assertEquals(SpssDatasourceFactory.DEFAULT_DATASOURCE_NAME, ds.getName());
  }

  @Test
  public void initilizeDatasourceWithValidFile() {
    dsFactory.setFile("src/test/resources/org/obiba/magma/datasource/spss/DatabaseTest.sav");
    Datasource ds = dsFactory.create();
    ds.initialise();
    Assert.assertEquals(SpssDatasourceFactory.DEFAULT_DATASOURCE_NAME, ds.getName());
  }

  @Test(expected = MagmaRuntimeException.class)
  public void initilizeDatasourceWithInvalidFile() {
      dsFactory.setFile("src/test/resources/org/obiba/magma/datasource/spss/DatabaseTest");
      Datasource ds = dsFactory.create();
      ds.initialise();
  }

  @Test
  public void getValueTableDatasourceWithValidFile() {
    dsFactory.setFile("src/test/resources/org/obiba/magma/datasource/spss/HOP phase1d LifeLines.sav");
    Datasource ds = dsFactory.create();
    ds.initialise();
    Assert.assertNotNull(ds.getValueTable("HOPphase1dLifeLines"));
  }

  @Test
  public void getValueTableDatasourceWithColonFileName() {
    dsFactory.setFile("src/test/resources/org/obiba/magma/datasource/spss/Database:Test.sav");
    Datasource ds = dsFactory.create();
    ds.initialise();
    Assert.assertNotNull(ds.getValueTable("DatabaseTest"));
  }

  @Test
  public void getVariableFromValueTable() {
    dsFactory.setFile("src/test/resources/org/obiba/magma/datasource/spss/DatabaseTest.sav");
    Datasource ds = dsFactory.create();
    ds.initialise();
    Assert.assertNotNull(ds.getValueTable("DatabaseTest").getVariable("race"));
  }

  @Test(expected = NoSuchVariableException.class)
  public void getVariableInUpperCaseFromValueTable() {
    dsFactory.setFile("src/test/resources/org/obiba/magma/datasource/spss/DatabaseTest.sav");
    Datasource ds = dsFactory.create();
    ds.initialise();
    Assert.assertNotNull(ds.getValueTable("DatabaseTest").getVariable("RACE"));
  }

  @Test(expected = NoSuchVariableException.class)
  public void getInvalidVariableFromValueTable() {
    dsFactory.setFile("src/test/resources/org/obiba/magma/datasource/spss/DatabaseTest.sav");
    Datasource ds = dsFactory.create();
    ds.initialise();
    Assert.assertNotNull(ds.getValueTable("DatabaseTest").getVariable("blabla"));
  }

  @Test
  public void getVariableTypeFromValueTable() {
    dsFactory.setFile("src/test/resources/org/obiba/magma/datasource/spss/DatabaseTest.sav");
    Datasource ds = dsFactory.create();
    ds.initialise();
    Assert.assertEquals(IntegerType.class, ds.getValueTable("DatabaseTest").getVariable("race").getValueType().getClass());
  }

}
