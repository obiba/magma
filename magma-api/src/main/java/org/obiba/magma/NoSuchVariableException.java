package org.obiba.magma;

public class NoSuchVariableException extends MagmaRuntimeException {

  private static final long serialVersionUID = 5887330656285998606L;

  private String valueTableName;

  private final String name;

  public NoSuchVariableException(String valueTableName, String name) {
    super("No such variable '" + name + "' in table '" + valueTableName + "'");
    this.valueTableName = valueTableName;
    this.name = name;
  }

  public NoSuchVariableException(String name) {
    super("No such variable '" + name + "'");
    this.name = name;
  }

  public String getValueTableName() {
    return valueTableName;
  }

  public String getName() {
    return name;
  }
}
