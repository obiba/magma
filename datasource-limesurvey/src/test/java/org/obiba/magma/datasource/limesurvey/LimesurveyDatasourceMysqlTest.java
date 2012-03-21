package org.obiba.magma.datasource.limesurvey;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.sql.DataSource;

import org.junit.Test;
import org.obiba.core.test.spring.DbUnitAwareTestExecutionListener;
import org.obiba.magma.Attribute;
import org.obiba.magma.Category;
import org.obiba.magma.Datasource;
import org.obiba.magma.ValueSet;
import org.obiba.magma.ValueTable;
import org.obiba.magma.Variable;
import org.obiba.magma.VariableEntity;
import org.obiba.magma.datasource.limesurvey.LimesurveyValueTable.LimesurveyVariableValueSource;
import org.obiba.magma.support.VariableEntityBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
import org.springframework.test.context.support.DirtiesContextTestExecutionListener;
import org.springframework.test.context.transaction.TransactionalTestExecutionListener;

import test.AbstractMagmaTest;
import test.SchemaTestExecutionListener;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableSortedSet;
import com.google.common.collect.Lists;

@org.junit.runner.RunWith(value = SpringJUnit4ClassRunner.class)
@org.springframework.test.context.ContextConfiguration(locations = { "/test-spring-context-mysql.xml" })
@org.springframework.test.context.transaction.TransactionConfiguration(transactionManager = "transactionManager")
@org.springframework.test.context.TestExecutionListeners(value = { DependencyInjectionTestExecutionListener.class, DirtiesContextTestExecutionListener.class, TransactionalTestExecutionListener.class, SchemaTestExecutionListener.class, DbUnitAwareTestExecutionListener.class })
public class LimesurveyDatasourceMysqlTest extends AbstractMagmaTest {

  @Autowired
  private DataSource datasource;

  @Test
  public void test() {

  }

  // @Test
  public void testCreateDatasourceFromTestMySqlDatabase() {
    LimesurveyDatasource limesurveyDatasource = new LimesurveyDatasource("lime", datasource);
    limesurveyDatasource.initialise();
    display(limesurveyDatasource);
  }

  private void display(Datasource datasource) {
    int nbVariable = 0;
    for(final ValueTable table : datasource.getValueTables()) {

      List<LimesurveyVariableValueSource> variables = Lists.newArrayList(Lists.transform(Lists.newArrayList(table.getVariables()), new Function<Variable, LimesurveyVariableValueSource>() {

        @Override
        public LimesurveyVariableValueSource apply(Variable input) {
          return (LimesurveyVariableValueSource) table.getVariableValueSource(input.getName());
        }
      }));
      Collections.sort(variables, new Comparator<LimesurveyVariableValueSource>() {

        @Override
        public int compare(LimesurveyVariableValueSource o1, LimesurveyVariableValueSource o2) {
          return o1.getVariable().getName().compareTo(o2.getVariable().getName());
        }
      });
      for(LimesurveyVariableValueSource lvv : variables) {
        Variable v = lvv.getVariable();
        System.out.print("Var '" + v.getName() + "' " + v.getValueType().getName() + " ");
        for(Attribute attr : v.getAttributes()) {
          System.out.print(attr.getName() + "=" + attr.getValue() + ", ");
        }
        System.out.println(lvv.getLimesurveyVariableField());
        for(Category c : v.getCategories()) {
          System.out.print("    Cat '" + c.getName() + "' ");
          for(Attribute attr : c.getAttributes()) {
            System.out.print(" " + attr.getName() + "=" + attr.getValue() + ", ");
          }
          System.out.println();
        }

        VariableEntity entity = new VariableEntityBean(table.getEntityType(), "1234");
        VariableEntity entity2 = new VariableEntityBean(table.getEntityType(), "5678");
        ValueSet valueSet = table.getValueSet(entity);
        ImmutableSortedSet<VariableEntity> sortedSet = ImmutableSortedSet.of(entity, entity2);
        // Iterable<Value> values = lvv.getValues(sortedSet);

        System.out.println(lvv.getValue(valueSet));
        // for(Value value : values) {
        // System.out.println(value);
        // }
      }
      nbVariable += variables.size();
    }
    System.out.println(nbVariable);
    System.out.println(datasource.getValueTables().size());
  }
}
