package org.obiba.magma;

public class NoSuchCollectionException extends MagmaRuntimeException {

  private static final long serialVersionUID = 5887330656285998606L;

  public NoSuchCollectionException(String collectionName) {
    super("No collection exists with the specified name '" + collectionName + "'");
  }

}
