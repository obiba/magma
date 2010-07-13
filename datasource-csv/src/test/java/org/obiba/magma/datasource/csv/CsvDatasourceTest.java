package org.obiba.magma.datasource.csv;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;

import java.io.File;
import java.io.IOException;
import java.util.Locale;

import junit.framework.Assert;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.obiba.magma.Category;
import org.obiba.magma.Datasource;
import org.obiba.magma.MagmaEngine;
import org.obiba.magma.MagmaRuntimeException;
import org.obiba.magma.Value;
import org.obiba.magma.ValueSet;
import org.obiba.magma.ValueTable;
import org.obiba.magma.ValueTableWriter;
import org.obiba.magma.Variable;
import org.obiba.magma.VariableEntity;
import org.obiba.magma.ValueTableWriter.ValueSetWriter;
import org.obiba.magma.ValueTableWriter.VariableWriter;
import org.obiba.magma.support.VariableEntityBean;
import org.obiba.magma.type.TextType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Iterables;

public class CsvDatasourceTest {

  private static final Logger log = LoggerFactory.getLogger(CsvDatasourceTest.class);

  @Before
  public void before() {
    new MagmaEngine();
  }

  @After
  public void after() {
    MagmaEngine.get().shutdown();
  }

  @Test
  public void testTable1VariableRead() {
    CsvDatasource datasource = new CsvDatasource("csv-datasource").addValueTable("Table1", //
    new File("src/test/resources/Table1/variables.csv"), //
    new File("src/test/resources/Table1/data.csv"));
    datasource.initialise();
    Assert.assertEquals(1, datasource.getValueTableNames().size());

    ValueTable table = datasource.getValueTable("Table1");
    Assert.assertNotNull(table);
    Assert.assertEquals("Participant", table.getEntityType());

    Variable var = table.getVariable("var1");
    Assert.assertNotNull(var);
    Assert.assertEquals("text", var.getValueType().getName());
    Assert.assertEquals("Participant", var.getEntityType());
    Assert.assertNull(var.getMimeType());
    Assert.assertNull(var.getUnit());
    Assert.assertNull(var.getOccurrenceGroup());
    Assert.assertFalse(var.isRepeatable());

    Assert.assertEquals(4, var.getCategories().size());
    for(Category category : var.getCategories()) {
      if(category.getName().equals("Y")) {
        Assert.assertEquals("yes", category.getAttribute("label", Locale.ENGLISH).getValue().toString());
      } else if(category.getName().equals("N")) {
        Assert.assertEquals("no", category.getAttribute("label", Locale.ENGLISH).getValue().toString());
      } else if(category.getName().equals("PNA")) {
        Assert.assertEquals("prefer not to answer", category.getAttribute("label", Locale.ENGLISH).getValue().toString());
      } else if(category.getName().equals("DNK")) {
        Assert.assertEquals("don't know", category.getAttribute("label", Locale.ENGLISH).getValue().toString());
      } else {
        Assert.assertFalse(true);
      }
    }

    Assert.assertEquals(1, var.getAttributes().size());
    Assert.assertTrue(var.hasAttribute("label"));
    Assert.assertEquals("Hello I'm variable one", var.getAttribute("label", Locale.ENGLISH).getValue().toString());
  }

  @Test
  public void testTable1DataRead() {
    CsvDatasource datasource = new CsvDatasource("csv-datasource").addValueTable("Table1", //
    new File("src/test/resources/Table1/variables.csv"), //
    new File("src/test/resources/Table1/data.csv"));
    datasource.initialise();
    Assert.assertEquals(1, datasource.getValueTableNames().size());

    ValueTable table = datasource.getValueTable("Table1");
    Assert.assertNotNull(table);
    Assert.assertEquals("Participant", table.getEntityType());

    Variable var = table.getVariable("var1");

    for(ValueSet valueSet : table.getValueSets()) {
      String identifier = valueSet.getVariableEntity().getIdentifier();
      Value value = table.getValue(var, valueSet);
      log.debug("var1[{}]={}", identifier, value);
      Assert.assertEquals("text", value.getValueType().getName());
      if(identifier.equals("1")) {
        Assert.assertEquals("Y", value.getValue());
      } else if(identifier.equals("2")) {
        Assert.assertEquals("N", value.getValue());
      } else if(identifier.equals("3")) {
        Assert.assertEquals("PNA", value.getValue());
      } else if(identifier.equals("4")) {
        Assert.assertEquals("DNK", value.getValue());
      } else {
        Assert.assertFalse(true);
      }
    }
  }

