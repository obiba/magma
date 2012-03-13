package org.obiba.magma.datasource.limesurvey;

import javax.sql.DataSource;

import org.junit.Assert;
import org.junit.Test;
import org.obiba.core.test.spring.DbUnitAwareTestExecutionListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
import org.springframework.test.context.support.DirtiesContextTestExecutionListener;
import org.springframework.test.context.transaction.TransactionalTestExecutionListener;

import test.AbstractMagmaTest;
import test.SchemaTestExecutionListener;
import test.TestSchema;

@org.junit.runner.RunWith(value = SpringJUnit4ClassRunner.class)
@org.springframework.test.context.ContextConfiguration(locations = { "/test-spring-context.xml" })
@org.springframework.test.context.transaction.TransactionConfiguration(transactionManager = "transactionManager")
@org.springframework.test.context.TestExecutionListeners(value = { DependencyInjectionTestExecutionListener.class, DirtiesContextTestExecutionListener.class, TransactionalTestExecutionListener.class, SchemaTestExecutionListener.class, DbUnitAwareTestExecutionListener.class })
public class LimesurveyDatasourceTest extends AbstractMagmaTest {

  @Autowired
  private DataSource datasource;

  @TestSchema(schemaLocation = "org/obiba/magma/datasource/limesurvey", beforeSchema = "schema-nometa.sql", afterSchema = "schema-notables.sql")
  @Test
  public void testCreateDatasourceFromExistingDatabase() {
    LimesurveyDatasource limesurveyDatasource = new LimesurveyDatasource("lime", datasource);
    limesurveyDatasource.initialise();
    Assert.assertEquals(6, limesurveyDatasource.getValueTableNames().size());
  }

}
