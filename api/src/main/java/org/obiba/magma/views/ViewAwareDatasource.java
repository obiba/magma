package org.obiba.magma.views;

import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.obiba.magma.Attribute;
import org.obiba.magma.Datasource;
import org.obiba.magma.NoSuchAttributeException;
import org.obiba.magma.NoSuchValueTableException;
import org.obiba.magma.Value;
import org.obiba.magma.ValueTable;
import org.obiba.magma.ValueTableWriter;
import org.obiba.magma.support.Disposables;
import org.obiba.magma.support.Initialisables;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;

public class ViewAwareDatasource implements Datasource {
  //
  // Instance Variables
  //

  private Datasource wrappedDatasource;

  private Set<ValueTable> views;

  //
  // Constructors
  //

  public ViewAwareDatasource(Datasource datasource, Set<ValueTable> views) {
    if(views == null) {
      throw new IllegalArgumentException("Null views");
    }

    this.wrappedDatasource = datasource;

    this.views = new HashSet<ValueTable>(views);
    this.views.addAll(views);
  }

  //
  // Datasource Methods
  //

  public void initialise() {
    // Initialize the wrapped datasource.
    Initialisables.initialise(wrappedDatasource);

    // Initialise the views.
    for(ValueTable view : views) {
      if(view instanceof View) {
        ((View) view).setDatasource(this);
      }
      Initialisables.initialise(view);
    }
  }

  public void dispose() {
    // Dispose of the wrapped datasource.
    Disposables.dispose(wrappedDatasource);
    Disposables.dispose(views);
  }

  public ValueTableWriter createWriter(String tableName, String entityType) {
    for(ValueTable view : views) {
      if(view.getName().equals(tableName)) {
        throw new UnsupportedOperationException("Cannot write to a View");
      }
    }
    return wrappedDatasource.createWriter(tableName, entityType);
  }

  public String getName() {
    return wrappedDatasource.getName();
  }

  public String getType() {
    return wrappedDatasource.getType();
  }

  public ValueTable getValueTable(String name) throws NoSuchValueTableException {
    for(ValueTable view : views) {
      if(view.getName().equals(name)) {
        return view;
      }
    }
    return this.wrappedDatasource.getValueTable(name);
  }

  public Set<ValueTable> getValueTables() {
    Set<ValueTable> valueTables = new HashSet<ValueTable>();
    valueTables.addAll(getWrappedTables());
    valueTables.addAll(views);

    return valueTables;
  }

  @Override
  public boolean hasValueTable(String name) {
    for(ValueTable view : views) {
      if(view.getName().equals(name)) {
        return true;
      }
    }
    return wrappedDatasource.hasValueTable(name);
  }

  public void setAttributeValue(String name, Value value) {
    wrappedDatasource.setAttributeValue(name, value);
  }

  public Attribute getAttribute(String name) throws NoSuchAttributeException {
    return wrappedDatasource.getAttribute(name);
  }

  @Override
  public Attribute getAttribute(String name, Locale locale) throws NoSuchAttributeException {
    return wrappedDatasource.getAttribute(name, locale);
  }

  @Override
  public String getAttributeStringValue(String name) throws NoSuchAttributeException {
    return wrappedDatasource.getAttributeStringValue(name);
  }

  @Override
  public Value getAttributeValue(String name) throws NoSuchAttributeException {
    return wrappedDatasource.getAttributeValue(name);
  }

  @Override
  public List<Attribute> getAttributes(String name) throws NoSuchAttributeException {
    return wrappedDatasource.getAttributes(name);
  }

  @Override
  public List<Attribute> getAttributes() {
    return wrappedDatasource.getAttributes();
  }

  @Override
  public boolean hasAttribute(String name) {
    return wrappedDatasource.hasAttribute(name);
  }

  @Override
  public boolean hasAttribute(String name, Locale locale) {
    return wrappedDatasource.hasAttribute(name, locale);
  }

  @Override
  public boolean hasAttributes() {
    return wrappedDatasource.hasAttributes();
  }

  //
  // Methods
  //

  public Set<View> getViews() {
    return ImmutableSet.copyOf(Iterables.filter(views, View.class));
  }

  /**
   * Add or replace View.
   */
  public void addView(ValueTable table) {
    Initialisables.initialise(table);
    if(hasView(table.getName())) removeView(table.getName());
    views.add(table);
    if(table instanceof View) {
      ((View) table).setDatasource(this);
    }
    Initialisables.initialise(table);
  }

  public void removeView(String name) {
    View view = getViewByName(name);
    if(view != null) {
      views.remove(view);
      Disposables.dispose(view);
    }
  }

  public boolean hasView(String name) {
    if(getViewByName(name) != null) {
      return true;
    }
    return false;
  }

  public View getView(String name) {
    View view = getViewByName(name);
    if(view != null) {
      return view;
    }
    throw new NoSuchValueTableException(wrappedDatasource.getName(), name);
  }

  private View getViewByName(String name) {
    for(ValueTable view : views) {
      if(view.getName().equals(name) && view instanceof View) {
        return (View) view;
      }
    }
    return null;
  }

  private Set<ValueTable> getWrappedTables() {
    return wrappedDatasource.getValueTables();
  }
}
