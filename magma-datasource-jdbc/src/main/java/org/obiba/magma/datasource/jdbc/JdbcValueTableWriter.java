/**
 *
 */
package org.obiba.magma.datasource.jdbc;

import java.io.ByteArrayInputStream;
import java.security.InvalidParameterException;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;
import javax.validation.constraints.NotNull;

import org.obiba.magma.Attribute;
import org.obiba.magma.Category;
import org.obiba.magma.Value;
import org.obiba.magma.ValueTableWriter;
import org.obiba.magma.Variable;
import org.obiba.magma.VariableEntity;
import org.obiba.magma.datasource.jdbc.JdbcDatasource.ChangeDatabaseCallback;
import org.obiba.magma.datasource.jdbc.support.AddColumnChangeBuilder;
import org.obiba.magma.datasource.jdbc.support.InsertDataChangeBuilder;
import org.obiba.magma.datasource.jdbc.support.TableUtils;
import org.obiba.magma.type.LocaleType;
import org.obiba.magma.type.TextType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.support.AbstractLobCreatingPreparedStatementCallback;
import org.springframework.jdbc.support.lob.DefaultLobHandler;
import org.springframework.jdbc.support.lob.LobCreator;
import org.springframework.util.Assert;

import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.collect.Iterables;

import liquibase.change.ColumnConfig;
import liquibase.change.core.AddColumnChange;
import liquibase.change.Change;
import liquibase.change.core.DropColumnChange;
import liquibase.change.core.InsertDataChange;
import liquibase.change.core.ModifyDataTypeChange;
import liquibase.change.core.UpdateDataChange;
import liquibase.structure.core.Column;
import liquibase.structure.core.Table;

class JdbcValueTableWriter implements ValueTableWriter {

  @SuppressWarnings("unused")
  private static final Logger log = LoggerFactory.getLogger(JdbcValueTableWriter.class);

  private final SimpleDateFormat timestampDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

  static final String VARIABLE_METADATA_TABLE = "variables";

  static final String VARIABLE_ATTRIBUTE_METADATA_TABLE = "variable_attributes";

  static final String CATEGORY_METADATA_TABLE = "categories";

  static final String VALUE_TABLE_COLUMN = "value_table";

  static final String VARIABLE_NAME_COLUMN = "variable_name";

  static final String VALUE_TYPE_COLUMN = "value_type";

  static final String ATTRIBUTE_NAME_COLUMN = "attribute_name";

  static final String ATTRIBUTE_LOCALE_COLUMN = "attribute_locale";

  static final String ATTRIBUTE_NAMESPACE_COLUMN = "attribute_namespace";

  static final String ATTRIBUTE_VALUE_COLUMN = "attribute_value";

  static final String CATEGORY_NAME_COLUMN = "name";

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

  private String formattedDate(java.util.Date date) {
    return timestampDateFormat.format(date);
  }

  private String getVariableSqlName(String variableName) {
    return valueTable.getVariableSqlName(variableName);
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
      Variable existingVariable = valueTable.getVariable(variable.getName());

      DropColumnChange dcc = new DropColumnChange();
      dcc.setTableName(valueTable.getSqlName());
      dcc.setColumnName(getVariableSqlName(existingVariable.getName()));
      changes.add(dcc);

      if(valueTable.hasUpdatedTimestampColumn()) {
        UpdateDataChange udc = new UpdateDataChange();
        udc.setTableName(valueTable.getSqlName());
        ColumnConfig col = new ColumnConfig();
        col.setName(valueTable.getUpdatedTimestampColumnName());
        col.setValueDate(formattedDate(new java.util.Date()));
        udc.addColumn(col);

        changes.add(udc);
      }

      JdbcTemplate jdbcTemplate = valueTable.getDatasource().getJdbcTemplate();
      jdbcTemplate
          .update(String.format("DELETE FROM %s WHERE value_table = ? AND name = ?", JdbcDatasource.VARIABLES_MAPPING),
              valueTable.getName(), variable.getName());
    }

    @Override
    public void close() {
      valueTable.getDatasource().doWithDatabase(new ChangeDatabaseCallback(changes));
      valueTable.refreshVariablesMap();
    }

    protected void doWriteVariable(Variable variable) {
      String columnName = getVariableSqlName(variable.getName());
      String dataType = variable.isRepeatable()
          ? SqlTypes.sqlTypeFor(TextType.get(), SqlTypes.TEXT_TYPE_HINT_LARGE)
          : SqlTypes.sqlTypeFor(variable.getValueType(),
              variable.getValueType().equals(TextType.get()) ? SqlTypes.TEXT_TYPE_HINT_MEDIUM : null);

      if(variableExists(variable)) {
        modifyColumn(columnName, dataType);
      } else {
        addNewColumn(variable.getName(), dataType);
      }
    }

