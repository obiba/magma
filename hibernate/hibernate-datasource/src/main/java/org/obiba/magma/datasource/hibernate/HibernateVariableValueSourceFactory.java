package org.obiba.magma.datasource.hibernate;

import java.io.Serializable;
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
import org.obiba.magma.datasource.hibernate.HibernateValueTable.HibernateValueSet;
import org.obiba.magma.datasource.hibernate.converter.HibernateMarshallingContext;
import org.obiba.magma.datasource.hibernate.converter.VariableConverter;
import org.obiba.magma.datasource.hibernate.domain.ValueSetValue;
import org.obiba.magma.datasource.hibernate.domain.VariableState;

public class HibernateVariableValueSourceFactory implements VariableValueSourceFactory {

  private HibernateValueTable valueTable;

  public HibernateVariableValueSourceFactory(HibernateValueTable valueTable) {
    super();
    this.valueTable = valueTable;
  }

  @Override
  public Set<VariableValueSource> createSources() {
    HibernateMarshallingContext ctx = HibernateMarshallingContext.create(getSessionFactory(), valueTable.getValueTableState());
    Set<VariableValueSource> sources = new LinkedHashSet<VariableValueSource>();
    AssociationCriteria criteria = AssociationCriteria.create(VariableState.class, getSessionFactory().getCurrentSession()).add("valueTable", Operation.eq, valueTable.getValueTableState()).addSortingClauses(SortingClause.create("pos"));
    for(Object obj : criteria.list()) {
      VariableState state = (VariableState) obj;
      Variable variable = VariableConverter.getInstance().unmarshal(state, ctx);
      HibernateVariableValueSource source = new HibernateVariableValueSource(variable, state);
      sources.add(source);
    }
    return sources;
  }

  public VariableValueSource createSource(Variable variable) {
    AssociationCriteria criteria = AssociationCriteria.create(VariableState.class, getSessionFactory().getCurrentSession()).add("valueTable", Operation.eq, valueTable.getValueTableState()).add("name", Operation.eq, variable.getName());
    VariableState variableState = (VariableState) criteria.getCriteria().uniqueResult();

    HibernateVariableValueSource source = new HibernateVariableValueSource(variable, variableState);
    return source;
  }

  private SessionFactory getSessionFactory() {
    return valueTable.getDatasource().getSessionFactory();
  }

  private class HibernateVariableValueSource implements VariableValueSource {

    private Serializable variableId;

    private Variable variable;

    public HibernateVariableValueSource(Variable variable, VariableState state) {
      super();
      this.variable = variable;
      this.variableId = state.getId();
    }

    @Override
    public Variable getVariable() {
      return variable;
    }

    @Override
    public Value getValue(ValueSet valueSet) {
      HibernateValueSet jpaValueSet = (HibernateValueSet) valueSet;
      AssociationCriteria criteria = AssociationCriteria.create(ValueSetValue.class, getSessionFactory().getCurrentSession()).add("variable.id", Operation.eq, variableId).add("valueSet", Operation.eq, jpaValueSet.getValueSetState());
      ValueSetValue valueSetValue = (ValueSetValue) criteria.getCriteria().uniqueResult();
      return valueSetValue != null ? valueSetValue.getValue() : (getVariable().isRepeatable() ? getValueType().nullSequence() : getValueType().nullValue());
    }

    @Override
    public ValueType getValueType() {
      return getVariable().getValueType();
    }

  }

}
