package org.obiba.magma.datasource.mongodb;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.Collection;

import org.junit.After;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;
import org.obiba.core.util.FileUtil;
import org.obiba.magma.Datasource;
import org.obiba.magma.MagmaEngine;
import org.obiba.magma.Value;
import org.obiba.magma.ValueSet;
import org.obiba.magma.ValueTable;
import org.obiba.magma.ValueTableWriter;
import org.obiba.magma.Variable;
import org.obiba.magma.VariableEntity;
import org.obiba.magma.datasource.fs.FsDatasource;
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

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.mongodb.MongoClient;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

@SuppressWarnings("UnusedAssignment")
public class MongoDBDatasourceTest {

  private static final String DB_TEST = "magma-test";

  private static final String TABLE_TEST = "TABLE";

  @Before
  public void before() throws UnknownHostException {
    // run test only if MongoDB is running
    Assume.assumeTrue(setupMongoDB());
  }

  private boolean setupMongoDB() throws UnknownHostException {
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
  public void testWriters() throws IOException {
    FsDatasource onyx = new FsDatasource("onyx", FileUtil.getFileFromResource("20-onyx-data.zip"));
    MongoDBDatasourceFactory factory = new MongoDBDatasourceFactory();
    factory.setName("ds-" + DB_TEST);
    factory.setDatabase(DB_TEST);
    Datasource ds = factory.create();
    Initialisables.initialise(ds, onyx);

    DatasourceCopier copier = DatasourceCopier.Builder.newCopier().build();
    copier.copy(onyx, ds);

    assertThat(ds.getValueTable("AnkleBrachial").getVariableEntities().size(), is(20));
    assertThat(Iterables.size(ds.getValueTable("AnkleBrachial").getVariables()), is(21));
  }

  @Test
  public void testIntegerSequenceWriter() throws IOException {
    Datasource ds = createDatasource();
    int id = 1;
    testWriteReadValue(ds, id++, IntegerType.get().sequenceOf("1,2,3,4"));
    testWriteReadValue(ds, id++, IntegerType.get().nullSequence());
  }

  @Test
  public void testTextWriter() throws IOException {
    Datasource ds = createDatasource();
    int id = 1;
    testWriteReadValue(ds, id++, TextType.get().valueOf("Il était déjà mort..."));
    testWriteReadValue(ds, id++, TextType.get().valueOf("!@#$%^&*()_+{}\":\\`~"));
    testWriteReadValue(ds, id++, TextType.get().nullValue());
  }

  @Test
  public void testIntegerWriter() throws IOException {
    Datasource ds = createDatasource();
    int id = 1;
    testWriteReadValue(ds, id++, IntegerType.get().valueOf("1"));
    testWriteReadValue(ds, id++, IntegerType.get().valueOf("-1"));
    testWriteReadValue(ds, id++, IntegerType.get().nullValue());
    testWriteReadValue(ds, id++, IntegerType.get().valueOf(Long.MAX_VALUE));
    testWriteReadValue(ds, id++, IntegerType.get().valueOf(Long.MIN_VALUE));
  }

  @Test
  public void testDecimalWriter() throws IOException {
    Datasource ds = createDatasource();
    int id = 1;
    testWriteReadValue(ds, id++, DecimalType.get().valueOf("1.2"));
    testWriteReadValue(ds, id++, DecimalType.get().valueOf("-1.2"));
    testWriteReadValue(ds, id++, DecimalType.get().nullValue());
    testWriteReadValue(ds, id++, DecimalType.get().valueOf(Double.MAX_VALUE));
    testWriteReadValue(ds, id++, DecimalType.get().valueOf(Double.MIN_VALUE));
  }

  @Test
  public void testDateWriter() throws IOException {
    Datasource ds = createDatasource();
    int id = 1;
    testWriteReadValue(ds, id++, DateType.get().valueOf("1973-01-15"));
    testWriteReadValue(ds, id++, DateType.get().nullValue());
  }

  @Test
  public void testDateTimeWriter() throws IOException {
    Datasource ds = createDatasource();
    int id = 1;
    testWriteReadValue(ds, id++, DateTimeType.get().valueOf("1973-01-15 11:03:14"));
    testWriteReadValue(ds, id++, DateTimeType.get().nullValue());
  }

  @Test
  public void testBooleanWriter() throws IOException {
    Datasource ds = createDatasource();
    int id = 1;
    testWriteReadValue(ds, id++, BooleanType.get().valueOf(true));
    testWriteReadValue(ds, id++, BooleanType.get().valueOf(false));
    testWriteReadValue(ds, id++, BooleanType.get().nullValue());
  }

  @Test
  public void testLocaleWriter() throws IOException {
    Datasource ds = createDatasource();
    int id = 1;
    testWriteReadValue(ds, id++, LocaleType.get().valueOf("ca_FR"));
    testWriteReadValue(ds, id++, LocaleType.get().valueOf("fr"));
    testWriteReadValue(ds, id++, LocaleType.get().nullValue());
  }

  @Test
  public void testBinaryWriter() throws IOException {
    Datasource ds = createDatasource();
    int id = 1;
    testWriteReadValue(ds, id++, BinaryType.get().valueOf("coucou".getBytes()));
    testWriteReadValue(ds, id++, BinaryType.get().valueOf(new byte[2]));
    testWriteReadValue(ds, id++, BinaryType.get().nullValue());

    Collection<Value> sequence = Lists.newArrayList();
    sequence.add(BinaryType.get().valueOf("coucou".getBytes()));
    sequence.add(BinaryType.get().nullValue());
    sequence.add(BinaryType.get().valueOf(new byte[2]));
    testWriteReadValue(ds, id++, BinaryType.get().sequenceOf(sequence));
  }

  @Test
  public void testPointWriter() throws IOException {
    Datasource ds = createDatasource();
    int id = 1;
    testWriteReadValue(ds, id++, PointType.get().valueOf("[30.1,40.2]"));
    testWriteReadValue(ds, id++, PointType.get().nullValue());
  }

  @Test
  public void testLineStringWriter() throws IOException {
    Datasource ds = createDatasource();
    int id = 1;
    testWriteReadValue(ds, id++, LineStringType.get().valueOf("[[30.1,40.2],[21.3,44.55]]"));
    testWriteReadValue(ds, id++, LineStringType.get().nullValue());
  }

  @Test
  public void testPolygonWriter() throws IOException {
    Datasource ds = createDatasource();
    int id = 1;
    testWriteReadValue(ds, id++, PolygonType.get().valueOf("[[[30.1,40.2],[21.3,44.55],[30.1,40.2]]]"));
    testWriteReadValue(ds, id++,
        PolygonType.get().valueOf("[[[30.1,40.2],[21.3,44.55],[30.1,40.2]],[[1.1,2.2],[3.3,4.4],[1.1,2.2]]]"));
    testWriteReadValue(ds, id++, PolygonType.get().nullValue());
  }

  @Test
  @Ignore("need mongodb server to be available")
  public void testRemoveVariable() throws IOException {
    Datasource ds = createDatasource();
    int id = 1;
    testWriteReadValue(ds, id++, TextType.get().valueOf("toto"));
    testWriteReadValue(ds, id++, TextType.get().valueOf("tutu"));
    testWriteReadValue(ds, id++, TextType.get().nullValue());
    testWriteReadValue(ds, id++, TextType.get().valueOf("tata"));
    id = 1;
    testWriteReadValue(ds, id++, IntegerType.get().valueOf("1"));
    testWriteReadValue(ds, id++, IntegerType.get().nullValue());
    testWriteReadValue(ds, id++, IntegerType.get().valueOf(Long.MAX_VALUE));
    testWriteReadValue(ds, id++, IntegerType.get().valueOf(Long.MIN_VALUE));
    ValueTableWriter.VariableWriter vw = ds.createWriter(TABLE_TEST, "Participant").writeVariables();
    vw.removeVariable(ds.getValueTable(TABLE_TEST).getVariable("TEXT"));
  }

  private Datasource createDatasource() {
    MongoDBDatasourceFactory factory = new MongoDBDatasourceFactory();
    factory.setName("ds-" + DB_TEST);
    factory.setDatabase(DB_TEST);
    Datasource ds = factory.create();
    Initialisables.initialise(ds);
    return ds;
  }

  private void testWriteReadValue(Datasource ds, int identifier, Value value) throws IOException {
    VariableEntity entity = new VariableEntityBean("Participant", Integer.toString(identifier));
    writeValue(ds, entity, value);
    readValue(ds, entity, value);
  }

  private void writeValue(Datasource ds, VariableEntity entity, Value value) throws IOException {
    Variable variable = Variable.Builder
        .newVariable(value.getValueType().getName().toUpperCase(), value.getValueType(), entity.getType())
        .repeatable(value.isSequence()).build();
    writeValue(ds, entity, variable, value);
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

  private void readValue(Datasource ds, VariableEntity entity, Value expected) {
    Variable variable = Variable.Builder
        .newVariable(expected.getValueType().getName().toUpperCase(), expected.getValueType(), entity.getType())
        .build();
    readValue(ds, entity, variable, expected);
  }

  private void readValue(Datasource ds, VariableEntity entity, Variable variable, Value expected) {
    ValueTable table = ds.getValueTable(TABLE_TEST);
    ValueSet valueSet = table.getValueSet(entity);
    Value value = table.getValue(variable, valueSet);

    assertThat(table.getEntityType(), is(variable.getEntityType()));
    assertThat(table.getVariable(variable.getName()).getValueType(), is(variable.getValueType()));
    if(expected.isSequence()) {
      assertThat(value.asSequence().getSize(), is(expected.asSequence().getSize()));
    }
    assertThat(value.toString(), is(expected.toString()));
  }
}
