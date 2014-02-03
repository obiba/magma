package org.obiba.magma.js.methods;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.mozilla.javascript.NativeArray;
import org.obiba.core.util.FileUtil;
import org.obiba.magma.Datasource;
import org.obiba.magma.MagmaEngine;
import org.obiba.magma.MagmaRuntimeException;
import org.obiba.magma.Value;
import org.obiba.magma.ValueSequence;
import org.obiba.magma.ValueSet;
import org.obiba.magma.ValueTable;
import org.obiba.magma.Variable;
import org.obiba.magma.datasource.csv.CsvDatasource;
import org.obiba.magma.js.AbstractJsTest;
import org.obiba.magma.js.CircularVariableDependencyRuntimeException;
import org.obiba.magma.js.ScriptableValue;
import org.obiba.magma.js.views.VariablesClause;
import org.obiba.magma.type.IntegerType;
import org.obiba.magma.type.TextType;
import org.obiba.magma.views.DefaultViewManagerImpl;
import org.obiba.magma.views.MemoryViewPersistenceStrategy;
import org.obiba.magma.views.View;
import org.obiba.magma.views.ViewManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.junit.Assert.fail;
import static org.mozilla.javascript.Context.getCurrentContext;
import static org.obiba.magma.ValueTableWriter.VariableWriter;

@SuppressWarnings({ "OverlyLongMethod", "PMD.NcssMethodCount", "OverlyCoupledClass" })
public class GlobalMethodsTest extends AbstractJsTest {

  private static final Logger log = LoggerFactory.getLogger(GlobalMethodsTest.class);

  @Test
  public void test_newValue_inferred_int() throws Exception {
    ScriptableValue sv = GlobalMethods.newValue(getCurrentContext(), getSharedScope(), new Object[] { 1 }, null);
    assertThat(sv.getValue().isNull()).isFalse();
    assertThat(sv.getValue().isSequence()).isFalse();
    assertThat((IntegerType) sv.getValueType()).isEqualTo(IntegerType.get());
    assertThat((Long) sv.getValue().getValue()).isEqualTo(1l);
  }

  @Test
  public void test_newValue_int() throws Exception {
    ScriptableValue sv = GlobalMethods
        .newValue(getCurrentContext(), getSharedScope(), new Object[] { "1", "integer" }, null);
    assertThat(sv.getValue().isNull()).isFalse();
    assertThat(sv.getValue().isSequence()).isFalse();
    assertThat((IntegerType) sv.getValueType()).isEqualTo(IntegerType.get());
    assertThat((Long) sv.getValue().getValue()).isEqualTo(1l);
  }

  @Test(expected = MagmaRuntimeException.class)
  public void test_newValue_wrong_type() throws Exception {
    GlobalMethods.newValue(getCurrentContext(), getSharedScope(), new Object[] { "qwerty", "integer" }, null);
  }

  @Test
  public void test_newSequence_int() throws Exception {
    ScriptableValue sv = GlobalMethods
        .newSequence(getCurrentContext(), getSharedScope(), new Object[] { new NativeArray(new Object[] { 1, 2, 3 }) },
            null);
    assertThat(sv.getValue().isSequence()).isTrue();
    assertThat((IntegerType) sv.getValueType()).isEqualTo(IntegerType.get());
    assertThat(sv.getValue().getLength()).isEqualTo(3);
    ValueSequence sequence = sv.getValue().asSequence();
    for(int i = 1; i <= 3; i++) {
      Value value = sequence.get(i - 1);
      assertThat((IntegerType) value.getValueType()).isEqualTo(IntegerType.get());
      assertThat((Long) value.getValue()).isEqualTo((long) i);
    }
  }

  @Test
  public void test_newSequence_String() throws Exception {
    ScriptableValue sv = GlobalMethods.newSequence(getCurrentContext(), getSharedScope(),
        new Object[] { new NativeArray(new Object[] { "1", "2", "3" }) }, null);
    assertThat(sv.getValue().isSequence()).isTrue();
    assertThat((TextType) sv.getValueType()).isEqualTo(TextType.get());
    assertThat(sv.getValue().getLength()).isEqualTo(3l);
    ValueSequence sequence = sv.getValue().asSequence();
    for(int i = 1; i <= 3; i++) {
      Value value = sequence.get(i - 1);
      assertThat((TextType) value.getValueType()).isEqualTo(TextType.get());
      assertThat((String) value.getValue()).isEqualTo(String.valueOf(i));
    }
  }

  @Test
  public void test_newSequence_with_int_type() throws Exception {
    ScriptableValue sv = GlobalMethods.newSequence(getCurrentContext(), getSharedScope(),
        new Object[] { new NativeArray(new Object[] { "1", "2", "3" }), "integer" }, null);
    assertThat(sv.getValue().isSequence()).isTrue();
    assertThat((IntegerType) sv.getValueType()).isEqualTo(IntegerType.get());
    assertThat(sv.getValue().getLength()).isEqualTo(3l);
    ValueSequence sequence = sv.getValue().asSequence();
    for(int i = 1; i <= 3; i++) {
      Value value = sequence.get(i - 1);
      assertThat((IntegerType) value.getValueType()).isEqualTo(IntegerType.get());
      assertThat((Long) value.getValue()).isEqualTo((long) i);
    }
  }

