/*
 * Copyright (c) 2019 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.magma.datasource.jdbc;


import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.obiba.magma.Value;
import org.obiba.magma.ValueSet;
import org.obiba.magma.ValueSetBatch;
import org.obiba.magma.VariableEntity;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Get the {@link JdbcValueSet}s for a batch of {@link VariableEntity} in one SQL query.
 */
public class JdbcValueSetBatch implements ValueSetBatch {

  private final JdbcValueTable table;

  private final List<VariableEntity> entities;

  private final JdbcValueSetFetcher fetcher;

  public JdbcValueSetBatch(JdbcValueTable table, List<VariableEntity> entities) {
    this.table = table;
    this.entities = entities;
    this.fetcher = new JdbcValueSetFetcher(table);
  }

  @Override
  public List<ValueSet> getValueSets() {
    // Map rows per entity identifier
    String idColumn = table.getSettings().getEntityIdentifierColumn();
    Map<String, List<Map<String, Value>>> rowsMap = Maps.newHashMap();
    fetcher.loadNonBinaryVariableValues(entities).stream() //
        .filter(valuesMap -> valuesMap.containsKey(idColumn)) //
        .forEach(valuesMap -> {
          String id = valuesMap.get(idColumn).toString();
          if (!rowsMap.containsKey(id)) {
            rowsMap.put(id, Lists.newArrayList());
          }
          rowsMap.get(id).add(valuesMap);
        });

    return entities.stream().map(e -> {
      JdbcValueSet vs = new JdbcValueSet(table, e);
      vs.populateResultSetCache(rowsMap.get(e.getIdentifier()));
      return vs;
    }).collect(Collectors.toList());
  }
}
