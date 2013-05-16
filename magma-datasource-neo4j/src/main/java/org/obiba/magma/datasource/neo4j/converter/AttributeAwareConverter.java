package org.obiba.magma.datasource.neo4j.converter;

import org.obiba.magma.Attribute;
import org.obiba.magma.AttributeAware;
import org.obiba.magma.AttributeAwareBuilder;
import org.obiba.magma.ValueType;
import org.obiba.magma.datasource.neo4j.domain.AbstractAttributeAwareNode;
import org.obiba.magma.datasource.neo4j.domain.AttributeNode;

public class AttributeAwareConverter {

  public void addAttributes(AttributeAware attributeAware, AbstractAttributeAwareNode node) {
    for(Attribute attribute : attributeAware.getAttributes()) {
      if(node.hasAttribute(attribute.getName(), attribute.getLocale())) {
        AttributeNode attributeNode = node.getAttribute(attribute.getName(), attribute.getLocale());
        attributeNode.getValue().copyProperties(attribute.getValue());
      } else {
        node.addAttribute(new AttributeNode(attribute));
      }
    }
  }

  public void buildAttributeAware(AttributeAwareBuilder<?> builder, AbstractAttributeAwareNode node) {
    for(AttributeNode attributeNode : node.getAttributes()) {

      ValueType.Factory.
          builder.addAttribute(
          Attribute.Builder.newAttribute().withName(attributeNode.getName()).withNamespace(attributeNode.getNamespace())
              .withLocale(attributeNode.getLocale()).withValue().build());
    }
  }

}
