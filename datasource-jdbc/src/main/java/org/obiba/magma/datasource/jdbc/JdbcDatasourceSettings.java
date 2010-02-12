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

  //
  // Constructors
  //

  public JdbcDatasourceSettings(String defaultEntityType, Set<String> mappedTables, Set<JdbcValueTableSettings> tableSettings) {
    if(defaultEntityType == null) {
      throw new IllegalArgumentException("null defaultEntityType");
    }
    this.defaultEntityType = defaultEntityType;

    ImmutableSet.Builder<String> mappedTablesBuilder = new ImmutableSet.Builder<String>();
    if(mappedTables != null) {
      mappedTablesBuilder.addAll(mappedTables);
    }
    this.mappedTables = mappedTablesBuilder.build();

    ImmutableSet.Builder<JdbcValueTableSettings> tableSettingsBuilder = new ImmutableSet.Builder<JdbcValueTableSettings>();
    if(tableSettings != null) {
      tableSettingsBuilder.addAll(tableSettings);
    }
    this.tableSettings = tableSettingsBuilder.build();
  }

  //
  // Methods
  //

  public String getDefaultEntityType() {
    return defaultEntityType;
  }

  public Set<String> getMappedTables() {
    return mappedTables;
  }

  public boolean hasMappedTable(String tableName) {
    return mappedTables.contains(tableName);
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
}
