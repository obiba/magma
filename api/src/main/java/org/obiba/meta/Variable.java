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

import java.lang.reflect.Constructor;
import java.util.Locale;
import java.util.Set;

import javax.xml.namespace.QName;

/**
 *
 */
public interface Variable {

  public static class Builder {

    private VariableBean variable = new VariableBean();

    public Builder(String collection, String name, ValueType type, String entityType) {
      variable.collection = collection;
      variable.name = name;
      variable.valueType = type;
      variable.entityType = entityType;
    }

    protected Builder(Builder builder) {
      this.variable = builder.variable;
    }

    public static Builder newVariable(String collection, String name, ValueType type, String entityType) {
      return new Builder(collection, name, type, entityType);
    }

    public Variable build() {
      return variable;
    }

    public Builder occurrenceGroup(String name) {
      variable.occurrenceGroup = name;
      return this;
    }

    public Builder repeatable() {
      variable.repeatable = true;
      return this;
    }

    public Builder addAttribute(String name, String value) {
      variable.attributes.put(name, Attribute.Builder.newAttribute(name).withValue(value).build());
      return this;
    }

    public Builder addAttribute(String name, String value, Locale locale) {
      variable.attributes.put(name, Attribute.Builder.newAttribute(name).withValue(locale, value).build());
      return this;
    }

    public Builder addCategory(String name, String code) {
      variable.categories.add(new CategoryBean(variable, name, code));
      return this;
    }

    /**
     * Add an array of category labels. The resulting {@code Category} instances will have a null {@code code} value.
     * This method is useful for creating categories out of {@code enum} constants for example.
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

    /**
     * Extends this builder to perform additional building capabilities for different variable nature. The specified
     * type must extend {@code Variable.Builder} and expose a public constructor that takes a single {@code
     * Variable.Builder} parameter; the constructor should call its super class' constructor with the same signature.
     * <p/>
     * An example class
     * 
     * <pre>
     * public class BuilderExtension extends Variable.Builder {
     *   public BuilderExtension(Variable.Builder builder) {
     *     super(builder);
     *   }
     * 
     *   public BuilderExtension withExtension(String value) {
     *     addAttribute(&quot;extension&quot;, value);
     *     return this;
     *   }
     * }
     * </pre>
     * 
     * @param <T>
     * @param type the {@code Builder} type to construct
     * @return an instance of {@code T} that extends {@code Builder}
     */
    public <T extends Builder> T extend(Class<T> type) {
      try {
        Constructor<T> ctor = type.getConstructor(Variable.Builder.class);
        return ctor.newInstance(this);
      } catch(NoSuchMethodException e) {
        throw new IllegalArgumentException("Builder extension type '" + type.getName() + "' must expose a public constructor that takes a single argument of type '" + Variable.Builder.class.getName() + "'.");
      } catch(RuntimeException e) {
        throw new IllegalArgumentException("Cannot instantiate builder extension type '" + type.getName() + "'", e);
      } catch(Exception e) {
        throw new IllegalArgumentException("Cannot instantiate builder extension type '" + type.getName() + "'", e);
      }
    }
  }

  public QName getQName();

  public String getCollection();

  public String getName();

  public String getEntityType();

  public boolean isForEntityType(String type);

  public boolean isRepeatable();

  public String getOccurrenceGroup();

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

  public boolean hasAttribute(String name);

  public Attribute getAttribute(String name);

  public Attribute getAttribute(String name, Locale locale);

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
