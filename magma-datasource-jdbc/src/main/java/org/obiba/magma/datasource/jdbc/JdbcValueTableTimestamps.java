/*
 * Copyright (c) 2017 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

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

  private final String ESC_CREATED_COLUMN, ESC_UPDATED_COLUMN, ESC_VALUE_TABLES_TABLE, ESC_DATASOURCE_COLUMN,
      ESC_NAME_COLUMN;

  public JdbcValueTableTimestamps(JdbcValueTable table) {
    this.table = table;
    fromMetaData = table.getDatasource().getSettings().isUseMetadataTables();
    withMultipleDatasources = table.getDatasource().getSettings().isMultipleDatasources();

    ESC_CREATED_COLUMN = table.getDatasource().escapeColumnName(CREATED_COLUMN);
    ESC_UPDATED_COLUMN = table.getDatasource().escapeColumnName(UPDATED_COLUMN);
    ESC_VALUE_TABLES_TABLE = table.getDatasource().escapeTableName(VALUE_TABLES_TABLE);
    ESC_DATASOURCE_COLUMN = table.getDatasource().escapeColumnName(DATASOURCE_COLUMN);
    ESC_NAME_COLUMN = table.getDatasource().escapeColumnName(NAME_COLUMN);
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
        .format("SELECT %s FROM %s WHERE %s = ? AND %s = ?", ESC_UPDATED_COLUMN, ESC_VALUE_TABLES_TABLE, ESC_DATASOURCE_COLUMN,
            ESC_NAME_COLUMN)
        : String.format("SELECT %s FROM %s WHERE %s = ?", ESC_UPDATED_COLUMN, ESC_VALUE_TABLES_TABLE, ESC_NAME_COLUMN);
    Object[] params = withMultipleDatasources
        ? new Object[] { table.getDatasource().getName(), table.getName() }
        : new Object[] { table.getName() };

    try {
      return table.getDatasource().getJdbcTemplate().queryForObject(sql, params, Date.class);
    } catch(Exception e) {
      return null;
    }
  }

  private Date getMetaCreatedDate() {
    String sql = withMultipleDatasources
        ? String
        .format("SELECT %s FROM %s WHERE %s = ? AND %s = ?", ESC_CREATED_COLUMN, ESC_VALUE_TABLES_TABLE, ESC_DATASOURCE_COLUMN,
            ESC_NAME_COLUMN)
        : String.format("SELECT %s FROM %s WHERE %s = ?", ESC_CREATED_COLUMN, ESC_VALUE_TABLES_TABLE, ESC_NAME_COLUMN);
    Object[] params = withMultipleDatasources
        ? new Object[] { table.getDatasource().getName(), table.getName() }
        : new Object[] { table.getName() };

    try {
      return table.getDatasource().getJdbcTemplate().queryForObject(sql, params, Date.class);
    } catch(Exception e) {
      return null;
    }
  }

  private Date getOldestValueSetCreatedDate() {
    JdbcDatasource datasource = table.getDatasource();
    String sql = String
        .format("SELECT MIN(%s) FROM %s", datasource.escapeColumnName(table.getCreatedTimestampColumnName()),
            datasource.escapeTableName(table.getSqlName()));
    try {
      return datasource.getJdbcTemplate().queryForObject(sql, Date.class);
    } catch(Exception e) {
      return null;
    }
  }

  private Date getLatestValueSetUpdatedDate() {
    JdbcDatasource datasource = table.getDatasource();
    String sql = String
        .format("SELECT MAX(%s) FROM %s", datasource.escapeColumnName(table.getUpdatedTimestampColumnName()),
            datasource.escapeTableName(table.getSqlName()));

    try {
      return table.getDatasource().getJdbcTemplate().queryForObject(sql, Date.class);
    } catch(Exception e) {
      return null;
    }
  }
}
