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

import org.obiba.magma.*;
import org.obiba.magma.datasource.hibernate.converter.HibernateValueLoaderFactory;
import org.obiba.magma.datasource.hibernate.domain.ValueSetState;
import org.obiba.magma.datasource.hibernate.domain.ValueSetValue;
import org.obiba.magma.support.ValueSetBean;
import org.obiba.magma.type.BinaryType;

import javax.validation.constraints.NotNull;

/**
 * .
 */
class HibernateValueSet extends ValueSetBean {

  private final HibernateValueSetFetcher fetcher;

  private ValueSetState valueSetState;

  HibernateValueSet(HibernateValueTable table, VariableEntity entity) {
    super(table, entity);
    this.fetcher = new HibernateValueSetFetcher(table);
  }

  public Value getValue(Variable variable) {
    ValueSetValue vsv = getValueSetState().getValueMap().get(variable.getName());
    if(vsv == null) {
      return variable.isRepeatable() ? variable.getValueType().nullSequence() : variable.getValueType().nullValue();
    }
    return variable.getValueType().equals(BinaryType.get()) //
        ? getBinaryValue(variable, vsv) //
        : vsv.getValue();
  }

  private Value getBinaryValue(Variable variable, ValueSetValue vsv) {
    Value val = vsv.getValue();
    ValueLoaderFactory factory = new HibernateValueLoaderFactory(((HibernateValueTable) getValueTable()).getDatasource().getSessionFactory(), vsv);
    return variable.isRepeatable() //
        ? BinaryType.get().sequenceOfReferences(factory, val) //
        : BinaryType.get().valueOfReference(factory, val);
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
