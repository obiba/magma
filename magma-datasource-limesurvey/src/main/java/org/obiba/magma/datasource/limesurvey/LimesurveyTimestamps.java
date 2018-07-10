/*
 * Copyright (c) 2017 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.magma.datasource.limesurvey;

import java.util.Date;

import javax.validation.constraints.NotNull;

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

  @NotNull
  @Override
  public Value getLastUpdate() {
    return queryTimestamp("MAX");
  }

  @NotNull
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
    Date lastUpdateDate = null;
    try {
      lastUpdateDate = jdbcTemplate.queryForObject(sql.toString(), Date.class);
    } catch(BadSqlGrammarException e) {
      log.info("survey_{} is probably not active", limesurveyValueTable.getSid());
    }

    if (lastUpdateDate == null) lastUpdateDate = new Date();
    return DateTimeType.get().valueOf(lastUpdateDate);
  }
}
