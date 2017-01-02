/*
 * Copyright (c) 2017 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

/**
 *
 */
package org.obiba.magma.test;

import java.lang.reflect.AnnotatedElement;

import javax.sql.DataSource;

import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.TestContext;
import org.springframework.test.context.TestExecutionListener;
import org.springframework.test.jdbc.JdbcTestUtils;

public class SchemaTestExecutionListener implements TestExecutionListener {
  //
  // TestExecutionListener Methods
  //

  @Override
  public void beforeTestClass(TestContext testContext) throws Exception {
  }

  @Override
  public void prepareTestInstance(TestContext testContext) throws Exception {
    handleElement(testContext, testContext.getTestClass(), true);
  }

  @Override
  public void beforeTestMethod(TestContext testContext) throws Exception {
    handleElement(testContext, testContext.getTestMethod(), true);
  }

  @Override
  public void afterTestMethod(TestContext testContext) throws Exception {
    handleElement(testContext, testContext.getTestMethod(), false);
  }

  @Override
  public void afterTestClass(TestContext testContext) throws Exception {
  }

  //
  // Methods
  //

  private void handleElement(TestContext testContext, AnnotatedElement element, boolean before) throws Exception {
    TestSchema testSchema = element.getAnnotation(TestSchema.class);
    if(testSchema != null) {
      handleAnnotation(testContext, testSchema, before);
    }
  }

  private void handleAnnotation(TestContext testContext, TestSchema testSchemaAnnotation, boolean before)
      throws Exception {
    DataSource dataSource = (DataSource) testContext.getApplicationContext()
        .getBean(testSchemaAnnotation.dataSourceBean());
    String sqlScript = before ? testSchemaAnnotation.beforeSchema() : testSchemaAnnotation.afterSchema();
    if(!sqlScript.isEmpty()) {
      String schemaLocation = testSchemaAnnotation.schemaLocation();
      if(!schemaLocation.isEmpty()) {
        sqlScript = schemaLocation + "/" + sqlScript;
      }
      JdbcTestUtils.executeSqlScript(new JdbcTemplate(dataSource), new ClassPathResource(sqlScript), true);
    }
  }
}