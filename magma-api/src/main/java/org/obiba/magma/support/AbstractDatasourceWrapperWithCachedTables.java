/*
 * Copyright (c) 2013 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.magma.support;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import javax.annotation.Nonnull;

import org.obiba.magma.Datasource;
import org.obiba.magma.NoSuchValueTableException;
import org.obiba.magma.ValueTable;

import com.google.common.collect.Sets;

/**
 *
 */
public abstract class AbstractDatasourceWrapperWithCachedTables extends AbstractDatasourceWrapper {

  private final Map<String, ValueTable> valueTablesCache = new LinkedHashMap<>(100);

  protected AbstractDatasourceWrapperWithCachedTables(@Nonnull Datasource wrapped) {
    super(wrapped);
  }

  protected abstract ValueTable createValueTable(ValueTable table);

  @Override
  public ValueTable getValueTable(String name) throws NoSuchValueTableException {
    ValueTable valueTable = null;
    try {
      valueTable = getCachedValueTable(name);
    } catch(NoSuchValueTableException e) {
      valueTable = createValueTable(super.getValueTable(name));
      valueTablesCache.put(name, valueTable);
    }
    return valueTable;
  }

  @Override
  public Set<ValueTable> getValueTables() {
    for(ValueTable sourceTable : super.getValueTables()) {
      try {
        getCachedValueTable(sourceTable.getName());
      } catch(NoSuchValueTableException e) {
        valueTablesCache.put(sourceTable.getName(), createValueTable(sourceTable));
      }
    }
    return Collections.unmodifiableSet(Sets.newHashSet(valueTablesCache.values()));
  }

  private ValueTable getCachedValueTable(String tableName) throws NoSuchValueTableException {
    ValueTable table = valueTablesCache.get(tableName);
    if(table == null) {
      throw new NoSuchValueTableException(getName(), tableName);
    }
    return table;
  }
}
