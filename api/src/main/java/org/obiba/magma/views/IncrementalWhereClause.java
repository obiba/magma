package org.obiba.magma.views;

import org.obiba.magma.NoSuchDatasourceException;
import org.obiba.magma.NoSuchValueTableException;
import org.obiba.magma.Timestamps;
import org.obiba.magma.Value;
import org.obiba.magma.ValueSet;
import org.obiba.magma.ValueTable;
import org.obiba.magma.support.MagmaEngineTableResolver;
import org.obiba.magma.support.NullValueTable;
import org.obiba.magma.type.DateTimeType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.annotations.VisibleForTesting;

/**
 * A "where" clause that can be used to create an incremental {@link View}.
 */
public class IncrementalWhereClause implements WhereClause {
  //
  // Constants
  //

  private static final Logger log = LoggerFactory.getLogger(IncrementalWhereClause.class);

  //
  // Instance Variables
  //

  private String sourceTableName;

  private String destinationTableName;

  /**
   * Cached source table.
   */
  private ValueTable sourceTable;

  /**
   * Cached destination table.
   */
  private ValueTable destinationTable;

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
   * Creates an <code>IncrementalWhereClause</code>, based on the specified source and destination tables.
   * 
   * @param sourceTableName fully-qualified name of the source {@link ValueTable}
   * @param destinationTableName fully-qualified name of the destination {@link ValueTable}
   */
  public IncrementalWhereClause(String sourceTableName, String destinationTableName) {
    if(sourceTableName == null) throw new IllegalArgumentException("null sourceTableName");
    if(destinationTableName == null) throw new IllegalArgumentException("null destinationTableName");

    this.sourceTableName = sourceTableName;
    this.destinationTableName = destinationTableName;
  }

  //
  // WhereClause Methods
  //

  public boolean where(ValueSet valueSet) {
    boolean include = false;

    sourceTable = getSourceTable();
    destinationTable = getDestinationTable();
    ValueSet destinationValueSet = getDestinationValueSet(valueSet);

    if(destinationValueSet != null) {
      Timestamps sourceTimestamps = sourceTable.getTimestamps(valueSet);
      Timestamps destinationTimestamps = destinationTable.getTimestamps(destinationValueSet);
      include = laterThan(sourceTimestamps, destinationTimestamps);
    } else {
      log.debug("No value set found in destination table for entity {}", valueSet.getVariableEntity());
      include = true;
    }

    log.debug("Include entity {} = {}", valueSet.getVariableEntity(), include);

    return include;
  }

  //
  // Methods
  //

  /**
   * Looks up the source table by its name and returns it. The table is cached for performance.
   * 
   * @return the source table
   * @throws NoSuchValueTableException if the source table does not exist
   */
  @VisibleForTesting
  ValueTable getSourceTable() throws NoSuchValueTableException {
    if(sourceTable == null) {
      sourceTable = MagmaEngineTableResolver.valueOf(sourceTableName).resolveTable();
    }
    return sourceTable;
  }

  /**
   * Looks up the destination table by its name and returns it. The table is cached for performance.
   * 
   * Note that when data is copied from one datasource to another, the destination table may not exist; the destination
   * datasource may not exist either (e.g., file-based datasource). In these cases, this method returns a
   * {@link NullValueTable}.
   * 
   * @return the destination table (or a null value table if it does not exist)
   */
  @VisibleForTesting
  ValueTable getDestinationTable() {
    if(destinationTable == null) {
      try {
        destinationTable = MagmaEngineTableResolver.valueOf(destinationTableName).resolveTable();
      } catch(NoSuchDatasourceException ex) {
        destinationTable = NullValueTable.get();
      } catch(NoSuchValueTableException ex) {
        destinationTable = NullValueTable.get();
      }
    }
    return destinationTable;
  }

  @VisibleForTesting
  ValueSet getDestinationValueSet(ValueSet valueSet) {
    ValueSet destinationValueSet = null;
    if(destinationTable.hasValueSet(valueSet.getVariableEntity())) {
      destinationValueSet = destinationTable.getValueSet(valueSet.getVariableEntity());
    }
    return destinationValueSet;
  }

  /**
   * Indicates whether the first timestamps are "later than" the second.
   * 
   * Note that if either <code>Timestamps</code> object is <code>null</code>, or if either one contains a
   * <code>null value</code> "updated" timestamp, this method returns <code>true</code>.
   * 
   * @return <code>true</code> if <code>ts1</code> is later than <code>ts2</code> (based on the "updated" timestamp)
   */
  @VisibleForTesting
  boolean laterThan(Timestamps ts1, Timestamps ts2) {
    Value u1 = ts1 != null ? ts1.getLastUpdate() : DateTimeType.get().nullValue();
    Value u2 = ts2 != null ? ts2.getLastUpdate() : DateTimeType.get().nullValue();
    log.debug("source.updated {} destination.updated {}", u1, u2);
    return (u1.isNull() || u2.isNull() || u1.compareTo(u2) > 0);
  }
}
