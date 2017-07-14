package org.obiba.magma.presto;

import com.facebook.presto.spi.type.DoubleType;
import com.facebook.presto.spi.type.Type;
import com.facebook.presto.spi.type.VarbinaryType;
import com.facebook.presto.spi.type.VarcharType;
import org.obiba.magma.ValueType;
import org.obiba.magma.type.*;

public class TypeConverter {

  public static Type convert(ValueType type) {
    if (TextType.get().equals(type))
      return VarcharType.createUnboundedVarcharType();
    if (IntegerType.get().equals(type))
      return com.facebook.presto.spi.type.IntegerType.INTEGER;
    if (DecimalType.get().equals(type))
      return DoubleType.DOUBLE;
    if (BooleanType.get().equals(type))
      return com.facebook.presto.spi.type.BooleanType.BOOLEAN;
    if (BinaryType.get().equals(type))
      return VarbinaryType.VARBINARY;
    if (DateType.get().equals(type) || DateTimeType.get().equals(type))
      return com.facebook.presto.spi.type.DateType.DATE;
    return VarcharType.createUnboundedVarcharType();
  }

}
