/*
 * Copyright (c) 2016 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.magma;

import java.util.List;
import java.util.Locale;

import javax.annotation.Nullable;

/**
 * Interface for things that have attributes.
 */
public interface AttributeAware {

  /**
   * Returns true if this instance has at least one {@code Attribute} (with any name).
   *
   * @return true when this instance has at least one {@code Attribute}
   */
  boolean hasAttributes();

  /**
   * Returns true if this instance has at least one {@code Attribute} with the specified name.
   *
   * @return true when this instance has at least one {@code Attribute} with the specified name; false otherwise.
   */
  boolean hasAttribute(String name);

  /**
   * Returns true if this instance has at least one {@code Attribute} in the specified {@code namespace} and,
   * optionally, with the specified {@code name}.
   *
   * @param namespace the namespace
   * @param name an optional name
   */
  boolean hasAttribute(String namespace, @Nullable String name);

  /**
   * Returns the first attribute associated with the specified name. Note that multiple instances of {@code Attribute}
   * can have the same name. This method will always return the first one.
   *
   * @param name
   * @return
   */
  Attribute getAttribute(String name) throws NoSuchAttributeException;

  /**
   * Returns the first attribute associated with the specified name in the specified namespace.
   *
   * @param namespace
   * @param name
   * @return
   */
  Attribute getAttribute(String namespace, String name) throws NoSuchAttributeException;

  boolean hasAttribute(String name, Locale locale);

  boolean hasAttribute(String namespace, String name, Locale locale);

  Attribute getAttribute(String name, Locale locale) throws NoSuchAttributeException;

  Attribute getAttribute(String namespace, String name, Locale locale) throws NoSuchAttributeException;

  /**
   * Equivalent to calling
   * <p/>
   * <pre>
   * getAttribute(name).getValue()
   *
   * <pre>
   */
  Value getAttributeValue(String name) throws NoSuchAttributeException;

  Value getAttributeValue(String namespace, String name) throws NoSuchAttributeException;

  /**
   * Equivalent to calling
   * <p/>
   * <pre>
   * getAttribute(name).getValue().toString()
   *
   * <pre>
   */
  String getAttributeStringValue(String name) throws NoSuchAttributeException;

  String getAttributeStringValue(String namespace, String name) throws NoSuchAttributeException;

  /**
   * Returns the list of attributes associated with the specified name.
   *
   * @param name the key of the attributes to return
   * @return
   * @throws NoSuchAttributeException when no attribute exists for the specified key
   */
  List<Attribute> getAttributes(String name) throws NoSuchAttributeException;

  List<Attribute> getAttributes(String namespace, String name) throws NoSuchAttributeException;

  List<Attribute> getNamespaceAttributes(String namespace) throws NoSuchAttributeException;

  List<Attribute> getAttributes();

}
