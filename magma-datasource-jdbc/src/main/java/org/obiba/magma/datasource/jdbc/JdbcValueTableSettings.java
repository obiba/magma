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

import com.google.common.base.Strings;

public class JdbcValueTableSettings {

  static final String ENTITY_ID_COLUMN = "id";

  //
  // Instance Variables
  //

  private String sqlTableName;

  private String magmaTableName;

  private String entityType;

  private String entityIdentifierColumn;

  /**
   * If provided, a column with this name will be populated with creation timestamps.
   */
  private String createdTimestampColumnName;

  /**
   * If provided, a column with this name will be populated with last update timestamps.
   */
  private String updatedTimestampColumnName;

  private String entityIdentifiersWhere;

  private String excludedColumns;

  private String includedColumns;

  private boolean multilines;

  //
  // Constructors
  //
  private JdbcValueTableSettings(String sqlTableName) {
    if(sqlTableName == null) {
      throw new IllegalArgumentException("null sqlTableName");
    }
    this.sqlTableName = sqlTableName;
    this.magmaTableName = sqlTableName;
    this.entityType = "Participant";
    this.entityIdentifierColumn = ENTITY_ID_COLUMN;
  }

  //
  // Methods
  //
  public String getSqlTableName() {
    return sqlTableName;
  }

  public String getMagmaTableName() {
    return magmaTableName != null ? magmaTableName : sqlTableName;
  }

  public String getEntityType() {
    return entityType;
  }

  public String getEntityIdentifierColumn() {
    return entityIdentifierColumn;
  }

  public String getCreatedTimestampColumnName() {
    return createdTimestampColumnName;
  }

  public boolean hasCreatedTimestampColumnName() {
    return !Strings.isNullOrEmpty(createdTimestampColumnName);
  }

  public String getUpdatedTimestampColumnName() {
    return updatedTimestampColumnName;
  }

  public boolean hasUpdatedTimestampColumnName() {
    return !Strings.isNullOrEmpty(updatedTimestampColumnName);
  }

  public boolean hasEntityIdentifiersWhere() {
    return !Strings.isNullOrEmpty(entityIdentifiersWhere);
  }

  public String getEntityIdentifiersWhere() {
    return entityIdentifiersWhere;
  }

  public String getExcludedColumns() {
    return excludedColumns;
  }

  public boolean hasExcludedColumns() {
    return !Strings.isNullOrEmpty(excludedColumns);
  }

  public String getIncludedColumns() {
    return includedColumns;
  }

  public boolean hasIncludedColumns() {
    return !Strings.isNullOrEmpty(includedColumns);
  }

  public boolean isMultilines() {
    return multilines;
  }

  public static Builder newSettings(String sqlTableName) {
    return new Builder(sqlTableName);
  }

  public static class Builder {
    private JdbcValueTableSettings settings;

    private Builder(String name) {
      this.settings = new JdbcValueTableSettings(name);
    }

    public Builder tableName(String name) {
      settings.magmaTableName = Strings.isNullOrEmpty(name) ? settings.sqlTableName : name;
      return this;
    }

    public Builder entityType(String entityType) {
      settings.entityType = entityType;
      return this;
    }

    public Builder createdTimestampColumn(String name) {
      settings.createdTimestampColumnName = name;
      return this;
    }

    public Builder updatedTimestampColumn(String name) {
      settings.updatedTimestampColumnName = name;
      return this;
    }

    public Builder entityIdentifierColumn(String name) {
      settings.entityIdentifierColumn = Strings.isNullOrEmpty(name) ? ENTITY_ID_COLUMN : name;
      return this;
    }

    public Builder entityIdentifiersWhere(String where) {
      settings.entityIdentifiersWhere = where;
      return this;
    }

    public Builder excludedColumns(String name) {
      settings.excludedColumns = name;
      return this;
    }

    public Builder includedColumns(String name) {
      settings.includedColumns = name;
      return this;
    }

    public Builder multilines() {
      return multilines(true);
    }

    public Builder multilines(boolean multilines) {
      settings.multilines = multilines;
      return this;
    }

    public JdbcValueTableSettings build() {
      return settings;
    }
  }

}

