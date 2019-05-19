/*
 * Copyright (c) 2019 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.magma.views;

import org.obiba.magma.ValueSet;
import org.obiba.magma.ValueSetBatch;
import org.obiba.magma.VariableEntity;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class JoinValueSetBatch implements ValueSetBatch {

  private final JoinTable joinTable;

  private final List<VariableEntity> entities;

  private final JoinValueSetFetcher fetcher;

  public JoinValueSetBatch(JoinTable joinTable, List<VariableEntity> entities) {
    this.joinTable = joinTable;
    this.entities = entities;
    this.fetcher = new JoinValueSetFetcher(joinTable);
  }

  @Override
  public List<ValueSet> getValueSets() {
    Map<String, List<ValueSet>> vsMap = fetcher.getInnerTableValueSets(entities);
    return entities.stream().map(e -> {
      JoinValueSet vs = new JoinValueSet(joinTable, e);
      vs.setInnerValueSets(vsMap.get(e.getIdentifier()));
      return vs;
    }).collect(Collectors.toList());
  }
}
