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
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.obiba.magma.Category;
import org.obiba.magma.Datasource;
import org.obiba.magma.MagmaEngine;
import org.obiba.magma.NoSuchVariableException;
import org.obiba.magma.Value;
import org.obiba.magma.ValueTable;
import org.obiba.magma.Variable;
import org.obiba.magma.VariableEntity;
import org.obiba.magma.datasource.spss.support.SpssDatasourceFactory;
import org.obiba.magma.datasource.spss.support.SpssDatasourceParsingException;
import org.obiba.magma.type.DecimalType;
import org.obiba.magma.type.TextType;

import junit.framework.Assert;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

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

  @Test
  public void getStringVariableCategories() {
    dsFactory.setFile("src/test/resources/org/obiba/magma/datasource/spss/StringCategories.sav");
    Datasource ds = dsFactory.create();
    ds.initialise();
    Variable variable = ds.getValueTable("StringCategories").getVariable("var1");
    assertThat(variable, not(is(nullValue())));
    Set<Category> categories = variable.getCategories();
    assertThat(categories.size(), is(4));
    Collection<String> expectedNames = new HashSet<String>(Arrays.asList(new String[] { "a", "b", "c", "d" }));

    for(Category category : categories) {
      assertThat(expectedNames.contains(category.getName()), is(true));
    }
  }

  @Test
  public void createUserDefinedEntityType() {
    try {
      File file = new File(
          getClass().getClassLoader().getResource("org/obiba/magma/datasource/spss/StringCategories.sav").toURI());

      dsFactory.addFile(file);
      dsFactory.setEntityType("Patate");
      Datasource ds = dsFactory.create();
      assertThat(ds, not(is(nullValue())));
      ds.initialise();
      ValueTable valueTable = ds.getValueTable("StringCategories");
      assertThat(valueTable, not(is(nullValue())));
      assertThat(valueTable.getEntityType(), is("Patate"));
    } catch(Exception e) {
      fail();
    }
  }
}
