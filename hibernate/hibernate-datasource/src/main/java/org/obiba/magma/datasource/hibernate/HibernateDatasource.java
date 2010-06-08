package org.obiba.magma.datasource.hibernate;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentMap;

import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.criterion.Restrictions;
import org.obiba.core.service.impl.hibernate.AssociationCriteria;
import org.obiba.core.service.impl.hibernate.AssociationCriteria.Operation;
import org.obiba.magma.NoSuchValueTableException;
import org.obiba.magma.ValueTable;
import org.obiba.magma.ValueTableWriter;
import org.obiba.magma.datasource.hibernate.converter.AttributeAwareConverter;
import org.obiba.magma.datasource.hibernate.converter.HibernateMarshallingContext;
import org.obiba.magma.datasource.hibernate.domain.AttributeState;
import org.obiba.magma.datasource.hibernate.domain.DatasourceState;
import org.obiba.magma.datasource.hibernate.domain.ValueTableState;
import org.obiba.magma.support.AbstractDatasource;

import com.google.common.collect.MapMaker;

public class HibernateDatasource extends AbstractDatasource {

  private static final String HIBERNATE_TYPE = "hibernate";

  private final SessionFactory sessionFactory;

  private Serializable datasourceId;

  /**
   * A Map of {@code org.hibernate.Transaction} to a list of all {@code HibernateValueTable} involved in the
   * transaction. This map uses weak keys, meaning that when the transaction object is no longer reference anywhere, its
   * entry is removed from the map.
   */
  private ConcurrentMap<Transaction, List<HibernateValueTableTransaction>> syncMap = new MapMaker().weakKeys().makeMap();

  public HibernateDatasource(String name, SessionFactory sessionFactory) {
    super(name, HIBERNATE_TYPE);
    if(sessionFactory == null) throw new IllegalArgumentException("sessionFactory cannot be null");

    this.sessionFactory = sessionFactory;
  }

  /**
   * Creates a writer for the specified table name and entity type. If the table does not exist, a new one is created.
   * <p>
   * Note that a Hibernate transaction must be active for this method to return an instance of {@code ValueTableWriter}
   */
  @Override
  public ValueTableWriter createWriter(String tableName, String entityType) {
    if(tableName == null) throw new IllegalArgumentException("tableName cannot be null");
    if(entityType == null) throw new IllegalArgumentException("entityType cannot be null");

    HibernateValueTableTransaction valueTableTransaction;
    if(hasTableTransaction(tableName)) {
      valueTableTransaction = getTableTransaction(tableName);
    } else {
      // A new transaction must be created. Either for an existing table or a new table.
      if(super.hasValueTable(tableName)) {
        // Create a transaction on an existing table
        HibernateValueTable valueTable = (HibernateValueTable) getValueTable(tableName);
        valueTableTransaction = newTableTransaction(valueTable, false);
      } else {
        // Create table transaction.
        ValueTableState valueTableState = new ValueTableState(tableName, entityType, getDatasourceState());
        getSessionFactory().getCurrentSession().save(valueTableState);
        // Create a transaction for a new table
        valueTableTransaction = newTableTransaction(new HibernateValueTable(this, valueTableState), true);
      }
    }

    return valueTableTransaction.getTransactionWriter();
  }

  /**
   * Returns true if a value table exists for the specified name or that a create table transaction is active for that
   * name.
   */
  @Override
  public boolean hasValueTable(String name) {
    // If parent doesn't have the table, there may be an active transaction creating this table.
    return super.hasValueTable(name) || hasTableTransaction(name);
  }

  /**
   * Returns the table with the specified name. If a create table transaction is active, the table will be visible
   * within the current transaction only.
   */
  @Override
  public ValueTable getValueTable(String name) throws NoSuchValueTableException {
    if(hasTableTransaction(name)) {
      return getTableTransaction(name).getValueTable();
    }
    return super.getValueTable(name);
  }

  @Override
  protected void onInitialise() {
    DatasourceState datasourceState = (DatasourceState) sessionFactory.getCurrentSession().createCriteria(DatasourceState.class).add(Restrictions.eq("name", getName())).uniqueResult();

    // If datasource not persisted, create the persisted DatasourceState.
    if(datasourceState == null) {
      datasourceState = new DatasourceState(getName());
      sessionFactory.getCurrentSession().save(datasourceState);
    } else {
      // If already persisted, load the persisted attributes for that datasource.
      for(AttributeState attribute : datasourceState.getAttributes()) {
        setAttributeValue(attribute.getName(), attribute.getValue());
      }

    }
    this.datasourceId = datasourceState.getId();
  }

