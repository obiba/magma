/*
 * Copyright (c) 2019 OBiBa. All rights reserved.
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
  @Ignore
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
  @Ignore
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

  private void writeVariable(ValueTableWriter valueTableWriter, Variable variable) throws IOException {
    try(ValueTableWriter.VariableWriter variableWriter = valueTableWriter.writeVariables()) {
      variableWriter.writeVariable(variable);
    }
  }
}
