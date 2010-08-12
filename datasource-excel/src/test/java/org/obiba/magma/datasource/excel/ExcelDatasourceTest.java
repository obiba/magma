package org.obiba.magma.datasource.excel;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import junit.framework.Assert;

import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.obiba.magma.Category;
import org.obiba.magma.Datasource;
import org.obiba.magma.MagmaEngine;
import org.obiba.magma.MagmaRuntimeException;
import org.obiba.magma.ValueTable;
import org.obiba.magma.ValueTableWriter;
import org.obiba.magma.Variable;
import org.obiba.magma.ValueTableWriter.VariableWriter;
import org.obiba.magma.support.DatasourceParsingException;
import org.obiba.magma.type.IntegerType;
import org.obiba.magma.type.TextType;

import com.google.common.collect.Iterables;

public class ExcelDatasourceTest {

  @Before
  public void before() {
    new MagmaEngine();
  }

  @After
  public void after() {
    MagmaEngine.get().shutdown();
  }

  /**
   * Test: missing columns, default values and user named columns. See:
   * http://wiki.obiba.org/confluence/display/CAG/Excel+Datasource+Improvements
   */
  @Test
  public void testReadUserDefined() {
    ExcelDatasource datasource = new ExcelDatasource("user-defined", new File("src/test/resources/org/obiba/magma/datasource/excel/user-defined.xls"));
    datasource.initialise();

    ValueTable table = datasource.getValueTable("Table1");
    Assert.assertNotNull(table);
    Assert.assertEquals("Participant", table.getEntityType());

    Variable var = table.getVariable("Var1");
    Assert.assertEquals(IntegerType.get(), var.getValueType());
    Assert.assertEquals("Participant", var.getEntityType());
    Assert.assertNull(var.getUnit());
    Assert.assertNull(var.getMimeType());
    Assert.assertFalse(var.isRepeatable());
    Assert.assertNull(var.getOccurrenceGroup());

    Assert.assertEquals(1, var.getAttributes().size());
    Assert.assertEquals("bar", var.getAttributeStringValue("foo"));

    Assert.assertEquals(2, var.getCategories().size());
    for(Category cat : var.getCategories()) {
      Assert.assertNull(cat.getCode());
      Assert.assertFalse(cat.isMissing());

      if(cat.getName().equals("C1")) {
        Assert.assertEquals(1, cat.getAttributes().size());
        Assert.assertEquals("tata", cat.getAttributeStringValue("toto"));
      } else {
        Assert.assertEquals(0, cat.getAttributes().size());
      }
    }

    var = table.getVariable("Var2");
    Assert.assertEquals(IntegerType.get(), var.getValueType());
    Assert.assertEquals(0, var.getAttributes().size());
    Assert.assertEquals(0, var.getCategories().size());
    var = table.getVariable("Var3");
    Assert.assertEquals(TextType.get(), var.getValueType());
    var = table.getVariable("Var4");
    Assert.assertEquals(TextType.get(), var.getValueType());
  }

  @Test
  public void testReadUserDefinedBogus1() {

    ExcelDatasource datasource = new ExcelDatasource("user-defined", new File("src/test/resources/org/obiba/magma/datasource/excel/user-defined-bogus1.xls"));
    try {
      datasource.initialise();
    } catch(MagmaRuntimeException e) {
      if(e.getCause() instanceof DatasourceParsingException) {
        DatasourceParsingException dpe = (DatasourceParsingException) e.getCause();
        // dpe.printTree();
        // System.out.println("******");
        dpe.printList();
        Assert.assertTrue(dpe.hasChildren());
        List<DatasourceParsingException> errors = dpe.getChildrenAsList();
        Assert.assertEquals(10, errors.size());
        assertDatasourceParsingException("DuplicateCategoryName", "[Categories, 4, Table1, Var1, C2]", errors.get(0));
        assertDatasourceParsingException("CategoryNameRequired", "[Categories, 5, Table1, Var1]", errors.get(1));
        assertDatasourceParsingException("DuplicateCategoryName", "[Categories, 7, Table1, Var2, C1]", errors.get(2));
        assertDatasourceParsingException("VariableNameRequired", "[Variables, 6, Table1]", errors.get(3));
        assertDatasourceParsingException("DuplicateVariableName", "[Variables, 7, Table1, Var1]", errors.get(4));
        assertDatasourceParsingException("VariableNameCannotContainColon", "[Variables, 8, Table1, Foo:Bar]", errors.get(5));
        assertDatasourceParsingException("UnknownValueType", "[Variables, 9, Table1, Var5, Numerical]", errors.get(6));
        assertDatasourceParsingException("UnidentifiedVariableName", "[Categories, 8, Table1, VarUnknown]", errors.get(7));
        assertDatasourceParsingException("CategoryVariableNameRequired", "[Categories, 9, Table1]", errors.get(8));
        assertDatasourceParsingException("CategoryVariableNameRequired", "[Categories, 10, Table1]", errors.get(9));
      } else {
        throw e;
      }
    }
  }

