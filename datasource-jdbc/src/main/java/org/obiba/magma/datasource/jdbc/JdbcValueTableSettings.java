package org.obiba.magma.datasource.jdbc;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.obiba.magma.datasource.jdbc.support.NameConverter;

public class JdbcValueTableSettings {
  //
  // Instance Variables
  //

  private String sqlTableName;

  private String magmaTableName;

  private String entityType;

  private List<String> entityIdentifierColumns;

  //
  // Constructors
  //

  public JdbcValueTableSettings(String sqlTableName, String magmaTableName, String entityType, List<String> entityIdentifierColumns) {
    if(sqlTableName == null) {
      throw new IllegalArgumentException("null sqlTableName");
    }
    if(entityIdentifierColumns == null || entityIdentifierColumns.isEmpty()) {
      throw new IllegalArgumentException("null or empty entityIdentityColumns");
    }

    this.sqlTableName = sqlTableName;

    this.magmaTableName = (magmaTableName != null) ? magmaTableName : NameConverter.toMagmaName(sqlTableName);

    this.entityType = entityType;

    this.entityIdentifierColumns = new ArrayList<String>(entityIdentifierColumns);
  }

  //
  // Methods
  //

  public String getSqlTableName() {
    return sqlTableName;
  }

  public String getMagmaTableName() {
    return magmaTableName;
  }

  public String getEntityType() {
    return entityType;
  }

  public List<String> getEntityIdentifierColumns() {
    return Collections.unmodifiableList(entityIdentifierColumns);
  }
}
