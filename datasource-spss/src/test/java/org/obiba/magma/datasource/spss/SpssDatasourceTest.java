/*
 * Copyright (c) 2013 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.magma.datasource.spss;

import java.io.File;
import java.util.Iterator;
import java.util.Set;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.obiba.magma.Datasource;
import org.obiba.magma.MagmaEngine;
import org.obiba.magma.NoSuchVariableException;
import org.obiba.magma.Value;
import org.obiba.magma.ValueTable;
import org.obiba.magma.VariableEntity;
import org.obiba.magma.datasource.spss.support.SpssDatasourceFactory;
import org.obiba.magma.datasource.spss.support.SpssDatasourceParsingException;
import org.obiba.magma.type.DecimalType;
import org.obiba.magma.type.TextType;

import junit.framework.Assert;

import static org.junit.Assert.assertTrue;

public class SpssDatasourceTest {

  private SpssDatasourceFactory dsFactory;

  @Before
  public void before() {
    new MagmaEngine();
    dsFactory = new SpssDatasourceFactory();
    dsFactory.setName(SpssDatasourceFactory.DEFAULT_DATASOURCE_NAME);
  }

  @After
  public void after() {
    MagmaEngine.get().shutdown();
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
    File file2 = new File("src/test/resources/org/obiba/magma/datasource/spss/StringCategories.sav");

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

  @Test(expected = SpssDatasourceParsingException.class)
  public void initilizeDatasourceWithInvalidFile() {
    dsFactory.setFile("src/test/resources/org/obiba/magma/datasource/spss/DatabaseTest");
    Datasource ds = dsFactory.create();
    ds.initialise();
  }

  @Test
  public void getValueTableDatasourceWithValidFile() {
    dsFactory.setFile("src/test/resources/org/obiba/magma/datasource/spss/StringCategories.sav");
    Datasource ds = dsFactory.create();
    ds.initialise();
    Assert.assertNotNull(ds.getValueTable("StringCategories"));
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

  @Test
  public void getVariableFromValueTableWithFrenchChars() {
    dsFactory.setFile("src/test/resources/org/obiba/magma/datasource/spss/dictionnaire_variablesT4-simple.sav");
    Datasource ds = dsFactory.create();
    ds.initialise();
    Assert.assertNotNull(ds.getValueTable("dictionnaire_variablesT4-simple").getVariable("ETATCIT4"));
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
  public void getDecimalVariableTypeFromValueTable() {
    dsFactory.setFile("src/test/resources/org/obiba/magma/datasource/spss/RobotChicken.sav");
    Datasource ds = dsFactory.create();
    ds.initialise();
    Assert.assertEquals(DecimalType.class,
        ds.getValueTable("RobotChicken").getVariable("VarDecimal").getValueType().getClass());
  }

  @Test
  public void getQYDVariableTypeFromValueTable() {
    dsFactory.setFile("src/test/resources/org/obiba/magma/datasource/spss/RobotChicken.sav");
    Datasource ds = dsFactory.create();
    ds.initialise();
    Assert.assertEquals(TextType.class,
        ds.getValueTable("RobotChicken").getVariable("VarQuarterly").getValueType().getClass());
  }

  @Test
  public void getCustomCurrencyVariableTypeFromValueTable() {
    dsFactory.setFile("src/test/resources/org/obiba/magma/datasource/spss/RobotChicken.sav");
    Datasource ds = dsFactory.create();
    ds.initialise();
    Assert.assertEquals(TextType.class,
        ds.getValueTable("RobotChicken").getVariable("VarCurrency").getValueType().getClass());
  }

  @Test
  public void getVariableEntitiesVariableEntityProvider() {
    dsFactory.setFile("src/test/resources/org/obiba/magma/datasource/spss/DatabaseTest.sav");
    Datasource ds = dsFactory.create();
    ds.initialise();

    ValueTable valueTable = ds.getValueTable("DatabaseTest");

    Set<VariableEntity> variableEntities = valueTable.getVariableEntities();
    assertTrue(variableEntities.size() == 200);
  }

  @Test
  public void getValueSetForGivenEntity() {
    dsFactory.setFile("src/test/resources/org/obiba/magma/datasource/spss/DatabaseTest.sav");
    Datasource ds = dsFactory.create();
    ds.initialise();

    ValueTable valueTable = ds.getValueTable("DatabaseTest");
    Iterator<VariableEntity> iterator = valueTable.getVariableEntities().iterator();

    if(iterator.hasNext()) {
      SpssValueSet valueSet = (SpssValueSet) valueTable.getValueSet(iterator.next());
      Value value = valueSet.getValue(ds.getValueTable("DatabaseTest").getVariable("race"));
      Assert.assertNotNull(value);
      Assert.assertFalse(value.isNull());
      Value expected = DecimalType.get().valueOf(4.0);
      Assert.assertTrue(value.compareTo(expected) == 0);
    }
  }

  @Test
  public void getValueSetForGivenEntityLargeDatasource() {
    dsFactory.setFile("src/test/resources/org/obiba/magma/datasource/spss/DatabaseTest.sav");
    Datasource ds = dsFactory.create();
    ds.initialise();
    ValueTable valueTable = ds.getValueTable("DatabaseTest");
    Iterator<VariableEntity> iterator = valueTable.getVariableEntities().iterator();

    if(iterator.hasNext()) {
      SpssValueSet valueSet = (SpssValueSet) valueTable.getValueSet(iterator.next());
      Value value = valueSet.getValue(ds.getValueTable("DatabaseTest").getVariable("race"));
      Assert.assertNotNull(value);
      Assert.assertFalse(value.isNull());
      Value expected = DecimalType.get().valueOf(4.0);
      Assert.assertTrue(value.compareTo(expected) == 0);
    }
  }

//  @Test
//  public void getValuesForAllEntitiesOfGivenVariable() {
//    dsFactory.setFile("src/test/resources/org/obiba/magma/datasource/spss/DatabaseTest.sav");
//    Datasource ds = dsFactory.create();
//    ds.initialise();
//
//    ValueTable valueTable = ds.getValueTable("DatabaseTest");
//    Set<VariableEntity> entities = valueTable.getVariableEntities();
//    Assert.assertNotNull(entities);
//    Assert.assertEquals(200, entities.size());
//
//    VectorSource bdVar = valueTable.getVariableValueSource("write").asVectorSource();
//    assertNotNull(bdVar);
//
//    Iterable<Value> values = bdVar.getValues(new TreeSet<VariableEntity>(entities));
//    Iterator<Value> iterator = values.iterator();
//    Assert.assertTrue(iterator.hasNext());
//    Assert.assertNotNull(values);
//
//    // Testing for the first 5 values, notice that the values are sorted
//    List<Value> expectedValues = new ArrayList<Value>();
//    expectedValues.add(DecimalType.get().valueOf(44.0));
//    expectedValues.add(DecimalType.get().valueOf(41.0));
//    expectedValues.add(DecimalType.get().valueOf(65.0));
//    expectedValues.add(DecimalType.get().valueOf(50.0));
//    expectedValues.add(DecimalType.get().valueOf(40.0));
//
//    for (Iterator<Value> iteratorExpected = expectedValues.iterator(); iteratorExpected.hasNext();) {
//      Assert.assertTrue(iterator.next().compareTo(iteratorExpected.next()) == 0);
//    }
//
//  }

}
