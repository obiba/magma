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

  @Override
  public VariableState marshal(Variable variable, HibernateMarshallingContext context) {
    AssociationCriteria criteria = AssociationCriteria.create(VariableState.class, context.getSessionFactory().getCurrentSession()).add("valueTable.id", Operation.eq, context.getValueTable().getId()).add("name", Operation.eq, variable.getName());
    VariableState varMemento = (VariableState) criteria.getCriteria().uniqueResult();
    if(varMemento == null) {
      varMemento = new VariableState(context.getValueTable(), variable);
    }
    // TODO set...
    context.getSessionFactory().getCurrentSession().save(varMemento);

    // set the context and go through categories
    context.setVariable(varMemento);
    for(Category category : variable.getCategories()) {
      CategoryConverter.getInstance().marshal(category, context);
    }

    // attributes
    AttributeAwareConverter.getInstance().marshal(variable, context);

    return varMemento;
  }

  @Override
  public Variable unmarshal(VariableState variableMemento, HibernateMarshallingContext context) {
    Variable.Builder builder = Variable.Builder.newVariable(variableMemento.getName(), variableMemento.getValueType(), variableMemento.getEntityType());
    builder.mimeType(variableMemento.getMimeType()).occurrenceGroup(variableMemento.getOccurrenceGroup()).referencedEntityType(variableMemento.getReferencedEntityType()).unit(variableMemento.getUnit());
    if(variableMemento.isRepeatable()) {
      builder.repeatable();
    }

    AssociationCriteria criteria = AssociationCriteria.create(CategoryState.class, context.getSessionFactory().getCurrentSession()).add("variable.id", Operation.eq, variableMemento.getId()).addSortingClauses(SortingClause.create("pos"));
    for(Object obj : criteria.list()) {
      builder.addCategory(CategoryConverter.getInstance().unmarshal((CategoryState) obj, context));
    }

    // attributes
    context.setAttributeAwareBuilder(builder);
    AttributeAwareConverter.getInstance().unmarshal(variableMemento, context);

    return builder.build();
  }

}
