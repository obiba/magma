package org.obiba.magma.audit.hibernate.support;

import java.util.Set;

import org.hibernate.cfg.AnnotationConfiguration;
import org.obiba.core.domain.AbstractEntity;
import org.obiba.magma.audit.hibernate.domain.HibernateVariableEntityAuditEvent;
import org.obiba.magma.audit.hibernate.domain.HibernateVariableEntityAuditLog;

import com.google.common.collect.ImmutableSet;

public class AnnotationConfigurationHelper {

  private final Set<Class<? extends AbstractEntity>> annotatedTypes
      = new ImmutableSet.Builder<Class<? extends AbstractEntity>>()
      .add(HibernateVariableEntityAuditEvent.class, HibernateVariableEntityAuditLog.class).build();

  public AnnotationConfiguration configure(AnnotationConfiguration configuration) {
    for(Class<?> type : getAnnotatedTypes()) {
      configuration.addAnnotatedClass(type);
    }
    return configuration;
  }

  public Set<Class<? extends AbstractEntity>> getAnnotatedTypes() {
    return annotatedTypes;
  }
}
