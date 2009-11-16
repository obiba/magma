package org.obiba.magma;

public class NoSuchVariableException extends MetaRuntimeException {

  private static final long serialVersionUID = 5887330656285998606L;

  private String collection;

  private String name;

  public NoSuchVariableException(String collection, String name) {
    super("No such variable '" + name + "' in collection '" + collection + "'");
    this.collection = collection;
    this.name = name;
  }

  public NoSuchVariableException(String name) {
    super("No such variable '" + name + "'");
    this.name = name;
  }

  public String getCollection() {
    return collection;
  }

  public String getName() {
    return name;
  }
}
