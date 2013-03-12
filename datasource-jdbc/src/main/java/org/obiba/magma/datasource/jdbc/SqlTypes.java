package org.obiba.magma.datasource.jdbc;

import org.obiba.magma.MagmaRuntimeException;
import org.obiba.magma.ValueType;
import org.obiba.magma.type.BinaryType;
import org.obiba.magma.type.BooleanType;
import org.obiba.magma.type.DateTimeType;
import org.obiba.magma.type.DateType;
import org.obiba.magma.type.DecimalType;
import org.obiba.magma.type.IntegerType;
import org.obiba.magma.type.LocaleType;
import org.obiba.magma.type.TextType;

class SqlTypes {
  static final String TEXT_TYPE_HINT_MEDIUM = "MEDIUM";

  static final String TEXT_TYPE_HINT_LARGE = "LARGE";

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

      // DateType
      case java.sql.Types.DATE:
        return DateType.get();

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
    if(sqlType.equals("BIGINT")) {
      return IntegerType.get();
    }
    if(sqlType.equals("DECIMAL")) {
      return DecimalType.get();
    }
    if(sqlType.equals("DATE")) {
      return DateType.get();
    }
    if(sqlType.equals("TIMESTAMP")) {
      return DateTimeType.get();
    }
    if(sqlType.equals("BLOB")) {
      return BinaryType.get();
    }

    return TextType.get();
  }

  static final String sqlTypeFor(ValueType valueType) {
    return sqlTypeFor(valueType, null);
  }

  static final String sqlTypeFor(ValueType valueType, String hint) {
    if(valueType.equals(TextType.get())) {
      // TODO: Formalize the notion of a "hint".
      if(TEXT_TYPE_HINT_MEDIUM.equals(hint)) {
        return "LONGVARCHAR"; // ONYX-285
      } else if(TEXT_TYPE_HINT_LARGE.equals(hint)) {
        return "LONGVARCHAR";
      } else {
        return "VARCHAR";
      }
    }
    if(valueType.equals(IntegerType.get())) {
      return "BIGINT";
    }
    if(valueType.equals(DecimalType.get())) {
      return "DOUBLE";
    }
    if(valueType.equals(DateType.get())) {
      return "DATE";
    }
    if(valueType.equals(DateTimeType.get())) {
      return "TIMESTAMP";
    }
    if(valueType.equals(BinaryType.get())) {
      return "BLOB";
    }
    if(valueType.equals(BooleanType.get())) {
      return "BOOLEAN";
    }
    if(valueType.equals(LocaleType.get())) {
      return "VARCHAR(255)";
    }

    throw new MagmaRuntimeException("no sql type for " + valueType);
  }

}
