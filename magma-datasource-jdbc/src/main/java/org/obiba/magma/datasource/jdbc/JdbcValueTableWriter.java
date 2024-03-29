/*
 * Copyright (c) 2019 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.magma.datasource.jdbc;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import liquibase.change.Change;
import liquibase.change.ColumnConfig;
import liquibase.change.core.AddColumnChange;
import liquibase.change.core.DropColumnChange;
import liquibase.change.core.ModifyDataTypeChange;
import liquibase.change.core.UpdateDataChange;
import liquibase.exception.DateParseException;
import liquibase.structure.core.Column;
import liquibase.structure.core.Table;
import org.obiba.magma.*;
import org.obiba.magma.datasource.jdbc.JdbcDatasource.ChangeDatabaseCallback;
import org.obiba.magma.datasource.jdbc.support.AddColumnChangeBuilder;
import org.obiba.magma.datasource.jdbc.support.InsertDataChangeBuilder;
import org.obiba.magma.datasource.jdbc.support.UpdateDataChangeBuilder;
import org.obiba.magma.support.VariableHelper;
import org.obiba.magma.type.DateTimeType;
import org.obiba.magma.type.DateType;
import org.obiba.magma.type.LocaleType;
import org.obiba.magma.type.TextType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCallback;
import org.springframework.jdbc.core.support.AbstractInterruptibleBatchPreparedStatementSetter;
import org.springframework.jdbc.support.lob.DefaultLobHandler;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;

import javax.validation.constraints.NotNull;
import java.io.ByteArrayInputStream;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

class JdbcValueTableWriter implements ValueTableWriter {

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

  private boolean hasValueSets;

  private final Set<String> identifiersAtInit;

  private final List<JdbcOperation> batch = Lists.newArrayList();

  private final String ESC_CATEGORY_ATTRIBUTES_TABLE, ESC_DATASOURCE_COLUMN, ESC_VALUE_TABLE_COLUMN, ESC_VARIABLE_COLUMN, ESC_NAME_COLUMN,
      ESC_CATEGORIES_TABLE, ESC_VARIABLES_TABLE, ESC_VARIABLE_ATTRIBUTES_TABLE;

  private int batchSize;

  JdbcValueTableWriter(JdbcValueTable valueTable) {
    if (valueTable.isSQLView()) throw new MagmaRuntimeException("A SQL view cannot be written");

    this.valueTable = valueTable;
    this.hasValueSets = valueTable.getValueSetCount() > 0;
    // might be a costly call but (1) most likely this will be empty and (2) will spare hasValueSet() calls
    this.identifiersAtInit = valueTable.getVariableEntities().stream()
        .map(VariableEntity::getIdentifier).collect(Collectors.toSet());
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

    List<JdbcOperation> toSave = null;

    synchronized (valueTable) {
      if (!batch.isEmpty()) {
        toSave = Lists.newArrayList(batch);
        batch.clear();
      }
    }

    if (toSave != null) batchUpdate(toSave);

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

  private void batchUpdate(final List<JdbcOperation> operations) {
    getTransactionTemplate().execute(new TransactionCallbackWithoutResult() {
      @Override
      protected void doInTransactionWithoutResult(TransactionStatus status) {
        batchUpdateInternal(operations);
      }
    });
  }

  @SuppressWarnings({"OverlyLongMethod", "PMD.NcssMethodCount"})
  private void batchUpdateInternal(List<JdbcOperation> operations) {
    final DefaultLobHandler lobHandler = new DefaultLobHandler();

    List<VariableEntity> entities = operations.stream()
        .filter(op -> op.getSql().startsWith("INSERT "))
        .map(JdbcOperation::getVariableEntity).collect(Collectors.toList());
    valueTable.getJdbcVariableEntityProvider().addAll(entities);

    List<String> sqls = operations.stream().map(JdbcOperation::getSql).distinct().collect(Collectors.toList());

    for (String sql : sqls) {
      final List<List<Value>> batchValues = operations.stream()
          .filter(op -> sql.equals(op.getSql()))
          .map(JdbcOperation::getParameters).collect(Collectors.toList());
      int[] res = getJdbcTemplate().batchUpdate(sql, new AbstractInterruptibleBatchPreparedStatementSetter() {
        @Override
        protected boolean setValuesIfAvailable(PreparedStatement ps, int i) throws SQLException {
          int index = 1;

          if (batchValues.size() <= i) {
            return false;
          }

          for (Value value : batchValues.get(i)) {
            Object columnValue = toColumnValue(value);
            if (columnValue instanceof byte[]) {
              lobHandler.getLobCreator().setBlobAsBinaryStream(ps, index++, new ByteArrayInputStream((byte[]) columnValue),
                  ((byte[]) columnValue).length);
            } else {
              ps.setObject(index++, columnValue);
            }
          }

          return true;
        }

        private Object toColumnValue(Value value) {
          Object columnValue = null;
          if (!value.isNull()) {
            if (value.isSequence()) {
              columnValue = value.toString();
            } else {
              columnValue = value.getValue();

              // Persist some objects as strings.
              if (value.getValueType() == LocaleType.get() || value.getValueType().isGeo()) {
                columnValue = value.toString();
              } else if (value.getValueType() == DateType.get()) {
                if (columnValue instanceof java.util.Date) {
                  columnValue = new Date(((java.util.Date) columnValue).getTime());
                } else if (columnValue instanceof MagmaDate) {
                  columnValue = new Date(((MagmaDate) columnValue).asDate().getTime());
                }
              } else if (value.getValueType() == DateTimeType.get()) {
                if (columnValue instanceof java.util.Date) {
                  columnValue = new Timestamp(((java.util.Date) columnValue).getTime());
                } else if (columnValue instanceof MagmaDate) {
                  columnValue = new Timestamp(((MagmaDate) columnValue).asDate().getTime());
                }
              }
            }
          }
          return columnValue;
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

    List<Change> schemaChanges = new ArrayList<>();
    List<Change> changes = new ArrayList<>();

    @Override
    public void writeVariable(@NotNull Variable variable) {
      if (!valueTable.isForEntityType(variable.getEntityType())) {
        throw new IncompatibleEntityTypeException(variable.getName(), valueTable.getEntityType(), variable.getEntityType());
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
      schemaChanges.add(dcc);

      if (valueTable.hasUpdatedTimestampColumn()) {
        UpdateDataChange udc = new UpdateDataChange();
        udc.setTableName(valueTable.getSqlName());
        ColumnConfig col = new ColumnConfig();
        col.setName(valueTable.getUpdatedTimestampColumnName());
        try {
          col.setValueDate(formattedDate(new java.util.Date()));
        } catch (DateParseException e) {
          throw new MagmaRuntimeException("Date formatting failure", e);
        }
        udc.addColumn(col);

        changes.add(udc);
      }
    }

    @Override
    public void close() {
      getDatasource().doWithDatabase(new ChangeDatabaseCallback(schemaChanges));
      getDatasource().doWithDatabase(new ChangeDatabaseCallback(changes));
      valueTable.refreshTable();
      valueTable.refreshVariablesMap();
    }

    protected void doWriteVariable(Variable variable) {
      String columnName = getVariableSqlName(variable.getName());
      String dataType = getDataType(variable);

      if (variableExists(variable)) {
        try {
          Variable existingVariable = valueTable.getVariableValueSource(variable.getName()).getVariable();
          boolean modifiedType = !getDataType(existingVariable).equals(dataType);
          if (modifiedType) {
            if (hasValueSets)
              ensureVariableUpdateCompatibility(existingVariable, variable);
            modifyColumn(columnName, dataType);
          }
        } catch (NoSuchVariableException e) {
          // should not happen, unless the database and the java object are not sync
          modifyColumn(columnName, dataType);
        }
      } else {
        addNewColumn(variable.getName(), dataType);
      }
    }

    private String getDataType(Variable variable) {
      return variable.isRepeatable() && !valueTable.isMultilines()
          ? SqlTypes.sqlTypeFor(TextType.get(), SqlTypes.TEXT_TYPE_HINT_LARGE)
          : SqlTypes.sqlTypeFor(variable.getValueType(),
          variable.getValueType().equals(TextType.get()) ? SqlTypes.TEXT_TYPE_HINT_MEDIUM : null);
    }

    private void ensureVariableUpdateCompatibility(Variable existingVariable, Variable variable) {
      // some properties cannot be modified if there are already data stored, this affects the data schema of the SQL table
      if (!existingVariable.getValueType().equals(variable.getValueType())) {
        throw new IncompatibleValueTypeException(variable.getName(), existingVariable.getValueType(), variable.getValueType());
      }
      if (existingVariable.isRepeatable() != variable.isRepeatable()) {
        throw new IncompatibleRepeatabilityException(variable.getName(), existingVariable.isRepeatable(), variable.isRepeatable());
      }
    }

    private void modifyColumn(String columnName, String dataType) {
      ModifyDataTypeChange modifyDataTypeChange = new ModifyDataTypeChange();
      modifyDataTypeChange.setTableName(valueTable.getSqlName());
      modifyDataTypeChange.setColumnName(columnName);
      modifyDataTypeChange.setNewDataType(dataType);
      schemaChanges.add(modifyDataTypeChange);
    }

    private void addNewColumn(String variableName, String dataType) {
      String columnName = valueTable.getVariableSqlName(variableName);
      AddColumnChange addColumnChange = AddColumnChangeBuilder.newBuilder()//
          .table(valueTable.getSqlName())//
          .column(columnName, dataType).build();
      schemaChanges.add(addColumnChange);
    }

    boolean variableExists(Variable variable) {
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
      super.doWriteVariable(variable);

      boolean variableExists = variableExists(variable);
      boolean insert = true;

      if (variableExists) {
          Variable existingVariable = valueTable.getVariableValueSource(variable.getName()).getVariable();
          insert = VariableHelper.isModified(existingVariable, variable);
          if (insert)
            deleteVariableMetadata(variable.getName());
      } else {
        addTableTimestampChange();
      }

      if (insert) {
        InsertDataChangeBuilder builder = new InsertDataChangeBuilder() //
            .tableName(VARIABLES_TABLE);

        if (getDatasource().getSettings().isMultipleDatasources())
          builder.withColumn(DATASOURCE_COLUMN, valueTable.getDatasource().getName());

        builder.withColumn(VALUE_TABLE_COLUMN, valueTable.getName()) //
            .withColumn(NAME_COLUMN, variable.getName()) //
            .withColumn(VALUE_TYPE_COLUMN, variable.getValueType().getName()) //
            .withColumn("ref_entity_type", variable.getReferencedEntityType())//
            .withColumn("mime_type", variable.getMimeType())//
            .withColumn("units", variable.getUnit()) //
            .withColumn("is_repeatable", variable.isRepeatable()) //
            .withColumn("occurrence_group", variable.getOccurrenceGroup()) //
            .withColumn("index", Integer.toString(variable.getIndex())) //
            .withColumn(SQL_NAME_COLUMN, valueTable.getVariableSqlName(variable.getName()));

        changes.add(builder.build());
        writeAttributes(variable);
        writeCategories(variable);
      }
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
          ? new Object[]{getDatasource().getName(), valueTable.getName(), variableName}
          : new Object[]{valueTable.getName(), variableName};

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
      if (!variable.hasAttributes()) return;
      for (Attribute attribute : variable.getAttributes()) {
        InsertDataChangeBuilder builder = new InsertDataChangeBuilder() //
            .tableName(VARIABLE_ATTRIBUTES_TABLE);

        if (getDatasource().getSettings().isMultipleDatasources())
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
      if (!variable.hasCategories()) return;
      for (Category category : variable.getCategories()) {
        InsertDataChangeBuilder builder = new InsertDataChangeBuilder() //
            .tableName(CATEGORIES_TABLE);

        if (getDatasource().getSettings().isMultipleDatasources())
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
      if (!category.hasAttributes()) return;
      for (Attribute attribute : category.getAttributes()) {
        InsertDataChangeBuilder builder = new InsertDataChangeBuilder() //
            .tableName(CATEGORY_ATTRIBUTES_TABLE);

        if (getDatasource().getSettings().isMultipleDatasources())
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

    private final JdbcLine jdbcLine;

    private String insertSql;

    private String updateSql;

    private String whereClause;

    private boolean remove;

    private JdbcValueSetWriter(VariableEntity entity) {
      this.entity = entity;
      this.jdbcLine = new JdbcLine(entity, valueTable);
    }

    @Override
    public void writeValue(@NotNull Variable variable, Value value) {
      hasValueSets = true;
      jdbcLine.setValue(variable, value);
    }

    @Override
    public void remove() {
      remove = true;
    }

    @Override
    public void close() {
      if (remove)
        doRemove();
      else
        doInsertOrUpdate();
    }

    private void doRemove() {
      getTransactionTemplate().execute(new TransactionCallbackWithoutResult() {
        @Override
        protected void doInTransactionWithoutResult(TransactionStatus status) {
          getJdbcTemplate()
              .execute(getDeleteSql(), (PreparedStatementCallback<Integer>) ps -> {
                ps.setString(1, entity.getIdentifier());
                return ps.executeUpdate();
              });
        }
      });
      valueTable.getJdbcVariableEntityProvider().remove(entity);
    }

    private void doInsertOrUpdate() {
      final String sql = identifiersAtInit.contains(entity.getIdentifier()) ? getUpdateSql() : getInsertSql();

      List<JdbcOperation> toSave = null;

      synchronized (valueTable) {
        jdbcLine.getLines().forEach(values -> {
          values.add(TextType.get().valueOf(entity.getIdentifier()));
          batch.add(new JdbcOperation(entity, sql, values));
        });

        if (batch.size() >= batchSize) {
          toSave = Lists.newArrayList(batch);
          batch.clear();
        }
      }

      if (toSave != null) {
        batchUpdate(toSave);
      }
    }

    private String getInsertSql() {
      if (insertSql == null) {
        String colNames = Joiner.on(", ").join(getEscapedColumnNames());
        colNames = colNames.isEmpty() ? getEscapedEntityIdentifierColumnName()
            : colNames + ", " + getEscapedEntityIdentifierColumnName();
        String values = Joiner.on(", ").join(Collections.nCopies(jdbcLine.size() + 1, "?"));
        insertSql = String.format("INSERT INTO %s (%s) VALUES (%s)", getEscapedTableName(), colNames, values);
      }

      return insertSql;
    }

    private String getUpdateSql() {
      if (updateSql == null) {
        String colNames = Joiner.on(", ")
            .join(getEscapedColumnNames().stream().map(c -> String.format("%s = ?", c)).collect(Collectors.toList()));
        updateSql = String.format("UPDATE %s SET %s %s", getEscapedTableName(), colNames, getWhereClause());
      }

      return updateSql;
    }

    private String getEscapedTableName() {
      return getDatasource().escapeTableName(valueTable.getSqlName());
    }

    private List<String> getEscapedColumnNames() {
      return jdbcLine.getColumnNames().stream().map(getDatasource()::escapeColumnName).collect(Collectors.toList());
    }

    private String getEscapedEntityIdentifierColumnName() {
      return getDatasource().escapeColumnName(valueTable.getSettings().getEntityIdentifierColumn());
    }

    private String getDeleteSql() {
      return String.format("DELETE FROM %s %s", getDatasource().escapeTableName(valueTable.getSqlName()), getWhereClause());
    }

    private String getWhereClause() {
      if (whereClause == null) {
        whereClause = String.format("WHERE %s = ?", getEscapedEntityIdentifierColumnName());
      }

      return whereClause;
    }
  }

  private class JdbcOperation {

    private final VariableEntity entity;

    private final String sql;

    private final List<Value> parameters;

    private JdbcOperation(VariableEntity entity, String sql, List<Value> parameters) {
      this.entity = entity;
      this.sql = sql;
      this.parameters = parameters;
    }

    public VariableEntity getVariableEntity() {
      return entity;
    }

    public String getSql() {
      return sql;
    }

    public List<Value> getParameters() {
      return parameters;
    }
  }
}