    private void modifyColumn(String columnName, String dataType) {
      ModifyDataTypeChange modifyDataTypeChange = new ModifyDataTypeChange();
      modifyDataTypeChange.setTableName(valueTable.getSqlName());
      modifyDataTypeChange.setColumnName(columnName);
      modifyDataTypeChange.setNewDataType(dataType);
      changes.add(modifyDataTypeChange);
    }

    private void addNewColumn(String variableName, String dataType) {
      String columnName = generateColumnName(variableName);

      AddColumnChange addColumnChange = AddColumnChangeBuilder.newBuilder()//
          .table(valueTable.getSqlName())//
          .column(columnName, dataType).build();

      InsertDataChange idc = InsertDataChangeBuilder.newBuilder().tableName(JdbcDatasource.VARIABLES_MAPPING)
          .withColumn("sql_name", columnName).withColumn("name", variableName)
          .withColumn("value_table", valueTable.getName()).build();

      changes.add(addColumnChange);
      changes.add(idc);
    }

    private String generateColumnName(String variableName) {
      return String.format("%s", TableUtils.normalize(variableName));
    }

    protected boolean variableExists(Variable variable) {
      String columnName = getVariableSqlName(variable.getName());

      return valueTable.getDatasource().getDatabaseSnapshot()
          .get(new Column(Table.class, null, null, valueTable.getSqlName(), columnName)) != null;
    }
  }

  private class JdbcMetadataVariableWriter extends JdbcVariableWriter {
    //
    // Constants
    //

    private static final String DELETE_VARIABLE_SQL = //
        "DELETE FROM " + VARIABLE_METADATA_TABLE + " WHERE value_table = ? AND name = ?";

    private static final String DELETE_VARIABLE_ATTRIBUTES_SQL = //
        "DELETE FROM " + VARIABLE_ATTRIBUTE_METADATA_TABLE + " WHERE value_table = ? AND variable_name = ?";

    private static final String DELETE_VARIABLE_CATEGORIES_SQL = //
        "DELETE FROM " + CATEGORY_METADATA_TABLE + " WHERE value_table = ? AND variable_name = ?";

    //
    // JdbcVariableWriter Methods
    //

    @Override
    protected void doWriteVariable(Variable variable) {
      boolean variableExists = variableExists(variable);

      if(variableExists) {
        deleteVariableMetadata(variable.getName());
      }

      InsertDataChangeBuilder builder = new InsertDataChangeBuilder();
      builder.tableName(VARIABLE_METADATA_TABLE) //
          .withColumn(VALUE_TABLE_COLUMN, valueTable.getName()) //
          .withColumn("name", variable.getName()) //
          .withColumn(VALUE_TYPE_COLUMN, variable.getValueType().getName()) //
          .withColumn("mime_type", variable.getMimeType())//
          .withColumn("units", variable.getUnit()) //
          .withColumn("is_repeatable", variable.isRepeatable()) //
          .withColumn("occurrence_group", variable.getOccurrenceGroup());

      changes.add(builder.build());

      if(variable.hasAttributes()) {
        writeAttributes(variable);
      }

      if(variable.hasCategories()) {
        writeCategories(variable);
      }

      if(!variableExists) {
        super.doWriteVariable(variable);
      }
    }

    @Override
    public void removeVariable(@NotNull Variable variable) {
      super.removeVariable(variable);
      deleteVariableMetadata(variable.getName());
    }

    //
    // Methods
    //

    private void deleteVariableMetadata(String variableName) {
      JdbcTemplate jdbcTemplate = valueTable.getDatasource().getJdbcTemplate();
      jdbcTemplate.update(DELETE_VARIABLE_SQL, valueTable.getName(), variableName);
      jdbcTemplate.update(DELETE_VARIABLE_ATTRIBUTES_SQL, valueTable.getName(), variableName);
      jdbcTemplate.update(DELETE_VARIABLE_CATEGORIES_SQL, valueTable.getName(), variableName);
    }

    private void writeAttributes(Variable variable) {
      for(Attribute attribute : variable.getAttributes()) {
        InsertDataChangeBuilder builder = new InsertDataChangeBuilder();
        builder.tableName(VARIABLE_ATTRIBUTE_METADATA_TABLE).withColumn(VALUE_TABLE_COLUMN, valueTable.getName())
            .withColumn(VARIABLE_NAME_COLUMN, variable.getName()).withColumn(ATTRIBUTE_NAME_COLUMN, attribute.getName())
            .withColumn(ATTRIBUTE_LOCALE_COLUMN, attribute.isLocalised() ? attribute.getLocale().toString() : "")
            .withColumn(ATTRIBUTE_NAMESPACE_COLUMN, attribute.hasNamespace() ? attribute.getNamespace() : "")
            .withColumn(ATTRIBUTE_VALUE_COLUMN, attribute.getValue().toString());
        changes.add(builder.build());
      }
    }

    private void writeCategories(Variable variable) {
      for(Category category : variable.getCategories()) {
        InsertDataChangeBuilder builder = new InsertDataChangeBuilder();
        builder.tableName(CATEGORY_METADATA_TABLE).withColumn(VALUE_TABLE_COLUMN, valueTable.getName())
            .withColumn(VARIABLE_NAME_COLUMN, variable.getName()).withColumn(CATEGORY_NAME_COLUMN, category.getName())
            .withColumn(CATEGORY_MISSING_COLUMN, category.isMissing());
        changes.add(builder.build());
      }
    }
  }

  private class JdbcValueSetWriter implements ValueSetWriter {

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

      columnValueMap.put(getVariableSqlName(variable.getName()), columnValue);
    }

    @Override
    public void remove() {
      columnValueMap.clear();
    }

    @Override
    public void close() {
      JdbcTemplate jdbcTemplate = valueTable.getDatasource().getJdbcTemplate();

      if(columnValueMap.size() == 0) {
        jdbcTemplate
            .execute(getDeleteSql(), getPreparedStatementCallback(getEntityIdentifierColumnValueMap().entrySet()));
        return;
      }

      jdbcTemplate.execute(valueTable.hasValueSet(entity) ? getUpdateSql() : getInsertSql(),
          getPreparedStatementCallback(
              Iterables.concat(getEntityIdentifierColumnValueMap().entrySet(), columnValueMap.entrySet())));
    }

    private <T extends Map.Entry<String, ?>> AbstractLobCreatingPreparedStatementCallback getPreparedStatementCallback(
        final Iterable<T> valueMap) {
      return new AbstractLobCreatingPreparedStatementCallback(new DefaultLobHandler()) {
        @Override
        protected void setValues(PreparedStatement ps, LobCreator lobCreator) throws SQLException {
          int index = 1;

          for(T entry : valueMap) {
            if(entry.getValue() instanceof byte[]) {
              lobCreator.setBlobAsBinaryStream(ps, index++, new ByteArrayInputStream((byte[]) entry.getValue()),
                  ((byte[]) entry.getValue()).length);
            } else if(entry.getValue() instanceof Date) {
              ps.setDate(index++, (Date) entry.getValue());
            } else {
              ps.setObject(index++, entry.getValue());
            }
          }
        }
      };
    }

    @SuppressWarnings({ "PMD.NcssMethodCount", "OverlyLongMethod" })
    private String getInsertSql() {
      Date timestamp = new Date(System.currentTimeMillis());

      if(valueTable.hasCreatedTimestampColumn()) {
        columnValueMap.put(valueTable.getCreatedTimestampColumnName(), timestamp);
      }

      if(valueTable.hasUpdatedTimestampColumn()) {
        columnValueMap.put(valueTable.getUpdatedTimestampColumnName(), timestamp);
      }

      String colNames = Joiner.on(", ")
          .join(Iterables.concat(getEntityIdentifierColumnValueMap().keySet(), columnValueMap.keySet()));
      String values = Joiner.on(", ")
          .join(Collections.nCopies(getEntityIdentifierColumnValueMap().size() + columnValueMap.size(), "?"));

      return String.format("INSERT INTO %s (%s) VALUES (%s)", valueTable.getSqlName(), colNames, values);
    }

    private String getUpdateSql() {
      if(valueTable.hasUpdatedTimestampColumn()) {
        if(valueTable.hasUpdatedTimestampColumn()) {
          columnValueMap.put(valueTable.getUpdatedTimestampColumnName(), new Date(System.currentTimeMillis()));
        }
      }

      String colNames = Joiner.on(", ")
          .join(Iterables.transform(columnValueMap.keySet(), new Function<String, String>() {
            @Nullable
            @Override
            public String apply(@Nullable String input) {
              return String.format("%s = ?", input);
            }
          }));

      StringBuffer sql = new StringBuffer();
      sql.append(String.format("UPDATE %s SET %s %s", valueTable.getSqlName(), colNames, getWhereClause()));

      return sql.toString();
    }

    private String getDeleteSql() {
      return String.format("DELETE FROM %s %s", valueTable.getSqlName(), getWhereClause());
    }

    private String getWhereClause() {
      StringBuffer whereClause = new StringBuffer();
      whereClause.append("WHERE ");

      whereClause.append(Joiner.on(" AND ")
          .join(Iterables.transform(getEntityIdentifierColumnValueMap().keySet(), new Function<String, String>() {
            @Nullable
            @Override
            public String apply(String input) {
              return String.format("%s = ?", input);
            }
          })));

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
  }
}