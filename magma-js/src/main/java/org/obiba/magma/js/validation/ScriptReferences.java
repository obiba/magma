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

import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import jakarta.annotation.Nullable;

import org.mozilla.javascript.CompilerEnvirons;
import org.mozilla.javascript.EvaluatorException;
import org.mozilla.javascript.Parser;
import org.mozilla.javascript.Token;
import org.mozilla.javascript.ast.AstNode;
import org.mozilla.javascript.ast.AstRoot;
import org.mozilla.javascript.ast.ElementGet;
import org.mozilla.javascript.ast.FunctionCall;
import org.mozilla.javascript.ast.KeywordLiteral;
import org.mozilla.javascript.ast.Name;
import org.mozilla.javascript.ast.NumberLiteral;
import org.mozilla.javascript.ast.StringLiteral;
import org.mozilla.javascript.ast.WithStatement;

/**
 * The variables whose values a script reads, as written in it: {@code x}, {@code table:x} or {@code ds.table:x}, to be
 * resolved as {@link org.obiba.magma.support.MagmaEngineVariableResolver} does. A reference that cannot be known
 * without running the script (a name that is computed, a value function called indirectly, or a way to reach the
 * global scope) makes the script dynamic, and then the references found are not all of them.
 * <p/>
 * Only the value functions are followed: {@code $this} stays in the current view and {@code $var} reads metadata.
 */
public record ScriptReferences(Set<String> variables, boolean dynamic) {

  /**
   * Value functions, and the positions of their arguments that name a variable.
   */
  private static final Map<String, Set<Integer>> VALUE_FUNCTIONS = Map.of(
      "$", Set.of(0),
      "$val", Set.of(0),
      "$value", Set.of(0),
      "$join", Set.of(0, 1),
      "$group", Set.of(0, 2),
      "$groups", Set.of(0, 2));

  /**
   * Names that give the global scope, or a way to compile code, from which the value functions can be reached.
   */
  private static final Set<String> ESCAPES = Set.of("eval", "Function", "globalThis", "constructor");

  public static ScriptReferences of(@Nullable String script) {
    Set<String> variables = new TreeSet<>();
    if (script == null || script.isBlank()) return new ScriptReferences(variables, false);

    CompilerEnvirons env = new CompilerEnvirons();
    env.setRecordingComments(false);
    AstRoot root;
    try {
      root = new Parser(env).parse(script, "script", 1);
    } catch (EvaluatorException e) {
      // not run either, but nothing can be said of it
      return new ScriptReferences(variables, true);
    }
    boolean[] dynamic = { false };
    root.visit(node -> {
      if (node instanceof FunctionCall call && call.getTarget() instanceof Name target
          && VALUE_FUNCTIONS.containsKey(target.getIdentifier())) {
        Set<Integer> positions = VALUE_FUNCTIONS.get(target.getIdentifier());
        for (int i = 0; i < call.getArguments().size(); i++) {
          if (!positions.contains(i)) continue;
          if (call.getArguments().get(i) instanceof StringLiteral literal) variables.add(literal.getValue());
          else dynamic[0] = true;
        }
      } else if (node instanceof Name name && isEscape(name)) {
        dynamic[0] = true;
      } else if (node instanceof ElementGet get && !isLiteralElement(get.getElement())) {
        // a computed property can be any of the names above
        dynamic[0] = true;
      } else if (node instanceof KeywordLiteral keyword && keyword.getType() == Token.THIS
          || node instanceof WithStatement) {
        dynamic[0] = true;
      }
      return true;
    });
    return new ScriptReferences(variables, dynamic[0]);
  }

  /**
   * A value function that is not called directly (aliased, applied), or a name that leads to the global scope.
   */
  private static boolean isEscape(Name name) {
    String id = name.getIdentifier();
    if (VALUE_FUNCTIONS.containsKey(id))
      return !(name.getParent() instanceof FunctionCall call && call.getTarget() == name);
    // __parent__, __proto__ and the like
    return ESCAPES.contains(id) || id.startsWith("__");
  }

  private static boolean isLiteralElement(AstNode element) {
    if (element instanceof NumberLiteral) return true;
    return element instanceof StringLiteral literal && !ESCAPES.contains(literal.getValue())
        && !VALUE_FUNCTIONS.containsKey(literal.getValue()) && !literal.getValue().startsWith("__");
  }
}
