package org.obiba.magma.views;

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
import com.google.common.collect.Sets;

public class ViewAwareDatasource implements Datasource {

  private final Datasource wrappedDatasource;

  private final Set<View> views;

  public ViewAwareDatasource(Datasource datasource, Set<View> views) {
    if(datasource == null) throw new IllegalArgumentException("datasource cannot be null");
    if(views == null) throw new IllegalArgumentException("views cannot be null");

    this.wrappedDatasource = datasource;
    this.views = Sets.newHashSet(views);
  }

  public void initialise() {
    Initialisables.initialise(wrappedDatasource);

    // Initialise the views.
    for(View view : views) {
      view.setDatasource(this);
      Initialisables.initialise(view);
    }
  }

  public void dispose() {
    Disposables.dispose(wrappedDatasource, views);
  }

  public ValueTableWriter createWriter(String tableName, String entityType) {
    if(hasView(tableName)) {
      throw new UnsupportedOperationException("Cannot write to a View");
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
    return hasView(name) ? this.getView(name) : this.wrappedDatasource.getValueTable(name);
  }

  public Set<ValueTable> getValueTables() {
    return Sets.union(getWrappedTables(), views);
  }

  @Override
  public boolean hasValueTable(String name) {
    return hasView(name) || wrappedDatasource.hasValueTable(name);
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

  public Set<View> getViews() {
    return ImmutableSet.copyOf(views);
  }

  /**
   * Add or replace View.
   */
  public void addView(View view) {
    if(wrappedDatasource.hasValueTable(view.getName())) {
      throw new IllegalArgumentException("can't add view to datasource: a table with this name '" + view.getName() + "' already exists");
    }

    if(hasView(view.getName())) {
      view.setCreated(getView(view.getName()).getTimestamps().getCreated());
      removeView(view.getName());
    }

    Initialisables.initialise(view);
    views.add(view);
    view.setDatasource(this);
  }

  public void removeView(String name) {
    View view = getViewByName(name);
    if(view != null) {
      views.remove(view);
      Disposables.dispose(view);
    }
  }

  public boolean hasView(String name) {
    return getViewByName(name) != null;
  }

  public View getView(String name) {
    View view = getViewByName(name);
    if(view != null) {
      return view;
    }
    throw new NoSuchValueTableException(wrappedDatasource.getName(), name);
  }

  private View getViewByName(String name) {
    for(View view : views) {
      if(view.getName().equals(name)) {
        return view;
      }
    }
    return null;
  }

  private Set<ValueTable> getWrappedTables() {
    return wrappedDatasource.getValueTables();
  }
}
