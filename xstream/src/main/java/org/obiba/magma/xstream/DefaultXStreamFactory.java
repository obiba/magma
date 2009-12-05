package org.obiba.magma.xstream;

import org.obiba.magma.DatasourceMetaData;
import org.obiba.magma.ValueType;
import org.obiba.magma.xstream.converter.AttributeConverter;
import org.obiba.magma.xstream.converter.CategoryConverter;
import org.obiba.magma.xstream.converter.ValueConverter;
import org.obiba.magma.xstream.converter.ValueSequenceConverter;
import org.obiba.magma.xstream.converter.VariableConverter;
import org.obiba.magma.xstream.mapper.MagmaMapper;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.mapper.MapperWrapper;

public class DefaultXStreamFactory implements XStreamFactory {

  @Override
  public XStream createXStream() {
    XStream xstream = new XStream() {
      @Override
      protected MapperWrapper wrapMapper(MapperWrapper next) {
        return new MagmaMapper(next);
      }
    };

    xstream.alias("datasourceMetaData", DatasourceMetaData.class);
    xstream.registerConverter(new VariableConverter(xstream.getMapper()));
    xstream.registerConverter(new CategoryConverter(xstream.getMapper()));
    xstream.registerConverter(new AttributeConverter());
    xstream.registerConverter(new ValueConverter());
    xstream.registerConverter(new ValueSequenceConverter());
    xstream.useAttributeFor(ValueType.class);
    xstream.setMode(XStream.NO_REFERENCES);

    xstream.processAnnotations(XStreamValueSet.class);
    xstream.processAnnotations(XStreamValueSetValue.class);
    return xstream;
  }

}
