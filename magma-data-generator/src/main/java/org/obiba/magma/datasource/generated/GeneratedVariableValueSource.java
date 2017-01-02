/*
 * Copyright (c) 2017 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.magma.datasource.generated;

import java.util.List;

import javax.validation.constraints.NotNull;

import org.obiba.magma.AbstractVariableValueSource;
import org.obiba.magma.Value;
import org.obiba.magma.ValueSet;
import org.obiba.magma.ValueSource;
import org.obiba.magma.ValueType;
import org.obiba.magma.Variable;
import org.obiba.magma.VariableValueSource;
import org.obiba.magma.VectorSource;
import org.obiba.magma.VectorSourceNotSupportedException;
import org.obiba.magma.js.JavascriptValueSource;
import org.obiba.magma.support.Initialisables;
import org.obiba.magma.type.BooleanType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;

abstract class GeneratedVariableValueSource extends AbstractVariableValueSource implements VariableValueSource {

  private final static Logger log = LoggerFactory.getLogger(GeneratedVariableValueSource.class);

  private final Variable variable;

  private final ValueSource condition;

  protected GeneratedVariableValueSource(Variable variable) {
    this.variable = variable;
    if(variable.hasAttribute("condition")) {
      JavascriptValueSource src = new JavascriptValueSource(BooleanType.get(),
          variable.getAttributeStringValue("condition"));
      try {
        Initialisables.initialise(src);
      } catch(RuntimeException e) {
        log.warn("Cannot compile condition for variable {}", variable.getName());
        //noinspection AssignmentToNull
        src = null;
      }
      condition = src;
    } else {
      condition = null;
    }
  }

  @NotNull
  @Override
  public Variable getVariable() {
    return variable;
  }

  @Override
  public boolean supportVectorSource() {
    return false;
  }

  @NotNull
  @Override
  public VectorSource asVectorSource() {
    throw new VectorSourceNotSupportedException(getClass());
  }

  @NotNull
  @Override
  public Value getValue(ValueSet valueSet) {
    if(shouldGenerate(valueSet)) {
      GeneratedValueSet gvs = (GeneratedValueSet) valueSet;
      try {
        Value existingValue = gvs.getExistingValue(getVariable().getName());
        if(existingValue != null) {
          return existingValue;
        }
        if(variable.isRepeatable()) {
          int sequenceLength = gvs.dataGenerator.nextInt(0, 10);
          List<Value> values = Lists.newArrayListWithCapacity(sequenceLength);
          for(int i = 0; i < sequenceLength; i++) {
            values.add(nextValue(getVariable(), gvs));
          }
          return gvs.put(getVariable().getName(), ValueType.Factory.newSequence(variable.getValueType(), values));
        }
        return gvs.put(getVariable().getName(), nextValue(getVariable(), gvs));
      } catch(RuntimeException e) {
        //noinspection StringConcatenationArgumentToLogCall
        log.warn("Error generating data for variable " + getVariable().getName(), e);
        return getValueType().nullValue();
      }
    }
    return getValueType().nullValue();
  }

  @NotNull
  @Override
  public ValueType getValueType() {
    return getVariable().getValueType();
  }

  protected boolean shouldGenerate(ValueSet valueSet) {
    if(condition != null) {
      try {
        Value value = condition.getValue(valueSet);
        return value.isNull() ? false : (Boolean) value.getValue();
      } catch(RuntimeException e) {
        log.warn("Error evaluating condition for variable {}", getVariable().getName());
      }
    }
    return true;
  }

  abstract protected Value nextValue(Variable variable, GeneratedValueSet gvs);

}
