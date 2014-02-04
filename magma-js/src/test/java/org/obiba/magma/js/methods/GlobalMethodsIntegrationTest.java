package org.obiba.magma.js.methods;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;

import org.junit.Assume;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.mozilla.javascript.WrappedException;
import org.obiba.magma.Datasource;
import org.obiba.magma.DatasourceFactory;
import org.obiba.magma.MagmaEngine;
import org.obiba.magma.Value;
import org.obiba.magma.ValueSet;
import org.obiba.magma.ValueTable;
import org.obiba.magma.ValueTableWriter;
import org.obiba.magma.Variable;
import org.obiba.magma.VectorSource;
import org.obiba.magma.datasource.generated.GeneratedValueTable;
import org.obiba.magma.datasource.mongodb.MongoDBDatasourceFactory;
import org.obiba.magma.js.AbstractJsTest;
import org.obiba.magma.js.CircularVariableDependencyRuntimeException;
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

import com.google.common.collect.Lists;
import com.mongodb.MongoClient;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.junit.Assert.fail;
import static org.obiba.magma.Variable.Builder.newVariable;

@SuppressWarnings({ "OverlyLongMethod", "PMD.NcssMethodCount", "OverlyCoupledClass" })
public class GlobalMethodsIntegrationTest extends AbstractJsTest {

  private static final Logger log = LoggerFactory.getLogger(GlobalMethodsIntegrationTest.class);

  private static final String MONGO_DB_TEST = "magma-test";

  private static final String MONGO_DB_URL = "mongodb://localhost/" + MONGO_DB_TEST;

  private static final String PARTICIPANT = "Participant";

  private static final String DATASOURCE = "ds";

  private static final String TABLE = "table";

  private static final String VARIABLE_AGE = "age";

  private static final String VARIABLE_WEIGHT = "weight";

  private static final String VARIABLE_HEIGHT = "height";

  private ViewManager viewManager;

  @Before
  @Override
  public void before() {
    super.before();
    // run test only if MongoDB is running
    Assume.assumeTrue(setupMongoDB());

    viewManager = new DefaultViewManagerImpl(new MemoryViewPersistenceStrategy());
  }

  @Override
  protected MagmaEngine newEngine() {
    MagmaEngine magmaEngine = super.newEngine();
    magmaEngine.extend(new MagmaXStreamExtension());
    return magmaEngine;
  }

  private boolean setupMongoDB() {
    try {
      MongoClient client = new MongoClient();
      client.dropDatabase(MONGO_DB_TEST);
      return true;
    } catch(Exception e) {
      return false;
    }
  }

  private Datasource getTestDatasource() throws IOException {
    DatasourceFactory factory = new MongoDBDatasourceFactory(DATASOURCE, MONGO_DB_URL);
    Datasource datasource = factory.create();

    List<Variable> variables = Lists.newArrayList( //
        newVariable(VARIABLE_AGE, IntegerType.get(), PARTICIPANT).build(), //
        newVariable(VARIABLE_WEIGHT, IntegerType.get(), PARTICIPANT).unit("kg").build(), //
        newVariable(VARIABLE_HEIGHT, IntegerType.get(), PARTICIPANT).unit("cm").build());
    ValueTable generatedValueTable = new GeneratedValueTable(datasource, variables, 10);

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
    viewManager.addView(DATASOURCE, viewTemplate, null);

    List<Long> tableValues = new ArrayList<>();
    for(ValueSet valueSet : table.getValueSets()) {
      tableValues.add((Long) table.getValue(kgWeight, valueSet).getValue());
    }

    List<Long> viewValues = new ArrayList<>();
    View view = viewManager.getView(DATASOURCE, "view");
    for(ValueSet valueSet : view.getValueSets()) {
      viewValues.add((Long) view.getValue(lbsWeight, valueSet).getValue());
    }
    for(int i = 0; i < viewValues.size(); i++) {
      Long kg = tableValues.get(i);
      Long lbs = viewValues.get(i);
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
    viewManager.addView(DATASOURCE, viewTemplate, null);

    VectorSource tableVectorSource = table.getVariableValueSource("weight").asVectorSource();
    assertThat(tableVectorSource).isNotNull();
    //noinspection ConstantConditions
    List<Value> tableValues = Lists
        .newArrayList(tableVectorSource.getValues(new TreeSet<>(table.getVariableEntities())));

    View view = viewManager.getView(DATASOURCE, "view");
    VectorSource viewVectorSource = view.getVariableValueSource("weight_in_lbs").asVectorSource();
    assertThat(viewVectorSource).isNotNull();
    int i = 0;
    //noinspection ConstantConditions
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
    viewManager.addView(DATASOURCE, viewTemplate, null);

    View view = viewManager.getView(DATASOURCE, "view");
    for(ValueSet valueSet : view.getValueSets()) {
      view.getValue(varA, valueSet);
    }
  }

  @Test
  @Ignore
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
    viewManager.addView(DATASOURCE, viewTemplate, null);

    View view = viewManager.getView(DATASOURCE, "view");
    VectorSource vectorSource = view.getVariableValueSource("A").asVectorSource();
    assertThat(vectorSource).isNotNull();
    //noinspection ConstantConditions
    for(Value value : vectorSource.getValues(new TreeSet<>(view.getVariableEntities()))) {
      value.getValue();
    }
  }

