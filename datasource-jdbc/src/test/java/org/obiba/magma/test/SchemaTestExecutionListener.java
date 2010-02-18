/**
 * 
 */
package org.obiba.magma.test;

import java.lang.reflect.AnnotatedElement;

import javax.sql.DataSource;

import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.simple.SimpleJdbcTemplate;
import org.springframework.test.context.TestContext;
import org.springframework.test.context.TestExecutionListener;
import org.springframework.test.jdbc.SimpleJdbcTestUtils;

public class SchemaTestExecutionListener implements TestExecutionListener {
  //
  // TestExecutionListener Methods
  //

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

  //
  // Methods
  //

  private void handleElement(TestContext testContext, AnnotatedElement element, boolean before) throws Exception {
    TestSchema testSchema = element.getAnnotation(TestSchema.class);
    if(testSchema != null) {
      handleAnnotation(testContext, testSchema, before);
    }
  }

  private void handleAnnotation(TestContext testContext, TestSchema testSchemaAnnotation, boolean before) throws Exception {
    DataSource dataSource = (DataSource) testContext.getApplicationContext().getBean(testSchemaAnnotation.dataSourceBean());
    String sqlScript = before ? testSchemaAnnotation.beforeSchema() : testSchemaAnnotation.afterSchema();
    if(sqlScript.length() != 0) {
      String schemaLocation = testSchemaAnnotation.schemaLocation();
      if(schemaLocation.length() != 0) {
        sqlScript = schemaLocation + "/" + sqlScript;
      }

      SimpleJdbcTestUtils.executeSqlScript(new SimpleJdbcTemplate(dataSource), new ClassPathResource(sqlScript), true);
    }
  }
}