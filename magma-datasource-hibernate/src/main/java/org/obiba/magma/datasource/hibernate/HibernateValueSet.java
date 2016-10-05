/*
 * Copyright (c) 2016 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.magma.datasource.hibernate;

import org.hibernate.Criteria;
import org.obiba.magma.NoSuchValueSetException;
import org.obiba.magma.Timestamps;
import org.obiba.magma.VariableEntity;
import org.obiba.magma.datasource.hibernate.domain.ValueSetState;
import org.obiba.magma.support.ValueSetBean;

import javax.validation.constraints.NotNull;

/**
 * Created by yannick on 05/10/16.
 */
class HibernateValueSet extends ValueSetBean {

  private final HibernateValueSetFetcher fetcher;

  private ValueSetState valueSetState;

  HibernateValueSet(HibernateValueTable table, VariableEntity entity) {
    super(table, entity);
    this.fetcher = new HibernateValueSetFetcher(table);
  }

  synchronized ValueSetState getValueSetState() {
    if (valueSetState == null) {
      valueSetState = fetcher.getValueSetState(getVariableEntity());
    }
    return valueSetState;
  }

  void setValueSetState(ValueSetState valueSetState) {
    this.valueSetState = valueSetState;
  }

  @NotNull
  @Override
  public Timestamps getTimestamps() {
    return HibernateValueTable.createTimestamps(getValueSetState());
  }
}
