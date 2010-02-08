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
    CategoryState categoryState = (CategoryState) criteria.getCriteria().uniqueResult();
    if(categoryState == null) {
      categoryState = new CategoryState(context.getVariable(), category.getName(), category.getCode(), category.isMissing());
      context.getSessionFactory().getCurrentSession().save(categoryState);
    }

    // attributes
    context.setAttributeAwareEntity(categoryState);
    AttributeAwareConverter.getInstance().marshal(category, context);

    return categoryState;
  }

  @Override
  public Category unmarshal(CategoryState categoryState, HibernateMarshallingContext context) {
    Category.Builder builder = Category.Builder.newCategory(categoryState.getName());
    builder.withCode(categoryState.getCode()).missing(categoryState.isMissing());

    context.setAttributeAwareBuilder(builder);
    context.setAttributeAwareEntity(categoryState);
    AttributeAwareConverter.getInstance().unmarshal(categoryState, context);

    return builder.build();
  }

}
