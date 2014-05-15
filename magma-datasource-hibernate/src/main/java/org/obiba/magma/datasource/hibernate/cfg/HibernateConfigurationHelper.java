package org.obiba.magma.datasource.hibernate.cfg;

import java.util.Set;

import org.hibernate.cfg.Configuration;
import org.obiba.magma.datasource.hibernate.domain.AttributeState;
import org.obiba.magma.datasource.hibernate.domain.CategoryState;
import org.obiba.magma.datasource.hibernate.domain.DatasourceState;
import org.obiba.magma.datasource.hibernate.domain.ValueSetBinaryValue;
import org.obiba.magma.datasource.hibernate.domain.ValueSetState;
import org.obiba.magma.datasource.hibernate.domain.ValueSetValue;
import org.obiba.magma.datasource.hibernate.domain.ValueTableState;
import org.obiba.magma.datasource.hibernate.domain.VariableEntityState;
import org.obiba.magma.datasource.hibernate.domain.VariableState;

import com.google.common.collect.ImmutableSet;

@SuppressWarnings("StaticMethodOnlyUsedInOneClass")
public class HibernateConfigurationHelper {

  private final static Set<Class<?>> ANNOTATED_TYPES = new ImmutableSet.Builder<Class<?>>()
      .add(DatasourceState.class, VariableEntityState.class, ValueTableState.class, ValueSetState.class,
          ValueSetValue.class, ValueSetBinaryValue.class, VariableState.class, CategoryState.class,
          AttributeState.class).build();

  public Configuration configure(Configuration configuration) {
    for(Class<?> type : getAnnotatedTypes()) {
      configuration.addAnnotatedClass(type);
    }
    return configuration;
  }

  public static Set<Class<?>> getAnnotatedTypes() {
    return ANNOTATED_TYPES;
  }

  public static Class<?>[] getAnnotatedTypesAsArray() {
    return ANNOTATED_TYPES.toArray(new Class<?>[ANNOTATED_TYPES.size()]);
  }
}
