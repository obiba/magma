package org.obiba.magma.hibernate.type;

import java.io.StringReader;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Map;

import org.dom4j.Node;
import org.hibernate.EntityMode;
import org.hibernate.HibernateException;
import org.hibernate.MappingException;
import org.hibernate.engine.Mapping;
import org.hibernate.engine.SessionFactoryImplementor;
import org.hibernate.engine.SessionImplementor;
import org.hibernate.type.AbstractType;
import org.obiba.magma.Value;
import org.obiba.magma.ValueType;

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
  public boolean isDirty(Object old, Object current, boolean[] checkable,
      SessionImplementor session) throws HibernateException {
    return old.equals(current) == false;
  }

  @Override
  public boolean isMutable() {
    // Value instances are immutable
    return false;
  }

  @Override
  public Object nullSafeGet(ResultSet rs, String name, SessionImplementor session,
      Object owner) throws HibernateException, SQLException {
    throw new UnsupportedOperationException();
  }

  @Override
  public Object nullSafeGet(ResultSet rs, String[] names, SessionImplementor session,
      Object owner) throws HibernateException, SQLException {
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
  public void nullSafeSet(PreparedStatement st, Object obj, int index, boolean[] settable,
      SessionImplementor session) throws HibernateException, SQLException {
    Value value = (Value) obj;

    int offset = 0;
    if(settable[0]) {
      st.setString(index + offset++, value.getValueType().getName());
    }
    if(settable[1]) {
      st.setBoolean(index + offset++, value.isSequence());
    }
    if(settable[2]) {
      st.setString(index + offset++, value.toString());
    }
  }

  @Override
  public void nullSafeSet(PreparedStatement st, Object obj, int index,
      SessionImplementor session) throws HibernateException, SQLException {
    Value value = (Value) obj;
    st.setString(index, value.getValueType().getName());
    st.setBoolean(index + 1, value.isSequence());
    String stringValue = value.toString();
    st.setClob(index + 2, new StringReader(stringValue), stringValue.length());
  }

  @Override
  public Object deepCopy(Object value, EntityMode entityMode,
      SessionFactoryImplementor factory) throws HibernateException {
    return ((Value) value).copy();
  }

  @Override
  public Class<?> getReturnedClass() {
    return Value.class;
  }

  @Override
  @SuppressWarnings({"unchecked", "rawtypes"})
  public Object replace(Object original, Object target, SessionImplementor session, Object owner,
      Map copyCache) throws HibernateException {
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
    return new int[] {Types.VARCHAR, Types.BIT, Types.CLOB};
  }

  @Override
  public boolean[] toColumnNullness(Object value, Mapping mapping) {
    return new boolean[] {false, false, false};
  }

  @Override
  public String toLoggableString(Object value, SessionFactoryImplementor factory) throws HibernateException {
    return value.toString();
  }

}
