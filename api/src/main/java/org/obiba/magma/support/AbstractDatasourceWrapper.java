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

import org.obiba.magma.Attribute;
import org.obiba.magma.Datasource;
import org.obiba.magma.NoSuchAttributeException;
import org.obiba.magma.NoSuchValueTableException;
import org.obiba.magma.Value;
import org.obiba.magma.ValueTable;
import org.obiba.magma.ValueTableWriter;

import com.google.common.base.Preconditions;

public abstract class AbstractDatasourceWrapper implements Datasource {

  private final Datasource wrapped;

  protected AbstractDatasourceWrapper(Datasource wrapped) {
    Preconditions.checkArgument(wrapped != null);
    this.wrapped = wrapped;
  }

  public Datasource getWrappedDatasource() {
    return wrapped;
  }

  public void dispose() {
    getWrappedDatasource().dispose();
  }

  public void initialise() {
    getWrappedDatasource().initialise();
  }

  public String getName() {
    return getWrappedDatasource().getName();
  }

  public String getType() {
    return getWrappedDatasource().getType();
  }

  public boolean hasValueTable(String name) {
    return getWrappedDatasource().hasValueTable(name);
  }

  public boolean hasAttributes() {
    return getWrappedDatasource().hasAttributes();
  }

  public ValueTable getValueTable(String name) throws NoSuchValueTableException {
    return getWrappedDatasource().getValueTable(name);
  }

  public Set<ValueTable> getValueTables() {
    return getWrappedDatasource().getValueTables();
  }

  public boolean canDropTable(String name) {
    return getWrappedDatasource().canDropTable(name);
  }

  public void dropTable(String name) {
    getWrappedDatasource().dropTable(name);
  }

  public boolean hasAttribute(String name) {
    return getWrappedDatasource().hasAttribute(name);
  }

  public ValueTableWriter createWriter(String tableName, String entityType) {
    return getWrappedDatasource().createWriter(tableName, entityType);
  }

  public void setAttributeValue(String name, Value value) {
    getWrappedDatasource().setAttributeValue(name, value);
  }

  public boolean hasAttribute(String namespace, String name) {
    return getWrappedDatasource().hasAttribute(namespace, name);
  }

  public Attribute getAttribute(String name) throws NoSuchAttributeException {
    return getWrappedDatasource().getAttribute(name);
  }

  public Attribute getAttribute(String namespace, String name) throws NoSuchAttributeException {
    return getWrappedDatasource().getAttribute(namespace, name);
  }

  public boolean hasAttribute(String name, Locale locale) {
    return getWrappedDatasource().hasAttribute(name, locale);
  }

  public boolean hasAttribute(String namespace, String name, Locale locale) {
    return getWrappedDatasource().hasAttribute(namespace, name, locale);
  }

  public Attribute getAttribute(String name, Locale locale) throws NoSuchAttributeException {
    return getWrappedDatasource().getAttribute(name, locale);
  }

  public Attribute getAttribute(String namespace, String name, Locale locale) throws NoSuchAttributeException {
    return getWrappedDatasource().getAttribute(namespace, name, locale);
  }

  public Value getAttributeValue(String name) throws NoSuchAttributeException {
    return getWrappedDatasource().getAttributeValue(name);
  }

  public Value getAttributeValue(String namespace, String name) throws NoSuchAttributeException {
    return getWrappedDatasource().getAttributeValue(namespace, name);
  }

  public String getAttributeStringValue(String name) throws NoSuchAttributeException {
    return getWrappedDatasource().getAttributeStringValue(name);
  }

  public String getAttributeStringValue(String namespace, String name) throws NoSuchAttributeException {
    return getWrappedDatasource().getAttributeStringValue(namespace, name);
  }

  public List<Attribute> getAttributes(String name) throws NoSuchAttributeException {
    return getWrappedDatasource().getAttributes(name);
  }

  public List<Attribute> getAttributes(String namespace, String name) throws NoSuchAttributeException {
    return getWrappedDatasource().getAttributes(namespace, name);
  }

  public List<Attribute> getNamespaceAttributes(String namespace) throws NoSuchAttributeException {
    return getWrappedDatasource().getNamespaceAttributes(namespace);
  }

  public List<Attribute> getAttributes() {
    return getWrappedDatasource().getAttributes();
  }
}
