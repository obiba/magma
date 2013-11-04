/*******************************************************************************
 * Copyright (c) 2012 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.magma.support;

import java.util.List;
import java.util.Locale;
import java.util.Set;

import javax.annotation.Nonnull;

import org.obiba.magma.Attribute;
import org.obiba.magma.Datasource;
import org.obiba.magma.NoSuchAttributeException;
import org.obiba.magma.NoSuchValueTableException;
import org.obiba.magma.Timestamps;
import org.obiba.magma.Value;
import org.obiba.magma.ValueTable;
import org.obiba.magma.ValueTableWriter;

import com.google.common.base.Preconditions;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;

public abstract class AbstractDatasourceWrapper implements Datasource {

  private final Datasource wrapped;

  @SuppressWarnings("ConstantConditions")
  protected AbstractDatasourceWrapper(@Nonnull Datasource wrapped) {
    Preconditions.checkArgument(wrapped != null, "wrapped datasource cannot be null");
    this.wrapped = wrapped;
  }

  public Datasource getWrappedDatasource() {
    return wrapped;
  }

  @Override
  public void dispose() {
    getWrappedDatasource().dispose();
  }

  @Override
  public void initialise() {
    getWrappedDatasource().initialise();
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
  public boolean hasValueTable(String name) {
    return getWrappedDatasource().hasValueTable(name);
  }

  @Override
  public boolean hasEntities(Predicate<ValueTable> predicate) {
    return Iterables.filter(getValueTables(), predicate).iterator().hasNext();
  }

  @Override
  public boolean hasAttributes() {
    return getWrappedDatasource().hasAttributes();
  }

  @Override
  public ValueTable getValueTable(String name) throws NoSuchValueTableException {
    return getWrappedDatasource().getValueTable(name);
  }

  @Override
  public Set<ValueTable> getValueTables() {
    return getWrappedDatasource().getValueTables();
  }

  @Override
  public boolean canDropTable(String name) {
    return getWrappedDatasource().canDropTable(name);
  }

  @Override
  public void dropTable(String name) {
    getWrappedDatasource().dropTable(name);
  }

  @Override
  public boolean canDrop() {
    return getWrappedDatasource().canDrop();
  }

  @Override
  public void drop() {
    getWrappedDatasource().drop();
  }

  @Nonnull
  @Override
  public Timestamps getTimestamps() {
    return getWrappedDatasource().getTimestamps();
  }

  @Override
  public boolean hasAttribute(String name) {
    return getWrappedDatasource().hasAttribute(name);
  }

  @Nonnull
  @Override
  public ValueTableWriter createWriter(@Nonnull String tableName, @Nonnull String entityType) {
    return getWrappedDatasource().createWriter(tableName, entityType);
  }

  @Override
  public void setAttributeValue(String name, Value value) {
    getWrappedDatasource().setAttributeValue(name, value);
  }

  @Override
  public boolean hasAttribute(String namespace, String name) {
    return getWrappedDatasource().hasAttribute(namespace, name);
  }

  @Override
  public Attribute getAttribute(String name) throws NoSuchAttributeException {
    return getWrappedDatasource().getAttribute(name);
  }

  @Override
  public Attribute getAttribute(String namespace, String name) throws NoSuchAttributeException {
    return getWrappedDatasource().getAttribute(namespace, name);
  }

  @Override
  public boolean hasAttribute(String name, Locale locale) {
    return getWrappedDatasource().hasAttribute(name, locale);
  }

  @Override
  public boolean hasAttribute(String namespace, String name, Locale locale) {
    return getWrappedDatasource().hasAttribute(namespace, name, locale);
  }

  @Override
  public Attribute getAttribute(String name, Locale locale) throws NoSuchAttributeException {
    return getWrappedDatasource().getAttribute(name, locale);
  }

  @Override
  public Attribute getAttribute(String namespace, String name, Locale locale) throws NoSuchAttributeException {
    return getWrappedDatasource().getAttribute(namespace, name, locale);
  }

  @Override
  public Value getAttributeValue(String name) throws NoSuchAttributeException {
    return getWrappedDatasource().getAttributeValue(name);
  }

  @Override
  public Value getAttributeValue(String namespace, String name) throws NoSuchAttributeException {
    return getWrappedDatasource().getAttributeValue(namespace, name);
  }

  @Override
  public String getAttributeStringValue(String name) throws NoSuchAttributeException {
    return getWrappedDatasource().getAttributeStringValue(name);
  }

  @Override
  public String getAttributeStringValue(String namespace, String name) throws NoSuchAttributeException {
    return getWrappedDatasource().getAttributeStringValue(namespace, name);
  }

  @Override
  public List<Attribute> getAttributes(String name) throws NoSuchAttributeException {
    return getWrappedDatasource().getAttributes(name);
  }

  @Override
  public List<Attribute> getAttributes(String namespace, String name) throws NoSuchAttributeException {
    return getWrappedDatasource().getAttributes(namespace, name);
  }

  @Override
  public List<Attribute> getNamespaceAttributes(String namespace) throws NoSuchAttributeException {
    return getWrappedDatasource().getNamespaceAttributes(namespace);
  }

  @Override
  public List<Attribute> getAttributes() {
    return getWrappedDatasource().getAttributes();
  }
}
