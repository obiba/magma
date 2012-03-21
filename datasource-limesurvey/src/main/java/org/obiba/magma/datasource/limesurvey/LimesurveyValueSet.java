package org.obiba.magma.datasource.limesurvey;

import java.util.List;
import java.util.Map;

import org.obiba.magma.Value;
import org.obiba.magma.ValueTable;
import org.obiba.magma.ValueType;
import org.obiba.magma.VariableEntity;
import org.obiba.magma.datasource.limesurvey.LimesurveyValueTable.LimesurveyVariableValueSource;
import org.obiba.magma.support.ValueSetBean;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

public class LimesurveyValueSet extends ValueSetBean {

  private Map<String, Object> cache;

  public LimesurveyValueSet(ValueTable table, VariableEntity entity) {
    super(table, entity);
  }

  public Value getValue(LimesurveyVariableValueSource limesurveyVariableValueSource) {
    String limesurveyField = limesurveyVariableValueSource.getLimesurveyVariableField();
    if(cache == null) {
      loadValues(limesurveyVariableValueSource);
    }
    ValueType type = limesurveyVariableValueSource.getVariable().getValueType();
    Object object = cache.get(limesurveyField);
    return type.valueOf("".equals(object) ? null : object);
  }

  private void loadValues(LimesurveyVariableValueSource limesurveyVariableValueSource) {
    cache = Maps.newHashMap();
    JdbcTemplate jdbcTemplate = new JdbcTemplate(getValueTable().getDatasource().getDataSource());
    String id = getVariableEntity().getIdentifier();
    StringBuilder sql = new StringBuilder();
    LimesurveyValueTable limeValueTable = (LimesurveyValueTable) getValueTable();
    // TODO prevent injection
    sql.append("SELECT * FROM survey_" + limeValueTable.getSid() + " ");
    sql.append("WHERE token=?");
    SqlRowSet rows = jdbcTemplate.queryForRowSet(sql.toString(), new Object[] { id });
    List<String> columns = Lists.newArrayList(rows.getMetaData().getColumnNames());
    if(rows.next()) {
      for(String column : columns) {
        Object object = rows.getObject(column);
        cache.put(column, object);
      }
    }
  }

  @Override
  public LimesurveyValueTable getValueTable() {
    return (LimesurveyValueTable) super.getValueTable();
  }

}
