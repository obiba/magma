package org.obiba.meta;

public interface Occurrence extends ValueSet {

  public ValueSet getParent();

  public String getGroup();

  public int getOrder();

}
