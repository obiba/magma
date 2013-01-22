package org.obiba.magma.datasource.limesurvey;

import java.util.Date;

import org.obiba.magma.Timestamps;
import org.obiba.magma.Value;
import org.obiba.magma.type.DateTimeType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.BadSqlGrammarException;
import org.springframework.jdbc.core.JdbcTemplate;

public class LimesurveyTimestamps implements Timestamps {

  private final LimesurveyValueTable limesurveyValueTable;

  private static final Logger log = LoggerFactory.getLogger(LimesurveyVariableEntityProvider.class);

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
    sql.append("SELECT ").append(sqlOperator).append("(submitdate) ");
    sql.append("FROM ").append(limesurveyValueTable.quoteAndPrefix("survey_" + limesurveyValueTable.getSid()));
    sql.append("WHERE submitdate is not NULL");
    Date lastUpdateDate;
    try {
      lastUpdateDate = jdbcTemplate.queryForObject(sql.toString(), Date.class);
    } catch(BadSqlGrammarException e) {
      lastUpdateDate = new Date();
      log.info("survey_" + limesurveyValueTable.getSid() + " is probably not active");
    }
    return DateTimeType.get().valueOf(lastUpdateDate);
  }
}
