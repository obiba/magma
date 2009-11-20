package org.obiba.magma.support;

import org.obiba.magma.Collection;

/**
 * To be implemented by {@code Collection} instances that wrap other {@code Collection} instances. Instead of
 * implementing this directly, consider extending the {@link AbstractCollectionWrapper} which provides default
 * implementation for all methods.
 */
public interface CollectionWrapper extends Collection {

  public Collection getWrappedCollection();

}
