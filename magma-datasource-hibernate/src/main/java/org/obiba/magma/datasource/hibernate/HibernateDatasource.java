/*
 * Copyright (c) 2017 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.magma.datasource.hibernate;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentMap;

import javax.validation.constraints.NotNull;

import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.criterion.Restrictions;
import org.obiba.core.service.impl.hibernate.AssociationCriteria;
import org.obiba.core.service.impl.hibernate.AssociationCriteria.Operation;
import org.obiba.magma.MagmaRuntimeException;
import org.obiba.magma.NoSuchValueTableException;
import org.obiba.magma.Timestamped;
import org.obiba.magma.Timestamps;
import org.obiba.magma.ValueTable;
import org.obiba.magma.ValueTableWriter;
import org.obiba.magma.datasource.hibernate.converter.AttributeAwareConverter;
import org.obiba.magma.datasource.hibernate.converter.HibernateMarshallingContext;
import org.obiba.magma.datasource.hibernate.domain.AttributeState;
import org.obiba.magma.datasource.hibernate.domain.DatasourceState;
import org.obiba.magma.datasource.hibernate.domain.ValueTableState;
import org.obiba.magma.datasource.hibernate.domain.VariableState;
import org.obiba.magma.support.AbstractDatasource;
import org.obiba.magma.support.UnionTimestamps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;
import com.google.common.base.Stopwatch;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.collect.MapMaker;

/**
 * Datasource based on entity-attribute-value model.
 */
public class HibernateDatasource extends AbstractDatasource {

  private static final Logger log = LoggerFactory.getLogger(HibernateDatasource.class);

  public static final String TYPE = "hibernate";

  private final SessionFactory sessionFactory;

  private Serializable datasourceId;

  /**
   * A Map of {@code org.hibernate.Transaction} to a list of all {@code HibernateValueTable} involved in the
   * transaction. This map uses weak keys, meaning that when the transaction object is no longer reference anywhere, its
   * entry is removed from the map.
   */
  private final ConcurrentMap<Transaction, List<HibernateValueTableTransaction>> syncMap = new MapMaker().weakKeys()
      .makeMap();

  @SuppressWarnings("ConstantConditions")
  public HibernateDatasource(@NotNull String name, @NotNull SessionFactory sessionFactory) {
    super(name, TYPE);
    if(sessionFactory == null) throw new IllegalArgumentException("sessionFactory cannot be null");

    this.sessionFactory = sessionFactory;
  }

