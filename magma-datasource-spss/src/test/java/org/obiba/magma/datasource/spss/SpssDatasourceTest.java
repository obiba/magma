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
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.obiba.magma.Category;
import org.obiba.magma.Datasource;
import org.obiba.magma.MagmaEngine;
import org.obiba.magma.MagmaRuntimeException;
import org.obiba.magma.NoSuchVariableException;
import org.obiba.magma.Value;
import org.obiba.magma.ValueTable;
import org.obiba.magma.Variable;
import org.obiba.magma.VariableEntity;
import org.obiba.magma.datasource.spss.support.SpssDatasourceFactory;
import org.obiba.magma.datasource.spss.support.SpssDatasourceParsingException;
import org.obiba.magma.support.DatasourceParsingException;
import org.obiba.magma.support.EntitiesPredicate;
import org.obiba.magma.type.DecimalType;
import org.obiba.magma.type.TextType;

import static org.fest.assertions.api.Assertions.assertThat;

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
  public void createDatasourceWithValidFile() throws Exception {
    dsFactory.setFile(getResourceFile("org/obiba/magma/datasource/spss/DatabaseTest.sav"));
    Datasource ds = dsFactory.create();
    assertThat(SpssDatasourceFactory.DEFAULT_DATASOURCE_NAME).isEqualTo(ds.getName());
  }

  @Test
  public void createDatasourceWithInvalidFile() throws Exception {
    dsFactory.setFile(getResourceFile("org/obiba/magma/datasource/spss/DatabaseTest.sav"));
    Datasource ds = dsFactory.create();
    assertThat(SpssDatasourceFactory.DEFAULT_DATASOURCE_NAME).isEqualTo(ds.getName());
  }

  @Test
  public void createDatasourceWithTwoFiles() throws Exception {
    File file1 = getResourceFile("org/obiba/magma/datasource/spss/DatabaseTest.sav");
    File file2 = getResourceFile("org/obiba/magma/datasource/spss/StringCategories.sav");

    dsFactory.addFile(file1);
    dsFactory.addFile(file2);

    Datasource ds = dsFactory.create();
    assertThat(SpssDatasourceFactory.DEFAULT_DATASOURCE_NAME).isEqualTo(ds.getName());
  }

  @Test
  public void initilizeDatasourceWithValidFile() throws Exception {
    dsFactory.setFile(getResourceFile("org/obiba/magma/datasource/spss/DatabaseTest.sav"));
    Datasource ds = dsFactory.create();
    ds.initialise();
    assertThat(SpssDatasourceFactory.DEFAULT_DATASOURCE_NAME).isEqualTo(ds.getName());
  }

  @Test(expected = DatasourceParsingException.class)
  public void initilizeDatasourceWithInvalidFile() throws Exception {
    dsFactory.setFile("org/obiba/magma/datasource/spss/DatabaseTest");
    Datasource ds = dsFactory.create();
    ds.initialise();
  }

  @Test
  public void getValueTableDatasourceWithValidFile() throws Exception {
    dsFactory.setFile(getResourceFile("org/obiba/magma/datasource/spss/StringCategories.sav"));
    Datasource ds = dsFactory.create();
    ds.initialise();
    assertThat(ds.getValueTable("StringCategories")).isNotNull();
  }

  @Test
  public void getValueTableDatasourceWithColonFileName() throws Exception {
    dsFactory.setFile(getResourceFile("org/obiba/magma/datasource/spss/Database:Test.sav"));
    Datasource ds = dsFactory.create();
    ds.initialise();
    assertThat(ds.getValueTable("DatabaseTest")).isNotNull();
  }

  @Test
  public void getVariableFromValueTable() throws Exception {
    dsFactory.setFile(getResourceFile("org/obiba/magma/datasource/spss/DatabaseTest.sav"));
    Datasource ds = dsFactory.create();
    ds.initialise();
    assertThat(ds.getValueTable("DatabaseTest").getVariable("race")).isNotNull();
  }

  @Test
  public void getVariableFromValueTableWithFrenchChars() throws Exception {
    dsFactory.setFile(getResourceFile("org/obiba/magma/datasource/spss/dictionnaire_variablesT4-simple.sav"));
    Datasource ds = dsFactory.create();
    ds.initialise();
    assertThat(ds.getValueTable("dictionnaire_variablesT4-simple").getVariable("ETATCIT4")).isNotNull();
  }

  @Test(expected = NoSuchVariableException.class)
  public void getVariableInUpperCaseFromValueTable() throws Exception {
    dsFactory.setFile(getResourceFile("org/obiba/magma/datasource/spss/DatabaseTest.sav"));
    Datasource ds = dsFactory.create();
    ds.initialise();
    assertThat(ds.getValueTable("DatabaseTest").getVariable("RACE")).isNotNull();
  }

  @Test(expected = NoSuchVariableException.class)
  public void getInvalidVariableFromValueTable() throws Exception {
    dsFactory.setFile(getResourceFile("org/obiba/magma/datasource/spss/DatabaseTest.sav"));
    Datasource ds = dsFactory.create();
    ds.initialise();
    assertThat(ds.getValueTable("DatabaseTest").getVariable("blabla")).isNotNull();
  }

  @Test
  public void getDecimalVariableTypeFromValueTable() throws Exception {
    dsFactory.setFile(getResourceFile("org/obiba/magma/datasource/spss/RobotChicken.sav"));
    Datasource ds = dsFactory.create();
    ds.initialise();
    assertThat(ds.getValueTable("RobotChicken").getVariable("VarDecimal").getValueType())
        .isInstanceOf(DecimalType.class);
  }

  @Test
  public void getQYDVariableTypeFromValueTable() throws Exception {
    dsFactory.setFile(getResourceFile("org/obiba/magma/datasource/spss/RobotChicken.sav"));
    Datasource ds = dsFactory.create();
    ds.initialise();
    assertThat(ds.getValueTable("RobotChicken").getVariable("VarQuarterly").getValueType())
        .isInstanceOf(TextType.class);
  }

  @Test
  public void getCustomCurrencyVariableTypeFromValueTable() throws Exception {
    dsFactory.setFile(getResourceFile("org/obiba/magma/datasource/spss/RobotChicken.sav"));
    Datasource ds = dsFactory.create();
    ds.initialise();
    assertThat(ds.getValueTable("RobotChicken").getVariable("VarCurrency").getValueType()).isInstanceOf(TextType.class);
  }

  @Test
  public void getVariableEntitiesVariableEntityProvider() throws Exception {
    dsFactory.setFile(getResourceFile("org/obiba/magma/datasource/spss/DatabaseTest.sav"));
    Datasource ds = dsFactory.create();
    ds.initialise();

    ValueTable valueTable = ds.getValueTable("DatabaseTest");

    Set<VariableEntity> variableEntities = valueTable.getVariableEntities();
    assertThat(variableEntities).hasSize(200);
  }

  @Test
  public void getValueSetForGivenEntity() throws Exception {
    dsFactory.setFile(getResourceFile("org/obiba/magma/datasource/spss/DatabaseTest.sav"));
    Datasource ds = dsFactory.create();
    ds.initialise();

    ValueTable valueTable = ds.getValueTable("DatabaseTest");
    Iterator<VariableEntity> iterator = valueTable.getVariableEntities().iterator();

    if(iterator.hasNext()) {
      SpssValueSet valueSet = (SpssValueSet) valueTable.getValueSet(iterator.next());
      Value value = valueSet.getValue(ds.getValueTable("DatabaseTest").getVariable("race"));
      assertThat(value).isNotNull();
      assertThat(value.isNull()).isFalse();
      Value expected = DecimalType.get().valueOf(4.0);
      assertThat(value.compareTo(expected)).isEqualTo(0);
    }
  }

  @Test
  public void getValueSetForGivenEntityLargeDatasource() throws Exception {
    dsFactory.setFile(getResourceFile("org/obiba/magma/datasource/spss/DatabaseTest.sav"));
    Datasource ds = dsFactory.create();
    ds.initialise();
    ValueTable valueTable = ds.getValueTable("DatabaseTest");
    Iterator<VariableEntity> iterator = valueTable.getVariableEntities().iterator();

    if(iterator.hasNext()) {
      SpssValueSet valueSet = (SpssValueSet) valueTable.getValueSet(iterator.next());
      Value value = valueSet.getValue(ds.getValueTable("DatabaseTest").getVariable("race"));
      assertThat(value).isNotNull();
      assertThat(value.isNull()).isFalse();
      Value expected = DecimalType.get().valueOf(4.0);
      assertThat(value.compareTo(expected)).isEqualTo(0);
    }
  }

  @Test
  public void getStringVariableCategories() throws Exception {
    dsFactory.setFile(getResourceFile("org/obiba/magma/datasource/spss/StringCategories.sav"));
    Datasource ds = dsFactory.create();
    ds.initialise();
    Variable variable = ds.getValueTable("StringCategories").getVariable("var1");
    assertThat(variable).isNotNull();
    Set<Category> categories = variable.getCategories();
    assertThat(categories).hasSize(4);
    Iterable<String> expectedNames = new HashSet<>(Arrays.asList(new String[] { "a", "b", "c", "d" }));
    for(Category category : categories) {
      assertThat(expectedNames).contains(category.getName());
    }
  }

  @Test
  public void createUserDefinedEntityType() throws Exception {
    dsFactory.addFile(getResourceFile("org/obiba/magma/datasource/spss/StringCategories.sav"));
    dsFactory.setEntityType("Patate");
    Datasource ds = dsFactory.create();
    assertThat(ds).isNotNull();
    ds.initialise();
    ValueTable valueTable = ds.getValueTable("StringCategories");
    assertThat(valueTable).isNotNull();
    assertThat(valueTable.getEntityType()).isEqualTo("Patate");
  }

  @Test
  public void readScientificNotationVarType() throws Exception {
    dsFactory.addFile(getResourceFile("org/obiba/magma/datasource/spss/ScientificNotationVar.sav"));
    dsFactory.setEntityType("Patate");
    Datasource ds = dsFactory.create();
    ds.initialise();
    ValueTable valueTable = ds.getValueTable("ScientificNotationVar");
    assertThat(valueTable).isNotNull();
    Iterator<VariableEntity> iterator = valueTable.getVariableEntities().iterator();

    if(iterator.hasNext()) {
      SpssValueSet valueSet = (SpssValueSet) valueTable.getValueSet(iterator.next());
      Value value = valueSet.getValue(ds.getValueTable("ScientificNotationVar").getVariable("num"));
      assertThat(value).isNotNull();
      assertThat(value.isNull()).isFalse();
      Value expected = DecimalType.get().valueOf(1E+010);
      assertThat(value.compareTo(expected)).isEqualTo(0);
    }
  }

  @Test
  public void testCategoryWithLocale() throws Exception {
    dsFactory.addFile(getResourceFile("org/obiba/magma/datasource/spss/DatabaseTest.sav"));
    dsFactory.setLocale("no");
    Datasource ds = dsFactory.create();
    ds.initialise();
    Variable variable = ds.getValueTable("DatabaseTest").getVariable("race");
    assertThat(variable).isNotNull();
    for(Category category : variable.getCategories()) {
      assertThat(category.getAttribute("label").getLocale().getLanguage()).isEqualTo("no");
    }
  }

  @Test
  public void testCategoryWithoutLocaleEntityTypeCharset() throws Exception {
    List<File> files = new ArrayList<>();
    files.add(getResourceFile("org/obiba/magma/datasource/spss/DatabaseTest.sav"));
    Datasource ds = new SpssDatasource("spss", files, null, null, null);
    ds.initialise();
    Variable variable = ds.getValueTable("DatabaseTest").getVariable("race");
    assertThat(variable).isNotNull();

    ValueTable valueTable = ds.getValueTable("DatabaseTest");
    assertThat(valueTable).isNotNull();
    assertThat(valueTable.getEntityType()).isEqualTo("Participant");

    for(Category category : variable.getCategories()) {
      assertThat(category.getAttribute("label").isLocalised()).isFalse();
    }
  }

  @Test(expected = DatasourceParsingException.class)
  public void testDuplicateIdentifier() throws Exception {
    dsFactory.setFile(getResourceFile("org/obiba/magma/datasource/spss/DuplicateIdentifier.sav"));
    Datasource ds = dsFactory.create();
    ds.initialise();
    ValueTable valueTable = ds.getValueTable("DuplicateIdentifier");
    valueTable.getVariableEntities();
  }

  @Test(expected = DatasourceParsingException.class)
  public void testInvalidEntityDueToVariableValueOverflow() throws Exception {
    dsFactory.setFile(getResourceFile("org/obiba/magma/datasource/spss/OverflowVarValue.sav"));
    Datasource ds = dsFactory.create();
    ds.initialise();
    ValueTable valueTable = ds.getValueTable("OverflowVarValue");
    valueTable.getVariableEntities();
  }

  @Test
  public void testHasNoEntities() throws Exception {
    dsFactory.setFile(getResourceFile("org/obiba/magma/datasource/spss/empty.sav"));
    Datasource ds = dsFactory.create();
    ds.initialise();
    assertThat(ds.hasEntities(new EntitiesPredicate.NonViewEntitiesPredicate())).isFalse();
  }

  @Test
  public void testHasEntities() throws URISyntaxException {
    dsFactory.setFile(getResourceFile("org/obiba/magma/datasource/spss/DatabaseTest.sav"));
    Datasource ds = dsFactory.create();
    ds.initialise();
    assertThat(ds.hasEntities(new EntitiesPredicate.NonViewEntitiesPredicate())).isTrue();
  }

  @Test(expected = SpssDatasourceParsingException.class)
  public void testInvalidVariableValueCharset() throws URISyntaxException {
    dsFactory.setFile(getResourceFile("org/obiba/magma/datasource/spss/invalid-var-value.sav"));
    Datasource ds = dsFactory.create();
    ds.initialise();
    ValueTable valueTable = ds.getValueTable("invalid-var-value");
    assertThat(valueTable).isNotNull();
    Iterator<VariableEntity> iterator = valueTable.getVariableEntities().iterator();

    if(iterator.hasNext()) {
      SpssValueSet valueSet = (SpssValueSet) valueTable.getValueSet(iterator.next());
      valueSet.getValue(ds.getValueTable("invalid-var-value").getVariable("var1"));
    }
  }

  @Test(expected = DatasourceParsingException.class)
  public void testInvalidVariableCategoryValueCharset() throws URISyntaxException {
    dsFactory.setFile(getResourceFile("org/obiba/magma/datasource/spss/invalid-category-value.sav"));
    Datasource ds = dsFactory.create();
    ds.initialise();
  }

  @Test
  public void testInvalidVariableValueType() throws URISyntaxException {
    dsFactory.setFile(getResourceFile("org/obiba/magma/datasource/spss/invalid-variable-value-type.sav"));
    Datasource ds = dsFactory.create();
    ds.initialise();
    ValueTable valueTable = ds.getValueTable("invalid-variable-value-type");
    assertThat(valueTable).isNotNull();
    Iterator<VariableEntity> iterator = valueTable.getVariableEntities().iterator();

    if(iterator.hasNext()) {
      SpssValueSet valueSet = (SpssValueSet) valueTable.getValueSet(iterator.next());
      Value v = valueSet.getValue(ds.getValueTable("invalid-variable-value-type").getVariable("var1"));
      System.out.println(v.getValue());
    }

  }

  @SuppressWarnings("ConstantConditions")
  private File getResourceFile(String resourcePath) throws URISyntaxException {
    return new File(getClass().getClassLoader().getResource(resourcePath).toURI());
  }
}
