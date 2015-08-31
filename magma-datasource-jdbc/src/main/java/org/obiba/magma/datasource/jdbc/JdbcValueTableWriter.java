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
import org.obiba.magma.datasource.jdbc.support.UpdateDataChangeBuilder;
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

import liquibase.change.Change;
import liquibase.change.ColumnConfig;
import liquibase.change.core.AddColumnChange;
import liquibase.change.core.DropColumnChange;
import liquibase.change.core.ModifyDataTypeChange;
import liquibase.change.core.UpdateDataChange;
import liquibase.structure.core.Column;
import liquibase.structure.core.Table;

class JdbcValueTableWriter implements ValueTableWriter {

  @SuppressWarnings("unused")
  private static final Logger log = LoggerFactory.getLogger(JdbcValueTableWriter.class);

  private final SimpleDateFormat timestampDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

  static final String VALUE_TABLES_TABLE = "value_tables";

  static final String VARIABLES_TABLE = "variables";

  static final String VARIABLE_ATTRIBUTES_TABLE = "variable_attributes";

  static final String CATEGORIES_TABLE = "categories";

  static final String CATEGORY_ATTRIBUTES_TABLE = "category_attributes";

  static final String DATASOURCE_COLUMN = "datasource";

  static final String VALUE_TABLE_COLUMN = "value_table";

  static final String VARIABLE_COLUMN = "variable";

  static final String CATEGORY_COLUMN = "category";

  static final String VALUE_TYPE_COLUMN = "value_type";

  static final String LOCALE_COLUMN = "locale";

  static final String NAMESPACE_COLUMN = "namespace";

  static final String VALUE_COLUMN = "value";

  static final String NAME_COLUMN = "name";

  static final String SQL_NAME_COLUMN = "sql_name";

  static final String MISSING_COLUMN = "missing";

  static final String ENTITY_ID_COLUMN = "entity_id";

  static final String ENTITY_TYPE_COLUMN = "entity_type";

  static final String CREATED_COLUMN = "created";

  static final String UPDATED_COLUMN = "updated";

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

  private class JdbcVariableWriter implements ValueTableWriter.VariableWriter {

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
      changes.add(addColumnChange);
    }

