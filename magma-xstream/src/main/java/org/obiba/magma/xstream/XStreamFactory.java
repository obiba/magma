package org.obiba.magma.xstream;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.reflection.ReflectionProvider;

public interface XStreamFactory {

  XStream createXStream();

  XStream createXStream(ReflectionProvider reflectionProvider);

  void registerConverter(Converter converter);

}
