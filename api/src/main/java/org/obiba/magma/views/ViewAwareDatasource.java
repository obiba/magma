package org.obiba.magma.views;

import java.util.Collections;
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

public class ViewAwareDatasource implements Datasource {
  //
  // Instance Variables
  //

  private String name;

  private Datasource wrappedDatasource;

  private Set<View> views;

  //
  // Constructors
  //

  public ViewAwareDatasource(String name, Datasource datasource, Set<View> views) {
    if(views == null) {
      throw new IllegalArgumentException("Null views");
    }
    if(views.isEmpty()) {
      throw new IllegalArgumentException("Empty views");
    }

    this.name = name;
    this.wrappedDatasource = datasource;

    this.views = new HashSet<View>(views);
    for(View view : views) {
      view.setDatasource(this);
    }
  }

  //
  // Datasource Methods
  //

  public void initialise() {
    // Initialize the wrapped datasource.
    wrappedDatasource.initialise();

    // Initialise the views.
    for(View view : views) {
      view.initialise();
    }
  }

  public void dispose() {
    // Dispose of the wrapped datasource.
    wrappedDatasource.dispose();
  }

  public ValueTableWriter createWriter(String tableName, String entityType) {
    return wrappedDatasource.createWriter(tableName, entityType);
  }

  public String getName() {
    return name;
  }

  public String getType() {
    return wrappedDatasource.getType();
  }

  public ValueTable getValueTable(String name) throws NoSuchValueTableException {
    for(View view : views) {
      if(view.getName().equals(name)) {
        return view;
      }
    }
    return wrappedDatasource.getValueTable(name);
  }

  public Set<ValueTable> getValueTables() {
    Set<ValueTable> valueTables = new HashSet<ValueTable>();
    valueTables.addAll(wrappedDatasource.getValueTables());
    valueTables.addAll(views);

    return valueTables;
  }

  @Override
  public boolean hasValueTable(String name) {
    for(View view : views) {
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
    return Collections.unmodifiableSet(views);
  }

}
