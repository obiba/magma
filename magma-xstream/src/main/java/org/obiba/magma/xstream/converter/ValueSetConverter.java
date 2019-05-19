/*
 * Copyright (c) 2019 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.magma.xstream.converter;

import org.obiba.magma.ValueSet;

import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.converters.collections.AbstractCollectionConverter;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;
import com.thoughtworks.xstream.mapper.Mapper;

public class ValueSetConverter extends AbstractCollectionConverter {

  public ValueSetConverter(Mapper mapper) {
    super(mapper);
  }

  @Override
  @SuppressWarnings("rawtypes")
  public boolean canConvert(Class type) {
    return ValueSet.class.isAssignableFrom(type);
  }

  @Override
  public void marshal(Object source, HierarchicalStreamWriter writer, MarshallingContext context) {
    ValueSet valueSet = (ValueSet) source;
    writer.addAttribute("entityType", valueSet.getVariableEntity().getType());
    writer.addAttribute("entityId", valueSet.getVariableEntity().getIdentifier());
  }

  @Override
  public Object unmarshal(HierarchicalStreamReader reader, UnmarshallingContext context) {
    throw new UnsupportedOperationException();
  }

}
