/**
 *
 */
package org.obiba.magma.datasource.jdbc;

import java.io.ByteArrayInputStream;
import java.security.InvalidParameterException;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.validation.constraints.NotNull;

import org.obiba.magma.Attribute;
import org.obiba.magma.Category;
import org.obiba.magma.Value;
import org.obiba.magma.ValueTableWriter;
import org.obiba.magma.Variable;
import org.obiba.magma.VariableEntity;
import org.obiba.magma.datasource.jdbc.JdbcDatasource.ChangeDatabaseCallback;
import org.obiba.magma.datasource.jdbc.support.BlobTypeVisitor;
import org.obiba.magma.datasource.jdbc.support.InsertDataChangeBuilder;
import org.obiba.magma.datasource.jdbc.support.NameConverter;
import org.obiba.magma.type.LocaleType;
import org.obiba.magma.type.TextType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.support.AbstractLobCreatingPreparedStatementCallback;
import org.springframework.jdbc.support.lob.DefaultLobHandler;
import org.springframework.jdbc.support.lob.LobCreator;
import org.springframework.util.Assert;

import com.google.common.collect.ImmutableList;

import liquibase.change.AddColumnChange;
import liquibase.change.Change;
import liquibase.change.ColumnConfig;
import liquibase.change.ModifyColumnChange;

class JdbcValueTableWriter implements ValueTableWriter {

  @SuppressWarnings("unused")
  private static final Logger log = LoggerFactory.getLogger(JdbcValueTableWriter.class);

  static final String VARIABLE_METADATA_TABLE = "variables";

  static final String ATTRIBUTE_METADATA_TABLE = "variable_attributes";

  static final String CATEGORY_METADATA_TABLE = "categories";

  static final String VALUE_TABLE_COLUMN = "value_table";

  static final String VARIABLE_NAME_COLUMN = "variable_name";

  static final String VALUE_TYPE_COLUMN = "value_type";

  static final String ATTRIBUTE_NAME_COLUMN = "attribute_name";

  static final String ATTRIBUTE_LOCALE_COLUMN = "attribute_locale";

  static final String ATTRIBUTE_NAMESPACE_COLUMN = "attribute_namespace";

  static final String ATTRIBUTE_VALUE_COLUMN = "attribute_value";

  static final String CATEGORY_NAME_COLUMN = "name";

  static final String CATEGORY_CODE_COLUMN = "code";

  static final String CATEGORY_MISSING_COLUMN = "missing";

  static final String ENTITY_ID_COLUMN = "entity_id";

  private final JdbcValueTable valueTable;

  JdbcValueTableWriter(JdbcValueTable valueTable) {
    this.valueTable = valueTable;
  }

  @NotNull
  @Override
  public ValueSetWriter writeValueSet(@NotNull VariableEntity entity) {
    return new JdbcValueSetWriter(entity);
  }

  @Override
  public VariableWriter writeVariables() {
    return valueTable.getDatasource().getSettings().isUseMetadataTables()
        ? new JdbcMetadataVariableWriter()
        : new JdbcVariableWriter();
  }

  @Override
  public void close() {
    valueTable.getDatasource().databaseChanged();
    valueTable.tableChanged();
  }

  private class JdbcVariableWriter implements VariableWriter {

    protected List<Change> changes = new ArrayList<>();

    @Override
    public void writeVariable(@NotNull Variable variable) {
      if(!valueTable.isForEntityType(variable.getEntityType())) {
        throw new InvalidParameterException(
            "Wrong entity type for variable '" + variable.getName() + "': " + valueTable.getEntityType() +
                " expected, " + variable.getEntityType() + " received.");
      }

      doWriteVariable(variable);

      valueTable.writeVariableValueSource(variable);
    }

    @Override
    public void removeVariable(@NotNull Variable variable) {
      throw new UnsupportedOperationException("Variable removal not implemented yet");
    }