  @Test
  public void test_$_value_set_with_self_reference() throws Exception {
    Datasource datasource = getTestDatasource();
    ValueTable table = datasource.getValueTable(TABLE);

    Variable circular = createIntVariable("circular", "$('ds.view:circular')");
    View viewTemplate = View.Builder.newView("view", table).list(new VariablesClause()).build();
    try(ValueTableWriter.VariableWriter variableWriter = viewTemplate.getListClause().createWriter()) {
      variableWriter.writeVariable(circular);
    }
    viewManager.addView(DATASOURCE, viewTemplate, null);

    View view = viewManager.getView(DATASOURCE, "view");
    try {
      for(ValueSet valueSet : view.getValueSets()) {
        view.getValue(circular, valueSet);
      }
      fail("Should throw WrappedException");
    } catch(WrappedException e) {
      Throwable cause = e.getCause();
      assertThat(cause).isNotNull();
      assertThat(cause).isInstanceOf(CircularVariableDependencyRuntimeException.class);
      assertThat(((CircularVariableDependencyRuntimeException) cause).getVariableRef()).isEqualTo("ds.view:circular");
    }
  }

  @Test
  public void test_$_vector_with_self_reference() throws Exception {
    Datasource datasource = getTestDatasource();
    ValueTable table = datasource.getValueTable(TABLE);

    Variable circular = createIntVariable("circular", "$('ds.view:circular')");
    View viewTemplate = View.Builder.newView("view", table).list(new VariablesClause()).build();
    try(ValueTableWriter.VariableWriter variableWriter = viewTemplate.getListClause().createWriter()) {
      variableWriter.writeVariable(circular);
    }
    viewManager.addView(DATASOURCE, viewTemplate, null);

    View view = viewManager.getView(DATASOURCE, "view");
    try {
      VectorSource vectorSource = view.getVariableValueSource("circular").asVectorSource();
      assertThat(vectorSource).isNotNull();
      //noinspection ConstantConditions
      for(Value value : vectorSource.getValues(new TreeSet<>(view.getVariableEntities()))) {
        value.getValue();
      }
      fail("Should throw WrappedException");
    } catch(WrappedException e) {
      Throwable cause = e.getCause();
      assertThat(cause).isNotNull();
      assertThat(cause).isInstanceOf(CircularVariableDependencyRuntimeException.class);
      assertThat(((CircularVariableDependencyRuntimeException) cause).getVariableRef()).isEqualTo("ds.view:circular");
    }
  }

