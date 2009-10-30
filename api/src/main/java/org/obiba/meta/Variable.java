/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.meta;

import java.util.Set;

import javax.xml.namespace.QName;

/**
 *
 */
public interface Variable {

  public static class Builder {

    private DefaultVariable variable = new DefaultVariable();

    public Builder(String collection, String name, ValueType type, String entityType) {
      variable.collection = collection;
      variable.name = name;
      variable.valueType = type;
      variable.entityType = entityType;
    }

    public static Builder newVariable(String collection, String name, ValueType type, String entityType) {
      return new Builder(collection, name, type, entityType);
    }

    public Variable build() {
      return variable;
    }

    public Builder repeatedWith(String name) {
      variable.repeatedVariable = name;
      return this;
    }

    public Builder repeatable() {
      variable.repeatable = true;
      return this;
    }

    public Builder addAttribute(String name, String value) {
      variable.attributes.put(name, value);
      return this;
    }

    public Builder addCategory(String name, String code) {
      variable.categories.add(new CategoryBean(variable, name, code));
      return this;
    }

    /**
     * Add an array of category labels. The resulting categories will have a null {@code code} attribute.
     * 
     * @param names
     * @return
     */
    public Builder addCategories(String... names) {
      for(String name : names) {
        variable.categories.add(new CategoryBean(variable, name, null));
      }
      return this;
    }
  }

  public QName getQName();

  public String getCollection();

  public String getName();

  public String getEntityType();

  public boolean isForEntityType(String type);

  public boolean isRepeatable();

  public String getRepeatedVariable();

  public ValueType getValueType();

  /**
   * The SI code of the measurement unit if applicable.
   * @return unit
   */
  public String getUnit();

  /**
   * The IANA mime-type of binary data if applicable.
   * @return the IANA mime-type
   */
  public String getMimeType();

  public String getAttribute(String name);

  /**
   * Used when this variable value is a pointer to another {@code IVariableEntity}. The value is considered to point to
   * the referenced entity's {@code identifier}.
   * 
   * @return the {@code entityType} that this value points to, this method returns null when the variable doesn't point
   * to another entity.
   */
  public String getReferencedEntityType();

  public Set<Category> getCategories();

}
