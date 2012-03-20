package org.obiba.magma.datasource.limesurvey;

import org.obiba.magma.Value;
import org.obiba.magma.ValueTable;
import org.obiba.magma.VariableEntity;
import org.obiba.magma.datasource.limesurvey.LimesurveyValueTable.LimesurveyVariableValueSource;
import org.obiba.magma.support.ValueSetBean;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;

public class LimesurveyValueSet extends ValueSetBean {

  public LimesurveyValueSet(ValueTable table, VariableEntity entity) {
    super(table, entity);
  }

  public Value getValue(LimesurveyVariableValueSource limesurveyVariableValueSource) {
    JdbcTemplate jdbcTemplate = getValueTable().getDatasource().getJdbcTemplate();
    String id = getVariableEntity().getIdentifier();
    StringBuilder sql = new StringBuilder();
    LimesurveyValueTable limeValueTable = (LimesurveyValueTable) getValueTable();
    sql.append("SELECT * FROM survey_" + limeValueTable.getSid() + " ");
    sql.append("WHERE token=?");
    SqlRowSet queryForRowSet = jdbcTemplate.queryForRowSet(sql.toString(), new Object[] { id });
    queryForRowSet.next();
    System.out.println(queryForRowSet.getString(limesurveyVariableValueSource.getLimesurveyField()));
    return null;
  }

  @Override
  public LimesurveyValueTable getValueTable() {
    return (LimesurveyValueTable) super.getValueTable();
  }

}
