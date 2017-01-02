/*
 * Copyright (c) 2017 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.magma;

import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * Each datasource will implement its specific method to extract efficiently a batch of {@link ValueSet}s.
 */
public interface ValueSetBatch {

  /**
   * {@link ValueSet}s must be in the same order as the provided {@link VariableEntity} list.
   * @return
   */
  @NotNull List<ValueSet> getValueSets();

}
