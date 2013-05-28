/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.magma;

import java.lang.reflect.Constructor;
import java.util.Collection;
import java.util.NoSuchElementException;
import java.util.Set;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.LinkedListMultimap;
import com.google.common.collect.ListMultimap;

/**
 * The meta-data of a {@code Value}. {@code Value} instances can be obtained for {@code ValueSet} instances. When this
 * value requires description, this is done through an instance of {@code Variable}.
 */
public interface Variable extends AttributeAware {

  /**
   * A builder for {@code Variable} instances. This uses the builder pattern for constructing {@code Variable}
   * instances.
   */
  class Builder extends AttributeAwareBuilder<Builder> {

    private VariableBean variable = new VariableBean();

    public Builder(String name, ValueType type, String entityType) {
      if(name == null) throw new IllegalArgumentException("name cannot be null");
      if(type == null) throw new IllegalArgumentException("type cannot be null");
      if(entityType == null) throw new IllegalArgumentException("entityType cannot be null");

      if(name.contains(":")) throw new IllegalArgumentException("variable name cannot contain ':'");
      if(name.isEmpty()) throw new IllegalArgumentException("variable name cannot be empty");

      variable.name = name;
      variable.valueType = type;
      variable.entityType = entityType;
    }

    protected Builder(Builder builder) {
      variable = builder.variable;
    }

    @Override
    protected ListMultimap<String, Attribute> getAttributes() {
      return variable.attributes;
    }

    @Override
    protected Builder getBuilder() {
      return this;
    }

    public static Builder newVariable(String name, ValueType type, String entityType) {
      return new Builder(name, type, entityType);
    }

    public static Builder sameAs(Variable variable, boolean sameCategories) {
      Builder b = newVariable(variable.getName(), variable.getValueType(), variable.getEntityType())
          .unit(variable.getUnit()).mimeType(variable.getMimeType())
          .referencedEntityType(variable.getReferencedEntityType());
      if(variable.isRepeatable()) {
        b.repeatable().occurrenceGroup(variable.getOccurrenceGroup());
      }
      for(Attribute a : variable.getAttributes()) {
        b.addAttribute(a);
      }
      if(sameCategories) {
        for(Category c : variable.getCategories()) {
          b.addCategory(c);
        }
      }
      return b;
    }

    public static Builder sameAs(Variable variable) {
      return sameAs(variable, true);
    }

    /**
     * Values from the provided override {@code Variable} will overwrite values in the {@code Variable} currently being
     * built.
     *
     * @param override The {@code Variable} contains values that will override values in the {@code Variable} currently
     * being built.
     */
    public Builder overrideWith(Variable override) {
      variable.name = override.getName();
      if(override.getValueType() != null) variable.valueType = override.getValueType();
      if(override.getEntityType() != null) variable.entityType = override.getEntityType();
      if(override.getMimeType() != null) variable.mimeType = override.getMimeType();
      if(override.getOccurrenceGroup() != null) variable.occurrenceGroup = override.getOccurrenceGroup();
      if(override.getUnit() != null) variable.unit = override.getUnit();
      variable.attributes = (LinkedListMultimap<String, Attribute>) overrideAttributes(getAttributes(),
          override.getAttributes());
      for(Category category : override.getCategories()) {
        overrideCategories(variable.categories, category);
      }
      return this;
    }

    private void overrideCategories(Collection<Category> categories, Category overrideCategory) {
      if(categoryWithNameExists(categories, overrideCategory.getName())) {
        Category existingCategory = getCategoryWithName(categories, overrideCategory.getName());
        Category.Builder builder = Category.Builder.sameAs(existingCategory);
        if(overrideCategory.getCode() != null) builder.withCode(overrideCategory.getCode());
        builder.missing(overrideCategory.isMissing());
        builder.clearAttributes();
        for(Attribute a : overrideAttributes(existingCategory.getAttributes(), overrideCategory.getAttributes())
            .values()) {
          builder.addAttribute(a);
        }
        categories.remove(existingCategory);
        categories.add(builder.build());
      } else {
        categories.add(overrideCategory);
      }
    }