  @Test
  public void testReadingDataOnlyTableHasOnlyOneTable() throws Exception {
    CsvDatasource datasource = new CsvDatasource("csv-datasource").addValueTable("TableDataOnly", //
    null, //
    new File("src/test/resources/TableDataOnly/data.csv"));
    datasource.initialise();

    assertThat(datasource.getValueTableNames().size(), is(1));
  }

  @Test
  public void testReadingDataOnlyTableIsNotNull() throws Exception {
    CsvDatasource datasource = new CsvDatasource("csv-datasource").addValueTable("TableDataOnly", //
    null, //
    new File("src/test/resources/TableDataOnly/data.csv"));
    datasource.initialise();

    assertThat(datasource.getValueTable("TableDataOnly"), notNullValue());
  }

  @Test
  public void testReadingDataOnlyTableEntityTypeIsDefaultParticipant() throws Exception {
    CsvDatasource datasource = new CsvDatasource("csv-datasource").addValueTable("TableDataOnly", //
    null, //
    new File("src/test/resources/TableDataOnly/data.csv"));
    datasource.initialise();
    ValueTable table = datasource.getValueTable("TableDataOnly");

    assertThat(table.getEntityType(), is(CsvValueTable.DEFAULT_ENTITY_TYPE));
  }

  @Test
  public void testReadingDataOnlyFavouriteIcecreamVariableExists() throws Exception {
    CsvDatasource datasource = new CsvDatasource("csv-datasource").addValueTable("TableDataOnly", //
    null, //
    new File("src/test/resources/TableDataOnly/data.csv"));
    datasource.initialise();
    ValueTable table = datasource.getValueTable("TableDataOnly");

    assertThat(table.getVariable("FavouriteIcecream"), notNullValue());
  }

  @Test
  public void testReadingSingleDataOnlyTableNullIcecreamValue() throws Exception {
    CsvDatasource datasource = new CsvDatasource("csv-datasource").addValueTable("TableDataOnly", //
    null, //
    new File("src/test/resources/TableDataOnly/data.csv"));
    datasource.initialise();
    ValueTable table = datasource.getValueTable("TableDataOnly");

    Variable favouriteIcecream = table.getVariable("FavouriteIcecream");

    for(ValueSet valueSet : table.getValueSets()) {
      String identifier = valueSet.getVariableEntity().getIdentifier();
      Value value = table.getValue(favouriteIcecream, valueSet);
      if(identifier.equals("1")) {
        assertThat(value.getValue(), nullValue());
      } else if(identifier.equals("2")) {
        assertThat(value.getValue(), nullValue());
      }
    }
  }

  @Test
  public void testReadingDataOnlyValueTypeIsText() throws Exception {
    CsvDatasource datasource = new CsvDatasource("csv-datasource").addValueTable("TableDataOnly", //
    null, //
    new File("src/test/resources/TableDataOnly/data.csv"));
    datasource.initialise();
    ValueTable table = datasource.getValueTable("TableDataOnly");

    for(Variable variable : table.getVariables()) {
      assertThat(variable.getValueType().getName(), is(TextType.get().getName()));
    }
  }

