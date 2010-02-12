package org.obiba.magma.datasource.hibernate.support;

import java.util.Set;

import org.hibernate.cfg.AnnotationConfiguration;
import org.obiba.core.domain.AbstractEntity;
import org.obiba.magma.datasource.hibernate.domain.CategoryState;
import org.obiba.magma.datasource.hibernate.domain.DatasourceState;
import org.obiba.magma.datasource.hibernate.domain.ValueSetState;
import org.obiba.magma.datasource.hibernate.domain.ValueSetValue;
import org.obiba.magma.datasource.hibernate.domain.ValueTableState;
import org.obiba.magma.datasource.hibernate.domain.VariableEntityState;
import org.obiba.magma.datasource.hibernate.domain.VariableState;
import org.obiba.magma.datasource.hibernate.domain.attribute.AttributeAwareAdapter;
import org.obiba.magma.datasource.hibernate.domain.attribute.HibernateAttribute;

import com.google.common.collect.ImmutableSet;

public class AnnotationConfigurationHelper {

  public AnnotationConfiguration configure(AnnotationConfiguration configuration) {
    for(Class<?> type : getAnnotatedTypes()) {
      configuration.addAnnotatedClass(type);
    }
    return configuration;
  }

  @SuppressWarnings("unchecked")
  public static Set<Class<? extends AbstractEntity>> getAnnotatedTypes() {
    return ImmutableSet.of(DatasourceState.class, VariableEntityState.class, ValueTableState.class, ValueSetState.class, ValueSetValue.class, VariableState.class, CategoryState.class, HibernateAttribute.class, AttributeAwareAdapter.class);
  }
}
