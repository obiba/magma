package org.obiba.magma.support;

import java.util.StringTokenizer;

import org.obiba.magma.Datasource;
import org.obiba.magma.MagmaEngine;
import org.obiba.magma.ValueTable;

/**
 * Extends {@link AbstractValueTableWrapper} and uses {@link MagmaEngine} to lookup the referenced {@link ValueTable}
 * instance.
 */
public class ValueTableReference extends AbstractValueTableWrapper {
  //
  // Instance Variables
  //

  /**
   * Fully-qualified {@link ValueTable} reference.
   */
  private String reference;

  /**
   * Cached {@link Datasource} name (extracted from reference).
   */
  private String datasourceName;

  /**
   * Cached {@link ValueTable} name (extracted from reference).
   */
  private String valueTableName;

  //
  // Constructors
  //

  /**
   * {@link ValueTableReference} constructor.
   * 
   * Note: The constructor creates an instance so long as the specified reference is well-formed (even if no such table
   * 
   * @param reference fully-qualified value table reference
   * @throws IllegalArgumentException if the reference is not well-formed
   */
  public ValueTableReference(String reference) {
    this.reference = reference;

    String[] referenceElements = extractReferenceElements();
    datasourceName = referenceElements[0];
    valueTableName = referenceElements[1];
  }

  //
  // AbstractValueTableWrapper Methods
  //

  @Override
  public ValueTable getWrappedValueTable() {
    return MagmaEngine.get().getDatasource(datasourceName).getValueTable(valueTableName);
  }

  //
  // Methods
  //

  private String[] extractReferenceElements() {
    if(reference != null) {
      StringTokenizer tokenizer = new StringTokenizer(reference, " .");
      if(tokenizer.countTokens() == 2) {
        String[] tokens = new String[2];
        tokens[0] = tokenizer.nextToken();
        tokens[1] = tokenizer.nextToken();
        return tokens;
      }
    }
    throw new IllegalArgumentException("Invalid ValueTable reference: " + reference);
  }
}
