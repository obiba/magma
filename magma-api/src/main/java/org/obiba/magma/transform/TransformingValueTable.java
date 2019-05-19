/*
 * Copyright (c) 2019 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.magma.transform;

import javax.validation.constraints.NotNull;

import org.obiba.magma.ValueSet;
import org.obiba.magma.ValueTable;
import org.obiba.magma.VariableEntity;
import org.obiba.magma.VariableValueSource;

public interface TransformingValueTable extends ValueTable {

  @NotNull
  BijectiveFunction<VariableEntity, VariableEntity> getVariableEntityMappingFunction();

  @NotNull
  BijectiveFunction<ValueSet, ValueSet> getValueSetMappingFunction();

  @NotNull
  BijectiveFunction<VariableValueSource, VariableValueSource> getVariableValueSourceMappingFunction();

}
