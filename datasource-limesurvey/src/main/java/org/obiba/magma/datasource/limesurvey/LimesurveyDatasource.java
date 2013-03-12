package org.obiba.magma.datasource.limesurvey;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

import javax.sql.DataSource;

import org.obiba.magma.MagmaRuntimeException;
import org.obiba.magma.ValueTable;
import org.obiba.magma.support.AbstractDatasource;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ConnectionCallback;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;

import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

public class LimesurveyDatasource extends AbstractDatasource {

  private static final String TYPE = "limesurvey";

  private static final String DEFAULT_TABLE_PREFIX = "";

  private final DataSource dataSource;

  private JdbcTemplate jdbcTemplate;

  private final String tablePrefix;

  private Map<String, Integer> sids;

  // identifier quote string
  private String iqs;

  public LimesurveyDatasource(String name, DataSource dataSource) {
    this(name, dataSource, DEFAULT_TABLE_PREFIX);
  }

  public LimesurveyDatasource(String name, DataSource dataSource, String tablePrefix) {
    super(name, TYPE);
    Preconditions.checkArgument(dataSource != null);
    iqs = "";
    this.dataSource = dataSource;
    jdbcTemplate = new JdbcTemplate(dataSource);
    this.tablePrefix = Objects.firstNonNull(tablePrefix, DEFAULT_TABLE_PREFIX);
  }

  @Override
  protected void onInitialise() {
    super.onInitialise();
    StringBuilder sqlDbVersion = new StringBuilder();
    sqlDbVersion.append("SELECT stg_value ");
    sqlDbVersion.append("FROM " + quoteAndPrefix("settings_global") + " ");
    sqlDbVersion.append("WHERE stg_name='DBVersion'");
    String dbVersion = jdbcTemplate.queryForObject(sqlDbVersion.toString(), String.class);
    try {
      if(Float.parseFloat(dbVersion) < 146) {
        throw new MagmaRuntimeException(
            "Limesurvey database version unsupported:" + dbVersion + " (must be greater equal than 146)");
      }
    } catch(NumberFormatException e) {
      throw new MagmaRuntimeException("Limesurvey database version unsupported:" + dbVersion);
    }
    iqs = jdbcTemplate.execute(new ConnectionCallback<String>() {
      @Override
      public String doInConnection(Connection con) throws SQLException, DataAccessException {
        return con.getMetaData().getIdentifierQuoteString();
      }
    });
    if(tablePrefix.contains(iqs)) {
      throw new MagmaRuntimeException("you can not use '" + iqs + "' character in '" + tablePrefix + "'");
    }
  }

  @Override
  protected Set<String> getValueTableNames() {
    JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
    StringBuilder sql = new StringBuilder();
    sql.append("SELECT s.sid, sls.surveyls_title ");
    sql.append("FROM " + quoteAndPrefix("surveys") + " s JOIN " + quoteAndPrefix("surveys_languagesettings") + " sls ");
    sql.append("ON (s.sid=sls.surveyls_survey_id AND s.language=sls.surveyls_language) ");

    Set<String> names = Sets.newLinkedHashSet();
    sids = Maps.newHashMap();
    SqlRowSet rows = jdbcTemplate.queryForRowSet(sql.toString());
    while(rows.next()) {
      String title = LimesurveyUtils.toValidMagmaName(rows.getString("surveyls_title"));
      title = LimesurveyUtils.removeSlashes(title);
      names.add(title);
      sids.put(title, rows.getInt("sid"));
    }
    return Collections.unmodifiableSet(names);
  }

  @Override
  protected ValueTable initialiseValueTable(String tableName) {
    return new LimesurveyValueTable(this, tableName, sids.get(tableName));
  }

  public JdbcTemplate getJdbcTemplate() {
    return jdbcTemplate;
  }

  public DataSource getDataSource() {
    return dataSource;
  }

  String quoteAndPrefix(String identifier) {
    return iqs + tablePrefix + identifier + iqs;
  }

}
