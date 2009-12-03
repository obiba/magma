package org.obiba.magma.support;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.obiba.magma.Datasource;
import org.obiba.magma.Initialisable;
import org.obiba.magma.ValueTable;

import com.google.common.collect.Iterables;

public abstract class AbstractDatasource implements Datasource {

  private String name;

  private String type;

  private Set<ValueTable> valueTables = new HashSet<ValueTable>();

  @Override
  public String getName() {
    return name;
  }

  @Override
  public String getType() {
    return type;
  }

  @Override
  public Set<ValueTable> getValueTables() {
    return Collections.unmodifiableSet(valueTables);
  }

  @Override
  public ValueTable getValueTable(String name) {
    return null;
  }

  @Override
  public void initialise() {

    for(String valueTable : getValueTableNames()) {
      valueTables.add(initialiseValueTable(valueTable));
    }

    for(Initialisable initialisable : Iterables.filter(getValueTables(), Initialisable.class)) {
      initialisable.initialise();
    }
  }

  protected abstract Set<String> getValueTableNames();

  protected abstract ValueTable initialiseValueTable(String tableName);
}
