package org.obiba.magma.datasource.csv;

import java.io.File;
import java.util.Locale;

import junit.framework.Assert;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.obiba.magma.Category;
import org.obiba.magma.MagmaEngine;
import org.obiba.magma.Value;
import org.obiba.magma.ValueSet;
import org.obiba.magma.ValueTable;
import org.obiba.magma.Variable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
}
