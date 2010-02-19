package org.obiba.magma;

public class DuplicateDatasourceNameException extends MagmaRuntimeException {

  private static final long serialVersionUID = 2416927981990092192L;

  private final Datasource existing;

  private final Datasource duplicate;

  public DuplicateDatasourceNameException(Datasource existing, Datasource duplicate) {
    super("Datasource with name '" + existing.getName() + "' already exists in MagmaEngine.");
    this.existing = existing;
    this.duplicate = duplicate;
  }

  public Datasource getExisting() {
    return existing;
  }

  public Datasource getDuplicate() {
    return duplicate;
  }
}
