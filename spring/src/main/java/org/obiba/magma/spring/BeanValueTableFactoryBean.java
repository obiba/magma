package org.obiba.magma.spring;

import org.obiba.magma.Datasource;
import org.obiba.magma.ValueTable;
import org.obiba.magma.VariableValueSourceFactory;
import org.obiba.magma.beans.BeanValueTable;
import org.obiba.magma.beans.ValueSetBeanResolver;
import org.obiba.magma.support.VariableEntityProvider;

public class BeanValueTableFactoryBean implements ValueTableFactoryBean {

  private String valueTableName;

  private VariableEntityProvider variableEntityProvider;

  private ValueSetBeanResolver resolver;

  private VariableValueSourceFactory factory;

  public void setValueTableName(String name) {
    this.valueTableName = name;
  }

  @Override
  public String getValueTableName() {
    return valueTableName;
  }

  public void setVariableEntityProvider(VariableEntityProvider variableEntityProvider) {
    this.variableEntityProvider = variableEntityProvider;
  }

  public void setVariableValueSourceFactory(VariableValueSourceFactory factory) {
    this.factory = factory;
  }

  public void setValueSetBeanResolver(ValueSetBeanResolver resolver) {
    this.resolver = resolver;
  }

  @Override
  public ValueTable buildValueTable(Datasource datasource) {
    if(resolver == null) {
      throw new IllegalStateException("valueSetBeanResolver property cannot be null");
    }
    if(factory == null) {
      throw new IllegalStateException("variableValueSourceFactory property cannot be null");
    }
    BeanValueTable bvt = new BeanValueTable(datasource, getValueTableName(), variableEntityProvider);
    bvt.addResolver(resolver);
    bvt.addVariableValueSources(factory);
    return bvt;
  }

}
