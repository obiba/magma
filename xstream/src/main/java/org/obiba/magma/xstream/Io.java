package org.obiba.magma.xstream;

import java.io.OutputStream;

import org.obiba.magma.Collection;
import org.obiba.magma.ValueType;
import org.obiba.magma.VariableEntity;
import org.obiba.magma.VariableValueSource;
import org.obiba.magma.support.ValueSetBean;
import org.obiba.magma.xstream.converter.AttributeConverter;
import org.obiba.magma.xstream.converter.CategoryConverter;
import org.obiba.magma.xstream.converter.ValueConverter;
import org.obiba.magma.xstream.converter.ValueSequenceConverter;
import org.obiba.magma.xstream.converter.VariableConverter;
import org.obiba.magma.xstream.mapper.MagmaMapper;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.mapper.MapperWrapper;

public class Io {

  private XStream xstream;

  public Io() {
    xstream = new XStream() {
      @Override
      protected MapperWrapper wrapMapper(MapperWrapper next) {
        return new MagmaMapper(next);
      }
    };
    xstream.registerConverter(new VariableConverter(xstream.getMapper()));
    xstream.registerConverter(new CategoryConverter(xstream.getMapper()));
    xstream.registerConverter(new AttributeConverter());
    xstream.registerConverter(new ValueConverter());
    xstream.registerConverter(new ValueSequenceConverter());
    xstream.useAttributeFor(ValueType.class);
    xstream.setMode(XStream.NO_REFERENCES);

    xstream.omitField(ValueSetBean.class, "collection");
    xstream.processAnnotations(XStreamValueSet.class);
    xstream.processAnnotations(XStreamValueSetValue.class);
  }

  public void writeVariables(Collection collection, OutputStream os) {
    for(String entityType : collection.getEntityTypes()) {
      xstream.toXML(collection.getVariables(entityType), os);
    }
  }

  public void writeEntities(Collection collection, OutputStream os) {

    for(String entityType : collection.getEntityTypes()) {
      for(VariableEntity entity : collection.getEntities(entityType)) {
        long start = System.currentTimeMillis();
        XStreamValueSet valueSet = new XStreamValueSet(collection.loadValueSet(entity));
        for(VariableValueSource source : collection.getVariableValueSources(entityType)) {
          try {
            valueSet.addValue(source);
          } catch(RuntimeException e) {
            System.err.println(e.getMessage());
          }
        }
        xstream.toXML(valueSet, os);
        System.err.println("Serialized entity in " + (System.currentTimeMillis() - start) / 1000 + "s");
      }
    }
  }

}
