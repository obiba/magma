package org.obiba.magma.datasource.limesurvey;

import javax.sql.DataSource;

import org.apache.commons.dbcp.BasicDataSource;
import org.junit.Ignore;
import org.junit.Test;
import org.obiba.magma.Datasource;
import org.obiba.magma.ValueSet;
import org.obiba.magma.ValueTable;
import org.obiba.magma.test.AbstractMagmaTest;

import static org.fest.assertions.api.Assertions.assertThat;

public class LimesurveyDatasourceMysqlTest extends AbstractMagmaTest {

  /**
   * To run this test you need to: <br>
   * - Import test.dump.sql or clsa.limesurvey.sql <br>
   * - Configure mysql connection in test-spring-context-mysql.xml
   */
  @Test
  @Ignore("cannot run without manual intervention")
  public void testCreateDatasourceFromTestMySqlDatabase() {
    Datasource limesurveyDatasource = new LimesurveyDatasource("lime", createDataSource());
    limesurveyDatasource.initialise();
    ValueTable table = limesurveyDatasource.getValueTable("60 min Questionnaire (Tracking Main Wave & Injury)");
    assertThat(table.getVariableEntities()).hasSize(1);
    assertThat(table.getTimestamps().getLastUpdate().toString()).isEqualTo("2012-07-20T10:40:41.000-0400");
    assertThat(table.getTimestamps().getCreated().toString()).isEqualTo("2012-07-20T10:40:41.000-0400");
//    for (Variable var :table.getVariables()) {
//      System.out.println(var.getName() + ":" + var.getValueType().getName());
//    }
    ValueSet vs = table.getValueSet(table.getVariableEntities().iterator().next());
    assertThat(table.getVariableValueSource("startdate").getValue(vs).toString())
        .isEqualTo("2012-07-20T10:28:35.000-0400");
    assertThat(table.getVariableValueSource("submitdate").getValue(vs).toString())
        .isEqualTo("2012-07-20T10:40:41.000-0400");
    assertThat(table.getVariableValueSource("lastpage").getValue(vs).toString()).isEqualTo("548");
    assertThat(table.getVariableValueSource("startlanguage").getValue(vs).toString()).isEqualTo("en");
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
