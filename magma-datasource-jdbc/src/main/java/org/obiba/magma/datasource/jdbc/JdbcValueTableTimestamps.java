package org.obiba.magma.datasource.jdbc;

import java.util.Date;

import org.obiba.magma.Timestamps;
import org.obiba.magma.Value;
import org.obiba.magma.type.DateTimeType;

import static org.obiba.magma.datasource.jdbc.JdbcValueTableWriter.CREATED_COLUMN;
import static org.obiba.magma.datasource.jdbc.JdbcValueTableWriter.DATASOURCE_COLUMN;
import static org.obiba.magma.datasource.jdbc.JdbcValueTableWriter.NAME_COLUMN;
import static org.obiba.magma.datasource.jdbc.JdbcValueTableWriter.UPDATED_COLUMN;
import static org.obiba.magma.datasource.jdbc.JdbcValueTableWriter.VALUE_TABLES_TABLE;

public class JdbcValueTableTimestamps implements Timestamps {

  private final JdbcValueTable table;

  private final boolean fromMetaData;

  private final boolean withMultipleDatasources;

  public JdbcValueTableTimestamps(JdbcValueTable table) {
    this.table = table;
    fromMetaData = table.getDatasource().getSettings().isUseMetadataTables();
    withMultipleDatasources = table.getDatasource().getSettings().isMultipleDatasources();
  }

  @Override
  public Value getLastUpdate() {
    Date date = null;

    if(fromMetaData) {
      date = getMetaUpdatedDate();
    }

    // Get last updated value set if any
    if(table.hasUpdatedTimestampColumn()) {
      Date latest = getLatestValueSetUpdatedDate();
      if(latest != null) {
        date = date == null ? latest : date.after(latest) ? date : latest;
      }
    }

    return DateTimeType.get().valueOf(date == null ? new Date() : date);
  }

  @Override
  public Value getCreated() {
    Date date = null;

    if(fromMetaData) {
      date = getMetaCreatedDate();
    }

    if(date == null && table.hasCreatedTimestampColumn()) {
      date = getOldestValueSetCreatedDate();
    }

    return DateTimeType.get().valueOf(date == null ? new Date() : date);
  }

  //
  // Private methods
  //
  private Date getMetaUpdatedDate() {
    String sql = withMultipleDatasources
        ? String
        .format("SELECT %s FROM %s WHERE %s = ? AND %s = ?", UPDATED_COLUMN, VALUE_TABLES_TABLE, DATASOURCE_COLUMN,
            NAME_COLUMN)
        : String.format("SELECT %s FROM %s WHERE %s = ?", UPDATED_COLUMN, VALUE_TABLES_TABLE, NAME_COLUMN);
    Object[] params = withMultipleDatasources
        ? new Object[] { table.getDatasource().getName(), table.getName() }
        : new Object[] { table.getName() };
    return table.getDatasource().getJdbcTemplate().queryForObject(sql, params, Date.class);
  }

  private Date getMetaCreatedDate() {
    String sql = withMultipleDatasources
        ? String
        .format("SELECT %s FROM %s WHERE %s = ? AND %s = ?", CREATED_COLUMN, VALUE_TABLES_TABLE, DATASOURCE_COLUMN,
            NAME_COLUMN)
        : String.format("SELECT %s FROM %s WHERE %s = ?", CREATED_COLUMN, VALUE_TABLES_TABLE, NAME_COLUMN);
    Object[] params = withMultipleDatasources
        ? new Object[] { table.getDatasource().getName(), table.getName() }
        : new Object[] { table.getName() };
    return table.getDatasource().getJdbcTemplate().queryForObject(sql, params, Date.class);
  }

  private Date getOldestValueSetCreatedDate() {
    String sql = String.format("SELECT MIN(%s) FROM %s", table.getCreatedTimestampColumnName(), table.getSqlName());
    return table.getDatasource().getJdbcTemplate().queryForObject(sql, Date.class);
  }

  private Date getLatestValueSetUpdatedDate() {
    String sql = String.format("SELECT MAX(%s) FROM %s", table.getUpdatedTimestampColumnName(), table.getSqlName());
    return table.getDatasource().getJdbcTemplate().queryForObject(sql, Date.class);
  }
}
