/*
 * Copyright (c) 2017 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.magma.js.methods;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.TimeUnit;

import com.google.common.collect.Maps;
import org.junit.After;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;
import org.obiba.magma.Datasource;
import org.obiba.magma.DatasourceFactory;
import org.obiba.magma.MagmaEngine;
import org.obiba.magma.Value;
import org.obiba.magma.ValueSet;
import org.obiba.magma.ValueTable;
import org.obiba.magma.ValueTableWriter;
import org.obiba.magma.Variable;
import org.obiba.magma.VariableEntity;
import org.obiba.magma.VariableValueSource;
import org.obiba.magma.VectorSource;
import org.obiba.magma.datasource.generated.GeneratedValueTable;
import org.obiba.magma.test.EmbeddedMongoProcessWrapper;
import org.obiba.magma.datasource.mongodb.MongoDBDatasourceFactory;
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

import static org.fest.assertions.api.Assertions.assertThat;
import static org.obiba.magma.Variable.Builder.newVariable;

@SuppressWarnings({ "OverlyLongMethod", "PMD.NcssMethodCount", "OverlyCoupledClass" })
public class GlobalMethodsMongoDbTest extends AbstractJsTest {

  private static final Logger log = LoggerFactory.getLogger(GlobalMethodsMongoDbTest.class);

  private static final String MONGO_DB_TEST = "magma-test";

  private static final String DATASOURCE = "ds";

  private static final String TABLE = "table";

  private static final String VARIABLE_AGE = "age";

  private static final String VARIABLE_WEIGHT = "weight";

  private static final String VARIABLE_HEIGHT = "height";

  private static final int NB_ENTITIES = 500;

  private ViewManager viewManager;

  private EmbeddedMongoProcessWrapper mongo;

  private String mongoDbUrl;

  @Before
  @Override
  public void before() {
    super.before();
    // run test only if MongoDB is running
    Assume.assumeTrue(setupMongoDB());

    viewManager = new DefaultViewManagerImpl(new MemoryViewPersistenceStrategy());
  }

  private boolean setupMongoDB() {
    try {
      mongo = new EmbeddedMongoProcessWrapper();
      mongo.start();
      mongoDbUrl = "mongodb://" + mongo.getServerSocketAddress() + '/' + MONGO_DB_TEST;
      return true;
    } catch(Exception e) {
      return false;
    }
  }

  @After
  @Override
  public void after() {
    super.after();
    mongo.stop();
  }

  @Override
  protected MagmaEngine newEngine() {
    MagmaEngine magmaEngine = super.newEngine();
    magmaEngine.extend(new MagmaXStreamExtension());
    return magmaEngine;
  }

  private Datasource getTestDatasource() throws IOException {
    DatasourceFactory factory = new MongoDBDatasourceFactory(DATASOURCE, mongoDbUrl);
    Datasource datasource = factory.create();

    List<Variable> variables = Lists.newArrayList( //
        newVariable(VARIABLE_AGE, IntegerType.get(), PARTICIPANT).addAttribute("min", "25").addAttribute("max", "90")
            .build(), //
        newVariable(VARIABLE_WEIGHT, IntegerType.get(), PARTICIPANT).unit("kg").addAttribute("min", "50")
            .addAttribute("max", "120").build(), //
        newVariable(VARIABLE_HEIGHT, IntegerType.get(), PARTICIPANT).unit("cm").addAttribute("min", "150")
            .addAttribute("max", "200").build());
    ValueTable generatedValueTable = new GeneratedValueTable(datasource, variables, NB_ENTITIES);

    Datasource viewAwareDatasource = viewManager.decorate(datasource);
    MagmaEngine.get().addDatasource(viewAwareDatasource);

    DatasourceCopier.Builder.newCopier().build().copy(generatedValueTable, TABLE, datasource);

    return viewAwareDatasource;
  }

  @Test
  public void test_$_value_set() throws Exception {
    Datasource datasource = getTestDatasource();
    ValueTable table = datasource.getValueTable(TABLE);

    Variable kgWeight = table.getVariable(VARIABLE_WEIGHT);

    Variable lbsWeight = createIntVariable("weight_in_lbs", "$('ds.table:weight') * 2.2");

    View viewTemplate = View.Builder.newView("view", table).list(new VariablesClause()).build();
    try(ValueTableWriter.VariableWriter variableWriter = viewTemplate.getListClause().createWriter()) {
      variableWriter.writeVariable(lbsWeight);
    }
    viewManager.addView(DATASOURCE, viewTemplate, null, null);

    Map<String,Long> tableValues = Maps.newHashMap();
    for(ValueSet valueSet : table.getValueSets()) {
      tableValues.put(valueSet.getVariableEntity().getIdentifier(), (Long) table.getValue(kgWeight, valueSet).getValue());
    }

    Map<String, Long> viewValues = Maps.newHashMap();
    View view = viewManager.getView(DATASOURCE, "view");
    for(ValueSet valueSet : view.getValueSets()) {
      viewValues.put(valueSet.getVariableEntity().getIdentifier(), (Long) view.getValue(lbsWeight, valueSet).getValue());
    }
    assertThat(tableValues.size()).isEqualTo(viewValues.size()).isEqualTo(NB_ENTITIES);

    for(String id : tableValues.keySet()) {
      Long kg = tableValues.get(id);
      Long lbs = viewValues.get(id);
      assertThat(lbs).isEqualTo((long) (kg * 2.2));
    }
  }

  @Test
  public void test_$_vector() throws Exception {
    Datasource datasource = getTestDatasource();
    ValueTable table = datasource.getValueTable(TABLE);

    Variable lbsWeight = createIntVariable("weight_in_lbs", "$('ds.table:weight') * 2.2");

    View viewTemplate = View.Builder.newView("view", table).list(new VariablesClause()).build();
    try(ValueTableWriter.VariableWriter variableWriter = viewTemplate.getListClause().createWriter()) {
      variableWriter.writeVariable(lbsWeight);
    }
    viewManager.addView(DATASOURCE, viewTemplate, null, null);

    VectorSource tableVectorSource = table.getVariableValueSource("weight").asVectorSource();
    assertThat(tableVectorSource).isNotNull();
    List<Value> tableValues = Lists
        .newArrayList(tableVectorSource.getValues(new TreeSet<>(table.getVariableEntities())));

    View view = viewManager.getView(DATASOURCE, "view");
    VectorSource viewVectorSource = view.getVariableValueSource("weight_in_lbs").asVectorSource();
    assertThat(viewVectorSource).isNotNull();
    int i = 0;
    for(Value viewValue : viewVectorSource.getValues(new TreeSet<>(view.getVariableEntities()))) {
      Long kg = (Long) tableValues.get(i++).getValue();
      Long lbs = (Long) viewValue.getValue();
      assertThat(lbs).isEqualTo((long) (kg * 2.2));
    }
  }

  @Test
  public void test_$_value_set_with_valid_dependencies() throws Exception {
    Datasource datasource = getTestDatasource();
    ValueTable table = datasource.getValueTable(TABLE);

    Variable varA = createIntVariable("A", "$('ds.view:B') + $('ds.view:C')");
    Variable varB = createIntVariable("B", "10");
    Variable varC = createIntVariable("C", "$('ds.view:D') * 10");
    Variable varD = createIntVariable("D", "$('ds.view:E') + 5");
    Variable varE = createIntVariable("E", "5");

    View viewTemplate = View.Builder.newView("view", table).list(new VariablesClause()).build();
    try(ValueTableWriter.VariableWriter variableWriter = viewTemplate.getListClause().createWriter()) {
      variableWriter.writeVariable(varA);
      variableWriter.writeVariable(varB);
      variableWriter.writeVariable(varC);
      variableWriter.writeVariable(varD);
      variableWriter.writeVariable(varE);
    }
    viewManager.addView(DATASOURCE, viewTemplate, null, null);

    View view = viewManager.getView(DATASOURCE, "view");
    for(ValueSet valueSet : view.getValueSets()) {
      view.getValue(varA, valueSet);
    }
  }

  @Test
  public void test_$_vector_with_valid_dependencies() throws Exception {
    Datasource datasource = getTestDatasource();
    ValueTable table = datasource.getValueTable(TABLE);

    Variable varA = createIntVariable("A", "$('ds.view:B') + $('ds.view:C')");
    Variable varB = createIntVariable("B", "10");
    Variable varC = createIntVariable("C", "$('ds.view:D') * 10");
    Variable varD = createIntVariable("D", "$('ds.view:E') + 5");
    Variable varE = createIntVariable("E", "5");

    View viewTemplate = View.Builder.newView("view", table).list(new VariablesClause()).build();
    try(ValueTableWriter.VariableWriter variableWriter = viewTemplate.getListClause().createWriter()) {
      variableWriter.writeVariable(varA);
      variableWriter.writeVariable(varB);
      variableWriter.writeVariable(varC);
      variableWriter.writeVariable(varD);
      variableWriter.writeVariable(varE);
    }
    viewManager.addView(DATASOURCE, viewTemplate, null, null);

    View view = viewManager.getView(DATASOURCE, "view");
    VariableValueSource variableValueSource = view.getVariableValueSource("A");
    assertThat(variableValueSource.supportVectorSource()).isTrue();
    VectorSource vectorSource = variableValueSource.asVectorSource();
    assertThat(vectorSource).isNotNull();
    for(Value value : vectorSource.getValues(new TreeSet<>(view.getVariableEntities()))) {
      value.getValue();
    }
  }

  @Test
  public void test_$this_simple_algo_value_set() throws Exception {
    Datasource datasource = getTestDatasource();
    ValueTable table = datasource.getValueTable(TABLE);

    Variable kgWeight = table.getVariable("weight");
    Variable kgWeightRef = createIntVariable("weight_in_kg", "$('ds.table:weight')");
    Variable lbsWeight = createIntVariable("weight_in_lbs", "$this('weight_in_kg') * 2.2");

    View viewTemplate = View.Builder.newView("view", table).list(new VariablesClause()).build();
    try(ValueTableWriter.VariableWriter variableWriter = viewTemplate.getListClause().createWriter()) {
      variableWriter.writeVariable(kgWeightRef);
      variableWriter.writeVariable(lbsWeight);
    }
    viewManager.addView(DATASOURCE, viewTemplate, null, null);

    Map<String,Long> tableValues = Maps.newHashMap();
    for(ValueSet valueSet : table.getValueSets()) {
      tableValues.put(valueSet.getVariableEntity().getIdentifier(), (Long) table.getValue(kgWeight, valueSet).getValue());
    }

    Map<String,Long> viewValues = Maps.newHashMap();
    View view = viewManager.getView(DATASOURCE, "view");
    for(ValueSet valueSet : view.getValueSets()) {
      viewValues.put(valueSet.getVariableEntity().getIdentifier(), (Long) view.getValue(lbsWeight, valueSet).getValue());
    }
    for(String id : tableValues.keySet()) {
      Long kg = tableValues.get(id);
      Long lbs = viewValues.get(id);
      assertThat(lbs).isEqualTo((long) (kg * 2.2));
    }
  }

  @Test
  public void test_$this_bmi_value_set() throws Exception {
    Datasource datasource = getTestDatasource();
    ValueTable table = datasource.getValueTable(TABLE);

    Variable weight_in_kg = createIntVariable("weight_in_kg", "$('ds.table:weight')");
    Variable height_in_cm = createIntVariable("height_in_cm", "$('ds.table:height')");
    Variable height_in_m = createDecimalVariable("height_in_m", "$this('height_in_cm') / 100");
    Variable weight_in_lbs = createIntVariable("weight_in_lbs", "$this('weight_in_kg') * 2.20462");
    Variable height_in_inches = createIntVariable("height_in_inches", "$this('height_in_cm') * 0.393701");
    Variable bmi_metric = createDecimalVariable("bmi_metric",
        "$this('weight_in_kg') / ($this('height_in_m') * $this('height_in_m'))");
    Variable bmi = createDecimalVariable("bmi",
        "$this('weight_in_lbs') / ($this('height_in_inches') * $this('height_in_inches')) * 703");

    Collection<Variable> variables = Lists
        .newArrayList(weight_in_kg, height_in_cm, height_in_m, bmi_metric, weight_in_lbs, height_in_inches, bmi);

    View viewTemplate = View.Builder.newView("view", table).list(new VariablesClause()).build();
    try(ValueTableWriter.VariableWriter variableWriter = viewTemplate.getListClause().createWriter()) {
      for(Variable variable : variables) {
        variableWriter.writeVariable(variable);
      }
    }
    viewManager.addView(DATASOURCE, viewTemplate, null, null);

    View view = viewManager.getView(DATASOURCE, "view");
    for(ValueSet valueSet : view.getValueSets()) {
//      log.debug("weight_in_kg: {}", view.getValue(weight_in_kg, valueSet));
//      log.debug("weight_in_lbs: {}", view.getValue(weight_in_lbs, valueSet));
//      log.debug("height_in_cm: {}", view.getValue(height_in_cm, valueSet));
//      log.debug("height_in_m: {}", view.getValue(height_in_m, valueSet));
//      log.debug("height_in_inches {}", view.getValue(height_in_inches, valueSet));
//      log.debug("bmi_metric: {}", view.getValue(bmi_metric, valueSet));
//      log.debug("bmi: {}", view.getValue(bmi, valueSet));
      double bmiValue = (double) view.getValue(bmi, valueSet).getValue();
      double bmiMetricValue = (double) view.getValue(bmi_metric, valueSet).getValue();
      assertThat(Math.abs(bmiValue - bmiMetricValue)).isLessThan(2);
    }
  }

  @Test
  public void test_$this_vector() throws Exception {
    Datasource datasource = getTestDatasource();
    ValueTable table = datasource.getValueTable(TABLE);

    Variable weight_in_kg = createIntVariable("weight_in_kg", "$('ds.table:weight')");
    Variable height_in_cm = createIntVariable("height_in_cm", "$('ds.table:height')");
    Variable height_in_m = createDecimalVariable("height_in_m", "$this('height_in_cm') / 100");
    Variable weight_in_lbs = createIntVariable("weight_in_lbs", "$this('weight_in_kg') * 2.20462");
    Variable height_in_inches = createIntVariable("height_in_inches", "$this('height_in_cm') * 0.393701");
    Variable bmi_metric = createDecimalVariable("bmi_metric",
        "$this('weight_in_kg') / ($this('height_in_m') * $this('height_in_m'))");
    Variable bmi = createDecimalVariable("bmi",
        "$this('weight_in_lbs') / ($this('height_in_inches') * $this('height_in_inches')) * 703");

    Collection<Variable> variables = Lists
        .newArrayList(weight_in_kg, height_in_cm, height_in_m, bmi_metric, weight_in_lbs, height_in_inches, bmi);

    View viewTemplate = View.Builder.newView("view", table).list(new VariablesClause()).build();
    try(ValueTableWriter.VariableWriter variableWriter = viewTemplate.getListClause().createWriter()) {
      for(Variable variable : variables) {
        variableWriter.writeVariable(variable);
      }
    }
    viewManager.addView(DATASOURCE, viewTemplate, null, null);

    View view = viewManager.getView(DATASOURCE, "view");

    SortedSet<VariableEntity> entities = new TreeSet<>(view.getVariableEntities());

    List<Double> bmiValues = new ArrayList<>();
    for(Value viewValue : view.getVariableValueSource("bmi").asVectorSource().getValues(entities)) {
      bmiValues.add((Double) viewValue.getValue());
    }

    List<Double> bmiMetricValues = new ArrayList<>();
    for(Value viewValue : view.getVariableValueSource("bmi_metric").asVectorSource().getValues(entities)) {
      bmiMetricValues.add((Double) viewValue.getValue());
    }

    assertThat(bmiValues.size()).isEqualTo(bmiMetricValues.size()).isEqualTo(NB_ENTITIES);
    for(int i = 0; i < NB_ENTITIES; i++) {
      assertThat(Math.abs(bmiValues.get(i) - bmiMetricValues.get(i))).isLessThan(2);
    }
  }

  @Test
  public void test_$this_value_set_performance() throws Exception {
    Datasource datasource = getTestDatasource();
    ValueTable table = datasource.getValueTable(TABLE);

    Collection<Variable> variables = Lists.newArrayList( //
        createIntVariable("A", "$('ds.table:weight')"), //
        createIntVariable("B", "$this('A')"), //
        createIntVariable("C", "$this('B')"));

    View viewTemplate = View.Builder.newView("view", table).list(new VariablesClause()).build();
    try(ValueTableWriter.VariableWriter variableWriter = viewTemplate.getListClause().createWriter()) {
      for(Variable variable : variables) {
        variableWriter.writeVariable(variable);
      }
    }
    viewManager.addView(DATASOURCE, viewTemplate, null, null);

    View view = viewManager.getView(DATASOURCE, "view");

    Variable varC = view.getVariable("C");

    Stopwatch stopwatch = Stopwatch.createStarted();
    for(ValueSet valueSet : view.getValueSets()) {
      view.getValue(varC, valueSet).getValue();
    }
    log.info("Load {} value sets in {}", NB_ENTITIES, stopwatch.stop());
    assertThat(stopwatch.elapsed(TimeUnit.SECONDS)).isLessThan(1);
  }

  @Test
  public void test_$this_vector_performance() throws Exception {
    Datasource datasource = getTestDatasource();
    ValueTable table = datasource.getValueTable(TABLE);

    Collection<Variable> variables = Lists.newArrayList( //
        createIntVariable("A", "$('ds.table:weight')"), //
        createIntVariable("B", "$this('A')"), //
        createIntVariable("C", "$this('B')"));

    View viewTemplate = View.Builder.newView("view", table).list(new VariablesClause()).build();
    try(ValueTableWriter.VariableWriter variableWriter = viewTemplate.getListClause().createWriter()) {
      for(Variable variable : variables) {
        variableWriter.writeVariable(variable);
      }
    }
    viewManager.addView(DATASOURCE, viewTemplate, null, null);

    View view = viewManager.getView(DATASOURCE, "view");
    Stopwatch stopwatch = Stopwatch.createStarted();
    SortedSet<VariableEntity> entities = new TreeSet<>(view.getVariableEntities());
    log.info("Load {} entities in {}", entities.size(), stopwatch);
    stopwatch.reset().start();
    for(Value viewValue : view.getVariableValueSource("C").asVectorSource().getValues(entities)) {
      viewValue.getValue();
    }
    log.info("Load vector for {} entities in {}", NB_ENTITIES, stopwatch.stop());
    assertThat(stopwatch.elapsed(TimeUnit.SECONDS)).isLessThan(1);

    datasource.dispose();
  }

  @Test
  public void test_$this_value_set_with_inner_$this() throws Exception {
    Datasource datasource = getTestDatasource();
    ValueTable table = datasource.getValueTable(TABLE);

    Variable varA = createIntVariable("A", "$this('B') + $this('C') + $this('D')");
    Variable varB = createIntVariable("B", "1");
    Variable varC = createIntVariable("C", "10");
    Variable varD = createIntVariable("D", "$this('B') * 5");

    View viewTemplate = View.Builder.newView("view", table).list(new VariablesClause()).build();
    try(ValueTableWriter.VariableWriter variableWriter = viewTemplate.getListClause().createWriter()) {
      variableWriter.writeVariable(varA);
      variableWriter.writeVariable(varB);
      variableWriter.writeVariable(varC);
      variableWriter.writeVariable(varD);
    }
    viewManager.addView(DATASOURCE, viewTemplate, null, null);

    View view = viewManager.getView(DATASOURCE, "view");
    for(ValueSet valueSet : view.getValueSets()) {
      view.getValue(varA, valueSet);
    }
  }

  @Test
  public void test_$this_vector_with_inner_$this() throws Exception {
    Datasource datasource = getTestDatasource();
    ValueTable table = datasource.getValueTable(TABLE);

    Variable varA = createIntVariable("A", "$this('B') + $this('C') + $this('D')");
    Variable varB = createIntVariable("B", "1");
    Variable varC = createIntVariable("C", "10");
    Variable varD = createIntVariable("D", "$this('B') * 5");

    View viewTemplate = View.Builder.newView("view", table).list(new VariablesClause()).build();
    try(ValueTableWriter.VariableWriter variableWriter = viewTemplate.getListClause().createWriter()) {
      variableWriter.writeVariable(varA);
      variableWriter.writeVariable(varB);
      variableWriter.writeVariable(varC);
      variableWriter.writeVariable(varD);
    }
    viewManager.addView(DATASOURCE, viewTemplate, null, null);

    View view = viewManager.getView(DATASOURCE, "view");
    for(ValueSet valueSet : view.getValueSets()) {
      view.getValue(varA, valueSet);
    }

    VectorSource vectorSource = view.getVariableValueSource("A").asVectorSource();
    assertThat(vectorSource).isNotNull();
    for(Value value : vectorSource.getValues(new TreeSet<>(view.getVariableEntities()))) {
      value.getValue();
    }
  }

}
