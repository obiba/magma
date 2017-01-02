/*
 * Copyright (c) 2017 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.magma.datasource.mongodb;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.TreeSet;

import javax.annotation.Nullable;

import org.junit.After;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.obiba.core.util.FileUtil;
import org.obiba.magma.Datasource;
import org.obiba.magma.DatasourceFactory;
import org.obiba.magma.MagmaEngine;
import org.obiba.magma.NoSuchVariableException;
import org.obiba.magma.Value;
import org.obiba.magma.ValueSequence;
import org.obiba.magma.ValueSet;
import org.obiba.magma.ValueTable;
import org.obiba.magma.ValueTableWriter;
import org.obiba.magma.ValueType;
import org.obiba.magma.Variable;
import org.obiba.magma.VariableEntity;
import org.obiba.magma.VariableValueSource;
import org.obiba.magma.datasource.fs.FsDatasource;
import org.obiba.magma.datasource.generated.GeneratedValueTable;
import org.obiba.magma.support.DatasourceCopier;
import org.obiba.magma.support.Initialisables;
import org.obiba.magma.support.VariableEntityBean;
import org.obiba.magma.test.EmbeddedMongoProcessWrapper;
import org.obiba.magma.type.BinaryType;
import org.obiba.magma.type.BooleanType;
import org.obiba.magma.type.DateTimeType;
import org.obiba.magma.type.DateType;
import org.obiba.magma.type.DecimalType;
import org.obiba.magma.type.IntegerType;
import org.obiba.magma.type.LineStringType;
import org.obiba.magma.type.LocaleType;
import org.obiba.magma.type.PointType;
import org.obiba.magma.type.PolygonType;
import org.obiba.magma.type.TextType;
import org.obiba.magma.xstream.MagmaXStreamExtension;

import com.google.common.base.Charsets;
import com.google.common.base.Function;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.junit.Assert.fail;

@SuppressWarnings({ "UnusedAssignment", "OverlyCoupledClass" })
public class MongoDBDatasourceTest {
  private static final String DB_TEST = "magma-test";

  private static final String TABLE_TEST = "TABLE";

  private static final String PARTICIPANT = "Participant";

  private static final String ONYX_DATA_5_ZIP = "5-onyx-data.zip";

  private String dbUrl;

  private EmbeddedMongoProcessWrapper mongo;

  @Before
  public void before() {
    // run test only if MongoDB is running
    Assume.assumeTrue(setupMongoDB());
  }

  private boolean setupMongoDB() {
    try {
      mongo = new EmbeddedMongoProcessWrapper();
      mongo.start();
      dbUrl = "mongodb://" + mongo.getServerSocketAddress() + '/' + DB_TEST;
      new MagmaEngine().extend(new MagmaXStreamExtension());
      return true;
    } catch(Exception e) {
      return false;
    }
  }

  @After
  public void after() {
    MagmaEngine.get().shutdown();
    mongo.stop();
  }

  @Test
  public void test_writers() throws IOException {
    FsDatasource onyx = new FsDatasource("onyx", FileUtil.getFileFromResource(ONYX_DATA_5_ZIP));
    DatasourceFactory factory = new MongoDBDatasourceFactory("ds-" + DB_TEST, dbUrl);
    Datasource ds = factory.create();
    Initialisables.initialise(ds, onyx);

    DatasourceCopier.Builder.newCopier().build().copy(onyx, ds);
    assertThat(ds.getValueTable("AnkleBrachial").getVariableEntities()).hasSize(5);
    assertThat(ds.getValueTable("AnkleBrachial").getVariables()).hasSize(21);
  }

  @Test
  public void test_restart_magma() throws IOException {
    FsDatasource onyx = new FsDatasource("onyx", FileUtil.getFileFromResource(ONYX_DATA_5_ZIP));
    Datasource datasource1 = new MongoDBDatasourceFactory("ds-" + DB_TEST, dbUrl).create();
    Initialisables.initialise(datasource1, onyx);
    DatasourceCopier.Builder.newCopier().build().copy(onyx, datasource1);

    Datasource datasource2 = new MongoDBDatasourceFactory("ds-" + DB_TEST, dbUrl).create();
    Initialisables.initialise(datasource2);
    ValueTable valueTable = datasource2.getValueTable("AnkleBrachial");
    assertThat(valueTable.getVariableEntities()).hasSize(5);
    assertThat(valueTable.getVariables()).hasSize(21);
    assertThat(valueTable.getVariableCount()).isEqualTo(21);
  }

  @Test
  public void test_integer_sequence_writer() throws Exception {
    Datasource ds = createDatasource();
    int id = 1;
    testWriteReadValue(ds, id++, IntegerType.get().sequenceOf("1,2,3,4"));
    testWriteReadValue(ds, id++, IntegerType.get().nullSequence());
  }

  @Test
  public void test_text_writer() throws Exception {
    Datasource ds = createDatasource();
    int id = 1;
    testWriteReadValue(ds, id++, TextType.get().valueOf("Il était déjà mort..."));
    testWriteReadValue(ds, id++, TextType.get().valueOf("!@#$%^&*()_+{}\":\\`~"));
    testWriteReadValue(ds, id++, TextType.get().nullValue());
  }

  @Test
  public void test_integer_writer() throws Exception {
    Datasource ds = createDatasource();
    int id = 1;
    testWriteReadValue(ds, id++, IntegerType.get().valueOf("1"));
    testWriteReadValue(ds, id++, IntegerType.get().valueOf("-1"));
    testWriteReadValue(ds, id++, IntegerType.get().nullValue());
    testWriteReadValue(ds, id++, IntegerType.get().valueOf(Long.MAX_VALUE));
    testWriteReadValue(ds, id++, IntegerType.get().valueOf(Long.MIN_VALUE));
  }

  @Test
  public void test_decimal_writer() throws Exception {
    Datasource ds = createDatasource();
    int id = 1;
    testWriteReadValue(ds, id++, DecimalType.get().valueOf("1.2"));
    testWriteReadValue(ds, id++, DecimalType.get().valueOf("-1.2"));
    testWriteReadValue(ds, id++, DecimalType.get().nullValue());
    testWriteReadValue(ds, id++, DecimalType.get().valueOf(Double.MAX_VALUE));
    testWriteReadValue(ds, id++, DecimalType.get().valueOf(Double.MIN_VALUE));
  }

  @Test
  public void test_date_writer() throws Exception {
    Datasource ds = createDatasource();
    int id = 1;
    testWriteReadValue(ds, id++, DateType.get().valueOf("1973-01-15"));
    testWriteReadValue(ds, id++, DateType.get().nullValue());
  }

  @Test
  public void test_dateTime_writer() throws Exception {
    Datasource ds = createDatasource();
    int id = 1;
    testWriteReadValue(ds, id++, DateTimeType.get().valueOf("1973-01-15 11:03:14"));
    testWriteReadValue(ds, id++, DateTimeType.get().nullValue());
  }

  @Test
  public void test_boolean_writer() throws Exception {
    Datasource ds = createDatasource();
    int id = 1;
    testWriteReadValue(ds, id++, BooleanType.get().valueOf(true));
    testWriteReadValue(ds, id++, BooleanType.get().valueOf(false));
    testWriteReadValue(ds, id++, BooleanType.get().nullValue());
  }

  @Test
  public void test_locale_writer() throws Exception {
    Datasource ds = createDatasource();
    int id = 1;
    testWriteReadValue(ds, id++, LocaleType.get().valueOf("ca_FR"));
    testWriteReadValue(ds, id++, LocaleType.get().valueOf("fr"));
    testWriteReadValue(ds, id++, LocaleType.get().nullValue());
  }

  @Test
  public void test_binary_writer() throws Exception {
    Datasource ds = createDatasource();
    int id = 1;
    testWriteReadValue(ds, id++, BinaryType.get().valueOf("coucou".getBytes(Charsets.UTF_8)));
    testWriteReadValue(ds, id++, BinaryType.get().valueOf(new byte[2]));
    testWriteReadValue(ds, id++, BinaryType.get().nullValue());
  }

  @Test
  public void test_binary_sequence_writer() throws Exception {
    Datasource ds = createDatasource();

    Collection<Value> sequence = Lists.newArrayList();
    sequence.add(BinaryType.get().valueOf("coucou".getBytes(Charsets.UTF_8)));
    sequence.add(BinaryType.get().nullValue());
    sequence.add(BinaryType.get().valueOf(new byte[2]));

    testWriteReadValue(ds, 1, BinaryType.get().sequenceOf(sequence));
  }

  @Test
  public void test_point_writer() throws Exception {
    Datasource ds = createDatasource();
    int id = 1;
    testWriteReadValue(ds, id++, PointType.get().valueOf("[30.1,40.2]"));
    testWriteReadValue(ds, id++, PointType.get().nullValue());
  }

  @Test
  public void test_line_string_writer() throws Exception {
    Datasource ds = createDatasource();
    int id = 1;
    testWriteReadValue(ds, id++, LineStringType.get().valueOf("[[30.1,40.2],[21.3,44.55]]"));
    testWriteReadValue(ds, id++, LineStringType.get().nullValue());
  }

  @Test
  public void test_polygon_writer() throws Exception {
    Datasource ds = createDatasource();
    int id = 1;
    testWriteReadValue(ds, id++, PolygonType.get().valueOf("[[[30.1,40.2],[21.3,44.55],[30.1,40.2]]]"));
    testWriteReadValue(ds, id++,
        PolygonType.get().valueOf("[[[30.1,40.2],[21.3,44.55],[30.1,40.2]],[[1.1,2.2],[3.3,4.4],[1.1,2.2]]]"));
    testWriteReadValue(ds, id++, PolygonType.get().nullValue());
  }

  @Test
  public void test_batch_writer() throws Exception {
    Datasource ds = createDatasource();
    ((MongoDBDatasource)ds).setBatchSize(1000);
    Variable variable = Variable.Builder.newVariable("BATCHTEST", TextType.get(), "Participant").repeatable(false)
        .build();

    try(ValueTableWriter tableWriter = ds.createWriter(TABLE_TEST, variable.getEntityType())) {
      try(ValueTableWriter.VariableWriter variableWriter = tableWriter.writeVariables()) {
        variableWriter.writeVariable(variable);
      }
    }

    try(ValueTableWriter tableWriter = ds.createWriter(TABLE_TEST, variable.getEntityType())) {
      for(int i = 0; i < 999; i++) {
        VariableEntity entity = new VariableEntityBean("Participant", Integer.toString(i));

        try(ValueTableWriter.ValueSetWriter valueSetWriter = tableWriter.writeValueSet(entity)) {
          valueSetWriter.writeValue(variable, TextType.get().valueOf("test value " + i));
        }
      }
    }
  }

  @Test
  public void test_batch_writer_replace_existing() throws Exception {
    Datasource ds = createDatasource();
    ((MongoDBDatasource) ds).setBatchSize(1000);
    Variable variable = Variable.Builder.newVariable("BATCHTEST", TextType.get(), PARTICIPANT).repeatable(false)
        .build();

    try(ValueTableWriter tableWriter = ds.createWriter(TABLE_TEST, variable.getEntityType())) {
      try(ValueTableWriter.VariableWriter variableWriter = tableWriter.writeVariables()) {
        variableWriter.writeVariable(variable);
      }
    }

    try(ValueTableWriter tableWriter = ds.createWriter(TABLE_TEST, variable.getEntityType())) {
      for(int i = 0; i < 999; i++) {
        VariableEntity entity = new VariableEntityBean(PARTICIPANT, Integer.toString(i));

        try(ValueTableWriter.ValueSetWriter valueSetWriter = tableWriter.writeValueSet(entity)) {
          valueSetWriter.writeValue(variable, TextType.get().valueOf("test value " + i));
        }
      }

      for(int i = 0; i < 999; i++) { //replace existing documents
        VariableEntity entity = new VariableEntityBean(PARTICIPANT, Integer.toString(i));

        try(ValueTableWriter.ValueSetWriter valueSetWriter = tableWriter.writeValueSet(entity)) {
          valueSetWriter.writeValue(variable, TextType.get().valueOf("new test value " + i));
        }
      }
    }

    ValueTable table = ds.getValueTable(TABLE_TEST);
    ValueSet valueSet = table.getValueSet(new VariableEntityBean(PARTICIPANT, "1"));
    Value value = table.getValue(variable, valueSet);

    assertThat(value.getValue()).isEqualTo("new test value 1");
  }

  @SuppressWarnings({ "OverlyLongMethod", "PMD.NcssMethodCount" })
  @Test
  public void test_remove_variable() throws Exception {
    Datasource ds = prepareDatasource();

    ValueTable table = ds.getValueTable(TABLE_TEST);
    ValueSet valueSet = table.getValueSet(new VariableEntityBean(PARTICIPANT, "1"));
    Variable textVariable = table.getVariable(generateVariableName(BinaryType.get()));

    assertThat(table.getVariables()).hasSize(2);
    assertThat(textVariable).isNotNull();
    assertThat(table.getVariable(generateVariableName(IntegerType.get()))).isNotNull();

    Value tableLastUpdate = table.getTimestamps().getLastUpdate();
    Value valueSetLastUpdate = valueSet.getTimestamps().getLastUpdate();

    ValueTableWriter.VariableWriter variableWriter = ds.createWriter(TABLE_TEST, PARTICIPANT).writeVariables();
    variableWriter.removeVariable(textVariable);

    int tableLastUpdateCompare = ds.getValueTable(TABLE_TEST).getTimestamps().getLastUpdate()
        .compareTo(tableLastUpdate);
    assertThat(tableLastUpdateCompare).isGreaterThan(0);

    int valueSetLastUpdateCompare = table.getValueSet(new VariableEntityBean(PARTICIPANT, "1")).getTimestamps()
        .getLastUpdate().compareTo(valueSetLastUpdate);
    assertThat(valueSetLastUpdateCompare).isGreaterThan(0);

    try {
      table.getVariable(textVariable.getName());
      fail("Should throw NoSuchVariableException");
    } catch(NoSuchVariableException e) {
    }

    //TODO check in mongo that values were removed

  }

  @Test
  public void test_remove_valueset() throws Exception {
    Datasource ds = prepareDatasource();

    VariableEntity oneEntity = new VariableEntityBean(PARTICIPANT, "1");
    ValueTable table = ds.getValueTable(TABLE_TEST);
    assertThat(table.hasValueSet(oneEntity)).isTrue();

    Value tableLastUpdate = table.getTimestamps().getLastUpdate();

    ValueTableWriter.ValueSetWriter valueSetWriter = ds.createWriter(TABLE_TEST, PARTICIPANT).writeValueSet(oneEntity);
    valueSetWriter.remove();
    valueSetWriter.close();

    int tableLastUpdateCompare = ds.getValueTable(TABLE_TEST).getTimestamps().getLastUpdate()
        .compareTo(tableLastUpdate);
    assertThat(tableLastUpdateCompare).isGreaterThan(0);

    assertThat(table.hasValueSet(oneEntity)).isFalse();

    //TODO check in mongo that values were removed
  }

  @Test
  public void test_remove_all_valuesets() throws Exception {
    Datasource ds = prepareDatasource();

    VariableEntity oneEntity = new VariableEntityBean(PARTICIPANT, "1");
    ValueTable table = ds.getValueTable(TABLE_TEST);
    assertThat(table.hasValueSet(oneEntity)).isTrue();
    assertThat(table.canDropValueSets()).isTrue();

    Value tableLastUpdate = table.getTimestamps().getLastUpdate();

    table.dropValueSets();

    int tableLastUpdateCompare = ds.getValueTable(TABLE_TEST).getTimestamps().getLastUpdate()
        .compareTo(tableLastUpdate);
    assertThat(tableLastUpdateCompare).isGreaterThan(0);

    assertThat(table.hasValueSet(oneEntity)).isFalse();

    //TODO check in mongo that values were removed
  }

  private Datasource prepareDatasource() throws Exception {
    Datasource ds = createDatasource();
    int id = 1;
    testWriteReadValue(ds, id++, BinaryType.get().valueOf("tutu".getBytes(Charsets.UTF_8)));
    testWriteReadValue(ds, id++, BinaryType.get().valueOf(new byte[2]));
    testWriteReadValue(ds, id++, BinaryType.get().nullValue());
    testWriteReadValue(ds, id++, BinaryType.get().valueOf("toto".getBytes(Charsets.UTF_8)));

    id = 1;
    testWriteReadValue(ds, id++, IntegerType.get().valueOf("1"));
    testWriteReadValue(ds, id++, IntegerType.get().nullValue());
    testWriteReadValue(ds, id++, IntegerType.get().valueOf(Long.MAX_VALUE));
    testWriteReadValue(ds, id++, IntegerType.get().valueOf(Long.MIN_VALUE));
    return ds;
  }

  @Test
  @SuppressWarnings({ "OverlyLongMethod", "PMD.NcssMethodCount" })
  public void test_update_variable() throws IOException {

    Variable variable1 = Variable.Builder.newVariable("Variable to update", IntegerType.get(), PARTICIPANT) //
        .unit("kg").addCategory("1", "One", false) //
        .build();
    Variable variable2 = Variable.Builder.newVariable("Variable 2", IntegerType.get(), PARTICIPANT) //
        .addCategory("2", "Two", false) //
        .build();
    Variable variable3 = Variable.Builder.newVariable("Variable 3", IntegerType.get(), PARTICIPANT) //
        .build();
    ImmutableSet<Variable> variables = ImmutableSet.of(variable1, variable2, variable3);

    Datasource datasource1 = createDatasource();
    ValueTable generatedValueTable = new GeneratedValueTable(datasource1, variables, 10);
    MagmaEngine.get().addDatasource(datasource1);
    DatasourceCopier.Builder.newCopier().build().copy(generatedValueTable, TABLE_TEST, datasource1);

    Variable newVariable = Variable.Builder.newVariable("Variable to update", IntegerType.get(), PARTICIPANT) //
        .unit("g").addCategory("1", "One", false) //
        .addCategory("2", "Two", false) //
        .build();
    try(ValueTableWriter tableWriter = datasource1.createWriter(TABLE_TEST, PARTICIPANT);
        ValueTableWriter.VariableWriter variableWriter = tableWriter.writeVariables()) {
      variableWriter.writeVariable(newVariable);
    }

    Datasource datasource2 = createDatasource();
    ValueTable table = datasource2.getValueTable(TABLE_TEST);
    assertThat(table.getVariables()).hasSize(3);

    Variable variable = table.getVariable("Variable to update");
    assertThat(variable.getUnit()).isEqualTo(newVariable.getUnit());
    assertThat(variable.getCategories()).hasSize(newVariable.getCategories().size());

    List<Variable> foundVariables = Lists.newArrayList(table.getVariables());
    assertThat(foundVariables.get(0).getName()).isEqualTo(newVariable.getName());
    assertThat(foundVariables.get(1).getName()).isEqualTo(variable2.getName());
    assertThat(foundVariables.get(2).getName()).isEqualTo(variable3.getName());
  }

  @Test
  public void test_count_variables() throws IOException {

    List<Variable> variables = new ArrayList<>();
    for(int i = 0; i < 100; i++) {
      variables.add(Variable.Builder.newVariable("Variable " + i, IntegerType.get(), PARTICIPANT).build());
    }

    Datasource ds1 = createDatasource();
    ValueTable generatedValueTable = new GeneratedValueTable(ds1, variables, 50);
    MagmaEngine.get().addDatasource(ds1);
    DatasourceCopier.Builder.newCopier().build().copy(generatedValueTable, "table1", ds1);

    assertThat(createDatasource().getValueTable("table1").getVariableCount()).isEqualTo(100);
  }

  @Test
  public void test_count_valueSets() throws InterruptedException, IOException {

    ImmutableSet<Variable> variables = ImmutableSet.of(//
        Variable.Builder.newVariable("Test Variable", IntegerType.get(), PARTICIPANT).build(), //
        Variable.Builder.newVariable("Other Variable", DecimalType.get(), PARTICIPANT).build());

    Datasource ds = createDatasource();
    ValueTable generatedValueTable = new GeneratedValueTable(ds, variables, 100);
    MagmaEngine.get().addDatasource(ds);
    DatasourceCopier.Builder.newCopier().build().copy(generatedValueTable, "table1", ds);

    assertThat(createDatasource().getValueTable("table1").getValueSetCount()).isEqualTo(100);
  }

  @Test
  @Ignore
  // See http://jira.obiba.org/jira/browse/OPAL-2423
  public void test_get_binary_values_as_vector() throws IOException {
    Variable variable1 = Variable.Builder.newVariable("V1", BinaryType.get(), PARTICIPANT) //
        .build();
    ImmutableSet<Variable> variables = ImmutableSet.of(variable1);

    Datasource datasource1 = createDatasource();
    ValueTable generatedValueTable = new GeneratedValueTable(datasource1, variables, 1);
    MagmaEngine.get().addDatasource(datasource1);
    DatasourceCopier.Builder.newCopier().build().copy(generatedValueTable, TABLE_TEST, datasource1);

    VariableValueSource variableValueSource = MagmaEngine.get().getDatasource(datasource1.getName())
        .getValueTable(TABLE_TEST).getVariableValueSource("V1");
    assertThat(variableValueSource.getValueType().getName().equals(BinaryType.get().getName()));

    TreeSet<VariableEntity> entities = Sets.newTreeSet(generatedValueTable.getVariableEntities());
    Iterable<Value> values = variableValueSource.asVectorSource().getValues(entities);
    for(Value value : values) {
      assertThat(value.getValue()).isNotNull();
      assertThat(value.getValueType().getName()).isEqualTo(BinaryType.get().getName());
    }
  }

  @Test
  public void test_get_values_null_non_null() throws IOException {
    Variable variable1 = Variable.Builder.newVariable("V1", BinaryType.get(), PARTICIPANT) //
        .build();
    ImmutableSet<Variable> variables = ImmutableSet.of(variable1);

    Datasource datasource1 = createDatasource();
    ValueTable generatedValueTable = new GeneratedValueTable(datasource1, variables, 1);
    MagmaEngine.get().addDatasource(datasource1);
    DatasourceCopier.Builder.newCopier().build().copy(generatedValueTable, TABLE_TEST, datasource1);

    final ValueTable valueTable = MagmaEngine.get().getDatasource(datasource1.getName()).getValueTable(TABLE_TEST);
    TreeSet<VariableEntity> entities = Sets.newTreeSet(valueTable.getVariableEntities());
    final Variable variable = MagmaEngine.get().getDatasource(datasource1.getName()).getValueTable(TABLE_TEST)
        .getVariable("V1");

    Iterable<Value> values = Iterables.transform(entities, new Function<VariableEntity, Value>() {
      @Nullable
      @Override
      public Value apply(@Nullable VariableEntity input) {
        ValueSet valueSet = valueTable.getValueSet(input);
        Value value = valueTable.getValue(variable, valueSet);
        return value;
      }
    });

    for(Value value : values) {
      assertThat(value).isNotNull();
    }
  }

  private Datasource createDatasource() {
    DatasourceFactory factory = new MongoDBDatasourceFactory("ds-" + DB_TEST, dbUrl);
    Datasource ds = factory.create();
    Initialisables.initialise(ds);
    return ds;
  }

  private String generateVariableName(ValueType type) {
    return type.getName().toUpperCase();
  }

  private void testWriteReadValue(Datasource ds, int identifier, Value value) throws Exception {
    VariableEntity entity = new VariableEntityBean("Participant", Integer.toString(identifier));
    Variable variable = Variable.Builder
        .newVariable(generateVariableName(value.getValueType()), value.getValueType(), entity.getType())
        .repeatable(value.isSequence()).build();
    writeValue(ds, entity, variable, value);
    Thread.sleep(10);
    readValue(ds, entity, variable, value);
  }

  private void writeValue(Datasource ds, VariableEntity entity, Variable variable, Value value) {
    try(ValueTableWriter tableWriter = ds.createWriter(TABLE_TEST, variable.getEntityType())) {
      try(ValueTableWriter.VariableWriter variableWriter = tableWriter.writeVariables()) {
        variableWriter.writeVariable(variable);
      }
      try(ValueTableWriter.ValueSetWriter valueSetWriter = tableWriter.writeValueSet(entity)) {
        valueSetWriter.writeValue(variable, value);
      }
    }
  }

  private void readValue(Datasource ds, VariableEntity entity, Variable variable, Value expected) {
    ValueTable table = ds.getValueTable(TABLE_TEST);
    ValueSet valueSet = table.getValueSet(entity);
    Value value = table.getValue(variable, valueSet);

    assertThat(table.getEntityType()).isEqualTo(variable.getEntityType());
    assertThat(table.getVariable(variable.getName()).getValueType()).isEqualTo(variable.getValueType());
    if(expected.isSequence()) {
      ValueSequence valueSequence = value.asSequence();
      ValueSequence expectedSequence = expected.asSequence();
      int expectedSize = expectedSequence.getSize();
      assertThat(valueSequence.getSize()).isEqualTo(expectedSize);
      for(int i = 0; i < expectedSize; i++) {
        Value valueItem = valueSequence.get(i);
        Value expectedItem = expectedSequence.get(i);
        assertThat(valueItem.isNull()).isEqualTo(expectedItem.isNull());
        assertThat(valueItem.toString()).isEqualTo(expectedItem.toString());
      }
    } else {
      assertThat(value.isNull()).isEqualTo(expected.isNull());
      assertThat(value.toString()).isEqualTo(expected.toString());
    }
  }
}
