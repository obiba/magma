/**
 * 
 */
package org.obiba.magma.datasource.generated;

import java.util.concurrent.ConcurrentMap;

import org.apache.commons.math.random.JDKRandomGenerator;
import org.apache.commons.math.random.RandomData;
import org.apache.commons.math.random.RandomDataImpl;
import org.apache.commons.math.random.RandomGenerator;
import org.obiba.magma.Value;
import org.obiba.magma.VariableEntity;
import org.obiba.magma.support.ValueSetBean;

import com.google.common.collect.MapMaker;

/**
 * Allows generating random data for this
 */
class GeneratedValueSet extends ValueSetBean {

  final RandomGenerator valueGenerator;

  final RandomData dataGenerator;

  final ConcurrentMap<String, Value> generatedValues = new MapMaker().makeMap();

  GeneratedValueSet(GeneratedValueTable table, VariableEntity entity) {
    super(table, entity);
    this.valueGenerator = new JDKRandomGenerator();
    this.valueGenerator.setSeed(Long.parseLong(entity.getIdentifier()));
    this.dataGenerator = new RandomDataImpl(valueGenerator);
  }

  boolean hasValue(String name) {
    return generatedValues.containsKey(name);
  }

  Value putIfAbsent(String name, Value value) {
    generatedValues.putIfAbsent(name, value);
    return value;
  }

  Value getExistingValue(String name) {
    return generatedValues.get(name);
  }
}