package org.obiba.magma;

import javax.annotation.Nullable;
import javax.validation.constraints.NotNull;

public class NoSuchValueTableException extends MagmaRuntimeException {

  private static final long serialVersionUID = 5887330656285998606L;

  @NotNull
  private final String tableName;

  @Nullable
  private String datasourceName;

  public NoSuchValueTableException(@NotNull String tableName) {
    super("No value table exists with the specified name '" + tableName + "'");
    this.tableName = tableName;
  }

  public NoSuchValueTableException(@Nullable String datasourceName, @NotNull String tableName) {
    super("No value table exists with the specified name '" + datasourceName + '.' + tableName + "'");
    this.datasourceName = datasourceName;
    this.tableName = tableName;
  }

  @Nullable
  public String getDatasourceName() {
    return datasourceName;
  }

  @NotNull
  public String getTableName() {
    return tableName;
  }
}
