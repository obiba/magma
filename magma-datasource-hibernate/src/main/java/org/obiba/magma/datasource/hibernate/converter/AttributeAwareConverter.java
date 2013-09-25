package org.obiba.magma.datasource.hibernate.converter;

import org.obiba.magma.Attribute;
import org.obiba.magma.AttributeAware;
import org.obiba.magma.AttributeAwareBuilder;
import org.obiba.magma.Attributes;
import org.obiba.magma.datasource.hibernate.domain.AbstractAttributeAwareEntity;
import org.obiba.magma.datasource.hibernate.domain.AttributeState;

public class AttributeAwareConverter {

  public void addAttributes(AttributeAware attributeAware, AbstractAttributeAwareEntity hibernateEntity) {
    for(Attribute attr : attributeAware.getAttributes()) {
      AttributeState as;
      if(hibernateEntity.hasAttribute(attr.getName(), attr.getLocale())) {
        as = hibernateEntity.getAttribute(attr.getName(), attr.getLocale());
      } else {
        as = new AttributeState(attr.getName(), attr.getNamespace(), attr.getLocale(), attr.getValue());
        hibernateEntity.addAttribute(as);
      }
      as.setValue(attr.getValue());
    }
  }

  public void setAttributes(AttributeAware attributeAware, AbstractAttributeAwareEntity hibernateEntity) {
    hibernateEntity.removeAllAttributes();
    addAttributes(attributeAware, hibernateEntity);
  }

  public void buildAttributeAware(AttributeAwareBuilder<?> builder, AbstractAttributeAwareEntity hibernateEntity) {
    for(AttributeState as : hibernateEntity.getAttributes()) {
      builder.addAttribute(Attributes.copyOf(as));
    }
  }

}
