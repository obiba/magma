package org.obiba.magma.datasource.limesurvey;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

import javax.sql.DataSource;

import org.obiba.magma.ValueTable;
import org.obiba.magma.support.AbstractDatasource;
import org.obiba.magma.type.TextType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

public class LimesurveyDatasource extends AbstractDatasource {

  public static final String TABLE_PREFIX_KEY = "table_prefix";

  private static final String TYPE = "jdbc";

  private JdbcTemplate jdbcTemplate;

  private Map<String, Integer> sids;

  private String tablePrefix = "";

  protected LimesurveyDatasource(String name, DataSource datasource) {
    super(name, TYPE);
    this.jdbcTemplate = new JdbcTemplate(datasource);
  }

  protected LimesurveyDatasource(String name, DataSource datasource, String tablePrefix) {
    this(name, datasource);
    this.tablePrefix = tablePrefix;
    setAttributeValue(TABLE_PREFIX_KEY, TextType.get().valueOf(tablePrefix));
  }

  @Override
  protected Set<String> getValueTableNames() {
    StringBuilder sql = new StringBuilder();
    sql.append("SELECT s.sid, sls.surveyls_title ");
    sql.append("FROM " + tablePrefix + "surveys s JOIN " + tablePrefix + "surveys_languagesettings sls ");
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
    return new LimesurveyValueTable(this, tableName, sids.get(tableName));
  }

  public JdbcTemplate getJdbcTemplate() {
    return jdbcTemplate;
  }

}
