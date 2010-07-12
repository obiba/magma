package org.obiba.magma.views;

import java.util.List;

import org.obiba.magma.Datasource;
import org.obiba.magma.Value;
import org.obiba.magma.ValueSet;
import org.obiba.magma.ValueTable;
import org.obiba.magma.audit.VariableEntityAuditEvent;
import org.obiba.magma.audit.VariableEntityAuditLog;
import org.obiba.magma.audit.VariableEntityAuditLogManager;

/**
 * A "where" clause that can be used to create an incremental {@link View}.
 * 
 * Given a destination {@link Datasource}, {@link ValueSet}s from a source {@link Datasource} are excluded if they have
 * been previously copied to the destination and they have not since been updated in the source. Whether this condition
 * holds or not is determined on the basis of the audit log.
 * 
 * @author cag-dspathis
 * 
 */
public class IncrementalWhereClause implements WhereClause {
  // 
  // Constants
  //

  private static final String COPY_AUDIT_EVENT = "COPY";

  private static final String COPY_DESTINATION = "destinationName";

  //
  // Instance Variables
  //

  private VariableEntityAuditLogManager auditLogManager;

  private String destinationTable;

  //
  // Constructors
  //

  /**
   * No-arg constructor (mainly for XStream).
   */
  public IncrementalWhereClause() {
    super();
  }

  /**
   * Creates an <code>IncrementalWhereClause</code>, based on the specified destination table.
   * 
   * @param destinationTable fully-qualified name of the destination {@link ValueTable}
   */
  public IncrementalWhereClause(String destinationTable) {
    if(destinationTable == null) throw new IllegalArgumentException("null destinationTable");

    this.destinationTable = destinationTable;
  }

  //
  // WhereClause Methods
  //

  @Override
  public boolean where(ValueSet valueSet) {
    VariableEntityAuditLog auditLog = auditLogManager.getAuditLog(valueSet.getVariableEntity());

    VariableEntityAuditEvent lastCopyFromSourceToDestination = getLastCopyFromSourceToDestination(valueSet, auditLog);
    if(lastCopyFromSourceToDestination == null) {
      return true;
    }

    VariableEntityAuditEvent lastUpdateInSource = getLastUpdateInSource(valueSet, auditLog);
    if(lastUpdateInSource == null) {
      // Don't know if this ValueSet is an update or not (nothing in the audit log). Include it just in case.
      return true;
    }

    return lastUpdateInSource.getDatetime().after(lastCopyFromSourceToDestination.getDatetime());
  }

  //
  // Methods
  //

  public void setAuditLogManager(VariableEntityAuditLogManager auditLogManager) {
    this.auditLogManager = auditLogManager;
  }

  public void setDestinationTable(String destinationTable) {
    this.destinationTable = destinationTable;
  }

  private VariableEntityAuditEvent getLastCopyFromSourceToDestination(ValueSet valueSet, VariableEntityAuditLog auditLog) {
    List<VariableEntityAuditEvent> eventsMostRecentFirst = auditLog.getAuditEvents(valueSet.getValueTable());
    for(VariableEntityAuditEvent event : eventsMostRecentFirst) {
      if(event.getType().equals(COPY_AUDIT_EVENT)) {
        Value destination = event.getDetailValue(COPY_DESTINATION);
        if(destination != null && destination.getValue().equals(destinationTable)) {
          return event;
        }
      }
    }

    return null;
  }

  private VariableEntityAuditEvent getLastUpdateInSource(ValueSet valueSet, VariableEntityAuditLog auditLog) {
    List<VariableEntityAuditEvent> eventsMostRecentFirst = auditLog.getAuditEvents(COPY_AUDIT_EVENT);
    for(VariableEntityAuditEvent event : eventsMostRecentFirst) {
      if(event.getDetailValue(COPY_DESTINATION).getValue().equals(valueSet.getValueTable().getDatasource().getName() + "." + valueSet.getValueTable().getName())) {
        return event;
      }
    }

    return null;
  }

}