    @Override
    public void close() {
      Iterable<BlobTypeVisitor> visitors = ImmutableList.of(new BlobTypeVisitor());
      valueTable.getDatasource().doWithDatabase(new ChangeDatabaseCallback(changes, visitors));
    }

    protected void doWriteVariable(Variable variable) {
      String columnName = NameConverter.toSqlName(variable.getName());
      ColumnConfig column = new ColumnConfig();
      column.setName(columnName);

      if(variable.isRepeatable()) {
        column.setType(SqlTypes.sqlTypeFor(TextType.get(), SqlTypes.TEXT_TYPE_HINT_LARGE));
      } else {
        column.setType(SqlTypes.sqlTypeFor(variable.getValueType(),
            variable.getValueType().equals(TextType.get()) ? SqlTypes.TEXT_TYPE_HINT_MEDIUM : null));
      }

      if(variableExists(variable)) {
        ModifyColumnChange modifyColumnChange = new ModifyColumnChange();
        modifyColumnChange.setTableName(valueTable.getSqlName());
        modifyColumnChange.addColumn(column);
        changes.add(modifyColumnChange);
      } else {
        AddColumnChange addColumnChange = new AddColumnChange();
        addColumnChange.setTableName(valueTable.getSqlName());
        addColumnChange.addColumn(column);
        changes.add(addColumnChange);
      }
    }

    protected boolean variableExists(Variable variable) {
      String columnName = NameConverter.toSqlName(variable.getName());
      return valueTable.getDatasource().getDatabaseSnapshot().getColumn(valueTable.getSqlName(), columnName) != null;
    }
  }

  private class JdbcMetadataVariableWriter extends JdbcVariableWriter {
    //
    // Constants
    //

    private static final String DELETE_VARIABLE_SQL = //
        "DELETE FROM " + VARIABLE_METADATA_TABLE + " WHERE value_table = ? AND name = ?";

    private static final String DELETE_VARIABLE_ATTRIBUTES_SQL = //
        "DELETE FROM " + ATTRIBUTE_METADATA_TABLE + " WHERE value_table = ? AND variable_name = ?";

    private static final String DELETE_VARIABLE_CATEGORIES_SQL = //
        "DELETE FROM " + CATEGORY_METADATA_TABLE + " WHERE value_table = ? AND variable_name = ?";

    //
    // Constructors
    //

    JdbcMetadataVariableWriter() {
    }

    //
    // JdbcVariableWriter Methods
    //

    @Override
    protected void doWriteVariable(Variable variable) {
      boolean variableExists = variableExists(variable);

      String variableSqlName = NameConverter.toSqlName(variable.getName());

      // For an EXISTING variable, delete the existing metadata.
      if(variableExists) {
        JdbcTemplate jdbcTemplate = valueTable.getDatasource().getJdbcTemplate();
        jdbcTemplate.update(DELETE_VARIABLE_SQL, valueTable.getSqlName(), variableSqlName);
        jdbcTemplate.update(DELETE_VARIABLE_ATTRIBUTES_SQL, valueTable.getSqlName(), variableSqlName);
        jdbcTemplate.update(DELETE_VARIABLE_CATEGORIES_SQL, valueTable.getSqlName(), variableSqlName);
      }

      // For ALL variables (existing and new), insert the new metadata.
      InsertDataChangeBuilder builder = new InsertDataChangeBuilder();
      builder.tableName(VARIABLE_METADATA_TABLE).withColumn(VALUE_TABLE_COLUMN, valueTable.getName())
          .withColumn("name", variableSqlName).withColumn(VALUE_TYPE_COLUMN, variable.getValueType().getName())
          .withColumn("mime_type", variable.getMimeType()).withColumn("units", variable.getUnit())
          .withColumn("is_repeatable", variable.isRepeatable())
          .withColumn("occurrence_group", variable.getOccurrenceGroup());
      changes.add(builder.build());

      if(variable.hasAttributes()) {
        writeAttributes(variable);
      }
      if(variable.hasCategories()) {
        writeCategories(variable);
      }

      // For a NEW variable, call the superclass's method to add the necessary column the value table.
      if(!variableExists) {
        super.doWriteVariable(variable);
      }
    }

