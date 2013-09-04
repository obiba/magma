package org.obiba.magma.datasource.mongodb;

import java.io.IOException;
import java.net.UnknownHostException;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
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
import com.mongodb.MongoClient;

import junit.framework.Assert;

@SuppressWarnings("UnusedAssignment")
public class MongoDBDatasourceTest {

  private static final String DB_TEST = "magma-test";

  private static final String TABLE_TEST = "TABLE";

  @Before
  public void before() throws UnknownHostException {
    MongoClient client = new MongoClient();
    client.dropDatabase(DB_TEST);
    new MagmaEngine().extend(new MagmaXStreamExtension());
  }

  @After
  public void after() {
    MagmaEngine.get().shutdown();
  }

  @Test
  @Ignore("need mongodb server to be available")
  public void testWriters() throws IOException {
    FsDatasource onyx = new FsDatasource("onyx", FileUtil.getFileFromResource("20-onyx-data.zip"));
    MongoDBDatasourceFactory factory = new MongoDBDatasourceFactory();
    factory.setName("ds-" + DB_TEST);
    factory.setDatabase(DB_TEST);
    Datasource ds = factory.create();
    Initialisables.initialise(ds, onyx);

    DatasourceCopier copier = DatasourceCopier.Builder.newCopier().build();
    copier.copy(onyx, ds);

    Assert.assertEquals(20, ds.getValueTable("AnkleBrachial").getVariableEntities().size());
    Assert.assertEquals(21, Iterables.size(ds.getValueTable("AnkleBrachial").getVariables()));
  }

  @Test
  @Ignore("need mongodb server to be available")
  public void testIntegerSequenceWriter() throws IOException {
    Datasource ds = createDatasource();
    int id = 1;
    testWriteReadValue(ds, id++, IntegerType.get().sequenceOf("1,2,3,4"));
    testWriteReadValue(ds, id++, IntegerType.get().nullSequence());
  }

  @Test
  @Ignore("need mongodb server to be available")
  public void testTextWriter() throws IOException {
    Datasource ds = createDatasource();
    int id = 1;
    testWriteReadValue(ds, id++, TextType.get().valueOf("Il était déjà mort..."));
    testWriteReadValue(ds, id++, TextType.get().valueOf("!@#$%^&*()_+{}\":\\`~"));
    testWriteReadValue(ds, id++, TextType.get().nullValue());
  }

  @Test
  @Ignore("need mongodb server to be available")
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
  @Ignore("need mongodb server to be available")
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
  @Ignore("need mongodb server to be available")
  public void testDateWriter() throws IOException {
    Datasource ds = createDatasource();
    int id = 1;
    testWriteReadValue(ds, id++, DateType.get().valueOf("1973-01-15"));
    testWriteReadValue(ds, id++, DateType.get().nullValue());
  }

  @Test
  @Ignore("need mongodb server to be available")
  public void testDateTimeWriter() throws IOException {
    Datasource ds = createDatasource();
    int id = 1;
    testWriteReadValue(ds, id++, DateTimeType.get().valueOf("1973-01-15 11:03:14"));
    testWriteReadValue(ds, id++, DateTimeType.get().nullValue());
  }

  @Test
  @Ignore("need mongodb server to be available")
  public void testBooleanWriter() throws IOException {
    Datasource ds = createDatasource();
    int id = 1;
    testWriteReadValue(ds, id++, BooleanType.get().valueOf(true));
    testWriteReadValue(ds, id++, BooleanType.get().valueOf(false));
    testWriteReadValue(ds, id++, BooleanType.get().nullValue());
  }

  @Test
  @Ignore("need mongodb server to be available")
  public void testLocaleWriter() throws IOException {
    Datasource ds = createDatasource();
    int id = 1;
    testWriteReadValue(ds, id++, LocaleType.get().valueOf("ca_FR"));
    testWriteReadValue(ds, id++, LocaleType.get().valueOf("fr"));
    testWriteReadValue(ds, id++, LocaleType.get().nullValue());
  }

  @Test
  @Ignore("need mongodb server to be available")
  public void testBinaryWriter() throws IOException {
    Datasource ds = createDatasource();
    int id = 1;
    testWriteReadValue(ds, id++, BinaryType.get().valueOf("coucou".getBytes()));
    testWriteReadValue(ds, id++, BinaryType.get().valueOf(new byte[2]));
    testWriteReadValue(ds, id++, BinaryType.get().nullValue());
  }

  @Test
  @Ignore("need mongodb server to be available")
  public void testPointWriter() throws IOException {
    Datasource ds = createDatasource();
    int id = 1;
    testWriteReadValue(ds, id++, PointType.get().valueOf("[30.1,40.2]"));
    testWriteReadValue(ds, id++, PointType.get().nullValue());
  }

  @Test
  @Ignore("need mongodb server to be available")
  public void testLineStringWriter() throws IOException {
    Datasource ds = createDatasource();
    int id = 1;
    testWriteReadValue(ds, id++, LineStringType.get().valueOf("[[30.1,40.2],[21.3,44.55]]"));
    testWriteReadValue(ds, id++, LineStringType.get().nullValue());
  }

  @Test
  @Ignore("need mongodb server to be available")
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
    writeValue(ds, Integer.toString(identifier), value);
    readValue(ds, Integer.toString(identifier), value);
  }

  private void writeValue(Datasource ds, String identifier, Value value) throws IOException {
    Variable variable = Variable.Builder
        .newVariable(value.getValueType().getName().toUpperCase(), value.getValueType(), "Participant")
        .repeatable(value.isSequence()).build();
    VariableEntity entity = new VariableEntityBean("Participant", identifier);
    writeValue(ds, entity, variable, value);
  }

  private void writeValue(Datasource ds, VariableEntity entity, Variable variable, Value value) throws IOException {
    ValueTableWriter vtw = ds.createWriter(TABLE_TEST, variable.getEntityType());
    ValueTableWriter.VariableWriter vw = vtw.writeVariables();
    vw.writeVariable(variable);
    vw.close();
    ValueTableWriter.ValueSetWriter vsw = vtw.writeValueSet(entity);
    vsw.writeValue(variable, value);
    vsw.close();
    vtw.close();
  }

  private void readValue(Datasource ds, String identifier, Value expected) {
    Variable variable = Variable.Builder
        .newVariable(expected.getValueType().getName().toUpperCase(), expected.getValueType(), "Participant").build();
    VariableEntity entity = new VariableEntityBean("Participant", identifier);
    readValue(ds, entity, variable, expected);
  }

  private void readValue(Datasource ds, VariableEntity entity, Variable variable, Value expected) {
    ValueTable vt = ds.getValueTable(TABLE_TEST);
    ValueSet vs = vt.getValueSet(entity);
    Value value = vt.getValue(variable, vs);

    Assert.assertEquals(variable.getEntityType(), vt.getEntityType());
    Assert.assertEquals(variable.getValueType(), vt.getVariable(variable.getName()).getValueType());
    if(expected.isSequence()) {
      Assert.assertEquals(expected.asSequence().getSize(), value.asSequence().getSize());
    }
    Assert.assertEquals(expected.toString(), value.toString());
  }
}