  @Test
  public void test_$_value_set_with_circular_dependencies() throws Exception {
    Datasource datasource = getTestDatasource();
    ValueTable table = datasource.getValueTable(TABLE);

    Variable varA = createIntVariable("A", "$('ds.view:B') + $('ds.view:C')");
    Variable varB = createIntVariable("B", "10");
    Variable varC = createIntVariable("C", "$('ds.view:D') * 10");
    Variable varD = createIntVariable("D", "$('ds.view:E') + 5");
    Variable varE = createIntVariable("E", "$('ds.view:A')");

    View viewTemplate = View.Builder.newView("view", table).list(new VariablesClause()).build();
    try(ValueTableWriter.VariableWriter variableWriter = viewTemplate.getListClause().createWriter()) {
      variableWriter.writeVariable(varA);
      variableWriter.writeVariable(varB);
      variableWriter.writeVariable(varC);
      variableWriter.writeVariable(varD);
      variableWriter.writeVariable(varE);
    }
    viewManager.addView(DATASOURCE, viewTemplate, null);

    View view = viewManager.getView(DATASOURCE, "view");
    try {
      for(ValueSet valueSet : view.getValueSets()) {
        view.getValue(varA, valueSet);
      }
      fail("Should throw WrappedException");
    } catch(WrappedException e) {
      Throwable cause = e.getCause();
      assertThat(cause).isNotNull();
      assertThat(cause).isInstanceOf(CircularVariableDependencyRuntimeException.class);
      assertThat(((CircularVariableDependencyRuntimeException) cause).getVariableRef()).isEqualTo("ds.view:A");
    }

  }

  @Test
  @Ignore
  public void test_$_vector_with_circular_dependencies() throws Exception {
    Datasource datasource = getTestDatasource();
    ValueTable table = datasource.getValueTable(TABLE);

    Variable varA = createIntVariable("A", "$('ds.view:B') + $('ds.view:C')");
    Variable varB = createIntVariable("B", "10");
    Variable varC = createIntVariable("C", "$('ds.view:D') * 10");
    Variable varD = createIntVariable("D", "$('ds.view:E') + 5");
    Variable varE = createIntVariable("E", "$('ds.view:A')");

    View viewTemplate = View.Builder.newView("view", table).list(new VariablesClause()).build();
    try(ValueTableWriter.VariableWriter variableWriter = viewTemplate.getListClause().createWriter()) {
      variableWriter.writeVariable(varA);
      variableWriter.writeVariable(varB);
      variableWriter.writeVariable(varC);
      variableWriter.writeVariable(varD);
      variableWriter.writeVariable(varE);
    }
    viewManager.addView(DATASOURCE, viewTemplate, null);

    View view = viewManager.getView(DATASOURCE, "view");
    try {
      VectorSource vectorSource = view.getVariableValueSource("A").asVectorSource();
      assertThat(vectorSource).isNotNull();
      //noinspection ConstantConditions
      for(Value value : vectorSource.getValues(new TreeSet<>(view.getVariableEntities()))) {
        value.getValue();
      }
      fail("Should throw WrappedException");
    } catch(WrappedException e) {
      Throwable cause = e.getCause();
      assertThat(cause).isNotNull();
      assertThat(cause).isInstanceOf(CircularVariableDependencyRuntimeException.class);
      assertThat(((CircularVariableDependencyRuntimeException) cause).getVariableRef()).isEqualTo("ds.view:A");
    }
  }

  @Test
  public void test_$this_value_set() throws Exception {
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
    viewManager.addView(DATASOURCE, viewTemplate, null);

    List<Long> tableValues = new ArrayList<>();
    for(ValueSet valueSet : table.getValueSets()) {
      tableValues.add((Long) table.getValue(kgWeight, valueSet).getValue());
    }

    List<Long> viewValues = new ArrayList<>();
    View view = viewManager.getView(DATASOURCE, "view");
    for(ValueSet valueSet : view.getValueSets()) {
      viewValues.add((Long) view.getValue(lbsWeight, valueSet).getValue());
    }
    for(int i = 0; i < viewValues.size(); i++) {
      Long kg = tableValues.get(i);
      Long lbs = viewValues.get(i);
      assertThat(lbs).isEqualTo((long) (kg * 2.2));
    }
  }

