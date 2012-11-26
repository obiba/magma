package org.obiba.magma.datasource.csv;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;

import java.io.File;
import java.io.IOException;
import java.util.Locale;
import java.util.Map;

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
import org.obiba.magma.ValueSequence;
import org.obiba.magma.ValueSet;
import org.obiba.magma.ValueTable;
import org.obiba.magma.ValueTableWriter;
import org.obiba.magma.ValueTableWriter.ValueSetWriter;
import org.obiba.magma.ValueTableWriter.VariableWriter;
import org.obiba.magma.Variable;
import org.obiba.magma.VariableEntity;
import org.obiba.magma.datasource.csv.support.Quote;
import org.obiba.magma.datasource.csv.support.Separator;
import org.obiba.magma.support.DatasourceParsingException;
import org.obiba.magma.support.VariableEntityBean;
import org.obiba.magma.type.IntegerType;
import org.obiba.magma.type.TextType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ImmutableMap;
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
  public void testSeparators() {
    Assert.assertEquals(Quote.SINGLE, Quote.fromString("'"));
    Assert.assertEquals(Quote.DOUBLE, Quote.fromString("\""));
    Assert.assertEquals('|', Quote.fromString("|").getCharacter());

    Assert.assertEquals(Separator.COMMA, Separator.fromString(","));
    Assert.assertEquals(Separator.SEMICOLON, Separator.fromString(";"));
    Assert.assertEquals(Separator.COLON, Separator.fromString(":"));
    Assert.assertEquals(Separator.TAB, Separator.fromString("\t"));
    Assert.assertEquals('|', Separator.fromString("|").getCharacter());
  }

  @Test
  public void test_supportsAnySeparator() {
    File samples = new File("src/test/resources/separators");
    File variables = new File(samples, "variables.csv");

    CsvDatasource ds = new CsvDatasource("variables").addValueTable("variables", variables, (File) null);
    ds.initialise();
    ValueTable reference = ds.getValueTable("variables");

    Map<String, String> separators = ImmutableMap.<String, String>builder().put("sample-comma.csv", ",")
        .put("sample-semicolon.csv", ";").put("sample-colon.csv", ":").put("sample-tab.csv", "tab")
        .put("sample-pipe.csv", "|").put("sample-space.csv", " ").build();
    for(File sample : samples.listFiles()) {
      if(separators.containsKey(sample.getName()) == false) continue;
      CsvDatasource datasource = new CsvDatasource("csv-datasource");
      datasource.setSeparator(Separator.fromString(separators.get(sample.getName())));
      datasource.addValueTable(reference, sample);
      try {
        datasource.initialise();
      } catch(DatasourceParsingException e) {
        e.printList();
        throw e;
      }
      ValueTable valueTable = datasource.getValueTable(reference.getName());
      Assert.assertEquals(16, valueTable.getVariableEntities().size());
      for(Variable v : valueTable.getVariables()) {
        for(ValueSet vs : valueTable.getValueSets()) {
          valueTable.getVariableValueSource(v.getName()).getValue(vs);
        }
      }
      datasource.dispose();
    }
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
      if("Y".equals(category.getName())) {
        Assert.assertEquals("yes", category.getAttribute("label", Locale.ENGLISH).getValue().toString());
      } else if("N".equals(category.getName())) {
        Assert.assertEquals("no", category.getAttribute("label", Locale.ENGLISH).getValue().toString());
      } else if("PNA".equals(category.getName())) {
        Assert
            .assertEquals("prefer not to answer", category.getAttribute("label", Locale.ENGLISH).getValue().toString());
      } else if("DNK".equals(category.getName())) {
        Assert.assertEquals("don't know", category.getAttribute("label", Locale.ENGLISH).getValue().toString());
      } else {
        Assert.assertFalse(true);
      }
    }

    Assert.assertEquals(3, var.getAttributes().size());
    Assert.assertTrue(var.hasAttribute("label"));
    Assert.assertEquals("Hello I'm variable one", var.getAttribute("label", Locale.ENGLISH).getValue().toString());
    Assert.assertEquals("ns1", var.getAttribute("ns1", "attr").getValue().toString());
    Assert.assertEquals("ns2", var.getAttribute("ns2", "attr", Locale.ENGLISH).getValue().toString());
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
      if("1".equals(identifier)) {
        Assert.assertEquals("Y", value.getValue());
      } else if("2".equals(identifier)) {
        Assert.assertEquals("N", value.getValue());
      } else if("3".equals(identifier)) {
        Assert.assertEquals("PNA", value.getValue());
      } else if("4".equals(identifier)) {
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
      if("1".equals(identifier) || "2".equals(identifier)) {
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
      if("1".equals(identifier)) {
        assertThat(coffeeValue.getValue().toString(), is("Second Cup"));
        assertThat(teaValue.getValue().toString(), is("Earl Grey"));
        assertThat(biscuitValue.getValue().toString(), is("cheese"));
      } else if("2".equals(identifier)) {
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
    setupDatasource.dispose();

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
      if("1".equals(identifier)) {
        assertThat(coffeeValue.getValue().toString(), is("Second Cup"));
        assertThat(teaValue.getValue().toString(), is("Orange Pekoe"));
        assertThat(biscuitValue.getValue().toString(), is("cheese"));
      }
    }
  }

  @Test
  public void testWritingDataOnlyModifyingMultipleValueSetsAndReadingBackFromReinitializedDatasource() throws Exception {
    String tableName = "TableDataOnly";
    String entityName = "Participant";
    CsvDatasource datasource = new TempTableBuilder(tableName)
        .addData(new File("src/test/resources/TableDataOnly/data.csv")).buildCsvDatasource("csv-datasource");

    Variable cityVariable = Variable.Builder.newVariable("City", TextType.get(), "Participant").build();
    Value cityValueVancouver = TextType.get().valueOf("Vancouver");

    ValueTableWriter writer = datasource.createWriter(tableName, entityName);
    writeData(new VariableEntityBean(entityName, "4"), writer, cityVariable, cityValueVancouver);
    writeData(new VariableEntityBean(entityName, "2"), writer, cityVariable, cityValueVancouver);
    writer.close();
    datasource.dispose();
    datasource.initialise();
    assertThat(readValue(datasource.getValueTable(tableName), new VariableEntityBean(entityName, "2"), cityVariable),
        is(cityValueVancouver));
    datasource.dispose();
  }

  @Test
  public void testWritingDataOnlyModifyingMultipleValueSetsAndReadingBackFromDatasource() throws Exception {
    String tableName = "TableDataOnly";
    String entityName = "Participant";
    CsvDatasource datasource = new TempTableBuilder(tableName)
        .addData(new File("src/test/resources/TableDataOnly/data.csv")).buildCsvDatasource("csv-datasource");

    Variable cityVariable = Variable.Builder.newVariable("City", TextType.get(), "Participant").build();
    Value cityValueVancouver = TextType.get().valueOf("Vancouver");

    ValueTableWriter writer = datasource.createWriter(tableName, entityName);
    writeData(new VariableEntityBean(entityName, "4"), writer, cityVariable, cityValueVancouver);
    writeData(new VariableEntityBean(entityName, "2"), writer, cityVariable, TextType.get().valueOf("Moncton"));
    writeData(new VariableEntityBean(entityName, "2"), writer, cityVariable, TextType.get().valueOf("Regina"));
    writeData(new VariableEntityBean(entityName, "2"), writer, cityVariable, cityValueVancouver);
    writer.close();

    assertThat(readValue(datasource.getValueTable(tableName), new VariableEntityBean(entityName, "2"), cityVariable),
        is(cityValueVancouver));
    datasource.dispose();
  }

  @Test
  public void testWritingDataOnlyModifyingMultipleWideByteValueSetsAndReadingBackFromDatasource() throws Exception {
    String tableName = "TableDataOnly";
    String entityName = "Participant";
    CsvDatasource datasource = new TempTableBuilder(tableName)
        .addData(new File("src/test/resources/TableDataOnly/data.csv")).buildCsvDatasource("csv-datasource");

    Variable cityVariable = Variable.Builder.newVariable("City", TextType.get(), "Participant").build();
    Value wideByteCityName = TextType.get().valueOf("Suggéré");

    ValueTableWriter writer = datasource.createWriter(tableName, entityName);
    // Write wide byte line
    writeData(new VariableEntityBean(entityName, "2"), writer, cityVariable, wideByteCityName);
    // Write line after wide byte line. This one is in danger of being partially overwritten during an update.
    writeData(new VariableEntityBean(entityName, "3"), writer, cityVariable, TextType.get().valueOf("Moncton"));
    // Update the wide byte line (2) to ensure that line (3) is not affected.
    writeData(new VariableEntityBean(entityName, "2"), writer, cityVariable, TextType.get().valueOf("Regina"));
    writer.close();

    assertThat(readValue(datasource.getValueTable(tableName), new VariableEntityBean(entityName, "3"), cityVariable),
        is(TextType.get().valueOf("Moncton")));
    datasource.dispose();
  }

  private Value readValue(ValueTable valueTable, VariableEntity variableEntity, Variable variable) {
    for(ValueSet valueSet : valueTable.getValueSets()) {
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
      if("5".equals(identifier)) {
        Assert.assertEquals(15l, value.getValue());
      } else if("6".equals(identifier)) {
        Assert.assertEquals(16l, value.getValue());
      } else if("7".equals(identifier)) {
        Assert.assertEquals(17l, value.getValue());
      } else if("8".equals(identifier)) {
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
        (File) null);
    // new File(testTableDirectory.getAbsoluteFile(), "data.csv"));

    datasource.initialise();
    writeVariableToDatasource(datasource, "test-table", testVariable);
    datasource.dispose();

    CsvDatasource readDs = new CsvDatasource("test-datasource").addValueTable("test-table", //
        new File(testTableDirectory.getAbsoluteFile(), "variables.csv"), //
        // null, //
        // new File(testTableDirectory.getAbsoluteFile(), "data.csv"));
        (File) null);
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

  private void writeVariableToDatasource(Datasource datasource, String tableName,
      Variable testVariable) throws IOException {
    ValueTableWriter writer = datasource.createWriter("test-table", "entityType");
    VariableWriter vw = writer.writeVariables();
    vw.writeVariable(testVariable);
    vw.close();
    writer.close();
  }

  private void writeData(VariableEntity variableEntity, ValueTableWriter valueTableWriter, Variable variable,
      Value value) throws IOException {
    ValueSetWriter valueSetWriter = valueTableWriter.writeValueSet(variableEntity);
    valueSetWriter.writeValue(variable, value);
    valueSetWriter.close();
  }

  private void writeVariable(ValueTableWriter valueTableWriter, Variable variable) throws IOException {
    VariableWriter variableWriter = valueTableWriter.writeVariables();
    variableWriter.writeVariable(variable);
    variableWriter.close();
  }

  // Variable Tests

  @Test
  public void testReadingVariables_ConfirmVarMetadata() throws Exception {
    String tableName = "TableVariablesOnly";
    String entityName = "Participant";
    CsvDatasource datasource = new TempTableBuilder(tableName)
        .addVariables(new File("src/test/resources/Table1/variables.csv")).buildCsvDatasource("csv-datasource");

    ValueTable table = datasource.getValueTable(tableName);
    Variable variable = table.getVariable("var2");

    assertThat(variable.getValueType().getName(), is(IntegerType.get().getName()));
    assertThat(variable.getEntityType(), is(entityName));
    assertThat(variable.getAttribute("label", Locale.ENGLISH).getValue().toString(), is("Hello I'm variable two"));
    assertThat(variable.getMimeType(), nullValue());
    datasource.dispose();
  }

  @Test(expected = MagmaRuntimeException.class)
  public void testWritingVariables_HeaderInFileWithoutRequiredNameCausesError() throws Exception {
    String tableName = "TableVariablesOnly";
    new TempTableBuilder(tableName).addVariables(new File("src/test/resources/Table1/variables_with_no_name.csv"))
        .buildCsvDatasource("csv-datasource");
  }

  @Test(expected = MagmaRuntimeException.class)
  public void testWritingVariables_HeaderInFileWithoutRequiredTypeCausesError() throws Exception {
    String tableName = "TableVariablesOnly";
    new TempTableBuilder(tableName).addVariables(new File("src/test/resources/Table1/variables_with_no_type.csv"))
        .buildCsvDatasource("csv-datasource");
  }

  @Test(expected = MagmaRuntimeException.class)
  public void testWritingVariables_HeaderInFileWithoutRequiredEntityTypeCausesError() throws Exception {
    String tableName = "TableVariablesOnly";
    new TempTableBuilder(tableName).addVariables(new File("src/test/resources/Table1/variables_with_no_entityType.csv"))
        .buildCsvDatasource("csv-datasource");
  }

  @Test
  public void testWritingVariables_MinimalHeaderIsValid() throws Exception {
    String tableName = "TableVariablesOnly";
    new TempTableBuilder(tableName).addVariables(new File("src/test/resources/Table1/variables_minimal_header.csv"))
        .buildCsvDatasource("csv-datasource");
  }

  @Test
  public void testWritingVariables_WriteNewVariableToEmptyFile() throws Exception {
    String tableName = "TableVariablesOnly";
    String entityName = "Participant";
    CsvDatasource datasource = new TempTableBuilder(tableName).addVariables()
        .variablesHeader("name#valueType#entityType#label".split("#")).buildCsvDatasource("csv-datasource");

    Variable variable = Variable.Builder.newVariable("coffee", TextType.get(), entityName)
        .addAttribute("label", "Please indicated your favourite coffee vendor.").build();

    ValueTableWriter writer = datasource.createWriter(tableName, entityName);
    writeVariable(writer, variable);
    writer.close();
  }

  @Test
  public void testWritingVariables_WriteNewVariableToEmptyFileWithoutProvidingVariablesHeader() throws Exception {
    String tableName = "TableVariablesOnly";
    String entityName = "Participant";
    CsvDatasource datasource = new TempTableBuilder(tableName).addVariables().buildCsvDatasource("csv-datasource");

    Variable variable = Variable.Builder.newVariable("coffee", TextType.get(), entityName)
        .addAttribute("label", "Please indicated your favourite coffee vendor.").build();

    ValueTableWriter writer = datasource.createWriter(tableName, entityName);
    writeVariable(writer, variable);
    writer.close();
  }

  @Test
  public void testWritingVariables_AddingVariablesToAnExistingFile() throws Exception {
    String tableName = "TableVariablesOnly";
    String entityName = "Participant";
    CsvDatasource datasource = new TempTableBuilder(tableName)
        .addVariables(new File("src/test/resources/Table1/variables.csv")).buildCsvDatasource("csv-datasource");

    Variable variable = Variable.Builder.newVariable("coffee", TextType.get(), entityName)
        .addAttribute("label", "Please indicated your favourite coffee vendor.").build();

    ValueTableWriter writer = datasource.createWriter(tableName, entityName);
    writeVariable(writer, variable);
    writer.close();
    datasource.dispose();
    datasource.initialise();
    assertThat(datasource.getValueTable(tableName).getVariable("coffee").getValueType().getName(), is("text"));
    datasource.dispose();
  }

  @Test
  public void testWritingVariables_UpdatingVariable() throws Exception {
    String tableName = "TableVariablesOnly";
    String entityName = "Participant";
    CsvDatasource datasource = new TempTableBuilder(tableName)
        .addVariables(new File("src/test/resources/Table1/variables.csv")).buildCsvDatasource("csv-datasource");

    Variable variable = Variable.Builder.newVariable("var2", TextType.get(), entityName)
        .addAttribute("label", "Please indicated your favourite coffee vendor.").build();
    Variable variable2 = Variable.Builder.newVariable("var2", TextType.get(), entityName).build();

    ValueTableWriter writer = datasource.createWriter(tableName, entityName);
    writeVariable(writer, variable);
    writeVariable(writer, variable2);
    writeVariable(writer, variable);
    writer.close();
    datasource.dispose();
    datasource.initialise();
    assertThat(datasource.getValueTable(tableName).getVariable("var2").getValueType().getName(), is("text"));
    datasource.dispose();
  }

  @Test
  public void testReadingVariables_GetVariables() throws Exception {
    String tableName = "TableVariablesOnly";
    CsvDatasource datasource = new TempTableBuilder(tableName)
        .addVariables(new File("src/test/resources/Table1/variables.csv")).buildCsvDatasource("csv-datasource");

    assertThat(((CsvValueTable) datasource.getValueTable(tableName)).getVariables().size(), is(2));
  }

  @Test
  public void testWritingVariables_UpdatingWideByteVariable() throws Exception {
    String tableName = "TableVariablesOnly";
    String entityName = "Participant";
    CsvDatasource datasource = new TempTableBuilder(tableName)
        .addVariables(new File("src/test/resources/Table1/variables.csv")).buildCsvDatasource("csv-datasource");

    Variable variable = Variable.Builder.newVariable("var2", TextType.get(), entityName)
        .addAttribute("label", "suggéré").build();
    Variable variable2 = Variable.Builder.newVariable("var2", TextType.get(), entityName).build();

    ValueTableWriter writer = datasource.createWriter(tableName, entityName);
    writeVariable(writer, variable);
    writeVariable(writer, variable2);
    writeVariable(writer, variable);
    writeVariable(writer, variable);
    writer.close();
    datasource.dispose();
    datasource.initialise();
    assertThat(datasource.getValueTable(tableName).getVariable("var2").getValueType().getName(), is("text"));
    datasource.dispose();
  }

  @Test
  public void testRepeatableDataRead() {
    CsvDatasource datasource = new CsvDatasource("csv-datasource").addValueTable("Participants", //
        new File("src/test/resources/Participants/variables.csv"), //
        new File("src/test/resources/Participants/data.csv"));
    datasource.initialise();
    Assert.assertEquals(1, datasource.getValueTableNames().size());

    ValueTable table = datasource.getValueTable("Participants");
    Assert.assertNotNull(table);
    Assert.assertEquals("Participant", table.getEntityType());

    Variable var = table.getVariable("Admin.Action.actionType");
    Assert.assertTrue(var.isRepeatable());

    int count = 0;
    for(ValueSet valueSet : table.getValueSets()) {
      count++;
      if(count == 1) {
        String identifier = valueSet.getVariableEntity().getIdentifier();
        Value value = table.getValue(var, valueSet);
        log.info("Admin.Action.actionType[{}]={}", identifier, value);
        Assert.assertEquals("text", value.getValueType().getName());
        Assert.assertTrue(value.isSequence());
        ValueSequence seq = value.asSequence();
        Assert.assertEquals(33, seq.getSize());
      }
    }
    Assert.assertEquals(2, count);
  }

  @Test
  public void testMultilineDataRead() {
    CsvDatasource datasource = new CsvDatasource("csv-datasource").addValueTable("Participants", //
        new File("src/test/resources/Participants/variables.csv"), //
        new File("src/test/resources/Participants/data.csv"));
    datasource.setQuote(Quote.DOUBLE);
    datasource.setSeparator(Separator.COMMA);
    datasource.setCharacterSet("UTF-8");
    datasource.setFirstRow(1);
    datasource.initialise();

    Assert.assertEquals(1, datasource.getValueTableNames().size());

    ValueTable table = datasource.getValueTable("Participants");
    Assert.assertNotNull(table);
    Assert.assertEquals("Participant", table.getEntityType());

    Variable var = table.getVariable("Admin.Action.comment");
    Assert.assertTrue(var.isRepeatable());

    int count = 0;
    for(ValueSet valueSet : table.getValueSets()) {
      count++;
      String identifier = valueSet.getVariableEntity().getIdentifier();
      Value value = table.getValue(var, valueSet);
      log.info("Admin.Action.comment[{}]={}", identifier, value);
      Assert.assertNotNull(value);
      Assert.assertEquals("text", value.getValueType().getName());
      Assert.assertTrue(value.isSequence());
      ValueSequence seq = value.asSequence();
      if(count == 1) {
        Assert.assertEquals(33, seq.getSize());
        Assert.assertEquals("sample collection by Val\ndata entry by Evan", seq.get(22).toString());
      } else if(count == 2) {
        Assert.assertEquals(
            "Unable to draw from left arm due to vein rolling upon puncture. Ct refused puncture to right arm. Saliva kit complete",
            seq.get(5).toString());
      }
    }
    Assert.assertEquals(2, count);
  }

  @Test
  public void testCharSet() {
    CsvDatasource datasource = new CsvDatasource("csv-datasource")
        .addValueTable("Drugs", new File("src/test/resources/medications/Drugs.csv"), "Drug");
    datasource.setQuote(Quote.DOUBLE);
    datasource.setSeparator(Separator.COMMA);
    datasource.setCharacterSet("UTF-8");
    datasource.setFirstRow(1);
    datasource.initialise();

    ValueTable table = datasource.getValueTable("Drugs");

    Variable var = table.getVariable("MEDICATION");

    checkValue(table, var, "02335204",
        "PREVNAR 13 (CORYNEBACTERIUM DIPHTHERIAE CRM-197 PROTEIN 34\u00b5G, PNEUMOCOCCAL POLYSACCHARIDE SEROTYPE 14 2.2\u00b5G, PNEUMOCOCCAL POLYSACCHARIDE SEROTYPE 18C 2.2\u00b5G, PNEUMOCOCCAL POLYSACCHARIDE SEROTYPE 19F 2.2\u00b5G, PNEUMOCOCCAL POLYSACCHARIDE SEROTYPE 23F 2.2\u00b5G, PNEUMOCOCCAL POLYSACCHARIDE SEROTYPE 4 2.2\u00b5G, PNEUMOCOCCAL POLYSACCHARIDE SEROTYPE 6B 4.4\u00b5G, PNEUMOCOCCAL POLYSACCHARIDE SEROTYPE 9V 2.2\u00b5G, PNEUMOCOCCAL POLYSACCHARIDE SEROTYPE 3 2.2\u00b5G, PNEUMOCOCCAL POLYSACCHARIDE SEROTYPE 5 2.2\u00b5G, PNEUMOCOCCAL POLYSACCHARIDE SEROTYPE 6A 2.2\u00b5G, PNEUMOCOCCAL POLYSACCHARIDE SEROTYPE 7F 2.2\u00b5G, PNEUMOCOCCAL POLYSACCHARIDE SEROTYP 19A 2.2\u00b5G, PNEUMOCOCCAL POLYSACCHARIDE SEROTYPE 1 2.2\u00b5G)");
    checkValue(table, var, "01918346", "COUMADIN TAB 2.5MG (WARFARIN SODIUM 2.5MG)");
  }

  private void checkValue(ValueTable table, Variable var, String identifier, String expected) {
    Value value = table.getValue(var, table.getValueSet(new VariableEntityBean("Drug", identifier)));
    log.info("{} : {}", identifier, value.toString());
    Assert.assertEquals(expected, value.toString());
  }
}
