package org.obiba.magma.js.views;

import org.junit.Test;
import org.obiba.magma.ValueSet;
import org.obiba.magma.Variable;
import org.obiba.magma.js.AbstractJsTest;

import static org.easymock.EasyMock.createMock;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class JavascriptClauseTest extends AbstractJsTest {

  @Test
  public void test_DefaultCtor() {
    new JavascriptClause();
  }

  @Test
  public void test_StringCtor() {
    String script = "the script";
    JavascriptClause clause = new JavascriptClause(script);
    assertThat(clause.getScript(), is(script));
  }

  @Test
  public void test_setScript_assignsScript() {
    JavascriptClause clause = new JavascriptClause();
    String script = "the New Script";
    clause.setScript(script);
    assertThat(clause.getScript(), is(script));
  }

  @Test
  public void test_getScriptName_returnsDefaultName() {
    JavascriptClause clause = new JavascriptClause();
    assertThat(clause.getScriptName(), is("customScript"));
  }

  @Test
  public void test_setScriptName_assignsName() {
    JavascriptClause clause = new JavascriptClause();
    String newName = "My Script";
    clause.setScriptName(newName);
    assertThat(clause.getScriptName(), is(newName));
  }

  @Test(expected = NullPointerException.class)
  public void test_initialise_throwsNullPointerWhenScriptIsNull() {
    JavascriptClause clause = new JavascriptClause();
    clause.initialise();
  }

  @Test(expected = IllegalStateException.class)
  public void test_select_throwsIllegalStateException() {
    JavascriptClause clause = new JavascriptClause();
    clause.select(null);
  }

  @Test(expected = IllegalArgumentException.class)
  public void test_select_throwsIllegalArgumentException() {
    JavascriptClause clause = new JavascriptClause("true");
    clause.initialise();
    clause.select(null);
  }

  @Test
  public void test_select_handlesBooleanReturnValue() {
    assertSelect("true", true);
    assertSelect("false", false);
  }

  @Test
  public void test_select_returnsFalseWhenScriptReturnsNonBoolean() {
    assertSelect("1.0", false);
    assertSelect("1", false);
    assertSelect("'foo'", false);
  }

  @Test
  public void test_select_returnsFalseWhenScriptReturnsScriptableValueThatIsNotBooleanType() {
    assertSelect("now()", false);
  }

  @Test(expected = IllegalStateException.class)
  public void test_where_throwsIllegalStateException() {
    JavascriptClause clause = new JavascriptClause();
    clause.where(null);
  }

  @Test(expected = IllegalArgumentException.class)
  public void test_where_throwsIllegalArgumentException() {
    JavascriptClause clause = new JavascriptClause("true");
    clause.initialise();
    clause.where(null);
  }

  @Test
  public void test_where_handlesBooleanReturnValue() {
    assertWhere("true", true);
    assertWhere("false", false);
  }

  @Test
  public void test_where_returnsFalseWhenScriptReturnsNonBoolean() {
    assertWhere("1.0", false);
    assertWhere("1", false);
    assertWhere("'foo'", false);
  }

  @Test
  public void test_where_returnsFalseWhenScriptReturnsScriptableValueThatIsNotBooleanType() {
    assertWhere("now()", false);
  }

  private void assertSelect(String script, boolean expected) {
    JavascriptClause clause = new JavascriptClause(script);
    clause.initialise();
    boolean selected = clause.select(createMock(Variable.class));
    assertThat(selected, is(expected));
  }

  private void assertWhere(String script, boolean expected) {
    JavascriptClause clause = new JavascriptClause(script);
    clause.initialise();
    boolean selected = clause.where(createMock(ValueSet.class));
    assertThat(selected, is(expected));
  }
}
