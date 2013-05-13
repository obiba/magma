/*
 * Copyright (c) 2013 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.magma.datasource.neo4j.domain;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.neo4j.graphdb.Direction;
import org.obiba.magma.NoSuchAttributeException;
import org.springframework.data.neo4j.annotation.NodeEntity;
import org.springframework.data.neo4j.annotation.RelatedTo;

import com.google.common.collect.LinkedListMultimap;
import com.google.common.collect.Multimap;

@NodeEntity
public abstract class AbstractAttributeAwareNode extends AbstractTimestampedGraphItem {

  @RelatedTo(type = "HAS_ATTRIBUTES", direction = Direction.OUTGOING)
  private List<AttributeNode> attributes;

  @SuppressWarnings("TransientFieldInNonSerializableClass")
  private transient Multimap<String, AttributeNode> attributeMap;

  public List<AttributeNode> getAttributes() {
    return attributes == null ? (attributes = new ArrayList<AttributeNode>()) : attributes;
  }

  public void setAttributes(List<AttributeNode> attributes) {
    this.attributes = attributes;
  }

  public AttributeNode getAttribute(String name, Locale locale) {
    for(AttributeNode attribute : attributeMap.get(name)) {
      if(locale != null && attribute.isLocalised() && locale.equals(attribute.getLocale())) {
        return attribute;
      }
      if(locale == null && !attribute.isLocalised()) {
        return attribute;
      }
    }
    throw new NoSuchAttributeException(name, getClass().getName());
  }

  public void addAttribute(AttributeNode ha) {
    getAttributeMap().put(ha.getName(), ha);
    getAttributes().add(ha);
  }

  public boolean hasAttribute(String name) {
    return getAttributeMap().containsKey(name);
  }

  public boolean hasAttribute(String name, Locale locale) {
    if(hasAttribute(name)) {
      for(AttributeNode ha : getAttributeMap().get(name)) {
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

  private Multimap<String, AttributeNode> getAttributeMap() {
    if(attributeMap == null) {
      attributeMap = LinkedListMultimap.create();
      for(AttributeNode ha : getAttributes()) {
        attributeMap.put(ha.getName(), ha);
      }
    }
    return attributeMap;
  }

}
