package org.obiba.magma.js.methods;

import java.io.IOException;
import java.util.Collection;
import java.util.Properties;
import java.util.SortedSet;
import java.util.TreeSet;

import org.hibernate.Transaction;
import org.hibernate.cfg.Environment;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.obiba.magma.Datasource;
import org.obiba.magma.MagmaEngine;
import org.obiba.magma.Value;
import org.obiba.magma.ValueTable;
import org.obiba.magma.ValueTableWriter;
import org.obiba.magma.Variable;
import org.obiba.magma.VariableEntity;
import org.obiba.magma.datasource.generated.GeneratedValueTable;
import org.obiba.magma.datasource.hibernate.HibernateDatasource;
import org.obiba.magma.datasource.hibernate.support.LocalSessionFactoryProvider;
import org.obiba.magma.js.AbstractJsTest;
import org.obiba.magma.js.views.VariablesClause;
import org.obiba.magma.support.DatasourceCopier;
import org.obiba.magma.type.IntegerType;
import org.obiba.magma.views.DefaultViewManagerImpl;
import org.obiba.magma.views.MemoryViewPersistenceStrategy;
import org.obiba.magma.views.View;
import org.obiba.magma.views.ViewManager;
import org.obiba.magma.xstream.MagmaXStreamExtension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Stopwatch;
import com.google.common.collect.Lists;

import static org.obiba.magma.Variable.Builder.newVariable;

@SuppressWarnings({ "OverlyLongMethod", "PMD.NcssMethodCount", "OverlyCoupledClass" })
public class GlobalMethodsHibernateTest extends AbstractJsTest {

  private static final Logger log = LoggerFactory.getLogger(GlobalMethodsHibernateTest.class);

  private static final String PARTICIPANT = "Participant";

  private static final String DATASOURCE = "ds";

  private static final String TABLE = "table";

  private static final String VARIABLE_WEIGHT = "weight";

  private ViewManager viewManager;

  private LocalSessionFactoryProvider provider;

  public GlobalMethodsHibernateTest() {
    System.setProperty("net.sf.ehcache.skipUpdateCheck", "true");
  }

  @Before
  @Override
  public void before() {
    super.before();
    provider = newHsqlProvider("globalMethods-test");
    viewManager = new DefaultViewManagerImpl(new MemoryViewPersistenceStrategy());
  }

  @After
  @Override
  public void after() {
    super.after();
    provider.getSessionFactory().close();
  }

  @Override
  protected MagmaEngine newEngine() {
    MagmaEngine magmaEngine = super.newEngine();
    magmaEngine.extend(new MagmaXStreamExtension());
    return magmaEngine;
  }

  private LocalSessionFactoryProvider newHsqlProvider(String testName) {
    LocalSessionFactoryProvider newProvider = new LocalSessionFactoryProvider("org.hsqldb.jdbcDriver",
        "jdbc:hsqldb:mem:" + testName + ";shutdown=true", "sa", "", "org.hibernate.dialect.HSQLDialect");
    Properties p = new Properties();
    p.setProperty(Environment.CACHE_PROVIDER, "org.hibernate.cache.HashtableCacheProvider");
    newProvider.setProperties(p);
    newProvider.initialise();
    return newProvider;
  }

//  private LocalSessionFactoryProvider newMysqlProvider() {
//    LocalSessionFactoryProvider newProvider = new LocalSessionFactoryProvider("com.mysql.jdbc.Driver",
//        "jdbc:mysql://localhost:3306/magma_test?characterEncoding=UTF-8", "root", "1234",
//        "org.hibernate.dialect.MySQL5InnoDBDialect");
//    Properties p = new Properties();
//    p.setProperty(Environment.CACHE_PROVIDER, "org.hibernate.cache.HashtableCacheProvider");
//    newProvider.setProperties(p);
//    newProvider.initialise();
//    return newProvider;
//  }

  private Datasource getGeneratedDatasource() {
    HibernateDatasource datasource = new HibernateDatasource(DATASOURCE, provider.getSessionFactory());

    Variable variable = newVariable(VARIABLE_WEIGHT, IntegerType.get(), PARTICIPANT).build();
    ValueTable generatedValueTable = new GeneratedValueTable(datasource, Lists.newArrayList(variable), 1000);

    Datasource viewAwareDatasource = viewManager.decorate(datasource);
    MagmaEngine.get().addDatasource(viewAwareDatasource);

    try {
      DatasourceCopier.Builder.newCopier().build().copy(generatedValueTable, TABLE, datasource);
    } catch(IOException e) {
      throw new RuntimeException(e);
    }

    return viewAwareDatasource;
  }

  @Test
  public void test_$this_vector_performance() throws Exception {

    provider.getSessionFactory().getCurrentSession().beginTransaction();

    Datasource datasource = getGeneratedDatasource();
    ValueTable table = datasource.getValueTable(TABLE);
    int nbTableEntities = table.getVariableEntities().size();

    Collection<Variable> variables = Lists.newArrayList( //
        createIntVariable("A", "$('ds.table:weight')"), //
        createIntVariable("B", "$this('A')"), //
        createIntVariable("C", "$this('B')"));

    View viewTemplate = View.Builder.newView("view", table).list(new VariablesClause()).build();
    ValueTableWriter.VariableWriter variableWriter = viewTemplate.getListClause().createWriter();
    for(Variable variable : variables) {
      variableWriter.writeVariable(variable);
    }
    viewManager.addView(DATASOURCE, viewTemplate);

    View view = viewManager.getView(DATASOURCE, "view");
    int nbViewEntities = view.getVariableEntities().size();

    Stopwatch stopwatch = new Stopwatch().start();
    SortedSet<VariableEntity> entities = new TreeSet<VariableEntity>(view.getVariableEntities());
    log.info("Load {} entities in {}", entities.size(), stopwatch);
    stopwatch.reset().start();
    //noinspection ConstantConditions
    for(Value viewValue : view.getVariableValueSource("C").asVectorSource().getValues(entities)) {
      viewValue.getValue();
    }
    log.info("Load vector in {}", stopwatch);

    provider.getSessionFactory().getCurrentSession().getTransaction().rollback();

//    cleanlyRemoveDatasource(datasource);
  }

  private static Variable createIntVariable(String name, String script) {
    return new Variable.Builder(name, IntegerType.get(), PARTICIPANT).addAttribute("script", script).build();
  }

  private void cleanlyRemoveDatasource(String name) {
    cleanlyRemoveDatasource(MagmaEngine.get().getDatasource(name));
  }

  private void cleanlyRemoveDatasource(Datasource ds) {
    Transaction tx = provider.getSessionFactory().getCurrentSession().getTransaction();
    if(tx == null || !tx.isActive()) {
      provider.getSessionFactory().getCurrentSession().beginTransaction();
    }
    MagmaEngine.get().removeDatasource(ds);
    provider.getSessionFactory().getCurrentSession().getTransaction().commit();
  }
}
