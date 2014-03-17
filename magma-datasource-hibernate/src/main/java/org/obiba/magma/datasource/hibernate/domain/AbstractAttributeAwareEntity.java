/*
 * Copyright (c) 2011 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.magma.datasource.hibernate.domain;

import java.util.List;
import java.util.Locale;

import javax.annotation.Nullable;
import javax.persistence.MappedSuperclass;
import javax.persistence.Transient;

import org.obiba.magma.NoSuchAttributeException;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.LinkedListMultimap;
import com.google.common.collect.Multimap;

@MappedSuperclass
public abstract class AbstractAttributeAwareEntity extends AbstractTimestampedEntity {

  private static final long serialVersionUID = 8238201229433337449L;

  @Transient
  private Multimap<String, AttributeState> attributeMap;

  public abstract List<AttributeState> getAttributes();

  public abstract void setAttributes(List<AttributeState> attributes);

  public AttributeState getAttribute(String name, @Nullable String namespace, @Nullable Locale locale) {
    if(hasAttribute(name)) {
      for(AttributeState attribute : getAttributeMap().get(name)) {
        if(namespace == null && !attribute.hasNamespace() ||
            namespace != null && attribute.hasNamespace() && namespace.equals(attribute.getNamespace())) {
          if(locale != null && attribute.isLocalised() && locale.equals(attribute.getLocale())) {
            return attribute;
          }
          if(locale == null && !attribute.isLocalised()) {
            return attribute;
          }
        }
      }
    }
    throw new NoSuchAttributeException(name, getClass().getName());
  }

  public void addAttribute(AttributeState attribute) {
    getAttributeMap().put(attribute.getName(), attribute);
    getAttributes().add(attribute);
  }

  public boolean hasAttribute(String name) {
    return getAttributeMap().containsKey(name);
  }

  public void removeAttribute(AttributeState attribute) {
    getAttributeMap().remove(attribute.getName(), attribute);
    getAttributes().remove(attribute);
    attribute.setParent(null);
  }

  public void removeAllAttributes() {
    for(AttributeState attribute : ImmutableList.copyOf(getAttributes())) {
      removeAttribute(attribute);
    }
  }

  public boolean hasAttribute(String name, @Nullable String namespace, @Nullable Locale locale) {
    if(hasAttribute(name)) {
      for(AttributeState attribute : getAttributeMap().get(name)) {
        if(namespace == null && !attribute.hasNamespace() ||
            namespace != null && attribute.hasNamespace() && namespace.equals(attribute.getNamespace())) {
          if(locale == null && !attribute.isLocalised()) {
            return true;
          }
          if(locale != null && attribute.isLocalised() && locale.equals(attribute.getLocale())) {
            return true;
          }
        }
      }
    }
    return false;
  }

  private Multimap<String, AttributeState> getAttributeMap() {
    if(attributeMap == null) {
      attributeMap = LinkedListMultimap.create();
      for(AttributeState attribute : getAttributes()) {
        attributeMap.put(attribute.getName(), attribute);
      }
    }
    return attributeMap;
  }

}
