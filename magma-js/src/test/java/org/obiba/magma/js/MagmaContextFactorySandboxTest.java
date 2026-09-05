/*
 * Copyright (c) 2026 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.magma.js;

import org.junit.Test;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.ContextFactory;
import org.mozilla.javascript.EcmaError;
import org.mozilla.javascript.EvaluatorException;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;
import org.obiba.magma.type.TextType;

import java.io.File;

import static org.fest.assertions.api.Assertions.assertThat;

/**
 * Scripts run in the JVM of the server that evaluates them: they must not be able to reach Java classes, neither
 * through the Rhino globals nor through the reflection of a wrapped Java object.
 */
public class MagmaContextFactorySandboxTest extends AbstractJsTest {

  @Test
  public void javaGlobalsAreNotDefined() {
    for(String global : new String[] { "Packages", "java", "javax", "org", "com", "getClass", "JavaAdapter", "JavaImporter" }) {
      assertThat(evaluateRaw("typeof " + global)).as(global).isEqualTo("undefined");
    }
  }

  // a harmless probe of the JVM: should the sandbox regress, the test must not run anything on the build machine

  @Test(expected = EcmaError.class)
  public void jvmCannotBeReachedThroughPackages() {
    evaluateRaw("Packages.java.lang.System.getProperty('java.version')");
  }

  @Test(expected = EcmaError.class)
  public void jvmCannotBeReachedThroughTopLevelPackage() {
    evaluateRaw("java.lang.System.getProperty('java.version')");
  }

  @Test(expected = EvaluatorException.class)
  public void javaObjectCannotBeWrappedForScripts() {
    ContextFactory.getGlobal().call(cx -> Context.javaToJS(new File("/"), getMagmaContext().newLocalScope()));
  }

  @Test(expected = EvaluatorException.class)
  public void magmaObjectCannotBeWrappedForScriptsEither() {
    // Magma values reach scripts through their prototypes, never as raw Java objects
    ContextFactory.getGlobal().call(cx -> Context.javaToJS(TextType.get(), getMagmaContext().newLocalScope()));
  }

  @Test
  public void scriptsStillSeeMagmaValues() {
    assertThat(evaluate("map({'hello': 'world'})", TextType.get().valueOf("hello")).getValue().toString()).isEqualTo("world");
  }

  private Object evaluateRaw(String script) {
    return ContextFactory.getGlobal().call(cx -> {
      Scriptable scope = getMagmaContext().newLocalScope();
      return cx.evaluateString(scope, script, "", 1, null);
    });
  }
}
