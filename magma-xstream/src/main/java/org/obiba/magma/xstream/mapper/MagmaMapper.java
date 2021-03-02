/*
 * Copyright (c) 2019 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.magma.xstream.mapper;

import org.obiba.magma.Attribute;
import org.obiba.magma.Category;
import org.obiba.magma.ValueSequence;
import org.obiba.magma.Variable;

import com.thoughtworks.xstream.mapper.Mapper;
import com.thoughtworks.xstream.mapper.MapperWrapper;

public class MagmaMapper extends MapperWrapper {

  public MagmaMapper(Mapper wrapped) {
    super(wrapped);
  }

  @Override
  @SuppressWarnings({ "unchecked", "rawtypes", "ConstantConditions" })
  public String serializedClass(Class type) {
    if(type == null) return super.serializedClass(type);
    if(Variable.class.isAssignableFrom(type)) {
      return "variable";
    }
    if(Attribute.class.isAssignableFrom(type)) {
      return "attribute";
    }
    if(Category.class.isAssignableFrom(type)) {
      return "category";
    }
    if(ValueSequence.class.equals(type)) {
      return "sequence";
    }
    return super.serializedClass(type);
  }

  @Override
  @SuppressWarnings({ "unchecked", "rawtypes", "ConstantConditions" })
  public Class realClass(String elementName) {
    if(elementName == null) return super.realClass(elementName);
    if("variable".equals(elementName)) {
      return Variable.class;
    }
    if("attribute".equals(elementName)) {
      return Attribute.class;
    }
    if("category".equals(elementName)) {
      return Category.class;
    }
    if("sequence".equals(elementName)) {
      return ValueSequence.class;
    }
    return super.realClass(elementName);
  }

}