    //
    // Methods
    //

    private void writeAttributes(Variable variable) {
      for(Attribute attribute : variable.getAttributes()) {
        InsertDataChangeBuilder builder = new InsertDataChangeBuilder();
        builder.tableName(ATTRIBUTE_METADATA_TABLE).withColumn(VALUE_TABLE_COLUMN, valueTable.getSqlName())
            .withColumn(VARIABLE_NAME_COLUMN, NameConverter.toSqlName(variable.getName()))
            .withColumn(ATTRIBUTE_NAME_COLUMN, attribute.getName())
            .withColumn(ATTRIBUTE_LOCALE_COLUMN, attribute.isLocalised() ? attribute.getLocale().toString() : "")
            .withColumn(ATTRIBUTE_NAMESPACE_COLUMN, attribute.hasNamespace() ? attribute.getNamespace() : "")
            .withColumn(ATTRIBUTE_VALUE_COLUMN, attribute.getValue().toString());
        changes.add(builder.build());
      }
    }

    private void writeCategories(Variable variable) {
      for(Category category : variable.getCategories()) {
        InsertDataChangeBuilder builder = new InsertDataChangeBuilder();
        builder.tableName(CATEGORY_METADATA_TABLE).withColumn(VALUE_TABLE_COLUMN, valueTable.getSqlName())
            .withColumn(VARIABLE_NAME_COLUMN, NameConverter.toSqlName(variable.getName()))
            .withColumn(CATEGORY_NAME_COLUMN, category.getName()).withColumn(CATEGORY_CODE_COLUMN, category.getCode())
            .withColumn(CATEGORY_MISSING_COLUMN, category.isMissing());
        changes.add(builder.build());
      }
    }

    @Override
    public void removeVariable(@NotNull Variable variable) {
      throw new UnsupportedOperationException("Variable removal not implemented yet");
    }
  }

  private class JdbcValueSetWriter implements ValueSetWriter {

    private final SimpleDateFormat timestampDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    private final VariableEntity entity;

    private final Map<String, Object> columnValueMap;

    private JdbcValueSetWriter(VariableEntity entity) {
      this.entity = entity;
      columnValueMap = new LinkedHashMap<>();
    }

    @Override
    public void writeValue(@NotNull Variable variable, Value value) {
      Object columnValue = null;
      if(!value.isNull()) {
        if(value.isSequence()) {
          columnValue = value.toString();
        } else {
          columnValue = value.getValue();

          // Persist Locale objects as strings.
          if(value.getValueType() == LocaleType.get()) {
            columnValue = value.toString();
          }
        }
      }
      columnValueMap.put(NameConverter.toSqlName(variable.getName()), columnValue);
    }

    @Override
    public void close() {
      if(columnValueMap.size() != 0) {
        JdbcTemplate jdbcTemplate = valueTable.getDatasource().getJdbcTemplate();

        jdbcTemplate.execute(valueTable.hasValueSet(entity) ? getUpdateSql() : getInsertSql(),
            new AbstractLobCreatingPreparedStatementCallback(new DefaultLobHandler()) {
              @Override
              protected void setValues(PreparedStatement ps, LobCreator lobCreator) throws SQLException {
                int index = 1;
                for(Map.Entry<String, Object> entry : columnValueMap.entrySet()) {
                  if(entry.getValue() instanceof byte[]) {
                    lobCreator.setBlobAsBinaryStream(ps, index++, new ByteArrayInputStream((byte[]) entry.getValue()),
                        ((byte[]) entry.getValue()).length);
                  } else {
                    ps.setObject(index++, entry.getValue());
                  }
                }
              }
            });
      }
    }

