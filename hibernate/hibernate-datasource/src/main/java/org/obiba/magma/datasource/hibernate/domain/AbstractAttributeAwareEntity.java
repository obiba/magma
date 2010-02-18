package org.obiba.magma.datasource.hibernate.domain;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import javax.persistence.MappedSuperclass;
import javax.persistence.Transient;

import org.hibernate.annotations.CollectionOfElements;
import org.obiba.core.domain.AbstractEntity;
import org.obiba.magma.NoSuchAttributeException;

import com.google.common.collect.LinkedListMultimap;
import com.google.common.collect.Multimap;

@SuppressWarnings("serial")
@MappedSuperclass
public abstract class AbstractAttributeAwareEntity extends AbstractEntity {

  @CollectionOfElements
  private List<AttributeState> attributes;

  @Transient
  private Multimap<String, AttributeState> attributeMap;

  public List<AttributeState> getAttributes() {
    return attributes != null ? attributes : (attributes = new ArrayList<AttributeState>());
  }

  public void setAttributes(List<AttributeState> attributes) {
    this.attributes = attributes;
  }

  public AttributeState getAttribute(String name, Locale locale) {
    for(AttributeState ha : attributeMap.get(name)) {
      if(locale != null && ha.isLocalised() && locale.equals(ha.getLocale())) {
        return ha;
      } else if(locale == null && ha.isLocalised() == false) {
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

  public boolean hasAttribute(String name, Locale locale) {
    if(hasAttribute(name)) {
      for(AttributeState ha : getAttributeMap().get(name)) {
        if(locale == null && ha.isLocalised() == false) {
          return true;
        }
        if(locale != null && ha.isLocalised() && ha.getLocale().equals(locale)) {
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
