package org.obiba.magma.presto;

import com.facebook.presto.spi.*;
import com.facebook.presto.spi.connector.ConnectorMetadata;
import com.google.common.collect.ImmutableList;
import org.obiba.magma.Datasource;

import java.util.*;
import java.util.stream.Collectors;

public class MagmaMetadata implements ConnectorMetadata {

  private final List<Datasource> datasources;

  public MagmaMetadata(Datasource datasource) {
    this.datasources = ImmutableList.of(datasource);
  }

  public MagmaMetadata(Collection<Datasource> datasource) {
    this.datasources = ImmutableList.copyOf(datasource);
  }

  @Override
  public List<String> listSchemaNames(ConnectorSession session) {
    return datasources.stream().map(ds -> ds.getName()).collect(Collectors.toList());
  }

  @Override
  public ConnectorTableHandle getTableHandle(ConnectorSession session, SchemaTableName tableName) {
    return new MagmaTableHandle(tableName, getDatasource(tableName.getSchemaName()).getValueTable(tableName.getTableName()));
  }

  @Override
  public List<ConnectorTableLayoutResult> getTableLayouts(ConnectorSession session, ConnectorTableHandle table, Constraint<ColumnHandle> constraint, Optional<Set<ColumnHandle>> desiredColumns) {
    MagmaTableHandle tableHandle = MagmaTableHandle.class.cast(table);
    return null;
  }

  @Override
  public ConnectorTableLayout getTableLayout(ConnectorSession session, ConnectorTableLayoutHandle handle) {
    return new ConnectorTableLayout(handle);
  }

  @Override
  public ConnectorTableMetadata getTableMetadata(ConnectorSession session, ConnectorTableHandle table) {
    MagmaTableHandle tableHandle = MagmaTableHandle.class.cast(table);
    return new ConnectorTableMetadata(tableHandle.getSchemaTableName(), tableHandle.getColumns());
  }

  @Override
  public List<SchemaTableName> listTables(ConnectorSession session, String schemaNameOrNull) {
    return getDatasource(schemaNameOrNull).getValueTables().stream()
        .map(vt -> new SchemaTableName(schemaNameOrNull, vt.getName())).collect(Collectors.toList());
  }

  @Override
  public Map<String, ColumnHandle> getColumnHandles(ConnectorSession session, ConnectorTableHandle tableHandle) {
    return MagmaTableHandle.class.cast(tableHandle).getColumns().stream().collect(Collectors.toMap(column -> column.getName(), column -> new MagmaColumnHandle()));
  }

  @Override
  public ColumnMetadata getColumnMetadata(ConnectorSession session, ConnectorTableHandle tableHandle, ColumnHandle columnHandle) {
    return null;
  }

  @Override
  public Map<SchemaTableName, List<ColumnMetadata>> listTableColumns(ConnectorSession session, SchemaTablePrefix prefix) {
    return null;
  }

  private Datasource getDatasource(String schemaName) {
    return datasources.stream().filter(ds -> ds.getName().equals(schemaName)).findFirst().get();
  }
}
