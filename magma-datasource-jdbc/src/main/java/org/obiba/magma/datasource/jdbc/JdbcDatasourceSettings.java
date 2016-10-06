package org.obiba.magma.datasource.jdbc;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import javax.annotation.Nullable;
import javax.validation.constraints.NotNull;

import com.google.common.base.Strings;

public class JdbcDatasourceSettings {
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

  public static final int MAX_BATCH_SIZE = 1000;

  private int batchSize = 100;

  //
  // Constructors
  //

  public JdbcDatasourceSettings() {
  }

  public JdbcDatasourceSettings(@NotNull String defaultEntityType, @Nullable Set<String> mappedTables,
      @Nullable Set<JdbcValueTableSettings> tableSettings, boolean useMetadataTables) {
    //noinspection ConstantConditions
    if(defaultEntityType == null) throw new IllegalArgumentException("null defaultEntityType");
    this.defaultEntityType = defaultEntityType;
    setMappedTables(mappedTables);
    setTableSettings(tableSettings);
    this.useMetadataTables = useMetadataTables;
  }

  //
  // Methods
  //

  public int getBatchSize() {
    return batchSize;
  }

  public void setBatchSize(int batchSize) {
    if(batchSize < 1 || batchSize > MAX_BATCH_SIZE) throw new IllegalArgumentException("Invalid batchSize");

    this.batchSize = batchSize;
  }

  public void setDefaultEntityType(@NotNull String defaultEntityType) {
    this.defaultEntityType = defaultEntityType;
  }

  public String getDefaultEntityType() {
    return defaultEntityType;
  }

  public void setMappedTables(Set<String> mappedTables) {
    this.mappedTables = mappedTables;
  }

  @NotNull
  public Set<String> getMappedTables() {
    if(mappedTables == null) mappedTables = new HashSet<>();
    return mappedTables;
  }

  public boolean hasMappedTables() {
    return !getMappedTables().isEmpty();
  }

  public boolean hasMappedTable(String tableName) {
    return getMappedTables().contains(tableName);
  }

  public void setTableSettings(Set<JdbcValueTableSettings> tableSettings) {
    this.tableSettings = tableSettings;
  }

  public void addTableSettings(JdbcValueTableSettings settings) {
    getTableSettings().add(settings);
  }

  @NotNull
  public Set<JdbcValueTableSettings> getTableSettings() {
    if(tableSettings == null) tableSettings = new HashSet<>();
    return tableSettings;
  }

  public boolean hasTableSettingsForSqlTable(String sqlTableName) {
    for(JdbcValueTableSettings settings : getTableSettings()) {
      if(settings.getSqlTableName().equals(sqlTableName)) {
        return true;
      }
    }
    return false;
  }

  @Nullable
  public JdbcValueTableSettings getTableSettingsForSqlTable(String sqlTableName) {
    for(JdbcValueTableSettings settings : getTableSettings()) {
      if(settings.getSqlTableName().equals(sqlTableName)) {
        return settings;
      }
    }
    return null;
  }

  @Nullable
  public JdbcValueTableSettings getTableSettingsForMagmaTable(String magmaTableName) {
    for(JdbcValueTableSettings settings : getTableSettings()) {
      if(settings.getMagmaTableName().equals(magmaTableName)) {
        return settings;
      }
    }
    return null;
  }

  public boolean isUseMetadataTables() {
    return useMetadataTables;
  }

  public void setUseMetadataTables(boolean useMetadataTables) {
    this.useMetadataTables = useMetadataTables;
  }

  public boolean isMultipleDatasources() {
    return multipleDatasources;
  }

  public void setMultipleDatasources(boolean multipleDatasources) {
    this.multipleDatasources = multipleDatasources;
  }

  public String getDefaultEntityIdColumnName() {
    return defaultEntityIdColumnName;
  }

  public void setDefaultEntityIdColumnName(String defaultEntityIdColumnName) {
    this.defaultEntityIdColumnName = defaultEntityIdColumnName;
  }

  public boolean hasEntityIdColumnName() {
    return !Strings.isNullOrEmpty(defaultEntityIdColumnName);
  }

  public String getDefaultCreatedTimestampColumnName() {
    return defaultCreatedTimestampColumnName;
  }

  public void setDefaultCreatedTimestampColumnName(String defaultCreatedTimestampColumnName) {
    this.defaultCreatedTimestampColumnName = defaultCreatedTimestampColumnName;
  }

  public String getDefaultUpdatedTimestampColumnName() {
    return defaultUpdatedTimestampColumnName;
  }

  public void setDefaultUpdatedTimestampColumnName(String defaultUpdatedTimestampColumnName) {
    this.defaultUpdatedTimestampColumnName = defaultUpdatedTimestampColumnName;
  }

  public boolean hasCreatedTimestampColumnName() {
    return !Strings.isNullOrEmpty(defaultCreatedTimestampColumnName);
  }

  public boolean hasUpdatedTimestampColumnName() {
    return !Strings.isNullOrEmpty(defaultUpdatedTimestampColumnName);
  }
}
