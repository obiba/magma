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

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import jakarta.annotation.Nullable;

import org.mozilla.javascript.CompilerEnvirons;
import org.mozilla.javascript.EvaluatorException;
import org.mozilla.javascript.Parser;
import org.mozilla.javascript.Token;
import org.mozilla.javascript.ast.AstNode;
import org.mozilla.javascript.ast.ElementGet;
import org.mozilla.javascript.ast.FunctionCall;
import org.mozilla.javascript.ast.KeywordLiteral;
import org.mozilla.javascript.ast.Name;
import org.mozilla.javascript.ast.NodeVisitor;
import org.mozilla.javascript.ast.NumberLiteral;
import org.mozilla.javascript.ast.StringLiteral;
import org.mozilla.javascript.ast.WithStatement;

/**
 * The variables whose values a script reads, as written in it: {@code x}, {@code table:x} or {@code ds.table:x}, to be
 * resolved as {@link org.obiba.magma.support.MagmaEngineVariableResolver} does. A reference that cannot be known
 * without running the script (a name that is computed, a value function called indirectly, or a way to reach the
 * global scope) makes the script dynamic, and then the references found are not all of them.
 * <p/>
 * Only the value functions are followed: {@code $this} stays in the current view, and {@code $var} and the like read
 * metadata. A call to any other {@code $} function, such as one from an extra
 * {@link org.obiba.magma.js.GlobalMethodProvider}, makes the script dynamic.
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
      "$group", Set.of(0, 2));

  /**
   * Global functions that read no value of another variable.
   */
  private static final Set<String> OTHER_FUNCTIONS = Set.of("$this", "$var", "$variable", "$id", "$identifier",
      "$created", "$lastupdate", "now", "log", "newValue", "newSequence");

  /**
   * Names that give the global scope, a way to compile code, or a way to read a property whose name is computed, from
   * which the value functions can be reached.
   */
  private static final Set<String> ESCAPES = Set.of("eval", "Function", "globalThis", "constructor", "Reflect",
      "getOwnPropertyDescriptor", "getOwnPropertyDescriptors", "getOwnPropertyNames");

  public static ScriptReferences of(@Nullable String script) {
    if (script == null || script.isBlank()) return new ScriptReferences(Set.of(), false);

    CompilerEnvirons env = new CompilerEnvirons();
    env.setRecordingComments(false);
    Visitor visitor = new Visitor();
    try {
      new Parser(env).parse(script, "script", 1).visit(visitor);
    } catch (EvaluatorException | StackOverflowError e) {
      // not run either, but nothing can be said of it
      return new ScriptReferences(Set.of(), true);
    }
    return new ScriptReferences(visitor.variables, visitor.dynamic);
  }

  /**
   * Global functions, in {@link org.obiba.magma.js.methods.GlobalMethods}, this class knows about.
   */
  static Set<String> knownFunctions() {
    Set<String> names = new TreeSet<>(VALUE_FUNCTIONS.keySet());
    names.addAll(OTHER_FUNCTIONS);
    return names;
  }

  private static final class Visitor implements NodeVisitor {

    private final Set<String> variables = new TreeSet<>();

    private boolean dynamic;

    @Override
    public boolean visit(AstNode node) {
      if (node instanceof FunctionCall call && call.getTarget() instanceof Name target) {
        visitCall(target.getIdentifier(), call.getArguments());
      } else if (node instanceof Name name && isEscape(name)) {
        dynamic = true;
      } else if (node instanceof StringLiteral literal && isEscape(literal.getValue())) {
        // ''['constructor'], {'constructor': c} = '', getOwnPropertyDescriptor(p, 'constructor')
        dynamic = true;
      } else if (node instanceof ElementGet get
          && !(get.getElement() instanceof StringLiteral literal && !VALUE_FUNCTIONS.containsKey(literal.getValue())
          || get.getElement() instanceof NumberLiteral)) {
        // a computed property can be any of the names above, and o['$join'] is an indirect call
        dynamic = true;
      } else if (node instanceof KeywordLiteral keyword && keyword.getType() == Token.THIS
          || node instanceof WithStatement) {
        dynamic = true;
      }
      return true;
    }

    private void visitCall(String function, List<AstNode> arguments) {
      Set<Integer> positions = VALUE_FUNCTIONS.get(function);
      if (positions == null) {
        if (function.startsWith("$") && !OTHER_FUNCTIONS.contains(function)) dynamic = true;
        return;
      }
      // with two arguments, $group reads all the variables of the occurrence group
      if ("$group".equals(function) && arguments.size() != 3) dynamic = true;
      for (int i = 0; i < arguments.size(); i++) {
        if (!positions.contains(i)) continue;
        if (arguments.get(i) instanceof StringLiteral literal) variables.add(literal.getValue());
        else dynamic = true;
      }
    }
  }

  /**
   * A value function that is not called directly (aliased, applied), or a name that leads to the global scope.
   */
  private static boolean isEscape(Name name) {
    String id = name.getIdentifier();
    if (VALUE_FUNCTIONS.containsKey(id))
      return !(name.getParent() instanceof FunctionCall call && call.getTarget() == name);
    return isEscape(id);
  }

  private static boolean isEscape(String id) {
    // __parent__, __proto__ and the like
    return ESCAPES.contains(id) || id.startsWith("__");
  }
}
