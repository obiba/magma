/*
 * Copyright (c) 2017 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

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
  String getUser();

  /**
   * Gets the application-specific nature of the event. For example, an application may define "CREATE" and "DELETE"
   * types.
   *
   * @return Type of event
   */
  String getType();

  /**
   * Gets the datasource where the event stems from.
   *
   * @return Datasource of event
   */
  String getDatasource();

  /**
   * Gets the ValueTable where the event stems from.
   *
   * @return ValueTable of event
   */
  String getValueTable();

  /**
   * Gets the date and time the event occurred.
   *
   * @return Time of event
   */
  Date getDatetime();

  /**
   * Gets a list of event-specific values that provide additional context.
   *
   * @return Event details
   */
  Map<String, Value> getDetails();

  /**
   * Gets the value for a specific event detail.
   *
   * @param name Name of the specific detail for which the value will be retrieved.
   * @return Value
   */
  Value getDetailValue(String name);
}
