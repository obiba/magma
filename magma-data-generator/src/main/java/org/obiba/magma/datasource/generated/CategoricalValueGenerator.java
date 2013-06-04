package org.obiba.magma.datasource.generated;

import java.util.List;

import org.obiba.magma.Category;
import org.obiba.magma.Value;
import org.obiba.magma.ValueSource;
import org.obiba.magma.Variable;
import org.obiba.magma.datasource.generated.support.ShuffleBag;
import org.obiba.magma.type.IntegerType;

import com.google.common.collect.Lists;

class CategoricalValueGenerator extends AbstractMissingValueVariableValueGenerator {

  private final ShuffleBag<String> shuffleBag = new ShuffleBag<String>();

  private boolean hasNonMissing = false;

  CategoricalValueGenerator(Variable variable) {
    super(variable);
    for(Category c : variable.getCategories()) {
      if(!c.isMissing()) {
        Integer quantity = getQuantity(c);
        if(quantity == null) {
          shuffleBag.add(c.getName());
        } else {
          if(quantity >= 1) {
            shuffleBag.add(c.getName(), quantity);
          }
        }
        hasNonMissing = true;
      }
    }
  }

  private Integer getQuantity(Category c) {
    String qStr = getAttributeStringValue(c, "quantity", "weight");
    Integer quantity = null;
    if(qStr == null) {
      qStr = getAttributeStringValue(c, "%", "percentage", "pct", "prevalence", "prev");
      if(qStr != null) {
        quantity = new Double(qStr).intValue() * 100;
      }
    } else {
      quantity = new Double(qStr).intValue();
    }
    return quantity;
  }

  @Override
  protected Value nonMissingValue(Variable variable, GeneratedValueSet gvs) {
    if(!hasNonMissing) return variable.getValueType().nullValue();
    return variable.getValueType().valueOf(shuffleBag.next());
  }

}
