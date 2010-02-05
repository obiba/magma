package org.obiba.magma.datasource.hibernate.converter;

import java.util.List;

import org.obiba.core.service.impl.hibernate.AssociationCriteria;
import org.obiba.core.service.impl.hibernate.AssociationCriteria.Operation;
import org.obiba.magma.Attribute;
import org.obiba.magma.AttributeAware;
import org.obiba.magma.datasource.hibernate.domain.attribute.AbstractAttributeAwareEntity;
import org.obiba.magma.datasource.hibernate.domain.attribute.AttributeAwareAdapter;
import org.obiba.magma.datasource.hibernate.domain.attribute.HibernateAttribute;

public class AttributeAwareConverter implements HibernateConverter<AbstractAttributeAwareEntity, AttributeAware> {

  private static AttributeAwareConverter attributeAwareConverter;

  public static AttributeAwareConverter getInstance() {
    if(attributeAwareConverter == null) {
      attributeAwareConverter = new AttributeAwareConverter();
    }
    return attributeAwareConverter;
  }

  @Override
  public AbstractAttributeAwareEntity marshal(AttributeAware attributeAware, HibernateMarshallingContext context) {

    AbstractAttributeAwareEntity adaptable = context.getAttributeAwareEntity();
    if(!attributeAware.hasAttributes()) {
      // nothing to persist
      return adaptable;
    }

    // find it or create it
    AttributeAwareAdapter hibernateEntity = getAttributeAware(adaptable, context);
    if(hibernateEntity == null) {
      hibernateEntity = new AttributeAwareAdapter();
      hibernateEntity.setAttributeAwareEntity(adaptable);
      context.getSessionFactory().getCurrentSession().save(hibernateEntity);
    }

    addAttributes(attributeAware, hibernateEntity, context);

    return adaptable;
  }

  @Override
  public AttributeAware unmarshal(AbstractAttributeAwareEntity adaptable, HibernateMarshallingContext context) {

    AttributeAwareAdapter hibernateEntity = getAttributeAware(context.getAttributeAwareEntity(), context);
    if(hibernateEntity != null) { // has attributes
      List<HibernateAttribute> attributes = hibernateEntity.getAttributes();
      for(HibernateAttribute attribute : attributes) {
        Attribute.Builder attBuilder = Attribute.Builder.newAttribute(attribute.getName());

        if(attribute.isLocalised()) {
          attBuilder.withValue(attribute.getLocale(), attribute.getValue().toString());
        } else {
          attBuilder.withValue(attribute.getValue());
        }

        context.getAttributeAwareBuilder().addAttribute(attBuilder.build());
      }
    }

    return null;
  }

  private void addAttributes(AttributeAware attributeAware, AttributeAwareAdapter hibernateEntity, HibernateMarshallingContext context) {

    // Delete old persisted attributes (if any exist).
    for(HibernateAttribute attribute : hibernateEntity.getAttributes()) {
      context.getSessionFactory().getCurrentSession().delete(attribute);
    }

    // Persist new attributes to database.
    for(Attribute attr : attributeAware.getAttributes()) {
      HibernateAttribute hibernateAttr = new HibernateAttribute(attr.getName(), attr.getLocale(), attr.getValue());
      hibernateAttr.setAdapter(hibernateEntity);
      context.getSessionFactory().getCurrentSession().save(hibernateAttr);
    }
  }

  private AttributeAwareAdapter getAttributeAware(AbstractAttributeAwareEntity attributeAwareEntity, HibernateMarshallingContext context) {
    AssociationCriteria criteria = AssociationCriteria.create(AttributeAwareAdapter.class, context.getSessionFactory().getCurrentSession()).add("attributeAwareId", Operation.eq, attributeAwareEntity.getId()).add("attributeAwareType", Operation.eq, attributeAwareEntity.getAttributeAwareType());
    return (AttributeAwareAdapter) criteria.getCriteria().uniqueResult();
  }

}
