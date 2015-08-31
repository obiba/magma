package org.obiba.magma.datasource.jdbc;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import javax.validation.constraints.NotNull;

import org.obiba.magma.Initialisable;
import org.obiba.magma.VariableEntity;
import org.obiba.magma.support.AbstractVariableEntityProvider;
import org.obiba.magma.support.VariableEntityBean;
import org.springframework.jdbc.core.RowMapper;

class JdbcVariableEntityProvider extends AbstractVariableEntityProvider implements Initialisable {

  private final JdbcValueTable valueTable;

  private Set<VariableEntity> entities = new LinkedHashSet<>();

  JdbcVariableEntityProvider(JdbcValueTable valueTable) {
    super(valueTable.getEntityType());
    this.valueTable = valueTable;
  }

  @Override
  public void initialise() {
    entities = new LinkedHashSet<>();
    List<VariableEntity> results = valueTable.getDatasource().getJdbcTemplate()
        .query(String.format("SELECT %s FROM %s", valueTable.getEntityIdentifierColumnsSql(), valueTable.getSqlName()),
            new RowMapper<VariableEntity>() {
              @Override
              public VariableEntity mapRow(ResultSet rs, int rowNum) throws SQLException {
                return new VariableEntityBean(valueTable.getEntityType(), valueTable.buildEntityIdentifier(rs));
              }

            });
    entities.addAll(results);
  }

  @NotNull
  @Override
  public Set<VariableEntity> getVariableEntities() {
    return Collections.unmodifiableSet(entities);
  }

}
