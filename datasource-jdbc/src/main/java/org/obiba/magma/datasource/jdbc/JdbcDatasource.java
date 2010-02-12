package org.obiba.magma.datasource.jdbc;

import java.sql.SQLException;
import java.util.LinkedHashSet;
import java.util.Set;

import javax.sql.DataSource;

import liquibase.database.Database;
import liquibase.database.DatabaseFactory;
import liquibase.database.structure.DatabaseSnapshot;
import liquibase.database.structure.Table;
import liquibase.exception.JDBCException;

import org.obiba.magma.MagmaRuntimeException;
import org.obiba.magma.ValueTable;
import org.obiba.magma.ValueTableWriter;
import org.obiba.magma.support.AbstractDatasource;
import org.springframework.jdbc.core.JdbcTemplate;

import com.google.common.collect.ImmutableSet;

public class JdbcDatasource extends AbstractDatasource {

  private static final String TYPE = "jdbc-mart";

  private Set<String> RESERVED_NAMES = ImmutableSet.of("variables");

  private Database database;

  private DatabaseSnapshot snapshot;

  private JdbcTemplate jdbcTemplate;

  public JdbcDatasource(String name, DataSource jdbcDatasource) {
    super(name, TYPE);
    this.jdbcTemplate = new JdbcTemplate(jdbcDatasource);
  }

  @Override
  public ValueTableWriter createWriter(String tableName, String entityType) {
    throw new UnsupportedOperationException();/*
                                               * JdbcMartValueTable table; if(hasValueTable(tableName) == false) { table
                                               * = new JdbcMartValueTable(this, tableName, entityType);
                                               * addValueTable(table); } else { table = (JdbcMartValueTable)
                                               * getValueTable(tableName); } return new JdbcMartValueTableWriter(table);
                                               */
  }

  @Override
  protected void onInitialise() {
    try {
      database = DatabaseFactory.getInstance().findCorrectDatabaseImplementation(jdbcTemplate.getDataSource().getConnection());
      snapshot = database.createDatabaseSnapshot(null, null);
      /*
       * if(snapshot.getTable("variables") == null) { CreateTableChange ctc = new CreateTableChange();
       * ctc.setTableName("variables");
       * 
       * ColumnConfig cc = new ColumnConfig(); cc.setName("name"); cc.setType("VARCHAR"); ctc.addColumn(cc);
       * ctc.executeStatements(database, (List<SqlVisitor>) Collections.EMPTY_LIST); }
       */
      for(Table table : snapshot.getTables()) {
        addValueTable(new JdbcValueTable(this, table, "Participant"));
      }
    } catch(JDBCException e) {
      throw new MagmaRuntimeException(e);
    } catch(SQLException e) {
      throw new MagmaRuntimeException(e);
    }

  }

  @Override
  protected void onDispose() {
    try {
      this.database.close();
    } catch(JDBCException e) {
    }
  }

  protected Set<String> getValueTableNames() {
    Set<String> names = new LinkedHashSet<String>();
    for(Table table : snapshot.getTables()) {
      if(RESERVED_NAMES.contains(table.getName().toLowerCase()) == false) {
        names.add(table.getName());
      }
    }
    return names;
  }

  protected ValueTable initialiseValueTable(String tableName) {
    return new JdbcValueTable(this, snapshot.getTable(tableName), "Participant");
  }

  Database getDatabase() {
    return database;
  }

}
