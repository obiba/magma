package org.obiba.magma.datasource.hibernate.converter;

import org.obiba.core.service.SortingClause;
import org.obiba.core.service.impl.hibernate.AssociationCriteria;
import org.obiba.core.service.impl.hibernate.AssociationCriteria.Operation;
import org.obiba.magma.Category;
import org.obiba.magma.Variable;
import org.obiba.magma.datasource.hibernate.domain.CategoryState;
import org.obiba.magma.datasource.hibernate.domain.VariableState;

public class VariableConverter implements HibernateConverter<VariableState, Variable> {

  public static VariableConverter getInstance() {
    return new VariableConverter();
  }

  public VariableState getStateForVariable(Variable variable, HibernateMarshallingContext context) {
    AssociationCriteria criteria = AssociationCriteria.create(VariableState.class, context.getSessionFactory().getCurrentSession()).add("valueTable", Operation.eq, context.getValueTable()).add("name", Operation.eq, variable.getName());
    return (VariableState) criteria.getCriteria().uniqueResult();
  }

  @Override
  public VariableState marshal(Variable variable, HibernateMarshallingContext context) {
    VariableState variableState = getStateForVariable(variable, context);
    if(variableState == null) {
      variableState = new VariableState(context.getValueTable(), variable);
      context.getSessionFactory().getCurrentSession().save(variableState);
    }

    // set the context and go through categories
    context.setVariable(variableState);
    for(Category category : variable.getCategories()) {
      CategoryConverter.getInstance().marshal(category, context);
    }

    // attributes
    AttributeAwareConverter.getInstance().marshal(variable, context);

    return variableState;
  }

  @Override
  public Variable unmarshal(VariableState variableState, HibernateMarshallingContext context) {
    Variable.Builder builder = Variable.Builder.newVariable(variableState.getName(), variableState.getValueType(), variableState.getEntityType());
    builder.mimeType(variableState.getMimeType()).occurrenceGroup(variableState.getOccurrenceGroup()).referencedEntityType(variableState.getReferencedEntityType()).unit(variableState.getUnit());
    if(variableState.isRepeatable()) {
      builder.repeatable();
    }

    AssociationCriteria criteria = AssociationCriteria.create(CategoryState.class, context.getSessionFactory().getCurrentSession()).add("variable.id", Operation.eq, variableState.getId()).addSortingClauses(SortingClause.create("pos"));
    for(Object obj : criteria.list()) {
      builder.addCategory(CategoryConverter.getInstance().unmarshal((CategoryState) obj, context));
    }

    // attributes
    context.setAttributeAwareBuilder(builder);
    context.setAttributeAwareEntity(variableState);
    AttributeAwareConverter.getInstance().unmarshal(variableState, context);

    return builder.build();
  }

}
