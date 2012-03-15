package org.obiba.magma.datasource.limesurvey;

import org.obiba.magma.Value;
import org.obiba.magma.ValueTable;
import org.obiba.magma.Variable;
import org.obiba.magma.VariableEntity;
import org.obiba.magma.support.ValueSetBean;
import org.springframework.jdbc.core.JdbcTemplate;

public class LimesurveyValueSet extends ValueSetBean {

  public LimesurveyValueSet(ValueTable table, VariableEntity entity) {
    super(table, entity);
  }

  public Value getValue(Variable variable) {
    JdbcTemplate jdbcTemplate = getValueTable().getDatasource().getJdbcTemplate();
    String id = getVariableEntity().getIdentifier();

    return null;
  }

  @Override
  public LimesurveyValueTable getValueTable() {
    return (LimesurveyValueTable) super.getValueTable();
  }

}
