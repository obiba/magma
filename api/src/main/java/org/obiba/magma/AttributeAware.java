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
   * @return true when this instance has at least one {@code Attribute}
   */
  public boolean hasAttributes();

  /**
   * Returns true if this instance has at least one {@code Attribute} with the specified name.
   * @return true when this instance has at least one {@code Attribute} with the specified name; false otherwise.
   */
  public boolean hasAttribute(String name);

  /**
   * Returns true if this instance has at least one {@code Attribute} in the specified {@code namespace} and,
   * optionally, with the specified {@code name}.
   * 
   * @param namespace the namespace
   * @param name an optional name
   */
  public boolean hasAttribute(String namespace, @Nullable String name);

  /**
   * Returns the first attribute associated with the specified name. Note that multiple instances of {@code Attribute}
   * can have the same name. This method will always return the first one.
   * @param name
   * @return
   */
  public Attribute getAttribute(String name) throws NoSuchAttributeException;

  /**
   * Returns the first attribute associated with the specified name in the specified namespace.
   * @param namespace
   * @param name
   * @return
   */
  public Attribute getAttribute(String namespace, String name) throws NoSuchAttributeException;

  public boolean hasAttribute(String name, Locale locale);

  public boolean hasAttribute(String namespace, String name, Locale locale);

  public Attribute getAttribute(String name, Locale locale) throws NoSuchAttributeException;

  public Attribute getAttribute(String namespace, String name, Locale locale) throws NoSuchAttributeException;

  /**
   * Equivalent to calling
   * 
   * <pre>
   * getAttribute(name).getValue()
   * 
   * <pre>
   */
  public Value getAttributeValue(String name) throws NoSuchAttributeException;

  public Value getAttributeValue(String namespace, String name) throws NoSuchAttributeException;

  /**
   * Equivalent to calling
   * 
   * <pre>
   * getAttribute(name).getValue().toString()
   * 
   * <pre>
   */
  public String getAttributeStringValue(String name) throws NoSuchAttributeException;

  public String getAttributeStringValue(String namespace, String name) throws NoSuchAttributeException;

  /**
   * Returns the list of attributes associated with the specified name.
   * @param name the key of the attributes to return
   * @return
   * @throws NoSuchAttributeException when no attribute exists for the specified key
   */
  public List<Attribute> getAttributes(String name) throws NoSuchAttributeException;

  public List<Attribute> getAttributes(String namespace, String name) throws NoSuchAttributeException;

  public List<Attribute> getNamespaceAttributes(String namespace) throws NoSuchAttributeException;

  public List<Attribute> getAttributes();

}