  @Test
  public void test_$this_vector() throws Exception {
    Datasource datasource = getTestDatasource();
    ValueTable table = datasource.getValueTable(TABLE);

    Variable kgWeightRef = createIntVariable("weight_in_kg", "$('ds.table:weight')");
    Variable lbsWeight = createIntVariable("weight_in_lbs", "$this('weight_in_kg') * 2.2");

    View viewTemplate = View.Builder.newView("view", table).list(new VariablesClause()).build();
    try(ValueTableWriter.VariableWriter variableWriter = viewTemplate.getListClause().createWriter()) {
      variableWriter.writeVariable(kgWeightRef);
      variableWriter.writeVariable(lbsWeight);
    }
    viewManager.addView(DATASOURCE, viewTemplate, null);

    VectorSource tableVectorSource = table.getVariableValueSource("weight").asVectorSource();
    assertThat(tableVectorSource).isNotNull();
    //noinspection ConstantConditions
    List<Value> tableValues = Lists
        .newArrayList(tableVectorSource.getValues(new TreeSet<>(table.getVariableEntities())));

    View view = viewManager.getView(DATASOURCE, "view");
    VectorSource viewVectorSource = view.getVariableValueSource("weight_in_lbs").asVectorSource();
    assertThat(viewVectorSource).isNotNull();
    int i = 0;
    //noinspection ConstantConditions
    for(Value viewValue : viewVectorSource.getValues(new TreeSet<>(view.getVariableEntities()))) {
      Long kg = (Long) tableValues.get(i++).getValue();
      Long lbs = (Long) viewValue.getValue();
      assertThat(lbs).isEqualTo((long) (kg * 2.2));
    }
  }

  @Test
  public void test_$this_value_set_with_self_reference() throws Exception {
    Datasource datasource = getTestDatasource();
    ValueTable table = datasource.getValueTable(TABLE);

    Variable circular = createIntVariable("circular", "$this('circular')");
    View viewTemplate = View.Builder.newView("view", table).list(new VariablesClause()).build();
    try(ValueTableWriter.VariableWriter variableWriter = viewTemplate.getListClause().createWriter()) {
      variableWriter.writeVariable(circular);
    }
    viewManager.addView(DATASOURCE, viewTemplate, null);

    try {
      View view = viewManager.getView(DATASOURCE, "view");
      for(ValueSet valueSet : view.getValueSets()) {
        view.getValue(circular, valueSet);
      }
      fail("Should throw WrappedException");
    } catch(WrappedException e) {
      Throwable cause = e.getCause();
      assertThat(cause).isNotNull();
      assertThat(cause).isInstanceOf(CircularVariableDependencyRuntimeException.class);
      assertThat(((CircularVariableDependencyRuntimeException) cause).getVariableRef()).isEqualTo("ds.view:circular");
    }
  }

  @Test
  public void test_$this_vector_with_self_reference() throws Exception {
    Datasource datasource = getTestDatasource();
    ValueTable table = datasource.getValueTable(TABLE);

    Variable circular = createIntVariable("circular", "$this('circular')");
    View viewTemplate = View.Builder.newView("view", table).list(new VariablesClause()).build();
    try(ValueTableWriter.VariableWriter variableWriter = viewTemplate.getListClause().createWriter()) {
      variableWriter.writeVariable(circular);
    }
    viewManager.addView(DATASOURCE, viewTemplate, null);

    View view = viewManager.getView(DATASOURCE, "view");
    try {
      VectorSource vectorSource = view.getVariableValueSource("circular").asVectorSource();
      assertThat(vectorSource).isNotNull();
      //noinspection ConstantConditions
      for(Value value : vectorSource.getValues(new TreeSet<>(view.getVariableEntities()))) {
        value.getValue();
      }
      fail("Should throw WrappedException");
    } catch(WrappedException e) {
      Throwable cause = e.getCause();
      assertThat(cause).isNotNull();
      assertThat(cause).isInstanceOf(CircularVariableDependencyRuntimeException.class);
      assertThat(((CircularVariableDependencyRuntimeException) cause).getVariableRef()).isEqualTo("ds.view:circular");
    }
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
    viewManager.addView(DATASOURCE, viewTemplate, null);

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
    viewManager.addView(DATASOURCE, viewTemplate, null);

    View view = viewManager.getView(DATASOURCE, "view");
    for(ValueSet valueSet : view.getValueSets()) {
      view.getValue(varA, valueSet);
    }

    VectorSource vectorSource = view.getVariableValueSource("A").asVectorSource();
    assertThat(vectorSource).isNotNull();
    //noinspection ConstantConditions
    for(Value value : vectorSource.getValues(new TreeSet<>(view.getVariableEntities()))) {
      value.getValue();
    }
  }

