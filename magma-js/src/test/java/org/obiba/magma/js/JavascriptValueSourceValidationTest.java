package org.obiba.magma.js;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;
import org.obiba.magma.Datasource;
import org.obiba.magma.DatasourceFactory;
import org.obiba.magma.MagmaEngine;
import org.obiba.magma.ValueTable;
import org.obiba.magma.ValueTableWriter;
import org.obiba.magma.Variable;
import org.obiba.magma.VariableValueSourceWrapper;
import org.obiba.magma.datasource.generated.GeneratedValueTable;
import org.obiba.magma.datasource.mongodb.MongoDBDatasourceFactory;
import org.obiba.magma.js.views.VariablesClause;
import org.obiba.magma.support.DatasourceCopier;
import org.obiba.magma.type.IntegerType;
import org.obiba.magma.views.DefaultViewManagerImpl;
import org.obiba.magma.views.MemoryViewPersistenceStrategy;
import org.obiba.magma.views.View;
import org.obiba.magma.views.ViewManager;
import org.obiba.magma.xstream.MagmaXStreamExtension;

import com.google.common.collect.Lists;
import com.mongodb.MongoClient;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.junit.Assert.fail;
import static org.obiba.magma.Variable.Builder.newVariable;

@SuppressWarnings("OverlyCoupledClass")
public class JavascriptValueSourceValidationTest extends AbstractJsTest {

//  private static final Logger log = LoggerFactory.getLogger(JavascriptValueSourceValidationTest.class);

  private static final String MONGO_DB_TEST = "magma-test";

  private static final String MONGO_DB_URL = "mongodb://localhost/" + MONGO_DB_TEST;

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
      client.close();
      return true;
    } catch(Exception e) {
      return false;
    }
  }

  private Datasource getTestDatasource() throws IOException {
    DatasourceFactory factory = new MongoDBDatasourceFactory(DATASOURCE, MONGO_DB_URL);
    Datasource datasource = factory.create();

    List<Variable> variables = Lists.newArrayList( //
        newVariable(VARIABLE_AGE, IntegerType.get(), PARTICIPANT).addAttribute("min", "25").addAttribute("max", "90")
            .build(), //
        newVariable(VARIABLE_WEIGHT, IntegerType.get(), PARTICIPANT).unit("kg").addAttribute("min", "50")
            .addAttribute("max", "120").build(), //
        newVariable(VARIABLE_HEIGHT, IntegerType.get(), PARTICIPANT).unit("cm").addAttribute("min", "150")
            .addAttribute("max", "200").build());
    ValueTable generatedValueTable = new GeneratedValueTable(datasource, variables, 1);

    Datasource viewAwareDatasource = viewManager.decorate(datasource);
    MagmaEngine.get().addDatasource(viewAwareDatasource);

    DatasourceCopier.Builder.newCopier().build().copy(generatedValueTable, TABLE, datasource);

    return viewAwareDatasource;
  }

  @Test
  public void test_parse_script() throws Exception {

    Set<VariableScriptValidator.VariableRefCall> calls = VariableScriptValidator.parseScript(
        "$('datasource1:table1.var1') / ($group(\"table1.var2\") * $this('var3') + $this('var3')) + 10 + $var('var4')");
    assertThat(calls).isNotNull();
    assertThat(calls).hasSize(3);
    assertThat(calls).contains(new VariableScriptValidator.VariableRefCall("$", "datasource1:table1.var1"));
    assertThat(calls).contains(new VariableScriptValidator.VariableRefCall("$this", "var3"));
    assertThat(calls).contains(new VariableScriptValidator.VariableRefCall("$var", "var4"));
  }

  @Test
  public void test_validate_script() throws Exception {
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
    viewManager.addView(DATASOURCE, viewTemplate, null);

    View view = viewManager.getView(DATASOURCE, "view");

    getJavascriptValueSource(view, "bmi_metric").validateScript();
    getJavascriptValueSource(view, "bmi").validateScript();
  }

  @Test
  public void test_validate_with_circular_dependencies() throws Exception {
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
      getJavascriptValueSource(view, "A").validateScript();
      fail("Should throw CircularVariableDependencyRuntimeException");
    } catch(CircularVariableDependencyRuntimeException e) {
      assertThat(e.getVariableRef()).isEqualTo("ds.view:A");
    }
  }

  @Test
  public void test_validate() throws Exception {
    Datasource datasource = getTestDatasource();
    ValueTable table = datasource.getValueTable(TABLE);

    Variable varA = createIntVariable("A", "$('ds.view:B') + $('ds.view:C')");
    Variable varB = createIntVariable("B", "10");
    Variable varC = createIntVariable("C", "$('ds.view:D') * $('ds.view:D')");
    Variable varD = createIntVariable("D", "$('ds.view:E') + 5");
    Variable varE = createIntVariable("E", "$('ds.view:B')");

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
    getJavascriptValueSource(view, "A").validateScript();
  }

  @Test
  public void test_validate_with_self_reference() throws Exception {
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
      getJavascriptValueSource(view, "circular").validateScript();
      fail("Should throw CircularVariableDependencyRuntimeException");
    } catch(CircularVariableDependencyRuntimeException e) {
      assertThat(e.getVariableRef()).isEqualTo("ds.view:circular");
    }
  }

  private JavascriptVariableValueSource getJavascriptValueSource(ValueTable view, String variableName) {
    return (JavascriptVariableValueSource) ((VariableValueSourceWrapper) view.getVariableValueSource(variableName))
        .getWrapped();
  }

}
