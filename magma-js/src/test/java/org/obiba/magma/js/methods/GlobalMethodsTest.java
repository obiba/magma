package org.obiba.magma.js.methods;

import java.util.ArrayList;
import java.util.Collection;

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
    Variable tableVariable = table.getVariable("var1");

    Variable viewVariable = new Variable.Builder("viewVariable", TextType.get(), table.getEntityType())
        .addAttribute("script", "$('ds.table:var1')").build();

    View viewTemplate = View.Builder.newView("view", table).list(new VariablesClause()).build();
    try(VariableWriter variableWriter = viewTemplate.getListClause().createWriter()) {
      variableWriter.writeVariable(viewVariable);
    }
    viewManager.addView("ds", viewTemplate, null);

    Collection<String> tableValues = new ArrayList<>();
    for(ValueSet valueSet : viewAwareDatasource.getValueTable("table").getValueSets()) {
      tableValues.add(table.getValue(tableVariable, valueSet).toString());
    }

    Collection<String> viewValues = new ArrayList<>();
    View view = viewManager.getView("ds", "view");
    for(ValueSet valueSet : view.getValueSets()) {
      viewValues.add(view.getValue(viewVariable, valueSet).toString());
    }
    assertThat(tableValues).isEqualTo(viewValues);
  }

  @Test
  public void test_$_basic_recursive_error() {
    CsvDatasource datasource = new CsvDatasource("ds").addValueTable("table", //
        FileUtil.getFileFromResource("org/obiba/magma/js/variables.csv"), //
        FileUtil.getFileFromResource("org/obiba/magma/js/data.csv"));

    ViewManager viewManager = new DefaultViewManagerImpl(new MemoryViewPersistenceStrategy());
    Datasource viewAwareDatasource = viewManager.decorate(datasource);
    MagmaEngine.get().addDatasource(viewAwareDatasource);

    ValueTable table = viewAwareDatasource.getValueTable("table");

    Variable viewVariable = new Variable.Builder("circular", TextType.get(), table.getEntityType())
        .addAttribute("script", "$('ds.view:circular')").build();

    View viewTemplate = View.Builder.newView("view", table).list(new VariablesClause()).build();
    try(VariableWriter variableWriter = viewTemplate.getListClause().createWriter()) {
      variableWriter.writeVariable(viewVariable);
    }
    viewManager.addView("ds", viewTemplate, null);

    try {
      View view = viewManager.getView("ds", "view");
      for(ValueSet valueSet : view.getValueSets()) {
        view.getValue(viewVariable, valueSet);
      }
      fail("Should throw CircularVariableDependencyRuntimeException");
    } catch(Exception e) {
      Throwable cause = e.getCause();
      assertThat(cause).isNotNull();
      assertThat(cause).isInstanceOf(CircularVariableDependencyRuntimeException.class);
    }
  }

  @Test
  public void test_this_inside_this() {

  }

}
