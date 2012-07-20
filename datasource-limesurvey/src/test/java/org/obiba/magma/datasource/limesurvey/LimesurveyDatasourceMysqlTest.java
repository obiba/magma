package org.obiba.magma.datasource.limesurvey;

import javax.sql.DataSource;

import junit.framework.Assert;

import org.apache.commons.dbcp.BasicDataSource;
import org.junit.Ignore;
import org.junit.Test;
import org.obiba.magma.ValueTable;

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
    ValueTable table = limesurveyDatasource.getValueTable("60 min Questionnaire (Tracking Main Wave & Injury)");
    Assert.assertEquals(1, table.getVariableEntities().size());
    Assert.assertEquals("2012-07-20T10:40:41.000-0400", table.getTimestamps().getLastUpdate().toString());
    Assert.assertEquals("2012-07-20T10:40:41.000-0400", table.getTimestamps().getCreated().toString());
    //DisplayHelper.display(limesurveyDatasource);
  }

  public DataSource createDataSource() {
    BasicDataSource bds = new BasicDataSource();
    bds.setUrl("jdbc:mysql://localhost:3306/limesurvey");
    bds.setUsername("root");
    bds.setPassword("rootadmin");
    return bds;
  }

}
