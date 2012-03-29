package org.obiba.magma.datasource.limesurvey;

import java.util.Date;

import org.obiba.magma.Timestamps;
import org.obiba.magma.Value;
import org.obiba.magma.type.DateTimeType;
import org.springframework.jdbc.core.JdbcTemplate;

public class LimesurveyTimestamps implements Timestamps {

  private LimesurveyValueTable limesurveyValueTable;

  public LimesurveyTimestamps(LimesurveyValueTable limesurveyValueTable) {
    this.limesurveyValueTable = limesurveyValueTable;
  }

  @Override
  public Value getLastUpdate() {
    return queryTimestamp("MAX");
  }

  @Override
  public Value getCreated() {
    return queryTimestamp("MIN");
  }

  private Value queryTimestamp(String sqlOperator) {
    JdbcTemplate jdbcTemplate = new JdbcTemplate(limesurveyValueTable.getDatasource().getDataSource());
    StringBuilder sql = new StringBuilder();
    sql.append("SELECT " + sqlOperator + "(submitdate) ");
    sql.append("FROM " + limesurveyValueTable.quoteAndPrefix("survey_" + limesurveyValueTable.getSid()));
    Date lastUpdateDate = jdbcTemplate.queryForObject(sql.toString(), Date.class);
    return DateTimeType.get().valueOf(lastUpdateDate);
  }
}
