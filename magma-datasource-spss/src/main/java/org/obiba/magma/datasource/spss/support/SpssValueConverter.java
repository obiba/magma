package org.obiba.magma.datasource.spss.support;

public interface SpssValueConverter {
  String convert(String value) throws SpssValueConversionException;
}