  @Test
  public void test_$this_value_set_with_circular_dependencies() throws Exception {
    Datasource datasource = getTestDatasource();
    ValueTable table = datasource.getValueTable(TABLE);

    Variable varA = createIntVariable("A", "$this('B') + $this('C') + $this('D')");
    Variable varB = createIntVariable("B", "1");
    Variable varC = createIntVariable("C", "10");
    Variable varD = createIntVariable("D", "$this('A') * 5");

    View viewTemplate = View.Builder.newView("view", table).list(new VariablesClause()).build();
    try(ValueTableWriter.VariableWriter variableWriter = viewTemplate.getListClause().createWriter()) {
      variableWriter.writeVariable(varA);
      variableWriter.writeVariable(varB);
      variableWriter.writeVariable(varC);
      variableWriter.writeVariable(varD);
    }
    viewManager.addView(DATASOURCE, viewTemplate, null);

    View view = viewManager.getView(DATASOURCE, "view");
    try {
      for(ValueSet valueSet : view.getValueSets()) {
        view.getValue(varA, valueSet);
      }
      fail("Should throw WrappedException");
    } catch(WrappedException e) {
      Throwable cause = e.getCause();
      assertThat(cause).isNotNull();
      assertThat(cause).isInstanceOf(CircularVariableDependencyRuntimeException.class);
      assertThat(((CircularVariableDependencyRuntimeException) cause).getVariableRef()).isEqualTo("ds.view:A");
    }
  }

  @Test
  @Ignore
  public void test_$this_vector_with_circular_dependencies() throws Exception {
    Datasource datasource = getTestDatasource();
    ValueTable table = datasource.getValueTable(TABLE);

    Variable varA = createIntVariable("A", "$this('B') + $this('C') + $this('D')");
    Variable varB = createIntVariable("B", "1");
    Variable varC = createIntVariable("C", "10");
    Variable varD = createIntVariable("D", "$this('A') * 5");

    View viewTemplate = View.Builder.newView("view", table).list(new VariablesClause()).build();
    try(ValueTableWriter.VariableWriter variableWriter = viewTemplate.getListClause().createWriter()) {
      variableWriter.writeVariable(varA);
      variableWriter.writeVariable(varB);
      variableWriter.writeVariable(varC);
      variableWriter.writeVariable(varD);
    }
    viewManager.addView(DATASOURCE, viewTemplate, null);

    View view = viewManager.getView(DATASOURCE, "view");
    try {
      VectorSource vectorSource = view.getVariableValueSource("A").asVectorSource();
      assertThat(vectorSource).isNotNull();
      //noinspection ConstantConditions
      for(Value value : vectorSource.getValues(new TreeSet<>(view.getVariableEntities()))) {
        value.getValue();
      }
      fail("Should throw WrappedException");
    } catch(WrappedException e) {
      Throwable cause = e.getCause();
      assertThat(cause).isNotNull();
      assertThat(cause).isInstanceOf(CircularVariableDependencyRuntimeException.class);
      assertThat(((CircularVariableDependencyRuntimeException) cause).getVariableRef()).isEqualTo("ds.view:A");
    }
  }

  private static Variable createIntVariable(String name, String script) {
    return new Variable.Builder(name, IntegerType.get(), PARTICIPANT).addAttribute("script", script).build();
  }

}
