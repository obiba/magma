package org.obiba.magma.datasource.mongodb;

import java.io.IOException;
import java.util.Collection;

import org.junit.After;
import org.junit.Assume;
import org.junit.Before;
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
import org.obiba.magma.datasource.fs.FsDatasource;
import org.obiba.magma.datasource.generated.GeneratedValueTable;
import org.obiba.magma.support.DatasourceCopier;
import org.obiba.magma.support.Initialisables;
import org.obiba.magma.support.VariableEntityBean;
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
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.mongodb.MongoClient;

import static com.google.common.collect.Iterables.size;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

@SuppressWarnings({ "UnusedAssignment", "OverlyCoupledClass" })
public class MongoDBDatasourceTest {

  private static final String DB_TEST = "magma-test";

  private static final String DB_URL = "mongodb://localhost/" + DB_TEST;

  private static final String TABLE_TEST = "TABLE";

  private static final String PARTICIPANT = "Participant";

  private static final String ONYX_DATA_5_ZIP = "5-onyx-data.zip";

  @Before
  public void before() {
    // run test only if MongoDB is running
    Assume.assumeTrue(setupMongoDB());
  }

  private boolean setupMongoDB() {
    try {
      MongoClient client = new MongoClient();
      client.dropDatabase(DB_TEST);
      new MagmaEngine().extend(new MagmaXStreamExtension());
      return true;
    } catch(Exception e) {
      return false;
    }
  }

  @After
  public void after() {
    MagmaEngine.get().shutdown();
  }

  @Test
  public void test_writers() throws IOException {
    FsDatasource onyx = new FsDatasource("onyx", FileUtil.getFileFromResource(ONYX_DATA_5_ZIP));
    DatasourceFactory factory = new MongoDBDatasourceFactory("ds-" + DB_TEST, DB_URL);
    Datasource ds = factory.create();
    Initialisables.initialise(ds, onyx);

    DatasourceCopier.Builder.newCopier().build().copy(onyx, ds);
    assertThat(ds.getValueTable("AnkleBrachial").getVariableEntities(), hasSize(5));
    assertThat(size(ds.getValueTable("AnkleBrachial").getVariables()), is(21));
  }

  @Test
  public void test_restart_magma() throws IOException {
    FsDatasource onyx = new FsDatasource("onyx", FileUtil.getFileFromResource(ONYX_DATA_5_ZIP));
    Datasource datasource1 = new MongoDBDatasourceFactory("ds-" + DB_TEST, DB_URL).create();
    Initialisables.initialise(datasource1, onyx);
    DatasourceCopier.Builder.newCopier().build().copy(onyx, datasource1);

    Datasource datasource2 = new MongoDBDatasourceFactory("ds-" + DB_TEST, DB_URL).create();
    Initialisables.initialise(datasource2);
    assertThat(datasource2.getValueTable("AnkleBrachial").getVariableEntities(), hasSize(5));
    assertThat(size(datasource2.getValueTable("AnkleBrachial").getVariables()), is(21));
  }

  @Test
  public void test_integer_sequence_writer() throws IOException {
    Datasource ds = createDatasource();
    int id = 1;
    testWriteReadValue(ds, id++, IntegerType.get().sequenceOf("1,2,3,4"));
    testWriteReadValue(ds, id++, IntegerType.get().nullSequence());
  }

  @Test
  public void test_text_writer() throws IOException {
    Datasource ds = createDatasource();
    int id = 1;
    testWriteReadValue(ds, id++, TextType.get().valueOf("Il était déjà mort..."));
    testWriteReadValue(ds, id++, TextType.get().valueOf("!@#$%^&*()_+{}\":\\`~"));
    testWriteReadValue(ds, id++, TextType.get().nullValue());
  }

  @Test
  public void test_integer_writer() throws IOException {
    Datasource ds = createDatasource();
    int id = 1;
    testWriteReadValue(ds, id++, IntegerType.get().valueOf("1"));
    testWriteReadValue(ds, id++, IntegerType.get().valueOf("-1"));
    testWriteReadValue(ds, id++, IntegerType.get().nullValue());
    testWriteReadValue(ds, id++, IntegerType.get().valueOf(Long.MAX_VALUE));
    testWriteReadValue(ds, id++, IntegerType.get().valueOf(Long.MIN_VALUE));
  }

