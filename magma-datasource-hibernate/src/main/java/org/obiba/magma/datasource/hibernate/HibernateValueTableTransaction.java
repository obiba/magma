package org.obiba.magma.datasource.hibernate;

import java.util.Collections;
import java.util.List;

import javax.validation.constraints.NotNull;

import org.hibernate.LockMode;
import org.obiba.magma.VariableEntity;
import org.obiba.magma.VariableValueSource;

import com.google.common.collect.Lists;

/**
 * Transaction synchronisation for modifications made to a {@code HibernateValueTable}.
 * <p/>
 * Modifications made to an existing table will only appear after completion of the transaction.
 * <p/>
 * New tables are only added to the {@code HibernateDatasource} after completion of the transaction.
 */
class HibernateValueTableTransaction extends HibernateDatasourceSynchronization {

  @NotNull
  private final HibernateValueTable valueTable;

  private final boolean createTableTransaction;

  private final List<VariableValueSource> uncommittedSources = Lists.newLinkedList();

  private final List<VariableValueSource> uncommittedRemovedSources = Lists.newLinkedList();

  private final List<VariableEntity> uncommittedEntities = Lists.newLinkedList();

  private final HibernateValueTableWriter transactionWriter;

  HibernateValueTableTransaction(@NotNull HibernateValueTable valueTable, boolean newTable) {
    super(valueTable.getDatasource());
    this.valueTable = valueTable;
    createTableTransaction = newTable;
    transactionWriter = new HibernateValueTableWriter(this);

    if(!newTable) {
      valueTable.getValueTableState(LockMode.PESSIMISTIC_FORCE_INCREMENT);
    }
  }

  public HibernateValueTableWriter getTransactionWriter() {
    return transactionWriter;
  }

  @NotNull
  public HibernateValueTable getValueTable() {
    return valueTable;
  }

  /**
   * Adds all {@code VariableValueSource} and {@code VariableEntity} to the {@code HibernateValueTable}. If the
   * {@code HibernateValueTable} was created during this transaction, it will also be added to the
   * {@code HibernateDatasource}.
   */
  @Override
  protected void commit() {
    super.commit();
    valueTable.commitEntities(uncommittedEntities);
    valueTable.commitSources(uncommittedSources);
    valueTable.commitRemovedSources(uncommittedRemovedSources);
    if(createTableTransaction) {
      valueTable.getDatasource().commitValueTable(valueTable);
    }
  }

  /**
   * Add a {@code VariableValueSource} to the list of uncommitted sources to be added after transaction completion.
   *
   * @param source the new {@code VariableValueSource}.
   */
  public void addSource(VariableValueSource source) {
    uncommittedSources.add(source);
  }

  public void removeSource(VariableValueSource source) {
    uncommittedRemovedSources.add(source);
  }

  /**
   * Returns the list of {@code VariableValueSource} to be committed after transaction completion.
   *
   * @return
   */
  public List<VariableValueSource> getUncommittedSources() {
    List<VariableValueSource> copy = Lists.newArrayList(uncommittedSources);
    copy.removeAll(uncommittedRemovedSources);
    return Collections.unmodifiableList(copy);
  }

  /**
   * Add a {@code VariableValueSource} to the list of uncommitted sources to be added after transaction completion.
   *
   * @param source the new {@code VariableValueSource}.
   */
  public void addEntity(VariableEntity entity) {
    uncommittedEntities.add(entity);
  }

  /**
   * Returns the list of {@code VariableEntity} to be committed after transaction completion.
   *
   * @return
   */
  public List<VariableEntity> getUncommittedEntities() {
    return Collections.unmodifiableList(uncommittedEntities);
  }
}
