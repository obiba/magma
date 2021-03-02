/*
 * Copyright (c) 2019 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.magma.xstream;

import com.google.common.collect.Lists;
import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.reflection.ReflectionProvider;
import com.thoughtworks.xstream.mapper.MapperWrapper;
import org.obiba.magma.ValueType;
import org.obiba.magma.xstream.converter.*;
import org.obiba.magma.xstream.mapper.MagmaMapper;

import java.util.List;

public class DefaultXStreamFactory implements XStreamFactory {

  private final transient List<Converter> converters = Lists.newArrayList();

  @Override
  public XStream createXStream() {
    return createXStream(null);
  }

  @Override
  @SuppressWarnings("PMD.NcssMethodCount")
  public XStream createXStream(ReflectionProvider reflectionProvider) {
    XStream xstream = null;
    //noinspection IfMayBeConditional
    if (reflectionProvider == null) {
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
    XStream.setupDefaultSecurity(xstream);
    xstream.allowTypesByWildcard(new String[]{
        "org.obiba.**"
    });

    xstream.registerConverter(new VariableConverter(xstream.getMapper()));
    xstream.registerConverter(new CategoryConverter(xstream.getMapper()));
    xstream.registerConverter(new AttributeConverter());
    xstream.registerConverter(new ValueConverter());
    xstream.registerConverter(new ValueSequenceConverter());
    xstream.registerConverter(new ValueTableConverter());
    xstream.registerConverter(new JoinTableConverter());

    // Converters are prioritized in reverse order of their addition
    for (Converter converter : converters) {
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
