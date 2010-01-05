package org.obiba.magma.datasource.hibernate.converter;

import org.obiba.core.service.impl.hibernate.AssociationCriteria;
import org.obiba.core.service.impl.hibernate.AssociationCriteria.Operation;
import org.obiba.magma.Category;
import org.obiba.magma.datasource.hibernate.domain.CategoryState;

public class CategoryConverter implements HibernateConverter<CategoryState, Category> {

  public static CategoryConverter getInstance() {
    return new CategoryConverter();
  }

  @Override
  public CategoryState marshal(Category category, HibernateMarshallingContext context) {
    AssociationCriteria criteria = AssociationCriteria.create(CategoryState.class, context.getSessionFactory().getCurrentSession()).add("variable.id", Operation.eq, context.getVariable().getId()).add("name", Operation.eq, category.getName());
    CategoryState catMemento = (CategoryState) criteria.getCriteria().uniqueResult();
    if(catMemento == null) {
      catMemento = new CategoryState(context.getVariable(), category.getName(), category.getCode(), category.isMissing());
    }

    context.getSessionFactory().getCurrentSession().save(catMemento);

    // attributes
    context.setAdaptable(catMemento);
    AttributeAwareConverter.getInstance().marshal(category, context);

    return null;
  }

  @Override
  public Category unmarshal(CategoryState categoryMemento, HibernateMarshallingContext context) {
    Category.Builder builder = Category.Builder.newCategory(categoryMemento.getName());
    builder.withCode(categoryMemento.getCode()).missing(categoryMemento.isMissing());

    context.setAttributeAwareBuilder(builder);
    AttributeAwareConverter.getInstance().unmarshal(categoryMemento, context);

    return builder.build();
  }

}
