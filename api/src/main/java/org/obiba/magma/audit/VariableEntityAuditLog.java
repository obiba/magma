package org.obiba.magma.audit;

import java.util.List;

import org.obiba.magma.Datasource;
import org.obiba.magma.VariableEntity;

/**
 * Interface for a VariableEntity's audit log. The audit log is an aggregation of all audit events of a particular
 * VariableEntity.
 */
public interface VariableEntityAuditLog {

  /**
   * Gets the VariableEntity to which this audit belongs to.
   * 
   * @return
   */
  public VariableEntity getVariableEntity();

  /**
   * Gets the list of events from the log for a specific datasource.
   * 
   * @param datasource
   * @return
   */
  public List<VariableEntityAuditEvent> getAuditEvents(Datasource datasource);

  /**
   * Gets a list of events from the log for a specific type.
   * 
   * @param type
   * @return
   */
  public List<VariableEntityAuditEvent> getAuditEvents(String type);

  /**
   * Gets the complete list of events from the log.
   * 
   * @return
   */
  public List<VariableEntityAuditEvent> getAuditEvents();
}
