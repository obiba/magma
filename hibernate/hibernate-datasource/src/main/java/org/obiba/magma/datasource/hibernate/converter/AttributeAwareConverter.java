package org.obiba.magma.datasource.hibernate.converter;

import org.obiba.magma.Attribute;
import org.obiba.magma.AttributeAware;
import org.obiba.magma.AttributeAwareBuilder;
import org.obiba.magma.datasource.hibernate.domain.AbstractAttributeAwareEntity;
import org.obiba.magma.datasource.hibernate.domain.AttributeState;

public class AttributeAwareConverter {

  public void addAttributes(AttributeAware attributeAware, AbstractAttributeAwareEntity hibernateEntity) {
    for(Attribute attr : attributeAware.getAttributes()) {
      AttributeState ha;
      if(hibernateEntity.hasAttribute(attr.getName(), attr.getLocale())) {
        ha = hibernateEntity.getAttribute(attr.getName(), attr.getLocale());
      } else {
        ha = new AttributeState(attr.getName(), attr.getLocale(), attr.getValue());
        hibernateEntity.addAttribute(ha);
      }
      ha.setValue(attr.getValue());
    }
  }

  public void buildAttributeAware(AttributeAwareBuilder<?> builder, AbstractAttributeAwareEntity hibernateEntity) {
    for(AttributeState ha : hibernateEntity.getAttributes()) {
      if(ha.getLocale() == null) {
        builder.addAttribute(Attribute.Builder.newAttribute(ha.getName()).withValue(ha.getValue()).build());
      } else {
        builder.addAttribute(Attribute.Builder.newAttribute(ha.getName()).withValue(ha.getLocale(), ha.getValue().toString()).build());
      }
    }
  }

}
