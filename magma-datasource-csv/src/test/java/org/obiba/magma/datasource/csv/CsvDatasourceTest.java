package org.obiba.magma.datasource.csv;

import java.io.File;
import java.io.IOException;
import java.util.Locale;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.junit.Test;
import org.obiba.magma.Category;
import org.obiba.magma.NoSuchValueSetException;
import org.obiba.magma.Value;
import org.obiba.magma.ValueSequence;
import org.obiba.magma.ValueSet;
import org.obiba.magma.ValueTable;
import org.obiba.magma.Variable;
import org.obiba.magma.VariableEntity;
import org.obiba.magma.datasource.csv.support.Quote;
import org.obiba.magma.datasource.csv.support.Separator;
import org.obiba.magma.support.AbstractValueTable;
import org.obiba.magma.support.DatasourceParsingException;
import org.obiba.magma.support.EntitiesPredicate;
import org.obiba.magma.support.VariableEntityBean;
import org.obiba.magma.test.AbstractMagmaTest;
import org.obiba.magma.type.IntegerType;
import org.obiba.magma.type.TextType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ImmutableMap;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.junit.Assert.fail;
import static org.obiba.core.util.FileUtil.getFileFromResource;
import static org.obiba.magma.datasource.csv.CsvValueTable.DEFAULT_ENTITY_TYPE;

@SuppressWarnings({ "PMD.NcssMethodCount", "OverlyLongMethod", "ResultOfMethodCallIgnored", "OverlyCoupledClass" })
@edu.umd.cs.findbugs.annotations.SuppressWarnings("RV_RETURN_VALUE_IGNORED_BAD_PRACTICE")
public class CsvDatasourceTest extends AbstractMagmaTest {

  private static final Logger log = LoggerFactory.getLogger(CsvDatasourceTest.class);

  @Test
  public void test_separators() {
    assertThat(Quote.fromString("'")).isEqualTo(Quote.SINGLE);
    assertThat(Quote.fromString("\"")).isEqualTo(Quote.DOUBLE);
    assertThat(Quote.fromString("|").getCharacter()).isEqualTo('|');

    assertThat(Separator.fromString(",")).isEqualTo(Separator.COMMA);
    assertThat(Separator.fromString(";")).isEqualTo(Separator.SEMICOLON);
    assertThat(Separator.fromString(":")).isEqualTo(Separator.COLON);
    assertThat(Separator.fromString("\t")).isEqualTo(Separator.TAB);
    assertThat(Separator.fromString("|").getCharacter()).isEqualTo('|');
  }

