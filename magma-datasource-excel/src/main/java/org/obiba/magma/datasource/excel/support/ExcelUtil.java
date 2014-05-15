package org.obiba.magma.datasource.excel.support;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nullable;
import javax.validation.constraints.NotNull;

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

  private static final int MAX_CELL_VALUE = 32767;

  private ExcelUtil() {}

  @SuppressWarnings({ "PMD.NcssMethodCount", "ConstantConditions" })
  public static void setCellValue(Cell cell, Value value) {
    if(value.isNull()) {
      return;
    }

    ValueType valueType = value.getValueType();

    String valueStr = value.toString();
    if(value.isSequence()) {
      setCellValue(cell, valueType, valueStr);
      return;
    }

    String valueTypeName = valueType.getName();

    //noinspection IfStatementWithTooManyBranches
    if(valueTypeName.equals(BinaryType.get().getName())) {
      setCellValue(cell, valueType, valueStr);
    } else if(valueTypeName.equals(BooleanType.get().getName())) {
      setCellValue(cell, valueType, (Boolean) value.getValue());
    } else if(valueTypeName.equals(DateTimeType.get().getName())) {
      setCellValue(cell, valueType, valueStr);
    } else if(valueTypeName.equals(DecimalType.get().getName())) {
      setCellValue(cell, valueType, (Double) value.getValue());
    } else if(valueTypeName.equals(IntegerType.get().getName())) {
      setCellValue(cell, valueType, (Long) value.getValue());
    } else if(valueTypeName.equals(LocaleType.get().getName()) || valueTypeName.equals(TextType.get().getName())) {
      setCellValue(cell, valueType, valueStr);
    }
  }

  public static void setCellValue(@NotNull Cell cell, ValueType valueType, boolean value) {
    if(valueType.getName().equals(BooleanType.get().getName())) {
      cell.setCellValue(value ? "1" : "0");
    } else {
      cell.setCellValue(value);
    }
  }

  public static void setCellValue(@NotNull Cell cell, ValueType valueType, Double value) {
    cell.setCellValue(value);
  }

  public static void setCellValue(@NotNull Cell cell, ValueType valueType, Long value) {
    cell.setCellValue(value);
  }

  public static void setCellValue(@NotNull Cell cell, ValueType valueType, @Nullable String value) {
    if(cell == null) throw new IllegalArgumentException("Cell cannot be null before setting a value");
    String validated = value;
    if(value != null && value.length() > MAX_CELL_VALUE) {
      validated = "WARN: Value to large for Excel.";
    }
    cell.setCellValue(validated);
  }

  @SuppressWarnings({ "OverlyLongMethod", "PMD.NcssMethodCount" })
  public static String getCellValueAsString(@Nullable Cell cell) {
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
   *
   * @param headers as defined by the user
   * @param header
   * @return null if not found
   */
  @Nullable
  public static String findNormalizedHeader(Iterable<String> headers, String header) {
    String normalized = normalizeHeader(header);
    for(String userHeader : headers) {
      if(normalizeHeader(userHeader).equals(normalized)) {
        return userHeader;
      }
    }
    return null;
  }

  /**
   * Allow for instance "Value Type" or "value_type" or "Value-Type" or "valuetype" for "valueType".
   *
   * @param userHeader
   * @return
   */
  private static String normalizeHeader(String userHeader) {
    String cached = cachedNormalizedHeaders.get(userHeader);
    if(cached != null) {
      return cached;
    } else {
      StringBuilder sb = new StringBuilder();
      for(int i = 0; i < userHeader.length(); i++) {
        char c = userHeader.charAt(i);
        if(c != ' ' && c != '_' && c != '-') {
          sb.append(c);
        }
      }
      String h = sb.toString().toLowerCase();
      cachedNormalizedHeaders.put(userHeader, h);
      return h;
    }
  }

  private static final Map<String, String> cachedNormalizedHeaders = new HashMap<>();
}
