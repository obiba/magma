package org.obiba.magma.audit;

import java.util.List;
import java.util.Map;

import org.obiba.magma.Datasource;
import org.obiba.magma.Value;
import org.obiba.magma.VariableEntity;

/**
 * Interface for a VariableEntity's audit log. The audit log is an aggregation of all audit events of a particular
 * VariableEntity.
 */
public interface VariableEntityAuditLog {

  /**
   * Allows creating new entries (events) within the log.
   * 
   * @param datasource The datasource where the event stems from.
   * @param type The application-specific nature of the event. For example, an application may define "CREATE" and
   * "DELETE" types. Although Magma may define some types, this API does not define any type.
   * @param details A list of event-specific values that provide additional context.
   * 
   * @return The event created
   */
  public VariableEntityAuditEvent createAuditEvent(Datasource datasource, String type, Map<String, Value> details);

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
