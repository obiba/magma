package org.obiba.magma.datasource.mongodb;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.Set;

import org.obiba.core.util.FileUtil;
import org.obiba.magma.Datasource;
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

import com.google.common.base.Stopwatch;
import com.mongodb.MongoClient;

import static com.google.common.collect.Iterables.size;

public class MongoDBDatasourceBenchmark {

  private static final Logger benchmarkLog = LoggerFactory.getLogger("benchmark");

  private static final String DB_BENCHMARK = "magma-benchmark";

  private static final String DB_URL = "mongodb://localhost/" + DB_BENCHMARK;

  private static final String ONYX_DATA_ZIP = "20-onyx-data.zip";

  private static final String FNAC_ZIP = "FNAC.zip";

  private final Stopwatch stopwatch = Stopwatch.createUnstarted();

  public static void main(String... args) throws Exception {
    MongoDBDatasourceBenchmark benchmark = new MongoDBDatasourceBenchmark();
    benchmark.setupMongoDB();
    benchmark.runBenchmark();
    benchmark.shutdown();
  }

  private MongoDBDatasourceBenchmark() {
  }

  private void setupMongoDB() throws UnknownHostException {
    MongoClient client = new MongoClient();
    client.dropDatabase(DB_BENCHMARK);
    new MagmaEngine().extend(new MagmaXStreamExtension());
  }

  private void runBenchmark() throws IOException {
    FsDatasource source = new FsDatasource("benchmark", FileUtil.getFileFromResource(ONYX_DATA_ZIP));
    Datasource datasource = new MongoDBDatasourceFactory("ds-" + DB_BENCHMARK, DB_URL).create();
    Initialisables.initialise(datasource, source);

    importData(source, datasource);
    Set<ValueTable> valueTables = getValueTables(datasource);
    for(ValueTable valueTable : valueTables) {
      Iterable<Variable> variables = getVariables(valueTable);
      Iterable<ValueSet> valueSets = getValueSets(valueTable);
      readAllValues(valueTable, variables, valueSets);
    }
  }

  private Set<ValueTable> getValueTables(Datasource ds) {
    stopwatch.reset().start();
    Set<ValueTable> valueTables = ds.getValueTables();
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

  private void importData(Datasource source, Datasource dest) throws IOException {
    stopwatch.reset().start();
    DatasourceCopier.Builder.newCopier().build().copy(source, dest);
    benchmarkLog.info("Import {} in {}", ONYX_DATA_ZIP, stopwatch);
  }

  private void shutdown() {
    MagmaEngine.get().shutdown();
  }

}
