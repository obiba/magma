package org.obiba.magma.audit;

import org.obiba.magma.VariableEntity;

/**
 * Interface for interacting with audit logs. It provides the ability to obtain an instance of VariableEntityAuditLog
 * for a specific VariableEntity.
 */
public interface VariableEntityAuditLogManager {

  /**
   * Obtain an instance of VariableEntityAuditLog for a specific VariableEntity.
   * 
   * @param entity Entity from which to obtain the VariableEntityAuditLog.
   * @return A VariableEntityAuditLog
   */
  public VariableEntityAuditLog getAuditLog(VariableEntity entity);

}
