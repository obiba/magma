/*
 * Copyright (c) 2013 OBiBa. All rights reserved.
 *  
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *  
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.magma.datasource.csv;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

import org.apache.commons.io.FileUtils;
import org.junit.Ignore;
import org.junit.Test;
import org.obiba.magma.Datasource;
import org.obiba.magma.MagmaRuntimeException;
import org.obiba.magma.Value;
import org.obiba.magma.ValueSequence;
import org.obiba.magma.ValueSet;
import org.obiba.magma.ValueTable;
import org.obiba.magma.ValueTableWriter;
import org.obiba.magma.Variable;
import org.obiba.magma.VariableEntity;
import org.obiba.magma.support.VariableEntityBean;
import org.obiba.magma.test.AbstractMagmaTest;
import org.obiba.magma.type.TextType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.obiba.core.util.FileUtil.getFileFromResource;
import static org.obiba.magma.datasource.csv.CsvValueTable.DEFAULT_ENTITY_TYPE;

/**
 *
 */
@SuppressWarnings({ "PMD.NcssMethodCount", "ResultOfMethodCallIgnored", "OverlyLongMethod" })
@edu.umd.cs.findbugs.annotations.SuppressWarnings({ "RV_RETURN_VALUE_IGNORED_BAD_PRACTICE", "SF_SWITCH_NO_DEFAULT" })
public class CsvValueTableWriterTest extends AbstractMagmaTest {

  private static final Logger log = LoggerFactory.getLogger(CsvValueTableWriterTest.class);

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

    try(ValueTableWriter tableWriter = datasource.createWriter("TableDataOnly", "Participant");
        ValueTableWriter.ValueSetWriter valueSetWriter = tableWriter.writeValueSet(variableEntity)) {
      valueSetWriter.writeValue(testVariable, secondCup);
    }

    CsvDatasource readDatasource = new CsvDatasource("read-csv-datasource").addValueTable("TableDataOnly", //
        null, //
        new File(tempTestDirectory.getCanonicalFile() + "/TableDataOnly", "data.csv"));
    readDatasource.initialise();

    ValueTable table = readDatasource.getValueTable("TableDataOnly");

    Variable variable = table.getVariable("test-variable");

    for(ValueSet valueSet : table.getValueSets()) {
      Value value = table.getValue(variable, valueSet);
      assertThat(value.getValue()).isNotNull();
      assertThat(value.getValue().toString()).isEqualTo("Second Cup");
    }
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

    assertThat(vt.getEntityType()).isEqualTo("entityType");
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

  @Test(expected = MagmaRuntimeException.class)
  public void test_writing_data_only_ensure_writing_extra_header_fails() throws Exception {
    // This existing datasource has the following header: entity_id,FirstName,LastName,Sex,City,FavouriteIcecream
    CsvDatasource datasource = new CsvDatasource("csv-datasource").addValueTable("TableDataOnly", //
        null, //
        getFileFromResource("org/obiba/magma/datasource/csv/TableDataOnly/data.csv"));
    datasource.initialise();

    // Attempt to add a new ValueSet containing a new Variable "coffee". Expect this to fail.
    VariableEntity variableEntity = new VariableEntityBean("Participant", "5");
    Variable testVariable = Variable.Builder.newVariable("coffee", TextType.get(), "Participant").build();
    Value secondCup = TextType.get().valueOf("Second Cup");

    try(ValueTableWriter tableWriter = datasource.createWriter("TableDataOnly", "Participant");
        ValueTableWriter.ValueSetWriter valueSetWriter = tableWriter.writeValueSet(variableEntity)) {
      valueSetWriter.writeValue(testVariable, secondCup);
    }
  }

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

    try(ValueTableWriter tableWriter = setupDatasource.createWriter("TableDataOnly", "Participant");
        ValueTableWriter.ValueSetWriter valueSetWriter = tableWriter.writeValueSet(variableEntity)) {
      valueSetWriter.writeValue(coffeeVariable, secondCup);
      valueSetWriter.writeValue(teaVariable, earlGrey);
      valueSetWriter.writeValue(biscuitVariable, cheese);
    }

    CsvDatasource writeDatasource = new CsvDatasource("csv-datasource").addValueTable("TableDataOnly", //
        null, //
        new File(tempTestDirectory.getCanonicalFile() + "/TableDataOnly", "data.csv"));
    writeDatasource.initialise();

    VariableEntity participantTwoEntity = new VariableEntityBean("Participant", "2");

    Value orangePekoe = TextType.get().valueOf("Orange Pekoe");

