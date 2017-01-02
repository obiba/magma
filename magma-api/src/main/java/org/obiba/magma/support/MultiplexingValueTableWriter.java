/*
 * Copyright (c) 2017 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.magma.support;

import java.util.Map;
import java.util.Set;

import javax.validation.constraints.NotNull;

import org.obiba.magma.Datasource;
import org.obiba.magma.Value;
import org.obiba.magma.ValueTable;
import org.obiba.magma.ValueTableWriter;
import org.obiba.magma.Variable;
import org.obiba.magma.VariableEntity;
import org.obiba.magma.support.DatasourceCopier.MultiplexingStrategy;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

public class MultiplexingValueTableWriter implements ValueTableWriter {

  private final ValueTable source;

  private final DatasourceCopier copier;

  private final Datasource destination;

  private final MultiplexingStrategy strategy;

  private final Map<String, ValueTableWriter> writers = Maps.newHashMap();

  public MultiplexingValueTableWriter(ValueTable source, DatasourceCopier copier, Datasource destination,
      MultiplexingStrategy strategy) {
    this.source = source;
    this.copier = copier;
    this.destination = destination;
    this.strategy = strategy;
  }

  @NotNull
  @Override
  public ValueSetWriter writeValueSet(@NotNull VariableEntity entity) {
    return new MultiplexedValueSetWriter(entity);
  }

  @Override
  public VariableWriter writeVariables() {
    return new MultiplexedVariableWriter();
  }

  @Override
  public void close() {
    for(ValueTableWriter writer : writers.values()) {
      writer.close();
    }
  }

  @SuppressWarnings({ "AssignmentToMethodParameter", "PMD.AvoidReassigningParameters" })
  private ValueTableWriter lookupWriter(String tableName) {
    if(tableName == null) {
      tableName = source.getName();
    }
    ValueTableWriter writer = writers.get(tableName);
    if(writer == null) {
      copier.notifyListeners(source, tableName, false);
      writer = copier.createValueTableWriter(source, tableName, destination);
      writers.put(tableName, writer);
    }
    return writer;
  }

  private class MultiplexedVariableWriter implements VariableWriter {

    private final Map<ValueTableWriter, VariableWriter> writers = Maps.newHashMap();

    @Override
    public void writeVariable(@NotNull Variable variable) {
      ValueTableWriter vtw = lookupWriter(strategy.multiplexVariable(variable));
      VariableWriter writer = writers.get(vtw);
      if(writer == null) {
        writer = vtw.writeVariables();
        writers.put(vtw, writer);
      }
      writer.writeVariable(variable);
    }

    @Override
    public void removeVariable(@NotNull Variable variable) {
      throw new UnsupportedOperationException("Variable cannot be removed from a Multiplexing table");
    }

    @Override
    public void close() {
      for(VariableWriter vw : writers.values()) {
        vw.close();
      }
      writers.clear();
    }

  }

  public class MultiplexedValueSetWriter implements ValueSetWriter {

    private final VariableEntity entity;

    private final Map<ValueTableWriter, ValueSetWriter> writers = Maps.newHashMap();

    private final Set<String> tables = Sets.newLinkedHashSet();

    public MultiplexedValueSetWriter(VariableEntity entity) {
      this.entity = entity;
    }

    public Set<String> getTables() {
      return tables;
    }

    @Override
    public void writeValue(@NotNull Variable variable, Value value) {
      String tableName = strategy.multiplexValueSet(entity, variable);
      tables.add(tableName);
      ValueTableWriter vtw = lookupWriter(tableName);

      ValueSetWriter writer = writers.get(vtw);
      if(writer == null) {
        writer = vtw.writeValueSet(entity);
        writers.put(vtw, writer);
      }
      writer.writeValue(variable, value);
    }

    @Override
    public void remove() {
      throw new UnsupportedOperationException();
    }

    @Override
    public void close() {
      for(ValueSetWriter vsw : writers.values()) {
        vsw.close();
      }
      writers.clear();
    }

  }

}