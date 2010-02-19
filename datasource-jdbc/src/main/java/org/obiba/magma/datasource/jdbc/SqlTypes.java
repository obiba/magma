package org.obiba.magma.datasource.jdbc;

import org.obiba.magma.MagmaRuntimeException;
import org.obiba.magma.ValueType;
import org.obiba.magma.type.BinaryType;
import org.obiba.magma.type.BooleanType;
import org.obiba.magma.type.DateTimeType;
import org.obiba.magma.type.DecimalType;
import org.obiba.magma.type.IntegerType;
import org.obiba.magma.type.TextType;

class SqlTypes {
  static final ValueType valueTypeFor(int sqlType) {
    switch(sqlType) {
    // BinaryType
    case java.sql.Types.BLOB: // fall through
    case java.sql.Types.LONGVARBINARY: // fall through
    case java.sql.Types.VARBINARY:
      return BinaryType.get();

      // BooleanType
    case java.sql.Types.BIT: // fall through
    case java.sql.Types.BOOLEAN:
      return BooleanType.get();

      // DecimalType
    case java.sql.Types.DECIMAL: // fall through
    case java.sql.Types.DOUBLE: // fall through
    case java.sql.Types.FLOAT: // fall through
    case java.sql.Types.NUMERIC: // fall through
    case java.sql.Types.REAL:
      return DecimalType.get();

      // DateTimeType
    case java.sql.Types.TIMESTAMP:
      return DateTimeType.get();

      // IntegerType
    case java.sql.Types.BIGINT: // fall through
    case java.sql.Types.INTEGER: // fall through
    case java.sql.Types.SMALLINT: // fall through
    case java.sql.Types.TINYINT:
      return IntegerType.get();

      // Everything else is mapped to TextType. Maybe this is not a correct approach, not every remaining type may map
      // to this.
    default:
      return TextType.get();
    }
  }

  static final ValueType valueTypeFor(String sqlType) {
    if(sqlType.equals("VARCHAR")) {
      return TextType.get();
    }
    if(sqlType.equals("INTEGER")) {
      return IntegerType.get();
    }
    if(sqlType.equals("DECIMAL")) {
      return DecimalType.get();
    }
    // if(sqlType.equals("DATE")) {
    // return DateType.get();
    // }
    if(sqlType.equals("TIMESTAMP")) {
      return DateTimeType.get();
    }
    if(sqlType.equals("BLOB")) {
      return BinaryType.get();
    }

    return TextType.get();
  }

  static final String sqlTypeFor(ValueType valueType) {
    if(valueType.getName().equals(TextType.get().getName())) {
      return "VARCHAR";
    }
    if(valueType.getName().equals(IntegerType.get().getName())) {
      return "INTEGER";
    }
    if(valueType.getName().equals(DecimalType.get().getName())) {
      return "DECIMAL";
    }
    // if(valueType.getName().equals(DateType.get().getName())) {
    // return "DATE";
    // }
    if(valueType.getName().equals(DateTimeType.get().getName())) {
      return "TIMESTAMP";
    }
    if(valueType.getName().equals(BinaryType.get().getName())) {
      return "BLOB";
    }
    if(valueType.getName().equals(BooleanType.get().getName())) {
      return "BOOLEAN";
    }

    throw new MagmaRuntimeException("no sql type for " + valueType);
  }
}
