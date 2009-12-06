package org.obiba.magma.support;

import java.util.Collections;
import java.util.HashSet;
import java.util.NoSuchElementException;
import java.util.Set;

import org.obiba.magma.Datasource;
import org.obiba.magma.DatasourceMetaData;
import org.obiba.magma.Disposable;
import org.obiba.magma.Initialisable;
import org.obiba.magma.NoSuchValueTableException;
import org.obiba.magma.ValueTable;
import org.obiba.magma.ValueTableWriter;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;

public abstract class AbstractDatasource implements Datasource {

  private String name;

  private String type;

  private DatasourceMetaData metadata;

  private Set<ValueTable> valueTables = new HashSet<ValueTable>();

  protected AbstractDatasource(String name, String type) {
    this.name = name;
    this.type = type;
  }

  @Override
  public String getName() {
    return name;
  }

  @Override
  public String getType() {
    return type;
  }

  @Override
  public DatasourceMetaData getMetaData() {
    return metadata;
  }

  @Override
  public Set<ValueTable> getValueTables() {
    return Collections.unmodifiableSet(valueTables);
  }

  public boolean hasValueTable(String name) {
    for(ValueTable vt : getValueTables()) {
      if(vt.getName().equals(name)) {
        return true;
      }
    }
    return false;
  }

  @Override
  public ValueTable getValueTable(final String name) throws NoSuchValueTableException {
    try {
      return Iterables.find(getValueTables(), new Predicate<ValueTable>() {
        @Override
        public boolean apply(ValueTable input) {
          return name.equals(input.getName());
        }
      });
    } catch(NoSuchElementException e) {
      throw new NoSuchValueTableException(name);
    }
  }

  @Override
  public void initialise() {
    metadata = readMetadata();

    for(String valueTable : getValueTableNames()) {
      addValueTable(initialiseValueTable(valueTable));
    }

  }

  @Override
  public void dispose() {
    for(Disposable disposable : Iterables.filter(getValueTables(), Disposable.class)) {
      disposable.dispose();
    }
    writeMetadata();
  }

  protected void addValueTable(ValueTable vt) {
    if(vt instanceof Initialisable) {
      ((Initialisable) vt).initialise();
    }
    valueTables.add(vt);
  }

  @Override
  public ValueTableWriter createWriter(String tableName) {
    throw new UnsupportedOperationException();
  }

  protected void writeMetadata() {

  }

  protected abstract DatasourceMetaData readMetadata();

  protected abstract Set<String> getValueTableNames();

  protected abstract ValueTable initialiseValueTable(String tableName);
}
