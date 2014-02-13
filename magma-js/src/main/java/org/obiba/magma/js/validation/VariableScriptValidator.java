package org.obiba.magma.js.validation;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.Nullable;
import javax.validation.constraints.NotNull;

import org.mozilla.javascript.CompilerEnvirons;
import org.mozilla.javascript.Parser;
import org.mozilla.javascript.ast.AstRoot;
import org.obiba.magma.AttributeAware;
import org.obiba.magma.MagmaEngine;
import org.obiba.magma.ValueTable;
import org.obiba.magma.Variable;
import org.obiba.magma.js.MagmaJsEvaluationRuntimeException;
import org.obiba.magma.support.MagmaEngineVariableResolver;
import org.obiba.magma.support.ValueTableWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.google.common.base.Stopwatch;
import com.google.common.base.Strings;
import com.google.common.base.Throwables;
import com.google.common.collect.ImmutableSet;

import static org.obiba.magma.js.JavascriptVariableBuilder.SCRIPT_ATTRIBUTE_NAME;

@SuppressWarnings("ConstantNamingConvention")
public class VariableScriptValidator {

  private static final Logger log = LoggerFactory.getLogger(VariableScriptValidator.class);

  private static final Pattern $_CALL = Pattern.compile("\\$\\(['\"](([\\d\\w.:\\-_]*))['\"]\\)");

  private static final Pattern $THIS_CALL = Pattern.compile("\\$this\\(['\"](([\\d\\w.:\\-_]*))['\"]\\)");

  private static final Pattern $VAR_CALL = Pattern.compile("\\$var\\(['\"](([\\d\\w.:\\-_]*))['\"]\\)");

  //  private static final Pattern $JOIN_CALL = Pattern.compile("(\\$join\\((['\"](([\\d\\w.:]*))['\"])*\\))");

  private static final CompilerEnvirons COMPILER_ENVIRONS = new CompilerEnvirons();

  static {
    COMPILER_ENVIRONS.setRecordingLocalJsDocComments(false);
    COMPILER_ENVIRONS.setAllowSharpComments(false);
    COMPILER_ENVIRONS.setRecordingComments(false);
  }

  @NotNull
  private final Variable variable;

  @NotNull
  private final ValueTable table;

  public VariableScriptValidator(@NotNull Variable variable, @NotNull ValueTable table) {
    //noinspection ConstantConditions
    Preconditions.checkArgument(table != null, "Cannot validate script with null table/view for " + variable.getName());
    this.variable = variable;
    this.table = table;
  }

  public void validateScript() throws VariableScriptValidationException {
    Stopwatch stopwatch = Stopwatch.createStarted();
    try {
      getVariableRefNode(new VariableRefNode(variable.getVariableReference(table), table, getScript(variable)));
    } catch(Exception e) {
      Throwables.propagateIfInstanceOf(e, CircularVariableDependencyException.class);
      throw new VariableScriptValidationException(e);
    }
    log.debug("Script validation of {} in {}", variable.getName(), stopwatch);
  }

  private static void getVariableRefNode(@NotNull VariableRefNode callerNode) {
    String script = callerNode.getScript();
    if(Strings.isNullOrEmpty(script)) {
      log.trace("{} has no script", callerNode.getVariableRef());
    } else {
      log.trace("Analyze {} script: {}", callerNode.getVariableRef(), script);
      for(VariableRefCall variableRefCall : parseScript(script)) {
        VariableRefNode calleeNode = asNode(variableRefCall, callerNode.getValueTable());
        callerNode.addCallee(calleeNode);
        getVariableRefNode(calleeNode);
      }
    }
  }

  @VisibleForTesting
  static Set<VariableRefCall> parseScript(String script) {
    String clearScript = clearScriptComments(script);
    ImmutableSet.Builder<VariableRefCall> builder = ImmutableSet.builder();
    parseSingleArgGlobalMethod(clearScript, $_CALL, "$", builder);
    parseSingleArgGlobalMethod(clearScript, $THIS_CALL, "$this", builder);
    parseSingleArgGlobalMethod(clearScript, $VAR_CALL, "$var", builder);
    return builder.build();
  }

  private static String clearScriptComments(String script) {
    AstRoot node = new Parser(COMPILER_ENVIRONS).parse(script, "script", 1);
    return node.toSource();
  }

  private static void parseSingleArgGlobalMethod(CharSequence script, Pattern pattern, String method,
      ImmutableSet.Builder<VariableRefCall> builder) {
    Matcher matcher = pattern.matcher(script);
    while(matcher.find()) {
      if(matcher.groupCount() == 2) {
        builder.add(new VariableRefCall(method, matcher.group(1)));
      }
    }
  }