  @Test
  public void testWritingDataOnlyOneTextVariable() throws Exception {
    File tempTestDirectory = new TempTableBuilder("TableDataOnly").addData().build();

    CsvDatasource datasource = new CsvDatasource("csv-datasource").addValueTable("TableDataOnly", //
    null, //
    new File(tempTestDirectory.getCanonicalFile() + "/TableDataOnly", "data.csv"));
    datasource.initialise();

    VariableEntity variableEntity = new VariableEntityBean("Participant", "1");
    Variable testVariable = Variable.Builder.newVariable("test-variable", TextType.get(), "Participant").build();
    Value secondCup = TextType.get().valueOf("Second Cup");

    ValueTableWriter writer = datasource.createWriter("TableDataOnly", "Participant");
    ValueSetWriter vsw = writer.writeValueSet(variableEntity);
    vsw.writeValue(testVariable, secondCup);
    vsw.close();

    writer.close();

    CsvDatasource readDatasource = new CsvDatasource("read-csv-datasource").addValueTable("TableDataOnly", //
    null, //
    new File(tempTestDirectory.getCanonicalFile() + "/TableDataOnly", "data.csv"));
    readDatasource.initialise();

    ValueTable table = readDatasource.getValueTable("TableDataOnly");

    Variable variable = table.getVariable("test-variable");

    for(ValueSet valueSet : table.getValueSets()) {
      Value value = table.getValue(variable, valueSet);
      assertThat(value.getValue().toString(), is("Second Cup"));
    }
  }

  @Test
  public void testValueTableGetVariableEntities() throws Exception {
    CsvDatasource datasource = new CsvDatasource("csv-datasource").addValueTable("TableDataOnly", //
    null, //
    new File("src/test/resources/TableDataOnly/data.csv"));
    datasource.initialise();

    ValueTable table = datasource.getValueTable("TableDataOnly");
    assertThat(table.getVariableEntities().size(), is(4));
  }

  @Test
  public void testValueTableGetVariables() throws Exception {
    CsvDatasource datasource = new CsvDatasource("csv-datasource").addValueTable("TableDataOnly", //
    null, //
    new File("src/test/resources/TableDataOnly/data.csv"));
    datasource.initialise();

    ValueTable table = datasource.getValueTable("TableDataOnly");
    assertThat(Iterables.size(table.getVariables()), is(5));

    CsvValueTable cvsValueTable = (CsvValueTable) table;
    assertThat(cvsValueTable.getVariables().size(), is(5));
  }

  @Test(expected = MagmaRuntimeException.class)
  public void testWritingDataOnlyEnsureWritingExtraHeaderFails() throws Exception {
    // This existing datasource has the following header: entity_id,FirstName,LastName,Sex,City,FavouriteIcecream
    CsvDatasource datasource = new CsvDatasource("csv-datasource").addValueTable("TableDataOnly", //
    null, //
    new File("src/test/resources/TableDataOnly/data.csv"));
    datasource.initialise();

    // Attempt to add a new ValueSet containing a new Variable "coffee". Expect this to fail.
    VariableEntity variableEntity = new VariableEntityBean("Participant", "5");
    Variable testVariable = Variable.Builder.newVariable("coffee", TextType.get(), "Participant").build();
    Value secondCup = TextType.get().valueOf("Second Cup");

    ValueTableWriter writer = datasource.createWriter("TableDataOnly", "Participant");
    ValueSetWriter vsw = writer.writeValueSet(variableEntity);
    vsw.writeValue(testVariable, secondCup);
    vsw.close();
    writer.close();
  }

