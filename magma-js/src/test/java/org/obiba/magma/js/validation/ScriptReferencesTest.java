/*
 * Copyright (c) 2026 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.magma.js.validation;

import org.junit.Test;

import static org.fest.assertions.api.Assertions.assertThat;

public class ScriptReferencesTest {

  @Test
  public void test_literal_references() {
    ScriptReferences refs = ScriptReferences.of(
        "// $('commented.Out:x')\n"
            + "$('x').plus($val('T:y')).plus($value('ds.T:z'))"
            + ".plus($join('ds.Lookup:v', 'ds.T:id')).plus($group('g', 1, 's')['s'])");

    assertThat(refs.dynamic()).isFalse();
    assertThat(refs.variables()).containsOnly("x", "T:y", "ds.T:z", "ds.Lookup:v", "ds.T:id", "g", "s");
  }

  @Test
  public void test_metadata_and_view_functions_are_not_references() {
    ScriptReferences refs = ScriptReferences.of("$this('a').plus($var('ds.T:b').name()).plus($id())");

    assertThat(refs.dynamic()).isFalse();
    assertThat(refs.variables()).isEmpty();
  }

  @Test
  public void test_usual_script_is_not_dynamic() {
    ScriptReferences refs = ScriptReferences.of(
        "var a = $('x'); var o = {k: 1}; a.isNull().value() ? o['k'] : [1, 2][0]");

    assertThat(refs.dynamic()).isFalse();
    assertThat(refs.variables()).containsOnly("x");
  }

  @Test
  public void test_computed_reference_is_dynamic() {
    assertDynamic("$join('ds.' + 'B:secret', 'id')");
    assertDynamic("$('ds.T:' + name)");
    assertDynamic("$(`ds.T:x`)");
    assertDynamic("$group('g', 1, s)");
  }

  @Test
  public void test_indirect_call_is_dynamic() {
    assertDynamic("var f = $join; f('ds.B:secret', 'id')");
    assertDynamic("$join.call(null, 'ds.B:secret', 'id')");
    assertDynamic("this['$jo' + 'in']('ds.B:secret', 'id')");
    assertDynamic("this['$join']('ds.B:secret', 'id')");
    assertDynamic("eval(\"$join('ds.B:secret', 'id')\")");
    assertDynamic("Function('return $join')()('ds.B:secret', 'id')");
    assertDynamic("''.constructor.constructor('return this')()");
    assertDynamic("''['constr' + 'uctor']");
    assertDynamic("({}).__parent__");
    assertDynamic("globalThis");
    assertDynamic("with (o) { x }");
  }

  @Test
  public void test_unparsable_script_is_dynamic() {
    assertDynamic("$('x'");
  }

  @Test
  public void test_empty_script() {
    assertThat(ScriptReferences.of(null).dynamic()).isFalse();
    assertThat(ScriptReferences.of(" ").variables()).isEmpty();
  }

  private static void assertDynamic(String script) {
    assertThat(ScriptReferences.of(script).dynamic()).as(script).isTrue();
  }
}
