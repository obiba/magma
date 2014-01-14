package org.obiba.magma.datasource.hibernate;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.Set;

import org.hibernate.SessionFactory;
import org.obiba.core.util.FileUtil;
import org.obiba.magma.MagmaEngine;
import org.obiba.magma.ValueSet;
import org.obiba.magma.ValueTable;
import org.obiba.magma.Variable;
import org.obiba.magma.datasource.fs.FsDatasource;
import org.obiba.magma.support.DatasourceCopier;
import org.obiba.magma.support.Initialisables;
import org.obiba.magma.xstream.MagmaXStreamExtension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;

import com.google.common.base.Stopwatch;

import static com.google.common.collect.Iterables.size;

@SuppressWarnings("MethodOnlyUsedFromInnerClass")
public class HibernateDatasourceBenchmark {

  private static final Logger log = LoggerFactory.getLogger(HibernateDatasourceBenchmark.class);

  private static final Logger benchmarkLog = LoggerFactory.getLogger("benchmark");

  private static final String DB_BENCHMARK = "magma-benchmark";

  private static final String ONYX_DATA_ZIP = "20-onyx-data.zip";

  private static final String FNAC_ZIP = "FNAC.zip";

  private final Stopwatch stopwatch = Stopwatch.createUnstarted();

  private TransactionTemplate transactionTemplate;

  private SessionFactory sessionFactory;

  private HibernateDatasource datasource;

  public static void main(String... args) throws Exception {
    HibernateDatasourceBenchmark benchmark = new ClassPathXmlApplicationContext("/benchmark-context.xml")
        .getBean(HibernateDatasourceBenchmark.class);
    benchmark.setup();
    benchmark.runBenchmark();
    benchmark.shutdown();
  }

  private void setup() throws UnknownHostException {
    new MagmaEngine().extend(new MagmaXStreamExtension());

    transactionTemplate.execute(new TransactionCallbackWithoutResult() {
      @Override
      protected void doInTransactionWithoutResult(TransactionStatus status) {
        try {
          HibernateDatasource ds = new HibernateDatasource(DB_BENCHMARK, sessionFactory);
          Initialisables.initialise(ds);
          MagmaEngine.get().addDatasource(ds);
          ds.drop();
          MagmaEngine.get().removeDatasource(MagmaEngine.get().getDatasource(DB_BENCHMARK));
        } catch(Exception e) {
          log.warn("Error while cleaning datasource", e);
        }
      }
    });
  }

  private void runBenchmark() throws IOException {

    importData();

    transactionTemplate.execute(new TransactionCallbackWithoutResult() {
      @Override
      protected void doInTransactionWithoutResult(TransactionStatus status) {
        Set<ValueTable> valueTables = getValueTables();
        for(ValueTable valueTable : valueTables) {
          Iterable<Variable> variables = getVariables(valueTable);
          Iterable<ValueSet> valueSets = getValueSets(valueTable);
          readAllValues(valueTable, variables, valueSets);
        }
      }
    });
  }

  private Set<ValueTable> getValueTables() {
    stopwatch.reset().start();
    Set<ValueTable> valueTables = datasource.getValueTables();
    benchmarkLog.info("Load {} tables in {}", valueTables.size(), stopwatch);
    return valueTables;
  }

  private Iterable<Variable> getVariables(ValueTable valueTable) {
    stopwatch.reset().start();
    Iterable<Variable> variables = valueTable.getVariables();
    benchmarkLog.info("{}: load {} variables in {}", valueTable.getName(), size(variables), stopwatch);
    return variables;
  }

  private Iterable<ValueSet> getValueSets(ValueTable valueTable) {
    stopwatch.reset().start();
    Iterable<ValueSet> valueSets = valueTable.getValueSets();
    benchmarkLog.info("{}: load {} valueSets in {}", valueTable.getName(), size(valueSets), stopwatch);
    return valueSets;
  }

  private void readAllValues(ValueTable valueTable, Iterable<Variable> variables, Iterable<ValueSet> valueSets) {
    stopwatch.reset().start();
    for(Variable variable : variables) {
      for(ValueSet valueSet : valueSets) {
        valueTable.getValue(variable, valueSet);
      }
    }
    benchmarkLog.info("{}: load values in {}", valueTable.getName(), stopwatch);
  }

  private void importData() throws IOException {

    transactionTemplate.execute(new TransactionCallbackRuntimeExceptions() {
      @Override
      protected void doAction(TransactionStatus status) throws Exception {
        FsDatasource source = new FsDatasource("benchmark", FileUtil.getFileFromResource(ONYX_DATA_ZIP));
        datasource = new HibernateDatasource(DB_BENCHMARK, sessionFactory);
        Initialisables.initialise(datasource, source);

        stopwatch.reset().start();
        DatasourceCopier.Builder.newCopier().build().copy(source, datasource);
        benchmarkLog.info("Import {} in {}", ONYX_DATA_ZIP, stopwatch);
      }
    });
  }

  private void shutdown() {
    MagmaEngine.get().shutdown();
  }

  public void setTransactionTemplate(TransactionTemplate transactionTemplate) {
    this.transactionTemplate = transactionTemplate;
  }

  public void setSessionFactory(SessionFactory sessionFactory) {
    this.sessionFactory = sessionFactory;
  }
}
