package org.obiba.magma.audit;

import java.util.List;

import org.obiba.magma.Datasource;
import org.obiba.magma.ValueTable;
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
   * Gets the list of events from the log for a specific datasource, most recent first.
   * 
   * @param datasource
   * @return
   */
  public List<VariableEntityAuditEvent> getAuditEvents(Datasource datasource);

  /**
   * Gets the list of events from the log for a specific value table, most recent first.
   * 
   * @param value table
   * @return
   */
  public List<VariableEntityAuditEvent> getAuditEvents(ValueTable valueTable);

  /**
   * Gets a list of events from the log for a specific type, most recent first.
   * 
   * @param type
   * @return
   */
  public List<VariableEntityAuditEvent> getAuditEvents(String type);

  /**
   * Gets the complete list of events from the log, most recent first.
   * 
   * @return
   */
  public List<VariableEntityAuditEvent> getAuditEvents();
}