  private void assertDatasourceParsingException(String expectedKey, String expectedParameters, DatasourceParsingException dpe) {
    Assert.assertEquals(expectedKey, dpe.getKey());
    Assert.assertEquals(expectedParameters, dpe.getParameters().toString());
  }

  @Test
  public void testWriteVariableIsReadBack() throws IOException {
    File tmpExcelFile = createTempFile(".xlsx");

    Variable testVariable = Variable.Builder.newVariable("test-variable", TextType.get(), "entityType").build();

    ExcelDatasource datasource = new ExcelDatasource("test", tmpExcelFile);
    datasource.initialise();
    writeVariableToDatasource(datasource, "test-table", testVariable);
    datasource.dispose();

    datasource = new ExcelDatasource("test", tmpExcelFile);
    datasource.initialise();
    Assert.assertNotNull(datasource.getValueTable("test-table"));
    Assert.assertNotNull(datasource.getValueTable("test-table").getVariable("test-variable"));
  }

  // Test for OPAL-232
  @Test
  public void testWriteVariableMultipleTimes() throws IOException {
    File tmpExcelFile = createTempFile(".xlsx");

    Variable testVariable = Variable.Builder.newVariable("test-variable", TextType.get(), "entityType").build();

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
    Assert.assertEquals(1, Iterables.size(datasource.getValueTable("test-table").getVariables()));
  }

  @Test
  public void testOPAL_238() throws IOException {
    File tmp = createTempFile(".xlsx");

    Workbook w = new XSSFWorkbook();
    Sheet s = w.createSheet();
    int i = 0;
    for(String str : readStrings("src/test/resources/org/obiba/magma/datasource/excel/opal-238-strings.txt")) {
      s.createRow(i++).createCell(0).setCellValue(str);
    }

    Throwable t = null;
    try {
      w.write(new FileOutputStream(tmp));
      w = new XSSFWorkbook(new FileInputStream(tmp));
      Assert.fail("If this test fails, it may mean that OPAL-238 is fixed. If so, reverse the assertions of this test.");
    } catch(RuntimeException e) {
      t = e;
    } catch(AssertionError e) {
      t = e;
    }
    Assert.assertNotNull("Due to a bug in POI, we expect to get an exception or an AssertionError (depends whether assertions are enabled).", t);
  }

  @Test
  public void testCreateDatasourceOnEmptyExcelFile() {
    ExcelDatasource datasource = new ExcelDatasource("empty", new File("src/test/resources/org/obiba/magma/datasource/excel/empty.xls"));
    datasource.initialise();
  }

  @Test
  public void testReadLongTableNames() {
    ExcelDatasource datasource = new ExcelDatasource("long", new File("src/test/resources/org/obiba/magma/datasource/excel/long-table-names.xlsx"));
    datasource.initialise();

    assertLongTableNames(datasource);
  }

  @Test
  public void testWriteLongTableNames() {
    ExcelDatasource datasource = new ExcelDatasource("long", new File("src/test/resources/org/obiba/magma/datasource/excel/long-table-names.xlsx"));
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
    Assert.assertNotNull(vt);
    Assert.assertEquals("Participant", vt.getEntityType());
    Assert.assertNotNull(vt.getVariable("FATHER_COUNTRY_BIRTH_LONG"));
    Assert.assertNotNull(vt.getVariable("FATHER_COUNTRY_BIRTH_SHORT"));
    Assert.assertNotNull(vt.getVariable("MOTHER_COUNTRY_BIRTH_LONG"));
    Assert.assertNotNull(vt.getVariable("MOTHER_COUNTRY_BIRTH_SHORT"));

    vt = datasource.getValueTable("ABCDEFGHIJKLMNOPQRSTUVWXYZABCDE");
    Assert.assertNotNull(vt);
    Assert.assertEquals("Participant", vt.getEntityType());
    Assert.assertNotNull(vt.getVariable("GENERIC_132"));
    Assert.assertNotNull(vt.getVariable("GENERIC_134"));

    Assert.assertEquals(2, datasource.getValueTableNames().size());
  }

  private List<String> readStrings(String filename) throws IOException {
    List<String> strs = new ArrayList<String>();
    BufferedReader bis = new BufferedReader(new FileReader(new File(filename)));
    String s;
    while((s = bis.readLine()) != null) {
      if(s.trim().length() > 0) strs.add(s.trim());
    }
    return strs;
  }

  private File createTempFile(String suffix) throws IOException {
    File tmpFile = File.createTempFile("test", suffix);
    tmpFile.delete();
    // tmpFile.deleteOnExit();
    return tmpFile;
  }

  private void writeVariableToDatasource(Datasource datasource, String tableName, Variable testVariable) throws IOException {
    ValueTableWriter writer = datasource.createWriter("test-table", "entityType");
    VariableWriter vw = writer.writeVariables();
    vw.writeVariable(testVariable);
    vw.close();
    writer.close();
  }
}
