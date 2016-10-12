/*
 * Copyright (c) 2016 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.magma;

import javax.validation.constraints.NotNull;

/**
 * Defines the contract for obtaining a {@link Value} of a specific {@link Variable} within a {@link ValueSet}
 */
public interface VariableValueSource extends ValueSource {

  @NotNull
  String getName();

  @NotNull
  Variable getVariable();

}
