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

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.obiba.magma.ValueSet;
import org.obiba.magma.VariableEntity;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class JoinValueSetFetcher {

  private final JoinTable joinTable;

  public JoinValueSetFetcher(JoinTable joinTable) {
    this.joinTable = joinTable;
  }

  synchronized List<ValueSet> getInnerTableValueSets(VariableEntity entity) {
    return joinTable.getTables().stream() //
        .map(valueTable -> valueTable.hasValueSet(entity) ? valueTable.getValueSet(entity) : new EmptyValueSet(valueTable, entity)) //
        .collect(Collectors.toList());
  }

  synchronized Map<String, List<ValueSet>> getInnerTableValueSets(List<VariableEntity> entities) {
    Map<String, List<ValueSet>> vsMap = Maps.newHashMap();
    joinTable.getTables().forEach(valueTable -> {
      // take advantage of batch query of each table
      Map<String, ValueSet> tvs = StreamSupport.stream(valueTable.getValueSets(entities.stream() //
          .filter(e -> valueTable.hasValueSet(e)) //
          .collect(Collectors.toList())).spliterator(), false)
          .collect(Collectors.toMap(valueSet -> valueSet.getVariableEntity().getIdentifier(), Function.identity()));

      // fill the holes with null value sets
      entities.forEach(e -> {
        ValueSet vs = tvs.containsKey(e.getIdentifier()) ? tvs.get(e.getIdentifier()) : new EmptyValueSet(valueTable, e);
        if (!vsMap.containsKey(e.getIdentifier())) {
          vsMap.put(e.getIdentifier(), Lists.newArrayList());
        }
        vsMap.get(e.getIdentifier()).add(vs);
      });
    });
    return vsMap;
  }

}
