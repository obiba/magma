/*
 * Copyright (c) 2019 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.magma.datasource.hibernate.type;

import java.io.StringReader;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Map;

import javax.annotation.Nullable;

import org.dom4j.Node;
import org.hibernate.HibernateException;
import org.hibernate.MappingException;
import org.hibernate.engine.spi.Mapping;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.engine.spi.SessionImplementor;
import org.hibernate.metamodel.relational.Size;
import org.hibernate.type.AbstractType;
import org.obiba.magma.Value;
import org.obiba.magma.ValueType;

import com.google.common.base.Strings;

/**
 * A Hibernate Type for persisting {@code Value} instances. The strategy uses 3 columns:
 * <ul>
 * <li>value_type: stores the name of the ValueType</li>
 * <li>is_sequence: stores true when the {@code Value} is a {@code ValueSequence},false otherwise.</li>
 * <li>value: stores the value returned by {@code value.toString()}</li>
 * </ul>
 */
public class ValueHibernateType extends AbstractType {

  private static final long serialVersionUID = 1L;

  @Override
  public int getColumnSpan(Mapping mapping) throws MappingException {
    return 3;
  }

  @Override
  public String getName() {
    return "Value";
  }

  @Override
  public boolean isDirty(Object old, Object current, boolean[] checkable, SessionImplementor session)
      throws HibernateException {
    return !old.equals(current);
  }

  @Override
  public boolean isMutable() {
    // Value instances are immutable
    return false;
  }

  @Override
  public Object nullSafeGet(ResultSet rs, String name, SessionImplementor session, Object owner)
      throws HibernateException, SQLException {
    throw new UnsupportedOperationException();
  }

  @Nullable
  @Override
  public Object nullSafeGet(ResultSet rs, String[] names, SessionImplementor session, Object owner)
      throws HibernateException, SQLException {
    String valueTypeName = rs.getString(names[0]);
    // Even when the column is NOT NULL, a SELECT statement can return NULL (using a left join for example).
    // When this column is null, we cannot construct a valid {@code Value} instance, so this method returns null
    if(valueTypeName == null) {
      return null;
    }
    ValueType valueType = ValueType.Factory.forName(valueTypeName);
    boolean isSequence = rs.getBoolean(names[1]);
    String stringValue = rs.getString(names[2]);
    return isSequence ? valueType.sequenceOf(stringValue) : valueType.valueOf(stringValue);
  }

  @Override
  public void nullSafeSet(PreparedStatement st, Object obj, int index, boolean[] settable, SessionImplementor session)
      throws HibernateException, SQLException {
    Value value = (Value) obj;

    int offset = 0;
    if(settable[0]) {
      st.setString(index + offset++, value.getValueType().getName());
    }
    if(settable[1]) {
      st.setBoolean(index + offset++, value.isSequence());
    }
    if(settable[2]) {
      st.setString(index + offset, value.toString());
    }
  }

  @Override
  public void nullSafeSet(PreparedStatement st, Object obj, int index, SessionImplementor session)
      throws HibernateException, SQLException {
    Value value = (Value) obj;
    st.setString(index, value.getValueType().getName());
    st.setBoolean(index + 1, value.isSequence());
    String stringValue = Strings.nullToEmpty(value.isNull() ? null : value.toString());
    st.setClob(index + 2, new StringReader(stringValue), stringValue.length());
  }

  @Override
  public Class<?> getReturnedClass() {
    return Value.class;
  }

  @Override
  @SuppressWarnings({ "unchecked", "rawtypes" })
  public Object replace(Object original, Object target, SessionImplementor session, Object owner, Map copyCache)
      throws HibernateException {
    // It is safe to return the original parameter since Value instances are immutable
    return original;
  }

  @Override
  public Object fromXMLNode(Node xml, Mapping factory) throws HibernateException {
    throw new UnsupportedOperationException();
  }

  @Override
  public void setToXMLNode(Node node, Object value, SessionFactoryImplementor factory) throws HibernateException {
    throw new UnsupportedOperationException();
  }

  @Override
  public int[] sqlTypes(Mapping mapping) throws MappingException {
    return new int[] { Types.VARCHAR, Types.BIT, Types.CLOB };
  }

  @Override
  public boolean[] toColumnNullness(Object value, Mapping mapping) {
    return new boolean[] { false, false, false };
  }

  @Override
  public String toLoggableString(Object value, SessionFactoryImplementor factory) throws HibernateException {
    return value.toString();
  }

  @Override
  public Object deepCopy(Object value, SessionFactoryImplementor factory) throws HibernateException {
    if(value == null) return null;
    return ((Value) value).copy();
  }

  @Override
  public Size[] dictatedSizes(Mapping mapping) throws MappingException {
    return defaultSizes(mapping);
  }

  @Override
  public Size[] defaultSizes(Mapping mapping) throws MappingException {
    return new Size[] { //
        new Size(Size.DEFAULT_PRECISION, Size.DEFAULT_SCALE, Size.DEFAULT_LENGTH, Size.LobMultiplier.NONE), // 255
        new Size(Size.DEFAULT_PRECISION, Size.DEFAULT_SCALE, 1, Size.LobMultiplier.NONE), // 1
        new Size(Size.DEFAULT_PRECISION, Size.DEFAULT_SCALE, 1, Size.LobMultiplier.G) // 1GB
    };
  }

}
