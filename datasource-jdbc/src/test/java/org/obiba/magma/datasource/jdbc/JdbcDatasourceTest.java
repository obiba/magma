package org.obiba.magma.datasource.jdbc;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;
import org.obiba.core.test.spring.Dataset;
import org.obiba.core.test.spring.DbUnitAwareTestExecutionListener;
import org.obiba.magma.ValueSet;
import org.obiba.magma.ValueTable;
import org.obiba.magma.Variable;
import org.obiba.magma.VariableEntity;
import org.obiba.magma.support.VariableEntityBean;
import org.obiba.magma.test.AbstractMagmaTest;
import org.obiba.magma.type.IntegerType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
import org.springframework.test.context.support.DirtiesContextTestExecutionListener;
import org.springframework.test.context.transaction.TransactionalTestExecutionListener;

@org.junit.runner.RunWith(value = SpringJUnit4ClassRunner.class)
@org.springframework.test.context.ContextConfiguration(locations = { "/test-spring-context.xml" })
@org.springframework.test.context.transaction.TransactionConfiguration(transactionManager = "transactionManager")
@org.springframework.test.context.TestExecutionListeners(value = { DependencyInjectionTestExecutionListener.class, DirtiesContextTestExecutionListener.class, TransactionalTestExecutionListener.class, DbUnitAwareTestExecutionListener.class })
public class JdbcDatasourceTest extends AbstractMagmaTest {
  //
  // Instance Variables
  //

  @Autowired
  private JdbcDatasource jdbcDatasource;

  //
  // Fixture Methods
  //

  @Before
  public void initialiseDatasource() {
    jdbcDatasource.initialise();
  }

  //
  // Test Methods
  //

  @Dataset
  @Test
  public void testCreateDatasourceFromExistingDatabase() {
    assertNotNull(jdbcDatasource);
    assertEquals("my-datasource", jdbcDatasource.getName());

    assertEquals(1, jdbcDatasource.getValueTables().size());
    assertTrue(jdbcDatasource.hasValueTable("BONE_DENSITY"));
    ValueTable bdTable = jdbcDatasource.getValueTable("BONE_DENSITY");
    assertEquals("Participant", bdTable.getEntityType());

    // Check variables.
    int variableCount = 0;
    for(Variable variable : bdTable.getVariables()) {
      variableCount++;
    }
    assertEquals(4, variableCount);

    // Check entities and value sets.
    int valueSetCount = 0;
    for(ValueSet valueSet : bdTable.getValueSets()) {
      valueSetCount++;
    }
    assertEquals(2, valueSetCount);
    VariableEntity entity1234_2 = new VariableEntityBean(bdTable.getEntityType(), "1234-2");
    VariableEntity entity1234_3 = new VariableEntityBean(bdTable.getEntityType(), "1234-3");
    assertTrue(bdTable.hasValueSet(entity1234_2));
    assertTrue(bdTable.hasValueSet(entity1234_3));

    // Check variable values.
    ValueSet vs1234_2 = bdTable.getValueSet(entity1234_2);
    ValueSet vs1234_3 = bdTable.getValueSet(entity1234_3);
    assertEquals(IntegerType.get().valueOf(64), bdTable.getValue(bdTable.getVariable("BD"), vs1234_2));
    assertEquals(IntegerType.get().valueOf(65), bdTable.getValue(bdTable.getVariable("BD_2"), vs1234_2));
    assertEquals(IntegerType.get().valueOf(65), bdTable.getValue(bdTable.getVariable("BD"), vs1234_3));
    assertEquals(IntegerType.get().valueOf(65), bdTable.getValue(bdTable.getVariable("BD_2"), vs1234_3));
  }
}