  @Test
  public void test_supports_any_separator() {
    File samples = getFileFromResource("separators");
    File variables = new File(samples, "variables.csv");

    CsvDatasource ds = new CsvDatasource("variables").addValueTable("variables", variables, (File) null);
    ds.initialise();
    ValueTable reference = ds.getValueTable("variables");

    Map<String, String> separators = ImmutableMap.<String, String>builder().put("sample-comma.csv", ",")
        .put("sample-semicolon.csv", ";").put("sample-colon.csv", ":").put("sample-tab.csv", "tab")
        .put("sample-pipe.csv", "|").put("sample-space.csv", " ").build();
    File[] files = samples.listFiles();
    assertThat(files).isNotNull();
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
      assertThat(valueTable.getVariableEntities()).hasSize(16);
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
  public void test_table_variable_read() {
    CsvDatasource datasource = new CsvDatasource("csv-datasource").addValueTable("Table1", //
        getFileFromResource("Table1/variables.csv"), //
        getFileFromResource("Table1/data.csv"));
    datasource.initialise();
    assertThat(datasource.getValueTableNames()).hasSize(1);

    ValueTable table = datasource.getValueTable("Table1");
    assertThat(table).isNotNull();
    assertThat(table.getEntityType()).isEqualTo("Participant");

    Variable var = table.getVariable("var1");
    assertThat(var).isNotNull();
    assertThat(var.getValueType().getName()).isEqualTo("text");
    assertThat(var.getEntityType()).isEqualTo("Participant");
    assertThat(var.getMimeType()).isNull();
    assertThat(var.getUnit()).isNull();
    assertThat(var.getOccurrenceGroup()).isNull();
    assertThat(var.isRepeatable()).isFalse();

    assertThat(var.getCategories()).hasSize(4);
    for(Category category : var.getCategories()) {
      String label = category.getAttribute("label", Locale.ENGLISH).getValue().toString();
      String categoryName = category.getName();
      switch(categoryName) {
        case "Y":
          assertThat(label).isEqualTo("yes");
          break;
        case "N":
          assertThat(label).isEqualTo("no");
          break;
        case "PNA":
          assertThat(label).isEqualTo("prefer not to answer");
          break;
        case "DNK":
          assertThat(label).isEqualTo("don't know");
          break;
        default:
          fail();
          break;
      }
    }

    assertThat(var.getAttributes()).hasSize(3);
    assertThat(var.hasAttribute("label")).isTrue();
    assertThat(var.getAttribute("label", Locale.ENGLISH).getValue().toString()).isEqualTo("Hello I'm variable one");
    assertThat(var.getAttribute("ns1", "attr").getValue().toString()).isEqualTo("ns1");
    assertThat(var.getAttribute("ns2", "attr", Locale.ENGLISH).getValue().toString()).isEqualTo("ns2");
  }

  @SuppressWarnings("IfStatementWithTooManyBranches")
  @Test
  public void test_table_data_read() {
    CsvDatasource datasource = new CsvDatasource("csv-datasource").addValueTable("Table1", //
        getFileFromResource("Table1/variables.csv"), //
        getFileFromResource("Table1/data.csv"));
    datasource.initialise();
    assertThat(datasource.getValueTableNames()).hasSize(1);

    ValueTable table = datasource.getValueTable("Table1");
    assertThat(table).isNotNull();
    assertThat(table.getEntityType()).isEqualTo("Participant");

    Variable var = table.getVariable("var1");

    for(ValueSet valueSet : table.getValueSets()) {
      String identifier = valueSet.getVariableEntity().getIdentifier();
      Value value = table.getValue(var, valueSet);
      log.debug("var1[{}]={}", identifier, value);
      assertThat(value.getValueType().getName()).isEqualTo("text");
      switch(identifier) {
        case "1":
          assertThat(value.getValue()).isEqualTo("Y");
          break;
        case "2":
          assertThat(value.getValue()).isEqualTo("N");
          break;
        case "3":
          assertThat(value.getValue()).isEqualTo("PNA");
          break;
        case "4":
          assertThat(value.getValue()).isEqualTo("DNK");
          break;
        default:
          fail();
          break;
      }
    }
  }

  @Test
  public void test_reading_data_only_table_has_only_one_table() throws Exception {
    CsvDatasource datasource = new CsvDatasource("csv-datasource").addValueTable("TableDataOnly", //
        null, //
        getFileFromResource("org/obiba/magma/datasource/csv/TableDataOnly/data.csv"));
    datasource.initialise();

    assertThat(datasource.getValueTableNames()).hasSize(1);
  }

  @Test
  public void test_reading_data_only_table_is_not_null() throws Exception {
    CsvDatasource datasource = new CsvDatasource("csv-datasource").addValueTable("TableDataOnly", //
        null, //
        getFileFromResource("org/obiba/magma/datasource/csv/TableDataOnly/data.csv"));
    datasource.initialise();

    assertThat(datasource.getValueTable("TableDataOnly")).isNotNull();
  }

  @Test
  public void test_reading_data_only_table_entity_type_is_default_participant() throws Exception {
    CsvDatasource datasource = new CsvDatasource("csv-datasource").addValueTable("TableDataOnly", //
        null, //
        getFileFromResource("org/obiba/magma/datasource/csv/TableDataOnly/data.csv"));
    datasource.initialise();
    ValueTable table = datasource.getValueTable("TableDataOnly");

    assertThat(table.getEntityType()).isEqualTo(DEFAULT_ENTITY_TYPE);
  }

  @Test
  public void test_reading_data_only_favourite_ice_cream_variable_exists() throws Exception {
    CsvDatasource datasource = new CsvDatasource("csv-datasource").addValueTable("TableDataOnly", //
        null, //
        getFileFromResource("org/obiba/magma/datasource/csv/TableDataOnly/data.csv"));
    datasource.initialise();
    ValueTable table = datasource.getValueTable("TableDataOnly");

    assertThat(table.getVariable("FavouriteIcecream")).isNotNull();
  }

  @Test
  public void test_reading_single_data_only_table_null_ice_cream_value() throws Exception {
    CsvDatasource datasource = new CsvDatasource("csv-datasource").addValueTable("TableDataOnly", //
        null, //
        getFileFromResource("org/obiba/magma/datasource/csv/TableDataOnly/data.csv"));
    datasource.initialise();
    ValueTable table = datasource.getValueTable("TableDataOnly");

    Variable favouriteIcecream = table.getVariable("FavouriteIcecream");

    for(ValueSet valueSet : table.getValueSets()) {
      String identifier = valueSet.getVariableEntity().getIdentifier();
      Value value = table.getValue(favouriteIcecream, valueSet);
      if("1".equals(identifier) || "2".equals(identifier)) {
        assertThat(value.isNull()).isTrue();
      }
    }
  }

  @Test
  public void test_reading_data_only_value_type_is_text() throws Exception {
    CsvDatasource datasource = new CsvDatasource("csv-datasource").addValueTable("TableDataOnly", //
        null, //
        getFileFromResource("org/obiba/magma/datasource/csv/TableDataOnly/data.csv"));
    datasource.initialise();
    ValueTable table = datasource.getValueTable("TableDataOnly");

    for(Variable variable : table.getVariables()) {
      assertThat(variable.getValueType().getName()).isEqualTo(TextType.get().getName());
    }
  }

  @Test
  public void test_value_table_get_variable_entities() throws Exception {
    CsvDatasource datasource = new CsvDatasource("csv-datasource").addValueTable("TableDataOnly", //
        null, getFileFromResource("org/obiba/magma/datasource/csv/TableDataOnly/data.csv"));
    datasource.initialise();

    ValueTable table = datasource.getValueTable("TableDataOnly");
    assertThat(table.getVariableEntities()).hasSize(4);
  }

  @Test
  public void test_value_table_get_variables() throws Exception {
    CsvDatasource datasource = new CsvDatasource("csv-datasource").addValueTable("TableDataOnly", //
        null, getFileFromResource("org/obiba/magma/datasource/csv/TableDataOnly/data.csv"));
    datasource.initialise();

    ValueTable table = datasource.getValueTable("TableDataOnly");
    assertThat(table.getVariables()).hasSize(5);

    //noinspection TypeMayBeWeakened
    CsvValueTable cvsValueTable = (CsvValueTable) table;
    assertThat(cvsValueTable.getVariables()).hasSize(5);
  }

  @SuppressWarnings("IfStatementWithTooManyBranches")
  @Test
  public void test_refTable_data_read() {
    CsvDatasource refDatasource = new CsvDatasource("csv-datasource1").addValueTable("Table1", //
        getFileFromResource("Table1/variables.csv"), //
        getFileFromResource("Table1/data.csv"));
    refDatasource.initialise();
    ValueTable refTable = refDatasource.getValueTable("Table1");

    CsvDatasource datasource = new CsvDatasource("csv-datasource2").addValueTable(refTable, //
        getFileFromResource("Table1/data2.csv"));
    datasource.initialise();
    assertThat(datasource.getValueTableNames()).hasSize(1);

    ValueTable table = datasource.getValueTable("Table1");
    assertThat(table).isNotNull();
    assertThat(table.getEntityType()).isEqualTo("Participant");

    Variable var = table.getVariable("var2");

    for(ValueSet valueSet : table.getValueSets()) {
      String identifier = valueSet.getVariableEntity().getIdentifier();
      Value value = table.getValue(var, valueSet);
      log.debug("var2[{}]={}", identifier, value);
      assertThat(value.getValueType().getName()).isEqualTo("integer");
      switch(identifier) {
        case "5":
          assertThat(value.getValue()).isEqualTo(15l);
          break;
        case "6":
          assertThat(value.getValue()).isEqualTo(16l);
          break;
        case "7":
          assertThat(value.getValue()).isEqualTo(17l);
          break;
        case "8":
          assertThat(value.getValue()).isEqualTo(18l);
          break;
        default:
          fail();
          break;
      }
    }
  }

  // Variable Tests

  @Test
  public void test_reading_variables_confirm_var_metadata() throws Exception {
    String tableName = "TableVariablesOnly";
    CsvDatasource datasource = new TempTableBuilder(tableName).addVariables(getFileFromResource("Table1/variables.csv"))
        .buildCsvDatasource("csv-datasource");

    ValueTable table = datasource.getValueTable(tableName);
    Variable variable = table.getVariable("var2");

    assertThat(variable.getValueType().getName()).isEqualTo(IntegerType.get().getName());
    assertThat(variable.getEntityType()).isEqualTo(DEFAULT_ENTITY_TYPE);
    assertThat(variable.getAttribute("label", Locale.ENGLISH).getValue().toString())
        .isEqualTo("Hello I'm variable two");
    assertThat(variable.getMimeType()).isNull();
    datasource.dispose();
  }

  @Test
  public void test_reading_variables_get_variables() throws Exception {
    String tableName = "TableVariablesOnly";
    CsvDatasource datasource = new TempTableBuilder(tableName).addVariables(getFileFromResource("Table1/variables.csv"))
        .buildCsvDatasource("csv-datasource");

    assertThat(((AbstractValueTable) datasource.getValueTable(tableName)).getVariables()).hasSize(2);
  }

  @Test
  public void test_repeatable_data_read() {
    CsvDatasource datasource = new CsvDatasource("csv-datasource").addValueTable("Participants", //
        getFileFromResource("org/obiba/magma/datasource/csv/Participants/variables.csv"), //
        getFileFromResource("org/obiba/magma/datasource/csv/Participants/data.csv"));
    datasource.initialise();
    assertThat(datasource.getValueTableNames()).hasSize(1);

    ValueTable table = datasource.getValueTable("Participants");
    assertThat(table).isNotNull();
    assertThat(table.getEntityType()).isEqualTo(DEFAULT_ENTITY_TYPE);

    Variable var = table.getVariable("Admin.Action.actionType");
    assertThat(var.isRepeatable()).isTrue();

    int count = 0;
    for(ValueSet valueSet : table.getValueSets()) {
      count++;
      if(count == 1) {
        String identifier = valueSet.getVariableEntity().getIdentifier();
        Value value = table.getValue(var, valueSet);
        log.info("Admin.Action.actionType[{}]={}", identifier, value);
        assertThat(value.getValueType().getName()).isEqualTo("text");
        assertThat(value.isSequence()).isTrue();
        assertThat(value.asSequence().getSize()).isEqualTo(33);
      }
    }
    assertThat(count).isEqualTo(2);
  }

  @Test
  public void test_multiline_data_read() {
    CsvDatasource datasource = new CsvDatasource("csv-datasource").addValueTable("Participants", //
        getFileFromResource("org/obiba/magma/datasource/csv/Participants/variables.csv"), //
        getFileFromResource("org/obiba/magma/datasource/csv/Participants/data.csv"));
    datasource.setQuote(Quote.DOUBLE);
    datasource.setSeparator(Separator.COMMA);
    datasource.setCharacterSet("UTF-8");
    datasource.setFirstRow(1);
    datasource.initialise();

    assertThat(datasource.getValueTableNames()).hasSize(1);

    ValueTable table = datasource.getValueTable("Participants");
    assertThat(table).isNotNull();
    assertThat(table.getEntityType()).isEqualTo(DEFAULT_ENTITY_TYPE);

    Variable var = table.getVariable("Admin.Action.comment");
    assertThat(var.isRepeatable()).isTrue();

    int count = 0;
    for(ValueSet valueSet : table.getValueSets()) {
      count++;
      String identifier = valueSet.getVariableEntity().getIdentifier();
      Value value = table.getValue(var, valueSet);
      log.info("Admin.Action.comment[{}]={}", identifier, value);
      assertThat(value).isNotNull();
      assertThat(value.getValueType().getName()).isEqualTo("text");
      assertThat(value.isSequence()).isTrue();

      ValueSequence seq = value.asSequence();
      if(count == 1) {
        assertThat(seq.getSize()).isEqualTo(33);
        assertThat(seq.get(22).toString()).isEqualTo("sample collection by Val\ndata entry by Evan");
      } else if(count == 2) {
        assertThat(seq.get(5).toString()).isEqualTo("Unable to draw from left arm due to vein rolling upon puncture. " +
            "Ct refused puncture to right arm. Saliva kit complete");
      }
    }
    assertThat(count).isEqualTo(2);
  }

  @Test
  public void test_charSet() {
    test_charSet("Drugs-iso.csv", "ISO-8859-1");
    test_charSet("Drugs-utf8.csv", "UTF-8");
  }

  public void test_charSet(String filename, String encoding) {
    log.info("Test {}", encoding);

    CsvDatasource datasource = new CsvDatasource("csv-datasource")
        .addValueTable("Drugs", getFileFromResource("org/obiba/magma/datasource/csv/medications/" + filename), "Drug");
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
    assertThat(value.toString()).isEqualTo(expected);
  }

  @SuppressWarnings("ReuseOfLocalVariable")
  @Test
  public void test_escaped_characters() {
    CsvDatasource datasource = new CsvDatasource("csv-datasource").addValueTable("Table1", //
        getFileFromResource("Table1/escaped-variables.csv"), //
        getFileFromResource("Table1/escaped-data.csv"));
    datasource.initialise();

    ValueTable table = datasource.getValueTable("Table1");
    Variable name = table.getVariable("name");
    Variable children = table.getVariable("children");
    assertEmperors(datasource, table, name, children);

    VariableEntity entity1 = new VariableEntityBean(DEFAULT_ENTITY_TYPE, "1");
    Value value = table.getValue(name, table.getValueSet(entity1));
    assertThat(value.isSequence()).isFalse();
    assertThat((String) value.getValue()).isEqualTo("Julius\nCaesar");

    value = table.getValue(children, table.getValueSet(entity1));
    assertThat(value.isSequence()).isTrue();
    ValueSequence valueSequence = value.asSequence();
    assertThat((String) valueSequence.get(0).getValue()).isEqualTo("Julia");
    assertThat((String) valueSequence.get(1).getValue()).isEqualTo("Caesarion");
    assertThat((String) valueSequence.get(2).getValue()).isEqualTo("Gaius\\\\Julius Caesar \"Octavianus\"");
    assertThat(valueSequence.get(3).isNull()).isTrue();
    assertThat((String) valueSequence.get(4).getValue()).isEqualTo("Marcus Junius\" Brutus");

    VariableEntity entity2 = new VariableEntityBean(DEFAULT_ENTITY_TYPE, "2");

    value = table.getValue(name, table.getValueSet(entity2));
    assertThat(value.isSequence()).isFalse();
    assertThat((String) value.getValue()).isEqualTo("Cleopatra\\\\\"VII");

    value = table.getValue(children, table.getValueSet(entity2));
    assertThat(value.isSequence()).isTrue();
    valueSequence = value.asSequence();
    assertThat((String) valueSequence.get(0).getValue()).isEqualTo("Ptolemy XV");
    assertThat(valueSequence.get(1).isNull()).isTrue();
    assertThat((String) valueSequence.get(2).getValue()).isEqualTo("Caesarion");
    assertThat((String) valueSequence.get(3).getValue()).isEqualTo("Alexander Helios");
    assertThat((String) valueSequence.get(4).getValue()).isEqualTo("Cleopatra Selene");
    assertThat((String) valueSequence.get(5).getValue()).isEqualTo("Ptolemy XVI Philadelphus");

    value = table.getValue(name, table.getValueSet(new VariableEntityBean(DEFAULT_ENTITY_TYPE, "3")));
    assertThat(value.isSequence()).isFalse();
    assertThat((String) value.getValue()).isEqualTo("Clau\\dius");

    try {
      table.getValue(name, table.getValueSet(new VariableEntityBean(DEFAULT_ENTITY_TYPE, "4")));
    } catch(NoSuchValueSetException e) {
      fail("Should throw NoSuchValueSetException for Participant 4");
    }
    try {
      table.getValue(name, table.getValueSet(new VariableEntityBean(DEFAULT_ENTITY_TYPE, "5")));
      fail("Should throw NoSuchValueSetException for Participant 5");
    } catch(NoSuchValueSetException e) {
    }
  }

  @SuppressWarnings("ReuseOfLocalVariable")
  @Test
  public void test_end_of_line() throws IOException {

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

    CsvDatasource datasource = new CsvDatasource("csv-datasource")
        .addValueTable("Table1", dataFile, DEFAULT_ENTITY_TYPE);
    datasource.initialise();

    assertThat(datasource.getValueTableNames()).hasSize(1);

    ValueTable table = datasource.getValueTable("Table1");
    assertThat(table).isNotNull();
    assertThat(table.getEntityType()).isEqualTo(DEFAULT_ENTITY_TYPE);

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
    assertThat(variable).isNotNull();
    assertThat(variable.getName()).isEqualTo(name);
    assertThat(variable.getValueType().getName()).isEqualTo("text");
    assertThat(variable.getEntityType()).isEqualTo(DEFAULT_ENTITY_TYPE);
    assertThat(variable.getMimeType()).isNull();
    assertThat(variable.getUnit()).isNull();
    assertThat(variable.getOccurrenceGroup()).isNull();
    assertThat(variable.isRepeatable()).isFalse();
  }

  @SuppressWarnings("ReuseOfLocalVariable")
  private void assertEolValue(ValueTable table, String entityId, String name, String completeName) {
    VariableEntity entity = new VariableEntityBean(DEFAULT_ENTITY_TYPE, entityId);
    Value value = table.getValue(table.getVariable("Name"), table.getValueSet(entity));
    assertThat(value.isSequence()).isFalse();
    assertThat((String) value.getValue()).isEqualTo(name);

    value = table.getValue(table.getVariable("Complete name"), table.getValueSet(entity));
    assertThat(value.isSequence()).isFalse();
    assertThat((String) value.getValue()).isEqualTo(completeName);
  }

  static void assertEmperors(CsvDatasource datasource, ValueTable table, Variable name, Variable children) {
    assertThat(datasource.getValueTableNames()).hasSize(1);

    assertThat(table).isNotNull();
    assertThat(table.getEntityType()).isEqualTo(DEFAULT_ENTITY_TYPE);

    assertThat(name).isNotNull();
    assertThat(name.getValueType().getName()).isEqualTo("text");
    assertThat(name.getEntityType()).isEqualTo(DEFAULT_ENTITY_TYPE);
    assertThat(name.getMimeType()).isNull();
    assertThat(name.getUnit()).isNull();
    assertThat(name.getOccurrenceGroup()).isNull();
    assertThat(name.isRepeatable()).isFalse();

    assertThat(children).isNotNull();
    assertThat(children.getValueType().getName()).isEqualTo("text");
    assertThat(children.getEntityType()).isEqualTo(DEFAULT_ENTITY_TYPE);
    assertThat(children.getMimeType()).isNull();
    assertThat(children.getUnit()).isNull();
    assertThat(children.getOccurrenceGroup()).isNull();
    assertThat(children.isRepeatable()).isTrue();
  }

  @Test
  public void test_OPAL_1811() throws Exception {
    CsvDatasource datasource = new CsvDatasource("csv-datasource").addValueTable("TableDataOnly", //
        null, //
        getFileFromResource("org/obiba/magma/datasource/csv/TableDataOnly/study3.csv"));
    datasource.initialise();
  }

  @Test(expected = DatasourceParsingException.class)
  public void test_fail_getting_reader() {
    CsvDatasource datasource = new CsvDatasource("bozo").addValueTable(new File("tata"));
    datasource.initialise();
  }

  @Test(expected = DatasourceParsingException.class)
  public void test_missing_name_header() {
    File samples = getFileFromResource("org/obiba/magma/datasource/csv/exceptions");
    File variables = new File(samples, "missing-name-variables.csv");

    CsvDatasource ds = new CsvDatasource("variables").addValueTable("variables", variables, (File) null);
    ds.initialise();
  }

  @Test(expected = DatasourceParsingException.class)
  public void test_missing_value_type_header() {
    File samples = getFileFromResource("org/obiba/magma/datasource/csv/exceptions");
    File variables = new File(samples, "missing-valueType-variables.csv");

    CsvDatasource ds = new CsvDatasource("variables").addValueTable("variables", variables, (File) null);
    ds.initialise();
  }

  @Test(expected = DatasourceParsingException.class)
  public void test_missing_entity_type_header() {
    File samples = getFileFromResource("org/obiba/magma/datasource/csv/exceptions");
    File variables = new File(samples, "missing-entityType-variables.csv");

    CsvDatasource ds = new CsvDatasource("variables").addValueTable("variables", variables, (File) null);
    ds.initialise();
  }

  @Test
  public void test_has_no_entities_on_empty_file() {
    File empty = getFileFromResource("empty.csv");
    CsvDatasource datasource = new CsvDatasource("csv-datasource").addValueTable("NoEntities", empty, (File) null);
    datasource.initialise();
    assertThat(datasource.hasEntities(new EntitiesPredicate.NonViewEntitiesPredicate())).isFalse();
  }

  @Test
  public void test_has_no_entities() {
    File samples = getFileFromResource("separators");
    File variables = new File(samples, "variables.csv");
    CsvDatasource datasource = new CsvDatasource("csv-datasource").addValueTable("HasEntities", variables, (File) null);
    datasource.initialise();
    assertThat(datasource.hasEntities(new EntitiesPredicate.NonViewEntitiesPredicate())).isFalse();
  }

  @Test
  public void test_has_entities() {
    CsvDatasource datasource = new CsvDatasource("csv-datasource").addValueTable("Table1", //
        getFileFromResource("Table1/variables.csv"), //
        getFileFromResource("Table1/data.csv"));
    datasource.initialise();
    assertThat(datasource.hasEntities(new EntitiesPredicate.NonViewEntitiesPredicate())).isTrue();
  }
}
