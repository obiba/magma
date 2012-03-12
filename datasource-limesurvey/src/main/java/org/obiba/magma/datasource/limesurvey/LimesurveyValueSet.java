package org.obiba.magma.datasource.limesurvey;

import org.obiba.magma.ValueSet;
import org.obiba.magma.ValueTable;
import org.obiba.magma.VariableEntity;
import org.obiba.magma.support.ValueSetBean;

public class LimesurveyValueSet extends ValueSetBean {

  public LimesurveyValueSet(ValueTable table, VariableEntity entity) {
    super(table, entity);
  }

  protected LimesurveyValueSet(ValueSet valueSet) {
    super(valueSet);
  }

}
