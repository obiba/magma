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

class HibernateVariableValueSourceFactory implements VariableValueSourceFactory {

  private final HibernateValueTable valueTable;

  HibernateVariableValueSourceFactory(HibernateValueTable valueTable) {
    super();
    if(valueTable == null) throw new IllegalArgumentException("valueTable cannot be null");
    this.valueTable = valueTable;
  }

  @Override
  public Set<VariableValueSource> createSources() {
    Set<VariableValueSource> sources = new LinkedHashSet<VariableValueSource>();
    AssociationCriteria criteria = AssociationCriteria.create(VariableState.class, getSessionFactory().getCurrentSession()).add("valueTable", Operation.eq, valueTable.getValueTableState()).addSortingClauses(SortingClause.create("pos"));
    for(Object obj : criteria.list()) {
      VariableState state = (VariableState) obj;
      HibernateVariableValueSource source = new HibernateVariableValueSource(state);
      sources.add(source);
    }
    return sources;
  }

  VariableValueSource createSource(VariableState variableState) {
    HibernateVariableValueSource source = new HibernateVariableValueSource(variableState);
    return source;
  }

  private SessionFactory getSessionFactory() {
    return valueTable.getDatasource().getSessionFactory();
  }

  private class HibernateVariableValueSource implements VariableValueSource {

    private final Serializable variableId;

    private Variable variable;

    public HibernateVariableValueSource(VariableState state) {
      if(state == null) throw new IllegalArgumentException("state cannot be null");
      if(state.getId() == null) throw new IllegalArgumentException("state must be persisted");
      this.variableId = state.getId();
    }

    @Override
    public synchronized Variable getVariable() {
      if(variable == null) {
        HibernateMarshallingContext ctx = valueTable.createContext();
        VariableState state = (VariableState) getSessionFactory().getCurrentSession().load(VariableState.class, variableId);
        variable = VariableConverter.getInstance().unmarshal(state, ctx);
      }
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
