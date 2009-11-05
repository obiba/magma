package org.obiba.meta.beans;

import java.util.Set;

import org.obiba.meta.Occurrence;
import org.obiba.meta.ValueSet;
import org.obiba.meta.Variable;
import org.springframework.util.Assert;

public class DefaultBeanValueSetConnection implements BeanValueSetConnection {

  private ValueSet valueSet;

  private BeansDatasource datasource;

  public DefaultBeanValueSetConnection(ValueSet valueSet, BeansDatasource datasource) {
    Assert.notNull(valueSet, "valueSet cannot be null");
    Assert.notNull(datasource, "datasource cannot be null");
    this.valueSet = valueSet;
    this.datasource = datasource;
  }

  public <B> B findBean(Class<B> type, Variable variable) {
    return datasource.resolveBean(this, type, variable);
  }

  @Override
  public ValueSet getValueSet() {
    return valueSet;
  }

  @Override
  public Set<Occurrence> loadOccurrences(Variable variable) {
    return datasource.loadOccurrences(this, variable);
  }

}
