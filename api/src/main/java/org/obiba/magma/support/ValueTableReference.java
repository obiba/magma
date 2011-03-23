package org.obiba.magma.support;

import org.obiba.magma.MagmaEngine;
import org.obiba.magma.ValueTable;

/**
 * Extends {@link AbstractValueTableWrapper} and uses {@link MagmaEngine} to lookup the referenced {@link ValueTable}
 * instance.
 */
public class ValueTableReference extends AbstractValueTableWrapper {

  /**
   * Fully-qualified {@link ValueTable} reference.
   */
  private String reference;

  /**
   * Cached {@link MagmaEngineTableResolver}
   */
  private transient MagmaEngineTableResolver tableResolver;

  /**
   * No-arg constructor for XStream.
   */
  public ValueTableReference() {

  }

  /**
   * {@link ValueTableReference} constructor.
   * 
   * Note: The constructor creates an instance so long as the specified reference is well-formed (even if no such table
   * 
   * @param reference fully-qualified value table reference
   * @throws IllegalArgumentException if the reference is not well-formed
   */
  public ValueTableReference(String reference) {
    if(reference == null) throw new IllegalArgumentException("reference cannot be null");
    this.reference = reference;
    getResolver();
  }

  @Override
  public ValueTable getWrappedValueTable() {
    return getResolver().resolveTable();
  }

  public String getReference() {
    return reference;
  }

  protected MagmaEngineTableResolver getResolver() {
    if(tableResolver == null) {
      tableResolver = MagmaEngineTableResolver.valueOf(reference);
    }
    return tableResolver;
  }

}
