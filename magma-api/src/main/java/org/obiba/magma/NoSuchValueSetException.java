package org.obiba.magma;

public class NoSuchValueSetException extends MagmaRuntimeException {

  private static final long serialVersionUID = 5887330656285998606L;

  private final String table;

  private final String entity;

  public NoSuchValueSetException(String table, String entity) {
    super("No ValueSet in table '" + table + "' for entity '" + entity + "'");
    this.table = table;
    this.entity = entity;
  }

  public NoSuchValueSetException(ValueTable table, VariableEntity entity) {
    this(table.getName(), entity.toString());
  }

  public String getTable() {
    return table;
  }

  public String getEntity() {
    return entity;
  }
}
