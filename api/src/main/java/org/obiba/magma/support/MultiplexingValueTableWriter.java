package org.obiba.magma.support;

import java.io.IOException;
import java.util.Map;
import java.util.Set;

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

  private ValueTable source;

  private DatasourceCopier copier;

  private final Datasource destination;

  private final MultiplexingStrategy strategy;

  private Map<String, ValueTableWriter> writers = Maps.newHashMap();

  public MultiplexingValueTableWriter(ValueTable source, DatasourceCopier copier, Datasource destination, MultiplexingStrategy strategy) {
    this.source = source;
    this.copier = copier;
    this.destination = destination;
    this.strategy = strategy;
  }

  @Override
  public ValueSetWriter writeValueSet(VariableEntity entity) {
    return new MultiplexedValueSetWriter(entity);
  }

  @Override
  public VariableWriter writeVariables() {
    return new MultiplexedVariableWriter();
  }

  @Override
  public void close() throws IOException {
    for(ValueTableWriter writer : writers.values()) {
      writer.close();
    }
  }

  private ValueTableWriter lookupWriter(Variable variable, String tableName) {
    ValueTableWriter writer = writers.get(tableName);
    if(writer == null) {
      copier.notifyListeners(source, tableName, false);
      writer = copier.createValueTableWriter(source, tableName, destination);
      writers.put(tableName, writer);
    }
    return writer;
  }

  private class MultiplexedVariableWriter implements VariableWriter {

    private Map<ValueTableWriter, VariableWriter> writers = Maps.newHashMap();

    @Override
    public void writeVariable(Variable variable) {
      ValueTableWriter vtw = lookupWriter(variable, strategy.multiplexVariable(variable));
      VariableWriter writer = writers.get(vtw);
      if(writer == null) {
        writer = vtw.writeVariables();
        writers.put(vtw, writer);
      }
      writer.writeVariable(variable);
    }

    @Override
    public void close() throws IOException {
      for(VariableWriter vw : writers.values()) {
        vw.close();
      }
      writers.clear();
    }

  }

  public class MultiplexedValueSetWriter implements ValueSetWriter {

    private VariableEntity entity;

    private Map<ValueTableWriter, ValueSetWriter> writers = Maps.newHashMap();

    private Set<String> tables = Sets.newLinkedHashSet();

    public MultiplexedValueSetWriter(VariableEntity entity) {
      this.entity = entity;
    }

    public Set<String> getTables() {
      return tables;
    }

    @Override
    public void writeValue(Variable variable, Value value) {
      String tableName = strategy.multiplexValueSet(entity, variable);
      tables.add(tableName);
      ValueTableWriter vtw = lookupWriter(variable, tableName);

      ValueSetWriter writer = writers.get(vtw);
      if(writer == null) {
        writer = vtw.writeValueSet(entity);
        writers.put(vtw, writer);
      }
      writer.writeValue(variable, value);
    }

    @Override
    public void close() throws IOException {
      for(ValueSetWriter vsw : writers.values()) {
        vsw.close();
      }
      writers.clear();
    }

  }

}