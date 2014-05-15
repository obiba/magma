package org.obiba.magma.datasource.jdbc;

import java.util.TreeSet;

import javax.sql.DataSource;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.obiba.core.test.spring.Dataset;
import org.obiba.core.test.spring.DbUnitAwareTestExecutionListener;
import org.obiba.magma.Category;
import org.obiba.magma.Timestamps;
import org.obiba.magma.Value;
import org.obiba.magma.ValueSet;
import org.obiba.magma.ValueTable;
import org.obiba.magma.ValueTableWriter;
import org.obiba.magma.ValueTableWriter.ValueSetWriter;
import org.obiba.magma.ValueTableWriter.VariableWriter;
import org.obiba.magma.Variable;
import org.obiba.magma.VariableEntity;
import org.obiba.magma.VectorSource;
import org.obiba.magma.support.VariableEntityBean;
import org.obiba.magma.test.AbstractMagmaTest;
import org.obiba.magma.test.SchemaTestExecutionListener;
import org.obiba.magma.test.TestSchema;
import org.obiba.magma.type.DecimalType;
import org.obiba.magma.type.IntegerType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
import org.springframework.test.context.support.DirtiesContextTestExecutionListener;
import org.springframework.test.context.transaction.TransactionConfiguration;
import org.springframework.test.context.transaction.TransactionalTestExecutionListener;

import static org.fest.assertions.api.Assertions.assertThat;

@SuppressWarnings({ "ReuseOfLocalVariable", "OverlyLongMethod", "PMD.NcssMethodCount" })
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("/test-spring-context.xml")
@TransactionConfiguration
@TestExecutionListeners({ DependencyInjectionTestExecutionListener.class, DirtiesContextTestExecutionListener.class,
    TransactionalTestExecutionListener.class, SchemaTestExecutionListener.class,
    DbUnitAwareTestExecutionListener.class })
public class JdbcDatasourceTest extends AbstractMagmaTest {

  @Autowired
  private DataSource dataSource;

  @TestSchema(schemaLocation = "org/obiba/magma/datasource/jdbc", beforeSchema = "schema-nometa.sql",
      afterSchema = "schema-notables.sql")
  @Dataset(filenames = "JdbcDatasourceTest-nometa.xml")
  @Test
  public void testCreateDatasourceFromExistingDatabase() {
    JdbcDatasource jdbcDatasource = new JdbcDatasource("my-datasource", dataSource, "Participant", false);
    jdbcDatasource.initialise();

    testCreateDatasourceFromExistingDatabase(jdbcDatasource);

    jdbcDatasource.dispose();
  }

  @TestSchema(schemaLocation = "org/obiba/magma/datasource/jdbc", beforeSchema = "schema-meta.sql",
      afterSchema = "schema-notables.sql")
  @Dataset(filenames = "JdbcDatasourceTest.xml")
  @Test
  public void testCreateDatasourceFromExistingDatabaseUsingMetadataTables() {
    JdbcDatasource jdbcDatasource = new JdbcDatasource("my-datasource", dataSource, "Participant", true);
    jdbcDatasource.initialise();

    testCreateDatasourceFromExistingDatabase(jdbcDatasource);

    ValueTable valueTable = jdbcDatasource.getValueTable("BONE_DENSITY");
    Variable bdVar = valueTable.getVariable("BD");
    Variable bdVar2 = valueTable.getVariable("BD_2");

    // Verify variable attributes.
    assertThat(bdVar.hasAttribute("description")).isTrue();
    assertThat(bdVar.getAttributeValue("description").toString()).isEqualTo("BD description");
    assertThat(bdVar2.hasAttribute("description")).isTrue();
    assertThat(bdVar2.getAttributeValue("description").toString()).isEqualTo("BD_2 description");

    // Verify categories.
    assertThat(hasCategory(bdVar, "PNA", "88")).isTrue();
    assertThat(hasCategory(bdVar, "DNK", "99")).isTrue();

    jdbcDatasource.dispose();
  }

  @TestSchema(schemaLocation = "org/obiba/magma/datasource/jdbc", beforeSchema = "schema-nometa.sql",
      afterSchema = "schema-notables.sql")
  @Dataset(filenames = "JdbcDatasourceTest-nometa.xml")
  @Test
  public void test_vectorSource() {
    JdbcDatasource jdbcDatasource = new JdbcDatasource("my-datasource", dataSource, "Participant", false);
    jdbcDatasource.initialise();

    testCreateDatasourceFromExistingDatabase(jdbcDatasource);

    ValueTable valueTable = jdbcDatasource.getValueTable("BONE_DENSITY");
    VectorSource bdVar = valueTable.getVariableValueSource("BD").asVectorSource();
    VectorSource bdVar2 = valueTable.getVariableValueSource("BD_2").asVectorSource();

    // Verify variable attributes.
    assertThat(bdVar).isNotNull();
    assertThat(bdVar2).isNotNull();

    Iterable<Value> values = bdVar.getValues(new TreeSet<>(valueTable.getVariableEntities()));

    assertThat(values).hasSize(2);

    values = bdVar2.getValues(new TreeSet<>(valueTable.getVariableEntities()));
    assertThat(values).hasSize(2);

    jdbcDatasource.dispose();
  }

  @TestSchema(schemaLocation = "org/obiba/magma/datasource/jdbc", beforeSchema = "schema-notables.sql",
      afterSchema = "schema-notables.sql")
  @Test
  public void testCreateDatasourceFromScratch() { // i.e., no existing database
    JdbcDatasource jdbcDatasource = new JdbcDatasource("my-datasource-nodb", dataSource, "Participant", false);
    jdbcDatasource.initialise();

    testCreateDatasourceFromScratch(jdbcDatasource);

    jdbcDatasource.dispose();
  }