  @Override
  protected void onDispose() {
    DatasourceState state = getDatasourceState();
    new AttributeAwareConverter().addAttributes(this, state);
    getSessionFactory().getCurrentSession().save(state);
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

  /**
   * Adds the specified {@code ValueTable} to the set of value tables this datasource holds. This method is used by
   * {@code HibernateValueTableTransaction} to add value tables that are created within a transaction.
   * 
   * @param vt the value table instance to add
   */
  void commitValueTable(ValueTable vt) {
    super.addValueTable(vt);
  };

  SessionFactory getSessionFactory() {
    return sessionFactory;
  }

  DatasourceState getDatasourceState() {
    return (DatasourceState) sessionFactory.getCurrentSession().get(DatasourceState.class, datasourceId);
  }

  HibernateMarshallingContext createContext() {
    return HibernateMarshallingContext.create(getSessionFactory(), getDatasourceState());
  }

  HibernateMarshallingContext createContext(ValueTableState valueTableState) {
    return HibernateMarshallingContext.create(getSessionFactory(), getDatasourceState(), valueTableState);
  }

  /**
   * Returns true if a transaction exists on the specified table name. False otherwise. Note that this will return true
   * only if the current thread is associated with the transaction.
   * 
   * @param name the name of the value table
   * @return true when a {@code HibernateValueTableTransaction} currently exists for the specified table name
   */
  boolean hasTableTransaction(String name) {
    for(HibernateValueTableTransaction tableTx : lookupTableTransactions()) {
      if(tableTx.getValueTable().getName().equals(name)) {
        return true;
      }
    }
    return false;
  }

  /**
   * Returns the currently visible {@code HibernateValueTableTransaction} for the specified table name.
   * <p>
   * Note that this method will throw an exception if no transaction exists for the table name. Use {@code
   * #hasTableTransaction} to test the existence of a transaction before calling this method.
   * 
   * @param name the name of the value table
   * @return the instance of {@code HibernateValueTableTransaction} if one exists.
   * @throws IllegalStateException if no such transaction instance exists.
   */
  HibernateValueTableTransaction getTableTransaction(String name) {
    for(HibernateValueTableTransaction tableTx : lookupTableTransactions()) {
      if(tableTx.getValueTable().getName().equals(name)) {
        return tableTx;
      }
    }
    throw new IllegalStateException("No transaction exists on table " + name);
  }

  /**
   * Creates a new {@code HibernateValueTableTransaction} for the specified value table. If a transaction already
   * exists, that transaction is returned.
   * @param valueTable the value table for which to create the transaction.
   * @param createTableTransaction true when this transaction is creating the value table, false otherwise.
   * @return a {@code HibernateValueTableTransaction} instance for the specified {@code HibernateValueTable}
   */
  synchronized HibernateValueTableTransaction newTableTransaction(HibernateValueTable valueTable, boolean createTableTransaction) {
    if(hasTableTransaction(valueTable.getName())) {
      return getTableTransaction(valueTable.getName());
    }
    HibernateValueTableTransaction tableTx = new HibernateValueTableTransaction(valueTable, createTableTransaction);
    lookupTableTransactions().add(tableTx);
    return tableTx;
  }

  /**
   * Returns the list of {@code HibernateValueTableTransaction} associated with the current {@code
   * org.hibernate.Transaction}. Within one Hibernate transaction, several value tables may be affected, as such, this
   * method returns a list of {@code HibernateValueTableTransaction} instances. This method never returns null.
   * <p>
   * If no Hibernate transaction exists this method returns an empty list.
   * @return
   */
  private List<HibernateValueTableTransaction> lookupTableTransactions() {

    Session currentSession;
    try {
      currentSession = getSessionFactory().getCurrentSession();
    } catch(HibernateException e) {
      // No current session. Obviously, no transaction.
      return Collections.emptyList();
    }

    Transaction tx = currentSession.getTransaction();
    if(tx != null) {
      List<HibernateValueTableTransaction> tableTxs = syncMap.putIfAbsent(tx, new ArrayList<HibernateValueTableTransaction>());
      if(tableTxs == null) {
        tableTxs = syncMap.get(tx);
      }
      return tableTxs;
    } else {
      return Collections.emptyList();
    }
  }

}
