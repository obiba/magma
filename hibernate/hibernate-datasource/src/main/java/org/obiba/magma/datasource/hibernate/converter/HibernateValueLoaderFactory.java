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

import org.hibernate.Query;
import org.hibernate.SessionFactory;
import org.obiba.core.service.impl.hibernate.AssociationCriteria;
import org.obiba.magma.Value;
import org.obiba.magma.ValueLoader;
import org.obiba.magma.ValueLoaderFactory;
import org.obiba.magma.datasource.hibernate.domain.ValueSetBinaryValue;
import org.obiba.magma.datasource.hibernate.domain.ValueSetValue;
import org.obiba.magma.type.BinaryType;
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
    return new HibernateBinaryValueLoader(sessionFactory, valueSetValue, valueRef, occurrence);
  }

  private static final class HibernateBinaryValueLoader implements ValueLoader, Serializable {

    private static final long serialVersionUID = -1878370804810810064L;

    private static final Logger log = LoggerFactory.getLogger(HibernateBinaryValueLoader.class);

    private final SessionFactory sessionFactory;

    private final ValueSetValue valueSetValue;

    private final Value valueRef;

    private final Integer occurrence;

    private byte[] value;

    private HibernateBinaryValueLoader(SessionFactory sessionFactory, ValueSetValue valueSetValue, Value valueRef,
        Integer occurrence) {
      this.sessionFactory = sessionFactory;
      this.valueSetValue = valueSetValue;
      this.valueRef = valueRef;
      this.occurrence = occurrence;
    }

    @Override
    public boolean isNull() {
      return valueRef == null || valueRef.isNull();
    }

    @Override
    public Object getValue() {
      if(value == null) {
        if(valueRef.getValueType().equals(BinaryType.get())) {
          // legacy
          log.debug("Loading binary from database");
          value = (byte[]) valueRef.getValue();
        } else {
          // load binary from binary_value_set_value table
          log.debug("Loading binary from binary_value_set_value table");

          ValueSetBinaryValue binaryValue = (ValueSetBinaryValue) AssociationCriteria
              .create(ValueSetBinaryValue.class, sessionFactory.getCurrentSession()) //
              .add("valueSetValue", Operation.eq, valueSetValue).getCriteria().uniqueResult();
          return binaryValue == null ? null : binaryValue.getValue();

//          try {
//            String path;
//            if(valueRef.toString().startsWith("{")) {
//              JSONObject properties = new JSONObject(valueRef.toString());
//              path = properties.getString("path");
//            } else {
//              path = valueRef.toString();
//            }
//            log.info("Loading binary from path: {}", path);
//            value = BinaryValueFileHelper.readValue(tableRoot, path);
//          } catch(JSONException e) {
//            log.error("Failed loading JSON binary value reference", e);
//            throw new MagmaRuntimeException(e);
//          }
        }
      }
      return value;
    }

  }

}
