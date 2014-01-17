package org.obiba.magma.datasource.excel;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

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
  public void testReadUserDefined() {
    Datasource datasource = new ExcelDatasource("user-defined",
        FileUtil.getFileFromResource("org/obiba/magma/datasource/excel/user-defined.xls"));
    datasource.initialise();

    ValueTable table = datasource.getValueTable("Table1");
    assertThat(table).isNotNull();
    assertThat(table.getEntityType()).isEqualTo("Participant");
    assertThat(table.getVariables()).hasSize(4);

    Variable var = table.getVariable("Var1");
    assertThat(var.getValueType()).isEqualTo(IntegerType.get());
    assertThat(var.getEntityType()).isEqualTo("Participant");
    assertThat(var.getUnit()).isNull();
    assertThat(var.getMimeType()).isNull();
    assertThat(var.isRepeatable()).isFalse();
    assertThat(var.getOccurrenceGroup()).isNull();

    assertThat(var.getAttributes()).hasSize(1);
    assertThat(var.getAttributeStringValue("foo")).isEqualTo("bar");

    assertThat(var.getCategories()).hasSize(2);
    for(Category cat : var.getCategories()) {
      assertThat(cat.getCode()).isNull();
      assertThat(cat.isMissing()).isFalse();

      if("C1".equals(cat.getName())) {
        assertThat(cat.getAttributes()).hasSize(1);
        assertThat(cat.getAttributeStringValue("toto")).isEqualTo("tata");
      } else {
        assertThat(cat.getAttributes()).isEmpty();
      }
    }

    var = table.getVariable("Var2");
    assertThat(var.getValueType()).isEqualTo(IntegerType.get());
    assertThat(var.getAttributes()).isEmpty();
    assertThat(var.getCategories()).isEmpty();
    var = table.getVariable("Var3");
    assertThat(var.getValueType()).isEqualTo(TextType.get());
    var = table.getVariable("Var4");
    assertThat(var.getValueType()).isEqualTo(TextType.get());
  }

  @Test
  public void testReadUserDefinedBogus() {

    Initialisable datasource = new ExcelDatasource("user-defined-bogus",
        FileUtil.getFileFromResource("org/obiba/magma/datasource/excel/user-defined-bogus.xls"));
    try {
      datasource.initialise();
    } catch(DatasourceParsingException dpe) {
      // dpe.printTree();
      // System.out.println("******");
      // dpe.printList();
      assertThat(dpe.hasChildren()).isTrue();
      List<DatasourceParsingException> errors = dpe.getChildrenAsList();
      assertThat(errors).hasSize(15);
      assertDatasourceParsingException("DuplicateCategoryName", "[Categories, 4, Table1, Var1, C2]", errors.get(0));
      assertDatasourceParsingException("CategoryNameRequired", "[Categories, 5, Table1, Var1]", errors.get(1));
      assertDatasourceParsingException("DuplicateCategoryName", "[Categories, 7, Table1, Var2, C1]", errors.get(2));
      assertDatasourceParsingException("VariableNameRequired", "[Variables, 6, Table1]", errors.get(3));
      assertDatasourceParsingException("DuplicateVariableName", "[Variables, 7, Table1, Var1]", errors.get(4));
      assertDatasourceParsingException("VariableNameCannotContainColon", "[Variables, 8, Table1, Foo:Bar]",
          errors.get(5));
      assertDatasourceParsingException("UnknownValueType", "[Variables, 9, Table1, Var5, Numerical]", errors.get(6));
      assertDatasourceParsingException("UnidentifiedVariableName", "[Categories, 8, Table1, VarUnknown]",
          errors.get(7));
      assertDatasourceParsingException("CategoryVariableNameRequired", "[Categories, 9, Table1]", errors.get(8));
      assertDatasourceParsingException("CategoryVariableNameRequired", "[Categories, 10, Table1]", errors.get(9));
      assertDatasourceParsingException("DuplicateColumns", "[Table1, 1, Table1, Var2]", errors.get(10));
      assertDatasourceParsingException("DuplicateColumns", "[Table1, 1, Table1, Var6]", errors.get(11));
      assertDatasourceParsingException("VariableNameCannotContainColon", "[Table1, 1, Table1, Toto:Tata]",
          errors.get(12));
      assertDatasourceParsingException("VariableNameRequired", "[Table1, 1, Table1]", errors.get(13));
      assertDatasourceParsingException("VariableNameRequired", "[Variables, 10, Table2]", errors.get(14));
    }
  }

  @Test
  public void testReadWriteUserDefinedNoTableColumn() throws IOException {
    Datasource datasource = new ExcelDatasource("user-defined-no-table-column",
        FileUtil.getFileFromResource("org/obiba/magma/datasource/excel/user-defined-no-table-column.xls"));
    datasource.initialise();

    assertThat(datasource.getValueTables()).hasSize(1);
    ValueTable table = datasource.getValueTable(ExcelDatasource.DEFAULT_TABLE_NAME);
    assertThat(table).isNotNull();
    assertThat(table.getVariables()).hasSize(3);
    Variable variable = table.getVariable("Var1");
    assertThat(variable.getCategories()).hasSize(3);

    // test that writing variable & category when some columns are missing does not fail
    Variable testVariable = Variable.Builder.newVariable("test-variable", TextType.get(), "Participant")
        .addCategories("test-category").build();
    writeVariableToDatasource(datasource, ExcelDatasource.DEFAULT_TABLE_NAME, testVariable);

  }

  @Test
  public void testReadWriteUserDefinedNoMeta() throws IOException {
    Datasource datasource = new ExcelDatasource("user-defined-no-meta",
        FileUtil.getFileFromResource("org/obiba/magma/datasource/excel/user-defined-no-meta.xls"));
    datasource.initialise();

    assertThat(datasource.getValueTables()).hasSize(1);
    ValueTable table = datasource.getValueTable("Table1");
    assertThat(table).isNotNull();
    assertThat(table.getVariables()).hasSize(2);
    Variable variable = table.getVariable("Var1");
    assertThat(variable.getValueType()).isEqualTo(TextType.get());
    assertThat(variable.getCategories()).isEmpty();
    variable = table.getVariable("Var2");
    assertThat(variable.getValueType()).isEqualTo(TextType.get());
    assertThat(variable.getCategories()).isEmpty();

    // test that writing variable & category when some columns are missing does not fail
    Variable testVariable = Variable.Builder.newVariable("test-variable", TextType.get(), "Participant")
        .addCategories("test-category").build();
    writeVariableToDatasource(datasource, "Table1", testVariable);

    // datasource.dispose();

  }

  @Test
  public void testReadUserDefinedMixedMeta() throws IOException {
    Datasource datasource = new ExcelDatasource("user-defined-mixed-meta",
        FileUtil.getFileFromResource("org/obiba/magma/datasource/excel/user-defined-mixed-meta.xls"));
    datasource.initialise();

    assertThat(datasource.getValueTables()).hasSize(1);
    ValueTable table = datasource.getValueTable("Table1");
    assertThat(table).isNotNull();
    assertThat(table.getVariables()).hasSize(3);
    Variable variable = table.getVariable("Var1");
    assertThat(variable.getValueType()).isEqualTo(IntegerType.get());
    assertThat(variable.getCategories()).hasSize(2);
    variable = table.getVariable("Var2");
    assertThat(variable.getValueType()).isEqualTo(IntegerType.get());
    assertThat(variable.getCategories()).isEmpty();
    variable = table.getVariable("Var3");
    assertThat(variable.getValueType()).isEqualTo(TextType.get());
    assertThat(variable.getCategories()).isEmpty();

  }

  @Test
  public void testReadUserDefinedBogusNoTableColumn() {
    Initialisable datasource = new ExcelDatasource("user-defined-bogus-no-table-column",
        FileUtil.getFileFromResource("org/obiba/magma/datasource/excel/user-defined-bogus-no-table-column.xls"));
    try {
      datasource.initialise();
    } catch(MagmaRuntimeException e) {
      if(e.getCause() instanceof DatasourceParsingException) {
        DatasourceParsingException dpe = (DatasourceParsingException) e.getCause();
        // dpe.printTree();
        // // System.out.println("******");
        // dpe.printList();
        assertThat(dpe.hasChildren()).isTrue();
        List<DatasourceParsingException> errors = dpe.getChildrenAsList();
        assertThat(errors).hasSize(8);
      }
    }
  }

  @Test
  public void testWriteVariableIsReadBack() throws IOException {
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

  // Test for OPAL-232
  @Test
  public void testWriteVariableMultipleTimes() throws IOException {
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
    assertThat(datasource.getValueTable("test-table").getVariables()).hasSize(1);

    Disposables.silentlyDispose(datasource);
    tmpExcelFile.delete();
  }

  @Test
  public void test_OPAL_238_strings_can_be_written() throws IOException {
    File tmp = createTempFile(".xlsx");

    Workbook w = new XSSFWorkbook();
    Sheet s = w.createSheet();
    int i = 0;
    for(String str : readStrings("org/obiba/magma/datasource/excel/opal-238-strings.txt")) {
      s.createRow(i++).createCell(0).setCellValue(str);
    }

    try(FileOutputStream outputStream = new FileOutputStream(tmp)) {
      w.write(outputStream);
    }
    try(FileInputStream inputStream = new FileInputStream(tmp)) {
      new XSSFWorkbook(inputStream);
    }

    tmp.delete();
  }

  @Test
  public void testCreateDatasourceOnEmptyExcelFile() {
    Initialisable datasource = new ExcelDatasource("empty",
        FileUtil.getFileFromResource("org/obiba/magma/datasource/excel/empty.xls"));
    datasource.initialise();
  }

  @Test
  public void testReadLongTableNames() {
    ExcelDatasource datasource = new ExcelDatasource("long",
        FileUtil.getFileFromResource("org/obiba/magma/datasource/excel/long-table-names.xlsx"));
    datasource.initialise();

    assertLongTableNames(datasource);
  }

  @Test
  public void testWriteLongTableNames() {
    Datasource datasource = new ExcelDatasource("long",
        FileUtil.getFileFromResource("org/obiba/magma/datasource/excel/long-table-names.xlsx"));
    datasource.initialise();

    File testFile = new File("target/long-table-names.xlsx");
    if(testFile.exists()) testFile.delete();
    ExcelDatasource datasource2 = new ExcelDatasource("long2", testFile);
    datasource2.initialise();

    for(ValueTable vt : datasource.getValueTables()) {
      ValueTableWriter vtWriter = datasource2.createWriter(vt.getName(), vt.getEntityType());
      VariableWriter vWriter = vtWriter.writeVariables();
      for(Variable v : vt.getVariables()) {
        vWriter.writeVariable(v);
      }
    }

    datasource2.dispose();

    datasource2 = new ExcelDatasource("long2", testFile);
    datasource2.initialise();
    assertLongTableNames(datasource2);
  }

  private void assertLongTableNames(ExcelDatasource datasource) {
    ValueTable vt = datasource.getValueTable("ABCDEFGHIJKLMNOPQRSTUVWXYZABCDEF");
    assertThat(vt).isNotNull();
    assertThat(vt.getEntityType()).isEqualTo("Participant");
    assertThat(vt.getVariable("FATHER_COUNTRY_BIRTH_LONG")).isNotNull();
    assertThat(vt.getVariable("FATHER_COUNTRY_BIRTH_SHORT")).isNotNull();
    assertThat(vt.getVariable("MOTHER_COUNTRY_BIRTH_LONG")).isNotNull();
    assertThat(vt.getVariable("MOTHER_COUNTRY_BIRTH_SHORT")).isNotNull();

    vt = datasource.getValueTable("ABCDEFGHIJKLMNOPQRSTUVWXYZABCDE");
    assertThat(vt).isNotNull();
    assertThat(vt.getEntityType()).isEqualTo("Participant");
    assertThat(vt.getVariable("GENERIC_132")).isNotNull();
    assertThat(vt.getVariable("GENERIC_134")).isNotNull();

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
    ValueTableWriter writer = datasource.createWriter(tableName, "Participant");
    VariableWriter vw = writer.writeVariables();
    vw.writeVariable(testVariable);
    vw.close();
    writer.close();
  }

  private void assertDatasourceParsingException(String expectedKey, String expectedParameters,
      DatasourceParsingException dpe) {
    assertThat(dpe.getKey()).isEqualTo(expectedKey);
    assertThat(dpe.getParameters().toString()).isEqualTo(expectedParameters);
  }

}
