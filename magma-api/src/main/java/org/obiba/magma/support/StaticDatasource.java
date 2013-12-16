/*******************************************************************************
 * Copyright (c) 2012 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.magma.support;

import java.io.IOException;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.validation.constraints.NotNull;

import org.obiba.magma.AbstractAttributeAware;
import org.obiba.magma.Attribute;
import org.obiba.magma.Datasource;
import org.obiba.magma.NoSuchValueTableException;
import org.obiba.magma.Timestamps;
import org.obiba.magma.Value;
import org.obiba.magma.ValueTable;
import org.obiba.magma.ValueTableWriter;
import org.obiba.magma.Variable;
import org.obiba.magma.VariableEntity;

import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.LinkedListMultimap;
import com.google.common.collect.ListMultimap;

/**
 *
 */
public class StaticDatasource extends AbstractAttributeAware implements Datasource {

  private final String name;

  private final ListMultimap<String, Attribute> attributes = LinkedListMultimap.create();

  private final Map<String, StaticValueTable> tableMap = new LinkedHashMap<>();

  public StaticDatasource(String name) {
    this.name = name;
  }

  public void addValueTable(StaticValueTable table) {
    tableMap.put(table.getName(), table);
  }

  @Override
  public void initialise() {
    for(StaticValueTable table : tableMap.values()) {
      Initialisables.initialise(table);
    }
  }

  @Override
  public void dispose() {
    tableMap.clear();
  }

  @Override
  public String getType() {
    return "static";
  }

  @Override
  public boolean hasValueTable(String name) {
    return tableMap.containsKey(name);
  }

  @Override
  public boolean hasEntities(Predicate<ValueTable> predicate) {
    return Iterables.filter(getValueTables(), predicate).iterator().hasNext();
  }

  @Override
  public ValueTable getValueTable(String name) throws NoSuchValueTableException {
    return tableMap.get(name);
  }

  @Override
  public Set<ValueTable> getValueTables() {
    return ImmutableSet.<ValueTable>builder().addAll(tableMap.values()).build();
  }

  @Override
  public boolean canDropTable(String name) {
    return hasValueTable(name);
  }

  @Override
  public boolean canRenameTable(String tableName) {
    return hasValueTable(name);
  }

  @Override
  public void renameTable(String tableName, String newName) {
    StaticValueTable table = tableMap.remove(tableName);
    table.setName(newName);
    tableMap.put(newName, table);
  }

  @Override
  public void dropTable(String name) {
    tableMap.remove(name);
  }

  @Override
  public boolean canDrop() {
    return true;
  }

  @Override
  public void drop() {
    tableMap.clear();
    attributes.clear();
  }

  @NotNull
  @Override
  public Timestamps getTimestamps() {
    return new UnionTimestamps(getValueTables());
  }

  @NotNull
  @Override
  public ValueTableWriter createWriter(@NotNull String tableName, @NotNull String entityType) {
    //noinspection ConstantConditions
    if(tableName == null) throw new IllegalArgumentException("tableName cannot be null");
    //noinspection ConstantConditions
    if(entityType == null) throw new IllegalArgumentException("entityType cannot be null");

    StaticValueTable table;
    if(hasValueTable(tableName)) {
      table = tableMap.get(tableName);
    } else {
      table = new StaticValueTable(this, tableName, new HashSet<String>(), entityType);
      addValueTable(table);
    }
    return new StaticValueTableWriter(table);
  }

  @Override
  public void setAttributeValue(String name, Value value) {
    Attribute attribute = Attribute.Builder.newAttribute(name).withValue(value).build();

    List<Attribute> attributesForName = getInstanceAttributes().get(name);
    if(attributesForName.isEmpty()) {
      attributesForName.add(attribute);
    } else {
      attributesForName.set(0, attribute);
    }
  }

  @Override
  public String getName() {
    return name;
  }

  @Override
  protected ListMultimap<String, Attribute> getInstanceAttributes() {
    return attributes;
  }

  /**
   *
   */
  private static final class StaticValueTableWriter implements ValueTableWriter {

    private final StaticValueTable table;

    private StaticValueTableWriter(StaticValueTable table) {
      this.table = table;
    }

    @Override
    public void close() throws IOException {
    }

    @Override
    public VariableWriter writeVariables() {
      return new VariableWriter() {

        @Override
        public void close() throws IOException {
        }

        @Override
        public void writeVariable(@NotNull Variable variable) {
          table.addVariable(Variable.Builder.sameAs(variable).build());
        }

        @Override
        public void removeVariable(@NotNull Variable variable) {
          table.removeVariable(variable.getName());
        }
      };
    }

    @NotNull
    @Override
    public ValueSetWriter writeValueSet(@NotNull final VariableEntity entity) {
      if(!table.hasVariableEntity(entity)) {
        table.addVariableEntity(entity);
      }
      return new ValueSetWriter() {

        @Override
        public void close() throws IOException {
        }

        @Override
        public void writeValue(@NotNull Variable variable, Value value) {
          table.addValues(entity.getIdentifier(), variable, value);
        }
      };
    }
  }

}
