package org.obiba.magma.datasource.generated;

import java.util.concurrent.ConcurrentMap;

import org.apache.commons.math.random.JDKRandomGenerator;
import org.apache.commons.math.random.RandomData;
import org.apache.commons.math.random.RandomDataImpl;
import org.apache.commons.math.random.RandomGenerator;
import org.obiba.magma.Value;
import org.obiba.magma.ValueTable;
import org.obiba.magma.VariableEntity;
import org.obiba.magma.support.ValueSetBean;

import com.google.common.collect.MapMaker;

/**
 * Allows generating random data for this
 */
class GeneratedValueSet extends ValueSetBean {

  final RandomGenerator valueGenerator;

  final RandomDataImpl dataGenerator;

  final ConcurrentMap<String, Value> generatedValues = new MapMaker().makeMap();

  GeneratedValueSet(ValueTable table, VariableEntity entity) {
    super(table, entity);
    valueGenerator = new JDKRandomGenerator();
    valueGenerator.setSeed(Long.parseLong(entity.getIdentifier()));
    dataGenerator = new RandomDataImpl(valueGenerator);
  }

  boolean hasValue(String name) {
    return generatedValues.containsKey(name);
  }

  Value put(String name, Value value) {
    generatedValues.put(name, value);
    return value;
  }

  Value getExistingValue(String name) {
    return generatedValues.get(name);
  }
}