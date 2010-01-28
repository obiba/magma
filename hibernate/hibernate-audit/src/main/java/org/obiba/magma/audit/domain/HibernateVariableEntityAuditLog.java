package org.obiba.magma.audit.domain;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.persistence.UniqueConstraint;

import org.obiba.core.domain.AbstractEntity;
import org.obiba.magma.Datasource;
import org.obiba.magma.VariableEntity;
import org.obiba.magma.audit.VariableEntityAuditEvent;
import org.obiba.magma.audit.VariableEntityAuditLog;

@Entity
@Table(name = "variable_entity_audit_log", uniqueConstraints = { @UniqueConstraint(columnNames = { "variable_entity_type", "variable_entity_identifier" }) })
public class HibernateVariableEntityAuditLog extends AbstractEntity implements VariableEntityAuditLog {

  private static final long serialVersionUID = 1L;

  @OneToMany(cascade = { CascadeType.ALL })
  private List<VariableEntityAuditEvent> auditEvents;

  @Column(nullable = false)
  private String variableEntityType;

  @Column(nullable = false)
  private String variableEntityIdentifier;

  @Transient
  private VariableEntity variableEntity;

  public HibernateVariableEntityAuditLog(VariableEntity variableEntity) {
    super();
    this.variableEntity = variableEntity;
    this.variableEntityType = variableEntity.getType();
    this.variableEntityIdentifier = variableEntity.getIdentifier();
    this.auditEvents = new ArrayList<VariableEntityAuditEvent>();
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
    return auditEvents;
  }

  @Override
  public VariableEntity getVariableEntity() {
    return variableEntity;
  }

}
