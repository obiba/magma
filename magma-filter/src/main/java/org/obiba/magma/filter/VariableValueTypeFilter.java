/*
 * Copyright (c) 2017 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.magma.filter;

import org.obiba.magma.Initialisable;
import org.obiba.magma.Variable;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;

@XStreamAlias("variableValueType")
public class VariableValueTypeFilter extends AbstractFilter<Variable> implements Initialisable {

  @XStreamAsAttribute
  private final String valueType;

  VariableValueTypeFilter(String valueType) {
    this.valueType = valueType;
  }

  @Override
  public void initialise() {
    Preconditions.checkState(!Strings.isNullOrEmpty(valueType));
  }

  @Override
  protected Boolean runFilter(Variable item) {
    initialise();
    return item.getValueType().getName().equalsIgnoreCase(valueType);
  }
}
