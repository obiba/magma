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
import org.obiba.magma.MagmaRuntimeException;
import org.obiba.magma.NoSuchVariableException;
import org.obiba.magma.datasource.spss.support.SpssDatasourceFactory;
import org.obiba.magma.type.IntegerType;

import junit.framework.Assert;

public class DatasourceSpssTest {

  private SpssDatasourceFactory dsFactory;

  private static final String ROOT_FOLDER = "/home/rhaeri/projects/magma/datasource-spss/";

  @Before
  public void setUp() {
    dsFactory = new SpssDatasourceFactory();
  }

  @Test
  public void createDatasourceWithValidFile() {
    SpssDatasource ds = dsFactory
        .create("spss", "src/test/resources/org/obiba/magma/datasource" + "/spss/DatabaseTest.sav");
    Assert.assertEquals("spss", ds.getName());
  }

  @Test
  public void createDatasourceWithInvalidFile() {
    SpssDatasource ds = dsFactory
        .create("spss", "src/test/resources/org/obiba/magma/datasource" + "/spss/DatabaseTest.sa");
    Assert.assertEquals("spss", ds.getName());
  }

  @Test
  public void createDatasourceWithTwoFiles() {
    File file1 = new File("src/test/resources/org/obiba/magma/datasource/spss/DatabaseTest.sav");
    File file2 = new File("src/test/resources/org/obiba/magma/datasource/spss/HOP phase1d LifeLines.sav");

    List<File> files = new ArrayList<File>();
    files.add(file1);
    files.add(file2);

    SpssDatasource ds = dsFactory.create(files);
    Assert.assertEquals(SpssDatasourceFactory.DEFAULT_DATASOURCE_NAME, ds.getName());
  }

  @Test
  public void initilizeDatasourceWithValidFile() {
    SpssDatasource ds = dsFactory.create("src/test/resources/org/obiba/magma/datasource/spss/DatabaseTest.sav");
    ds.initialise();
    Assert.assertEquals("spss", ds.getName());
  }

  @Test(expected = MagmaRuntimeException.class)
  public void initilizeDatasourceWithInvalidFile() {
      SpssDatasource ds = dsFactory.create("src/test/resources/org/obiba/magma/datasource/spss/DatabaseTest");
      ds.initialise();
  }

  @Test
  public void getValueTableDatasourceWithValidFile() {
    SpssDatasource ds = dsFactory.create("src/test/resources/org/obiba/magma/datasource/spss/HOP phase1d LifeLines.sav");
    ds.initialise();
    Assert.assertNotNull(ds.getValueTable("HOPphase1dLifeLines"));
  }

  @Test
  public void getValueTableDatasourceWithColonFileName() {
    SpssDatasource ds = dsFactory.create("src/test/resources/org/obiba/magma/datasource/spss/Database:Test.sav");
    ds.initialise();
    Assert.assertNotNull(ds.getValueTable("DatabaseTest"));
  }

  @Test
  public void getVariableFromValueTable() {
    SpssDatasource ds = dsFactory.create("src/test/resources/org/obiba/magma/datasource/spss/DatabaseTest.sav");
    ds.initialise();
    Assert.assertNotNull(ds.getValueTable("DatabaseTest").getVariable("race"));
  }

  @Test(expected = NoSuchVariableException.class)
  public void getVariableInUpperCaseFromValueTable() {
    SpssDatasource ds = dsFactory.create("src/test/resources/org/obiba/magma/datasource/spss/DatabaseTest.sav");
    ds.initialise();
    Assert.assertNotNull(ds.getValueTable("DatabaseTest").getVariable("RACE"));
  }

  @Test(expected = NoSuchVariableException.class)
  public void getInvalidVariableFromValueTable() {
    SpssDatasource ds = dsFactory.create("src/test/resources/org/obiba/magma/datasource/spss/DatabaseTest.sav");
    ds.initialise();
    Assert.assertNotNull(ds.getValueTable("DatabaseTest").getVariable("blabla"));
  }

  @Test
  public void getVariableTypeFromValueTable() {
    SpssDatasource ds = dsFactory.create("src/test/resources/org/obiba/magma/datasource/spss/DatabaseTest.sav");
    ds.initialise();
    Assert.assertEquals(IntegerType.class, ds.getValueTable("DatabaseTest").getVariable("race").getValueType().getClass());
  }

}
