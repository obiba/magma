package org.obiba.magma.datasource.excel.support;

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
    if(value.isNull()) {
      return;
    }

    ValueType valueType = value.getValueType();

    if(value.isSequence()) {
      setCellValue(cell, valueType, value.toString());
      return;
    }

    if(valueType.getName().equals(BinaryType.get().getName())) {
      setCellValue(cell, valueType, value.toString());
    } else if(valueType.getName().equals(BooleanType.get().getName())) {
      setCellValue(cell, valueType, (Boolean) value.getValue());
    } else if(valueType.getName().equals(DateTimeType.get().getName())) {
      setCellValue(cell, valueType, value.toString());
    } else if(valueType.getName().equals(DecimalType.get().getName())) {
      setCellValue(cell, valueType, (Double) value.getValue());
    } else if(valueType.getName().equals(IntegerType.get().getName())) {
      setCellValue(cell, valueType, (Long) value.getValue());
    } else if(valueType.getName().equals(LocaleType.get().getName())) {
      setCellValue(cell, valueType, value.toString());
    } else if(valueType.getName().equals(TextType.get().getName())) {
      setCellValue(cell, valueType, value.toString());
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
    if(cell == null) throw new IllegalArgumentException("Cell cannot be null before setting a value");
    if(value != null && value.length() > 32767) {
      value = "WARN: Value to large for Excel.";
    }
    cell.setCellValue(value);
  }

  public static String getCellValueAsString(Cell cell) {
    String value = "";
    if(cell != null) {
      switch(cell.getCellType()) {
      case Cell.CELL_TYPE_STRING:
      case Cell.CELL_TYPE_BLANK:
        value = cell.getStringCellValue();
        break;
      case Cell.CELL_TYPE_BOOLEAN:
        value = String.valueOf(cell.getBooleanCellValue());
        break;
      case Cell.CELL_TYPE_ERROR:
        value = String.valueOf(cell.getErrorCellValue());
        break;
      case Cell.CELL_TYPE_FORMULA:
        value = String.valueOf(cell.getCellFormula());
        break;
      case Cell.CELL_TYPE_NUMERIC:
        value = String.valueOf(cell.getNumericCellValue());
        if(value.endsWith(".0")) {
          value = value.substring(0, value.length() - 2);
        }
        break;
      default:
        break;
      }
    }
    return value;
  }

  /**
   * Find in user headers the given magma excel header.
   * @param headers as defined by the user
   * @param header
   * @return null if not found
   */
  public static String findNormalizedHeader(final Iterable<String> headers, final String header) {
    for(String userHeader : headers) {
      if(normalizeHeader(userHeader).equalsIgnoreCase(normalizeHeader(header))) {
        return userHeader;
      }
    }
    return null;
  }

  /**
   * Allow for instance "Value Type" or "value_type" or "Value-Type" or "valuetype" for "valueType".
   * @param userHeader
   * @return
   */
  private static String normalizeHeader(String userHeader) {
    String h = userHeader.replace(" ", "");
    h = h.replace("_", "");
    h = h.replace("-", "");
    return h;
  }
}
