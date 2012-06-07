package org.obiba.magma.datasource.jdbc;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.IOException;

import org.junit.Test;
import org.obiba.magma.Datasource;
import org.obiba.magma.ValueTableWriter;
import org.obiba.magma.ValueTableWriter.VariableWriter;
import org.obiba.magma.Variable;
import org.obiba.magma.test.AbstractMagmaTest;
import org.obiba.magma.type.IntegerType;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.converters.reflection.PureJavaReflectionProvider;

public class JdbcDatasourceFactoryTest extends AbstractMagmaTest {

  @Test
  public void testDeserializeFactoryAndCreateDatasource() {
    JdbcDatasourceFactory factory = readSettings("factory.xml");
    Datasource jdbcDatasource = factory.create();
    jdbcDatasource.initialise();

    assertNotNull(jdbcDatasource);
    assertEquals("my-datasource", jdbcDatasource.getName());
  }

  @Test
  public void testFactoryWithDefaultTimestampColumnNames() throws IOException {
    JdbcDatasourceFactory factory = readSettings("factory-with-timestamps.xml");
    Datasource jdbcDatasource = factory.create();
    jdbcDatasource.initialise();

    assertNotNull(jdbcDatasource);
    assertEquals("my-datasource", jdbcDatasource.getName());

    ValueTableWriter writer = jdbcDatasource.createWriter("newTable", "test");
    VariableWriter vw = writer.writeVariables();
    vw.writeVariable(Variable.Builder.newVariable("TEST", IntegerType.get(), "test").build());
    vw.close();
    writer.close();

    JdbcValueTable table = (JdbcValueTable) jdbcDatasource.getValueTable("newTable");
    assertEquals("updated", table.getUpdatedTimestampColumnName());
    assertEquals("created", table.getCreatedTimestampColumnName());
  }

  @Test
  public void testFactoryWithOverridenTimestampColumnNames() throws IOException {
    JdbcDatasourceFactory factory = readSettings("factory-with-timestamps.xml");
    Datasource jdbcDatasource = factory.create();
    jdbcDatasource.initialise();

    assertNotNull(jdbcDatasource);
    assertEquals("my-datasource", jdbcDatasource.getName());

    ValueTableWriter writer = jdbcDatasource.createWriter("override_names", "test");
    VariableWriter vw = writer.writeVariables();
    vw.writeVariable(Variable.Builder.newVariable("TEST", IntegerType.get(), "test").build());
    vw.close();
    writer.close();

    JdbcValueTable table = (JdbcValueTable) jdbcDatasource.getValueTable("override_names");
    assertEquals("lastUpdate", table.getUpdatedTimestampColumnName());
    assertEquals("inserted", table.getCreatedTimestampColumnName());
  }

  private JdbcDatasourceFactory readSettings(String settings) {
    return (JdbcDatasourceFactory) new XStream(new PureJavaReflectionProvider()).fromXML(JdbcDatasourceFactoryTest.class.getResourceAsStream(settings));
  }
}
