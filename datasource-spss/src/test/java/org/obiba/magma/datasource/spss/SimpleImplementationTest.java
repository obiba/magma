package org.obiba.magma.datasource.spss;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.junit.Test;
import org.obiba.magma.Category;
import org.obiba.magma.ValueTable;
import org.obiba.magma.Variable;
import org.obiba.magma.datasource.spss.support.SpssDatasourceFactory;
import org.obiba.magma.datasource.spss.support.SpssMagmaEngineTest;
import org.opendatafoundation.data.spss.SPSSFile;
import org.opendatafoundation.data.spss.SPSSFileException;
import org.opendatafoundation.data.spss.SPSSVariable;

public class SimpleImplementationTest extends SpssMagmaEngineTest {

  private static final String ROOT_FOLDER = ".";

  @Test
  public void testCreation() {

    File file1 = new File(ROOT_FOLDER+"/src/test/resources/org/obiba/magma/datasource/spss/DatabaseTest.sav");
    File file2 = new File(ROOT_FOLDER+"/src/test/resources/org/obiba/magma/datasource/spss/HOP phase1d LifeLines.sav");

    List<File> files = new ArrayList<File>();
    files.add(file1);
    files.add(file2);

    SpssDatasourceFactory factory = new SpssDatasourceFactory();
    factory.setName(SpssDatasourceFactory.DEFAULT_DATASOURCE_NAME);
    factory.addFile(file1);
    factory.addFile(file2);

    SpssDatasource ds = (SpssDatasource)factory.create();
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

  @Test
  public void testEntityDataLoading() {
//    File file = new File(ROOT_FOLDER+"/src/test/resources/org/obiba/magma/datasource/spss/DatabaseTest.sav");
//    File file = new File(ROOT_FOLDER+"/src/test/resources/org/obiba/magma/datasource/spss/HOP phase1d LifeLines
// .sav");
    File file = new File(ROOT_FOLDER+"/src/test/resources/org/obiba/magma/datasource/spss/dictionnaire_variablesT1.sav");

    try {
      SPSSFile spssFile = new SPSSFile(file);
      spssFile.logFlag = false;
      spssFile.loadMetadata();
      spssFile.loadData();

      // first column is used for variable entities
      SPSSVariable entity = spssFile.getVariable(0);
      System.out.println("Variable entity column: " + entity.getName());

      // Uncomment only for testing
//      for(int i = 1; i <= entity.getNumberOfObservation(); i++) {
//        System.out.println("Value: " + entity.getValueAsString(i, new FileFormatInfo(FileFormatInfo.Format.ASCII)));
//      }

    } catch(FileNotFoundException e) {
      e.printStackTrace();
    } catch(SPSSFileException e) {
      e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
    } catch(IOException e) {
      e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
    }

  }

}
