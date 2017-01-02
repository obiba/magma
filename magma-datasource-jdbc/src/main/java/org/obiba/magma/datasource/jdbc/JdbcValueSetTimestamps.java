/*
 * Copyright (c) 2017 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.magma.datasource.jdbc;

import javax.validation.constraints.NotNull;

import org.obiba.magma.Timestamps;
import org.obiba.magma.Value;
import org.obiba.magma.ValueSet;
import org.obiba.magma.type.DateTimeType;

public class JdbcValueSetTimestamps implements Timestamps {

  private final JdbcValueSet valueSet;

  public JdbcValueSetTimestamps(ValueSet valueSet) {
    this.valueSet = (JdbcValueSet) valueSet;
  }

  @NotNull
  @Override
  public Value getCreated() {
    return valueSet.getValueTable().hasCreatedTimestampColumn() && valueSet.getCreated() != null //
        ? valueSet.getCreated() //
        : DateTimeType.get().nullValue();
  }

  @NotNull
  @Override
  public Value getLastUpdate() {
    return valueSet.getValueTable().hasUpdatedTimestampColumn() && valueSet.getUpdated() != null //
        ? valueSet.getUpdated() //
        : DateTimeType.get().nullValue();
  }
}
