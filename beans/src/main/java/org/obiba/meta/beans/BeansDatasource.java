package org.obiba.meta.beans;

import java.util.Set;

import org.obiba.meta.Datasource;
import org.obiba.meta.Occurrence;
import org.obiba.meta.ValueSet;
import org.obiba.meta.Variable;

public interface BeansDatasource extends Datasource {

  public BeanValueSetConnection createConnection(ValueSet valueSet);

  public Set<Occurrence> loadOccurrences(BeanValueSetConnection connection, Variable variable);

  public <B> B resolveBean(BeanValueSetConnection connection, Class<B> type, Variable variable);

}
