/*
 * Copyright (c) 2019 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.magma.datasource.jdbc;

import com.google.common.collect.Sets;
import com.google.common.primitives.Ints;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.obiba.core.test.spring.Dataset;
import org.obiba.core.test.spring.DbUnitAwareTestExecutionListener;
import org.obiba.magma.*;
import org.obiba.magma.ValueTableWriter.ValueSetWriter;
import org.obiba.magma.ValueTableWriter.VariableWriter;
import org.obiba.magma.support.VariableEntityBean;
import org.obiba.magma.test.AbstractMagmaTest;
import org.obiba.magma.test.SchemaTestExecutionListener;
import org.obiba.magma.test.TestSchema;
import org.obiba.magma.type.DecimalType;
import org.obiba.magma.type.IntegerType;
import org.obiba.magma.type.TextType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
import org.springframework.test.context.support.DirtiesContextTestExecutionListener;
import org.springframework.test.context.transaction.TransactionConfiguration;
import org.springframework.test.context.transaction.TransactionalTestExecutionListener;

import javax.sql.DataSource;
import java.util.stream.Collectors;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.fest.assertions.api.Assertions.fail;

@SuppressWarnings({"ReuseOfLocalVariable", "OverlyLongMethod", "PMD.NcssMethodCount"})
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("/test-spring-context.xml")
@TransactionConfiguration
@TestExecutionListeners({DependencyInjectionTestExecutionListener.class, DirtiesContextTestExecutionListener.class,
    TransactionalTestExecutionListener.class, SchemaTestExecutionListener.class,
    DbUnitAwareTestExecutionListener.class})
public class JdbcDatasourceTest extends AbstractMagmaTest {

  @Rule
  public final ExpectedException exception = ExpectedException.none();

  @Autowired
  private DataSource dataSource;

  @TestSchema(schemaLocation = "org/obiba/magma/datasource/jdbc", beforeSchema = "schema-nometa.sql",
      afterSchema = "schema-notables.sql")
  @Dataset(filenames = "JdbcDatasourceTest-nometa.xml")
  @Test
  public void testCreateDatasourceFromExistingDatabase() {
    JdbcDatasource jdbcDatasource = new JdbcDatasource("my-datasource", dataSource,
        JdbcDatasourceSettings.newSettings("Participant").mappedTables(Sets.newHashSet("BONE_DENSITY")).build());
    jdbcDatasource.initialise();

    createDatasourceFromExistingDatabase(jdbcDatasource);

    jdbcDatasource.dispose();
  }

  @TestSchema(schemaLocation = "org/obiba/magma/datasource/jdbc", beforeSchema = "schema-nometa-where.sql",
      afterSchema = "schema-notables.sql")
  @Dataset(filenames = "JdbcDatasourceTest-nometa.xml")
  @Test
  public void testCreateDatasourceFromExistingDatabaseWithWhereClause() {
    testCreateDatasourceFromExistingDatabaseWithTableSettings();
  }

  @TestSchema(schemaLocation = "org/obiba/magma/datasource/jdbc", beforeSchema = "schema-nometa-repeatables.sql",
      afterSchema = "schema-notables.sql")
  @Dataset(filenames = "JdbcDatasourceTest-nometa-repeatables.xml")
  @Test
  public void testCreateDatasourceFromExistingDatabaseWithDetectedMultilines() {
    testCreateDatasourceFromExistingDatabaseWithTableSettings();
  }

  @TestSchema(schemaLocation = "org/obiba/magma/datasource/jdbc", beforeSchema = "schema-nometa-repeatables.sql",
      afterSchema = "schema-notables.sql")
  @Dataset(filenames = "JdbcDatasourceTest-nometa-repeatables.xml")
  @Test
  public void testCreateDatasourceFromExistingDatabaseWithTableMultilines() {
    JdbcValueTableSettings tableSettings = JdbcValueTableSettings.newSettings("BONE_DENSITY").entityType("Participant")
        .entityIdentifierColumn("PART_ID").multilines().build();
    JdbcDatasource jdbcDatasource = new JdbcDatasource("my-datasource", dataSource,
        JdbcDatasourceSettings.newSettings("Participant").mappedTables(Sets.newHashSet("BONE_DENSITY")).tableSettings(Sets.newHashSet(tableSettings)).build());
    jdbcDatasource.initialise();

    createDatasourceFromExistingDatabase(jdbcDatasource);

    jdbcDatasource.dispose();
  }

  @TestSchema(schemaLocation = "org/obiba/magma/datasource/jdbc", beforeSchema = "schema-nometa-repeatables.sql",
      afterSchema = "schema-notables.sql")
  @Dataset(filenames = "JdbcDatasourceTest-nometa-repeatables.xml")
  @Test
  public void testCreateDatasourceFromExistingDatabaseWithDatasourceMultilines() {
    JdbcValueTableSettings tableSettings = JdbcValueTableSettings.newSettings("BONE_DENSITY").entityType("Participant")
        .entityIdentifierColumn("PART_ID").build();
    JdbcDatasource jdbcDatasource = new JdbcDatasource("my-datasource", dataSource,
        JdbcDatasourceSettings.newSettings("Participant").mappedTables(Sets.newHashSet("BONE_DENSITY"))
            .tableSettings(Sets.newHashSet(tableSettings)).multilines().build());
    jdbcDatasource.initialise();

    createDatasourceFromExistingDatabase(jdbcDatasource);

    jdbcDatasource.dispose();
  }

  @TestSchema(schemaLocation = "org/obiba/magma/datasource/jdbc", beforeSchema = "schema-meta.sql",
      afterSchema = "schema-notables.sql")
  @Dataset(filenames = "JdbcDatasourceTest.xml")
  @Test
  @Ignore //TODO: fix dbunit config in obiba-commons to quote identifier names
  public void testCreateDatasourceFromExistingDatabaseUsingMetadataTables() {
    JdbcDatasource jdbcDatasource = new JdbcDatasource("my-datasource", dataSource, "Participant", true);
    jdbcDatasource.initialise();

    createDatasourceFromExistingDatabase(jdbcDatasource);

    ValueTable valueTable = jdbcDatasource.getValueTable("BONE_DENSITY");
    Variable bdVar = valueTable.getVariable("BD");
    Variable bdVar2 = valueTable.getVariable("BD_2");

    // Verify variable attributes.
    assertThat(bdVar.hasAttribute("description")).isTrue();
    assertThat(bdVar.getAttributeValue("description").toString()).isEqualTo("BD description");
    assertThat(bdVar2.hasAttribute("description")).isTrue();
    assertThat(bdVar2.getAttributeValue("description").toString()).isEqualTo("BD_2 description");

    // Verify categories.
    assertThat(hasCategory(bdVar, "PNA")).isTrue();
    assertThat(hasCategory(bdVar, "DNK")).isTrue();

    jdbcDatasource.dispose();
  }

  @TestSchema(schemaLocation = "org/obiba/magma/datasource/jdbc", beforeSchema = "schema-nometa.sql",
      afterSchema = "schema-notables.sql")
  @Dataset(filenames = "JdbcDatasourceTest-nometa.xml")
  @Test
  public void test_vectorSource() {
    JdbcDatasource jdbcDatasource = new JdbcDatasource("my-datasource", dataSource,
        JdbcDatasourceSettings.newSettings("Participant").mappedTables(Sets.newHashSet("BONE_DENSITY")).build());
    jdbcDatasource.initialise();

    createDatasourceFromExistingDatabase(jdbcDatasource);

    ValueTable valueTable = jdbcDatasource.getValueTable("BONE_DENSITY");
    VectorSource bdVar = valueTable.getVariableValueSource("BD").asVectorSource();
    VectorSource bdVar2 = valueTable.getVariableValueSource("BD_2").asVectorSource();

    // Verify variable attributes.
    assertThat(bdVar).isNotNull();
    assertThat(bdVar2).isNotNull();

    Iterable<Value> values = bdVar.getValues(valueTable.getVariableEntities());

    assertThat(values).hasSize(2);

    values = bdVar2.getValues(valueTable.getVariableEntities());
    assertThat(values).hasSize(2);

    jdbcDatasource.dispose();
  }

  @TestSchema(schemaLocation = "org/obiba/magma/datasource/jdbc", beforeSchema = "schema-nometa-where.sql",
      afterSchema = "schema-notables.sql")
  @Dataset(filenames = "JdbcDatasourceTest-nometa-where.xml")
  @Test
  public void test_vectorSourceWithWhereClause() {
    JdbcValueTableSettings tableSettings = JdbcValueTableSettings.newSettings("BONE_DENSITY")
        .entityIdentifierColumn("PART_ID").entityIdentifiersWhere("VISIT_ID = 2").build();
    JdbcDatasourceSettings settings = JdbcDatasourceSettings.newSettings("Participant").tableSettings(Sets.newHashSet(tableSettings)).build();
    JdbcDatasource jdbcDatasource = new JdbcDatasource("my-datasource", dataSource, settings);
    jdbcDatasource.initialise();

    createDatasourceFromExistingDatabase(jdbcDatasource);

    ValueTable valueTable = jdbcDatasource.getValueTable("BONE_DENSITY");
    VectorSource bdVar = valueTable.getVariableValueSource("BD").asVectorSource();
    VectorSource bdVar2 = valueTable.getVariableValueSource("BD_2").asVectorSource();

    // Verify variable attributes.
    assertThat(bdVar).isNotNull();
    assertThat(bdVar2).isNotNull();

    Iterable<Value> values = bdVar.getValues(valueTable.getVariableEntities());

    assertThat(values).hasSize(2);

    values = bdVar2.getValues(valueTable.getVariableEntities());
    assertThat(values).hasSize(2);

    jdbcDatasource.dispose();
  }

  @TestSchema(schemaLocation = "org/obiba/magma/datasource/jdbc", beforeSchema = "schema-nometa-where.sql",
      afterSchema = "schema-notables.sql")
  @Dataset(filenames = "JdbcDatasourceTest-nometa-where.xml")
  @Test
  public void test_datasourceWithSettingsFactory() {
    JdbcValueTableSettingsFactory tableSettingsFactory = JdbcValueTableSettingsFactory.newSettings("BONE_DENSITY", "VISIT_ID")
        .tableName("BD_").entityIdentifierColumn("PART_ID").build();
    JdbcDatasourceSettings settings = JdbcDatasourceSettings.newSettings("Participant").tableSettingsFactories(Sets.newHashSet(tableSettingsFactory)).build();
    JdbcDatasource jdbcDatasource = new JdbcDatasource("my-datasource", dataSource, settings);
    jdbcDatasource.initialise();

    assertThat(jdbcDatasource).isNotNull();
    assertThat(jdbcDatasource.getName()).isEqualTo("my-datasource");

    assertThat(jdbcDatasource.getValueTables()).hasSize(2);
    assertThat(jdbcDatasource.hasValueTable("BD_1")).isTrue();
    assertThat(jdbcDatasource.hasValueTable("BD_2")).isTrue();
    ValueTable bdTable = jdbcDatasource.getValueTable("BD_1");
    assertThat(bdTable.getEntityType()).isEqualTo("Participant");
    assertThat(bdTable.getVariableEntityCount()).isEqualTo(1);
    bdTable = jdbcDatasource.getValueTable("BD_2");
    assertThat(bdTable.getEntityType()).isEqualTo("Participant");
    assertThat(bdTable.getVariableEntityCount()).isEqualTo(2);
    jdbcDatasource.dispose();
  }

  @TestSchema(schemaLocation = "org/obiba/magma/datasource/jdbc", beforeSchema = "schema-notables.sql",
      afterSchema = "schema-notables.sql")
  @Test
  public void testCreateDatasourceFromScratch() { // i.e., no existing database
    JdbcDatasource jdbcDatasource = new JdbcDatasource("my-datasource-nodb", dataSource, "Participant", false);
    jdbcDatasource.initialise();

    createDatasourceFromScratch(jdbcDatasource);

    jdbcDatasource.dispose();
  }

  @TestSchema(schemaLocation = "org/obiba/magma/datasource/jdbc", beforeSchema = "schema-meta.sql",
      afterSchema = "schema-notables.sql")
  @Test
  public void testCreateDatasourceFromScratchUsingMetadataTables() {
    JdbcDatasourceSettings settings = JdbcDatasourceSettings.newSettings("Participant").useMetadataTables().multipleDatasources().build();
    JdbcDatasource jdbcDatasource = new JdbcDatasource("my-datasource-nodb", dataSource, settings);
    jdbcDatasource.initialise();

    createDatasourceFromScratch(jdbcDatasource);

    jdbcDatasource.dispose();
  }

  @TestSchema(schemaLocation = "org/obiba/magma/datasource/jdbc", beforeSchema = "schema-notables.sql",
      afterSchema = "schema-notables.sql")
  @Test
  public void test_Timestamped() {
    JdbcDatasource jdbcDatasource = new JdbcDatasource("my-datasource-nodb", dataSource, getDataSourceSettings());
    jdbcDatasource.initialise();

    createDatasourceFromScratch(jdbcDatasource);

    ValueTable t = jdbcDatasource.getValueTables().iterator().next();
    Timestamps ts = t.getTimestamps();

    assertThat(ts).isNotNull();
    assertThat(ts.getCreated()).isNotNull();
    assertThat(ts.getCreated().isNull()).isFalse();
    assertThat(ts.getLastUpdate()).isNotNull();
    assertThat(ts.getLastUpdate().isNull()).isFalse();

    jdbcDatasource.dispose();
  }

  @TestSchema(schemaLocation = "org/obiba/magma/datasource/jdbc", beforeSchema = "schema-notables.sql")
  @Test
  public void testDropDatasource() {
    JdbcDatasource jdbcDatasource = new JdbcDatasource("my-datasource-nodb", dataSource, getDataSourceSettings());
    jdbcDatasource.initialise();

    createDatasourceFromScratch(jdbcDatasource);
    jdbcDatasource.drop();
    jdbcDatasource.dispose();

    assertThat(jdbcDatasource.getValueTables()).isEmpty();
  }

  @TestSchema(schemaLocation = "org/obiba/magma/datasource/jdbc", beforeSchema = "schema-notables.sql")
  @Test
  public void testRemoveValueSet() {
    JdbcDatasource jdbcDatasource = new JdbcDatasource("my-datasource-nodb", dataSource, getDataSourceSettings());
    jdbcDatasource.initialise();

    createDatasourceFromScratch(jdbcDatasource);

    VariableEntity myEntity1 = new VariableEntityBean("Participant", "1");

    try (ValueTableWriter writer = jdbcDatasource.createWriter("MY_TABLE", "Participant")) {
      try (ValueSetWriter vsWriter = writer.writeValueSet(myEntity1)) {
        vsWriter.remove();
      }
    }

    ValueTable vt = jdbcDatasource.getValueTable("MY_TABLE");
    assertThat(vt.getValueSets()).isEmpty();
  }

  @TestSchema(schemaLocation = "org/obiba/magma/datasource/jdbc", beforeSchema = "schema-notables.sql")
  @Test
  public void testRemoveVariable() {
    JdbcDatasource jdbcDatasource = new JdbcDatasource("my-datasource-nodb", dataSource, getDataSourceSettings());
    jdbcDatasource.initialise();

    createDatasourceFromScratch(jdbcDatasource);

    try (ValueTableWriter writer = jdbcDatasource.createWriter("MY_TABLE", "Participant")) {
      try (VariableWriter varWriter = writer.writeVariables()) {
        varWriter.removeVariable(jdbcDatasource.getValueTable("MY_TABLE").getVariable("MY_VAR1"));
      }
    }

    ValueTable vt = jdbcDatasource.getValueTable("MY_TABLE");
    exception.expect(NoSuchVariableException.class);

    fail(String.format("Variable %s not removed.", vt.getVariable("MY_VAR1")));
  }

  @TestSchema(schemaLocation = "org/obiba/magma/datasource/jdbc", beforeSchema = "schema-notables.sql",
      afterSchema = "schema-notables.sql")
  @Test
  public void testValueSetCount() {
    JdbcDatasource jdbcDatasource = new JdbcDatasource("my-datasource-nodb", dataSource, getDataSourceSettings());
    jdbcDatasource.initialise();

    createDatasourceFromScratch(jdbcDatasource);
    ValueTable vt = jdbcDatasource.getValueTable("MY_TABLE");

    assertThat(vt.getValueSetCount()).isEqualTo(1);
    assertThat(vt.getVariableEntityCount()).isEqualTo(1);
    assertThat(vt.getVariableCount()).isEqualTo(2);

    jdbcDatasource.dispose();
  }

  @TestSchema(schemaLocation = "org/obiba/magma/datasource/jdbc", beforeSchema = "schema-notables.sql",
      afterSchema = "schema-notables.sql")
  @Test
  public void testColumnsNormalized() {
    JdbcDatasource jdbcDatasource = new JdbcDatasource("my-datasource-nodb", dataSource, getDataSourceSettings());
    jdbcDatasource.initialise();

    try (ValueTableWriter tableWriter = jdbcDatasource.createWriter("MY_TABLE", "Participant")) {
      // Write some variables.
      try (VariableWriter variableWriter = tableWriter.writeVariables()) {
        variableWriter.writeVariable(Variable.Builder.newVariable("test.MY_VAR1", IntegerType.get(), "Participant").build());
        variableWriter.writeVariable(Variable.Builder.newVariable("test.MY_VAR2", DecimalType.get(), "Participant").build());
      }

      // Write a value set.
      VariableEntity myEntity1 = new VariableEntityBean("Participant", "1");

      try (ValueSetWriter valueSetWriter = tableWriter.writeValueSet(myEntity1)) {
        Variable myVar1 = jdbcDatasource.getValueTable("MY_TABLE").getVariable("test.MY_VAR1");
        Variable myVar2 = jdbcDatasource.getValueTable("MY_TABLE").getVariable("test.MY_VAR2");
        valueSetWriter.writeValue(myVar1, IntegerType.get().valueOf(77));
        valueSetWriter.writeValue(myVar2, IntegerType.get().valueOf(78));
      }
    }

    ValueTable vt = jdbcDatasource.getValueTable("MY_TABLE");

    assertThat(vt.getValueSetCount()).isEqualTo(1);
    assertThat(vt.getVariableEntityCount()).isEqualTo(1);
    assertThat(vt.getVariableCount()).isEqualTo(2);

    jdbcDatasource.dispose();
  }

  @TestSchema(schemaLocation = "org/obiba/magma/datasource/jdbc", beforeSchema = "schema-notables.sql",
      afterSchema = "schema-notables.sql")
  @Test
  public void testSinglelinesWriterWithMetadataTables() {
    JdbcDatasource jdbcDatasource = new JdbcDatasource("my-datasource-nodb", dataSource, getDataSourceSettingsBuilder(false).useMetadataTables().build());
    jdbcDatasource.initialise();
    writeTestValueSet(jdbcDatasource);
    jdbcDatasource.dispose();

    jdbcDatasource = new JdbcDatasource("my-datasource-nodb", dataSource, getDataSourceSettingsBuilder(false).useMetadataTables().build());
    jdbcDatasource.initialise();

    VariableEntity myEntity1 = new VariableEntityBean("Participant", "1");

    ValueTable vt = jdbcDatasource.getValueTable("MY_TABLE");
    assertThat(vt.getValueSetCount()).isEqualTo(1);
    assertThat(vt.hasValueSet(myEntity1)).isTrue();
    ValueSet vs = vt.getValueSet(myEntity1);

    assertThat(vt.hasVariable("MY_VAR1")).isTrue();
    Variable myVar1 = vt.getVariable("MY_VAR1");
    assertThat(myVar1.isRepeatable()).isTrue();

    assertThat(vt.hasVariable("MY_VAR2")).isTrue();
    Variable myVar2 = vt.getVariable("MY_VAR2");
    assertThat(myVar2.isRepeatable()).isTrue();

    assertThat(vt.hasVariable("MY_VAR3")).isTrue();
    Variable myVar3 = vt.getVariable("MY_VAR3");
    assertThat(myVar3.isRepeatable()).isFalse();

    Value val1 = vt.getValue(myVar1, vs);
    assertThat(val1.isSequence()).isTrue();
    assertThat(val1.asSequence().getSize()).isEqualTo(3);
    assertThat(val1.asSequence().get(0).getValue()).isEqualTo(77L);
    assertThat(val1.asSequence().get(1).getValue()).isEqualTo(78L);
    assertThat(val1.asSequence().get(2).getValue()).isEqualTo(79L);

    Value val2 = vt.getValue(myVar2, vs);
    assertThat(val2.isSequence()).isTrue();
    assertThat(val2.asSequence().getSize()).isEqualTo(2);
    assertThat(val2.asSequence().get(0).getValue()).isEqualTo(81.0);
    assertThat(val2.asSequence().get(1).getValue()).isEqualTo(82.0);

    Value val3 = vt.getValue(myVar3, vs);
    assertThat(val3.isSequence()).isFalse();
    assertThat(val3.getValue()).isEqualTo("Coucou");

    jdbcDatasource.dispose();
  }

  @TestSchema(schemaLocation = "org/obiba/magma/datasource/jdbc", beforeSchema = "schema-notables.sql",
      afterSchema = "schema-notables.sql")
  @Test
  public void testSinglelinesWriterWithoutMetadataTables() {
    JdbcDatasource jdbcDatasource = new JdbcDatasource("my-datasource-nodb", dataSource, getDataSourceSettingsBuilder(false).build());
    jdbcDatasource.initialise();
    writeTestValueSet(jdbcDatasource);
    jdbcDatasource.dispose();

    JdbcValueTableSettings tableSettings = JdbcValueTableSettings.newSettings("MY_TABLE").build();
    jdbcDatasource = new JdbcDatasource("my-datasource-nodb", dataSource, getDataSourceSettingsBuilder(false)
        .tableSettings(Sets.newHashSet(tableSettings)).build());
    jdbcDatasource.initialise();

    VariableEntity myEntity1 = new VariableEntityBean("Participant", "1");

    ValueTable vt = jdbcDatasource.getValueTable("MY_TABLE");
    assertThat(vt.getValueSetCount()).isEqualTo(1);
    assertThat(vt.hasValueSet(myEntity1)).isTrue();
    ValueSet vs = vt.getValueSet(myEntity1);

    assertThat(vt.hasVariable("MY_VAR1")).isTrue();
    Variable myVar1 = vt.getVariable("MY_VAR1");
    assertThat(myVar1.isRepeatable()).isFalse();

    assertThat(vt.hasVariable("MY_VAR2")).isTrue();
    Variable myVar2 = vt.getVariable("MY_VAR2");
    assertThat(myVar2.isRepeatable()).isFalse();

    assertThat(vt.hasVariable("MY_VAR3")).isTrue();
    Variable myVar3 = vt.getVariable("MY_VAR3");
    assertThat(myVar3.isRepeatable()).isFalse();

    Value val1 = vt.getValue(myVar1, vs);
    assertThat(val1.isSequence()).isFalse();
    assertThat(val1.getValue()).isEqualTo("77,78,79");

    Value val2 = vt.getValue(myVar2, vs);
    assertThat(val2.isSequence()).isFalse();
    assertThat(val2.getValue()).isEqualTo("81,82");

    Value val3 = vt.getValue(myVar3, vs);
    assertThat(val3.isSequence()).isFalse();
    assertThat(val3.getValue()).isEqualTo("Coucou");

    jdbcDatasource.dispose();
  }

  @TestSchema(schemaLocation = "org/obiba/magma/datasource/jdbc", beforeSchema = "schema-notables.sql",
      afterSchema = "schema-notables.sql")
  @Test
  public void testMultilinesWriterWithMetadataTables() {
    // specifying useMetadataTables allows to read back the written table (otherwise table is not detected because it has no primary key)
    JdbcDatasource jdbcDatasource = new JdbcDatasource("my-datasource-nodb", dataSource, getDataSourceSettingsBuilder(true).useMetadataTables().build());
    jdbcDatasource.initialise();
    writeTestValueSet(jdbcDatasource);
    jdbcDatasource.dispose();

    jdbcDatasource = new JdbcDatasource("my-datasource-nodb", dataSource, getDataSourceSettingsBuilder(true).useMetadataTables().build());
    jdbcDatasource.initialise();

    VariableEntity myEntity1 = new VariableEntityBean("Participant", "1");

    ValueTable vt = jdbcDatasource.getValueTable("MY_TABLE");
    assertThat(vt.getValueSetCount()).isEqualTo(1);
    assertThat(vt.hasValueSet(myEntity1)).isTrue();
    ValueSet vs = vt.getValueSet(myEntity1);

    assertThat(vt.hasVariable("MY_VAR1")).isTrue();
    Variable myVar1 = vt.getVariable("MY_VAR1");
    assertThat(myVar1.isRepeatable()).isTrue();

    assertThat(vt.hasVariable("MY_VAR2")).isTrue();
    Variable myVar2 = vt.getVariable("MY_VAR2");
    assertThat(myVar2.isRepeatable()).isTrue();

    assertThat(vt.hasVariable("MY_VAR3")).isTrue();
    Variable myVar3 = vt.getVariable("MY_VAR3");
    assertThat(myVar3.isRepeatable()).isFalse();

    Value val1 = vt.getValue(myVar1, vs);
    assertThat(val1.isSequence()).isTrue();
    assertThat(val1.asSequence().getSize()).isEqualTo(3);
    assertThat(val1.asSequence().get(0).getValue()).isEqualTo(77L);
    assertThat(val1.asSequence().get(1).getValue()).isEqualTo(78L);
    assertThat(val1.asSequence().get(2).getValue()).isEqualTo(79L);

    Value val2 = vt.getValue(myVar2, vs);
    assertThat(val2.isSequence()).isTrue();
    assertThat(val2.asSequence().getSize()).isEqualTo(3);
    assertThat(val2.asSequence().get(0).getValue()).isEqualTo(81.0);
    assertThat(val2.asSequence().get(1).getValue()).isEqualTo(82.0);
    assertThat(val2.asSequence().get(2).isNull()).isTrue();

    Value val3 = vt.getValue(myVar3, vs);
    assertThat(val3.isSequence()).isFalse();
    assertThat(val3.getValue()).isEqualTo("Coucou");

    jdbcDatasource.dispose();
  }

  @TestSchema(schemaLocation = "org/obiba/magma/datasource/jdbc", beforeSchema = "schema-notables.sql",
      afterSchema = "schema-notables.sql")
  @Test
  public void testMultilinesWriterWithoutMetadataTables() {
    JdbcDatasource jdbcDatasource = new JdbcDatasource("my-datasource-nodb", dataSource, getDataSourceSettingsBuilder(true).build());
    jdbcDatasource.initialise();
    writeTestValueSet(jdbcDatasource);
    jdbcDatasource.dispose();

    JdbcValueTableSettings tableSettings = JdbcValueTableSettings.newSettings("MY_TABLE").build();
    jdbcDatasource = new JdbcDatasource("my-datasource-nodb", dataSource, getDataSourceSettingsBuilder(false)
        .tableSettings(Sets.newHashSet(tableSettings)).build());
    jdbcDatasource.initialise();

    VariableEntity myEntity1 = new VariableEntityBean("Participant", "1");

    ValueTable vt = jdbcDatasource.getValueTable("MY_TABLE");
    assertThat(vt.getValueSetCount()).isEqualTo(1);
    assertThat(vt.hasValueSet(myEntity1)).isTrue();
    ValueSet vs = vt.getValueSet(myEntity1);

    assertThat(vt.hasVariable("MY_VAR1")).isTrue();
    Variable myVar1 = vt.getVariable("MY_VAR1");
    assertThat(myVar1.isRepeatable()).isTrue();

    assertThat(vt.hasVariable("MY_VAR2")).isTrue();
    Variable myVar2 = vt.getVariable("MY_VAR2");
    assertThat(myVar2.isRepeatable()).isTrue();

    assertThat(vt.hasVariable("MY_VAR3")).isTrue();
    Variable myVar3 = vt.getVariable("MY_VAR3");
    assertThat(myVar3.isRepeatable()).isTrue();

    Value val1 = vt.getValue(myVar1, vs);
    assertThat(val1.isSequence()).isTrue();
    assertThat(val1.asSequence().getSize()).isEqualTo(3);
    assertThat(val1.asSequence().get(0).getValue()).isEqualTo(77L);
    assertThat(val1.asSequence().get(1).getValue()).isEqualTo(78L);
    assertThat(val1.asSequence().get(2).getValue()).isEqualTo(79L);

    Value val2 = vt.getValue(myVar2, vs);
    assertThat(val2.isSequence()).isTrue();
    assertThat(val2.asSequence().getSize()).isEqualTo(3);
    assertThat(val2.asSequence().get(0).getValue()).isEqualTo(81.0);
    assertThat(val2.asSequence().get(1).getValue()).isEqualTo(82.0);
    assertThat(val2.asSequence().get(2).isNull()).isTrue();

    Value val3 = vt.getValue(myVar3, vs);
    assertThat(val3.isSequence()).isTrue();
    assertThat(val3.asSequence().getSize()).isEqualTo(3);
    assertThat(val3.asSequence().get(0).getValue()).isEqualTo("Coucou");
    assertThat(val3.asSequence().get(1).getValue()).isEqualTo("Coucou");
    assertThat(val3.asSequence().get(2).getValue()).isEqualTo("Coucou");

    jdbcDatasource.dispose();
  }

  //
  // Methods
  //

  private void testCreateDatasourceFromExistingDatabaseWithTableSettings() {
    JdbcValueTableSettings tableSettings = JdbcValueTableSettings.newSettings("BONE_DENSITY").entityType("Participant")
        .entityIdentifierColumn("PART_ID").build();
    JdbcDatasource jdbcDatasource = new JdbcDatasource("my-datasource", dataSource,
        JdbcDatasourceSettings.newSettings("Participant").mappedTables(Sets.newHashSet("BONE_DENSITY")).tableSettings(Sets.newHashSet(tableSettings)).build());
    jdbcDatasource.initialise();

    createDatasourceFromExistingDatabase(jdbcDatasource);

    jdbcDatasource.dispose();
  }

  private void writeTestValueSet(JdbcDatasource jdbcDatasource) {
    VariableEntity myEntity1 = new VariableEntityBean("Participant", "1");

    try (ValueTableWriter tableWriter = jdbcDatasource.createWriter("MY_TABLE", "Participant")) {
      // Write some variables.
      try (VariableWriter variableWriter = tableWriter.writeVariables()) {
        variableWriter.writeVariable(Variable.Builder.newVariable("MY_VAR1", IntegerType.get(), "Participant").repeatable().build());
        variableWriter.writeVariable(Variable.Builder.newVariable("MY_VAR2", DecimalType.get(), "Participant").repeatable().build());
        variableWriter.writeVariable(Variable.Builder.newVariable("MY_VAR3", TextType.get(), "Participant").build());
      }

      // Write a value set.
      try (ValueSetWriter valueSetWriter = tableWriter.writeValueSet(myEntity1)) {
        Variable myVar1 = jdbcDatasource.getValueTable("MY_TABLE").getVariable("MY_VAR1");
        Variable myVar2 = jdbcDatasource.getValueTable("MY_TABLE").getVariable("MY_VAR2");
        Variable myVar3 = jdbcDatasource.getValueTable("MY_TABLE").getVariable("MY_VAR3");
        valueSetWriter.writeValue(myVar1, IntegerType.get().sequenceOf(Ints.asList(77, 78, 79).stream().map(v -> IntegerType.get().valueOf(v)).collect(Collectors.toList())));
        valueSetWriter.writeValue(myVar2, IntegerType.get().sequenceOf(Ints.asList(81, 82).stream().map(v -> IntegerType.get().valueOf(v)).collect(Collectors.toList())));
        valueSetWriter.writeValue(myVar3, TextType.get().valueOf("Coucou"));
      }
    }
  }

  private JdbcDatasourceSettings getDataSourceSettings() {
    return getDataSourceSettingsBuilder(false).multipleDatasources().build();
  }

  private JdbcDatasourceSettings.Builder getDataSourceSettingsBuilder(boolean multilines) {
    return JdbcDatasourceSettings.newSettings("Participant")
        .createdTimestampColumn("created").updatedTimestampColumn("updated").multilines(multilines);
  }

  private void createDatasourceFromExistingDatabase(JdbcDatasource jdbcDatasource) {
    assertThat(jdbcDatasource).isNotNull();
    assertThat(jdbcDatasource.getName()).isEqualTo("my-datasource");

    assertThat(jdbcDatasource.getValueTables()).hasSize(1);
    assertThat(jdbcDatasource.hasValueTable("BONE_DENSITY")).isTrue();
    ValueTable bdTable = jdbcDatasource.getValueTable("BONE_DENSITY");
    assertThat(bdTable.getEntityType()).isEqualTo("Participant");

    assertThat(bdTable.getVariables()).hasSize(3);
    assertThat(bdTable.getValueSets()).hasSize(2);
    assertThat(bdTable.getValueSetCount()).isEqualTo(2);
    VariableEntity entity1234_2 = new VariableEntityBean(bdTable.getEntityType(), "12342");
    VariableEntity entity1234_3 = new VariableEntityBean(bdTable.getEntityType(), "12343");
    assertThat(bdTable.hasValueSet(entity1234_2)).isTrue();
    assertThat(bdTable.hasValueSet(entity1234_3)).isTrue();

    // Check variable values.
    ValueSet vs1234_2 = bdTable.getValueSet(entity1234_2);
    ValueSet vs1234_3 = bdTable.getValueSet(entity1234_3);
    Variable bdVar = bdTable.getVariable("BD");
    if (bdVar.isRepeatable()) {
      Value value = bdTable.getValue(bdVar, vs1234_2);
      assertThat(value.isSequence()).isTrue();
      assertThat(value.asSequence().getSize()).isEqualTo(2);
      value = bdTable.getValue(bdVar, vs1234_3);
      assertThat(value.isSequence()).isTrue();
      assertThat(value.asSequence().getSize()).isEqualTo(3);
    } else {
      assertThat(bdTable.getValue(bdVar, vs1234_2)).isEqualTo(IntegerType.get().valueOf(64));
      assertThat(bdTable.getValue(bdVar, vs1234_3)).isEqualTo(IntegerType.get().valueOf(65));
    }
    Variable bd2Var = bdTable.getVariable("BD_2");
    if (bd2Var.isRepeatable()) {
      Value value = bdTable.getValue(bd2Var, vs1234_2);
      assertThat(value.isSequence()).isTrue();
      assertThat(value.asSequence().getSize()).isEqualTo(2);
      value = bdTable.getValue(bd2Var, vs1234_3);
      assertThat(value.isSequence()).isTrue();
      assertThat(value.asSequence().getSize()).isEqualTo(3);
    } else {
      assertThat(bdTable.getValue(bd2Var, vs1234_2)).isEqualTo(IntegerType.get().valueOf(65));
      assertThat(bdTable.getValue(bd2Var, vs1234_3)).isEqualTo(IntegerType.get().valueOf(65));
    }
  }

  private void createDatasourceFromScratch(JdbcDatasource jdbcDatasource) {
    // Create a new ValueTable.
    try (ValueTableWriter tableWriter = jdbcDatasource.createWriter("MY_TABLE", "Participant")) {
      assertThat(tableWriter).isNotNull();
      assertThat(jdbcDatasource.getName()).isEqualTo("my-datasource-nodb");
      assertThat(jdbcDatasource.hasValueTable("MY_TABLE")).isTrue();

      // Write some variables.
      try (VariableWriter variableWriter = tableWriter.writeVariables()) {
        variableWriter.writeVariable(Variable.Builder.newVariable("MY_VAR1", IntegerType.get(), "Participant").build());
        variableWriter.writeVariable(Variable.Builder.newVariable("MY_VAR2", DecimalType.get(), "Participant").build());
      }

      // Write a value set.
      VariableEntity myEntity1 = new VariableEntityBean("Participant", "1");
      try (ValueSetWriter valueSetWriter = tableWriter.writeValueSet(myEntity1)) {
        Variable myVar1 = jdbcDatasource.getValueTable("MY_TABLE").getVariable("MY_VAR1");
        Variable myVar2 = jdbcDatasource.getValueTable("MY_TABLE").getVariable("MY_VAR2");
        valueSetWriter.writeValue(myVar1, IntegerType.get().valueOf(77));
        valueSetWriter.writeValue(myVar2, IntegerType.get().valueOf(78));
      }
    }
  }

  private boolean hasCategory(Variable variable, String categoryName) {
    if (variable.hasCategories()) {
      for (Category category : variable.getCategories()) {
        if (category.getName().equals(categoryName)) {
          return true;
        }
      }
    }
    return false;
  }
}
