package org.obiba.magma.datasource.limesurvey;

import javax.sql.DataSource;

import org.apache.commons.dbcp.BasicDataSource;
import org.junit.Ignore;
import org.junit.Test;
import org.obiba.magma.support.DatasourceParsingException;
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
    try {
      LimesurveyDatasource limesurveyDatasource = new LimesurveyDatasource("lime", createDataSource());
      limesurveyDatasource.initialise();
      DisplayHelper.display(limesurveyDatasource);
    } catch(DatasourceParsingException e) {
      e.printList();
    }
  }

  public DataSource createDataSource() {
    BasicDataSource bds = new BasicDataSource();
    bds.setUrl("jdbc:mysql://localhost:3306/limesurvey");
    bds.setUsername("limesurvey");
    bds.setPassword("limesurvey");
    return bds;
  }

}
