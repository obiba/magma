package org.obiba.magma.datasource.limesurvey;

import javax.sql.DataSource;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import test.AbstractMagmaTest;

@org.junit.runner.RunWith(value = SpringJUnit4ClassRunner.class)
@org.springframework.test.context.ContextConfiguration(locations = { "/test-spring-context-mysql.xml" })
public class LimesurveyDatasourceMysqlTest extends AbstractMagmaTest {

  @Autowired
  private DataSource datasource;

  // use to avoid "No runnable methods" exception
  @Test
  public void test() {

  }

  /**
   * To run this test you need to: <br>
   * - Import test.dump.sql or clsa.limesurvey.sql <br>
   * - Configure mysql connection in test-spring-context-mysql.xml
   */
  // @Test
  public void testCreateDatasourceFromTestMySqlDatabase() {
    LimesurveyDatasource limesurveyDatasource = new LimesurveyDatasource("lime", datasource);
    limesurveyDatasource.initialise();
    DisplayHelper.display(limesurveyDatasource);
  }

}
