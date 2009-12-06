package org.obiba.magma;

public class NoSuchDatasourceException extends MagmaRuntimeException {

  private static final long serialVersionUID = 5887330656285998606L;

  public NoSuchDatasourceException(String datasourceName) {
    super("No datasource exists with the specified name '" + datasourceName + "'");
  }

}
