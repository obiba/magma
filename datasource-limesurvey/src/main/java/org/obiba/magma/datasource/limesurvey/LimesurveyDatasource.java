package org.obiba.magma.datasource.limesurvey;

import java.sql.SQLException;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

import javax.sql.DataSource;

import org.obiba.magma.MagmaRuntimeException;
import org.obiba.magma.ValueTable;
import org.obiba.magma.support.AbstractDatasource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

public class LimesurveyDatasource extends AbstractDatasource {

  private static final String TYPE = "jdbc";

  private static final String DEFAULT_TABLE_PREFIX = "";

  private final DataSource dataSource;

  private final String tablePrefix;

  private Map<String, Integer> sids;

  private String iqs = "";

  public LimesurveyDatasource(String name, DataSource dataSource) {
    this(name, dataSource, DEFAULT_TABLE_PREFIX);
  }

  public LimesurveyDatasource(String name, DataSource dataSource, String tablePrefix) {
    super(name, TYPE);
    this.dataSource = dataSource;
    this.tablePrefix = tablePrefix;
  }

  @Override
  protected void onInitialise() {
    super.onInitialise();
    try {
      iqs = dataSource.getConnection().getMetaData().getIdentifierQuoteString();
    } catch(SQLException e) {
      throw new MagmaRuntimeException(e);
    }
  }

  @Override
  protected Set<String> getValueTableNames() {
    JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
    StringBuilder sql = new StringBuilder();
    sql.append("SELECT s.sid, sls.surveyls_title ");
    sql.append("FROM " + quoteIdentifier(tablePrefix + "surveys") + " s JOIN " + quoteIdentifier(tablePrefix + "surveys_languagesettings") + " sls ");
    sql.append("ON (s.sid=sls.surveyls_survey_id AND s.language=sls.surveyls_language) ");

    Set<String> names = Sets.newLinkedHashSet();
    sids = Maps.newHashMap();
    SqlRowSet rows = jdbcTemplate.queryForRowSet(sql.toString());

    while(rows.next()) {
      String title = rows.getString("surveyls_title");
      names.add(title);
      sids.put(title, new Integer(rows.getInt("sid")));
    }
    return Collections.unmodifiableSet(names);
  }

  @Override
  protected ValueTable initialiseValueTable(String tableName) {
    return new LimesurveyValueTable(this, tableName, sids.get(tableName), tablePrefix);
  }

  DataSource getDataSource() {
    return dataSource;
  }

  String getIqs() {
    return iqs;
  }

  String quoteIdentifier(String identifier) {
    return iqs + identifier + iqs;
  }

}
