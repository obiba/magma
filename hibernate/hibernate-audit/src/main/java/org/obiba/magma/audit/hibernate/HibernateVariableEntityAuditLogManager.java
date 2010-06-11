package org.obiba.magma.audit.hibernate;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

import org.hibernate.FlushMode;
import org.hibernate.SessionFactory;
import org.obiba.core.service.impl.hibernate.AssociationCriteria;
import org.obiba.core.service.impl.hibernate.AssociationCriteria.Operation;
import org.obiba.magma.Datasource;
import org.obiba.magma.Value;
import org.obiba.magma.ValueSet;
import org.obiba.magma.ValueTable;
import org.obiba.magma.VariableEntity;
import org.obiba.magma.audit.UserProvider;
import org.obiba.magma.audit.VariableEntityAuditEvent;
import org.obiba.magma.audit.VariableEntityAuditLog;
import org.obiba.magma.audit.VariableEntityAuditLogManager;
import org.obiba.magma.audit.hibernate.domain.HibernateVariableEntityAuditEvent;
import org.obiba.magma.audit.hibernate.domain.HibernateVariableEntityAuditLog;
import org.obiba.magma.audit.support.CopyAuditor;
import org.obiba.magma.support.DatasourceCopier.Builder;
import org.obiba.magma.type.TextType;

import com.google.common.base.Function;
import com.google.common.collect.Lists;

public class HibernateVariableEntityAuditLogManager implements VariableEntityAuditLogManager {

  private SessionFactory sessionFactory;

  private UserProvider userProvider;

  @Override
  public CopyAuditor createAuditor(Builder builder, Datasource destination, Function<VariableEntity, VariableEntity> entityMapper) {
    HibernateCopyAuditor auditor = new HibernateCopyAuditor(destination, entityMapper);
    builder.withListener(auditor);
    return auditor;
  }

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
  public VariableEntityAuditEvent createAuditEvent(VariableEntityAuditLog log, ValueTable source, String type, Map<String, Value> details) {
    if(log == null) throw new IllegalArgumentException("log cannot be null");
    if(source == null) throw new IllegalArgumentException("source cannot be null");
    if(type == null) throw new IllegalArgumentException("type cannot be null");
    HibernateVariableEntityAuditEvent auditEvent = new HibernateVariableEntityAuditEvent();
    auditEvent.setDatasource(source.getDatasource().getName());
    auditEvent.setValueTable(source.getName());
    auditEvent.setType(type);
    auditEvent.setDetails(details);
    auditEvent.setDatetime(new Date());
    auditEvent.setUser(userProvider.getUsername());
    ((HibernateVariableEntityAuditLog) log).addEvent(auditEvent);
    return auditEvent;
  }

  public void setSessionFactory(SessionFactory sessionFactory) {
    this.sessionFactory = sessionFactory;
  }

  public void setUserProvider(UserProvider userProvider) {
    this.userProvider = userProvider;
  }

  private class HibernateCopyAuditor implements CopyAuditor {

    private final Datasource destination;

    private final Function<VariableEntity, VariableEntity> entityMapper;

    private final List<Callable<?>> audits = Lists.newArrayList();

    public HibernateCopyAuditor(Datasource destination, Function<VariableEntity, VariableEntity> entityMapper) {
      if(destination == null) throw new IllegalArgumentException("destination cannot be null");
      this.destination = destination;
      this.entityMapper = entityMapper;
    }

    @Override
    public void onValueSetCopied(final ValueTable source, ValueSet valueSet, final String... tables) {
      final VariableEntity entity = entityMapper != null ? entityMapper.apply(valueSet.getVariableEntity()) : valueSet.getVariableEntity();
      audits.add(new Callable<Object>() {

        @Override
        public Object call() throws Exception {
          VariableEntityAuditLog log = getAuditLog(entity);
          for(String tableName : tables) {
            createAuditEvent(log, source, "COPY", createCopyDetails(entity, tableName));
          }
          sessionFactory.getCurrentSession().saveOrUpdate(log);
          return null;
        }

      });
    }

    @Override
    public void onValueSetCopy(ValueTable source, ValueSet valueSet) {
    }

    private Map<String, Value> createCopyDetails(VariableEntity entity, String tableName) {
      Map<String, Value> details = new HashMap<String, Value>();
      details.put("destinationName", TextType.get().valueOf(destination.getName() + "." + tableName));
      return details;
    }

    @Override
    public void startAuditing() {
    }

    @Override
    public void completeAuditing() {
      FlushMode mode = sessionFactory.getCurrentSession().getFlushMode();
      if(mode != FlushMode.MANUAL) {
        sessionFactory.getCurrentSession().setFlushMode(FlushMode.MANUAL);
      }
      // Do the actual auditing
      for(Callable<?> c : audits) {
        try {
          c.call();
        } catch(Exception e) {
          throw new RuntimeException(e);
        }
      }
      audits.clear();
      sessionFactory.getCurrentSession().flush();

      if(mode != FlushMode.MANUAL) {
        sessionFactory.getCurrentSession().setFlushMode(mode);
      }
    }

  }
}
