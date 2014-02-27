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
import org.obiba.magma.NoSuchVariableException;
import org.obiba.magma.Value;
import org.obiba.magma.ValueTable;
import org.obiba.magma.Variable;
import org.obiba.magma.VariableEntity;
import org.obiba.magma.datasource.spss.support.SpssDatasourceFactory;
import org.obiba.magma.datasource.spss.support.SpssDatasourceParsingException;
import org.obiba.magma.support.DatasourceParsingException;
import org.obiba.magma.support.EntitiesPredicate;
import org.obiba.magma.type.DateType;
import org.obiba.magma.type.DecimalType;
import org.obiba.magma.type.TextType;

import static org.fest.assertions.api.Assertions.assertThat;
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
  public void testCreateDatasourceWithValidFile() throws Exception {
    dsFactory.setFile(getResourceFile("org/obiba/magma/datasource/spss/DatabaseTest.sav"));
    Datasource ds = dsFactory.create();
    assertThat(SpssDatasourceFactory.DEFAULT_DATASOURCE_NAME).isEqualTo(ds.getName());
  }

  @Test
  public void textCreateDatasourceWithInvalidFile() throws Exception {
    dsFactory.setFile(getResourceFile("org/obiba/magma/datasource/spss/DatabaseTest.sav"));
    Datasource ds = dsFactory.create();
    assertThat(SpssDatasourceFactory.DEFAULT_DATASOURCE_NAME).isEqualTo(ds.getName());
  }

  @Test
  public void testCreateDatasourceWithTwoFiles() throws Exception {
    File file1 = getResourceFile("org/obiba/magma/datasource/spss/DatabaseTest.sav");
    File file2 = getResourceFile("org/obiba/magma/datasource/spss/StringCategories.sav");

    dsFactory.addFile(file1);
    dsFactory.addFile(file2);

    Datasource ds = dsFactory.create();
    assertThat(SpssDatasourceFactory.DEFAULT_DATASOURCE_NAME).isEqualTo(ds.getName());
  }

  @Test
  public void testInitilizeDatasourceWithValidFile() throws Exception {
    dsFactory.setFile(getResourceFile("org/obiba/magma/datasource/spss/DatabaseTest.sav"));
    Datasource ds = dsFactory.create();
    ds.initialise();
    assertThat(SpssDatasourceFactory.DEFAULT_DATASOURCE_NAME).isEqualTo(ds.getName());
  }

  @Test(expected = DatasourceParsingException.class)
  public void testInitilizeDatasourceWithInvalidFile() throws Exception {
    dsFactory.setFile("org/obiba/magma/datasource/spss/DatabaseTest");
    Datasource ds = dsFactory.create();
    ds.initialise();
  }

  @Test
  public void testGetValueTableDatasourceWithValidFile() throws Exception {
    dsFactory.setFile(getResourceFile("org/obiba/magma/datasource/spss/DatabaseTest.sav"));
    Datasource ds = dsFactory.create();
    ds.initialise();
    assertThat(ds.getValueTable("DatabaseTest")).isNotNull();
  }

  @Test
  public void testGetValueTableDatasourceWithColonFileName() throws Exception {
    dsFactory.setFile(getResourceFile("org/obiba/magma/datasource/spss/Database:Test.sav"));
    Datasource ds = dsFactory.create();
    ds.initialise();
    assertThat(ds.getValueTable("DatabaseTest")).isNotNull();
  }

  @Test
  public void testGetVariableFromValueTable() throws Exception {
    dsFactory.setFile(getResourceFile("org/obiba/magma/datasource/spss/DatabaseTest.sav"));
    Datasource ds = dsFactory.create();
    ds.initialise();
    assertThat(ds.getValueTable("DatabaseTest").getVariable("race")).isNotNull();
  }

  @Test
  public void testGetVariableFromValueTableWithFrenchChars() throws Exception {
    dsFactory.setFile(getResourceFile("org/obiba/magma/datasource/spss/dictionnaire_variables-french-characters.sav"));
    Datasource ds = dsFactory.create();
    ds.initialise();
    assertThat(ds.getValueTable("dictionnaire_variables-french-characters").getVariable("ETATCIT4")).isNotNull();
  }

  @Test(expected = NoSuchVariableException.class)
  public void testGetInvalidVariableFromValueTable() throws Exception {
    dsFactory.setFile(getResourceFile("org/obiba/magma/datasource/spss/DatabaseTest.sav"));
    Datasource ds = dsFactory.create();
    ds.initialise();
    assertThat(ds.getValueTable("DatabaseTest").getVariable("blabla")).isNotNull();
  }

  @Test(expected = NoSuchVariableException.class)
  public void testGetVariableInUpperCaseFromValueTable() throws Exception {
    dsFactory.setFile(getResourceFile("org/obiba/magma/datasource/spss/DatabaseTest.sav"));
    Datasource ds = dsFactory.create();
    ds.initialise();
    assertThat(ds.getValueTable("DatabaseTest").getVariable("RACE")).isNotNull();
  }

  @Test
  public void testGetDecimalVariableTypeFromValueTable() throws Exception {
    dsFactory.setFile(getResourceFile("org/obiba/magma/datasource/spss/spss-variable-types.sav"));
    Datasource ds = dsFactory.create();
    ds.initialise();
    assertThat(ds.getValueTable("spss-variable-types").getVariable("VarDecimal").getValueType())
        .isInstanceOf(DecimalType.class);
  }

  @Test
  public void testGetQYDVariableTypeFromValueTable() throws Exception {
    dsFactory.setFile(getResourceFile("org/obiba/magma/datasource/spss/spss-variable-types.sav"));
    Datasource ds = dsFactory.create();
    ds.initialise();
    assertThat(ds.getValueTable("spss-variable-types").getVariable("VarQuarterly").getValueType())
        .isInstanceOf(TextType.class);
  }

  @Test
  public void testGetCustomCurrencyVariableTypeFromValueTable() throws Exception {
    dsFactory.setFile(getResourceFile("org/obiba/magma/datasource/spss/spss-variable-types.sav"));
    Datasource ds = dsFactory.create();
    ds.initialise();
    assertThat(ds.getValueTable("spss-variable-types").getVariable("VarCurrency").getValueType())
        .isInstanceOf(TextType.class);
  }

  @Test
  public void testGetVariableEntitiesVariableEntityProvider() throws Exception {
    dsFactory.setFile(getResourceFile("org/obiba/magma/datasource/spss/DatabaseTest.sav"));
    Datasource ds = dsFactory.create();
    ds.initialise();

    ValueTable valueTable = ds.getValueTable("DatabaseTest");

    Set<VariableEntity> variableEntities = valueTable.getVariableEntities();
    assertThat(variableEntities).hasSize(200);
  }

  @Test
  public void testGetValueSetForGivenEntity() throws Exception {
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

//  TODO comment out until we find a large file.
//  @Test
//  public void testGetValueSetForGivenEntityLargeDatasource() throws Exception {
//    dsFactory.setFile(getResourceFile("org/obiba/magma/datasource/spss/large-file.sav"));
//    Datasource ds = dsFactory.create();
//    ds.initialise();
//    ValueTable valueTable = ds.getValueTable("large-file");
//    Iterator<VariableEntity> iterator = valueTable.getVariableEntities().iterator();
//
//    if(iterator.hasNext()) {
//      SpssValueSet valueSet = (SpssValueSet) valueTable.getValueSet(iterator.next());
//      Value value = valueSet.getValue(ds.getValueTable("large-file").getVariable("id"));
//      assertThat(value).isNotNull();
//      assertThat(value.isNull()).isFalse();
//      Value expected = TextType.get().valueOf("var001");
//      assertThat(value.compareTo(expected)).isEqualTo(0);
//    }
//  }

  @Test
  public void testGetStringVariableCategories() throws Exception {
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

  @Test
  public void testInvalidVariableValueCharset() throws URISyntaxException {
    try {
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
      fail("Must have thrown DatasourceParsingException");
    } catch(SpssDatasourceParsingException e) {
      assertThat(e.getMessage()).startsWith("Invalid characters in variable value")
          .contains("(Data info: variable='var1'").contains("String with invalid characters");
    }

  }

  @Test
  public void testInvalidVariableCategoryValueCharset() throws URISyntaxException {
    try {
      dsFactory.setFile(getResourceFile("org/obiba/magma/datasource/spss/invalid-category-value.sav"));
      Datasource ds = dsFactory.create();
      ds.initialise();
    } catch(DatasourceParsingException e) {
      assertThat(e.hasChildren()).isTrue();
      for(DatasourceParsingException ch : e.getChildren()) {
        assertThat(ch.getMessage()).startsWith("Failed to create variable value source.")
            .contains("(Variable info: name='var1'").contains("(String with invalid characters");
      }
    }
  }

  @Test
  public void testInvalidVariableCategoryNameCharset() throws URISyntaxException {
    try {
      dsFactory.setFile(getResourceFile("org/obiba/magma/datasource/spss/invalid-category-name.sav"));
      Datasource ds = dsFactory.create();
      ds.initialise();
      fail("Must have thrown DatasourceParsingException");
    } catch(DatasourceParsingException e) {
      assertThat(e.hasChildren()).isTrue();
      for(DatasourceParsingException ch : e.getChildren()) {
        assertThat(ch.getMessage()).startsWith("Invalid characters found for category")
            .contains("(Variable info: name='var1'").contains("String with invalid characters");
      }
    }
  }

  @Test(expected = NullPointerException.class)
  public void testVariableValueOverflow() throws URISyntaxException {
    dsFactory.setFile(getResourceFile("org/obiba/magma/datasource/spss/variable-value-overflow.sav"));
    Datasource ds = dsFactory.create();
    ds.initialise();
    ValueTable valueTable = ds.getValueTable("variable-value-overflow");
    assertThat(valueTable).isNotNull();
    Iterator<VariableEntity> iterator = valueTable.getVariableEntities().iterator();

    if(iterator.hasNext()) {
      SpssValueSet valueSet = (SpssValueSet) valueTable.getValueSet(iterator.next());
      Value v = valueSet.getValue(ds.getValueTable("variable-value-overflow").getVariable("var1"));
      v.getValue();
    }

  }

  @Test
  public void testInvalidEntityVariable() throws URISyntaxException {
    try {
      dsFactory.setFile(getResourceFile("org/obiba/magma/datasource/spss/invalid-entity-variable.sav"));
      Datasource ds = dsFactory.create();
      ds.initialise();
      ValueTable valueTable = ds.getValueTable("invalid-entity-variable");
      assertThat(valueTable).isNotNull();
      valueTable.getVariableEntities().iterator();
      fail("Must have thrown DatasourceParsingException");
    } catch(SpssDatasourceParsingException e) {
      assertThat(e.getMessage()).startsWith("Invalid characters in variable value")
          .contains("(Data info: variable='var1'").contains("String with invalid characters");
    }

  }

  @Test
  public void testInvalidVariableAttribute() throws Exception {
    try {
      dsFactory.addFile(getResourceFile("org/obiba/magma/datasource/spss/invalid-variable-attribute.sav"));
      Datasource ds = dsFactory.create();
      ds.initialise();
    } catch(DatasourceParsingException e) {
      assertThat(e.hasChildren()).isTrue();
      for(DatasourceParsingException ch : e.getChildren()) {
        assertThat(ch.getMessage()).startsWith("Failed to create variable value source.")
            .contains("(Variable info: name='var1'").contains("String with invalid characters");
      }
    }
  }

  @Test
  public void testInvalidMissingValue() throws Exception {
    try {
      dsFactory.addFile(getResourceFile("org/obiba/magma/datasource/spss/invalid-missing-value.sav"));
      Datasource ds = dsFactory.create();
      ds.initialise();
    } catch(DatasourceParsingException e) {
      assertThat(e.hasChildren()).isTrue();
      for(DatasourceParsingException ch : e.getChildren()) {
        assertThat(ch.getMessage()).startsWith("Invalid characters found for category")
            .contains("(Variable info: name='var1'").contains("String with invalid characters");
      }
    }
  }

  @Test
  public void testConvertADateToMagmaDate() throws Exception {
    dsFactory.addFile(getResourceFile("org/obiba/magma/datasource/spss/date-value-types.sav"));
    Datasource ds = dsFactory.create();
    ds.initialise();
    ValueTable valueTable = ds.getValueTable("date-value-types");
    assertThat(valueTable).isNotNull();
    Iterator<VariableEntity> iterator = valueTable.getVariableEntities().iterator();

    if(iterator.hasNext()) {
      SpssValueSet valueSet = (SpssValueSet) valueTable.getValueSet(iterator.next());
      assertThat(valueSet.getValue(valueTable.getVariable("var1")).compareTo(DateType.get().valueOf("2013-4-15")))
          .isEqualTo(0);
      assertThat(valueSet.getValue(valueTable.getVariable("var2")).compareTo(DateType.get().valueOf("2012-12-30")))
          .isEqualTo(0);
      assertThat(valueSet.getValue(valueTable.getVariable("var3")).compareTo(DateType.get().valueOf("2014-03-12")))
          .isEqualTo(0);
      assertThat(valueSet.getValue(valueTable.getVariable("var4")).compareTo(DateType.get().valueOf("2010-09-23")))
          .isEqualTo(0);
      assertThat(valueSet.getValue(valueTable.getVariable("var5")).compareTo(DateType.get().valueOf("2010-03-12")))
          .isEqualTo(0);
      assertThat(valueSet.getValue(valueTable.getVariable("var6")).compareTo(DateType.get().valueOf("2010-02-12")))
          .isEqualTo(0);
    }
  }

  @SuppressWarnings("ConstantConditions")
  private File getResourceFile(String resourcePath) throws URISyntaxException {
    return new File(getClass().getClassLoader().getResource(resourcePath).toURI());
  }
}