    try(ValueTableWriter tableWriter = writeDatasource.createWriter("TableDataOnly", "Participant");
        ValueTableWriter.ValueSetWriter valueSetWriter = tableWriter.writeValueSet(participantTwoEntity)) {
      valueSetWriter.writeValue(teaVariable, orangePekoe);
    }

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
      switch(identifier) {
        case "1":
          assertThat(coffeeValue.getValue().toString()).isEqualTo("Second Cup");
          assertThat(teaValue.getValue().toString()).isEqualTo("Earl Grey");
          assertThat(biscuitValue.getValue().toString()).isEqualTo("cheese");
          break;
        case "2":
          assertThat(coffeeValue.isNull()).isTrue();
          assertThat(teaValue.getValue().toString()).isEqualTo("Orange Pekoe");
          assertThat(biscuitValue.isNull()).isTrue();
          break;
      }
    }
  }

  @Test
  public void test_writing_data_only_modifying_multiple_value_sets_and_reading_back_from_reinitialized_datasource()
      throws Exception {
    String tableName = "TableDataOnly";
    String entityName = "Participant";
    CsvDatasource datasource = new TempTableBuilder(tableName)
        .addData(getFileFromResource("org/obiba/magma/datasource/csv/TableDataOnly/data.csv"))
        .buildCsvDatasource("csv-datasource");

    Variable cityVariable = Variable.Builder.newVariable("City", TextType.get(), "Participant").build();
    Value cityValueVancouver = TextType.get().valueOf("Vancouver");

    Map<Variable, Value> values = Maps.newHashMap();

    try(ValueTableWriter writer = datasource.createWriter(tableName, entityName)) {
      values.put(cityVariable, cityValueVancouver);
      writeValueSet(new VariableEntityBean(DEFAULT_ENTITY_TYPE, "4"), writer, values);

      values.put(cityVariable, cityValueVancouver);
      writeValueSet(new VariableEntityBean(DEFAULT_ENTITY_TYPE, "2"), writer, values);
    }

    datasource.dispose();
    datasource.initialise();
    assertThat(readValue(datasource.getValueTable(tableName), new VariableEntityBean(entityName, "2"), cityVariable))
        .isEqualTo(cityValueVancouver);
    datasource.dispose();
  }

  @Test
  public void test_writing_data_only_modifying_multiple_value_sets_and_reading_back_from_datasource() throws Exception {
    String tableName = "TableDataOnly";
    CsvDatasource datasource = new TempTableBuilder(tableName)
        .addData(getFileFromResource("org/obiba/magma/datasource/csv/TableDataOnly/data.csv"))
        .buildCsvDatasource("csv-datasource");

    Variable cityVariable = Variable.Builder.newVariable("City", TextType.get(), "Participant").build();
    Value cityValueVancouver = TextType.get().valueOf("Vancouver");

    VariableEntity entity2 = new VariableEntityBean(DEFAULT_ENTITY_TYPE, "2");

    Map<Variable, Value> values = Maps.newHashMap();
    try(ValueTableWriter writer = datasource.createWriter(tableName, DEFAULT_ENTITY_TYPE)) {

      values.put(cityVariable, cityValueVancouver);
      writeValueSet(new VariableEntityBean(DEFAULT_ENTITY_TYPE, "4"), writer, values);

      values.put(cityVariable, TextType.get().valueOf("Moncton"));
      writeValueSet(entity2, writer, values);

      values.put(cityVariable, TextType.get().valueOf("Regina"));
      writeValueSet(entity2, writer, values);

      values.put(cityVariable, cityValueVancouver);
      writeValueSet(entity2, writer, values);
    }

    assertThat(
        readValue(datasource.getValueTable(tableName), new VariableEntityBean(DEFAULT_ENTITY_TYPE, "2"), cityVariable))
        .isEqualTo(cityValueVancouver);
    datasource.dispose();
  }

  @Test
  public void test_writing_data_only_modifying_multiple_wide_byte_value_sets_and_reading_back_from_datasource()
      throws Exception {
    String tableName = "TableDataOnly";
    CsvDatasource datasource = new TempTableBuilder(tableName)
        .addData(getFileFromResource("org/obiba/magma/datasource/csv/TableDataOnly/data.csv"))
        .buildCsvDatasource("csv-datasource");

    Variable cityVariable = Variable.Builder.newVariable("City", TextType.get(), "Participant").build();
    Value wideByteCityName = TextType.get().valueOf("Sugg�r�");

    Map<Variable, Value> values = Maps.newHashMap();
    VariableEntity entity2 = new VariableEntityBean(DEFAULT_ENTITY_TYPE, "2");

    try(ValueTableWriter writer = datasource.createWriter(tableName, DEFAULT_ENTITY_TYPE)) {
      // Write wide byte line
      values.put(cityVariable, wideByteCityName);
      writeValueSet(entity2, writer, values);

      // Write line after wide byte line. This one is in danger of being partially overwritten during an update.
      values.put(cityVariable, TextType.get().valueOf("Moncton"));
      writeValueSet(new VariableEntityBean(DEFAULT_ENTITY_TYPE, "3"), writer, values);

      // Update the wide byte line (2) to ensure that line (3) is not affected.
      values.put(cityVariable, TextType.get().valueOf("Regina"));
      writeValueSet(entity2, writer, values);
    }

    assertThat(
        readValue(datasource.getValueTable(tableName), new VariableEntityBean(DEFAULT_ENTITY_TYPE, "3"), cityVariable))
        .isEqualTo(TextType.get().valueOf("Moncton"));
    datasource.dispose();
  }

  @Test(expected = MagmaRuntimeException.class)
  public void test_writing_variables_header_in_file_without_required_name_causes_error() throws Exception {
    String tableName = "TableVariablesOnly";
    new TempTableBuilder(tableName)
        .addVariables(getFileFromResource("org/obiba/magma/datasource/csv/Table1/variables_with_no_name.csv"))
        .buildCsvDatasource("csv-datasource");
  }

  @Test(expected = MagmaRuntimeException.class)
  public void test_writing_variables_header_in_file_without_required_type_causes_error() throws Exception {
    String tableName = "TableVariablesOnly";
    new TempTableBuilder(tableName)
        .addVariables(getFileFromResource("org/obiba/magma/datasource/csv/Table1/variables_with_no_type.csv"))
        .buildCsvDatasource("csv-datasource");
  }

  @Test(expected = MagmaRuntimeException.class)
  public void test_writing_variables_header_in_file_without_required_entity_type_causes_error() throws Exception {
    String tableName = "TableVariablesOnly";
    new TempTableBuilder(tableName)
        .addVariables(getFileFromResource("org/obiba/magma/datasource/csv/Table1/variables_with_no_entityType.csv"))
        .buildCsvDatasource("csv-datasource");
  }

  @Test
  public void test_writing_variables_minimal_header_is_valid() throws Exception {
    String tableName = "TableVariablesOnly";
    new TempTableBuilder(tableName)
        .addVariables(getFileFromResource("org/obiba/magma/datasource/csv/Table1/variables_minimal_header.csv"))
        .buildCsvDatasource("csv-datasource");
  }

  @Test
  public void test_writing_variables_write_new_variable_to_empty_file() throws Exception {
    String tableName = "TableVariablesOnly";
    CsvDatasource datasource = new TempTableBuilder(tableName).addVariables()
        .variablesHeader("name#valueType#entityType#label".split("#")).buildCsvDatasource("csv-datasource");

    Variable variable = Variable.Builder.newVariable("coffee", TextType.get(), DEFAULT_ENTITY_TYPE)
        .addAttribute("label", "Please indicated your favourite coffee vendor.").build();

    try(ValueTableWriter writer = datasource.createWriter(tableName, DEFAULT_ENTITY_TYPE)) {
      writeVariable(writer, variable);
    }
  }

  @Test
  public void test_writing_variables_write_new_variable_to_empty_file_without_providing_variables_header()
      throws Exception {
    String tableName = "TableVariablesOnly";
    CsvDatasource datasource = new TempTableBuilder(tableName).addVariables().buildCsvDatasource("csv-datasource");

    Variable variable = Variable.Builder.newVariable("coffee", TextType.get(), DEFAULT_ENTITY_TYPE)
        .addAttribute("label", "Please indicated your favourite coffee vendor.").build();

    try(ValueTableWriter writer = datasource.createWriter(tableName, DEFAULT_ENTITY_TYPE)) {
      writeVariable(writer, variable);
    }
  }

  @SuppressWarnings("ReuseOfLocalVariable")
  @Test
  public void test_writing_escaped_characters() throws Exception {

    File dataFile = getFileFromResource("org/obiba/magma/datasource/csv/Table1/no-data.csv");

    CsvDatasource datasource = new CsvDatasource("csv-datasource")
        .addValueTable("Table1", getFileFromResource("org/obiba/magma/datasource/csv/Table1/escaped-variables.csv"),
            dataFile);
    datasource.initialise();

    ValueTable table = datasource.getValueTable("Table1");
    Variable name = table.getVariable("name");
    Variable children = table.getVariable("children");

    CsvDatasourceTest.assertEmperors(datasource, table, name, children);

    VariableEntity entity = new VariableEntityBean(DEFAULT_ENTITY_TYPE, "1");

    try(ValueTableWriter writer = datasource.createWriter(table.getName(), table.getEntityType())) {
      Map<Variable, Value> values = Maps.newHashMap();
      values.put(name, TextType.get().valueOf("Julius\nCaesar"));
      values.put(children,
          getSequenceOf("Julia", "Caesarion", "Gaius\\\\Julius Caesar \"Octavianus\"", null, "Marcus Junius\" Brutus"));
      writeValueSet(entity, writer, values);
    }

    assertJuliusCaesarName(table, name, entity);
    assertJuliusCaesarChildren(table, children, entity);

    String fileContent = FileUtils.readFileToString(dataFile);
    log.debug("\n=====\n{}=====", fileContent);

    assertThat(fileContent).isEqualTo("\"entity_id\",\"name\",\"children\"\n" +
        "\"1\",\"Julius\n" +
        "Caesar\",\"\"\"Julia\"\",\"\"Caesarion\"\",\"\"Gaius\\\\Julius Caesar \"\"\"\"Octavianus\"\"\"\"\"\",,\"\"Marcus Junius\"\"\"\" Brutus\"\"\"\n");

    datasource.dispose();

    datasource = new CsvDatasource("csv-datasource")
        .addValueTable("Table1", getFileFromResource("org/obiba/magma/datasource/csv/Table1/escaped-variables.csv"),
            dataFile);
    datasource.initialise();

    table = datasource.getValueTable("Table1");
    name = table.getVariable("name");
    children = table.getVariable("children");

    CsvDatasourceTest.assertEmperors(datasource, table, name, children);
    assertJuliusCaesarName(table, name, entity);
    assertJuliusCaesarChildren(table, children, entity);

    datasource.dispose();
  }

  private void assertJuliusCaesarChildren(ValueTable table, Variable children, VariableEntity entity) {
    Value value = table.getValue(children, table.getValueSet(entity));
    assertThat(value.isSequence()).isTrue();
    ValueSequence valueSequence = value.asSequence();
    assertThat((String) valueSequence.get(0).getValue()).isEqualTo("Julia");
    assertThat((String) valueSequence.get(1).getValue()).isEqualTo("Caesarion");
    assertThat((String) valueSequence.get(2).getValue()).isEqualTo("Gaius\\\\Julius Caesar \"Octavianus\"");
    assertThat(valueSequence.get(3).isNull()).isTrue();
    assertThat((String) valueSequence.get(4).getValue()).isEqualTo("Marcus Junius\" Brutus");
  }

  private void assertJuliusCaesarName(ValueTable table, Variable name, VariableEntity entity) {
    Value value = table.getValue(name, table.getValueSet(entity));
    assertThat(value.isSequence()).isFalse();
    assertThat((String) value.getValue()).isEqualTo("Julius\nCaesar");
  }

  private File createTempDirectory(String suffix) throws IOException {
    File dir = File.createTempFile(suffix, "");
    dir.delete();
    dir.mkdir();
    dir.deleteOnExit();
    return dir;
  }

  private void writeVariableToDatasource(Datasource datasource, Variable testVariable) throws IOException {
    try(ValueTableWriter tableWriter = datasource.createWriter("test-table", "entityType");
        ValueTableWriter.VariableWriter variableWriter = tableWriter.writeVariables()) {
      variableWriter.writeVariable(testVariable);
    }
  }

  private void writeValueSet(VariableEntity variableEntity, ValueTableWriter valueTableWriter,
      Map<Variable, Value> values) throws IOException {
    try(ValueTableWriter.ValueSetWriter valueSetWriter = valueTableWriter.writeValueSet(variableEntity)) {
      for(Map.Entry<Variable, Value> entry : values.entrySet()) {
        valueSetWriter.writeValue(entry.getKey(), entry.getValue());
      }
    }
  }

  private void writeVariable(ValueTableWriter valueTableWriter, Variable variable) throws IOException {
    try(ValueTableWriter.VariableWriter variableWriter = valueTableWriter.writeVariables()) {
      variableWriter.writeVariable(variable);
    }
  }

  private Value getSequenceOf(String... values) {
    List<Value> list = Lists.newArrayList();
    for(String str : values) {
      list.add(TextType.get().valueOf(str));
    }
    return TextType.get().sequenceOf(list);
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
}
