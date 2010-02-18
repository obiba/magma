package org.obiba.magma.datasource.hibernate;

import java.io.Serializable;
import java.util.LinkedHashSet;
import java.util.Set;

import org.hibernate.FetchMode;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;
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
    AssociationCriteria criteria = AssociationCriteria.create(VariableState.class, getCurrentSession()).add("valueTable", Operation.eq, valueTable.getValueTableState()).addSortingClauses(SortingClause.create("pos"));
    for(Object obj : criteria.list()) {
      VariableState state = (VariableState) obj;
      sources.add(new HibernateVariableValueSource(state, false));
    }
    return sources;
  }

  VariableValueSource createSource(VariableState variableState) {
    return new HibernateVariableValueSource(variableState, true);
  }

  private Session getCurrentSession() {
    return valueTable.getDatasource().getSessionFactory().getCurrentSession();
  }

  private class HibernateVariableValueSource implements VariableValueSource {

    private final Serializable variableId;

    private Variable variable;

    private String name;

    public HibernateVariableValueSource(VariableState state, boolean unmarshall) {
      if(state == null) throw new IllegalArgumentException("state cannot be null");
      if(state.getId() == null) throw new IllegalArgumentException("state must be persisted");
      this.variableId = state.getId();
      this.name = state.getName();

      if(unmarshall) {
        unmarshall(state);
      }
    }

    @Override
    public synchronized Variable getVariable() {
      if(variable == null) {
        VariableState state = (VariableState) getCurrentSession().createCriteria(VariableState.class).add(Restrictions.idEq(this.variableId)).setFetchMode("categories", FetchMode.JOIN).uniqueResult();
        unmarshall(state);
      }
      return variable;
    }

    @Override
    public Value getValue(ValueSet valueSet) {
      HibernateValueSet hibernateValueSet = (HibernateValueSet) valueSet;
      AssociationCriteria criteria = AssociationCriteria.create(ValueSetValue.class, getCurrentSession()).add("variable.id", Operation.eq, variableId).add("valueSet", Operation.eq, hibernateValueSet.getValueSetState());
      ValueSetValue valueSetValue = (ValueSetValue) criteria.getCriteria().uniqueResult();
      return valueSetValue != null ? valueSetValue.getValue() : (getVariable().isRepeatable() ? getValueType().nullSequence() : getValueType().nullValue());
    }

    @Override
    public ValueType getValueType() {
      return getVariable().getValueType();
    }

    /**
     * Initialises the {@code variable} attribute from the provided state
     * @param state
     */
    private void unmarshall(VariableState state) {
      variable = VariableConverter.getInstance().unmarshal(state, null);
    }

    @Override
    public boolean equals(Object obj) {
      if(this == obj) {
        return true;
      }
      if(obj == null) {
        return false;
      }
      if(obj instanceof HibernateVariableValueSource == false) {
        return super.equals(obj);
      }
      HibernateVariableValueSource rhs = (HibernateVariableValueSource) obj;
      return this.name.equals(rhs.name);
    }

    @Override
    public int hashCode() {
      return this.name.hashCode();
    }
  }

}