  /**
   * Creates a writer for the specified table name and entity type. If the table does not exist, a new one is created.
   * <p/>
   * Note that a Hibernate transaction must be active for this method to return an instance of {@code ValueTableWriter}
   */
  @NotNull
  @Override
  public ValueTableWriter createWriter(@NotNull String tableName, @NotNull String entityType) {
    Preconditions.checkArgument(tableName != null, "tableName cannot be null");
    Preconditions.checkArgument(entityType != null, "entityType cannot be null");

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
        getSessionFactory().getCurrentSession().refresh(valueTableState); //OPAL-2635
        // Create a transaction for a new table
        valueTableTransaction = newTableTransaction(new HibernateValueTable(this, valueTableState), true);
      }
    }

    return valueTableTransaction.getTransactionWriter();
  }

  @Override
  public boolean isTransactional() {
    return true;
  }

  /**
   * Returns true if a value table exists for the specified name or that a create table transaction is active for that
   * tableName.
   */
  @Override
  public boolean hasValueTable(String tableName) {
    // If parent doesn't have the table, there may be an active transaction creating this table.
    return super.hasValueTable(tableName) || hasTableTransaction(tableName);
  }

  /**
   * Returns the table with the specified tableName. If a create table transaction is active, the table will be visible
   * within the current transaction only.
   */
  @Override
  public ValueTable getValueTable(String tableName) throws NoSuchValueTableException {
    if(hasTableTransaction(tableName)) {
      return getTableTransaction(tableName).getValueTable();
    }
    return super.getValueTable(tableName);
  }

  @Override
  public boolean canDropTable(String tableName) {
    return hasValueTable(tableName);
  }

  @Override
  public void dropTable(@NotNull String tableName) {

    Stopwatch stopwatch = Stopwatch.createStarted();
    String tableFullName = getName() + "." + tableName;
    log.info("Dropping table {}", tableFullName);

    HibernateValueTable valueTable = (HibernateValueTable) getValueTable(tableName);
    ValueTableState tableState = valueTable.getValueTableState();
    removeValueTable(tableName);

    // cannot use cascading because DELETE (and INSERT) do not cascade via relationships in JPQL query
    Session session = getSessionFactory().getCurrentSession();

    deleteValueSets(tableFullName, session,
        session.getNamedQuery("findValueSetIdsByTableId").setParameter("valueTableId", tableState.getId()).list());

    deleteTableVariables(tableFullName, tableState, session);

    session.delete(tableState);
    log.debug("Dropped table '{}' in {}", tableFullName, stopwatch);

    // force datasource timestamp update
    updateDatasourceLastUpdate();
  }

  private void deleteTableVariables(String tableFullName, ValueTableState tableState, Session session) {
    Stopwatch stopwatch = Stopwatch.createStarted();
    List<VariableState> variables = AssociationCriteria.create(VariableState.class, session)
        .add("valueTable", Operation.eq, tableState).list();
    for(VariableState v : variables) {
      session.delete(v);
    }
    log.debug("Deleted {} variables from {} in {}", variables.size(), tableFullName, stopwatch);
  }

  private void updateDatasourceLastUpdate() {
    getDatasourceState().setUpdated(new Date());
  }

  @Override
  public boolean canDrop() {
    return true;
  }

  @Override
  public void drop() {
    for(String valueTable : getValueTableNames()) {
      dropTable(valueTable);
    }
    sessionFactory.getCurrentSession().delete(getDatasourceState());
  }

  void deleteValueSets(String tableFullName, @SuppressWarnings("TypeMayBeWeakened") Session session,
      Collection<?> valueSetIds) {
    if(valueSetIds.isEmpty()) return;
    Stopwatch stopwatch = Stopwatch.createStarted();
    int nbDeletedBinaries = session.getNamedQuery("deleteValueSetBinaryValues")
        .setParameterList("valueSetIds", valueSetIds).executeUpdate();
    log.debug("Deleted {} binaries from {} in {}", nbDeletedBinaries, tableFullName, stopwatch.stop());

    stopwatch.start();
    int nbDeletedValues = session.getNamedQuery("deleteValueSetValues").setParameterList("valueSetIds", valueSetIds)
        .executeUpdate();
    log.debug("Deleted {} values from {} in {}", nbDeletedValues, tableFullName, stopwatch.stop());

    stopwatch.start();
    int nbDeletedValueSets = session.getNamedQuery("deleteValueSetStates")
        .setParameterList("valueTableIds", valueSetIds).executeUpdate();
    log.debug("Deleted {} valueSets from {} in {}", nbDeletedValueSets, tableFullName, stopwatch.stop());
  }

  @Override
  protected void onInitialise() {
    DatasourceState datasourceState = (DatasourceState) sessionFactory.getCurrentSession()
        .createCriteria(DatasourceState.class).add(Restrictions.eq("name", getName())).uniqueResult();

    // If datasource not persisted, create the persisted DatasourceState.
    if(datasourceState == null) {
      datasourceState = new DatasourceState(getName());
      sessionFactory.getCurrentSession().save(datasourceState);
      sessionFactory.getCurrentSession().refresh(datasourceState); //OPAL-2635
    } else {
      // If already persisted, load the persisted attributes for that datasource.
      for(AttributeState attribute : datasourceState.getAttributes()) {
        setAttributeValue(attribute.getName(), attribute.getValue());
      }

    }
    datasourceId = datasourceState.getId();
  }

  @Override
  protected void onDispose() {
    DatasourceState state = getDatasourceState();
    if(state != null) { // in case we dropped the datasource
      new AttributeAwareConverter().addAttributes(this, state);
      getSessionFactory().getCurrentSession().save(state);
    }
  }

  @Override
  protected Set<String> getValueTableNames() {
    Set<String> names = new LinkedHashSet<>();
    AssociationCriteria criteria = AssociationCriteria.create(ValueTableState.class, sessionFactory.getCurrentSession())
        .add("datasource.id", Operation.eq, datasourceId);
    for(Object obj : criteria.list()) {
      ValueTableState state = (ValueTableState) obj;
      names.add(state.getName());
    }
    return names;
  }

  @Override
  protected ValueTable initialiseValueTable(String tableName) {
    return new HibernateValueTable(this,
        (ValueTableState) AssociationCriteria.create(ValueTableState.class, sessionFactory.getCurrentSession())
            .add("datasource.id", Operation.eq, datasourceId) //
            .add("name", Operation.eq, tableName) //
            .getCriteria().uniqueResult());
  }

  @NotNull
  @Override
  public Timestamps getTimestamps() {
    ImmutableSet.Builder<Timestamped> builder = ImmutableSet.builder();
    builder.addAll(getValueTables()).add(getDatasourceState());
    return new UnionTimestamps(builder.build());
  }

  @Override
  public boolean canRenameTable(String tableName) {
    return hasValueTable(tableName);
  }

  @Override
  public void renameTable(String tableName, String newName) {
    if(canRenameTable(newName)) throw new MagmaRuntimeException("A table already exists with the name: " + newName);

    log.info("Renaming table {} to {}", tableName, newName);
    ((HibernateValueTable) getValueTable(tableName)).setName(newName);
    updateDatasourceLastUpdate();
  }

  /**
   * Adds the specified {@code ValueTable} to the set of value tables this datasource holds. This method is used by
   * {@code HibernateValueTableTransaction} to add value tables that are created within a transaction.
   *
   * @param vt the value table instance to add
   */
  void commitValueTable(ValueTable vt) {
    addValueTable(vt);
  }

  /**
   * Use with caution!
   *
   * @return sessionFactory used by the datasource
   */
  public SessionFactory getSessionFactory() {
    return sessionFactory;
  }

  DatasourceState getDatasourceState() {
    return (DatasourceState) sessionFactory.getCurrentSession().get(DatasourceState.class, datasourceId);
  }

  @SuppressWarnings("UnusedDeclaration")
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
   * <p/>
   * Note that this method will throw an exception if no transaction exists for the table name. Use
   * {@code #hasTableTransaction} to test the existence of a transaction before calling this method.
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
   *
   * @param valueTable the value table for which to create the transaction.
   * @param createTableTransaction true when this transaction is creating the value table, false otherwise.
   * @return a {@code HibernateValueTableTransaction} instance for the specified {@code HibernateValueTable}
   */
  synchronized HibernateValueTableTransaction newTableTransaction(HibernateValueTable valueTable,
      boolean createTableTransaction) {
    if(hasTableTransaction(valueTable.getName())) {
      return getTableTransaction(valueTable.getName());
    }
    HibernateValueTableTransaction tableTx = new HibernateValueTableTransaction(valueTable, createTableTransaction);
    lookupTableTransactions().add(tableTx);
    return tableTx;
  }

  /**
   * Returns the list of {@code HibernateValueTableTransaction} associated with the current
   * {@code org.hibernate.Transaction}. Within one Hibernate transaction, several value tables may be affected, as such,
   * this method returns a list of {@code HibernateValueTableTransaction} instances. This method never returns null.
   * <p/>
   * If no Hibernate transaction exists this method returns an empty list.
   *
   * @return list of {@code HibernateValueTableTransaction} associated with the current
   * {@code org.hibernate.Transaction}
   */
  private synchronized Collection<HibernateValueTableTransaction> lookupTableTransactions() {

    Session currentSession;
    try {
      currentSession = getSessionFactory().getCurrentSession();
    } catch(HibernateException e) {
      // No current session. Obviously, no transaction.
      return Collections.emptyList();
    }

    autoCleanSyncMap();
    Transaction tx = currentSession.getTransaction();
    if(tx != null) {
      List<HibernateValueTableTransaction> tableTxs = syncMap
          .putIfAbsent(tx, new ArrayList<HibernateValueTableTransaction>());
      if(tableTxs == null) {
        tableTxs = syncMap.get(tx);
      }
      return tableTxs;
    } else {
      return Collections.emptyList();
    }
  }

  private void autoCleanSyncMap() {
    for (Map.Entry<Transaction, List<HibernateValueTableTransaction>> entry : syncMap.entrySet()) {
      List<HibernateValueTableTransaction> txs = entry.getValue();
      if (txs.size() > 0) {
        List<HibernateValueTableTransaction> toRemove = Lists.newArrayList();
        for (HibernateValueTableTransaction tx : txs) {
          if (tx.isClosed()) toRemove.add(tx);
        }
        txs.removeAll(toRemove);
      }

      if (txs.isEmpty()) syncMap.remove(entry.getKey());
    }
  }
}
