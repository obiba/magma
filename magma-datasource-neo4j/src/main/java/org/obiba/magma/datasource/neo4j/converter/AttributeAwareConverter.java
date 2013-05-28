/*
 * Copyright (c) 2013 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.magma.datasource.neo4j.converter;

import org.obiba.magma.Attribute;
import org.obiba.magma.AttributeAware;
import org.obiba.magma.AttributeAwareBuilder;
import org.obiba.magma.Value;
import org.obiba.magma.datasource.neo4j.domain.AbstractAttributeAwareNode;
import org.obiba.magma.datasource.neo4j.domain.AttributeNode;
import org.obiba.magma.datasource.neo4j.domain.ValueNode;

public class AttributeAwareConverter {

  private final ValueConverter valueConverter = ValueConverter.getInstance();

  public static AttributeAwareConverter getInstance() {
    return new AttributeAwareConverter();
  }

  /**
   * Create and save AttributeNode
   */
  public void addAttributes(AttributeAware attributeAware, AbstractAttributeAwareNode node,
      Neo4jMarshallingContext context) {
    context.getNeo4jTemplate().fetch(node.getAttributes());
    for(Attribute attribute : attributeAware.getAttributes()) {
      if(node.hasAttribute(attribute.getName(), attribute.getLocale())) {
        AttributeNode attributeNode = node.getAttribute(attribute.getName(), attribute.getLocale());
        attributeNode.getValue().copyProperties(attribute.getValue());
      } else {
        ValueNode valueNode = new ValueNode(attribute.getValue());
        context.getNeo4jTemplate().save(valueNode);

        AttributeNode attributeNode = new AttributeNode();
        attributeNode.setName(attribute.getName());
        attributeNode.setNamespace(attribute.getNamespace());
        attributeNode.setLocale(attribute.getLocale());
        attributeNode.setValue(valueNode);
        attributeNode.setParent(node);
        context.getNeo4jTemplate().save(attributeNode);

        node.addAttribute(attributeNode);
      }
    }
  }

  public void buildAttributeAware(AttributeAwareBuilder<?> builder, AbstractAttributeAwareNode node,
      Neo4jMarshallingContext context) {
    context.getNeo4jTemplate().fetch(node.getAttributes());
    for(AttributeNode attributeNode : node.getAttributes()) {
      Value value = valueConverter.unmarshal(attributeNode.getValue(), context);
      builder.addAttribute(
          Attribute.Builder.newAttribute().withName(attributeNode.getName()).withNamespace(attributeNode.getNamespace())
              .withLocale(attributeNode.getLocale()).withValue(value).build());
    }
  }

}
