package org.obiba.magma.audit.hibernate.domain;

import java.util.Date;
import java.util.Map;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;
import org.hibernate.annotations.CollectionOfElements;
import org.hibernate.annotations.Columns;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;
import org.obiba.core.domain.AbstractEntity;
import org.obiba.magma.Value;
import org.obiba.magma.audit.VariableEntityAuditEvent;
import org.obiba.magma.datasource.hibernate.domain.ValueHibernateType;

@Entity
@Table(name = "variable_entity_audit_event")
@TypeDef(name = "value", typeClass = ValueHibernateType.class)
public class HibernateVariableEntityAuditEvent extends AbstractEntity implements VariableEntityAuditEvent {

  private static final long serialVersionUID = 1L;

  @Column(nullable = false)
  private String datasource;

  @Column(nullable = false)
  @Temporal(TemporalType.TIMESTAMP)
  private Date datetime;

  @CollectionOfElements(targetElement = Value.class)
  @Type(type = "value")
  @Columns(columns = { @Column(name = "value_type", nullable = false), @Column(name = "is_sequence", nullable = false), @Column(name = "value", length = Integer.MAX_VALUE, nullable = false) })
  @Cascade(CascadeType.ALL)
  private Map<String, Value> details;

  @Column(nullable = false)
  private String type;

  @Column(nullable = false)
  private String user;

  @Override
  public String getDatasource() {
    return datasource;
  }

  @Override
  public Date getDatetime() {
    return datetime;
  }

  @Override
  public Value getDetailValue(String name) {
    return details.get(name);
  }

  @Override
  public Map<String, Value> getDetails() {
    return details;
  }

  @Override
  public String getType() {
    return type;
  }

  @Override
  public String getUser() {
    return user;
  }

  public void setDatasource(String datasource) {
    this.datasource = datasource;
  }

  public void setDatetime(Date datetime) {
    this.datetime = datetime;
  }

  public void setDetails(Map<String, Value> details) {
    this.details = details;
  }

  public void setType(String type) {
    this.type = type;
  }

  public void setUser(String user) {
    this.user = user;
  }

}