  @Test
  public void test_$() {
    CsvDatasource datasource = new CsvDatasource("ds").addValueTable("table", //
        FileUtil.getFileFromResource("org/obiba/magma/js/variables.csv"), //
        FileUtil.getFileFromResource("org/obiba/magma/js/data.csv"));

    ViewManager viewManager = new DefaultViewManagerImpl(new MemoryViewPersistenceStrategy());
    Datasource viewAwareDatasource = viewManager.decorate(datasource);
    MagmaEngine.get().addDatasource(viewAwareDatasource);

    ValueTable table = viewAwareDatasource.getValueTable("table");
    Variable kgWeight = table.getVariable("weight");

    Variable lbsWeight = createIntVariable("weight_in_lbs", "$('ds.table:weight') * 2.2");

    View viewTemplate = View.Builder.newView("view", table).list(new VariablesClause()).build();
    try(VariableWriter variableWriter = viewTemplate.getListClause().createWriter()) {
      variableWriter.writeVariable(lbsWeight);
    }
    viewManager.addView("ds", viewTemplate, null);

    List<Long> tableValues = new ArrayList<>();
    for(ValueSet valueSet : viewAwareDatasource.getValueTable("table").getValueSets()) {
      tableValues.add((Long) table.getValue(kgWeight, valueSet).getValue());
    }

    List<Long> viewValues = new ArrayList<>();
    View view = viewManager.getView("ds", "view");
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
  public void test_$_dependencies() {
    CsvDatasource datasource = new CsvDatasource("ds").addValueTable("table", //
        FileUtil.getFileFromResource("org/obiba/magma/js/variables.csv"), //
        FileUtil.getFileFromResource("org/obiba/magma/js/data.csv"));

    ViewManager viewManager = new DefaultViewManagerImpl(new MemoryViewPersistenceStrategy());
    Datasource viewAwareDatasource = viewManager.decorate(datasource);
    MagmaEngine.get().addDatasource(viewAwareDatasource);

    ValueTable table = viewAwareDatasource.getValueTable("table");

    Variable varA = createIntVariable("A", "$('ds.view:B') + $('ds.view:C')");
    Variable varB = createIntVariable("B", "10");
    Variable varC = createIntVariable("C", "$('ds.view:D') * 10");
    Variable varD = createIntVariable("D", "$('ds.view:E') + 5");
    Variable varE = createIntVariable("E", "5");

    View viewTemplate = View.Builder.newView("view", table).list(new VariablesClause()).build();
    try(VariableWriter variableWriter = viewTemplate.getListClause().createWriter()) {
      variableWriter.writeVariable(varA);
      variableWriter.writeVariable(varB);
      variableWriter.writeVariable(varC);
      variableWriter.writeVariable(varD);
      variableWriter.writeVariable(varE);
    }
    viewManager.addView("ds", viewTemplate, null);

    View view = viewManager.getView("ds", "view");
    for(ValueSet valueSet : view.getValueSets()) {
      view.getValue(varA, valueSet);
    }
  }

  @Test
  public void test_$_self_reference() {
    CsvDatasource datasource = new CsvDatasource("ds").addValueTable("table", //
        FileUtil.getFileFromResource("org/obiba/magma/js/variables.csv"), //
        FileUtil.getFileFromResource("org/obiba/magma/js/data.csv"));

    ViewManager viewManager = new DefaultViewManagerImpl(new MemoryViewPersistenceStrategy());
    Datasource viewAwareDatasource = viewManager.decorate(datasource);
    MagmaEngine.get().addDatasource(viewAwareDatasource);

    ValueTable table = viewAwareDatasource.getValueTable("table");

    Variable circular = createIntVariable("circular", "$('ds.view:circular')");
    View viewTemplate = View.Builder.newView("view", table).list(new VariablesClause()).build();
    try(VariableWriter variableWriter = viewTemplate.getListClause().createWriter()) {
      variableWriter.writeVariable(circular);
    }
    viewManager.addView("ds", viewTemplate, null);

    try {
      View view = viewManager.getView("ds", "view");
      for(ValueSet valueSet : view.getValueSets()) {
        view.getValue(circular, valueSet);
      }
      fail("Should throw CircularVariableDependencyRuntimeException");
    } catch(Exception e) {
      Throwable cause = e.getCause();
      assertThat(cause).isNotNull();
      assertThat(cause).isInstanceOf(CircularVariableDependencyRuntimeException.class);
      assertThat(((CircularVariableDependencyRuntimeException) cause).getVariableRef()).isEqualTo("ds.view:circular");
    }
  }

  @Test
  public void test_$_circular_dependencies() {
    CsvDatasource datasource = new CsvDatasource("ds").addValueTable("table", //
        FileUtil.getFileFromResource("org/obiba/magma/js/variables.csv"), //
        FileUtil.getFileFromResource("org/obiba/magma/js/data.csv"));

    ViewManager viewManager = new DefaultViewManagerImpl(new MemoryViewPersistenceStrategy());
    Datasource viewAwareDatasource = viewManager.decorate(datasource);
    MagmaEngine.get().addDatasource(viewAwareDatasource);

    ValueTable table = viewAwareDatasource.getValueTable("table");

    Variable varA = createIntVariable("A", "$('ds.view:B') + $('ds.view:C')");
    Variable varB = createIntVariable("B", "10");
    Variable varC = createIntVariable("C", "$('ds.view:D') * 10");
    Variable varD = createIntVariable("D", "$('ds.view:E') + 5");
    Variable varE = createIntVariable("E", "$('ds.view:A')");

    View viewTemplate = View.Builder.newView("view", table).list(new VariablesClause()).build();
    try(VariableWriter variableWriter = viewTemplate.getListClause().createWriter()) {
      variableWriter.writeVariable(varA);
      variableWriter.writeVariable(varB);
      variableWriter.writeVariable(varC);
      variableWriter.writeVariable(varD);
      variableWriter.writeVariable(varE);
    }
    viewManager.addView("ds", viewTemplate, null);

    try {
      View view = viewManager.getView("ds", "view");
      for(ValueSet valueSet : view.getValueSets()) {
        view.getValue(varA, valueSet);
      }
      fail("Should throw CircularVariableDependencyRuntimeException");
    } catch(Exception e) {
      Throwable cause = e.getCause();
      assertThat(cause).isNotNull();
      assertThat(cause).isInstanceOf(CircularVariableDependencyRuntimeException.class);
      assertThat(((CircularVariableDependencyRuntimeException) cause).getVariableRef()).isEqualTo("ds.view:A");
    }
  }

  @Test
  public void test_$this() {
    CsvDatasource datasource = new CsvDatasource("ds").addValueTable("table", //
        FileUtil.getFileFromResource("org/obiba/magma/js/variables.csv"), //
        FileUtil.getFileFromResource("org/obiba/magma/js/data.csv"));

    ViewManager viewManager = new DefaultViewManagerImpl(new MemoryViewPersistenceStrategy());
    Datasource viewAwareDatasource = viewManager.decorate(datasource);
    MagmaEngine.get().addDatasource(viewAwareDatasource);

    ValueTable table = viewAwareDatasource.getValueTable("table");
    Variable kgWeight = table.getVariable("weight");

    Variable kgWeightRef = createIntVariable("weight_in_kg", "$('ds.table:weight')");
    Variable lbsWeight = createIntVariable("weight_in_lbs", "$this('weight_in_kg') * 2.2");

    View viewTemplate = View.Builder.newView("view", table).list(new VariablesClause()).build();
    try(VariableWriter variableWriter = viewTemplate.getListClause().createWriter()) {
      variableWriter.writeVariable(kgWeightRef);
      variableWriter.writeVariable(lbsWeight);
    }
    viewManager.addView("ds", viewTemplate, null);

    List<Long> tableValues = new ArrayList<>();
    for(ValueSet valueSet : viewAwareDatasource.getValueTable("table").getValueSets()) {
      tableValues.add((Long) table.getValue(kgWeight, valueSet).getValue());
    }

    List<Long> viewValues = new ArrayList<>();
    View view = viewManager.getView("ds", "view");
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
  public void test_$this_self_reference() {
    CsvDatasource datasource = new CsvDatasource("ds").addValueTable("table", //
        FileUtil.getFileFromResource("org/obiba/magma/js/variables.csv"), //
        FileUtil.getFileFromResource("org/obiba/magma/js/data.csv"));

    ViewManager viewManager = new DefaultViewManagerImpl(new MemoryViewPersistenceStrategy());
    Datasource viewAwareDatasource = viewManager.decorate(datasource);
    MagmaEngine.get().addDatasource(viewAwareDatasource);

    ValueTable table = viewAwareDatasource.getValueTable("table");

    Variable circular = createIntVariable("circular", "$this('circular')");
    View viewTemplate = View.Builder.newView("view", table).list(new VariablesClause()).build();
    try(VariableWriter variableWriter = viewTemplate.getListClause().createWriter()) {
      variableWriter.writeVariable(circular);
    }
    viewManager.addView("ds", viewTemplate, null);

    try {
      View view = viewManager.getView("ds", "view");
      for(ValueSet valueSet : view.getValueSets()) {
        view.getValue(circular, valueSet);
      }
      fail("Should throw CircularVariableDependencyRuntimeException");
    } catch(Exception e) {
      Throwable cause = e.getCause();
      assertThat(cause).isNotNull();
      assertThat(cause).isInstanceOf(CircularVariableDependencyRuntimeException.class);
      assertThat(((CircularVariableDependencyRuntimeException) cause).getVariableRef()).isEqualTo("ds.view:circular");
    }
  }

  @Test
  public void test_inner_$this() {

  }

  private static Variable createIntVariable(String name, String script) {
    return new Variable.Builder(name, IntegerType.get(), "Participant").addAttribute("script", script).build();
  }

}
