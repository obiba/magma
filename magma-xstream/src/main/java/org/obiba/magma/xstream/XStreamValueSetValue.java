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

import org.obiba.magma.Value;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;

@XStreamAlias(value = "variableValue")
public class XStreamValueSetValue {

  @XStreamAsAttribute
  private final String variable;

  private Value value;

  public XStreamValueSetValue(String variable, Value value) {
    this.variable = variable;
    this.value = value;
  }

  public String getVariable() {
    return variable;
  }

  public Value getValue() {
    return value;
  }

  public void setValue(Value value) {
    this.value = value;
  }
}
