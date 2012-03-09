/*******************************************************************************
 * Copyright (c) 2012 OBiBa. All rights reserved.
 *  
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *  
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.magma.support;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.obiba.magma.Attribute;
import org.obiba.magma.Datasource;
import org.obiba.magma.NoSuchAttributeException;
import org.obiba.magma.NoSuchValueTableException;
import org.obiba.magma.NoSuchVariableException;
import org.obiba.magma.Value;
import org.obiba.magma.ValueTable;
import org.obiba.magma.ValueTableWriter;
import org.obiba.magma.Variable;
import org.obiba.magma.support.DatasourceCopier.VariableTransformer;
import org.obiba.magma.views.AbstractTransformingValueTableWrapper;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;

/**
 *
 */
public class MultiplexingDatasource implements Datasource {

  private final Datasource wrapped;

  private final ValueTableMultiplexer tableMultiplexer;

  private final VariableTransformer variableTransformer;

  private HashMap<String, MultiplexValueTable> tables = Maps.newHashMap();

  public MultiplexingDatasource(Datasource wrapped, ValueTableMultiplexer tableMultiplexer, VariableTransformer variableTransformer) {
    super();
    this.wrapped = wrapped;
    this.variableTransformer = variableTransformer == null ? new NoOpTransformer() : variableTransformer;
    this.tableMultiplexer = tableMultiplexer == null ? new NoOpMultiplexer() : tableMultiplexer;
  }

  @Override
  public void initialise() {
    Initialisables.initialise(wrapped);
    for(ValueTable wTable : wrapped.getValueTables()) {
      for(Variable wVariable : wTable.getVariables()) {
        String tableName = tableMultiplexer.multiplex(wTable, wVariable);
        MultiplexValueTable table = tables.get(tableName);
        if(table == null) {
          table = new MultiplexValueTable(tableName, wTable);
          tables.put(tableName, table);
        }
        table.addVariable(wTable, wVariable);
      }
    }
  }

  @Override
  public void dispose() {
    Disposables.dispose(wrapped);
  }

  @Override
  public String getName() {
    return wrapped.getName();
  }

  @Override
  public String getType() {
    return wrapped.getType();
  }

  @Override
  public boolean hasValueTable(String name) {
    return tables.containsKey(name);
  }

  @Override
  public ValueTable getValueTable(String name) throws NoSuchValueTableException {
    if(tables.containsKey(name) == false) {
      throw new NoSuchValueTableException(name);
    }
    return tables.get(name);
  }

  @Override
  public Set<ValueTable> getValueTables() {
    return ImmutableSet.<ValueTable> builder().addAll(tables.values()).build();
  }

  @Override
  public boolean canDropTable(String name) {
    return false;
  }

  @Override
  public void dropTable(String name) {
    throw new UnsupportedOperationException("cannot drop table");
  }

  @Override
  public ValueTableWriter createWriter(String tableName, String entityType) {
    throw new UnsupportedOperationException("cannot write table");
  }

  //
  // Attributes methods
  //

  @Override
  public boolean hasAttributes() {
    return wrapped.hasAttributes();
  }

  @Override
  public boolean hasAttribute(String name) {
    return wrapped.hasAttribute(name);
  }

  @Override
  public Attribute getAttribute(String name) throws NoSuchAttributeException {
    return wrapped.getAttribute(name);
  }

  @Override
  public boolean hasAttribute(String name, Locale locale) {
    return wrapped.hasAttribute(name, locale);
  }

  @Override
  public Attribute getAttribute(String name, Locale locale) throws NoSuchAttributeException {
    return wrapped.getAttribute(name, locale);
  }

  @Override
  public Value getAttributeValue(String name) throws NoSuchAttributeException {
    return wrapped.getAttributeValue(name);
  }

  @Override
  public String getAttributeStringValue(String name) throws NoSuchAttributeException {
    return wrapped.getAttributeStringValue(name);
  }

  @Override
  public List<Attribute> getAttributes(String name) throws NoSuchAttributeException {
    return wrapped.getAttributes(name);
  }

  @Override
  public List<Attribute> getAttributes() {
    return wrapped.getAttributes();
  }

  @Override
  public void setAttributeValue(String name, Value value) {
    wrapped.setAttributeValue(name, value);
  }

  //
  // Inner classes and interfaces
  //

  public interface ValueTableMultiplexer {

    public String multiplex(ValueTable table, Variable variable);

  }

  public static class VariableAttributeMultiplexer implements ValueTableMultiplexer {

    private final String attributeName;

    private final String defaultName;

    public VariableAttributeMultiplexer(String attributeName) {
      this(attributeName, null);
    }

    public VariableAttributeMultiplexer(String attributeName, String defaultName) {
      this.attributeName = attributeName;
      this.defaultName = defaultName;
    }

    @Override
    public String multiplex(ValueTable table, Variable variable) {
      return variable.hasAttribute(attributeName) ? variable.getAttributeStringValue(attributeName) : getDefaultMultiplex(table);
    }

    private String getDefaultMultiplex(ValueTable table) {
      return defaultName != null ? defaultName : table.getName();
    }

  }

  private static final class NoOpMultiplexer implements ValueTableMultiplexer {
    @Override
    public String multiplex(ValueTable table, Variable variable) {
      return table.getName();
    }
  }

  public abstract static class VariableNameTransformer implements VariableTransformer {

    protected abstract String transformName(Variable variable);

    @Override
    public Variable transform(Variable variable) {
      return Variable.Builder.sameAs(variable).name(transformName(variable)).build();
    }
  }

  public static class VariableAliasTransformer extends VariableNameTransformer {

    private final String alias;

    public VariableAliasTransformer() {
      this("alias");
    }

    public VariableAliasTransformer(String alias) {
      super();
      this.alias = alias;
    }

    @Override
    protected String transformName(Variable variable) {
      return variable.hasAttribute(alias) ? variable.getAttributeStringValue(alias) : variable.getName();
    }

  }

  private static class NoOpTransformer implements VariableTransformer {
    @Override
    public Variable transform(Variable variable) {
      return variable;
    }
  }

  private class MultiplexValueTable extends AbstractTransformingValueTableWrapper {

    private final ValueTable wrappedTable;

    private final String name;

    private HashMap<String, Variable> variables = Maps.newLinkedHashMap();

    public MultiplexValueTable(String name, ValueTable wrappedTable) {
      this.name = name;
      this.wrappedTable = wrappedTable;
    }

    @Override
    public String getName() {
      return name;
    }

    @Override
    public ValueTable getWrappedValueTable() {
      return wrappedTable;
    }

    protected void addVariable(final ValueTable table, final Variable variable) {
      if(wrappedTable.getName().equals(table.getName()) == false) {
        throw new UnsupportedOperationException("cannot multiplex different tables (" + wrappedTable.getName() + ", " + table.getName() + ") into the same table: " + getName());
      }
      Variable transformed = variableTransformer.transform(variable);
      if(variables.containsKey(transformed.getName())) {
        throw new UnsupportedOperationException("cannot transform several variables (" + variable.getName() + ",...) into variable with same name: " + transformed.getName());
      }
      variables.put(transformed.getName(), transformed);
      addVariableNameMapping(transformed.getName(), variable.getName());
    }

    @Override
    public Iterable<Variable> getVariables() {
      return ImmutableSet.<Variable> builder().addAll(variables.values()).build();
    }

    @Override
    public Variable getVariable(String name) throws NoSuchVariableException {
      return variables.get(name);
    }

  }

}