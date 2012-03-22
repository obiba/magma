package org.obiba.magma.datasource.limesurvey;

import javax.sql.DataSource;

import org.apache.commons.dbcp.BasicDataSource;
import org.junit.Ignore;
import org.junit.Test;

import test.AbstractMagmaTest;

public class LimesurveyDatasourceMysqlTest extends AbstractMagmaTest {

  /**
   * To run this test you need to: <br>
   * - Import test.dump.sql or clsa.limesurvey.sql <br>
   * - Configure mysql connection in test-spring-context-mysql.xml
   */
  @Test
  @Ignore("cannot run without manual intervention")
  public void testCreateDatasourceFromTestMySqlDatabase() {
    LimesurveyDatasource limesurveyDatasource = new LimesurveyDatasource("lime", createDataSource());
    limesurveyDatasource.initialise();
    DisplayHelper.display(limesurveyDatasource);
  }

  public DataSource createDataSource() {
    BasicDataSource bds = new BasicDataSource();
    bds.setUrl("jdbc:mysql://localhost:3306/limesurvey");
    bds.setUsername("limesurvey");
    bds.setPassword("limersurvey");
    return bds;
  }

}
