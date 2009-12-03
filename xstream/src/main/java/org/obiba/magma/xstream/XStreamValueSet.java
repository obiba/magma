package org.obiba.magma.xstream;

import java.util.LinkedList;
import java.util.List;

import org.obiba.magma.Value;
import org.obiba.magma.Variable;
import org.obiba.magma.VariableEntity;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;
import com.thoughtworks.xstream.annotations.XStreamImplicit;

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

  public XStreamValueSet(String valueTable, VariableEntity entity) {
    this.valueTable = valueTable;
    this.entityType = entity.getType();
    this.entityIdentifier = entity.getIdentifier();
  }

  public void addValue(Variable variable, Value value) {
    values.add(new XStreamValueSetValue(variable.getName(), value));
  }

}
