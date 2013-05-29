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

import javax.annotation.Nonnull;

import org.hibernate.Query;
import org.hibernate.SessionFactory;
import org.hibernate.classic.Session;
import org.obiba.core.service.impl.hibernate.AssociationCriteria;
import org.obiba.magma.MagmaRuntimeException;
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

  private final Serializable variableId;

  private final Serializable valueSetId;

  public HibernateValueLoaderFactory(SessionFactory sessionFactory, Serializable variableId, Serializable valueSetId) {
    this.sessionFactory = sessionFactory;
    this.variableId = variableId;
    this.valueSetId = valueSetId;
  }

  public HibernateValueLoaderFactory(SessionFactory sessionFactory, ValueSetValue valueSetValue) {
    this(sessionFactory, valueSetValue.getVariable().getId(), valueSetValue.getValueSet().getId());
  }

  @Override
  public ValueLoader create(Value valueRef, Integer occurrence) {
    return new HibernateBinaryValueLoader(sessionFactory, variableId, valueSetId, occurrence, valueRef);
  }

  private static final class HibernateBinaryValueLoader implements ValueLoader, Serializable {

    private static final long serialVersionUID = -1878370804810810064L;

    private static final Logger log = LoggerFactory.getLogger(HibernateBinaryValueLoader.class);

    private final SessionFactory sessionFactory;

    private final Serializable variableId;

    private final Serializable valueSetId;

    private final int occurrence;

    private final Value valueRef;

    private byte[] value;

    private HibernateBinaryValueLoader(SessionFactory sessionFactory, Serializable variableId, Serializable valueSetId,
        Integer occurrence, Value valueRef) {
      this.sessionFactory = sessionFactory;
      this.variableId = variableId;
      this.valueSetId = valueSetId;
      this.occurrence = occurrence == null ? 0 : occurrence;
      this.valueRef = valueRef;
    }

    @Override
    public boolean isNull() {
      return valueRef == null || valueRef.isNull();
    }

    @Nonnull
    @Override
    @SuppressWarnings("ConstantConditions")
    @edu.umd.cs.findbugs.annotations.SuppressWarnings("NP_NULL_ON_SOME_PATH")
    public Object getValue() {
      if(value == null) {
        if(BinaryType.get().equals(valueRef.getValueType())) {
          log.trace("Loading binary from value_set_value table (Base64)");
          value = (byte[]) valueRef.getValue();
        } else {
          log.trace("Loading binary from value_set_binary_value table");
          Session session = sessionFactory.getCurrentSession();
          ValueSetBinaryValue binaryValue = (ValueSetBinaryValue) AssociationCriteria
              .create(ValueSetBinaryValue.class, session) //
              .add("variable.id", Operation.eq, variableId) //
              .add("valueSet.id", Operation.eq, valueSetId) //
              .add("occurrence", Operation.eq, occurrence) //
              .getCriteria().uniqueResult();
          if(binaryValue == null) {
            throw new MagmaRuntimeException(
                "Cannot find binary value for variable[" + variableId + "], valueSet[" + valueSetId +
                    "] and occurrence[" + occurrence + "]");
          }
          value = binaryValue.getValue();
          session.evict(binaryValue); // remove binaries from session to avoid filling up memory
        }
      }
      return value;
    }

    @Override
    public long getLength() {
      if(BinaryType.get().equals(valueRef.getValueType())) {
        log.trace("Loading binary from value_set_value table (Base64)");
        value = (byte[]) valueRef.getValue();
        return value == null ? 0 : value.length;
      }
      Query query = sessionFactory.getCurrentSession()
          .createSQLQuery("SELECT binaryValue.size FROM value_set_binary_value binaryValue " + //
              "WHERE binaryValue.variable_id = :variable_id " + //
              "AND binaryValue.value_set_id = :value_set_id " + //
              "AND binaryValue.occurrence = :occurrence") //
          .setParameter("variable_id", variableId) //
          .setParameter("value_set_id", valueSetId) //
          .setParameter("occurrence", occurrence);
      Integer result = (Integer) query.uniqueResult();
      return result == null ? 0 : result.longValue();
    }

  }

}
