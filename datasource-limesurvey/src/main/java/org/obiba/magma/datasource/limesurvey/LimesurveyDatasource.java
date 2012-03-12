package org.obiba.magma.datasource.limesurvey;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import javax.sql.DataSource;

import org.obiba.magma.ValueTable;
import org.obiba.magma.support.AbstractDatasource;
import org.springframework.jdbc.core.JdbcTemplate;

import com.google.common.collect.Sets;

public class LimesurveyDatasource extends AbstractDatasource {

  private static final String TYPE = "jdbc";

  private JdbcTemplate jdbcTemplate;

  protected LimesurveyDatasource(String name, DataSource datasource) {
    super(name, TYPE);
    this.jdbcTemplate = new JdbcTemplate(datasource);
  }

  @Override
  // TODO sid is table name... but it must a kind of 'label'
  protected Set<String> getValueTableNames() {
    Set<String> names = Sets.newLinkedHashSet();
    String sql = "SELECT s.sid FROM surveys s";

    List<String> sids = jdbcTemplate.queryForList(sql, String.class);
    names.addAll(sids);

    return Collections.unmodifiableSet(names);
  }

  @Override
  protected ValueTable initialiseValueTable(String tableName) {
    return new LimesurveyValueTable(this, tableName);
  }

  public JdbcTemplate getJdbcTemplate() {
    return jdbcTemplate;
  }
}
