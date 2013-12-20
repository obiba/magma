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

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import javax.persistence.ElementCollection;
import javax.persistence.MappedSuperclass;
import javax.persistence.Transient;

import org.obiba.magma.NoSuchAttributeException;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.LinkedListMultimap;
import com.google.common.collect.Multimap;

@MappedSuperclass
public abstract class AbstractAttributeAwareEntity extends AbstractTimestampedEntity {

  private static final long serialVersionUID = 8238201229433337449L;

  @ElementCollection // always cascaded
  private List<AttributeState> attributes;

  @Transient
  private Multimap<String, AttributeState> attributeMap;

  public List<AttributeState> getAttributes() {
    return attributes == null ? (attributes = new ArrayList<>()) : attributes;
  }

  public void setAttributes(List<AttributeState> attributes) {
    this.attributes = attributes;
  }

  public AttributeState getAttribute(String name, Locale locale) {
    for(AttributeState ha : attributeMap.get(name)) {
      if(locale != null && ha.isLocalised() && locale.equals(ha.getLocale())) {
        return ha;
      }
      if(locale == null && !ha.isLocalised()) {
        return ha;
      }
    }
    throw new NoSuchAttributeException(name, getClass().getName());
  }

  public void addAttribute(AttributeState ha) {
    getAttributeMap().put(ha.getName(), ha);
    getAttributes().add(ha);
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

  public boolean hasAttribute(String name, Locale locale) {
    if(hasAttribute(name)) {
      for(AttributeState ha : getAttributeMap().get(name)) {
        if(locale == null && !ha.isLocalised()) {
          return true;
        }
        if(locale != null && ha.isLocalised() && locale.equals(ha.getLocale())) {
          return true;
        }
      }
    }
    return false;
  }

  private Multimap<String, AttributeState> getAttributeMap() {
    if(attributeMap == null) {
      attributeMap = LinkedListMultimap.create();
      for(AttributeState ha : getAttributes()) {
        attributeMap.put(ha.getName(), ha);
      }
    }
    return attributeMap;
  }

}
