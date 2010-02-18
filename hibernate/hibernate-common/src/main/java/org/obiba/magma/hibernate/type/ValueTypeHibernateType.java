package org.obiba.magma.hibernate.type;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

import org.hibernate.HibernateException;
import org.hibernate.dialect.Dialect;
import org.hibernate.type.DiscriminatorType;
import org.hibernate.type.ImmutableType;
import org.obiba.magma.ValueType;

/**
 * String representation of {@link ValueType}.
 * 
 */
public class ValueTypeHibernateType extends ImmutableType implements DiscriminatorType {

  private static final long serialVersionUID = 1L;

  public Object stringToObject(String xml) throws Exception {
    return ValueType.Factory.forName(xml);
  }

  @Override
  public Object fromStringValue(String xml) throws HibernateException {
    return ValueType.Factory.forName(xml);
  }

  @Override
  public Object get(ResultSet rs, String name) throws HibernateException, SQLException {
    return ValueType.Factory.forName(rs.getString(name));
  }

  @Override
  public void set(PreparedStatement st, Object value, int index) throws HibernateException, SQLException {
    st.setString(index, toString(value));
  }

  @Override
  public int sqlType() {
    return Types.VARCHAR;
  }

  @Override
  public String toString(Object value) throws HibernateException {
    return ((ValueType) value).getName();
  }

  public String getName() {
    return "value_type";
  }

  public Class<?> getReturnedClass() {
    return ValueType.class;
  }

  public String objectToSQLString(Object value, Dialect dialect) throws Exception {
    return '\'' + toString(value) + '\'';
  }

}
