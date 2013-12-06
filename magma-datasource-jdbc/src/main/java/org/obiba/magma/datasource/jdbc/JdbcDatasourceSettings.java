package org.obiba.magma.datasource.jdbc;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

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
   * The default a column name for creation timestamps
   */
  private String defaultCreatedTimestampColumnName;

  /**
   * The default a column name for update timestamps
   */
  private String defaultUpdatedTimestampColumnName;

  public JdbcDatasourceSettings() {
  }

  //
  // Constructors
  //
  public JdbcDatasourceSettings(@Nonnull String defaultEntityType, @Nullable Set<String> mappedTables,
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

  public void setDefaultEntityType(@Nonnull String defaultEntityType) {
    this.defaultEntityType = defaultEntityType;
  }

  public String getDefaultEntityType() {
    return defaultEntityType;
  }

  public void setMappedTables(Set<String> mappedTables) {
    this.mappedTables = mappedTables;
  }

  @Nonnull
  public Collection<String> getMappedTables() {
    if(mappedTables == null) mappedTables = new HashSet<>();
    return mappedTables;
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

  @Nonnull
  public Set<JdbcValueTableSettings> getTableSettings() {
    if(tableSettings == null) tableSettings = new HashSet<>();
    return tableSettings;
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

  public boolean isCreatedTimestampColumnNameProvided() {
    return !Strings.isNullOrEmpty(defaultCreatedTimestampColumnName);
  }

  public boolean isUpdatedTimestampColumnNameProvided() {
    return !Strings.isNullOrEmpty(defaultUpdatedTimestampColumnName);
  }

}
