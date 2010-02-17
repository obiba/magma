package org.obiba.magma.datasource.hibernate;

import javax.transaction.Status;
import javax.transaction.Synchronization;

import org.hibernate.Transaction;

/**
 * Implements {@code Synchronization} and offers two methods for derived classes: commit() and rollback().
 * <p>
 * When a new instance of this class is created, it will automatically attach itself to the current transaction. If no
 * transaction exists, an exception is thrown.
 * 
 */
class HibernateDatasourceSynchronization implements Synchronization {

  HibernateDatasourceSynchronization(HibernateDatasource hibernateDatasource) {
    if(hibernateDatasource == null) throw new IllegalArgumentException("hibernateDatasource cannot be null");
    Transaction tx = hibernateDatasource.getSessionFactory().getCurrentSession().getTransaction();
    if(tx == null) {
      throw new IllegalStateException("No transaction, cannot create synchronization.");
    }
    tx.registerSynchronization(this);
  }

  @Override
  public void beforeCompletion() {
  }

  @Override
  public void afterCompletion(int status) {

    switch(status) {
    case Status.STATUS_COMMITTED:
      commit();
      break;

    case Status.STATUS_ROLLEDBACK:
      rollback();
      break;

    default:
      break;
    }
  }

  protected void commit() {

  }

  protected void rollback() {

  }
}