  @TestSchema(schemaLocation = "org/obiba/magma/datasource/jdbc", beforeSchema = "schema-notables.sql",
      afterSchema = "schema-notables.sql")
  @Test
  public void testCreateDatasourceFromScratchUsingMetadataTables() {
    JdbcDatasourceSettings settings = new JdbcDatasourceSettings("Participant", null, null, true);
    JdbcDatasource jdbcDatasource = new JdbcDatasource("my-datasource-nodb", dataSource, settings);
    jdbcDatasource.initialise();

    testCreateDatasourceFromScratch(jdbcDatasource);

    jdbcDatasource.dispose();
  }

  @TestSchema(schemaLocation = "org/obiba/magma/datasource/jdbc", beforeSchema = "schema-notables.sql",
      afterSchema = "schema-notables.sql")
  @Test
  public void test_Timestamped() {
    JdbcDatasourceSettings settings = new JdbcDatasourceSettings("Participant", null, null, false);
    settings.setDefaultCreatedTimestampColumnName("created");
    settings.setDefaultUpdatedTimestampColumnName("updated");

    JdbcDatasource jdbcDatasource = new JdbcDatasource("my-datasource-nodb", dataSource, settings);
    jdbcDatasource.initialise();

    testCreateDatasourceFromScratch(jdbcDatasource);

    ValueTable t = jdbcDatasource.getValueTables().iterator().next();
    Timestamps ts = t.getTimestamps();

    assertThat(ts).isNotNull();
    assertThat(ts.getCreated()).isNotNull();
    assertThat(ts.getCreated().isNull()).isFalse();
    assertThat(ts.getLastUpdate()).isNotNull();
    assertThat(ts.getLastUpdate().isNull()).isFalse();

    jdbcDatasource.dispose();
  }

  //
  // Methods
  //

  private void testCreateDatasourceFromExistingDatabase(JdbcDatasource jdbcDatasource) {
    assertThat(jdbcDatasource).isNotNull();
    assertThat(jdbcDatasource.getName()).isEqualTo("my-datasource");

    assertThat(jdbcDatasource.getValueTables()).hasSize(1);
    assertThat(jdbcDatasource.hasValueTable("BONE_DENSITY")).isTrue();
    ValueTable bdTable = jdbcDatasource.getValueTable("BONE_DENSITY");
    assertThat(bdTable.getEntityType()).isEqualTo("Participant");

    assertThat(bdTable.getVariables()).hasSize(2);
    assertThat(bdTable.getValueSets()).hasSize(2);
    assertThat(bdTable.getValueSetCount()).isEqualTo(2);
    VariableEntity entity1234_2 = new VariableEntityBean(bdTable.getEntityType(), "1234-2");
    VariableEntity entity1234_3 = new VariableEntityBean(bdTable.getEntityType(), "1234-3");
    assertThat(bdTable.hasValueSet(entity1234_2)).isTrue();
    assertThat(bdTable.hasValueSet(entity1234_3)).isTrue();

    // Check variable values.
    ValueSet vs1234_2 = bdTable.getValueSet(entity1234_2);
    assertThat(bdTable.getValue(bdTable.getVariable("BD"), vs1234_2)).isEqualTo(IntegerType.get().valueOf(64));
    assertThat(bdTable.getValue(bdTable.getVariable("BD_2"), vs1234_2)).isEqualTo(IntegerType.get().valueOf(65));
    ValueSet vs1234_3 = bdTable.getValueSet(entity1234_3);
    assertThat(bdTable.getValue(bdTable.getVariable("BD"), vs1234_3)).isEqualTo(IntegerType.get().valueOf(65));
    assertThat(bdTable.getValue(bdTable.getVariable("BD_2"), vs1234_3)).isEqualTo(IntegerType.get().valueOf(65));
  }

  private void testCreateDatasourceFromScratch(JdbcDatasource jdbcDatasource) {
    // Create a new ValueTable.
    try(ValueTableWriter tableWriter = jdbcDatasource.createWriter("my_table", "Participant")) {
      assertThat(tableWriter).isNotNull();
      assertThat(jdbcDatasource.getName()).isEqualTo("my-datasource-nodb");
      assertThat(jdbcDatasource.hasValueTable("my_table")).isTrue();

      // Write some variables.
      try(VariableWriter variableWriter = tableWriter.writeVariables()) {
        variableWriter.writeVariable(Variable.Builder.newVariable("my_var1", IntegerType.get(), "Participant").build());
        variableWriter.writeVariable(Variable.Builder.newVariable("my_var2", DecimalType.get(), "Participant").build());
      }

      // Write a value set.
      VariableEntity myEntity1 = new VariableEntityBean("Participant", "1");
      try(ValueSetWriter valueSetWriter = tableWriter.writeValueSet(myEntity1)) {
        Variable myVar1 = jdbcDatasource.getValueTable("my_table").getVariable("my_var1");
        Variable myVar2 = jdbcDatasource.getValueTable("my_table").getVariable("my_var2");
        valueSetWriter.writeValue(myVar1, IntegerType.get().valueOf(77));
        valueSetWriter.writeValue(myVar2, IntegerType.get().valueOf(78));
      }
    }
  }

  private boolean hasCategory(Variable variable, String categoryName, String categoryCode) {
    if(variable.hasCategories()) {
      for(Category category : variable.getCategories()) {
        if(category.getName().equals(categoryName) && category.getCode().equals(categoryCode)) {
          return true;
        }
      }
    }
    return false;
  }

}
