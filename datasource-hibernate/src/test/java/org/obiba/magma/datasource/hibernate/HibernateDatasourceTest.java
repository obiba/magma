package org.obiba.magma.datasource.hibernate;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.obiba.magma.MagmaEngine;
import org.obiba.magma.ValueTableWriter;
import org.obiba.magma.Variable;
import org.obiba.magma.datasource.hibernate.support.LocalSessionFactoryProvider;
import org.obiba.magma.type.IntegerType;
import org.obiba.magma.type.TextType;

public class HibernateDatasourceTest {

  @Before
  public void createMetaEngine() {
    new MagmaEngine();
  }

  @After
  public void shutdownMetaEngine() {
    MagmaEngine.get().shutdown();
  }

  @Test
  public void testSimple() throws Exception {
    LocalSessionFactoryProvider provider = new LocalSessionFactoryProvider("org.hsqldb.jdbcDriver", "jdbc:hsqldb:file:target/integration-hibernate.db;shutdown=true", "sa", "", "org.hibernate.dialect.HSQLDialect");
    provider.initialise();
    HibernateDatasource ds = new HibernateDatasource("jpa_test_simple", provider.getSessionFactory());

    try {
      provider.getSessionFactory().getCurrentSession().beginTransaction();
      MagmaEngine.get().addDatasource(ds);

      ValueTableWriter vtWriter = ds.createWriter("tata", "Participant");

      vtWriter.writeVariables().writeVariable(Variable.Builder.newVariable("Var1", TextType.get(), "Participant").build());
      vtWriter.writeVariables().writeVariable(Variable.Builder.newVariable("Var2", IntegerType.get(), "Participant").build());
      vtWriter.close();
      MagmaEngine.get().removeDatasource(ds);
      provider.getSessionFactory().getCurrentSession().getTransaction().commit();
    } catch(Exception e) {
      throw e;
    }

  }
}
