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

import org.obiba.magma.*;
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
import org.springframework.jdbc.core.support.AbstractInterruptibleBatchPreparedStatementSetter;
import org.springframework.jdbc.core.support.AbstractLobCreatingPreparedStatementCallback;
import org.springframework.jdbc.support.lob.DefaultLobHandler;
import org.springframework.jdbc.support.lob.LobCreator;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.util.Assert;

import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;

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

  static final String ENTITY_TYPE_COLUMN = "entity_type";

  static final String CREATED_COLUMN = "created";

  static final String UPDATED_COLUMN = "updated";

  private final JdbcValueTable valueTable;

  private final Multimap<String, List<Object>> batch = HashMultimap.create();

  private final String ESC_CATEGORY_ATTRIBUTES_TABLE, ESC_DATASOURCE_COLUMN, ESC_VALUE_TABLE_COLUMN, ESC_VARIABLE_COLUMN, ESC_NAME_COLUMN,
      ESC_CATEGORIES_TABLE, ESC_VARIABLES_TABLE, ESC_VARIABLE_ATTRIBUTES_TABLE;

  private int batchSize;

  JdbcValueTableWriter(JdbcValueTable valueTable) {
    if (valueTable.isSQLView()) throw new MagmaRuntimeException("A SQL view cannot be written");

    this.valueTable = valueTable;
    ESC_CATEGORY_ATTRIBUTES_TABLE = valueTable.getDatasource().escapeTableName(CATEGORY_ATTRIBUTES_TABLE);
    ESC_CATEGORIES_TABLE = valueTable.getDatasource().escapeTableName(CATEGORIES_TABLE);
    ESC_VARIABLES_TABLE = valueTable.getDatasource().escapeTableName(VARIABLES_TABLE);
    ESC_VARIABLE_ATTRIBUTES_TABLE = valueTable.getDatasource().escapeTableName(VARIABLE_ATTRIBUTES_TABLE);
    ESC_DATASOURCE_COLUMN = valueTable.getDatasource().escapeColumnName(DATASOURCE_COLUMN);
    ESC_VALUE_TABLE_COLUMN = valueTable.getDatasource().escapeColumnName(VALUE_TABLE_COLUMN);
    ESC_VARIABLE_COLUMN = valueTable.getDatasource().escapeColumnName(VARIABLE_COLUMN);
    ESC_NAME_COLUMN = valueTable.getDatasource().escapeColumnName(NAME_COLUMN);
    batchSize = valueTable.getDatasource().getSettings().getBatchSize();
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

    Multimap<String, List<Object>> toSave = null;

    synchronized(valueTable) {
      if(!batch.isEmpty()) {
        toSave = ImmutableMultimap.copyOf(batch);
        batch.clear();
      }
    }

    if(toSave != null) batchUpdate(toSave);

    getDatasource().databaseChanged();
    valueTable.tableChanged();
  }

  private JdbcTemplate getJdbcTemplate() {
    return this.valueTable.getDatasource().getJdbcTemplate();
  }

  private JdbcDatasource getDatasource() {
    return this.valueTable.getDatasource();
  }

  private TransactionTemplate getTransactionTemplate() {
    return this.valueTable.getDatasource().getTransactionTemplate();
  }

  private void batchUpdate(final Multimap<String, List<Object>> toSave) {
    getTransactionTemplate().execute(new TransactionCallbackWithoutResult() {
      @Override
      protected void doInTransactionWithoutResult(TransactionStatus status) {
        batchUpdateInternal(toSave);
      }
    });
  }

  private void batchUpdateInternal(Multimap<String, List<Object>> toSave) {
    final DefaultLobHandler lobHandler = new DefaultLobHandler();

    for(String sql : toSave.keySet()) {
      final List<List<Object>> batchValues = Lists.newArrayList(toSave.get(sql));
      int[] res = getJdbcTemplate().batchUpdate(sql, new AbstractInterruptibleBatchPreparedStatementSetter() {
        @Override
        protected boolean setValuesIfAvailable(PreparedStatement ps, int i) throws SQLException {
          int index = 1;

          if(batchValues.size() <= i) {
            return false;
          }

          for(Object value : batchValues.get(i)) {
            if(value instanceof byte[]) {
              lobHandler.getLobCreator().setBlobAsBinaryStream(ps, index++, new ByteArrayInputStream((byte[]) value),
                  ((byte[]) value).length);
            } else if(value instanceof java.util.Date) {
              ps.setDate(index++, new Date(((java.util.Date) value).getTime()));
            } else if(value instanceof MagmaDate) {
              ps.setDate(index++, new Date(((MagmaDate) value).asDate().getTime()));
            } else {
              ps.setObject(index++, value);
            }
          }

          return true;
        }
      });

      log.debug("batchUpdate modified {} rows", res.length);
    }
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
      getDatasource().doWithDatabase(new ChangeDatabaseCallback(changes));
      valueTable.refreshTable();
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

      return getDatasource().getDatabaseSnapshot()
          .get(new Column(Table.class, null, null, valueTable.getSqlName(), columnName)) != null;
    }
  }

  private class JdbcMetadataVariableWriter extends JdbcVariableWriter {

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

      InsertDataChangeBuilder builder = new InsertDataChangeBuilder() //
          .tableName(VARIABLES_TABLE);

      if(getDatasource().getSettings().isMultipleDatasources())
        builder.withColumn(DATASOURCE_COLUMN, valueTable.getDatasource().getName());

      builder.withColumn(VALUE_TABLE_COLUMN, valueTable.getName()) //
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
      String whereClause = getDatasource().getSettings().isMultipleDatasources()
          ? String.format("%s = '%s' AND %s = '%s'", ESC_DATASOURCE_COLUMN, getDatasource().getName(),
          ESC_NAME_COLUMN, valueTable.getName())
          : String.format("%s = '%s'", ESC_NAME_COLUMN, valueTable.getName());
      changes.add(UpdateDataChangeBuilder.newBuilder().tableName(VALUE_TABLES_TABLE) //
          .withColumn(UPDATED_COLUMN, new java.util.Date()) //
          .where(whereClause).build());
    }

    //
    // Methods
    //

    private void deleteVariableMetadata(String variableName) {
      JdbcTemplate jdbcTemplate = getJdbcTemplate();
      JdbcDatasourceSettings settings = getDatasource().getSettings();
      Object[] params = settings.isMultipleDatasources()
          ? new Object[] { getDatasource().getName(), valueTable.getName(), variableName }
          : new Object[] { valueTable.getName(), variableName };

      // Delete category attributes
      String sql = settings.isMultipleDatasources()
          ? String
          .format("DELETE FROM %s WHERE %s = ? AND %s = ? AND %s = ?", ESC_CATEGORY_ATTRIBUTES_TABLE, ESC_DATASOURCE_COLUMN,
              ESC_VALUE_TABLE_COLUMN, ESC_VARIABLE_COLUMN)
          : String.format("DELETE FROM %s WHERE %s = ? AND %s = ?", ESC_CATEGORY_ATTRIBUTES_TABLE, ESC_VALUE_TABLE_COLUMN,
          ESC_VARIABLE_COLUMN);
      jdbcTemplate.update(sql, params);

      // Delete categories
      sql = settings.isMultipleDatasources()
          ? String.format("DELETE FROM %s WHERE %s = ? AND %s = ? AND %s = ?", ESC_CATEGORIES_TABLE, ESC_DATASOURCE_COLUMN,
          ESC_VALUE_TABLE_COLUMN, ESC_VARIABLE_COLUMN)
          : String.format("DELETE FROM %s WHERE %s = ? AND %s = ?", ESC_CATEGORIES_TABLE, ESC_VALUE_TABLE_COLUMN, ESC_VARIABLE_COLUMN);
      jdbcTemplate.update(sql, params);

      // Delete variable attributes
      sql = settings.isMultipleDatasources()
          ? String
          .format("DELETE FROM %s WHERE %s = ? AND %s = ? AND %s = ?", ESC_VARIABLE_ATTRIBUTES_TABLE, ESC_DATASOURCE_COLUMN,
              ESC_VALUE_TABLE_COLUMN, ESC_VARIABLE_COLUMN)
          : String.format("DELETE FROM %s WHERE %s = ? AND %s = ?", ESC_VARIABLE_ATTRIBUTES_TABLE, ESC_VALUE_TABLE_COLUMN,
          ESC_VARIABLE_COLUMN);
      jdbcTemplate.update(sql, params);

      // Delete variable
      sql = settings.isMultipleDatasources()
          ? String.format("DELETE FROM %s WHERE %s = ? AND %s = ? AND %s = ?", ESC_VARIABLES_TABLE, ESC_DATASOURCE_COLUMN,
          ESC_VALUE_TABLE_COLUMN, ESC_NAME_COLUMN)
          : String.format("DELETE FROM %s WHERE %s = ? AND %s = ?", ESC_VARIABLES_TABLE, ESC_VALUE_TABLE_COLUMN, ESC_NAME_COLUMN);
      jdbcTemplate.update(sql, params);

      addTableTimestampChange();
    }

    private void writeAttributes(Variable variable) {
      if(!variable.hasAttributes()) return;
      for(Attribute attribute : variable.getAttributes()) {
        InsertDataChangeBuilder builder = new InsertDataChangeBuilder() //
            .tableName(VARIABLE_ATTRIBUTES_TABLE);

        if(getDatasource().getSettings().isMultipleDatasources())
          builder.withColumn(DATASOURCE_COLUMN, valueTable.getDatasource().getName());

        builder.withColumn(VALUE_TABLE_COLUMN, valueTable.getName()) //
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
        InsertDataChangeBuilder builder = new InsertDataChangeBuilder() //
            .tableName(CATEGORIES_TABLE);

        if(getDatasource().getSettings().isMultipleDatasources())
          builder.withColumn(DATASOURCE_COLUMN, valueTable.getDatasource().getName());

        builder.withColumn(VALUE_TABLE_COLUMN, valueTable.getName()) //
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
        InsertDataChangeBuilder builder = new InsertDataChangeBuilder() //
            .tableName(CATEGORY_ATTRIBUTES_TABLE);

        if(getDatasource().getSettings().isMultipleDatasources())
          builder.withColumn(DATASOURCE_COLUMN, valueTable.getDatasource().getName());

        builder.withColumn(VALUE_TABLE_COLUMN, valueTable.getName()) //
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

    private String insertSql;

    private String updateSql;

    private String whereClause;

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

          // Persist some objects as strings.
          if(value.getValueType() == LocaleType.get() || value.getValueType().isGeo()) {
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
      if(columnValueMap.isEmpty()) {
        getTransactionTemplate().execute(new TransactionCallbackWithoutResult() {
          @Override
          protected void doInTransactionWithoutResult(TransactionStatus status) {
            getJdbcTemplate()
                .execute(getDeleteSql(), getPreparedStatementCallback(getEntityIdentifierColumnValueMap().entrySet()));
          }
        });

        return;
      }

      final String sql = valueTable.hasValueSet(entity) ? getUpdateSql() : getInsertSql();

      if(batchSize > 1) {
        Multimap<String, List<Object>> toSave = null;

        synchronized(valueTable) {
          batch.put(sql, Lists.newArrayList(Iterables
              .concat((Iterable) columnValueMap.values(), (Iterable) getEntityIdentifierColumnValueMap().values())));

          if(batch.size() >= batchSize) {
            toSave = ImmutableMultimap.copyOf(batch);
            batch.clear();
          }
        }

        if(toSave != null) {
          batchUpdate(toSave);
        }
      } else {
        getTransactionTemplate().execute(new TransactionCallbackWithoutResult() {
          @Override
          protected void doInTransactionWithoutResult(TransactionStatus status) {
            getJdbcTemplate().execute(sql, getPreparedStatementCallback(
                Iterables.concat(columnValueMap.entrySet(), getEntityIdentifierColumnValueMap().entrySet())));
          }
        });
      }
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
            } else if(entry.getValue() instanceof java.util.Date) {
              ps.setDate(index++, new Date(((java.util.Date) entry.getValue()).getTime()));
            } else {
              ps.setObject(index++, entry.getValue());
            }
          }
        }
      };
    }

    private String getInsertSql() {
      if(insertSql == null) {
        final JdbcDatasource datasource = getDatasource();
        java.util.Date timestamp = new java.util.Date();

        if(valueTable.hasCreatedTimestampColumn()) {
          columnValueMap.put(valueTable.getCreatedTimestampColumnName(), timestamp);
        }

        if(valueTable.hasUpdatedTimestampColumn()) {
          columnValueMap.put(valueTable.getUpdatedTimestampColumnName(), timestamp);
        }

        Map<String, Object> entityIdentifierColumnValueMap = getEntityIdentifierColumnValueMap();
        String colNames = Joiner.on(", ").join(Iterables
            .transform(Iterables.concat(columnValueMap.keySet(), entityIdentifierColumnValueMap.keySet()),
                new Function<String, String>() {
                  @Nullable
                  @Override
                  public String apply(@Nullable String input) {
                    return datasource.escapeColumnName(input);
                  }
                }));
        String values = Joiner.on(", ")
            .join(Collections.nCopies(entityIdentifierColumnValueMap.size() + columnValueMap.size(), "?"));

        insertSql = String
            .format("INSERT INTO %s (%s) VALUES (%s)", datasource.escapeTableName(valueTable.getSqlName()), colNames,
                values);
      }

      return insertSql;
    }

    private String getUpdateSql() {
      if(updateSql == null) {
        if(valueTable.hasUpdatedTimestampColumn()) {
          columnValueMap.put(valueTable.getUpdatedTimestampColumnName(), new java.util.Date());
        }

        String colNames = Joiner.on(", ")
            .join(Iterables.transform(columnValueMap.keySet(), new Function<String, String>() {
              @Nullable
              @Override
              public String apply(@Nullable String input) {
                return String.format("%s = ?", getDatasource().escapeColumnName(input));
              }
            }));

        StringBuffer sql = new StringBuffer();
        sql.append(String.format("UPDATE %s SET %s %s", getDatasource().escapeTableName(valueTable.getSqlName()), colNames,
            getWhereClause()));

        updateSql = sql.toString();
      }

      return updateSql;
    }

    private String getDeleteSql() {
      return String.format("DELETE FROM %s %s", getDatasource().escapeTableName(valueTable.getSqlName()), getWhereClause());
    }

    private String getWhereClause() {
      if(whereClause == null) {
        whereClause = "WHERE " + Joiner.on(" AND ")
            .join(Iterables.transform(getEntityIdentifierColumnValueMap().keySet(), new Function<String, String>() {
              @Nullable
              @Override
              public String apply(String input) {
                return String.format("%s = ?", getDatasource().escapeColumnName(input));
              }
            }));
      }

      return whereClause;
    }

    private Map<String, Object> getEntityIdentifierColumnValueMap() {
      Map<String, Object> entityIdentifierColumnValueMap = new LinkedHashMap<>();
      String entityIdentifierColumn = valueTable.getSettings().getEntityIdentifierColumn();
      entityIdentifierColumnValueMap.put(entityIdentifierColumn, entity.getIdentifier());
      return entityIdentifierColumnValueMap;
    }
  }
}