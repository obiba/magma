package org.obiba.magma.datasource.excel.support;

import java.util.Locale;

import org.apache.poi.ss.usermodel.Cell;
import org.obiba.magma.Value;
import org.obiba.magma.ValueType;
import org.obiba.magma.type.BinaryType;
import org.obiba.magma.type.BooleanType;
import org.obiba.magma.type.DateTimeType;
import org.obiba.magma.type.DecimalType;
import org.obiba.magma.type.IntegerType;
import org.obiba.magma.type.LocaleType;
import org.obiba.magma.type.TextType;

public class ExcelUtil {

  public static void setCellValue(Cell cell, Value value) {
    ValueType valueType = value.getValueType();

    if(value.isNull()) {
      return;
    }

    if(valueType.getName().equals(BinaryType.get().getName())) {
      setCellValue(cell, valueType, BinaryType.get().toString(value));
    } else if(valueType.getName().equals(BooleanType.get().getName())) {
      setCellValue(cell, valueType, (Boolean) value.getValue());
    } else if(valueType.getName().equals(DateTimeType.get().getName())) {
      String formattedDateTime = DateTimeType.get().toString(value);
      setCellValue(cell, valueType, formattedDateTime);
    } else if(valueType.getName().equals(DecimalType.get().getName())) {
      setCellValue(cell, valueType, (Double) value.getValue());
    } else if(valueType.getName().equals(IntegerType.get().getName())) {
      setCellValue(cell, valueType, (Long) value.getValue());
    } else if(valueType.getName().equals(LocaleType.get().getName())) {
      setCellValue(cell, valueType, ((Locale) value.getValue()).toString());
    } else if(valueType.getName().equals(TextType.get().getName())) {
      setCellValue(cell, valueType, (String) value.getValue());
    }
  }

  public static void setCellValue(Cell cell, ValueType valueType, boolean value) {
    if(valueType.getName().equals(BooleanType.get().getName())) {
      cell.setCellValue(value ? "1" : "0");
    } else {
      cell.setCellValue(value);
    }
  }

  public static void setCellValue(Cell cell, ValueType valueType, Double value) {
    cell.setCellValue(value);
  }

  public static void setCellValue(Cell cell, ValueType valueType, Long value) {
    cell.setCellValue(value);
  }

  public static void setCellValue(Cell cell, ValueType valueType, String value) {
    cell.setCellValue(value);
  }

}