    private static boolean categoryWithNameExists(Iterable<Category> categories, String name) {
      try {
        getCategoryWithName(categories, name);
        return true;
      } catch(NoSuchElementException e) {
        return false;
      }
    }

    private static Category getCategoryWithName(Iterable<Category> categories, final String name)
        throws NoSuchElementException {
      return Iterables.find(categories, new Predicate<Category>() {
        @Override
        public boolean apply(Category input) {
          return input != null && input.getName().equals(name);
        }
      });
    }

    @SuppressWarnings("UnusedDeclaration")
    public Builder clearAttributes() {
      getAttributes().clear();
      return this;
    }

    @SuppressWarnings("UnusedDeclaration")
    public Builder clearCategories() {
      variable.categories.clear();
      return this;
    }

    /**
     * Tests whether this {@code Builder} instance is constructing a variable with any of the the specified names.
     *
     * @param name one or more names to test
     * @return true if any of the specified names is equal to the variable's name
     */
    public boolean isName(String... name) {
      for(String aName : name) {
        if(variable.name.equals(aName)) {
          return true;
        }
      }
      return false;
    }

    public Builder name(String name) {
      if(name == null) throw new IllegalArgumentException("name cannot be null");
      variable.name = name;
      return this;
    }

    public Builder type(ValueType type) {
      if(type == null) throw new IllegalArgumentException("type cannot be null");
      variable.valueType = type;
      return this;
    }

