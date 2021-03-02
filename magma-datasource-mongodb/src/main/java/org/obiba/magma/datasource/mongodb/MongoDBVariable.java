/*
 * Copyright (c) 2019 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.magma.datasource.mongodb;

import java.util.List;
import java.util.Locale;
import java.util.Set;

import javax.annotation.Nullable;
import javax.validation.constraints.NotNull;

import org.obiba.magma.Attribute;
import org.obiba.magma.Category;
import org.obiba.magma.NoSuchAttributeException;
import org.obiba.magma.Value;
import org.obiba.magma.ValueTable;
import org.obiba.magma.ValueType;
import org.obiba.magma.Variable;

import com.google.common.base.Objects;

public class MongoDBVariable implements Variable {

  private final String id;

  private final Variable variable;

  public MongoDBVariable(Variable variable, String id) {
    this.variable = variable;
    this.id = id;
  }

  public String getId() {
    return id;
  }

  @Override
  public boolean hasAttributes() {
    return variable.hasAttributes();
  }

  @Override
  public boolean hasAttribute(String name) {
    return variable.hasAttribute(name);
  }

  @Override
  public boolean hasAttribute(String namespace, @Nullable String name) {
    return variable.hasAttribute(namespace, name);
  }

  @Override
  public Attribute getAttribute(String name) throws NoSuchAttributeException {
    return variable.getAttribute(name);
  }

  @Override
  public String getName() {
    return variable.getName();
  }

  @Override
  public String getEntityType() {
    return variable.getEntityType();
  }

  @Override
  public Attribute getAttribute(String namespace, String name) throws NoSuchAttributeException {
    return variable.getAttribute(namespace, name);
  }

  @Override
  public boolean hasAttribute(String name, Locale locale) {
    return variable.hasAttribute(name, locale);
  }

  @Override
  public boolean hasAttribute(String namespace, String name, Locale locale) {
    return variable.hasAttribute(namespace, name, locale);
  }

  @Override
  public Attribute getAttribute(String name, Locale locale) throws NoSuchAttributeException {
    return variable.getAttribute(name, locale);
  }

  @Override
  public boolean isForEntityType(String type) {
    return variable.isForEntityType(type);
  }

  @Override
  public Attribute getAttribute(String namespace, String name, Locale locale) throws NoSuchAttributeException {
    return variable.getAttribute(namespace, name, locale);
  }

  @Override
  public Value getAttributeValue(String name) throws NoSuchAttributeException {
    return variable.getAttributeValue(name);
  }

  @Override
  public Value getAttributeValue(String namespace, String name) throws NoSuchAttributeException {
    return variable.getAttributeValue(namespace, name);
  }

  @Override
  public boolean isRepeatable() {
    return variable.isRepeatable();
  }

  @Override
  public String getAttributeStringValue(String name) throws NoSuchAttributeException {
    return variable.getAttributeStringValue(name);
  }

  @Override
  public String getAttributeStringValue(String namespace, String name) throws NoSuchAttributeException {
    return variable.getAttributeStringValue(namespace, name);
  }

  @Override
  public String getOccurrenceGroup() {
    return variable.getOccurrenceGroup();
  }

  @Override
  public List<Attribute> getAttributes(String name) throws NoSuchAttributeException {
    return variable.getAttributes(name);
  }

  @Override
  public ValueType getValueType() {
    return variable.getValueType();
  }

  @Override
  public List<Attribute> getAttributes(String namespace, String name) throws NoSuchAttributeException {
    return variable.getAttributes(namespace, name);
  }

  @Override
  public String getUnit() {
    return variable.getUnit();
  }

  @Override
  public List<Attribute> getNamespaceAttributes(String namespace) throws NoSuchAttributeException {
    return variable.getNamespaceAttributes(namespace);
  }

  @Override
  public String getMimeType() {
    return variable.getMimeType();
  }

  @Override
  public List<Attribute> getAttributes() {
    return variable.getAttributes();
  }

  @Override
  public String getReferencedEntityType() {
    return variable.getReferencedEntityType();
  }

  @Override
  public int getIndex() {
    return variable.getIndex();
  }

  @Override
  public boolean hasCategories() {
    return variable.hasCategories();
  }

  @Nullable
  @Override
  public Category getCategory(String categoryName) {
    return variable.getCategory(categoryName);
  }

  @Override
  public Set<Category> getCategories() {
    return variable.getCategories();
  }

  @Override
  public boolean isMissingValue(Value value) {
    return variable.isMissingValue(value);
  }

  @Override
  public boolean areAllCategoriesMissing() {
    return variable.areAllCategoriesMissing();
  }

  @Override
  public String getVariableReference(@NotNull ValueTable table) {
    return variable.getVariableReference(table);
  }

  @Override
  public String toString() {
    return "MongoDBVariable{" +
        "id='" + id + '\'' +
        ", variable=" + variable +
        '}';
  }
}
