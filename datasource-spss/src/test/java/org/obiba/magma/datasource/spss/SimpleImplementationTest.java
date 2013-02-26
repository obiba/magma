package org.obiba.magma.datasource.spss;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.junit.Test;
import org.obiba.magma.Category;
import org.obiba.magma.ValueTable;
import org.obiba.magma.Variable;
import org.obiba.magma.datasource.spss.support.SpssMagmaEngineTest;


public class SimpleImplementationTest extends SpssMagmaEngineTest {

  private static final String ROOT_FOLDER = ".";

  @Test
  public void testCreation() {

    File file1 = new File(ROOT_FOLDER+"/src/test/resources/org/obiba/magma/datasource/spss/DatabaseTest.sav");
    File file2 = new File(ROOT_FOLDER+"/src/test/resources/org/obiba/magma/datasource/spss/HOP phase1d LifeLines.sav");

    List<File> files = new ArrayList<File>();
    files.add(file1);
    files.add(file2);

    SpssDatasource ds = new SpssDatasource("spss", files);
    ds.initialise();


    ValueTable valueTable = ds.getValueTable("DatabaseTest");
    System.out.println(">>>>> " + valueTable.getName());

    Variable variable = valueTable.getVariable("race");
    System.out.println("N: " + variable.getName() + " T: " + variable.getValueType() +  " CAT: " + variable .getCategories());

    valueTable = ds.getValueTable("HOPphase1dLifeLines");
    System.out.println(">>>>> " + valueTable.getName());

    Set<Category> categories = variable.getCategories();

    for (Category category : categories) {
      System.out.println("CN: " + category.getName() + " CC: " + category.getCode() + " isMissing: " + category
          .isMissing());
    }

    variable = valueTable.getVariable("HEALTH17A1");
    System.out.println("N: " + variable.getName() + " T: " + variable.getValueType() +  " CAT: " + variable .getCategories());

    categories = variable .getCategories();

    for (Category category : categories) {
      System.out.println("CN: " + category.getName() + " CC: " + category.getCode() + " isMissing: " + category
          .isMissing());
    }

  }
}
