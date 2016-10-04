/*
 * Copyright (c) 2016 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.magma.views;

import com.google.common.base.Objects;
import org.obiba.magma.ValueType;
import org.obiba.magma.Variable;

import javax.validation.constraints.NotNull;

/**
 * Created by yannick on 04/10/16.
 */
class JoinVariable {

  @NotNull
  private final String name;

  @NotNull
  private final ValueType valueType;

  private final boolean repeatable;

  private JoinVariable(@NotNull String name, @NotNull ValueType valueType, boolean repeatable) {
    this.name = name;
    this.valueType = valueType;
    this.repeatable = repeatable;
  }

  JoinVariable(@NotNull Variable variable) {
    this(variable.getName(), variable.getValueType(), variable.isRepeatable());
  }

  @NotNull
  public String getName() {
    return name;
  }

  @NotNull
  public ValueType getValueType() {
    return valueType;
  }

  public boolean isRepeatable() {
    return repeatable;
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(name, valueType, repeatable);
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null || getClass() != obj.getClass()) {
      return false;
    }
    JoinVariable other = (JoinVariable) obj;
    return Objects.equal(name, other.name) && Objects.equal(valueType, other.valueType) &&
        repeatable == other.repeatable;
  }
}
