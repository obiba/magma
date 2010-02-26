package org.obiba.magma.audit.hibernate.domain;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import org.obiba.core.domain.AbstractEntity;
import org.obiba.magma.Datasource;
import org.obiba.magma.ValueTable;
import org.obiba.magma.VariableEntity;
import org.obiba.magma.audit.VariableEntityAuditEvent;
import org.obiba.magma.audit.VariableEntityAuditLog;
import org.obiba.magma.support.VariableEntityBean;

@Entity
@Table(name = "variable_entity_audit_log", uniqueConstraints = { @UniqueConstraint(columnNames = { "variableEntityType", "variableEntityIdentifier" }) })
public class HibernateVariableEntityAuditLog extends AbstractEntity implements VariableEntityAuditLog {

  private static final long serialVersionUID = 1L;

  @OneToMany(cascade = { CascadeType.ALL })
  @JoinColumn(name = "variable_entity_audit_log_id", nullable = false)
  @OrderBy("datetime DESC")
  private List<HibernateVariableEntityAuditEvent> auditEvents;

  @Column(nullable = false)
  private String variableEntityType;

  @Column(nullable = false)
  private String variableEntityIdentifier;

  public HibernateVariableEntityAuditLog() {
    super();
  }

  public HibernateVariableEntityAuditLog(VariableEntity variableEntity) {
    super();
    this.variableEntityType = variableEntity.getType();
    this.variableEntityIdentifier = variableEntity.getIdentifier();
    this.auditEvents = new ArrayList<HibernateVariableEntityAuditEvent>();
  }

  @Override
  public List<VariableEntityAuditEvent> getAuditEvents(Datasource datasource) {
    List<VariableEntityAuditEvent> filteredEvents = new ArrayList<VariableEntityAuditEvent>();
    for(VariableEntityAuditEvent auditEvent : auditEvents) {
      if(auditEvent.getDatasource().equals(datasource.getName())) {
        filteredEvents.add(auditEvent);
      }
    }
    return filteredEvents;
  }

  @Override
  public List<VariableEntityAuditEvent> getAuditEvents(ValueTable valueTable) {
    List<VariableEntityAuditEvent> filteredEvents = new ArrayList<VariableEntityAuditEvent>();
    for(VariableEntityAuditEvent auditEvent : auditEvents) {
      if(auditEvent.getDatasource().equals(valueTable.getDatasource().getName()) && auditEvent.getValueTable().equals(valueTable.getName())) {
        filteredEvents.add(auditEvent);
      }
    }
    return filteredEvents;
  }

  @Override
  public List<VariableEntityAuditEvent> getAuditEvents(String type) {
    List<VariableEntityAuditEvent> filteredEvents = new ArrayList<VariableEntityAuditEvent>();
    for(VariableEntityAuditEvent auditEvent : auditEvents) {
      if(auditEvent.getType().equals(type)) {
        filteredEvents.add(auditEvent);
      }
    }
    return filteredEvents;
  }

  @Override
  public List<VariableEntityAuditEvent> getAuditEvents() {
    return new ArrayList<VariableEntityAuditEvent>(auditEvents);
  }

  public void addEvent(HibernateVariableEntityAuditEvent auditEvent) {
    auditEvents.add(auditEvent);
  }

  @Override
  public VariableEntity getVariableEntity() {
    return new VariableEntityBean(variableEntityType, variableEntityIdentifier);
  }

}
