/*
 * Copyright (c) 2017 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.magma.datasource.jdbc;

import java.sql.Types;

import javax.annotation.Nullable;

import org.obiba.magma.MagmaRuntimeException;
import org.obiba.magma.ValueType;
import org.obiba.magma.type.BinaryType;
import org.obiba.magma.type.BooleanType;
import org.obiba.magma.type.DateTimeType;
import org.obiba.magma.type.DateType;
import org.obiba.magma.type.DecimalType;
import org.obiba.magma.type.IntegerType;
import org.obiba.magma.type.LineStringType;
import org.obiba.magma.type.LocaleType;
import org.obiba.magma.type.PointType;
import org.obiba.magma.type.PolygonType;
import org.obiba.magma.type.TextType;

class SqlTypes {
  static final String TEXT_TYPE_HINT_MEDIUM = "MEDIUM";

  static final String TEXT_TYPE_HINT_LARGE = "LARGE";

  private SqlTypes() {}

  @SuppressWarnings({ "PMD.NcssMethodCount", "OverlyLongMethod" })
  static ValueType valueTypeFor(int sqlType) {
    switch(sqlType) {
      // BinaryType
      case Types.BLOB: // fall through
      case Types.LONGVARBINARY: // fall through
      case Types.VARBINARY:
      case Types.BINARY:
        return BinaryType.get();

      // BooleanType
      case Types.BIT: // fall through
      case Types.BOOLEAN:
        return BooleanType.get();

      // DecimalType
      case Types.DECIMAL: // fall through
      case Types.DOUBLE: // fall through
      case Types.FLOAT: // fall through
      case Types.NUMERIC: // fall through
      case Types.REAL:
        return DecimalType.get();

      // DateType
      case Types.DATE:
        return DateType.get();

      // DateTimeType
      case Types.TIMESTAMP:
        return DateTimeType.get();

      // IntegerType
      case Types.BIGINT: // fall through
      case Types.INTEGER: // fall through
      case Types.SMALLINT: // fall through
      case Types.TINYINT:
        return IntegerType.get();

      // Everything else is mapped to TextType. Maybe this is not a correct approach, not every remaining type may map
      // to this.
      default:
        return TextType.get();
    }
  }

  @SuppressWarnings({ "PMD.NcssMethodCount", "OverlyLongMethod" })
  static String sqlTypeFor(ValueType valueType, @Nullable String hint) {
    if(valueType.equals(TextType.get())) {
      // TODO: Formalize the notion of a "hint".
      if(TEXT_TYPE_HINT_MEDIUM.equals(hint)) {
        return "LONGVARCHAR"; // ONYX-285
      }

      if(TEXT_TYPE_HINT_LARGE.equals(hint)) {
        return "LONGVARCHAR";
      }

      return "VARCHAR";
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
    if(valueType.equals(PointType.get())) {
      return "LONGVARCHAR";
    }
    if(valueType.equals(LineStringType.get())) {
      return "LONGVARCHAR";
    }
    if(valueType.equals(PolygonType.get())) {
      return "LONGVARCHAR";
    }

    throw new MagmaRuntimeException("No sql type for " + valueType);
  }

}
