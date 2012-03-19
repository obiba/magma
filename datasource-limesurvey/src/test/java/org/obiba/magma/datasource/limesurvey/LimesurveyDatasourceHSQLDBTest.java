package org.obiba.magma.datasource.limesurvey;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.sql.DataSource;

import org.junit.Assert;
import org.junit.Test;
import org.obiba.core.test.spring.DbUnitAwareTestExecutionListener;
import org.obiba.magma.Attribute;
import org.obiba.magma.Category;
import org.obiba.magma.Datasource;
import org.obiba.magma.ValueTable;
import org.obiba.magma.Variable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
import org.springframework.test.context.support.DirtiesContextTestExecutionListener;
import org.springframework.test.context.transaction.TransactionalTestExecutionListener;

import test.AbstractMagmaTest;
import test.SchemaTestExecutionListener;
import test.TestSchema;

import com.google.common.collect.Lists;

@org.junit.runner.RunWith(value = SpringJUnit4ClassRunner.class)
@org.springframework.test.context.ContextConfiguration(locations = { "/test-spring-context-hsqldb.xml" })
@org.springframework.test.context.transaction.TransactionConfiguration(transactionManager = "transactionManager")
@org.springframework.test.context.TestExecutionListeners(value = { DependencyInjectionTestExecutionListener.class, DirtiesContextTestExecutionListener.class, TransactionalTestExecutionListener.class, SchemaTestExecutionListener.class, DbUnitAwareTestExecutionListener.class })
public class LimesurveyDatasourceHSQLDBTest extends AbstractMagmaTest {

  @Autowired
  private DataSource datasource;

  @TestSchema(schemaLocation = "org/obiba/magma/datasource/limesurvey/clsa", beforeSchema = "schema-nometa.sql", afterSchema = "schema-notables.sql")
  // @Test
  public void testCreateDatasourceFromCLSADatabase() {
    LimesurveyDatasource limesurveyDatasource = new LimesurveyDatasource("lime", datasource);
    limesurveyDatasource.initialise();
    Assert.assertEquals(8, limesurveyDatasource.getValueTableNames().size());
    display(limesurveyDatasource);
  }

  @TestSchema(schemaLocation = "org/obiba/magma/datasource/limesurvey/test", beforeSchema = "schema-nometa.sql", afterSchema = "schema-notables.sql")
  @Test
  public void testCreateDatasourceFromTestDatabase() {
    LimesurveyDatasource limesurveyDatasource = new LimesurveyDatasource("lime", datasource, "test_");
    limesurveyDatasource.initialise();
    // display(limesurveyDatasource);
  }

  private void display(Datasource datasource) {
    int nbVariable = 0;
    for(ValueTable table : datasource.getValueTables()) {
      List<Variable> variables = Lists.newArrayList(table.getVariables());
      Collections.sort(variables, new Comparator<Variable>() {

        @Override
        public int compare(Variable o1, Variable o2) {
          return o1.getName().compareTo(o2.getName());
        }
      });
      for(Variable v : variables) {
        System.out.print("Var '" + v.getName() + "' " + v.getValueType().getName() + " ");
        for(Attribute attr : v.getAttributes()) {
          System.out.print(attr.getName() + "=" + attr.getValue() + ", ");
        }
        System.out.println();
        for(Category c : v.getCategories()) {
          System.out.print("    Cat '" + c.getName() + "' ");
          for(Attribute attr : c.getAttributes()) {
            System.out.print(" " + attr.getName() + "=" + attr.getValue() + ", ");
          }
          System.out.println();
        }
      }
      System.out.println(variables.size());
      nbVariable += variables.size();
    }
    System.out.println(nbVariable);
  }

}
