package org.obiba.meta.xstream;

import java.io.OutputStream;

import org.obiba.meta.Collection;
import org.obiba.meta.Occurrence;
import org.obiba.meta.ValueType;
import org.obiba.meta.VariableEntity;
import org.obiba.meta.VariableValueSource;
import org.obiba.meta.support.ValueSetBean;
import org.obiba.meta.xstream.converter.AttributeConverter;
import org.obiba.meta.xstream.converter.CategoryConverter;
import org.obiba.meta.xstream.converter.ValueConverter;
import org.obiba.meta.xstream.converter.VariableConverter;
import org.obiba.meta.xstream.mapper.MetaMapper;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.mapper.MapperWrapper;

public class Io {

  public void write(Collection collection, OutputStream os) {
    XStream xstream = new XStream() {
      @Override
      protected MapperWrapper wrapMapper(MapperWrapper next) {
        return new MetaMapper(next);
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
            for(Occurrence occurrence : valueSet.connect().loadOccurrences(source.getVariable())) {
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
