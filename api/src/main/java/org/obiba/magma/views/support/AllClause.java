package org.obiba.magma.views.support;

import org.obiba.magma.ValueSet;
import org.obiba.magma.Variable;
import org.obiba.magma.views.SelectClause;
import org.obiba.magma.views.WhereClause;

public final class AllClause implements SelectClause, WhereClause {
  //
  // SelectClause Methods
  //

  @Override
  public boolean select(Variable variable) {
    return true;
  }

  //
  // WhereClause Methods
  //

  @Override
  public boolean where(ValueSet valueSet) {
    // TODO Auto-generated method stub
    return true;
  }

}
