package org.obiba.magma.datasource.jdbc;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Test;
import org.obiba.magma.Datasource;

import com.thoughtworks.xstream.XStream;

public class JdbcDatasourceFactoryTest {

  @Test
  public void testDeserializeFactoryAndCreateDatasource() {
    JdbcDatasourceFactory factory = (JdbcDatasourceFactory) (new XStream()).fromXML(JdbcDatasourceFactoryTest.class.getResourceAsStream("factory.xml"));
    Datasource jdbcDatasource = factory.create();
    jdbcDatasource.initialise();

    assertNotNull(jdbcDatasource);
    assertEquals("my-datasource", jdbcDatasource.getName());
    assertEquals(0, jdbcDatasource.getValueTables().size());
  }
}
