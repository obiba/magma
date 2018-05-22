/*
 * Copyright (c) 2017 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.magma.js.validation;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.junit.After;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.obiba.core.util.FileUtil;
import org.obiba.magma.Datasource;
import org.obiba.magma.DatasourceFactory;
import org.obiba.magma.MagmaEngine;
import org.obiba.magma.NoSuchVariableException;
import org.obiba.magma.Value;
import org.obiba.magma.ValueSet;
import org.obiba.magma.ValueTable;
import org.obiba.magma.ValueTableWriter;
import org.obiba.magma.Variable;
import org.obiba.magma.VariableValueSourceWrapper;
import org.obiba.magma.datasource.fs.FsDatasource;
import org.obiba.magma.datasource.generated.GeneratedValueTable;
import org.obiba.magma.test.EmbeddedMongoProcessWrapper;
import org.obiba.magma.datasource.mongodb.MongoDBDatasourceFactory;
import org.obiba.magma.js.AbstractJsTest;
import org.obiba.magma.js.JavascriptVariableValueSource;
import org.obiba.magma.js.views.VariablesClause;
import org.obiba.magma.support.DatasourceCopier;
import org.obiba.magma.support.Initialisables;
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
import com.thoughtworks.xstream.XStream;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.junit.Assert.fail;
import static org.obiba.magma.Variable.Builder.newVariable;

@Ignore
@SuppressWarnings({ "PMD.NcssMethodCount", "OverlyLongMethod", "OverlyCoupledClass" })
public class VariableScriptValidatorTest extends AbstractJsTest {

  private static final Logger log = LoggerFactory.getLogger(VariableScriptValidatorTest.class);

  private static final String MONGO_DB_TEST = "magma-test";

  private static final String DATASOURCE = "ds";

  private static final String TABLE = "table";

  private static final String VARIABLE_AGE = "age";

  private static final String VARIABLE_WEIGHT = "weight";

  private static final String VARIABLE_HEIGHT = "height";

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

  private boolean setupMongoDB() {
    try {
      mongo = new EmbeddedMongoProcessWrapper();
      mongo.start();
      mongoDbUrl = "mongodb://" +mongo.getServerSocketAddress() + '/' + MONGO_DB_TEST;
      return true;
    } catch(Exception e) {
      return false;
    }
  }

  private Datasource getTestDatasource() throws IOException {
    DatasourceFactory factory = new MongoDBDatasourceFactory(DATASOURCE, mongoDbUrl);
    Datasource datasource = factory.create();

    List<Variable> variables = Lists.newArrayList( //
        newVariable(VARIABLE_AGE, IntegerType.get(), AbstractJsTest.PARTICIPANT).addAttribute("min", "25").addAttribute("max", "90")
            .build(), //
        newVariable(VARIABLE_WEIGHT, IntegerType.get(), AbstractJsTest.PARTICIPANT).unit("kg").addAttribute("min", "50")
            .addAttribute("max", "120").build(), //
        newVariable(VARIABLE_HEIGHT, IntegerType.get(), AbstractJsTest.PARTICIPANT).unit("cm").addAttribute("min", "150")
            .addAttribute("max", "200").build());
    ValueTable generatedValueTable = new GeneratedValueTable(datasource, variables, 10);

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
  public void test_validate_bmi_script() throws Exception {
    Datasource datasource = getTestDatasource();
    ValueTable table = datasource.getValueTable(TABLE);

    Variable weight_in_kg = AbstractJsTest.createIntVariable("weight_in_kg", "$('ds.table:weight')");
    Variable height_in_cm = AbstractJsTest.createIntVariable("height_in_cm", "$('ds.table:height')");
    Variable height_in_m = AbstractJsTest.createDecimalVariable("height_in_m", "$this('height_in_cm') / 100");
    Variable weight_in_lbs = AbstractJsTest.createIntVariable("weight_in_lbs", "$this('weight_in_kg') * 2.20462");
    Variable height_in_inches = AbstractJsTest.createIntVariable("height_in_inches", "$this('height_in_cm') * 0.393701");
    Variable bmi_metric = AbstractJsTest.createDecimalVariable("bmi_metric",
        "$this('weight_in_kg') / ($this('height_in_m') * $this('height_in_m'))");
    Variable bmi = AbstractJsTest.createDecimalVariable("bmi",
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

    validateJavascriptValueSource(view, "bmi_metric");
    validateJavascriptValueSource(view, "bmi");
  }

  @Test
  public void test_validate_with_circular_dependencies() throws Exception {
    Datasource datasource = getTestDatasource();
    ValueTable table = datasource.getValueTable(TABLE);

    Variable varA = AbstractJsTest.createIntVariable("A", "$('ds.view:B') + $('ds.view:F') + $('ds.view:C')");
    Variable varB = AbstractJsTest.createIntVariable("B", "$('ds.view:F')");
    Variable varC = AbstractJsTest.createIntVariable("C", "$('ds.view:D') * 10");
    Variable varD = AbstractJsTest.createIntVariable("D", "$('ds.view:E') + 5");
    Variable varE = AbstractJsTest.createIntVariable("E", "$('ds.view:A')");
    Variable varF = AbstractJsTest.createIntVariable("F", "10");

    View viewTemplate = View.Builder.newView("view", table).list(new VariablesClause()).build();
    try(ValueTableWriter.VariableWriter variableWriter = viewTemplate.getListClause().createWriter()) {
      variableWriter.writeVariable(varA);
      variableWriter.writeVariable(varB);
      variableWriter.writeVariable(varC);
      variableWriter.writeVariable(varD);
      variableWriter.writeVariable(varE);
      variableWriter.writeVariable(varF);
    }
    viewManager.addView(DATASOURCE, viewTemplate, null, null);

    View view = viewManager.getView(DATASOURCE, "view");
    try {
      validateJavascriptValueSource(view, "A");
      fail("Should throw CircularVariableDependencyRuntimeException");
    } catch(CircularVariableDependencyException e) {
      assertThat(e.getVariableRef()).isEqualTo("ds.view:A");
    }
  }

  @Test
  public void test_validate() throws Exception {
    Datasource datasource = getTestDatasource();
    ValueTable table = datasource.getValueTable(TABLE);

    Variable varA = AbstractJsTest.createIntVariable("A", "$('ds.view:B') + $('ds.view:C')");
    Variable varB = AbstractJsTest.createIntVariable("B", "10");
    Variable varC = AbstractJsTest.createIntVariable("C", "$('ds.view:D') * $('ds.view:D')");
    Variable varD = AbstractJsTest.createIntVariable("D", "$('ds.view:E') + 5");
    Variable varE = AbstractJsTest.createIntVariable("E", "$('ds.view:B')");

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
    validateJavascriptValueSource(view, "A");
  }

  @Test
  public void test_validate_with_self_reference() throws Exception {
    Datasource datasource = getTestDatasource();
    ValueTable table = datasource.getValueTable(TABLE);

    Variable circular = AbstractJsTest.createIntVariable("circular", "$('ds.view:circular')");
    View viewTemplate = View.Builder.newView("view", table).list(new VariablesClause()).build();
    try(ValueTableWriter.VariableWriter variableWriter = viewTemplate.getListClause().createWriter()) {
      variableWriter.writeVariable(circular);
    }
    viewManager.addView(DATASOURCE, viewTemplate, null, null);

    View view = viewManager.getView(DATASOURCE, "view");
    try {
      validateJavascriptValueSource(view, "circular");
      fail("Should throw CircularVariableDependencyRuntimeException");
    } catch(CircularVariableDependencyException e) {
      assertThat(e.getVariableRef()).isEqualTo("ds.view:circular");
    }
  }

  @Test
  public void test_validate_with_missing_variable() throws Exception {
    Datasource datasource = getTestDatasource();
    ValueTable table = datasource.getValueTable(TABLE);

    Variable var = AbstractJsTest.createIntVariable("var", "$('non-existing')");
    View viewTemplate = View.Builder.newView("view", table).list(new VariablesClause()).build();
    try(ValueTableWriter.VariableWriter variableWriter = viewTemplate.getListClause().createWriter()) {
      variableWriter.writeVariable(var);
    }
    viewManager.addView(DATASOURCE, viewTemplate, null, null);

    View view = viewManager.getView(DATASOURCE, "view");
    try {
      validateJavascriptValueSource(view, "var");
      fail("Should throw NoSuchVariableException");
    } catch(NoSuchVariableException e) {
      assertThat(e.getName()).isEqualTo("non-existing");
    }

    try {
      for(ValueSet valueSet : view.getValueSets()) {
        view.getValue(var, valueSet).getValue();
      }
    } catch(NoSuchVariableException e) {
      assertThat(e.getName()).isEqualTo("non-existing");
    }

    try {
      for(Value value : view.getVariableValueSource("var").asVectorSource()
          .getValues(new TreeSet<>(view.getVariableEntities()))) {
        value.getValue();
      }
    } catch(NoSuchVariableException e) {
      assertThat(e.getName()).isEqualTo("non-existing");
    }
  }

  @Test
  public void test_FNAC() throws Exception {

    DatasourceFactory factory = new MongoDBDatasourceFactory(DATASOURCE, mongoDbUrl);
    Datasource datasource = factory.create();
    Datasource viewAwareDatasource = viewManager.decorate(datasource);
    MagmaEngine.get().addDatasource(viewAwareDatasource);

    Datasource fsDatasource = new FsDatasource("fs", FileUtil.getFileFromResource("FNAC.zip"));
    Initialisables.initialise(fsDatasource);

    DatasourceCopier.Builder.newCopier().build().copy(fsDatasource, datasource);

    XStream xstream = MagmaEngine.get().getExtension(MagmaXStreamExtension.class).getXStreamFactory().createXStream();
    View viewTemplate = (View) xstream.fromXML(FileUtil.getFileFromResource("HOP.xml"));

    viewManager.addView(DATASOURCE, viewTemplate, null, null);

    View view = viewManager.getView(DATASOURCE, "HOP");

    Stopwatch stopwatch = Stopwatch.createUnstarted();
    for(Variable variable : view.getVariables()) {
      stopwatch.reset().start();
      validateJavascriptValueSource(view, variable.getName());
      log.debug("Validate {} script in {}", variable.getName(), stopwatch);
    }
  }

  private void validateJavascriptValueSource(ValueTable view, String variableName) {
    ((JavascriptVariableValueSource) ((VariableValueSourceWrapper) view.getVariableValueSource(variableName))
        .getWrapped()).validateScript();
  }

}
