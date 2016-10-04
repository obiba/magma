package org.obiba.magma.datasource.jdbc;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import com.google.common.base.Strings;

public class JdbcValueTableSettings {

  private static final String ENTITY_ID_COLUMN = "id";

  //
  // Instance Variables
  //

  private String sqlTableName;

  private String magmaTableName;

  private String entityType;

  private List<String> entityIdentifierColumns;

  /**
   * If provided, a column with this name will be populated with creation timestamps.
   */
  private String createdTimestampColumnName;

  /**
   * If provided, a column with this name will be populated with last update timestamps.
   */
  private String updatedTimestampColumnName;

  //
  // Constructors
  //
  public JdbcValueTableSettings() {
  }

  public JdbcValueTableSettings(String sqlTableName, String magmaTableName, String entityType,
      List<String> entityIdentifierColumns) {

    if(sqlTableName == null) {
      throw new IllegalArgumentException("null sqlTableName");
    }

    this.sqlTableName = sqlTableName;

    this.magmaTableName = magmaTableName != null ? magmaTableName : sqlTableName;

    this.entityType = entityType;

    this.entityIdentifierColumns = entityIdentifierColumns == null || entityIdentifierColumns.isEmpty() ? Arrays
        .asList(ENTITY_ID_COLUMN) : new ArrayList<>(entityIdentifierColumns);
  }

  public JdbcValueTableSettings(String sqlTableName, String magmaTableName, String entityType,
      String entityIdColumnName) {
    this(sqlTableName, magmaTableName, entityType,
        Arrays.asList(Strings.isNullOrEmpty(entityIdColumnName) ? ENTITY_ID_COLUMN : entityIdColumnName));
  }

  //
  // Methods
  //

  public void setSqlTableName(String sqlTableName) {
    this.sqlTableName = sqlTableName;
  }

  public String getSqlTableName() {
    return sqlTableName;
  }

  public void setMagmaTableName(String magmaTableName) {
    this.magmaTableName = magmaTableName;
  }

  public String getMagmaTableName() {
    return magmaTableName != null ? magmaTableName : sqlTableName;
  }

  public void setEntityType(String entityType) {
    this.entityType = entityType;
  }

  public String getEntityType() {
    return entityType;
  }

  public void setEntityIdentifierColumns(List<String> entityIdentifierColumns) {
    this.entityIdentifierColumns = new ArrayList<>();

    if(entityIdentifierColumns != null) {
      this.entityIdentifierColumns.addAll(entityIdentifierColumns);
    }
  }

  public List<String> getEntityIdentifierColumns() {
    return Collections.unmodifiableList(entityIdentifierColumns);
  }

  public String getEntityIdentifierColumn() {
    return entityIdentifierColumns.get(0);
  }

  public String getCreatedTimestampColumnName() {
    return createdTimestampColumnName;
  }

  public void setCreatedTimestampColumnName(String createdTimestampColumnName) {
    this.createdTimestampColumnName = createdTimestampColumnName;
  }

  public String getUpdatedTimestampColumnName() {
    return updatedTimestampColumnName;
  }

  public void setUpdatedTimestampColumnName(String updatedTimestampColumnName) {
    this.updatedTimestampColumnName = updatedTimestampColumnName;
  }

  public boolean isCreatedTimestampColumnNameProvided() {
    return !Strings.isNullOrEmpty(createdTimestampColumnName);
  }

  public boolean isUpdatedTimestampColumnNameProvided() {
    return !Strings.isNullOrEmpty(updatedTimestampColumnName);
  }
}
