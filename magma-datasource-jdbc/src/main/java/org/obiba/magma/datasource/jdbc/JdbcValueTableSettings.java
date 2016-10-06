package org.obiba.magma.datasource.jdbc;

import com.google.common.base.Strings;

public class JdbcValueTableSettings {

  private static final String ENTITY_ID_COLUMN = "id";

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

  //
  // Constructors
  //
  public JdbcValueTableSettings(String sqlTableName, String magmaTableName, String entityType, String entityIdentifierColumn) {
    if(sqlTableName == null) {
      throw new IllegalArgumentException("null sqlTableName");
    }
    this.sqlTableName = sqlTableName;
    this.magmaTableName = Strings.isNullOrEmpty(magmaTableName) ? sqlTableName : magmaTableName;
    this.entityType = entityType;
    this.entityIdentifierColumn = Strings.isNullOrEmpty(entityIdentifierColumn) ? ENTITY_ID_COLUMN : entityIdentifierColumn;
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

  public void setEntityIdentifierColumn(String entityIdentifierColumn) {
    this.entityIdentifierColumn = entityIdentifierColumn;
  }

  public String getEntityIdentifierColumn() {
    return entityIdentifierColumn;
  }

  public String getCreatedTimestampColumnName() {
    return createdTimestampColumnName;
  }

  public void setCreatedTimestampColumnName(String createdTimestampColumnName) {
    this.createdTimestampColumnName = createdTimestampColumnName;
  }

  public boolean hasCreatedTimestampColumnName() {
    return !Strings.isNullOrEmpty(createdTimestampColumnName);
  }

  public String getUpdatedTimestampColumnName() {
    return updatedTimestampColumnName;
  }

  public void setUpdatedTimestampColumnName(String updatedTimestampColumnName) {
    this.updatedTimestampColumnName = updatedTimestampColumnName;
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

  public void setEntityIdentifiersWhere(String entityIdentifiersWhere) {
    this.entityIdentifiersWhere = entityIdentifiersWhere;
  }
}