  @Test
  public void testWritingDataOnlyAddingNewValueSet() throws Exception {
    File tempTestDirectory = new TempTableBuilder("TableDataOnly").addData().build();

    CsvDatasource setupDatasource = new CsvDatasource("setup-datasource").addValueTable("TableDataOnly", //
    null, //
    new File(tempTestDirectory.getCanonicalFile() + "/TableDataOnly", "data.csv"));
    setupDatasource.initialise();

    VariableEntity variableEntity = new VariableEntityBean("Participant", "1");

    Variable coffeeVariable = Variable.Builder.newVariable("coffee", TextType.get(), "Participant").build();
    Value secondCup = TextType.get().valueOf("Second Cup");

    Variable teaVariable = Variable.Builder.newVariable("tea", TextType.get(), "Participant").build();
    Value earlGrey = TextType.get().valueOf("Earl Grey");

    Variable biscuitVariable = Variable.Builder.newVariable("biscuit", TextType.get(), "Participant").build();
    Value cheese = TextType.get().valueOf("cheese");

    ValueTableWriter writer = setupDatasource.createWriter("TableDataOnly", "Participant");
    ValueSetWriter vsw = writer.writeValueSet(variableEntity);
    vsw.writeValue(coffeeVariable, secondCup);
    vsw.writeValue(teaVariable, earlGrey);
    vsw.writeValue(biscuitVariable, cheese);
    vsw.close();
    writer.close();

    CsvDatasource writeDatasource = new CsvDatasource("csv-datasource").addValueTable("TableDataOnly", //
    null, //
    new File(tempTestDirectory.getCanonicalFile() + "/TableDataOnly", "data.csv"));
    writeDatasource.initialise();

    VariableEntity participantTwoEntity = new VariableEntityBean("Participant", "2");

    Value orangePekoe = TextType.get().valueOf("Orange Pekoe");

    ValueTableWriter testWriter = writeDatasource.createWriter("TableDataOnly", "Participant");
    ValueSetWriter valueSetWriter = testWriter.writeValueSet(participantTwoEntity);
    valueSetWriter.writeValue(teaVariable, orangePekoe);
    valueSetWriter.close();
    testWriter.close();

    CsvDatasource readDatasource = new CsvDatasource("read-datasource").addValueTable("TableDataOnly", //
    null, //
    new File(tempTestDirectory.getCanonicalFile() + "/TableDataOnly", "data.csv"));
    readDatasource.initialise();

    ValueTable table = readDatasource.getValueTable("TableDataOnly");

    Variable verifyCoffeeVariable = table.getVariable("coffee");
    Variable verifyTeaVariable = table.getVariable("tea");
    Variable verifyBiscuitVariable = table.getVariable("biscuit");

    for(ValueSet valueSet : table.getValueSets()) {
      String identifier = valueSet.getVariableEntity().getIdentifier();
      Value coffeeValue = table.getValue(verifyCoffeeVariable, valueSet);
      Value teaValue = table.getValue(verifyTeaVariable, valueSet);
      Value biscuitValue = table.getValue(verifyBiscuitVariable, valueSet);
      if(identifier.equals("1")) {
        assertThat(coffeeValue.getValue().toString(), is("Second Cup"));
        assertThat(teaValue.getValue().toString(), is("Earl Grey"));
        assertThat(biscuitValue.getValue().toString(), is("cheese"));
      } else if(identifier.equals("2")) {
        assertThat(coffeeValue.getValue(), nullValue());
        assertThat(teaValue.getValue().toString(), is("Orange Pekoe"));
        assertThat(biscuitValue.getValue(), nullValue());
      }
    }
  }

