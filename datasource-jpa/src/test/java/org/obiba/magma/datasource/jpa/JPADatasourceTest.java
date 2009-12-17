package org.obiba.magma.datasource.jpa;

import java.io.IOException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.obiba.magma.MagmaEngine;
import org.obiba.magma.ValueTableWriter;
import org.obiba.magma.Variable;
import org.obiba.magma.type.IntegerType;
import org.obiba.magma.type.TextType;

public class JPADatasourceTest {

  @Before
  public void createMetaEngine() {
    new MagmaEngine();
  }

  @After
  public void shutdownMetaEngine() {
    MagmaEngine.get().shutdown();
  }

  @Test
  public void testSimple() throws IOException {
    JPADatasourceFactory factory = new JPADatasourceFactory("org.hsqldb.jdbcDriver", "jdbc:hsqldb:file:target/jpa_test_simple;shutdown=true", "sa", "", "org.hibernate.dialect.HSQLDialect");
    JPADatasource ds = factory.create("jpa_test_simple");

    MagmaEngine.get().addDatasource(ds);

    ValueTableWriter vtWriter = ds.createWriter("tata", "Participant");

    vtWriter.writeVariables().writeVariable(Variable.Builder.newVariable("Var1", TextType.get(), "Participant").build());
    vtWriter.writeVariables().writeVariable(Variable.Builder.newVariable("Var2", IntegerType.get(), "Participant").build());
    vtWriter.close();
    MagmaEngine.get().removeDatasource(ds);

  }
}
