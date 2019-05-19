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

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.obiba.magma.Value;
import org.obiba.magma.Variable;
import org.obiba.magma.VariableEntity;

import com.google.common.collect.Maps;
import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;
import com.thoughtworks.xstream.annotations.XStreamImplicit;
import com.thoughtworks.xstream.annotations.XStreamOmitField;

@XStreamAlias(value = "valueSet")
public class XStreamValueSet {

  @XStreamAsAttribute
  @SuppressWarnings("FieldCanBeLocal")
  private final String valueTable;

  @XStreamAsAttribute
  @SuppressWarnings("FieldCanBeLocal")
  private final String entityType;

  @XStreamAsAttribute
  @SuppressWarnings("FieldCanBeLocal")
  private final String entityIdentifier;

  @XStreamImplicit
  @SuppressWarnings("TypeMayBeWeakened")
  private List<XStreamValueSetValue> values = new LinkedList<>();

  @XStreamOmitField
  private Map<String, XStreamValueSetValue> valueMap = Maps.newHashMap();

  public XStreamValueSet(String valueTable, VariableEntity entity) {
    this.valueTable = valueTable;
    entityType = entity.getType();
    entityIdentifier = entity.getIdentifier();
  }

  public void setValue(Variable variable, Value value) {
    XStreamValueSetValue valueSetValue = valueMap.get(variable.getName());

    if(valueSetValue != null) {
      valueSetValue.setValue(value);
    } else {
      XStreamValueSetValue xvalue = new XStreamValueSetValue(variable.getName(), value);
      values.add(xvalue);
      valueMap.put(variable.getName(), xvalue);
    }
  }

  public Value getValue(Variable variable) {
    XStreamValueSetValue valueSetValue = valueMap.get(variable.getName());
    return valueSetValue == null ? variable.getValueType().nullValue() : valueSetValue.getValue();
  }

  /**
   * XStream does not instantiate default values for fields. Thus, we must implement readResolve and set it ourself.
   * <p/>
   * Note that we don't set the {@code #values} field as it will never be null after deserialisation.
   *
   * @return this
   */
  private Object readResolve() {
    if(values == null) {
      values = new LinkedList<>();
    }
    valueMap = Maps.newHashMap();
    for(XStreamValueSetValue xvalue : values) {
      valueMap.put(xvalue.getVariable(), xvalue);
    }
    return this;
  }

}
