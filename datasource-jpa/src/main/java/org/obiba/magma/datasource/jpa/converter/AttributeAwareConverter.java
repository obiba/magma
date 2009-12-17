package org.obiba.magma.datasource.jpa.converter;

import org.obiba.core.service.impl.hibernate.AssociationCriteria;
import org.obiba.core.service.impl.hibernate.AssociationCriteria.Operation;
import org.obiba.magma.Attribute;
import org.obiba.magma.AttributeAware;
import org.obiba.magma.NoSuchAttributeException;
import org.obiba.magma.datasource.jpa.domain.adaptable.AbstractAdaptableEntity;
import org.obiba.magma.datasource.jpa.domain.attribute.AttributeAwareAdapter;
import org.obiba.magma.datasource.jpa.domain.attribute.JPAAttribute;

public class AttributeAwareConverter implements JPAConverter<AbstractAdaptableEntity, AttributeAware> {

  public static AttributeAwareConverter getInstance() {
    return new AttributeAwareConverter();
  }

  @Override
  public AbstractAdaptableEntity marshal(AttributeAware attributeAware, JPAMarshallingContext context) {
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
  public AttributeAware unmarshal(AbstractAdaptableEntity adaptable, JPAMarshallingContext context) {
    AttributeAwareAdapter attAware = getAttributeAware(adaptable, context);
    if(attAware != null) {
      AssociationCriteria criteria = makeAttributeCriteria(attAware, context);
      for(Object obj : criteria.list()) {
        context.getAttributeAwareBuilder().addAttribute((JPAAttribute) obj);
      }
    }

    // cannot access builder :(
    return null;
  }

  private void deleteAttributes(AttributeAwareAdapter attAware, AttributeAware attributeAware, JPAMarshallingContext context) {
    AssociationCriteria criteria = makeAttributeCriteria(attAware, context);
    for(Object obj : criteria.list()) {
      JPAAttribute jpaAttr = (JPAAttribute) obj;
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

  private void addOrUpdateAttribute(AttributeAwareAdapter attAware, Attribute attr, JPAMarshallingContext context) {
    // find it or create it
    AssociationCriteria criteria = makeAttributeCriteria(attAware, context);
    criteria.add("name", Operation.eq, attr.getName()).add("locale", Operation.eq, attr.getLocale());
    JPAAttribute jpaAttr = (JPAAttribute) criteria.getCriteria().uniqueResult();
    if(jpaAttr == null) {
      jpaAttr = new JPAAttribute(attr.getName(), attr.getLocale(), attr.getValue());
      jpaAttr.setAdapter(attAware);
    } else {
      jpaAttr.setValue(attr.getValue());
    }
    context.getSessionFactory().getCurrentSession().save(jpaAttr);

  }

  private AttributeAwareAdapter getAttributeAware(AbstractAdaptableEntity adaptable, JPAMarshallingContext context) {
    AssociationCriteria criteria = AssociationCriteria.create(AttributeAwareAdapter.class, context.getSessionFactory().getCurrentSession()).add("adaptableId", Operation.eq, adaptable.getId()).add("adaptableType", Operation.eq, adaptable.getAdaptableType());
    return (AttributeAwareAdapter) criteria.getCriteria().uniqueResult();
  }

  private AssociationCriteria makeAttributeCriteria(AttributeAwareAdapter attAware, JPAMarshallingContext context) {
    return AssociationCriteria.create(JPAAttribute.class, context.getSessionFactory().getCurrentSession()).add("adapter", Operation.eq, attAware);
  }

}
