package org.obiba.magma.support;

import java.util.Collections;
import java.util.HashSet;
import java.util.NoSuchElementException;
import java.util.Set;

import org.obiba.magma.AbstractAttributeAware;
import org.obiba.magma.Attribute;
import org.obiba.magma.Datasource;
import org.obiba.magma.Disposable;
import org.obiba.magma.Initialisable;
import org.obiba.magma.NoSuchValueTableException;
import org.obiba.magma.ValueTable;
import org.obiba.magma.ValueTableWriter;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.LinkedListMultimap;
import com.google.common.collect.ListMultimap;

public abstract class AbstractDatasource extends AbstractAttributeAware implements Datasource {

  private String name;

  private String type;

  private Set<ValueTable> valueTables = new HashSet<ValueTable>();

  private ListMultimap<String, Attribute> attributes = LinkedListMultimap.create();

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
  public Set<ValueTable> getValueTables() {
    return Collections.unmodifiableSet(valueTables);
  }

  @Override
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
    onInitialise();
    for(String valueTable : getValueTableNames()) {
      addValueTable(initialiseValueTable(valueTable));
    }
  }

  @Override
  public void dispose() {
    for(Disposable disposable : Iterables.filter(getValueTables(), Disposable.class)) {
      disposable.dispose();
    }
    onDispose();
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

  @Override
  protected ListMultimap<String, Attribute> getInstanceAttributes() {
    return attributes;
  }

  protected void onInitialise() {

  }

  protected void onDispose() {

  }

  protected abstract Set<String> getValueTableNames();

  protected abstract ValueTable initialiseValueTable(String tableName);

}
