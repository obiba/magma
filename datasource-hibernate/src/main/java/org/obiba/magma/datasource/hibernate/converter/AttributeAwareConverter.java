package org.obiba.magma.datasource.hibernate.converter;

import java.util.List;

import org.obiba.core.service.impl.hibernate.AssociationCriteria;
import org.obiba.core.service.impl.hibernate.AssociationCriteria.Operation;
import org.obiba.magma.Attribute;
import org.obiba.magma.AttributeAware;
import org.obiba.magma.NoSuchAttributeException;
import org.obiba.magma.datasource.hibernate.domain.adaptable.AbstractAdaptableEntity;
import org.obiba.magma.datasource.hibernate.domain.attribute.AttributeAwareAdapter;
import org.obiba.magma.datasource.hibernate.domain.attribute.HibernateAttribute;

public class AttributeAwareConverter implements HibernateConverter<AbstractAdaptableEntity, AttributeAware> {

  public static AttributeAwareConverter getInstance() {
    return new AttributeAwareConverter();
  }

  @Override
  public AbstractAdaptableEntity marshal(AttributeAware attributeAware, HibernateMarshallingContext context) {
    AbstractAdaptableEntity adaptable = context.getAdaptable();
    // find it or create it
    AttributeAwareAdapter attAware = getAttributeAware(adaptable, context);
    if(attAware == null) {
      if(!attributeAware.hasAttributes()) {
        // nothing to persist
        return adaptable;
      } else {
        attAware = new AttributeAwareAdapter();
        attAware.setAdaptable(adaptable);
        context.getSessionFactory().getCurrentSession().save(attAware);
      }
    }

    // any attributes to delete ?
    deleteAttributes(attAware, attributeAware, context);

    // add or update attributes
    for(Attribute attr : attributeAware.getAttributes()) {
      addOrUpdateAttribute(attAware, attr, context);
    }

    return adaptable;
  }

  @Override
  public AttributeAware unmarshal(AbstractAdaptableEntity adaptable, HibernateMarshallingContext context) {

    AttributeAwareAdapter attributeAware = getAttributeAware(context.getAdaptable(), context);
    List<HibernateAttribute> attributes = attributeAware.getAttributes();
    for(HibernateAttribute attribute : attributes) {
      Attribute.Builder attBuilder = Attribute.Builder.newAttribute(attribute.getName());

      if(attribute.isLocalised()) {
        attBuilder.withValue(attribute.getLocale(), attribute.getValue().toString());
      } else {
        attBuilder.withValue(attribute.getValue());
      }

      context.getAttributeAwareBuilder().addAttribute(attBuilder.build());
    }

    return null;
  }

  private void deleteAttributes(AttributeAwareAdapter attAware, AttributeAware attributeAware, HibernateMarshallingContext context) {
    AssociationCriteria criteria = makeAttributeCriteria(attAware, context);
    for(Object obj : criteria.list()) {
      HibernateAttribute jpaAttr = (HibernateAttribute) obj;
      boolean toDelete = false;
      if(!attributeAware.hasAttributes() || !attributeAware.hasAttribute(jpaAttr.getName())) {
        toDelete = true;
      } else {
        // TODO missing a method hasAttribute(name, locale)
        try {
          attributeAware.getAttribute(jpaAttr.getName(), jpaAttr.getLocale());
        } catch(NoSuchAttributeException e) {
          toDelete = true;
        }
      }

      if(!attributeAware.hasAttributes()) {
        // TODO make sure AttributeAwareAdapter is removed
      }

      context.getSessionFactory().getCurrentSession().delete(jpaAttr);
    }

  }

  private void addOrUpdateAttribute(AttributeAwareAdapter attAware, Attribute attr, HibernateMarshallingContext context) {
    // find it or create it
    AssociationCriteria criteria = makeAttributeCriteria(attAware, context);
    criteria.add("name", Operation.eq, attr.getName()).add("locale", Operation.eq, attr.getLocale());
    HibernateAttribute jpaAttr = (HibernateAttribute) criteria.getCriteria().uniqueResult();
    if(jpaAttr == null) {
      jpaAttr = new HibernateAttribute(attr.getName(), attr.getLocale(), attr.getValue());
      jpaAttr.setAdapter(attAware);
    } else {
      jpaAttr.setValue(attr.getValue());
    }
    context.getSessionFactory().getCurrentSession().save(jpaAttr);

  }

  private AttributeAwareAdapter getAttributeAware(AbstractAdaptableEntity adaptable, HibernateMarshallingContext context) {
    AssociationCriteria criteria = AssociationCriteria.create(AttributeAwareAdapter.class, context.getSessionFactory().getCurrentSession()).add("adaptableId", Operation.eq, adaptable.getId()).add("adaptableType", Operation.eq, adaptable.getAdaptableType());
    return (AttributeAwareAdapter) criteria.getCriteria().uniqueResult();
  }

  private AssociationCriteria makeAttributeCriteria(AttributeAwareAdapter attAware, HibernateMarshallingContext context) {
    return AssociationCriteria.create(HibernateAttribute.class, context.getSessionFactory().getCurrentSession()).add("adapter", Operation.eq, attAware);
  }

}
