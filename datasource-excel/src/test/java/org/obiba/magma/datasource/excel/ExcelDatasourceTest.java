package org.obiba.magma.datasource.excel;

import java.io.File;
import java.io.IOException;

import junit.framework.Assert;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.obiba.magma.Datasource;
import org.obiba.magma.MagmaEngine;
import org.obiba.magma.ValueTableWriter;
import org.obiba.magma.Variable;
import org.obiba.magma.ValueTableWriter.VariableWriter;
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
