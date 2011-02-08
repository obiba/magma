package org.obiba.magma.xstream;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.reflection.ReflectionProvider;

public interface XStreamFactory {

  public XStream createXStream();

  public XStream createXStream(ReflectionProvider reflectionProvider);

  public void registerConverter(Converter converter);

}
