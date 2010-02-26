package org.obiba.magma;

public class NoSuchDatasourceException extends MagmaRuntimeException {

  private static final long serialVersionUID = 5887330656285998606L;

  private String datasourceName;

  public NoSuchDatasourceException(String datasourceName) {
    super("No datasource exists with the specified name '" + datasourceName + "'");
    this.datasourceName = datasourceName;
  }

  public String getDatasourceName() {
    return datasourceName;
  }

}