  @Test
  public void testWritingDataOnlyModifyingValueSet() throws Exception {
    File tempTestDirectory = new TempTableBuilder("TableDataOnly").addData().build();

    CsvDatasource setupDatasource = new CsvDatasource("setup-datasource").addValueTable("TableDataOnly", //
    null, //
    new File(tempTestDirectory.getCanonicalFile() + "/TableDataOnly", "data.csv"));
    setupDatasource.initialise();

    VariableEntity variableEntity = new VariableEntityBean("Participant", "1");

    Variable coffeeVariable = Variable.Builder.newVariable("coffee", TextType.get(), "Participant").build();
    Value secondCup = TextType.get().valueOf("Second Cup");

    Variable teaVariable = Variable.Builder.newVariable("tea", TextType.get(), "Participant").build();
    Value earlGrey = TextType.get().valueOf("Earl Grey");

    Variable biscuitVariable = Variable.Builder.newVariable("biscuit", TextType.get(), "Participant").build();
    Value cheese = TextType.get().valueOf("cheese");

    ValueTableWriter writer = setupDatasource.createWriter("TableDataOnly", "Participant");
    ValueSetWriter vsw = writer.writeValueSet(variableEntity);
    vsw.writeValue(coffeeVariable, secondCup);
    vsw.writeValue(teaVariable, earlGrey);
    vsw.writeValue(biscuitVariable, cheese);
    vsw.close();
    writer.close();

    CsvDatasource writeDatasource = new CsvDatasource("csv-datasource").addValueTable("TableDataOnly", //
    null, //
    new File(tempTestDirectory.getCanonicalFile() + "/TableDataOnly", "data.csv"));
    writeDatasource.initialise();

    Value orangePekoe = TextType.get().valueOf("Orange Pekoe");

    ValueTableWriter testWriter = writeDatasource.createWriter("TableDataOnly", "Participant");
    ValueSetWriter valueSetWriter = testWriter.writeValueSet(variableEntity);
    valueSetWriter.writeValue(teaVariable, orangePekoe);
    valueSetWriter.close();
    testWriter.close();

    CsvDatasource readDatasource = new CsvDatasource("read-datasource").addValueTable("TableDataOnly", //
    null, //
    new File(tempTestDirectory.getCanonicalFile() + "/TableDataOnly", "data.csv"));
    readDatasource.initialise();

    ValueTable table = readDatasource.getValueTable("TableDataOnly");

    Variable verifyCoffeeVariable = table.getVariable("coffee");
    Variable verifyTeaVariable = table.getVariable("tea");
    Variable verifyBiscuitVariable = table.getVariable("biscuit");

    for(ValueSet valueSet : table.getValueSets()) {
      String identifier = valueSet.getVariableEntity().getIdentifier();
      Value coffeeValue = table.getValue(verifyCoffeeVariable, valueSet);
      Value teaValue = table.getValue(verifyTeaVariable, valueSet);
      Value biscuitValue = table.getValue(verifyBiscuitVariable, valueSet);
      if(identifier.equals("1")) {
        assertThat(coffeeValue.getValue().toString(), is("Second Cup"));
        assertThat(teaValue.getValue().toString(), is("Orange Pekoe"));
        assertThat(biscuitValue.getValue().toString(), is("cheese"));
      }
    }
  }

  @Test
  public void testWritingDataOnlyModifyingMultipleValueSets() throws Exception {
    String tableName = "TableDataOnly";
    String entityName = "Participant";
    CsvDatasource datasource = new TempTableBuilder(tableName).addData(new File("src/test/resources/TableDataOnly/data.csv")).buildCsvDatasource("csv-datasource");

    Variable cityVariable = Variable.Builder.newVariable("City", TextType.get(), "Participant").build();
    Value cityValueVancouver = TextType.get().valueOf("Vancouver");

    ValueTableWriter writer = datasource.createWriter(tableName, entityName);
    writeData(new VariableEntityBean(entityName, "4"), writer, cityVariable, cityValueVancouver);
    writeData(new VariableEntityBean(entityName, "2"), writer, cityVariable, cityValueVancouver);
    writer.close();
    datasource.dispose();
    datasource.initialise();
    // Must also work without re-initializing datasource.
    assertThat(readValue(datasource.getValueTable(tableName), new VariableEntityBean(entityName, "2"), cityVariable), is(cityValueVancouver));
  }

  private Value readValue(ValueTable valueTable, VariableEntity variableEntity, Variable variable) {
    for(ValueSet valueSet : valueTable.getValueSets()) {
      // String identifier = valueSet.getVariableEntity().getIdentifier();
      Value value = valueTable.getValue(variable, valueSet);
      if(valueSet.getVariableEntity().equals(variableEntity)) {
        return value;
      }
    }
    return null;
  }

