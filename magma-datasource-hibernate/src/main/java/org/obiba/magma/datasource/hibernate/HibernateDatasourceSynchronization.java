/*
 * Copyright (c) 2019 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.magma.datasource.hibernate;

import javax.transaction.Status;
import javax.transaction.Synchronization;

import org.hibernate.Session;
import org.hibernate.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implements {@code Synchronization} and offers two methods for derived classes: commit() and rollback().
 * <p/>
 * When a new instance of this class is created, it will automatically attach itself to the current transaction. If no
 * transaction exists, an exception is thrown.
 */
class HibernateDatasourceSynchronization implements Synchronization {

  private static final Logger log = LoggerFactory.getLogger(HibernateDatasourceSynchronization.class);

  private final Session session;

  private final long txId;

  HibernateDatasourceSynchronization(HibernateDatasource hibernateDatasource) {
    if(hibernateDatasource == null) throw new IllegalArgumentException("hibernateDatasource cannot be null");
    session = hibernateDatasource.getSessionFactory().getCurrentSession();
    Transaction tx = session.getTransaction();
    if(tx == null) {
      throw new IllegalStateException("No transaction, cannot create synchronization.");
    }
    txId = tx.hashCode();
    log.debug("Registering synchronization: session {} tx {}", session.hashCode(), txId);
    tx.registerSynchronization(this);

  }

  @Override
  public void beforeCompletion() {
    log.debug("before completion: session {} tx {}", session.hashCode(), txId);
  }

  @Override
  public void afterCompletion(int status) {
    log.debug("after completion ({}): session {} tx {}", status, session.hashCode(), txId);
    switch(status) {
      case Status.STATUS_COMMITTED:
        commit();
        break;

      case Status.STATUS_ROLLEDBACK:
        rollback();
        break;

      default:
        log.error("Unknown TX status {} session {} tx {}", status, session.hashCode(), txId);
        break;
    }
  }

  public boolean isClosed() {
    return !session.isOpen();
  }

  protected void commit() {

  }

  protected void rollback() {

  }

  protected Session getSession() {
    return session;
  }


}
