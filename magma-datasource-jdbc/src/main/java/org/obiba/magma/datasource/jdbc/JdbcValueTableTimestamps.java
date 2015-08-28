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

  private JdbcValueTable table;

  private boolean fromMetaData = false;

  public JdbcValueTableTimestamps(JdbcValueTable table) {
    this.table = table;
    fromMetaData = table.getDatasource().getSettings().isUseMetadataTables();
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
      if (latest != null) {
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
    String sql = String.format("SELECT %s FROM %s WHERE %s = '%s' AND %s = '%s'", UPDATED_COLUMN, VALUE_TABLES_TABLE,
        DATASOURCE_COLUMN, table.getDatasource().getName(), NAME_COLUMN, table.getName());
    return table.getDatasource().getJdbcTemplate().queryForObject(sql, Date.class);
  }

  private Date getMetaCreatedDate() {
    String sql = String.format("SELECT %s FROM %s WHERE %s = '%s' AND %s = '%s'", CREATED_COLUMN, VALUE_TABLES_TABLE,
        DATASOURCE_COLUMN, table.getDatasource().getName(), NAME_COLUMN, table.getName());
    return table.getDatasource().getJdbcTemplate().queryForObject(sql, Date.class);
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
