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
import java.util.stream.Collectors;

public class JoinValueSetFetcher {

  private final JoinTable joinTable;

  public JoinValueSetFetcher(JoinTable joinTable) {
    this.joinTable = joinTable;
  }

  synchronized Iterable<ValueSet> getInnerTableValueSets(VariableEntity entity) {
    return joinTable.getTables().stream() //
        .filter(valueTable -> valueTable.hasValueSet(entity)) //
        .map(valueTable -> valueTable.getValueSet(entity)) //
        .collect(Collectors.toList());
  }

  synchronized Map<String, List<ValueSet>> getInnerTableValueSets(List<VariableEntity> entities) {
    Map<String, List<ValueSet>> vsMap = Maps.newHashMap();
    joinTable.getTables().forEach(valueTable -> {
      valueTable.getValueSets(entities.stream() //
          .filter(e -> valueTable.hasValueSet(e)) //
          .collect(Collectors.toList())).forEach(vs -> {
         if (!vsMap.containsKey(vs.getVariableEntity().getIdentifier())) {
           vsMap.put(vs.getVariableEntity().getIdentifier(), Lists.newArrayList());
         }
         vsMap.get(vs.getVariableEntity().getIdentifier()).add(vs);
      });
    });
    return vsMap;
  }

}
