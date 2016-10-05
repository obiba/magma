/*
 * Copyright (c) 2016 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.magma.datasource.hibernate;

import com.google.common.collect.Maps;
import org.obiba.magma.ValueSet;
import org.obiba.magma.ValueSetBatch;
import org.obiba.magma.VariableEntity;
import org.obiba.magma.datasource.hibernate.domain.ValueSetState;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Get the {@link HibernateValueSet}s from on {@link org.obiba.magma.datasource.hibernate.domain.ValueSetState} query.
 */
public class HibernateValueSetBatch implements ValueSetBatch {

  private final HibernateValueTable table;

  private final List<VariableEntity> entities;

  private final HibernateValueSetFetcher fetcher;

  public HibernateValueSetBatch(HibernateValueTable table, List<VariableEntity> entities) {
    this.table = table;
    this.entities = entities;
    this.fetcher = new HibernateValueSetFetcher(table);
  }

  @Override
  public List<ValueSet> getValueSets() {
    Map<String, ValueSetState> valueSetStateMap = Maps.newHashMap();
    fetcher.getValueSetStates(entities).forEach(vss -> valueSetStateMap.put(vss.getVariableEntity().getIdentifier(), vss));
    return entities.stream().map(e -> {
      HibernateValueSet vs = new HibernateValueSet(table, e);
      vs.setValueSetState(valueSetStateMap.get(e.getIdentifier()));
      return vs;
    }).collect(Collectors.toList());
  }
}
