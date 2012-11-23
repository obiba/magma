/*******************************************************************************
 * Copyright (c) 2012 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.magma.datasource.hibernate.converter;

import java.io.Serializable;

import org.hibernate.SessionFactory;
import org.obiba.core.service.impl.hibernate.AssociationCriteria;
import org.obiba.magma.Value;
import org.obiba.magma.ValueLoader;
import org.obiba.magma.ValueLoaderFactory;
import org.obiba.magma.datasource.hibernate.domain.ValueSetBinaryValue;
import org.obiba.magma.datasource.hibernate.domain.ValueSetValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.obiba.core.service.impl.hibernate.AssociationCriteria.Operation;

/**
 *
 */
public class HibernateValueLoaderFactory implements ValueLoaderFactory {

  private final SessionFactory sessionFactory;

  private final ValueSetValue valueSetValue;

  public HibernateValueLoaderFactory(SessionFactory sessionFactory, ValueSetValue valueSetValue) {
    this.sessionFactory = sessionFactory;
    this.valueSetValue = valueSetValue;
  }

  @Override
  public ValueLoader create(Value valueRef, Integer occurrence) {
    return new HibernateBinaryValueLoader(sessionFactory, valueSetValue, valueRef);
  }

  private static final class HibernateBinaryValueLoader implements ValueLoader, Serializable {

    private static final long serialVersionUID = -1878370804810810064L;

    private static final Logger log = LoggerFactory.getLogger(HibernateBinaryValueLoader.class);

    private final SessionFactory sessionFactory;

    private final ValueSetValue valueSetValue;

    private final Value valueRef;

    private byte[] value;

    private HibernateBinaryValueLoader(SessionFactory sessionFactory, ValueSetValue valueSetValue, Value valueRef) {
      this.sessionFactory = sessionFactory;
      this.valueSetValue = valueSetValue;
      this.valueRef = valueRef;
    }

    @Override
    public boolean isNull() {
      return valueRef == null || valueRef.isNull();
    }

    @Override
    public Object getValue() {
      if(value == null) {
        log.debug("Loading binary from value_set_binary_value table");

        ValueSetBinaryValue binaryValue = (ValueSetBinaryValue) AssociationCriteria
            .create(ValueSetBinaryValue.class, sessionFactory.getCurrentSession()) //
            .add("valueSetValue", Operation.eq, valueSetValue).getCriteria().uniqueResult();
        value = binaryValue == null ? null : binaryValue.getValue();
      }
      return value;
    }

  }

}
