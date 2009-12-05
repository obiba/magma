package org.obiba.magma.xstream;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.obiba.magma.Value;
import org.obiba.magma.Variable;
import org.obiba.magma.VariableEntity;

import com.google.common.base.Function;
import com.google.common.collect.MapMaker;
import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;
import com.thoughtworks.xstream.annotations.XStreamImplicit;
import com.thoughtworks.xstream.annotations.XStreamOmitField;

@XStreamAlias(value = "valueSet")
public class XStreamValueSet {

  @XStreamAsAttribute
  private String valueTable;

  @XStreamAsAttribute
  private String entityType;

  @XStreamAsAttribute
  private String entityIdentifier;

  @XStreamImplicit
  private List<XStreamValueSetValue> values = new LinkedList<XStreamValueSetValue>();

  @XStreamOmitField
  private Map<String, XStreamValueSetValue> valueMap = new MapMaker().makeComputingMap(new FindValueComputation());

  public XStreamValueSet(String valueTable, VariableEntity entity) {
    this.valueTable = valueTable;
    this.entityType = entity.getType();
    this.entityIdentifier = entity.getIdentifier();
  }

  public void setValue(Variable variable, Value value) {
    XStreamValueSetValue valueSetValue = lookupValue(variable.getName());

    if(valueSetValue != null) {
      valueSetValue.setValue(value);
    } else {
      values.add(new XStreamValueSetValue(variable.getName(), value));
    }
  }

  public Value getValue(Variable variable) {
    XStreamValueSetValue valueSetValue = lookupValue(variable.getName());
    if(valueSetValue != null) {
      return valueSetValue.getValue();
    } else {
      return variable.getValueType().nullValue();
    }
  }

  private XStreamValueSetValue lookupValue(String name) {
    try {
      return valueMap.get(name);
    } catch(NullPointerException e) {
      // When the computation returns null, get throws a NullPointerException
      return null;
    }
  }

  /**
   * XStream does not instantiate default values for fields. Thus, we must implement readResolve and set it ourself.
   * <p>
   * Note that we don't set the {@code #values} field as it will never be null after deserialisation.
   * @return this
   */
  private Object readResolve() {
    valueMap = new MapMaker().makeComputingMap(new FindValueComputation());
    return this;
  }

  /**
   * Finds an {@code XStreamValueSetValue} in {@code XStreamValueSet#values} using the variable name.
   * <p>
   * This is used to lazily convert the {@code List} into a {@code Map}. The first time a key is requested, the process
   * is O(n), subsequent requests for the same key will be O(1) (or close to).
   */
  private class FindValueComputation implements Function<String, XStreamValueSetValue> {
    @Override
    public XStreamValueSetValue apply(String from) {
      for(XStreamValueSetValue valueSetValue : values) {
        if(valueSetValue.getVariable().equals(from)) {
          return valueSetValue;
        }
      }
      return null;
    }
  }

}
