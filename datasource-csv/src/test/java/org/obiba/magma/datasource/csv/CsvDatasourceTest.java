package org.obiba.magma.datasource.csv;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Locale;
import java.util.Map;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.obiba.magma.Category;
import org.obiba.magma.Datasource;
import org.obiba.magma.MagmaEngine;
import org.obiba.magma.MagmaRuntimeException;
import org.obiba.magma.NoSuchValueSetException;
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
import org.obiba.magma.support.AbstractValueTable;
import org.obiba.magma.support.DatasourceParsingException;
import org.obiba.magma.support.VariableEntityBean;
import org.obiba.magma.type.IntegerType;
import org.obiba.magma.type.TextType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterables;

import junit.framework.Assert;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertNull;
import static junit.framework.Assert.assertTrue;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

@SuppressWarnings({ "OverlyLongMethod", "ResultOfMethodCallIgnored", "OverlyCoupledClass" })
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
  public void test_separators() {
    assertEquals(Quote.SINGLE, Quote.fromString("'"));
    assertEquals(Quote.DOUBLE, Quote.fromString("\""));
    assertEquals('|', Quote.fromString("|").getCharacter());

    assertEquals(Separator.COMMA, Separator.fromString(","));
    assertEquals(Separator.SEMICOLON, Separator.fromString(";"));
    assertEquals(Separator.COLON, Separator.fromString(":"));
    assertEquals(Separator.TAB, Separator.fromString("\t"));
    assertEquals('|', Separator.fromString("|").getCharacter());
  }

  @Nonnull
  static File getFileFromResource(String path) throws URISyntaxException {
    URL resource = CsvDatasourceTest.class.getClassLoader().getResource(path);
    URI uri = resource == null ? null : resource.toURI();
    if(uri == null) throw new IllegalArgumentException("Cannot find file at " + path);
    return new File(uri);
  }

  @Test
  public void test_supports_any_separator() throws URISyntaxException {
    File samples = getFileFromResource("separators");
    File variables = new File(samples, "variables.csv");

    CsvDatasource ds = new CsvDatasource("variables").addValueTable("variables", variables, (File) null);
    ds.initialise();
    ValueTable reference = ds.getValueTable("variables");

    Map<String, String> separators = ImmutableMap.<String, String>builder().put("sample-comma.csv", ",")
        .put("sample-semicolon.csv", ";").put("sample-colon.csv", ":").put("sample-tab.csv", "tab")
        .put("sample-pipe.csv", "|").put("sample-space.csv", " ").build();
    File[] files = samples.listFiles();
    assertThat(files, notNullValue());
    //noinspection ConstantConditions
    for(File sample : files) {
      String fileName = sample.getName();
      if(!separators.containsKey(fileName)) continue;
      CsvDatasource datasource = new CsvDatasource("csv-datasource");
      datasource.setSeparator(Separator.fromString(separators.get(fileName)));
      datasource.addValueTable(reference, sample);
      try {
        datasource.initialise();
      } catch(DatasourceParsingException e) {
        e.printList();
        throw e;
      }
      ValueTable valueTable = datasource.getValueTable(reference.getName());
      assertThat(valueTable.getVariableEntities().size(), is(16));
      for(Variable v : valueTable.getVariables()) {
        for(ValueSet vs : valueTable.getValueSets()) {
          valueTable.getVariableValueSource(v.getName()).getValue(vs);
        }
      }
      datasource.dispose();
    }
  }

  @SuppressWarnings("IfStatementWithTooManyBranches")
  @Test
  public void test_table_variable_read() throws URISyntaxException {
    CsvDatasource datasource = new CsvDatasource("csv-datasource").addValueTable("Table1", //
        getFileFromResource("Table1/variables.csv"), //
        getFileFromResource("Table1/data.csv"));
    datasource.initialise();
    assertThat(datasource.getValueTableNames().size(), is(1));

    ValueTable table = datasource.getValueTable("Table1");
    assertThat(table, notNullValue());
    assertThat(table.getEntityType(), is("Participant"));

    Variable var = table.getVariable("var1");
    assertThat(var, notNullValue());
    assertThat(var.getValueType().getName(), is("text"));
    assertEquals("Participant", var.getEntityType());
    assertNull(var.getMimeType());
    assertNull(var.getUnit());
    assertNull(var.getOccurrenceGroup());
    assertFalse(var.isRepeatable());

    assertEquals(4, var.getCategories().size());
    for(Category category : var.getCategories()) {
      String label = category.getAttribute("label", Locale.ENGLISH).getValue().toString();
      String categoryName = category.getName();
      if("Y".equals(categoryName)) {
        assertEquals("yes", label);
      } else if("N".equals(categoryName)) {
        assertEquals("no", label);
      } else if("PNA".equals(categoryName)) {
        assertEquals("prefer not to answer", label);
      } else if("DNK".equals(categoryName)) {
        assertEquals("don't know", label);
      } else {
        Assert.fail();
      }
    }

    assertEquals(3, var.getAttributes().size());
    assertTrue(var.hasAttribute("label"));
    assertEquals("Hello I'm variable one", var.getAttribute("label", Locale.ENGLISH).getValue().toString());
    assertEquals("ns1", var.getAttribute("ns1", "attr").getValue().toString());
    assertEquals("ns2", var.getAttribute("ns2", "attr", Locale.ENGLISH).getValue().toString());
  }

  @SuppressWarnings("IfStatementWithTooManyBranches")
  @Test
  public void test_table_data_read() throws URISyntaxException {
    CsvDatasource datasource = new CsvDatasource("csv-datasource").addValueTable("Table1", //
        getFileFromResource("Table1/variables.csv"), //
        getFileFromResource("Table1/data.csv"));
    datasource.initialise();
    assertThat(datasource.getValueTableNames().size(), is(1));

    ValueTable table = datasource.getValueTable("Table1");
    assertThat(table, notNullValue());
    assertThat(table.getEntityType(), is("Participant"));

    Variable var = table.getVariable("var1");

    for(ValueSet valueSet : table.getValueSets()) {
      String identifier = valueSet.getVariableEntity().getIdentifier();
      Value value = table.getValue(var, valueSet);
      log.debug("var1[{}]={}", identifier, value);
      assertEquals("text", value.getValueType().getName());
      if("1".equals(identifier)) {
        assertEquals("Y", value.getValue());
      } else if("2".equals(identifier)) {
        assertEquals("N", value.getValue());
      } else if("3".equals(identifier)) {
        assertEquals("PNA", value.getValue());
      } else if("4".equals(identifier)) {
        assertEquals("DNK", value.getValue());
      } else {
        Assert.fail();
      }
    }
  }

  @Test
  public void test_reading_data_only_table_has_only_one_table() throws Exception {
    CsvDatasource datasource = new CsvDatasource("csv-datasource").addValueTable("TableDataOnly", //
        null, //
        getFileFromResource("TableDataOnly/data.csv"));
    datasource.initialise();

    assertThat(datasource.getValueTableNames().size(), is(1));
  }

  @Test
  public void test_reading_data_only_table_is_not_null() throws Exception {
    CsvDatasource datasource = new CsvDatasource("csv-datasource").addValueTable("TableDataOnly", //
        null, //
        getFileFromResource("TableDataOnly/data.csv"));
    datasource.initialise();

    assertThat(datasource.getValueTable("TableDataOnly"), notNullValue());
  }

  @Test
  public void test_reading_data_only_table_entity_type_is_default_participant() throws Exception {
    CsvDatasource datasource = new CsvDatasource("csv-datasource").addValueTable("TableDataOnly", //
        null, //
        getFileFromResource("TableDataOnly/data.csv"));
    datasource.initialise();
    ValueTable table = datasource.getValueTable("TableDataOnly");

    assertThat(table.getEntityType(), is(CsvValueTable.DEFAULT_ENTITY_TYPE));
  }

  @Test
  public void test_reading_data_only_favourite_ice_cream_variable_exists() throws Exception {
    CsvDatasource datasource = new CsvDatasource("csv-datasource").addValueTable("TableDataOnly", //
        null, //
        getFileFromResource("TableDataOnly/data.csv"));
    datasource.initialise();
    ValueTable table = datasource.getValueTable("TableDataOnly");

    assertThat(table.getVariable("FavouriteIcecream"), notNullValue());
  }

  @Test
  public void test_reading_single_data_only_table_null_ice_cream_value() throws Exception {
    CsvDatasource datasource = new CsvDatasource("csv-datasource").addValueTable("TableDataOnly", //
        null, //
        getFileFromResource("TableDataOnly/data.csv"));
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
  public void test_reading_data_only_value_type_is_text() throws Exception {
    CsvDatasource datasource = new CsvDatasource("csv-datasource").addValueTable("TableDataOnly", //
        null, //
        getFileFromResource("TableDataOnly/data.csv"));
    datasource.initialise();
    ValueTable table = datasource.getValueTable("TableDataOnly");

    for(Variable variable : table.getVariables()) {
      assertThat(variable.getValueType().getName(), is(TextType.get().getName()));
    }
  }

  @Test
  public void test_writing_data_only_one_text_variable() throws Exception {
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
      assertNotNull(value.getValue());
      //noinspection ConstantConditions
      assertThat(value.getValue().toString(), is("Second Cup"));
    }
  }

  @Test
  public void test_value_table_get_variable_entities() throws Exception {
    CsvDatasource datasource = new CsvDatasource("csv-datasource").addValueTable("TableDataOnly", //
        null, getFileFromResource("TableDataOnly/data.csv"));
    datasource.initialise();

    ValueTable table = datasource.getValueTable("TableDataOnly");
    assertThat(table.getVariableEntities().size(), is(4));
  }

  @Test
  public void test_value_table_get_variables() throws Exception {
    CsvDatasource datasource = new CsvDatasource("csv-datasource").addValueTable("TableDataOnly", //
        null, getFileFromResource("TableDataOnly/data.csv"));
    datasource.initialise();

    ValueTable table = datasource.getValueTable("TableDataOnly");
    assertThat(Iterables.size(table.getVariables()), is(5));

    //noinspection TypeMayBeWeakened
    CsvValueTable cvsValueTable = (CsvValueTable) table;
    assertThat(cvsValueTable.getVariables().size(), is(5));
  }

  @Test(expected = MagmaRuntimeException.class)
  public void test_writing_data_only_ensure_writing_extra_header_fails() throws Exception {
    // This existing datasource has the following header: entity_id,FirstName,LastName,Sex,City,FavouriteIcecream
    CsvDatasource datasource = new CsvDatasource("csv-datasource").addValueTable("TableDataOnly", //
        null, //
        getFileFromResource("TableDataOnly/data.csv"));
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

  @SuppressWarnings("ConstantConditions")
  @Test
  public void test_writing_data_only_adding_new_value_set() throws Exception {
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

  @SuppressWarnings("ConstantConditions")
  @Test
  public void test_writing_data_only_modifying_value_set() throws Exception {
    File tempTestDirectory = new TempTableBuilder("TableDataOnly").addData().build();

    CsvDatasource setupDatasource = new CsvDatasource("setup-datasource").addValueTable("TableDataOnly", //
        null, new File(tempTestDirectory.getCanonicalFile() + "/TableDataOnly", "data.csv"));
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
  public void test_writing_data_only_modifying_multiple_value_sets_and_reading_back_from_reinitialized_datasource()
      throws Exception {
    String tableName = "TableDataOnly";
    String entityName = "Participant";
    CsvDatasource datasource = new TempTableBuilder(tableName).addData(getFileFromResource("TableDataOnly/data.csv"))
        .buildCsvDatasource("csv-datasource");

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
  public void test_writing_data_only_modifying_multiple_value_sets_and_reading_back_from_datasource() throws Exception {
    String tableName = "TableDataOnly";
    String entityName = "Participant";
    CsvDatasource datasource = new TempTableBuilder(tableName).addData(getFileFromResource("TableDataOnly/data.csv"))
        .buildCsvDatasource("csv-datasource");

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
  public void test_writing_data_only_modifying_multiple_wide_byte_value_sets_and_reading_back_from_datasource()
      throws Exception {
    String tableName = "TableDataOnly";
    String entityName = "Participant";
    CsvDatasource datasource = new TempTableBuilder(tableName).addData(getFileFromResource("TableDataOnly/data.csv"))
        .buildCsvDatasource("csv-datasource");

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

  @Nullable
  private Value readValue(ValueTable valueTable, VariableEntity variableEntity, Variable variable) {
    for(ValueSet valueSet : valueTable.getValueSets()) {
      Value value = valueTable.getValue(variable, valueSet);
      if(valueSet.getVariableEntity().equals(variableEntity)) {
        return value;
      }
    }
    return null;
  }

  @SuppressWarnings("IfStatementWithTooManyBranches")
  @Test
  public void test_refTable_data_read() throws URISyntaxException {
    CsvDatasource refDatasource = new CsvDatasource("csv-datasource1").addValueTable("Table1", //
        getFileFromResource("Table1/variables.csv"), //
        getFileFromResource("Table1/data.csv"));
    refDatasource.initialise();
    ValueTable refTable = refDatasource.getValueTable("Table1");

    CsvDatasource datasource = new CsvDatasource("csv-datasource2").addValueTable(refTable, //
        getFileFromResource("Table1/data2.csv"));
    datasource.initialise();
    assertThat(datasource.getValueTableNames().size(), is(1));

    ValueTable table = datasource.getValueTable("Table1");
    assertThat(table, notNullValue());
    assertThat(table.getEntityType(), is("Participant"));

    Variable var = table.getVariable("var2");

    for(ValueSet valueSet : table.getValueSets()) {
      String identifier = valueSet.getVariableEntity().getIdentifier();
      Value value = table.getValue(var, valueSet);
      log.debug("var2[{}]={}", identifier, value);
      assertEquals("integer", value.getValueType().getName());
      if("5".equals(identifier)) {
        assertEquals(15l, value.getValue());
      } else if("6".equals(identifier)) {
        assertEquals(16l, value.getValue());
      } else if("7".equals(identifier)) {
        assertEquals(17l, value.getValue());
      } else if("8".equals(identifier)) {
        assertEquals(18l, value.getValue());
      } else {
        assertFalse(true);
      }
    }
  }

  @Ignore
  @Test
  public void test_write_variable_is_read_back() throws Exception {
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
    writeVariableToDatasource(datasource, testVariable);
    datasource.dispose();
  }

  @Ignore
  @Test
  public void test_write_variable_schema_line() throws Exception {
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
    writeVariableToDatasource(datasource, testVariable);
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

  private void writeVariableToDatasource(Datasource datasource, Variable testVariable) throws IOException {
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
  public void test_reading_variables_confirm_var_metadata() throws Exception {
    String tableName = "TableVariablesOnly";
    String entityName = "Participant";
    CsvDatasource datasource = new TempTableBuilder(tableName).addVariables(getFileFromResource("Table1/variables.csv"))
        .buildCsvDatasource("csv-datasource");

    ValueTable table = datasource.getValueTable(tableName);
    Variable variable = table.getVariable("var2");

    assertThat(variable.getValueType().getName(), is(IntegerType.get().getName()));
    assertThat(variable.getEntityType(), is(entityName));
    assertThat(variable.getAttribute("label", Locale.ENGLISH).getValue().toString(), is("Hello I'm variable two"));
    assertThat(variable.getMimeType(), nullValue());
    datasource.dispose();
  }

  @Test(expected = MagmaRuntimeException.class)
  public void test_writing_variables_header_in_file_without_required_name_causes_error() throws Exception {
    String tableName = "TableVariablesOnly";
    new TempTableBuilder(tableName).addVariables(getFileFromResource("Table1/variables_with_no_name.csv"))
        .buildCsvDatasource("csv-datasource");
  }

  @Test(expected = MagmaRuntimeException.class)
  public void test_writing_variables_header_in_file_without_required_type_causes_error() throws Exception {
    String tableName = "TableVariablesOnly";
    new TempTableBuilder(tableName).addVariables(getFileFromResource("Table1/variables_with_no_type.csv"))
        .buildCsvDatasource("csv-datasource");
  }

  @Test(expected = MagmaRuntimeException.class)
  public void test_writing_variables_header_in_file_without_required_entity_type_causes_error() throws Exception {
    String tableName = "TableVariablesOnly";
    new TempTableBuilder(tableName).addVariables(getFileFromResource("Table1/variables_with_no_entityType.csv"))
        .buildCsvDatasource("csv-datasource");
  }

  @Test
  public void test_writing_variables_minimal_header_is_valid() throws Exception {
    String tableName = "TableVariablesOnly";
    new TempTableBuilder(tableName).addVariables(getFileFromResource("Table1/variables_minimal_header.csv"))
        .buildCsvDatasource("csv-datasource");
  }

  @Test
  public void test_writing_variables_write_new_variable_to_empty_file() throws Exception {
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
  public void test_writing_variables_write_new_variable_to_empty_file_without_providing_variables_header()
      throws Exception {
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
  public void test_writing_variables_adding_variables_to_an_existing_file() throws Exception {
    String tableName = "TableVariablesOnly";
    String entityName = "Participant";
    CsvDatasource datasource = new TempTableBuilder(tableName).addVariables(getFileFromResource("Table1/variables.csv"))
        .buildCsvDatasource("csv-datasource");

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
  public void test_writing_variables_updating_variable() throws Exception {
    String tableName = "TableVariablesOnly";
    String entityName = "Participant";
    CsvDatasource datasource = new TempTableBuilder(tableName).addVariables(getFileFromResource("Table1/variables.csv"))
        .buildCsvDatasource("csv-datasource");

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
  public void test_reading_variables_get_variables() throws Exception {
    String tableName = "TableVariablesOnly";
    CsvDatasource datasource = new TempTableBuilder(tableName).addVariables(getFileFromResource("Table1/variables.csv"))
        .buildCsvDatasource("csv-datasource");

    assertThat(((AbstractValueTable) datasource.getValueTable(tableName)).getVariables().size(), is(2));
  }

  @Test
  public void test_writing_variables_updating_wide_byte_variable() throws Exception {
    String tableName = "TableVariablesOnly";
    String entityName = "Participant";
    CsvDatasource datasource = new TempTableBuilder(tableName).addVariables(getFileFromResource("Table1/variables.csv"))
        .buildCsvDatasource("csv-datasource");

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
  public void test_repeatable_data_read() throws URISyntaxException {
    CsvDatasource datasource = new CsvDatasource("csv-datasource").addValueTable("Participants", //
        getFileFromResource("Participants/variables.csv"), //
        getFileFromResource("Participants/data.csv"));
    datasource.initialise();
    assertThat(datasource.getValueTableNames().size(), is(1));

    ValueTable table = datasource.getValueTable("Participants");
    assertThat(table, notNullValue());
    assertThat(table.getEntityType(), is("Participant"));

    Variable var = table.getVariable("Admin.Action.actionType");
    assertTrue(var.isRepeatable());

    int count = 0;
    for(ValueSet valueSet : table.getValueSets()) {
      count++;
      if(count == 1) {
        String identifier = valueSet.getVariableEntity().getIdentifier();
        Value value = table.getValue(var, valueSet);
        log.info("Admin.Action.actionType[{}]={}", identifier, value);
        assertThat(value.getValueType().getName(), is("text"));
        assertThat(value.isSequence(), is(true));
        assertThat(value.asSequence().getSize(), is(33));
      }
    }
    assertThat(count, is(2));
  }

  @Test
  public void test_multiline_data_read() throws URISyntaxException {
    CsvDatasource datasource = new CsvDatasource("csv-datasource").addValueTable("Participants", //
        getFileFromResource("Participants/variables.csv"), //
        getFileFromResource("Participants/data.csv"));
    datasource.setQuote(Quote.DOUBLE);
    datasource.setSeparator(Separator.COMMA);
    datasource.setCharacterSet("UTF-8");
    datasource.setFirstRow(1);
    datasource.initialise();

    assertThat(datasource.getValueTableNames().size(), is(1));

    ValueTable table = datasource.getValueTable("Participants");
    assertThat(table, notNullValue());
    assertThat(table.getEntityType(), is("Participant"));

    Variable var = table.getVariable("Admin.Action.comment");
    assertThat(var.isRepeatable(), is(true));

    int count = 0;
    for(ValueSet valueSet : table.getValueSets()) {
      count++;
      String identifier = valueSet.getVariableEntity().getIdentifier();
      Value value = table.getValue(var, valueSet);
      log.info("Admin.Action.comment[{}]={}", identifier, value);
      assertThat(value, notNullValue());
      assertThat(value.getValueType().getName(), is("text"));
      assertThat(value.isSequence(), is(true));

      ValueSequence seq = value.asSequence();
      if(count == 1) {
        assertThat(seq.getSize(), is(33));
        assertThat(seq.get(22).toString(), is("sample collection by Val\ndata entry by Evan"));
      } else if(count == 2) {
        assertThat(seq.get(5).toString(), is("Unable to draw from left arm due to vein rolling upon puncture. " +
            "Ct refused puncture to right arm. Saliva kit complete"));
      }
    }
    assertThat(count, is(2));
  }

  @Test
  public void test_charSet() throws URISyntaxException {
    test_charSet("Drugs-iso.csv", "ISO-8859-1");
    test_charSet("Drugs-utf8.csv", "UTF-8");
  }

  public void test_charSet(String filename, String encoding) throws URISyntaxException {
    log.info("Test {}", encoding);

    CsvDatasource datasource = new CsvDatasource("csv-datasource")
        .addValueTable("Drugs", getFileFromResource("medications/" + filename), "Drug");
    datasource.setQuote(Quote.DOUBLE);
    datasource.setSeparator(Separator.COMMA);
    datasource.setCharacterSet(encoding);
    datasource.setFirstRow(1);

    datasource.initialise();

    ValueTable table = datasource.getValueTable("Drugs");
    Variable var = table.getVariable("MEDICATION");
    assertCharSetValue(table, var, "02335204",
        "PREVNAR 13 (CORYNEBACTERIUM DIPHTHERIAE CRM-197 PROTEIN 34\u00b5G, PNEUMOCOCCAL POLYSACCHARIDE SEROTYPE 14 2.2\u00b5G, PNEUMOCOCCAL POLYSACCHARIDE SEROTYPE 18C 2.2\u00b5G, PNEUMOCOCCAL POLYSACCHARIDE SEROTYPE 19F 2.2\u00b5G, PNEUMOCOCCAL POLYSACCHARIDE SEROTYPE 23F 2.2\u00b5G, PNEUMOCOCCAL POLYSACCHARIDE SEROTYPE 4 2.2\u00b5G, PNEUMOCOCCAL POLYSACCHARIDE SEROTYPE 6B 4.4\u00b5G, PNEUMOCOCCAL POLYSACCHARIDE SEROTYPE 9V 2.2\u00b5G, PNEUMOCOCCAL POLYSACCHARIDE SEROTYPE 3 2.2\u00b5G, PNEUMOCOCCAL POLYSACCHARIDE SEROTYPE 5 2.2\u00b5G, PNEUMOCOCCAL POLYSACCHARIDE SEROTYPE 6A 2.2\u00b5G, PNEUMOCOCCAL POLYSACCHARIDE SEROTYPE 7F 2.2\u00b5G, PNEUMOCOCCAL POLYSACCHARIDE SEROTYP 19A 2.2\u00b5G, PNEUMOCOCCAL POLYSACCHARIDE SEROTYPE 1 2.2\u00b5G)");
    assertCharSetValue(table, var, "01918346", "COUMADIN TAB 2.5MG (WARFARIN SODIUM 2.5MG)");
  }

  private void assertCharSetValue(ValueTable table, Variable var, String identifier, String expected) {
    Value value = table.getValue(var, table.getValueSet(new VariableEntityBean("Drug", identifier)));
    log.info("{} : {}", identifier, value.toString());
    assertThat(value.toString(), is(expected));
  }

  @SuppressWarnings("ReuseOfLocalVariable")
  @Test
  public void test_backslash() throws URISyntaxException {
    CsvDatasource datasource = new CsvDatasource("csv-datasource").addValueTable("Table1", //
        getFileFromResource("Table1/escaped-variables.csv"), //
        getFileFromResource("Table1/escaped-data.csv"));
    datasource.initialise();

    assertThat(datasource.getValueTableNames().size(), is(1));

    ValueTable table = datasource.getValueTable("Table1");
    assertThat(table, notNullValue());
    assertThat(table.getEntityType(), is("Participant"));

    Variable name = table.getVariable("name");
    assertThat(name, notNullValue());
    assertThat(name.getValueType().getName(), is("text"));
    assertThat(name.getEntityType(), is("Participant"));
    assertThat(name.getMimeType(), nullValue());
    assertThat(name.getUnit(), nullValue());
    assertThat(name.getOccurrenceGroup(), nullValue());
    assertThat(name.isRepeatable(), is(false));

    Variable children = table.getVariable("children");
    assertThat(children, notNullValue());
    assertThat(children.getValueType().getName(), is("text"));
    assertThat(children.getEntityType(), is("Participant"));
    assertThat(children.getMimeType(), nullValue());
    assertThat(children.getUnit(), nullValue());
    assertThat(children.getOccurrenceGroup(), nullValue());
    assertThat(children.isRepeatable(), is(true));

    VariableEntity entity1 = new VariableEntityBean("Participant", "1");
    Value value = table.getValue(name, table.getValueSet(entity1));
    assertThat(value.isSequence(), is(false));
    assertThat((String) value.getValue(), is("Julius\nCaesar"));

    value = table.getValue(children, table.getValueSet(entity1));
    assertThat(value.isSequence(), is(true));
    ValueSequence valueSequence = value.asSequence();
    assertThat((String) valueSequence.get(0).getValue(), is("Julia"));
    assertThat((String) valueSequence.get(1).getValue(), is("Caesarion"));
    assertThat((String) valueSequence.get(2).getValue(), is("Gaius\\\\\\\\Julius Caesar Octavianus"));
    assertThat(valueSequence.get(3).isNull(), is(true));
    assertThat((String) valueSequence.get(4).getValue(), is("Marcus Junius Brutus"));

    VariableEntity entity2 = new VariableEntityBean("Participant", "2");

    value = table.getValue(name, table.getValueSet(entity2));
    assertThat(value.isSequence(), is(false));
    assertThat((String) value.getValue(), is("Cleopatra\\\\\"VII"));

    value = table.getValue(children, table.getValueSet(entity2));
    assertThat(value.isSequence(), is(true));
    valueSequence = value.asSequence();
    assertThat((String) valueSequence.get(0).getValue(), is("Ptolemy XV"));
    assertThat(valueSequence.get(1).isNull(), is(true));
    assertThat((String) valueSequence.get(2).getValue(), is("Caesarion"));
    assertThat((String) valueSequence.get(3).getValue(), is("Alexander Helios"));
    assertThat((String) valueSequence.get(4).getValue(), is("Cleopatra Selene"));
    assertThat((String) valueSequence.get(5).getValue(), is("Ptolemy XVI Philadelphus"));

    value = table.getValue(name, table.getValueSet(new VariableEntityBean("Participant", "3")));
    assertThat(value.isSequence(), is(false));
    assertThat((String) value.getValue(), is("Clau\\dius"));

    try {
      table.getValue(name, table.getValueSet(new VariableEntityBean("Participant", "4")));
    } catch(NoSuchValueSetException e) {
      Assert.fail("Should throw NoSuchValueSetException for Participant 4");
    }
    try {
      table.getValue(name, table.getValueSet(new VariableEntityBean("Participant", "5")));
      Assert.fail("Should throw NoSuchValueSetException for Participant 5");
    } catch(NoSuchValueSetException e) {
    }
  }

  @SuppressWarnings("ReuseOfLocalVariable")
  @Test
  public void test_endOfLine() throws URISyntaxException, IOException {

    StringBuilder sb = new StringBuilder();
    sb.append("entity_id,Name,\"Complete name\"\n");
    sb.append("1,Augustus,\"GAIVS IVLIVS \nCAESAR OCTAVIANVS\"\n");
    sb.append("2,Tiberius,\"TIBERIVS IVLIVS CAESAR AVGVSTVS\"\r");
    sb.append("3,Caligula,\"GAIVS IVLIVS CAESAR AVGVSTVS GERMANICVS\"\r\n");
    sb.append("4,Claudius,\"TIBERIVS CLAVDIVS CAESAR AVGVSTVS GERMANICVS\"\n\n");
    sb.append("5,Nero,\"NERO CLAVDIVS CAESAR AVGVSTVS GERMANICVS\"");

    File dataFile = File.createTempFile("magma", "test-eol");
    dataFile.deleteOnExit();
    FileUtils.writeStringToFile(dataFile, sb.toString(), "utf-8");

    CsvDatasource datasource = new CsvDatasource("csv-datasource").addValueTable("Table1", dataFile, "Participant");
    datasource.initialise();

    assertThat(datasource.getValueTableNames().size(), is(1));

    ValueTable table = datasource.getValueTable("Table1");
    assertThat(table, notNullValue());
    assertThat(table.getEntityType(), is("Participant"));

    assertEolVariable(table, "Name");
    assertEolVariable(table, "Complete name");

    assertEolValue(table, "1", "Augustus", "GAIVS IVLIVS \nCAESAR OCTAVIANVS");
    assertEolValue(table, "2", "Tiberius", "TIBERIVS IVLIVS CAESAR AVGVSTVS");
    assertEolValue(table, "3", "Caligula", "GAIVS IVLIVS CAESAR AVGVSTVS GERMANICVS");
    assertEolValue(table, "4", "Claudius", "TIBERIVS CLAVDIVS CAESAR AVGVSTVS GERMANICVS");
    assertEolValue(table, "5", "Nero", "NERO CLAVDIVS CAESAR AVGVSTVS GERMANICVS");
  }

  private void assertEolVariable(ValueTable table, String name) {
    Variable variable = table.getVariable(name);
    assertThat(variable, notNullValue());
    assertThat(variable.getName(), is(name));
    assertThat(variable.getValueType().getName(), is("text"));
    assertThat(variable.getEntityType(), is("Participant"));
    assertThat(variable.getMimeType(), nullValue());
    assertThat(variable.getUnit(), nullValue());
    assertThat(variable.getOccurrenceGroup(), nullValue());
    assertThat(variable.isRepeatable(), is(false));
  }

  @SuppressWarnings("ReuseOfLocalVariable")
  private void assertEolValue(ValueTable table, String entityId, String name, String completeName) {
    VariableEntity entity = new VariableEntityBean("Participant", entityId);
    Value value = table.getValue(table.getVariable("Name"), table.getValueSet(entity));
    assertThat(value.isSequence(), is(false));
    assertThat((String) value.getValue(), is(name));

    value = table.getValue(table.getVariable("Complete name"), table.getValueSet(entity));
    assertThat(value.isSequence(), is(false));
    assertThat((String) value.getValue(), is(completeName));
  }

}
