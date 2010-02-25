package org.obiba.magma.audit;

import java.util.Date;
import java.util.Map;

import org.obiba.magma.Value;

/**
 * Interface for an entry in the audit log. It represents an event that has been audited.
 */
public interface VariableEntityAuditEvent {

  /**
   * Gets the unique identifier of the user that caused the event to happen. Usually the username.
   * 
   * @return User identifier
   */
  public String getUser();

  /**
   * Gets the application-specific nature of the event. For example, an application may define "CREATE" and "DELETE"
   * types.
   * 
   * @return Type of event
   */
  public String getType();

  /**
   * Gets the datasource where the event stems from.
   * 
   * @return Datasource of event
   */
  public String getDatasource();

  /**
   * Gets the ValueTable where the event stems from.
   * 
   * @return ValueTable of event
   */
  public String getValueTable();

  /**
   * Gets the date and time the event occurred.
   * 
   * @return Time of event
   */
  public Date getDatetime();

  /**
   * Gets a list of event-specific values that provide additional context.
   * 
   * @return Event details
   */
  public Map<String, Value> getDetails();

  /**
   * Gets the value for a specific event detail.
   * 
   * @param name Name of the specific detail for which the value will be retrieved.
   * @return Value
   */
  public Value getDetailValue(String name);
}
