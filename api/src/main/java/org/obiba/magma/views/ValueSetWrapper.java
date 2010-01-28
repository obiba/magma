/**
 * 
 */
package org.obiba.magma.views;

import org.obiba.magma.ValueSet;
import org.obiba.magma.ValueTable;
import org.obiba.magma.VariableEntity;

class ValueSetWrapper implements ValueSet {
  private View view;

  private ValueSet wrapped;

  ValueSetWrapper(View view, ValueSet wrapped) {
    this.view = view;
    this.wrapped = wrapped;
  }

  @Override
  public ValueTable getValueTable() {
    return view;
  }

  @Override
  public VariableEntity getVariableEntity() {
    return view.getVariableEntityTransformer().apply(wrapped.getVariableEntity());
  }

  public ValueSet getWrappedValueSet() {
    return wrapped;
  }
}