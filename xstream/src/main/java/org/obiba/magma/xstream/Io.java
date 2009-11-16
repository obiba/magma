package org.obiba.magma.xstream;

import java.io.OutputStream;

import org.obiba.magma.Collection;
import org.obiba.magma.Occurrence;
import org.obiba.magma.ValueType;
import org.obiba.magma.VariableEntity;
import org.obiba.magma.VariableValueSource;
import org.obiba.magma.support.ValueSetBean;
import org.obiba.magma.xstream.converter.AttributeConverter;
import org.obiba.magma.xstream.converter.CategoryConverter;
import org.obiba.magma.xstream.converter.ValueConverter;
import org.obiba.magma.xstream.converter.VariableConverter;
import org.obiba.magma.xstream.mapper.MagmaMapper;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.mapper.MapperWrapper;

public class Io {

  public void write(Collection collection, OutputStream os) {
    XStream xstream = new XStream() {
      @Override
      protected MapperWrapper wrapMapper(MapperWrapper next) {
        return new MagmaMapper(next);
      }
    };
    xstream.registerConverter(new VariableConverter(xstream.getMapper()));
    xstream.registerConverter(new CategoryConverter(xstream.getMapper()));
    xstream.registerConverter(new AttributeConverter());
    xstream.registerConverter(new ValueConverter());
    xstream.useAttributeFor(ValueType.class);

    xstream.omitField(ValueSetBean.class, "collection");
    xstream.processAnnotations(XStreamValueSet.class);
    xstream.processAnnotations(XStreamValueSetValue.class);
    xstream.toXML(collection.getVariables(), os);

    for(String entityType : collection.getEntityTypes()) {
      for(VariableEntity entity : collection.getEntities(entityType)) {
        XStreamValueSet valueSet = new XStreamValueSet(collection.loadValueSet(entity));
        for(VariableValueSource source : collection.getVariableValueSources(entityType)) {
          if(source.getVariable().isRepeatable()) {
            for(Occurrence occurrence : collection.loadOccurrences(valueSet, source.getVariable())) {
              valueSet.addValue(occurrence, source);
            }

          } else {
            valueSet.addValue(source);
          }
        }
        xstream.toXML(valueSet, os);
      }
    }
  }
}
