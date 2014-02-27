package org.obiba.magma.datasource.spss.support;

import org.opendatafoundation.data.spss.SPSSStringVariable;
import org.opendatafoundation.data.spss.SPSSVariable;

import com.google.common.base.Strings;

public class SpssVariableValueConverter {

  private SpssVariableValueConverter() {}

  public static String convert(SPSSVariable spssVariable, String value) throws SpssValueConversionException {
    String trimmedValue = value.trim();
    if(Strings.isNullOrEmpty(trimmedValue) || (spssVariable instanceof SPSSStringVariable)) return value;

    switch(SpssVariableTypeMapper.getSpssNumericDataType(spssVariable)) {
      case ADATE:
        return new SpssDateValueConverters.ADateValueConverter().convert(trimmedValue);
      case DATE:
        return new SpssDateValueConverters.DateValueConverter().convert(trimmedValue);
      case DATETIME:
        return new SpssDateValueConverters.DateTimeValueConverter().convert(trimmedValue);
      default:
        return value;
    }
  }

}
