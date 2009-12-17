package org.obiba.magma.datasource.jpa;

import java.util.LinkedHashSet;
import java.util.Set;

import org.hibernate.SessionFactory;
import org.obiba.core.service.SortingClause;
import org.obiba.core.service.impl.hibernate.AssociationCriteria;
import org.obiba.core.service.impl.hibernate.AssociationCriteria.Operation;
import org.obiba.magma.Value;
import org.obiba.magma.ValueSet;
import org.obiba.magma.ValueType;
import org.obiba.magma.Variable;
import org.obiba.magma.VariableValueSource;
import org.obiba.magma.VariableValueSourceFactory;
import org.obiba.magma.datasource.jpa.converter.JPAMarshallingContext;
import org.obiba.magma.datasource.jpa.converter.VariableConverter;
import org.obiba.magma.datasource.jpa.domain.ValueSetValue;
import org.obiba.magma.datasource.jpa.domain.ValueTableState;
import org.obiba.magma.datasource.jpa.domain.VariableState;

public class JPAVariableValueSourceFactory implements VariableValueSourceFactory {

  private ValueTableState valueTableMemento;

  private SessionFactory sessionFactory;

  public JPAVariableValueSourceFactory(ValueTableState valueTableMemento) {
    super();
    this.valueTableMemento = valueTableMemento;
  }

  public void setSessionFactory(SessionFactory sessionFactory) {
    this.sessionFactory = sessionFactory;
  }

  @Override
  public Set<VariableValueSource> createSources() {
    Set<VariableValueSource> sources = new LinkedHashSet<VariableValueSource>();
    AssociationCriteria criteria = AssociationCriteria.create(VariableState.class, sessionFactory.getCurrentSession()).add("valueTable.id", Operation.eq, valueTableMemento.getId()).addSortingClauses(SortingClause.create("pos"));
    for(Object obj : criteria.list()) {
      JPAVariableValueSource source = new JPAVariableValueSource((VariableState) obj);
      sources.add(source);
    }
    return sources;
  }

  private class JPAVariableValueSource implements VariableValueSource {

    private VariableState variableMemento;

    private Variable variable;

    public JPAVariableValueSource(VariableState memento) {
      super();
      this.variableMemento = memento;
    }

    @Override
    public Variable getVariable() {
      if(variable == null) {
        variable = VariableConverter.getInstance().unmarshal(variableMemento, JPAMarshallingContext.create(sessionFactory, valueTableMemento));
      }

      return variable;
    }

    @Override
    public Value getValue(ValueSet valueSet) {
      AssociationCriteria criteria = AssociationCriteria.create(ValueSetValue.class, sessionFactory.getCurrentSession()).add("variable.id", Operation.eq, variableMemento.getId()).add("valueSet.valueTable.id", Operation.eq, valueTableMemento.getId());
      criteria.add("valueSet.variableEntity.identifier", Operation.eq, valueSet.getVariableEntity().getIdentifier()).add("valueSet.variableEntity.type", Operation.eq, valueSet.getVariableEntity().getType());
      ValueSetValue valueSetValue = (ValueSetValue) criteria.getCriteria().uniqueResult();
      return valueSetValue != null ? valueSetValue.getValue() : null;
    }

    @Override
    public ValueType getValueType() {
      return getVariable().getValueType();
    }

  }

}
