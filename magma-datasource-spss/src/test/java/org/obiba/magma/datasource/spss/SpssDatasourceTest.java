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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.junit.After;
import org.junit.Assert;
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
import org.obiba.magma.support.DatasourceParsingException;
import org.obiba.magma.type.DecimalType;
import org.obiba.magma.type.TextType;

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
    try {
      dsFactory.setFile(getResourceFile("org/obiba/magma/datasource/spss/DatabaseTest.sav"));
      Datasource ds = dsFactory.create();
      Assert.assertEquals(SpssDatasourceFactory.DEFAULT_DATASOURCE_NAME, ds.getName());
    } catch(Exception e) {
      fail();
    }

  }

  @Test
  public void createDatasourceWithInvalidFile() {
    try {
      dsFactory.setFile(getResourceFile("org/obiba/magma/datasource/spss/DatabaseTest.sav"));
      Datasource ds = dsFactory.create();
      Assert.assertEquals(SpssDatasourceFactory.DEFAULT_DATASOURCE_NAME, ds.getName());
    } catch(Exception e) {
      fail();
    }
  }

  @Test
  public void createDatasourceWithTwoFiles() {
    try {
      File file1 = getResourceFile("org/obiba/magma/datasource/spss/DatabaseTest.sav");
      File file2 = getResourceFile("org/obiba/magma/datasource/spss/StringCategories.sav");

      dsFactory.addFile(file1);
      dsFactory.addFile(file2);

      Datasource ds = dsFactory.create();
      Assert.assertEquals(SpssDatasourceFactory.DEFAULT_DATASOURCE_NAME, ds.getName());
    } catch(Exception e) {
      fail();
    }
  }

  @Test
  public void initilizeDatasourceWithValidFile() {
    try {
      dsFactory.setFile(getResourceFile("org/obiba/magma/datasource/spss/DatabaseTest.sav"));
      Datasource ds = dsFactory.create();
      ds.initialise();
      Assert.assertEquals(SpssDatasourceFactory.DEFAULT_DATASOURCE_NAME, ds.getName());
    } catch(Exception e) {
      fail();
    }
  }

  @Test(expected = DatasourceParsingException.class)
  public void initilizeDatasourceWithInvalidFile() {
    dsFactory.setFile("org/obiba/magma/datasource/spss/DatabaseTest");
    Datasource ds = dsFactory.create();
    ds.initialise();
  }

  @Test
  public void getValueTableDatasourceWithValidFile() {
    try {
      dsFactory.setFile(getResourceFile("org/obiba/magma/datasource/spss/StringCategories.sav"));
      Datasource ds = dsFactory.create();
      ds.initialise();
      Assert.assertNotNull(ds.getValueTable("StringCategories"));
    } catch(Exception e) {
      fail();
    }
  }

  @Test
  public void getValueTableDatasourceWithColonFileName() {
    try {
      dsFactory.setFile(getResourceFile("org/obiba/magma/datasource/spss/Database:Test.sav"));
      Datasource ds = dsFactory.create();
      ds.initialise();
      Assert.assertNotNull(ds.getValueTable("DatabaseTest"));
    } catch(Exception e) {
      fail();
    }
  }

  @Test
  public void getVariableFromValueTable() {
    try {
      dsFactory.setFile(getResourceFile("org/obiba/magma/datasource/spss/DatabaseTest.sav"));
      Datasource ds = dsFactory.create();
      ds.initialise();
      Assert.assertNotNull(ds.getValueTable("DatabaseTest").getVariable("race"));
    } catch(Exception e) {
      fail();
    }

  }

  @Test
  public void getVariableFromValueTableWithFrenchChars() {
    try {
      dsFactory.setFile(getResourceFile("org/obiba/magma/datasource/spss/dictionnaire_variablesT4-simple.sav"));
      Datasource ds = dsFactory.create();
      ds.initialise();
      Assert.assertNotNull(ds.getValueTable("dictionnaire_variablesT4-simple").getVariable("ETATCIT4"));
    } catch(URISyntaxException e) {
      fail();
    }
  }

  @Test(expected = NoSuchVariableException.class)
  public void getVariableInUpperCaseFromValueTable() {
    try {
      dsFactory.setFile(getResourceFile("org/obiba/magma/datasource/spss/DatabaseTest.sav"));
      Datasource ds = dsFactory.create();
      ds.initialise();
      Assert.assertNotNull(ds.getValueTable("DatabaseTest").getVariable("RACE"));
    } catch(URISyntaxException e) {
      fail();
    }
  }

  @Test(expected = NoSuchVariableException.class)
  public void getInvalidVariableFromValueTable() {
    try {
      dsFactory.setFile(getResourceFile("org/obiba/magma/datasource/spss/DatabaseTest.sav"));
      Datasource ds = dsFactory.create();
      ds.initialise();
      Assert.assertNotNull(ds.getValueTable("DatabaseTest").getVariable("blabla"));
    } catch(URISyntaxException e) {
      fail();
    }
  }

  @Test
  public void getDecimalVariableTypeFromValueTable() {
    try {
      dsFactory.setFile(getResourceFile("org/obiba/magma/datasource/spss/RobotChicken.sav"));
      Datasource ds = dsFactory.create();
      ds.initialise();
      Assert.assertEquals(DecimalType.class,
          ds.getValueTable("RobotChicken").getVariable("VarDecimal").getValueType().getClass());
    } catch(Exception e) {
      fail();
    }
  }

  @Test
  public void getQYDVariableTypeFromValueTable() {
    try {
      dsFactory.setFile(getResourceFile("org/obiba/magma/datasource/spss/RobotChicken.sav"));
      Datasource ds = dsFactory.create();
      ds.initialise();
      Assert.assertEquals(TextType.class,
          ds.getValueTable("RobotChicken").getVariable("VarQuarterly").getValueType().getClass());
    } catch(Exception e) {
      fail();
    }
  }

  @Test
  public void getCustomCurrencyVariableTypeFromValueTable() {
    try {
      dsFactory.setFile(getResourceFile("org/obiba/magma/datasource/spss/RobotChicken.sav"));
      Datasource ds = dsFactory.create();
      ds.initialise();
      Assert.assertEquals(TextType.class,
          ds.getValueTable("RobotChicken").getVariable("VarCurrency").getValueType().getClass());
    } catch(Exception e) {
      fail();
    }
  }

  @Test
  public void getVariableEntitiesVariableEntityProvider() {
    try {
      dsFactory.setFile(getResourceFile("org/obiba/magma/datasource/spss/DatabaseTest.sav"));
      Datasource ds = dsFactory.create();
      ds.initialise();

      ValueTable valueTable = ds.getValueTable("DatabaseTest");

      Set<VariableEntity> variableEntities = valueTable.getVariableEntities();
      assertTrue(variableEntities.size() == 200);
    } catch(Exception e) {
      fail();
    }
  }

  @Test
  public void getValueSetForGivenEntity() {
    try {
      dsFactory.setFile(getResourceFile("org/obiba/magma/datasource/spss/DatabaseTest.sav"));
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
        assertTrue(value.compareTo(expected) == 0);
      }
    } catch(Exception e) {
      fail();
    }
  }

  @Test
  public void getValueSetForGivenEntityLargeDatasource() {
    try {
      dsFactory.setFile(getResourceFile("org/obiba/magma/datasource/spss/DatabaseTest.sav"));
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
        assertTrue(value.compareTo(expected) == 0);
      }
    } catch(Exception e) {
      fail();
    }
  }

  @Test
  public void getStringVariableCategories() {
    try {
      dsFactory.setFile(getResourceFile("org/obiba/magma/datasource/spss/StringCategories.sav"));
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
    } catch(Exception e) {
      fail();
    }
  }

  @Test
  public void createUserDefinedEntityType() {
    try {
      dsFactory.addFile(getResourceFile("org/obiba/magma/datasource/spss/StringCategories.sav"));
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

  @Test
  public void readScientificNotationVarType() {
    try {
      dsFactory.addFile(getResourceFile("org/obiba/magma/datasource/spss/ScientificNotationVar.sav"));
      dsFactory.setEntityType("Patate");
      Datasource ds = dsFactory.create();
      ds.initialise();
      ValueTable valueTable = ds.getValueTable("ScientificNotationVar");
      assertThat(valueTable, not(is(nullValue())));
      Iterator<VariableEntity> iterator = valueTable.getVariableEntities().iterator();

      if(iterator.hasNext()) {
        SpssValueSet valueSet = (SpssValueSet) valueTable.getValueSet(iterator.next());
        Value value = valueSet.getValue(ds.getValueTable("ScientificNotationVar").getVariable("num"));
        Assert.assertNotNull(value);
        Assert.assertFalse(value.isNull());
        Value expected = DecimalType.get().valueOf(1E+010);
        assertTrue(value.compareTo(expected) == 0);
      }

    } catch(Exception e) {
      fail();
    }
  }

  @Test
  public void testCategoryWithLocale() {
    try {
      dsFactory.addFile(getResourceFile("org/obiba/magma/datasource/spss/DatabaseTest.sav"));
      dsFactory.setLocale("no");
      Datasource ds = dsFactory.create();
      ds.initialise();
      Variable variable = ds.getValueTable("DatabaseTest").getVariable("race");
      assertThat(variable, not(is(nullValue())));

      for(Category category : variable.getCategories()) {
        assertThat(category.getAttribute("label").getLocale().getLanguage(), is("no"));
      }
    } catch(Exception e) {
      fail();
    }

  }

  @Test
  public void testCategoryWithoutLocaleEntityTypeCharset() {
    try {
      List<File> files = new ArrayList<File>();
      files.add(getResourceFile("org/obiba/magma/datasource/spss/DatabaseTest.sav"));
      Datasource ds = new SpssDatasource("spss", files, null, null, null);
      ds.initialise();
      Variable variable = ds.getValueTable("DatabaseTest").getVariable("race");
      assertThat(variable, not(is(nullValue())));

      ValueTable valueTable = ds.getValueTable("DatabaseTest");
      assertThat(valueTable, not(is(nullValue())));
      assertThat(valueTable.getEntityType(), is("Participant"));

      for(Category category : variable.getCategories()) {
        assertThat(category.getAttribute("label").getLocale(), is(nullValue()));
      }
    } catch(Exception e) {
      fail();
    }

  }

  @Test(expected = DatasourceParsingException.class)
  public void testDuplicateIdentifier() {
    try {
      dsFactory.setFile(getResourceFile("org/obiba/magma/datasource/spss/DuplicateIdentifier.sav"));
      Datasource ds = dsFactory.create();
      ds.initialise();
      ValueTable valueTable = ds.getValueTable("DuplicateIdentifier");
      valueTable.getVariableEntities();
    } catch(URISyntaxException e) {
      fail();
    }
  }

  @Test(expected = DatasourceParsingException.class)
  public void testInvalidEntityDueToVariableValueOverflow() {
    try {
      dsFactory.setFile(getResourceFile("org/obiba/magma/datasource/spss/OverflowVarValue.sav"));
      Datasource ds = dsFactory.create();
      ds.initialise();
      ValueTable valueTable = ds.getValueTable("OverflowVarValue");
      valueTable.getVariableEntities();
    } catch(URISyntaxException e) {
      fail();
    }
  }

  private File getResourceFile(String resourcePath) throws URISyntaxException {
    return new File(getClass().getClassLoader().getResource(resourcePath).toURI());
  }
}