    @SuppressWarnings({ "PMD.NcssMethodCount", "OverlyLongMethod" })
    private String getInsertSql() {
      String timestamp = formattedDate(new Date());
      if(valueTable.hasCreatedTimestampColumn()) {
        writeValue(Variable.Builder
            .newVariable(valueTable.getCreatedTimestampColumnName(), TextType.get(), valueTable.getEntityType())
            .build(), TextType.get().valueOf(timestamp));
      }
      if(valueTable.hasUpdatedTimestampColumn()) {
        writeValue(Variable.Builder
            .newVariable(valueTable.getUpdatedTimestampColumnName(), TextType.get(), valueTable.getEntityType())
            .build(), TextType.get().valueOf(timestamp));
      }

      StringBuffer sql = new StringBuffer();

      sql.append("INSERT INTO ");
      sql.append(valueTable.getSqlName());

      Map<String, String> entityIdentifierColumnValueMap = getEntityIdentifierColumnValueMap();

      sql.append(" (");
      for(Map.Entry<String, String> entry : entityIdentifierColumnValueMap.entrySet()) {
        sql.append(entry.getKey());
        sql.append(", ");
      }
      for(Map.Entry<String, Object> entry : columnValueMap.entrySet()) {
        sql.append(entry.getKey());
        sql.append(", ");
      }
      deleteFromEnd(sql, ", ");
      sql.append(") ");

      sql.append("VALUES (");
      for(Map.Entry<String, String> entry : entityIdentifierColumnValueMap.entrySet()) {
        sql.append("'");
        sql.append(entry.getValue());
        sql.append("'");
        sql.append(", ");
      }
      for(int i = 0; i < columnValueMap.size(); i++) {
        sql.append("?");
        sql.append(", ");
      }
      deleteFromEnd(sql, ", ");
      sql.append(")");

      return sql.toString();
    }

    private String getUpdateSql() {
      if(valueTable.hasUpdatedTimestampColumn()) {
        writeValue(Variable.Builder
            .newVariable(valueTable.getUpdatedTimestampColumnName(), TextType.get(), valueTable.getEntityType())
            .build(), TextType.get().valueOf(formattedDate(new Date())));
      }
      StringBuffer sql = new StringBuffer();

      sql.append("UPDATE ");
      sql.append(valueTable.getSqlName());

      sql.append(" SET ");
      for(Map.Entry<String, Object> entry : columnValueMap.entrySet()) {
        sql.append(entry.getKey());
        sql.append(" = ?, ");
      }
      deleteFromEnd(sql, ", ");

      sql.append(" ");
      sql.append(getWhereClause());

      return sql.toString();
    }

    private String getWhereClause() {
      StringBuffer whereClause = new StringBuffer();

      whereClause.append("WHERE ");
      for(Map.Entry<String, String> entry : getEntityIdentifierColumnValueMap().entrySet()) {
        whereClause.append(entry.getKey());
        whereClause.append(" = ");
        whereClause.append("'");
        whereClause.append(entry.getValue());
        whereClause.append("'");
        whereClause.append(" AND ");
      }
      deleteFromEnd(whereClause, " AND ");

      return whereClause.toString();
    }

    private Map<String, String> getEntityIdentifierColumnValueMap() {
      Map<String, String> entityIdentifierColumnValueMap = new LinkedHashMap<>();

      List<String> entityIdentifierColumns = valueTable.getSettings().getEntityIdentifierColumns();

      String[] entityIdentifierValues = entityIdentifierColumns.size() > 1
          ? entity.getIdentifier().split("-")
          : new String[] { entity.getIdentifier() };
      Assert.isTrue(entityIdentifierColumns.size() == entityIdentifierValues.length,
          "number of entity identifier columns does not match number of entity identifiers");

      for(int i = 0; i < entityIdentifierColumns.size(); i++) {
        entityIdentifierColumnValueMap.put(entityIdentifierColumns.get(i), entityIdentifierValues[i]);
      }

      return entityIdentifierColumnValueMap;
    }

    private void deleteFromEnd(StringBuffer sb, String stringToDelete) {
      sb.delete(sb.length() - stringToDelete.length(), sb.length());
    }

    private String formattedDate(Date date) {
      return timestampDateFormat.format(date);
    }
  }
}