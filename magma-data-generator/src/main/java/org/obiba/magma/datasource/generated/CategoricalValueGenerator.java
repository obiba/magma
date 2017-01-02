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

import org.obiba.magma.Category;
import org.obiba.magma.Value;
import org.obiba.magma.Variable;
import org.obiba.magma.datasource.generated.support.ShuffleBag;

class CategoricalValueGenerator extends AbstractMissingValueVariableValueGenerator {

  private final ShuffleBag<String> shuffleBag = new ShuffleBag<>();

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

  private Integer getQuantity(Category category) {
    String qStr = getAttributeStringValue(category, "quantity", "weight");
    Integer quantity = null;
    if(qStr == null) {
      qStr = getAttributeStringValue(category, "%", "percentage", "pct", "prevalence", "prev");
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
