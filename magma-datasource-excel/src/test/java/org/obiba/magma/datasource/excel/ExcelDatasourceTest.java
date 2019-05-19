/*
 * Copyright (c) 2019 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.magma.datasource.excel;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.Test;
import org.obiba.core.util.FileUtil;
import org.obiba.magma.Category;
import org.obiba.magma.Datasource;
import org.obiba.magma.Initialisable;
import org.obiba.magma.MagmaRuntimeException;
import org.obiba.magma.ValueTable;
import org.obiba.magma.ValueTableWriter;
import org.obiba.magma.ValueTableWriter.VariableWriter;
import org.obiba.magma.Variable;
import org.obiba.magma.support.DatasourceParsingException;
import org.obiba.magma.support.Disposables;
import org.obiba.magma.test.AbstractMagmaTest;
import org.obiba.magma.type.IntegerType;
import org.obiba.magma.type.TextType;

import com.google.common.base.Charsets;
import com.google.common.io.Files;

import static org.fest.assertions.api.Assertions.assertThat;

@SuppressWarnings({ "OverlyLongMethod", "ReuseOfLocalVariable", "ResultOfMethodCallIgnored", "PMD.NcssMethodCount" })
@edu.umd.cs.findbugs.annotations.SuppressWarnings("RV_RETURN_VALUE_IGNORED_BAD_PRACTICE")
public class ExcelDatasourceTest extends AbstractMagmaTest {

  /**
   * Test: missing columns, default values and user named columns. See:
   * http://wiki.obiba.org/confluence/display/CAG/Excel+Datasource+Improvements
   */
  @Test
  public void test_read() {
    Datasource datasource = new ExcelDatasource("user-defined",
        FileUtil.getFileFromResource("org/obiba/magma/datasource/excel/user-defined.xls"));
    datasource.initialise();

    ValueTable table = datasource.getValueTable("Table1");
    assertThat(table).isNotNull();
    assertThat(table.getEntityType()).isEqualTo("Participant");
    assertThat(table.getVariables()).hasSize(4);
    assertThat(table.getVariableCount()).isEqualTo(4);

    Variable variable = table.getVariable("Var1");
    assertThat(variable.getValueType()).isEqualTo(IntegerType.get());
    assertThat(variable.getEntityType()).isEqualTo("Participant");
    assertThat(variable.getUnit()).isNull();
    assertThat(variable.getMimeType()).isNull();
    assertThat(variable.isRepeatable()).isFalse();
    assertThat(variable.getOccurrenceGroup()).isNull();

    assertThat(variable.getAttributes()).hasSize(1);
    assertThat(variable.getAttributeStringValue("foo")).isEqualTo("bar");

    assertThat(variable.getCategories()).hasSize(2);
    for(Category category : variable.getCategories()) {
      assertThat(category.getCode()).isNull();
      assertThat(category.isMissing()).isFalse();

      if("C1".equals(category.getName())) {
        assertThat(category.getAttributes()).hasSize(1);
        assertThat(category.getAttributeStringValue("toto")).isEqualTo("tata");
      } else {
        assertThat(category.getAttributes()).isEmpty();
      }
    }

    variable = table.getVariable("Var2");
    assertThat(variable.getValueType()).isEqualTo(IntegerType.get());
    assertThat(variable.getAttributes()).isEmpty();
    assertThat(variable.getCategories()).isEmpty();
    variable = table.getVariable("Var3");
    assertThat(variable.getValueType()).isEqualTo(TextType.get());
    variable = table.getVariable("Var4");
    assertThat(variable.getValueType()).isEqualTo(TextType.get());
  }

  @Test
  public void test_read_bogus() {

    Initialisable datasource = new ExcelDatasource("user-defined-bogus",
        FileUtil.getFileFromResource("org/obiba/magma/datasource/excel/user-defined-bogus.xls"));
    try {
      datasource.initialise();
    } catch(DatasourceParsingException dpe) {
      assertThat(dpe.hasChildren()).isTrue();
      List<DatasourceParsingException> errors = dpe.getChildrenAsList();
      assertThat(errors).hasSize(10);
      assertDatasourceParsingException("DuplicateCategoryName", "[Categories, 4, Table1, Var1, C2]", errors.get(0));
      assertDatasourceParsingException("CategoryNameRequired", "[Categories, 5, Table1, Var1]", errors.get(1));
      assertDatasourceParsingException("DuplicateCategoryName", "[Categories, 7, Table1, Var2, C1]", errors.get(2));
      assertDatasourceParsingException("VariableNameRequired", "[Variables, 6, Table1]", errors.get(3));
      assertDatasourceParsingException("DuplicateVariableName", "[Variables, 7, Table1, Var1]", errors.get(4));
      assertDatasourceParsingException("VariableNameCannotContainColon", "[Variables, 8, Table1, Foo:Bar]", errors.get(5));
      assertDatasourceParsingException("UnknownValueType", "[Variables, 9, Table1, Var5, Numerical]", errors.get(6));
      assertDatasourceParsingException("CategoryVariableNameRequired", "[Categories, 9, Table1]", errors.get(7));
      assertDatasourceParsingException("CategoryVariableNameRequired", "[Categories, 10, Table1]", errors.get(8));
      assertDatasourceParsingException("VariableNameRequired", "[Variables, 10, Table2]", errors.get(9));
    }
  }

  @Test
  public void test_read_write_without_table_column() throws IOException {
    Datasource datasource = new ExcelDatasource("user-defined-no-table-column",
        FileUtil.getFileFromResource("org/obiba/magma/datasource/excel/user-defined-no-table-column.xls"));
    datasource.initialise();

    assertThat(datasource.getValueTables()).hasSize(1);
    ValueTable table = datasource.getValueTable(ExcelDatasource.DEFAULT_TABLE_NAME);
    assertThat(table).isNotNull();
    assertThat(table.getVariables()).hasSize(3);
    assertThat(table.getVariableCount()).isEqualTo(3);
    assertThat(table.getVariable("Var1").getCategories()).hasSize(3);

    // test that writing variable & category when some columns are missing does not fail
    Variable testVariable = Variable.Builder.newVariable("test-variable", TextType.get(), "Participant")
        .addCategories("test-category").build();
    writeVariableToDatasource(datasource, ExcelDatasource.DEFAULT_TABLE_NAME, testVariable);
  }

  @Test
  public void test_read_write_without_meta() throws IOException {
    Datasource datasource = new ExcelDatasource("user-defined-no-meta",
        FileUtil.getFileFromResource("org/obiba/magma/datasource/excel/user-defined-no-meta.xls"));
    datasource.initialise();

    assertThat(datasource.getValueTables()).hasSize(0);

    // test that writing variable & category when some columns are missing does not fail
    Variable testVariable = Variable.Builder.newVariable("test-variable", TextType.get(), "Participant")
        .addCategories("test-category").build();
    writeVariableToDatasource(datasource, "Table1", testVariable);
  }

  @Test
  public void test_read_mixed_meta() throws IOException {
    Datasource datasource = new ExcelDatasource("user-defined-mixed-meta",
        FileUtil.getFileFromResource("org/obiba/magma/datasource/excel/user-defined-mixed-meta.xls"));
    datasource.initialise();

    assertThat(datasource.getValueTables()).hasSize(1);
    ValueTable table = datasource.getValueTable("Table1");
    assertThat(table).isNotNull();
    assertThat(table.getVariables()).hasSize(2);
    assertThat(table.getVariableCount()).isEqualTo(2);
    Variable variable = table.getVariable("Var1");
    assertThat(variable.getValueType()).isEqualTo(IntegerType.get());
    assertThat(variable.getCategories()).hasSize(2);
    variable = table.getVariable("Var2");
    assertThat(variable.getValueType()).isEqualTo(IntegerType.get());
    assertThat(variable.getCategories()).isEmpty();
  }

  @Test
  public void test_read_bogus_without_table_column() {
    Initialisable datasource = new ExcelDatasource("user-defined-bogus-no-table-column",
        FileUtil.getFileFromResource("org/obiba/magma/datasource/excel/user-defined-bogus-no-table-column.xls"));
    try {
      datasource.initialise();
    } catch(MagmaRuntimeException e) {
      if(e.getCause() instanceof DatasourceParsingException) {
        DatasourceParsingException dpe = (DatasourceParsingException) e.getCause();
        assertThat(dpe.hasChildren()).isTrue();
        List<DatasourceParsingException> errors = dpe.getChildrenAsList();
        assertThat(errors).hasSize(8);
      }
    }
  }

  @Test
  public void test_write_variable_is_read_back() throws IOException {
    File tmpExcelFile = createTempFile(".xlsx");

    Variable testVariable = Variable.Builder.newVariable("test-variable", TextType.get(), "Participant").build();

    ExcelDatasource datasource = new ExcelDatasource("test", tmpExcelFile);
    datasource.initialise();
    writeVariableToDatasource(datasource, "test-table", testVariable);
    datasource.dispose();

    datasource = new ExcelDatasource("test", tmpExcelFile);
    datasource.initialise();
    assertThat(datasource.getValueTable("test-table")).isNotNull();
    assertThat(datasource.getValueTable("test-table").getVariable("test-variable")).isNotNull();

    Disposables.silentlyDispose(datasource);
    tmpExcelFile.delete();
  }

  @Test
  public void test_write_variable_multiple_times_OPAL_232() throws IOException {
    File tmpExcelFile = createTempFile(".xlsx");

    Variable testVariable = Variable.Builder.newVariable("test-variable", TextType.get(), "Participant").build();

    ExcelDatasource datasource = new ExcelDatasource("test", tmpExcelFile);
    datasource.initialise();
    writeVariableToDatasource(datasource, "test-table", testVariable);
    datasource.dispose();

    datasource = new ExcelDatasource("test", tmpExcelFile);
    datasource.initialise();
    writeVariableToDatasource(datasource, "test-table", testVariable);
    datasource.dispose();

    datasource = new ExcelDatasource("test", tmpExcelFile);
    datasource.initialise();
    ValueTable valueTable = datasource.getValueTable("test-table");
    assertThat(valueTable.getVariables()).hasSize(1);
    assertThat(valueTable.getVariableCount()).isEqualTo(1);

    Disposables.silentlyDispose(datasource);
    tmpExcelFile.delete();
  }

  @Test
  public void test_strings_can_be_written_OPAL_238() throws IOException {
    File tmp = createTempFile(".xlsx");

    Workbook workbook = new XSSFWorkbook();
    Sheet sheet = workbook.createSheet();
    int rowIndex = 0;
    for(String str : readStrings("org/obiba/magma/datasource/excel/opal-238-strings.txt")) {
      sheet.createRow(rowIndex++).createCell(0).setCellValue(str);
    }

    try(FileOutputStream outputStream = new FileOutputStream(tmp)) {
      workbook.write(outputStream);
    }
    try(FileInputStream inputStream = new FileInputStream(tmp)) {
      new XSSFWorkbook(inputStream);
    }

    tmp.delete();
  }

  @Test
  public void test_create_datasource_on_empty_excel_file() {
    Initialisable datasource = new ExcelDatasource("empty",
        FileUtil.getFileFromResource("org/obiba/magma/datasource/excel/empty.xls"));
    datasource.initialise();
  }

  @Test
  public void test_read_long_table_names() {
    ExcelDatasource datasource = new ExcelDatasource("long",
        FileUtil.getFileFromResource("org/obiba/magma/datasource/excel/long-table-names.xlsx"));
    datasource.initialise();

    Set<String> c = datasource.getVariablesCustomAttributeNames();

    assertThat(c.size()).isEqualTo(22);
    assertLongTableNames(datasource);
  }

  @Test
  public void test_write_long_table_names() {
    Datasource datasource = new ExcelDatasource("long",
        FileUtil.getFileFromResource("org/obiba/magma/datasource/excel/long-table-names.xlsx"));
    datasource.initialise();

    File testFile = new File("target/long-table-names.xlsx");
    if(testFile.exists()) testFile.delete();
    ExcelDatasource datasource2 = new ExcelDatasource("long2", testFile);
    datasource2.initialise();

    for(ValueTable table : datasource.getValueTables()) {
      try(ValueTableWriter tableWriter = datasource2.createWriter(table.getName(), table.getEntityType());
          VariableWriter variableWriter = tableWriter.writeVariables()) {
        for(Variable variable : table.getVariables()) {
          variableWriter.writeVariable(variable);
        }
      }
    }

    datasource2.dispose();

    datasource2 = new ExcelDatasource("long2", testFile);
    datasource2.initialise();
    assertLongTableNames(datasource2);
  }

  @Test
  @SuppressWarnings("ConstantConditions")
  public void test_read_empty_rows() {
    Datasource datasource = new ExcelDatasource("empty-rows",
        FileUtil.getFileFromResource("org/obiba/magma/datasource/excel/empty-rows.xls"));
    datasource.initialise();

    ValueTable table = datasource.getValueTable("table1");
    assertThat(table).isNotNull();
    assertThat(table.getEntityType()).isEqualTo("Participant");
    assertThat(table.getVariables()).hasSize(2);
    assertThat(table.getVariableCount()).isEqualTo(2);

    Variable var1 = table.getVariable("var1");
    assertThat(var1.getValueType()).isEqualTo(TextType.get());
    assertThat(var1.getAttributes()).hasSize(1);
    assertThat(var1.getAttribute("label").getLocale()).isEqualTo(Locale.ENGLISH);
    assertThat(var1.getAttribute("label").getValue().toString()).isEqualTo("Variable 1");
    assertThat(var1.getEntityType()).isEqualTo("Participant");
    assertThat(var1.getUnit()).isNull();
    assertThat(var1.getMimeType()).isNull();
    assertThat(var1.isRepeatable()).isFalse();
    assertThat(var1.getOccurrenceGroup()).isNull();
    assertThat(var1.getCategories()).hasSize(2);

    Category cat1 = var1.getCategory("cat1");
    assertThat(cat1).isNotNull();
    assertThat(cat1.getCode()).isNull();
    assertThat(cat1.isMissing()).isFalse();
    assertThat(cat1.getAttributes()).hasSize(1);
    assertThat(cat1.getAttribute("label").getLocale()).isEqualTo(Locale.ENGLISH);
    assertThat(cat1.getAttribute("label").getValue().toString()).isEqualTo("Categorie 1");

    Category cat2 = var1.getCategory("cat2");
    assertThat(cat2).isNotNull();
    assertThat(cat2.getCode()).isNull();
    assertThat(cat2.isMissing()).isFalse();
    assertThat(cat2.getAttributes()).hasSize(1);
    assertThat(cat2.getAttribute("label").getLocale()).isEqualTo(Locale.ENGLISH);
    assertThat(cat2.getAttribute("label").getValue().toString()).isEqualTo("Categorie 2");

    Variable var2 = table.getVariable("var2");
    assertThat(var2.getValueType()).isEqualTo(TextType.get());
    assertThat(var2.getAttributes()).hasSize(1);
    assertThat(var2.getAttribute("label").getLocale()).isEqualTo(Locale.ENGLISH);
    assertThat(var2.getAttribute("label").getValue().toString()).isEqualTo("Variable 2");
    assertThat(var2.getCategories()).isEmpty();
  }

  private void assertLongTableNames(ExcelDatasource datasource) {
    ValueTable table = datasource.getValueTable("ABCDEFGHIJKLMNOPQRSTUVWXYZABCDEF");
    assertThat(table).isNotNull();
    assertThat(table.getEntityType()).isEqualTo("Participant");
    assertThat(table.getVariable("FATHER_COUNTRY_BIRTH_LONG")).isNotNull();
    assertThat(table.getVariable("FATHER_COUNTRY_BIRTH_SHORT")).isNotNull();
    assertThat(table.getVariable("MOTHER_COUNTRY_BIRTH_LONG")).isNotNull();
    assertThat(table.getVariable("MOTHER_COUNTRY_BIRTH_SHORT")).isNotNull();

    table = datasource.getValueTable("ABCDEFGHIJKLMNOPQRSTUVWXYZABCDE");
    assertThat(table).isNotNull();
    assertThat(table.getEntityType()).isEqualTo("Participant");
    assertThat(table.getVariable("GENERIC_132")).isNotNull();
    assertThat(table.getVariable("GENERIC_134")).isNotNull();

    assertThat(datasource.getValueTableNames()).hasSize(2);
  }

  private Iterable<String> readStrings(String filename) throws IOException {
    return Files.readLines(FileUtil.getFileFromResource(filename), Charsets.UTF_8);
  }

  private File createTempFile(String suffix) throws IOException {
    File tmpFile = File.createTempFile("test", suffix);
    tmpFile.delete();
    // tmpFile.deleteOnExit();
    return tmpFile;
  }

  private void writeVariableToDatasource(Datasource datasource, String tableName, Variable testVariable)
      throws IOException {
    try(ValueTableWriter tableWriter = datasource.createWriter(tableName, "Participant");
        VariableWriter variableWriter = tableWriter.writeVariables()) {
      variableWriter.writeVariable(testVariable);
    }
  }

  private void assertDatasourceParsingException(String expectedKey, String expectedParameters,
      DatasourceParsingException dpe) {
    assertThat(dpe.getKey()).isEqualTo(expectedKey);
    assertThat(dpe.getParameters().toString()).isEqualTo(expectedParameters);
  }

}
