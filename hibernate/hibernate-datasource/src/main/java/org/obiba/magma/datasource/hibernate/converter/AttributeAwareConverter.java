package org.obiba.magma.datasource.hibernate.converter;

import org.obiba.magma.Attribute;
import org.obiba.magma.AttributeAware;
import org.obiba.magma.AttributeAwareBuilder;
import org.obiba.magma.datasource.hibernate.domain.AbstractAttributeAwareEntity;
import org.obiba.magma.datasource.hibernate.domain.AttributeState;

public class AttributeAwareConverter {

  public void addAttributes(AttributeAware attributeAware, AbstractAttributeAwareEntity hibernateEntity) {
    for(Attribute attr : attributeAware.getAttributes()) {
      AttributeState as;
      if(hibernateEntity.hasAttribute(attr.getName(), attr.getLocale())) {
        as = hibernateEntity.getAttribute(attr.getName(), attr.getLocale());
      } else {
        as = new AttributeState(attr.getName(), attr.getLocale(), attr.getValue());
        hibernateEntity.addAttribute(as);
      }
      as.setValue(attr.getValue());
    }
  }

  public void buildAttributeAware(AttributeAwareBuilder<?> builder, AbstractAttributeAwareEntity hibernateEntity) {
    for(AttributeState as : hibernateEntity.getAttributes()) {
      if(as.isLocalised()) {
        builder.addAttribute(Attribute.Builder.newAttribute(as.getName()).withValue(as.getLocale(), as.getValue().toString()).build());
      } else {
        builder.addAttribute(Attribute.Builder.newAttribute(as.getName()).withValue(as.getValue()).build());
      }
    }
  }

}
