/*
 * Copyright (c) 2016 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.magma.datasource.jdbc;

import com.google.common.base.Strings;

import javax.annotation.Nullable;
import javax.validation.constraints.NotNull;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class JdbcDatasourceSettings {

  public static final int MAX_BATCH_SIZE = 1000;

  //
  // Instance Variables
  //

  private String defaultEntityType;

  private Set<String> mappedTables;

  private Set<JdbcValueTableSettings> tableSettings;

  private boolean useMetadataTables;

  /**
   * True if meta data tables schema supports multiple datasources.
   */
  private boolean multipleDatasources;

  /**
   * The default column name for entity identification.
   */
  private String defaultEntityIdColumnName;

  /**
   * The default column name for creation timestamps
   */
  private String defaultCreatedTimestampColumnName;

  /**
   * The default column name for update timestamps
   */
  private String defaultUpdatedTimestampColumnName;

  private int batchSize = 100;

  private boolean multilines = false;

  //
  // Constructors
  //

  private JdbcDatasourceSettings(String entityType) {
    this.defaultEntityType = entityType;
  }

  //
  // Methods
  //

  public int getBatchSize() {
    return batchSize;
  }

  public String getDefaultEntityType() {
    return defaultEntityType;
  }

  @NotNull
  public Set<String> getMappedTables() {
    if (mappedTables == null) mappedTables = new HashSet<>();
    return mappedTables;
  }

  public boolean hasMappedTables() {
    return !getMappedTables().isEmpty();
  }

  public boolean hasMappedTable(String tableName) {
    return getMappedTables().contains(tableName);
  }

  public void addTableSettings(JdbcValueTableSettings settings) {
    getTableSettings().add(settings);
  }

  @NotNull
  public Set<JdbcValueTableSettings> getTableSettings() {
    if (tableSettings == null) tableSettings = new HashSet<>();
    return tableSettings;
  }

  public boolean hasTableSettingsForSqlTable(String sqlTableName) {
    for (JdbcValueTableSettings settings : getTableSettings()) {
      if (settings.getSqlTableName().equals(sqlTableName)) {
        return true;
      }
    }
    return false;
  }

  @Nullable
  public List<JdbcValueTableSettings> getTableSettingsForSqlTable(String sqlTableName) {
    return getTableSettings().stream() //
        .filter(settings -> settings.getSqlTableName().equals(sqlTableName)) //
        .collect(Collectors.toList());
  }

  @Nullable
  public JdbcValueTableSettings getTableSettingsForMagmaTable(String magmaTableName) {
    for (JdbcValueTableSettings settings : getTableSettings()) {
      if (settings.getMagmaTableName().equals(magmaTableName)) {
        return settings;
      }
    }
    return null;
  }

  public boolean isUseMetadataTables() {
    return useMetadataTables;
  }


  public boolean isMultipleDatasources() {
    return multipleDatasources;
  }

  public String getDefaultEntityIdColumnName() {
    return defaultEntityIdColumnName;
  }

  public boolean hasEntityIdColumnName() {
    return !Strings.isNullOrEmpty(defaultEntityIdColumnName);
  }

  public String getDefaultCreatedTimestampColumnName() {
    return defaultCreatedTimestampColumnName;
  }

  public String getDefaultUpdatedTimestampColumnName() {
    return defaultUpdatedTimestampColumnName;
  }

  public boolean hasCreatedTimestampColumnName() {
    return !Strings.isNullOrEmpty(defaultCreatedTimestampColumnName);
  }

  public boolean hasUpdatedTimestampColumnName() {
    return !Strings.isNullOrEmpty(defaultUpdatedTimestampColumnName);
  }

  public boolean isMultilines() {
    return multilines;
  }

  public static Builder newSettings(String defaultEntityType) {
    return new Builder(defaultEntityType);
  }

  public static class Builder {

    private JdbcDatasourceSettings settings;

    private Builder(String defaultEntityType) {
      this.settings = new JdbcDatasourceSettings(defaultEntityType);
    }

    public Builder mappedTables(Set<String> mappedTables) {
      settings.mappedTables = mappedTables;
      return this;
    }

    public Builder multipleDatasources() {
      return multipleDatasources(true);
    }

    public Builder multipleDatasources(boolean multipleDatasources) {
      settings.multipleDatasources = multipleDatasources;
      return this;
    }

    public Builder tableSettings(Set<JdbcValueTableSettings> tableSettings) {
      settings.tableSettings = tableSettings;
      return this;
    }

    public Builder useMetadataTables() {
      useMetadataTables(true);
      return this;
    }

    public Builder useMetadataTables(boolean useMetadataTables) {
      settings.useMetadataTables = useMetadataTables;
      return this;
    }


    public Builder batchSize(int batchSize) {
      if (batchSize < 1 || batchSize > MAX_BATCH_SIZE) throw new IllegalArgumentException("Invalid batchSize");
      settings.batchSize = batchSize;
      return this;
    }

    public Builder createdTimestampColumn(String name) {
      settings.defaultCreatedTimestampColumnName = name;
      return this;
    }

    public Builder updatedTimestampColumn(String name) {
      settings.defaultUpdatedTimestampColumnName = name;
      return this;
    }

    public Builder entityIdentifierColumn(String name) {
      settings.defaultEntityIdColumnName = Strings.isNullOrEmpty(name) ? JdbcValueTableSettings.ENTITY_ID_COLUMN : name;
      return this;
    }

    public Builder multilines() {
      return multilines(true);
    }

    public Builder multilines(boolean multilines) {
      settings.multilines = multilines;
      return this;
    }

    public JdbcDatasourceSettings build() {
      if (settings.defaultEntityType == null) throw new IllegalArgumentException("null defaultEntityType");
      return settings;
    }
  }
}
