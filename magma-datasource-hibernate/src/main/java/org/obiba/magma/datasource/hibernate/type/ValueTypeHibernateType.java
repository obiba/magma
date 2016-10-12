/*
 * Copyright (c) 2016 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.magma.datasource.hibernate.type;

import java.io.Reader;
import java.sql.Clob;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

import org.hibernate.HibernateException;
import org.hibernate.dialect.Dialect;
import org.hibernate.engine.spi.SessionImplementor;
import org.hibernate.type.AbstractStandardBasicType;
import org.hibernate.type.DiscriminatorType;
import org.hibernate.type.descriptor.WrapperOptions;
import org.hibernate.type.descriptor.java.AbstractTypeDescriptor;
import org.hibernate.type.descriptor.java.DataHelper;
import org.hibernate.type.descriptor.sql.VarcharTypeDescriptor;
import org.obiba.magma.ValueType;

/**
 * String representation of {@link ValueType}.
 */
public class ValueTypeHibernateType extends AbstractStandardBasicType<ValueType>
    implements DiscriminatorType<ValueType> {

  private static final long serialVersionUID = 1L;

  public ValueTypeHibernateType() {
    super(VarcharTypeDescriptor.INSTANCE, ValueTypeDescriptor.INSTANCE);
  }

  @Override
  public ValueType stringToObject(String xml) throws Exception {
    return ValueType.Factory.forName(xml);
  }

  @Override
  public ValueType fromStringValue(String xml) throws HibernateException {
    return ValueType.Factory.forName(xml);
  }

  @Override
  public Object get(ResultSet rs, String name, SessionImplementor session) throws HibernateException, SQLException {
    return ValueType.Factory.forName(rs.getString(name));
  }

  @Override
  public void set(PreparedStatement st, ValueType value, int index, SessionImplementor session)
      throws HibernateException, SQLException {
    st.setString(index, toString(value));
  }

  @Override
  public String toString(ValueType value) throws HibernateException {
    return value.getName();
  }

  @Override
  public String getName() {
    return "value_type";
  }

  @Override
  public void nullSafeSet(PreparedStatement st, Object value, int index, boolean[] settable, SessionImplementor session)
      throws HibernateException, SQLException {
    if(value == null) {
      st.setNull(index, Types.VARCHAR);
    } else {
      set(st, (ValueType) value, index, session);
    }
  }

  @Override
  public String objectToSQLString(ValueType value, Dialect dialect) throws Exception {
    return '\'' + toString(value) + '\'';
  }

  @SuppressWarnings("Singleton")
  private static class ValueTypeDescriptor extends AbstractTypeDescriptor<ValueType> {

    private static final long serialVersionUID = 518391863123949881L;

    @SuppressWarnings("TypeMayBeWeakened")
    private static final ValueTypeDescriptor INSTANCE = new ValueTypeDescriptor();

    private ValueTypeDescriptor() {
      super(ValueType.class);
    }

    @Override
    public String toString(ValueType value) {
      return value.toString();
    }

    @Override
    public ValueType fromString(String string) {
      return ValueType.Factory.forName(string);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <X> X unwrap(ValueType value, Class<X> type, WrapperOptions options) {
      if(value == null) {
        return null;
      }
      if(ValueType.class.isAssignableFrom(type)) {
        return (X) value;
      }
      throw unknownUnwrap(type);
    }

    @Override
    public <X> ValueType wrap(X value, WrapperOptions options) {
      if(value == null) {
        return null;
      }
      if(String.class.isInstance(value)) {
        return ValueType.Factory.forName((String) value);
      }
      if(Reader.class.isInstance(value)) {
        return ValueType.Factory.forName(DataHelper.extractString((Reader) value));
      }
      if(Clob.class.isInstance(value)) {
        return ValueType.Factory.forName(DataHelper.extractString((Clob) value));
      }
      throw unknownWrap(value.getClass());
    }
  }

}