  private static VariableRefNode asNode(VariableRefCall variableRefCall, @NotNull ValueTable table) {

    MagmaEngineVariableResolver reference = MagmaEngineVariableResolver.valueOf(variableRefCall.getVariableRef());
    switch(variableRefCall.getMethod()) {
      case "$":
        if(reference.getDatasourceName() == null || reference.getTableName() == null) {
          if(table.isView()) {
            ValueTable wrappedTable = ((ValueTableWrapper) table).getWrappedValueTable();
            Variable variable = reference.resolveSource(wrappedTable).getVariable();
            return new VariableRefNode(Variable.Reference.getReference(wrappedTable, variable), wrappedTable,
                getScript(variable));
          }
          Variable variable = reference.resolveSource(table).getVariable();
          return new VariableRefNode(Variable.Reference.getReference(table, variable), table, getScript(variable));
        }
        Variable variable = reference.resolveSource().getVariable();
        return new VariableRefNode(Variable.Reference
            .getReference(reference.getDatasourceName(), reference.getTableName(), variable.getName()),
            MagmaEngine.get().getDatasource(reference.getDatasourceName()).getValueTable(reference.getTableName()),
            getScript(variable));
      case "$this":
      case "$var":
        Variable thisVariable = reference.resolveSource(table).getVariable();
        return new VariableRefNode(Variable.Reference.getReference(table, thisVariable), table,
            getScript(thisVariable));
      default:
        throw new MagmaJsEvaluationRuntimeException("Unsupported method validation for " + variableRefCall.getMethod());
    }
  }

  @Nullable
  private static String getScript(AttributeAware variable) {
    return variable.hasAttribute(SCRIPT_ATTRIBUTE_NAME) //
        ? variable.getAttributeStringValue(SCRIPT_ATTRIBUTE_NAME) //
        : null;
  }

  @VisibleForTesting
  static class VariableRefCall {

    @NotNull
    private final String method;

    @NotNull
    private final String variableRef;

    VariableRefCall(@NotNull String method, @NotNull String variableRef) {
      this.method = method;
      this.variableRef = variableRef;
    }

    @NotNull
    public String getMethod() {
      return method;
    }

    @NotNull
    public String getVariableRef() {
      return variableRef;
    }

    @Override
    public String toString() {
      return method + "('" + variableRef + "')";
    }

    @Override
    public int hashCode() {
      return Objects.hashCode(method, variableRef);
    }

    @Override
    public boolean equals(Object obj) {
      if(this == obj) return true;
      if(obj == null || getClass() != obj.getClass()) return false;
      VariableRefCall other = (VariableRefCall) obj;
      return Objects.equal(method, other.method) && Objects.equal(variableRef, other.variableRef);
    }

  }

  static class VariableRefNode implements Serializable {

    private static final long serialVersionUID = -6622597054116817497L;

    @NotNull
    private final String variableRef;

    @NotNull
    private transient final ValueTable valueTable;

    @Nullable
    private final String script;

    private final Set<VariableRefNode> callers = new HashSet<>();

    private final Set<VariableRefNode> callees = new HashSet<>();

    VariableRefNode(@NotNull String variableRef, @NotNull ValueTable valueTable, @Nullable String script) {
      this.variableRef = variableRef;
      this.valueTable = valueTable;
      this.script = script;
    }

    @NotNull
    public String getVariableRef() {
      return variableRef;
    }

    public Set<VariableRefNode> getCallers() {
      return callers;
    }

    public Set<VariableRefNode> getCallees() {
      return callees;
    }

    @Nullable
    public String getScript() {
      return script;
    }

    @NotNull
    public ValueTable getValueTable() {
      return valueTable;
    }

    public void addCallee(@NotNull VariableRefNode callee) throws CircularVariableDependencyException {
      callee.callers.add(this);
      callees.add(callee);
      checkCircularDependencies(callee, new HashSet<VariableRefNode>());
    }

    private static void checkCircularDependencies(@Nullable VariableRefNode node,
        Collection<VariableRefNode> callersList) throws CircularVariableDependencyException {
      if(node == null) return;
      if(callersList.contains(node)) {
        throw new CircularVariableDependencyException(node);
      }
      callersList.add(node);
      for(VariableRefNode caller : node.getCallers()) {
        checkCircularDependencies(caller, callersList);
      }
    }

    @Override
    @SuppressWarnings("SimplifiableIfStatement")
    public boolean equals(Object o) {
      if(this == o) return true;
      if(!(o instanceof VariableRefNode)) return false;
      return variableRef.equals(((VariableRefNode) o).variableRef);
    }

    @Override
    public int hashCode() {
      return variableRef.hashCode();
    }

    @Override
    public String toString() {
      return Objects.toStringHelper(this).omitNullValues().addValue(variableRef).add("callers", callers).toString();
    }

  }
}
