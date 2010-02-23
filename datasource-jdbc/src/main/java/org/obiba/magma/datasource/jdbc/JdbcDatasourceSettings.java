package org.obiba.magma.datasource.jdbc;

import java.util.Set;

import com.google.common.collect.ImmutableSet;

public class JdbcDatasourceSettings {
  //
  // Instance Variables
  //

  private String defaultEntityType;

  private Set<String> mappedTables;

  private Set<JdbcValueTableSettings> tableSettings;

  private boolean useMetadataTables;

  //
  // Constructors
  //

  public JdbcDatasourceSettings() {
    setMappedTables(null);
    setTableSettings(null);
  }

  public JdbcDatasourceSettings(String defaultEntityType, Set<String> mappedTables, Set<JdbcValueTableSettings> tableSettings, boolean useMetadataTables) {
    if(defaultEntityType == null) {
      throw new IllegalArgumentException("null defaultEntityType");
    }
    this.defaultEntityType = defaultEntityType;

    setMappedTables(mappedTables);
    setTableSettings(tableSettings);

    this.useMetadataTables = useMetadataTables;
  }

  //
  // Methods
  //

  public void setDefaultEntityType(String defaultEntityType) {
    this.defaultEntityType = defaultEntityType;
  }

  public String getDefaultEntityType() {
    return defaultEntityType;
  }

  public void setMappedTables(Set<String> mappedTables) {
    ImmutableSet.Builder<String> mappedTablesBuilder = new ImmutableSet.Builder<String>();
    if(mappedTables != null) {
      mappedTablesBuilder.addAll(mappedTables);
    }
    this.mappedTables = mappedTablesBuilder.build();
  }

  public Set<String> getMappedTables() {
    return mappedTables;
  }

  public boolean hasMappedTable(String tableName) {
    return mappedTables.contains(tableName);
  }

  public void setTableSettings(Set<JdbcValueTableSettings> tableSettings) {
    ImmutableSet.Builder<JdbcValueTableSettings> tableSettingsBuilder = new ImmutableSet.Builder<JdbcValueTableSettings>();
    if(tableSettings != null) {
      tableSettingsBuilder.addAll(tableSettings);
    }
    this.tableSettings = tableSettingsBuilder.build();
  }

  public Set<JdbcValueTableSettings> getTableSettings() {
    return tableSettings;
  }

  public JdbcValueTableSettings getTableSettingsForSqlTable(String sqlTableName) {
    for(JdbcValueTableSettings tableSettings : getTableSettings()) {
      if(tableSettings.getSqlTableName().equals(sqlTableName)) {
        return tableSettings;
      }
    }
    return null;
  }

  public JdbcValueTableSettings getTableSettingsForMagmaTable(String magmaTableName) {
    for(JdbcValueTableSettings tableSettings : getTableSettings()) {
      if(tableSettings.getMagmaTableName().equals(magmaTableName)) {
        return tableSettings;
      }
    }
    return null;
  }

  public boolean useMetadataTables() {
    return useMetadataTables;
  }
}