  @Test
  public void testRefTable1DataRead() {
    CsvDatasource refDatasource = new CsvDatasource("csv-datasource1").addValueTable("Table1", //
    new File("src/test/resources/Table1/variables.csv"), //
    new File("src/test/resources/Table1/data.csv"));
    refDatasource.initialise();
    ValueTable refTable = refDatasource.getValueTable("Table1");

    CsvDatasource datasource = new CsvDatasource("csv-datasource2").addValueTable(refTable, //
    new File("src/test/resources/Table1/data2.csv"));
    datasource.initialise();
    Assert.assertEquals(1, datasource.getValueTableNames().size());

    ValueTable table = datasource.getValueTable("Table1");
    Assert.assertNotNull(table);
    Assert.assertEquals("Participant", table.getEntityType());

    Variable var = table.getVariable("var2");

    for(ValueSet valueSet : table.getValueSets()) {
      String identifier = valueSet.getVariableEntity().getIdentifier();
      Value value = table.getValue(var, valueSet);
      log.debug("var2[{}]={}", identifier, value);
      Assert.assertEquals("integer", value.getValueType().getName());
      if(identifier.equals("5")) {
        Assert.assertEquals(15l, value.getValue());
      } else if(identifier.equals("6")) {
        Assert.assertEquals(16l, value.getValue());
      } else if(identifier.equals("7")) {
        Assert.assertEquals(17l, value.getValue());
      } else if(identifier.equals("8")) {
        Assert.assertEquals(18l, value.getValue());
      } else {
        Assert.assertFalse(true);
      }
    }
  }

  @Ignore
  @Test
  public void testWriteVariableIsReadBack() throws Exception {
    File tempCsvTestDirectory = createTempDirectory("csvTest");
    File testTableDirectory = new File(tempCsvTestDirectory.getAbsoluteFile(), "test-table");
    testTableDirectory.mkdir();

    Variable testVariable = Variable.Builder.newVariable("test-variable", TextType.get(), "entityType").build();
    // CsvDatasource datasource = new CsvDatasource("testDatasource").addValueTable(tmpCsvFile);
    // CsvDatasource datasource = new CsvDatasource("test-datasource", testTableDirectory);
    CsvDatasource datasource = new CsvDatasource("test-datasource").addValueTable("test-table", //
    new File(testTableDirectory.getAbsoluteFile(), "variables.csv"), //
    new File(testTableDirectory.getAbsoluteFile(), "data.csv"));
    // CsvDatasource datasource = new CsvDatasource("bubba").addValueTable(tableName, variablesFile, dataFile)
    datasource.initialise();
    writeVariableToDatasource(datasource, "test-table", testVariable);
    datasource.dispose();
  }

  @Ignore
  @Test
  public void testWriteVariableSchemaLine() throws Exception {
    File tempCsvTestDirectory = createTempDirectory("csvTest");
    File testTableDirectory = new File(tempCsvTestDirectory.getAbsoluteFile(), "test-table");
    testTableDirectory.mkdir();

    Variable testVariable = Variable.Builder.newVariable("test-variable", TextType.get(), "entityType").build();

    CsvDatasource datasource = new CsvDatasource("test-datasource").addValueTable("test-table", //
    // null, //
    new File(testTableDirectory.getAbsoluteFile(), "variables.csv"), //
    null);
    // new File(testTableDirectory.getAbsoluteFile(), "data.csv"));

    datasource.initialise();
    writeVariableToDatasource(datasource, "test-table", testVariable);
    datasource.dispose();

    CsvDatasource readDs = new CsvDatasource("test-datasource").addValueTable("test-table", //
    new File(testTableDirectory.getAbsoluteFile(), "variables.csv"), //
    // null, //
    // new File(testTableDirectory.getAbsoluteFile(), "data.csv"));
    null);
    readDs.initialise();

    ValueTable vt = readDs.getValueTable("test-table");

    assertThat(vt.getEntityType(), is("entityType"));
  }

  private File createTempDirectory(String suffix) throws IOException {
    File tempDirectory = File.createTempFile(suffix, "");
    tempDirectory.delete();
    tempDirectory.mkdir();
    // tempDirectory.deleteOnExit();
    return tempDirectory;
  }

  private void writeVariableToDatasource(Datasource datasource, String tableName, Variable testVariable) throws IOException {
    ValueTableWriter writer = datasource.createWriter("test-table", "entityType");
    VariableWriter vw = writer.writeVariables();
    vw.writeVariable(testVariable);
    vw.close();
    writer.close();
  }

  private void writeData(VariableEntity variableEntity, ValueTableWriter valueTableWriter, Variable variable, Value value) throws IOException {
    ValueSetWriter valueSetWriter = valueTableWriter.writeValueSet(variableEntity);
    valueSetWriter.writeValue(variable, value);
    valueSetWriter.close();
  }

}
