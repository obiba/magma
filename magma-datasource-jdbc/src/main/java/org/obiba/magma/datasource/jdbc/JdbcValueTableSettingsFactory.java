/*
 * Copyright (c) 2019 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.magma.datasource.jdbc;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;

import javax.validation.constraints.NotNull;
import java.util.Collection;
import java.util.List;

import static org.obiba.magma.datasource.jdbc.JdbcValueTableSettings.ENTITY_ID_COLUMN;

public class JdbcValueTableSettingsFactory {

  //
  // Instance Variables
  //

  private String sqlTableName;

  private String magmaTableName;

  /**
   * Partitioning criteria: distinct values will be used as where statements for building {@link JdbcValueTableSettings}.
   */
  private String tablePartitionColumn;

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
  private JdbcValueTableSettingsFactory(String sqlTableName, String tablePartitionColumn) {
    if(Strings.isNullOrEmpty(sqlTableName)) {
      throw new IllegalArgumentException("null or empty sqlTableName");
    }
    if(Strings.isNullOrEmpty(tablePartitionColumn)) {
      throw new IllegalArgumentException("null or empty tablePartitionColumn");
    }
    this.sqlTableName = sqlTableName;
    this.tablePartitionColumn = tablePartitionColumn;
    this.entityType = "Participant";
    this.entityIdentifierColumn = ENTITY_ID_COLUMN;
  }

  //
  // Methods
  //
  @NotNull
  public Collection<JdbcValueTableSettings> createSettings(List<String> partitionCriteria, JdbcDatasource datasource) {
    List<JdbcValueTableSettings> settings = Lists.newArrayList();
    if (partitionCriteria == null || partitionCriteria.isEmpty()) return settings;
    for (String filter : partitionCriteria) {
      // build the where criteria
      String where = datasource.escapeColumnName(tablePartitionColumn) + " = '" + filter + "'";
      if (!Strings.isNullOrEmpty(entityIdentifiersWhere)) where = where + " AND " + entityIdentifiersWhere;
      settings.add(JdbcValueTableSettings.newSettings(sqlTableName)
          .tableName(Strings.isNullOrEmpty(magmaTableName) ? filter : magmaTableName + filter)
          .entityType(entityType).createdTimestampColumn(createdTimestampColumnName)
          .updatedTimestampColumn(updatedTimestampColumnName).entityIdentifierColumn(entityIdentifierColumn)
          .excludedColumns(excludedColumns).includedColumns(includedColumns).multilines(multilines)
          .entityIdentifiersWhere(where).build());
    }
    return settings;
  }

  public String getSqlTableName() {
    return sqlTableName;
  }

  public String getTablePartitionColumn() {
    return tablePartitionColumn;
  }

  public boolean hasMagmaTableName() {
    return !Strings.isNullOrEmpty(magmaTableName);
  }

  public String getMagmaTableName() {
    return magmaTableName;
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

  public static Builder newSettings(String sqlTableName, String entityIdentifiersFilterColumn) {
    return new Builder(sqlTableName, entityIdentifiersFilterColumn);
  }

  public static class Builder {
    private JdbcValueTableSettingsFactory factory;

    private Builder(String name, String tablePartitionColumn) {
      this.factory = new JdbcValueTableSettingsFactory(name, tablePartitionColumn);
    }

    public Builder tableName(String name) {
      factory.magmaTableName = Strings.isNullOrEmpty(name) ? factory.sqlTableName : name;
      return this;
    }

    public Builder entityType(String entityType) {
      factory.entityType = entityType;
      return this;
    }

    public Builder createdTimestampColumn(String name) {
      factory.createdTimestampColumnName = name;
      return this;
    }

    public Builder updatedTimestampColumn(String name) {
      factory.updatedTimestampColumnName = name;
      return this;
    }

    public Builder entityIdentifierColumn(String name) {
      factory.entityIdentifierColumn = Strings.isNullOrEmpty(name) ? ENTITY_ID_COLUMN : name;
      return this;
    }

    public Builder entityIdentifiersWhere(String where) {
      factory.entityIdentifiersWhere = where;
      return this;
    }

    public Builder excludedColumns(String name) {
      factory.excludedColumns = name;
      return this;
    }

    public Builder includedColumns(String name) {
      factory.includedColumns = name;
      return this;
    }

    public Builder multilines() {
      return multilines(true);
    }

    public Builder multilines(boolean multilines) {
      factory.multilines = multilines;
      return this;
    }

    public JdbcValueTableSettingsFactory build() {
      return factory;
    }
  }

}

