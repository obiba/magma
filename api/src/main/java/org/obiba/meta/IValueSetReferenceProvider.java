package org.obiba.meta;

import java.util.Set;

public interface IValueSetReferenceProvider {

  public String getEntityType();

  public Set<IValueSetReference> getValueSetReferences();

}
