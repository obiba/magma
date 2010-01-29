package org.obiba.magma.audit.hibernate;

import java.util.Date;
import java.util.Map;

import org.hibernate.SessionFactory;
import org.obiba.core.service.impl.hibernate.AssociationCriteria;
import org.obiba.core.service.impl.hibernate.AssociationCriteria.Operation;
import org.obiba.magma.Datasource;
import org.obiba.magma.Value;
import org.obiba.magma.VariableEntity;
import org.obiba.magma.audit.UserProvider;
import org.obiba.magma.audit.VariableEntityAuditEvent;
import org.obiba.magma.audit.VariableEntityAuditLog;
import org.obiba.magma.audit.VariableEntityAuditLogManager;
import org.obiba.magma.audit.hibernate.domain.HibernateVariableEntityAuditEvent;
import org.obiba.magma.audit.hibernate.domain.HibernateVariableEntityAuditLog;
import org.springframework.beans.factory.annotation.Autowired;

public class HibernateVariableEntityAuditLogManager implements VariableEntityAuditLogManager {

  @Autowired
  private SessionFactory sessionFactory;

  @Autowired
  private UserProvider userProvider;

  @Override
  public VariableEntityAuditLog getAuditLog(VariableEntity entity) {
    AssociationCriteria criteria = AssociationCriteria.create(HibernateVariableEntityAuditLog.class, sessionFactory.getCurrentSession()).add("variableEntityType", Operation.eq, entity.getType()).add("variableEntityIdentifier", Operation.eq, entity.getIdentifier());
    HibernateVariableEntityAuditLog log = (HibernateVariableEntityAuditLog) criteria.getCriteria().uniqueResult();
    if(log == null) {
      log = new HibernateVariableEntityAuditLog(entity);
    }
    return log;
  }

  @Override
  public VariableEntityAuditEvent createAuditEvent(VariableEntityAuditLog log, Datasource datasource, String type, Map<String, Value> details) {
    HibernateVariableEntityAuditEvent auditEvent = new HibernateVariableEntityAuditEvent();
    auditEvent.setDatasource(datasource.getName());
    auditEvent.setType(type);
    auditEvent.setDetails(details);
    auditEvent.setDatetime(new Date());
    auditEvent.setUser(userProvider.getUsername());
    log.getAuditEvents().add(auditEvent);
    return auditEvent;
  }
}