    @SuppressWarnings("UnusedDeclaration")
    public boolean isType(ValueType type) {
      return variable.valueType == type;
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

    public Builder repeatable(boolean repeatable) {
      variable.repeatable = repeatable;
      return this;
    }

    public Builder unit(String unit) {
      // TODO: Should we parse the unit and make it's valid? Using jscience API for example.
      variable.unit = unit;
      return this;
    }

    public Builder mimeType(String mimeType) {
      variable.mimeType = mimeType;
      return this;
    }

    public Builder referencedEntityType(String entityType) {
      variable.referencedEntityType = entityType;
      return this;
    }

    public Builder addCategory(String name, String code) {
      return addCategory(name, code, null);
    }

    public Builder addCategory(String name, String code, @Nullable Iterable<Category.BuilderVisitor> visitors) {
      Category.Builder categoryBuilder = Category.Builder.newCategory(name).withCode(code);
      if(visitors != null) {
        for(Category.BuilderVisitor categoryVisitor : visitors) {
          categoryBuilder.accept(categoryVisitor);
        }
      }
      variable.categories.add(categoryBuilder.build());
      return this;
    }

    public Builder addCategory(String name, String code, boolean missing) {
      variable.categories.add(Category.Builder.newCategory(name).withCode(code).missing(missing).build());
      return this;
    }

    public Builder addCategory(Category category) {
      variable.categories.add(category);
      return this;
    }

    /**
     * Add an array of category labels. The resulting {@code Category} instances will have a null {@code code} value.
     * This method is useful for creating categories out of {@code enum} constants for example.
     *
     * @param names
     * @return this
     */
    public Builder addCategories(String... names) {
      for(String name : names) {
        variable.categories.add(Category.Builder.newCategory(name).build());
      }
      return this;
    }

    public Builder addCategories(Iterable<Category> categories) {
      for(Category category : categories) {
        variable.categories.add(category);
      }
      return this;
    }

    /**
     * Accepts a {@code BuilderVisitor} to allow it to visit this {@code Builder} instance.
     *
     * @param visitor the visitor to accept; cannot be null.
     * @return this
     */
    public Builder accept(BuilderVisitor visitor) {
      visitor.visit(this);
      return this;
    }

    /**
     * Accepts a collection of visitors and calls {@code #accept(BuilderVisitor)} on each instance.
     *
     * @param visitors the collection of visitors to accept
     * @return this
     */
    public Builder accept(Iterable<? extends BuilderVisitor> visitors) {
      for(BuilderVisitor visitor : visitors) {
        accept(visitor);
      }
      return this;
    }

    /**
     * Extends this builder to perform additional building capabilities for different variable nature. The specified
     * type must extend {@code Variable.Builder} and expose a public constructor that takes a single
     * {@code Variable.Builder} parameter; the constructor should call its super class' constructor with the same
     * signature.
     * <p/>
     * An example class
     * <p/>
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
        Constructor<T> ctor = type.getConstructor(Builder.class);
        return ctor.newInstance(this);
      } catch(NoSuchMethodException e) {
        throw new IllegalArgumentException("Builder extension type '" + type.getName() +
            "' must expose a public constructor that takes a single argument of type '" + Builder.class.getName() +
            "'.");
      } catch(RuntimeException e) {
        throw new IllegalArgumentException("Cannot instantiate builder extension type '" + type.getName() + "'", e);
      } catch(Exception e) {
        throw new IllegalArgumentException("Cannot instantiate builder extension type '" + type.getName() + "'", e);
      }
    }
  }

  /**
   * Visitor pattern for contributing to a {@code Builder} instance through composition.
   */
  interface BuilderVisitor {

    /**
     * Visit a builder instance and contribute to the variable being built.
     *
     * @param builder the instance to contribute to.
     */
    void visit(Builder builder);

  }

  /**
   * The name of the variable. A variable's name must be unique within its {@code Collection}.
   *
   * @return the name of the variable.
   */
  String getName();

  /**
   * Returns the {@code entityType} this variable is associated with.
   *
   * @return
   */
  String getEntityType();

  /**
   * Returns true when this variable is for values of the specified {@code entityType}
   *
   * @param type the type of entity to test
   * @return true when this variable is for values of the specified {@code entityType}, false otherwise.
   */
  boolean isForEntityType(String type);

  /**
   * Returns true when this variable is repeatable. A repeatable variable is one where multiple {@code Value} instances
   * may exist within a single {@code ValueSet}. A single {@code Value} within a {@code ValueSet} is referenced by an
   * {@link Occurrence} instance.
   *
   * @return true when this variable may have multiple values within a single {@code ValueSet}
   */
  boolean isRepeatable();

  /**
   * When a variable is repeatable, the repeated values are grouped together, this method returns the name of this
   * group. The name is arbitrary but must be unique within a {@code Collection}.
   *
   * @return the name of the repeating group
   */
  String getOccurrenceGroup();

  /**
   * Returns the {@code ValueType} of this variable instance. Any {@code Value} obtained for this {@code variable}
   * should be of this type.
   *
   * @return the {@code ValueType} of this variable.
   */
  ValueType getValueType();

  /**
   * The SI code of the measurement unit if applicable.
   *
   * @return unit
   */
  String getUnit();

  /**
   * The IANA mime-type of binary data if applicable.
   *
   * @return the IANA mime-type
   */
  String getMimeType();

  /**
   * Used when this variable value is a pointer to another {@code VariableEntity}. The value is considered to point to
   * the referenced entity's {@code identifier}.
   *
   * @return the {@code entityType} that this value points to, this method returns null when the variable doesn't point
   *         to another entity.
   */
  String getReferencedEntityType();

  /**
   * Returns true if this variable has at least on {@code Category}
   *
   * @return
   */
  boolean hasCategories();

  /**
   * Returns the set of categories for this {@code Variable}. This method returns null when the variable has no
   * categories. To determine if a {@code Variable} instance has categories, use the {@code #hasCategories()} method.
   *
   * @return a {@code Set} of {@code Category} instances or null if none exist
   */
  Set<Category> getCategories();

  /**
   * Returns true when {@code value} is equal to a {@code Category} marked as {@code missing} or when
   * {@code Value#isNull} returns true
   *
   * @param value the value to test
   * @return true when the value is considered {@code missing}, false otherwise.
   */
  boolean isMissingValue(Value value);

  boolean areAllCategoriesMissing();

  String getVariableReference(@Nonnull ValueTable table);

}