  @Test
  public void test_decimal_writer() throws IOException {
    Datasource ds = createDatasource();
    int id = 1;
    testWriteReadValue(ds, id++, DecimalType.get().valueOf("1.2"));
    testWriteReadValue(ds, id++, DecimalType.get().valueOf("-1.2"));
    testWriteReadValue(ds, id++, DecimalType.get().nullValue());
    testWriteReadValue(ds, id++, DecimalType.get().valueOf(Double.MAX_VALUE));
    testWriteReadValue(ds, id++, DecimalType.get().valueOf(Double.MIN_VALUE));
  }

  @Test
  public void test_date_writer() throws IOException {
    Datasource ds = createDatasource();
    int id = 1;
    testWriteReadValue(ds, id++, DateType.get().valueOf("1973-01-15"));
    testWriteReadValue(ds, id++, DateType.get().nullValue());
  }

  @Test
  public void test_dateTime_writer() throws IOException {
    Datasource ds = createDatasource();
    int id = 1;
    testWriteReadValue(ds, id++, DateTimeType.get().valueOf("1973-01-15 11:03:14"));
    testWriteReadValue(ds, id++, DateTimeType.get().nullValue());
  }

  @Test
  public void test_boolean_writer() throws IOException {
    Datasource ds = createDatasource();
    int id = 1;
    testWriteReadValue(ds, id++, BooleanType.get().valueOf(true));
    testWriteReadValue(ds, id++, BooleanType.get().valueOf(false));
    testWriteReadValue(ds, id++, BooleanType.get().nullValue());
  }

  @Test
  public void test_locale_writer() throws IOException {
    Datasource ds = createDatasource();
    int id = 1;
    testWriteReadValue(ds, id++, LocaleType.get().valueOf("ca_FR"));
    testWriteReadValue(ds, id++, LocaleType.get().valueOf("fr"));
    testWriteReadValue(ds, id++, LocaleType.get().nullValue());
  }

  @Test
  public void test_binary_writer() throws IOException {
    Datasource ds = createDatasource();
    int id = 1;
    testWriteReadValue(ds, id++, BinaryType.get().valueOf("coucou".getBytes(Charsets.UTF_8)));
    testWriteReadValue(ds, id++, BinaryType.get().valueOf(new byte[2]));
    testWriteReadValue(ds, id++, BinaryType.get().nullValue());
  }

  @Test
  public void test_binary_sequence_writer() throws IOException {
    Datasource ds = createDatasource();

    Collection<Value> sequence = Lists.newArrayList();
    sequence.add(BinaryType.get().valueOf("coucou".getBytes(Charsets.UTF_8)));
    sequence.add(BinaryType.get().nullValue());
    sequence.add(BinaryType.get().valueOf(new byte[2]));

    testWriteReadValue(ds, 1, BinaryType.get().sequenceOf(sequence));
  }

  @Test
  public void test_point_writer() throws IOException {
    Datasource ds = createDatasource();
    int id = 1;
    testWriteReadValue(ds, id++, PointType.get().valueOf("[30.1,40.2]"));
    testWriteReadValue(ds, id++, PointType.get().nullValue());
  }

  @Test
  public void test_line_string_writer() throws IOException {
    Datasource ds = createDatasource();
    int id = 1;
    testWriteReadValue(ds, id++, LineStringType.get().valueOf("[[30.1,40.2],[21.3,44.55]]"));
    testWriteReadValue(ds, id++, LineStringType.get().nullValue());
  }

  @Test
  public void test_polygon_writer() throws IOException {
    Datasource ds = createDatasource();
    int id = 1;
    testWriteReadValue(ds, id++, PolygonType.get().valueOf("[[[30.1,40.2],[21.3,44.55],[30.1,40.2]]]"));
    testWriteReadValue(ds, id++,
        PolygonType.get().valueOf("[[[30.1,40.2],[21.3,44.55],[30.1,40.2]],[[1.1,2.2],[3.3,4.4],[1.1,2.2]]]"));
    testWriteReadValue(ds, id++, PolygonType.get().nullValue());
  }

