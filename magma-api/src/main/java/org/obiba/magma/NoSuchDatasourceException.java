package org.obiba.magma;

import javax.validation.constraints.NotNull;

public class NoSuchDatasourceException extends MagmaRuntimeException {

  private static final long serialVersionUID = 5887330656285998606L;

  @NotNull
  private final String datasourceName;

  public NoSuchDatasourceException(@NotNull String datasourceName) {
    super("No datasource exists with the specified name '" + datasourceName + "'");
    this.datasourceName = datasourceName;
  }

  @NotNull
  public String getDatasourceName() {
    return datasourceName;
  }

}
