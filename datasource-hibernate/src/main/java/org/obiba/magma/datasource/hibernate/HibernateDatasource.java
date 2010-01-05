package org.obiba.magma.datasource.hibernate;

import java.io.Serializable;
import java.util.LinkedHashSet;
import java.util.Set;

import org.hibernate.SessionFactory;
import org.hibernate.criterion.Restrictions;
import org.obiba.core.service.impl.hibernate.AssociationCriteria;
import org.obiba.core.service.impl.hibernate.AssociationCriteria.Operation;
import org.obiba.magma.ValueTable;
import org.obiba.magma.ValueTableWriter;
import org.obiba.magma.datasource.hibernate.converter.HibernateMarshallingContext;
import org.obiba.magma.datasource.hibernate.domain.DatasourceState;
import org.obiba.magma.datasource.hibernate.domain.ValueTableState;
import org.obiba.magma.support.AbstractDatasource;

public class HibernateDatasource extends AbstractDatasource {

  private static final String JPA_TYPE = "jpa";

  private SessionFactory sessionFactory;

  private Serializable datasourceId;

  public HibernateDatasource(String name, SessionFactory sessionFactory) {
    super(name, JPA_TYPE);
    this.sessionFactory = sessionFactory;
  }

  @Override
  public ValueTableWriter createWriter(String tableName, String entityType) {
    HibernateValueTable valueTable;
    if(hasValueTable(tableName)) {
      valueTable = (HibernateValueTable) getValueTable(tableName);
    } else {
      ValueTableState valueTableState = new ValueTableState(tableName, entityType, getDatasourceState());
      sessionFactory.getCurrentSession().save(valueTableState);
      addValueTable(valueTable = new HibernateValueTable(this, valueTableState));
    }
    return new HibernateValueTableWriter(valueTable);
  }

  @Override
  protected void onInitialise() {
    DatasourceState state = (DatasourceState) sessionFactory.getCurrentSession().createCriteria(DatasourceState.class).add(Restrictions.eq("name", getName())).uniqueResult();
    if(state == null) {
      state = new DatasourceState(getName());
      sessionFactory.getCurrentSession().save(state);
    } else {
      HibernateMarshallingContext context = HibernateMarshallingContext.create(sessionFactory);
      // TODO attribute aware builder for datasource
      // context.setAttributeAwareBuilder(null);
      // AttributeAwareConverter.getInstance().unmarshal(datasourceMemento, context);
    }
    this.datasourceId = state.getId();
  }

  protected Set<String> getValueTableNames() {
    Set<String> names = new LinkedHashSet<String>();
    AssociationCriteria criteria = AssociationCriteria.create(ValueTableState.class, sessionFactory.getCurrentSession()).add("datasource.id", Operation.eq, datasourceId);
    for(Object obj : criteria.list()) {
      ValueTableState state = (ValueTableState) obj;
      names.add(state.getName());
    }
    return names;
  }

  protected ValueTable initialiseValueTable(String tableName) {
    return new HibernateValueTable(this, (ValueTableState) AssociationCriteria.create(ValueTableState.class, sessionFactory.getCurrentSession()).add("datasource.id", Operation.eq, datasourceId).add("name", Operation.eq, tableName).getCriteria().uniqueResult());
  }

  SessionFactory getSessionFactory() {
    return sessionFactory;
  }

  DatasourceState getDatasourceState() {
    return (DatasourceState) sessionFactory.getCurrentSession().get(DatasourceState.class, datasourceId);
  }

}