  @SuppressWarnings({ "ReuseOfLocalVariable", "OverlyLongMethod", "PMD.NcssMethodCount" })
  @Test
  public void test_remove_variable() throws IOException {
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

    ValueTable table = ds.getValueTable(TABLE_TEST);
    ValueSet valueSet = table.getValueSet(new VariableEntityBean(PARTICIPANT, "1"));
    Variable textVariable = table.getVariable(generateVariableName(BinaryType.get()));

    assertThat(size(table.getVariables()), is(2));
    assertThat(textVariable, notNullValue());
    assertThat(table.getVariable(generateVariableName(IntegerType.get())), notNullValue());

    Value tableLastUpdate = table.getTimestamps().getLastUpdate();
    Value valueSetLastUpdate = valueSet.getTimestamps().getLastUpdate();

    ValueTableWriter.VariableWriter variableWriter = ds.createWriter(TABLE_TEST, PARTICIPANT).writeVariables();
    variableWriter.removeVariable(textVariable);

    int tableLastUpdateCompare = ds.getValueTable(TABLE_TEST).getTimestamps().getLastUpdate()
        .compareTo(tableLastUpdate);
    assertThat(tableLastUpdateCompare > 0, is(true));

    int valueSetLastUpdateCompare = table.getValueSet(new VariableEntityBean(PARTICIPANT, "1")).getTimestamps()
        .getLastUpdate().compareTo(valueSetLastUpdate);
    assertThat(valueSetLastUpdateCompare > 0, is(true));

    try {
      table.getVariable(textVariable.getName());
      fail("Should throw NoSuchVariableException");
    } catch(NoSuchVariableException e) {
    }

    //TODO check in mongo that values were removed

  }

  //@Test
  public void test_update_variable() throws IOException {

    ImmutableSet<Variable> variables = ImmutableSet.of(//
        Variable.Builder.newVariable("Variable to update", IntegerType.get(), PARTICIPANT) //
            .addCategory("1", "One", false) //
            .build(), //
        Variable.Builder.newVariable("Other Variable", IntegerType.get(), PARTICIPANT) //
            .addCategory("2", "Two", false) //
            .build());

    Datasource ds = createDatasource();
    ValueTable generatedValueTable = new GeneratedValueTable(ds, variables, 10);
    MagmaEngine.get().addDatasource(ds);
    DatasourceCopier.Builder.newCopier().build().copy(generatedValueTable, TABLE_TEST, ds);

    ValueTable table = ds.getValueTable(TABLE_TEST);
    Variable variable = table.getVariable("Variable to update");

    //ds.createWriter(TABLE_TEST, PARTICIPANT).writeVariables().writeVariable();

  }

  private Datasource createDatasource() {
    DatasourceFactory factory = new MongoDBDatasourceFactory("ds-" + DB_TEST, DB_URL);
    Datasource ds = factory.create();
    Initialisables.initialise(ds);
    return ds;
  }

  private String generateVariableName(ValueType type) {
    return type.getName().toUpperCase();
  }

  private void testWriteReadValue(Datasource ds, int identifier, Value value) throws IOException {
    VariableEntity entity = new VariableEntityBean("Participant", Integer.toString(identifier));
    Variable variable = Variable.Builder
        .newVariable(generateVariableName(value.getValueType()), value.getValueType(), entity.getType())
        .repeatable(value.isSequence()).build();
    writeValue(ds, entity, variable, value);
    readValue(ds, entity, variable, value);
  }

  private void writeValue(Datasource ds, VariableEntity entity, Variable variable, Value value) throws IOException {
    ValueTableWriter tableWriter = ds.createWriter(TABLE_TEST, variable.getEntityType());
    ValueTableWriter.VariableWriter variableWriter = tableWriter.writeVariables();
    variableWriter.writeVariable(variable);
    variableWriter.close();
    ValueTableWriter.ValueSetWriter valueSetWriter = tableWriter.writeValueSet(entity);
    valueSetWriter.writeValue(variable, value);
    valueSetWriter.close();
    tableWriter.close();
  }

  private void readValue(Datasource ds, VariableEntity entity, Variable variable, Value expected) {
    ValueTable table = ds.getValueTable(TABLE_TEST);
    ValueSet valueSet = table.getValueSet(entity);
    Value value = table.getValue(variable, valueSet);

    assertThat(table.getEntityType(), is(variable.getEntityType()));
    assertThat(table.getVariable(variable.getName()).getValueType(), is(variable.getValueType()));
    if(expected.isSequence()) {
      ValueSequence valueSequence = value.asSequence();
      ValueSequence expectedSequence = expected.asSequence();
      int expectedSize = expectedSequence.getSize();
      assertThat(valueSequence.getSize(), is(expectedSize));
      for(int i = 0; i < expectedSize; i++) {
        Value valueItem = valueSequence.get(i);
        Value expectedItem = expectedSequence.get(i);
        assertThat(valueItem.isNull(), is(expectedItem.isNull()));
        assertThat(valueItem.toString(), is(expectedItem.toString()));
      }
    } else {
      assertThat(value.isNull(), is(expected.isNull()));
      assertThat(value.toString(), is(expected.toString()));
    }
  }
}
