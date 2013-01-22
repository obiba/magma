package org.obiba.magma.support;

import org.obiba.magma.ValueTable;

public interface ValueTableWrapper extends ValueTable {

  ValueTable getWrappedValueTable();

}