    protected String generateColumnName(String variableName) {
      return String.format("%s", TableUtils.normalize(variableName, 64));
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
        "DELETE FROM " + VARIABLES_TABLE + " WHERE " + DATASOURCE_COLUMN + " = ? AND " + VALUE_TABLE_COLUMN +
            " = ? AND " + NAME_COLUMN + " = ?";

    private static final String DELETE_VARIABLE_ATTRIBUTES_SQL = //
        "DELETE FROM " + VARIABLE_ATTRIBUTES_TABLE + " WHERE " + DATASOURCE_COLUMN + " = ? AND " +
            VALUE_TABLE_COLUMN + " = ? AND " + VARIABLE_COLUMN + " = ?";

    private static final String DELETE_VARIABLE_CATEGORIES_SQL = //
        "DELETE FROM " + CATEGORIES_TABLE + " WHERE " + DATASOURCE_COLUMN + " = ? AND " + VALUE_TABLE_COLUMN +
            " = ? AND " + VARIABLE_COLUMN + " = ?";

    private static final String DELETE_VARIABLE_CATEGORY_ATTRIBUTES_SQL = //
        "DELETE FROM " + CATEGORY_ATTRIBUTES_TABLE + " WHERE " + DATASOURCE_COLUMN + " = ? AND " +
            VALUE_TABLE_COLUMN + " = ? AND " + VARIABLE_COLUMN + " = ?";

    //
    // JdbcVariableWriter Methods
    //

    @Override
    protected void doWriteVariable(Variable variable) {
      boolean variableExists = variableExists(variable);

      if(variableExists) {
        deleteVariableMetadata(variable.getName());
      } else {
        addTableTimestampChange();
      }

      InsertDataChangeBuilder builder = new InsertDataChangeBuilder();
      builder.tableName(VARIABLES_TABLE) //
          .withColumn(DATASOURCE_COLUMN, valueTable.getDatasource().getName()) //
          .withColumn(VALUE_TABLE_COLUMN, valueTable.getName()) //
          .withColumn(NAME_COLUMN, variable.getName()) //
          .withColumn(VALUE_TYPE_COLUMN, variable.getValueType().getName()) //
          .withColumn("mime_type", variable.getMimeType())//
          .withColumn("units", variable.getUnit()) //
          .withColumn("is_repeatable", variable.isRepeatable()) //
          .withColumn("occurrence_group", variable.getOccurrenceGroup()) //
          .withColumn("index", Integer.toString(variable.getIndex())) //
          .withColumn(SQL_NAME_COLUMN, generateColumnName(variable.getName()));

      changes.add(builder.build());
      writeAttributes(variable);
      writeCategories(variable);

      super.doWriteVariable(variable);
    }

    @Override
    public void removeVariable(@NotNull Variable variable) {
      super.removeVariable(variable);
      deleteVariableMetadata(variable.getName());
    }

    private void addTableTimestampChange() {
      changes.add(UpdateDataChangeBuilder.newBuilder().tableName(VALUE_TABLES_TABLE) //
          .withColumn(UPDATED_COLUMN, new java.util.Date()) //
          .where(String
              .format("%s = '%s' AND %s = '%s'", DATASOURCE_COLUMN, valueTable.getDatasource().getName(), NAME_COLUMN,
                  valueTable.getName())).build());
    }

    //
    // Methods
    //

    private void deleteVariableMetadata(String variableName) {
      JdbcTemplate jdbcTemplate = valueTable.getDatasource().getJdbcTemplate();
      jdbcTemplate
          .update(DELETE_VARIABLE_SQL, valueTable.getDatasource().getName(), valueTable.getName(), variableName);
      jdbcTemplate.update(DELETE_VARIABLE_ATTRIBUTES_SQL, valueTable.getDatasource().getName(), valueTable.getName(),
          variableName);
      jdbcTemplate.update(DELETE_VARIABLE_CATEGORIES_SQL, valueTable.getDatasource().getName(), valueTable.getName(),
          variableName);
      jdbcTemplate
          .update(DELETE_VARIABLE_CATEGORY_ATTRIBUTES_SQL, valueTable.getDatasource().getName(), valueTable.getName(),
              variableName);
      addTableTimestampChange();
    }

    private void writeAttributes(Variable variable) {
      if(!variable.hasAttributes()) return;
      for(Attribute attribute : variable.getAttributes()) {
        InsertDataChangeBuilder builder = new InsertDataChangeBuilder();
        builder.tableName(VARIABLE_ATTRIBUTES_TABLE) //
            .withColumn(DATASOURCE_COLUMN, valueTable.getDatasource().getName()) //
            .withColumn(VALUE_TABLE_COLUMN, valueTable.getName()) //
            .withColumn(VARIABLE_COLUMN, variable.getName()) //
            .withColumn(NAME_COLUMN, attribute.getName()) //
            .withColumn(LOCALE_COLUMN, attribute.isLocalised() ? attribute.getLocale().toString() : "") //
            .withColumn(NAMESPACE_COLUMN, attribute.hasNamespace() ? attribute.getNamespace() : "") //
            .withColumn(VALUE_COLUMN, attribute.getValue().toString());
        changes.add(builder.build());
      }
    }

    private void writeCategories(Variable variable) {
      if(!variable.hasCategories()) return;
      for(Category category : variable.getCategories()) {
        InsertDataChangeBuilder builder = new InsertDataChangeBuilder();
        builder.tableName(CATEGORIES_TABLE) //
            .withColumn(DATASOURCE_COLUMN, valueTable.getDatasource().getName()) //
            .withColumn(VALUE_TABLE_COLUMN, valueTable.getName()) //
            .withColumn(VARIABLE_COLUMN, variable.getName()) //
            .withColumn(NAME_COLUMN, category.getName()) //
            .withColumn(MISSING_COLUMN, category.isMissing());
        changes.add(builder.build());
        writeCategoryAttributes(variable, category);
      }
    }

    private void writeCategoryAttributes(Variable variable, Category category) {
      if(!category.hasAttributes()) return;
      for(Attribute attribute : category.getAttributes()) {
        InsertDataChangeBuilder builder = new InsertDataChangeBuilder();
        builder.tableName(CATEGORY_ATTRIBUTES_TABLE) //
            .withColumn(DATASOURCE_COLUMN, valueTable.getDatasource().getName()) //
            .withColumn(VALUE_TABLE_COLUMN, valueTable.getName()) //
            .withColumn(VARIABLE_COLUMN, variable.getName()) //
            .withColumn(CATEGORY_COLUMN, category.getName()) //
            .withColumn(NAME_COLUMN, attribute.getName()) //
            .withColumn(LOCALE_COLUMN, attribute.isLocalised() ? attribute.getLocale().toString() : "") //
            .withColumn(NAMESPACE_COLUMN, attribute.hasNamespace() ? attribute.getNamespace() : "") //
            .withColumn(VALUE_COLUMN, attribute.getValue().toString());
        changes.add(builder.build());
      }
    }
  }

  private class JdbcValueSetWriter implements ValueTableWriter.ValueSetWriter {

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
      java.util.Date timestamp = new java.util.Date();

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