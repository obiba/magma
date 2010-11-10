package org.obiba.magma.security;

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

import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;

public class SecuredDatasource implements Datasource {

  private final Authorizer authz;

  private final Datasource datasource;

  public SecuredDatasource(Authorizer authorizer, Datasource datasource) {
    this.authz = authorizer;
    this.datasource = datasource;
  }

  @Override
  public ValueTableWriter createWriter(String tableName, String entityType) {
    return getWrappedDatasource().createWriter(tableName, entityType);
  }

  @Override
  public String getName() {
    return getWrappedDatasource().getName();
  }

  @Override
  public String getType() {
    return getWrappedDatasource().getType();
  }

  @Override
  public ValueTable getValueTable(String name) throws NoSuchValueTableException {
    ValueTable table = getWrappedDatasource().getValueTable(name);
    if(table != null && canReadTable(name) == false) throw new NoSuchValueTableException(datasource.getName(), name);
    return new SecuredValueTable(authz, table);
  }

  @Override
  public Set<ValueTable> getValueTables() {
    return ImmutableSet.copyOf(Iterables.filter(getWrappedDatasource().getValueTables(), new Predicate<ValueTable>() {

      @Override
      public boolean apply(ValueTable input) {
        return canReadTable(input.getName());
      }
    }));
  }

  @Override
  public boolean hasValueTable(String name) {
    return getWrappedDatasource().hasValueTable(name) && canReadTable(name);
  }

  @Override
  public void setAttributeValue(String name, Value value) {
    getWrappedDatasource().setAttributeValue(name, value);
  }

  @Override
  public void initialise() {
    getWrappedDatasource().initialise();
  }

  @Override
  public void dispose() {
    getWrappedDatasource().dispose();
  }

  @Override
  public Attribute getAttribute(String name) throws NoSuchAttributeException {
    return getWrappedDatasource().getAttribute(name);
  }

  @Override
  public Attribute getAttribute(String name, Locale locale) throws NoSuchAttributeException {
    return getWrappedDatasource().getAttribute(name, locale);
  }

  @Override
  public String getAttributeStringValue(String name) throws NoSuchAttributeException {
    return getWrappedDatasource().getAttributeStringValue(name);
  }

  @Override
  public Value getAttributeValue(String name) throws NoSuchAttributeException {
    return getWrappedDatasource().getAttributeValue(name);
  }

  @Override
  public List<Attribute> getAttributes(String name) throws NoSuchAttributeException {
    return getWrappedDatasource().getAttributes(name);
  }

  @Override
  public List<Attribute> getAttributes() {
    return getWrappedDatasource().getAttributes();
  }

  @Override
  public boolean hasAttribute(String name) {
    return getWrappedDatasource().hasAttribute(name);
  }

  @Override
  public boolean hasAttribute(String name, Locale locale) {
    return getWrappedDatasource().hasAttribute(name, locale);
  }

  @Override
  public boolean hasAttributes() {
    return getWrappedDatasource().hasAttributes();
  }

  protected Datasource getWrappedDatasource() {
    return this.datasource;
  }

  protected boolean canReadTable(String name) {
    return authz.isPermitted("magma:read:" + getName() + ":" + name);
  }
}
