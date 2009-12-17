package org.obiba.magma.datasource.jpa.converter;

import org.obiba.core.service.impl.hibernate.AssociationCriteria;
import org.obiba.core.service.impl.hibernate.AssociationCriteria.Operation;
import org.obiba.magma.Category;
import org.obiba.magma.datasource.jpa.domain.CategoryState;

public class CategoryConverter implements JPAConverter<CategoryState, Category> {

  public static CategoryConverter getInstance() {
    return new CategoryConverter();
  }

  @Override
  public CategoryState marshal(Category category, JPAMarshallingContext context) {
    AssociationCriteria criteria = AssociationCriteria.create(CategoryState.class, context.getSessionFactory().getCurrentSession()).add("variable.id", Operation.eq, context.getVariable().getId()).add("name", Operation.eq, category.getName());
    CategoryState catMemento = (CategoryState) criteria.getCriteria().uniqueResult();
    if(catMemento == null) {
      catMemento = new CategoryState(context.getVariable(), category.getName());
    }
    // TODO set...
    context.getSessionFactory().getCurrentSession().save(catMemento);

    // attributes
    context.setAdaptable(catMemento);
    AttributeAwareConverter.getInstance().marshal(category, context);

    return null;
  }

  @Override
  public Category unmarshal(CategoryState categoryMemento, JPAMarshallingContext context) {
    Category.Builder builder = Category.Builder.newCategory(categoryMemento.getName());
    builder.withCode(categoryMemento.getCode()).missing(categoryMemento.isMissing());

    // TODO attributes

    return builder.build();
  }

}
