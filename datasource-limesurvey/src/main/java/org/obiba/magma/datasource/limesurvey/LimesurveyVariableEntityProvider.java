package org.obiba.magma.datasource.limesurvey;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.google.common.collect.Sets;
import org.obiba.magma.Datasource;
import org.obiba.magma.Initialisable;
import org.obiba.magma.VariableEntity;
import org.obiba.magma.support.AbstractVariableEntityProvider;
import org.obiba.magma.support.VariableEntityBean;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

public class LimesurveyVariableEntityProvider extends AbstractVariableEntityProvider implements Initialisable {

  private HashSet<VariableEntity> entities;

  private final Integer sid;

  private final LimesurveyDatasource datasource;

  protected LimesurveyVariableEntityProvider(String entityType, Datasource datasource, Integer sid) {
    super(entityType);
    this.datasource = (LimesurveyDatasource) datasource;
    this.sid = sid;
  }

  @Override
  public void initialise() {
    String sqlEntities = "SELECT " + datasource.quoteAndPrefix("token") + " FROM " + datasource
        .quoteAndPrefix("survey_" + sid);
    JdbcTemplate jdbcTemplate = new JdbcTemplate(datasource.getDataSource());
    List<VariableEntity> entityList = jdbcTemplate.query(sqlEntities, new RowMapper<VariableEntity>() {

      @Override
      public VariableEntity mapRow(ResultSet rs, int rowNum) throws SQLException {
        String entityId = rs.getString("token");
        return new VariableEntityBean(LimesurveyValueTable.PARTICIPANT, entityId);
      }
    });
    entities = Sets.newHashSet(entityList);
  }

  @Override
  public Set<VariableEntity> getVariableEntities() {
    return Collections.unmodifiableSet(entities);
  }
}
