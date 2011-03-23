package org.obiba.magma.xstream;

import java.util.List;

import org.obiba.magma.ValueType;
import org.obiba.magma.xstream.converter.AttributeConverter;
import org.obiba.magma.xstream.converter.CategoryConverter;
import org.obiba.magma.xstream.converter.JoinTableConverter;
import org.obiba.magma.xstream.converter.ValueConverter;
import org.obiba.magma.xstream.converter.ValueSequenceConverter;
import org.obiba.magma.xstream.converter.ValueTableConverter;
import org.obiba.magma.xstream.converter.VariableConverter;
import org.obiba.magma.xstream.mapper.MagmaMapper;

import com.google.common.collect.Lists;
import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.reflection.ReflectionProvider;
import com.thoughtworks.xstream.mapper.MapperWrapper;

public class DefaultXStreamFactory implements XStreamFactory {

  private transient List<Converter> converters = Lists.newArrayList();

  @Override
  public XStream createXStream() {
    return createXStream(null);
  }

  @Override
  public XStream createXStream(ReflectionProvider reflectionProvider) {
    XStream xstream = null;
    if(reflectionProvider == null) {
      xstream = new XStream() {
        @Override
        protected MapperWrapper wrapMapper(MapperWrapper next) {
          return new MagmaMapper(next);
        }
      };
    } else {
      xstream = new XStream(reflectionProvider) {
        @Override
        protected MapperWrapper wrapMapper(MapperWrapper next) {
          return new MagmaMapper(next);
        }
      };
    }

    xstream.registerConverter(new VariableConverter(xstream.getMapper()));
    xstream.registerConverter(new CategoryConverter(xstream.getMapper()));
    xstream.registerConverter(new AttributeConverter());
    xstream.registerConverter(new ValueConverter());
    xstream.registerConverter(new ValueSequenceConverter());
    xstream.registerConverter(new ValueTableConverter());
    xstream.registerConverter(new JoinTableConverter());

    // Converters are prioritized in reverse order of their addition
    for(Converter converter : this.converters) {
      xstream.registerConverter(converter);
    }

    xstream.useAttributeFor(ValueType.class);
    xstream.setMode(XStream.NO_REFERENCES);

    xstream.processAnnotations(XStreamValueSet.class);
    xstream.processAnnotations(XStreamValueSetValue.class);
    return xstream;
  }

  @Override
  public void registerConverter(Converter converter) {
    converters.add(converter);
  }

}
