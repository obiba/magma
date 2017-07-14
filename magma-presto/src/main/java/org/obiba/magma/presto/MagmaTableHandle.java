package org.obiba.magma.presto;

import com.facebook.presto.spi.ColumnMetadata;
import com.facebook.presto.spi.ConnectorTableHandle;
import com.facebook.presto.spi.SchemaTableName;
import org.obiba.magma.ValueTable;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class MagmaTableHandle implements ConnectorTableHandle {

  private final SchemaTableName schemaTableName;

  private final ValueTable table;

  public MagmaTableHandle(SchemaTableName schemaTableName, ValueTable table) {
    this.schemaTableName = schemaTableName;
    this.table = table;
  }

  public SchemaTableName getSchemaTableName() {
    return schemaTableName;
  }

  public List<ColumnMetadata> getColumns() {
    return StreamSupport.stream(table.getVariables().spliterator(), false)
        .map(variable -> new ColumnMetadata(variable.getName(), TypeConverter.convert(variable.getValueType())))
        .collect(Collectors.toList());
  }

}
