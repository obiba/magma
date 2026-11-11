/*
 * Copyright (c) 2026 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.magma.js.views;

import java.util.List;
import java.util.stream.Collectors;

import org.junit.Test;
import org.obiba.magma.MagmaEngine;
import org.obiba.magma.NoSuchValueSetException;
import org.obiba.magma.Value;
import org.obiba.magma.ValueSet;
import org.obiba.magma.ValueTable;
import org.obiba.magma.ValueType;
import org.obiba.magma.VariableEntity;
import org.obiba.magma.VariableValueSource;
import org.obiba.magma.VectorSource;
import org.obiba.magma.js.AbstractJsTest;
import org.obiba.magma.support.AbstractVariableValueSourceWrapper;
import org.obiba.magma.support.StaticDatasource;
import org.obiba.magma.support.StaticValueTable;
import org.obiba.magma.support.ValueSetBean;
import org.obiba.magma.type.IntegerType;
import org.obiba.magma.type.TextType;
import org.obiba.magma.views.View;

import com.google.common.collect.ImmutableList;

import static org.fest.assertions.api.Assertions.assertThat;

public class JavascriptClauseFilterTest extends AbstractJsTest {

  private static final List<String> IDS = ImmutableList.of("1", "2", "3", "4");

  private static final List<String> SCRIPTS = ImmutableList.of( //
      "$('AGE').ge(20)", //
      "$('ds.other:SEX').eq('M')", //
      "$('AGE').ge(20).and($('ds.other:SEX').eq('M'))", //
      "$id().eq('2')", //
      "$('AGE').isNull().not() ? $('AGE').lt(40) : false");

  @Test
  public void test_filter_vectors_selects_as_where_without_reading_value_sets() {
    VectorTable table = setup(true);
    for(String script : SCRIPTS) {
      JavascriptClause clause = clause(script);
      List<String> expected = whereIds(clause, table);
      table.valueSetReads = 0;
      assertThat(ids(clause.filter(table, table.getVariableEntities(), null))).as(script).isEqualTo(expected);
      assertThat(table.valueSetReads).as(script).isEqualTo(0);
    }
  }

  @Test
  public void test_filter_falls_back_to_value_sets_when_vectors_are_not_supported() {
    VectorTable table = setup(false);
    for(String script : SCRIPTS) {
      JavascriptClause clause = clause(script);
      assertThat(ids(clause.filter(table, table.getVariableEntities(), null))).as(script)
          .isEqualTo(whereIds(clause, table));
    }
  }

  @Test
  public void test_view_reads_values_of_the_filtered_entities_only() {
    VectorTable table = setup(true);
    View view = View.Builder.newView("view", table).where(clause("$('AGE').ge(20)")).build();
    view.initialise();
    assertThat(ids(view.getVariableEntities())).isEqualTo(ImmutableList.of("2", "3", "4"));
    assertThat(view.getValueSet(entity(table, "3")).getVariableEntity().getIdentifier()).isEqualTo("3");
    try {
      view.getValueSet(entity(table, "1"));
      throw new AssertionError("entity 1 is filtered out");
    } catch(NoSuchValueSetException expected) {
      // filtered out
    }
  }

  private VectorTable setup(boolean vectors) {
    StaticDatasource ds = new StaticDatasource("ds");
    VectorTable table = new VectorTable(ds, "table", vectors);
    table.addVariables(IntegerType.get(), "AGE");
    VectorTable other = new VectorTable(ds, "other", vectors);
    other.addVariables(TextType.get(), "SEX");
    String[] sexes = { "M", "F", "M", "F" };
    for(int i = 0; i < IDS.size(); i++) {
      table.addValues(IDS.get(i), "AGE", i == 0 ? null : (i + 1) * 10);
      other.addValues(IDS.get(i), "SEX", sexes[i]);
    }
    ds.addValueTable(table);
    ds.addValueTable(other);
    MagmaEngine.get().addDatasource(ds);
    return table;
  }

  private static JavascriptClause clause(String script) {
    JavascriptClause clause = new JavascriptClause(script);
    clause.initialise();
    return clause;
  }

  private static List<String> whereIds(JavascriptClause clause, ValueTable table) {
    return ids(table.getVariableEntities().stream().filter(e -> clause.where(table.getValueSet(e)))
        .collect(Collectors.toList()));
  }

  private static List<String> ids(List<VariableEntity> entities) {
    return entities.stream().map(VariableEntity::getIdentifier).collect(Collectors.toList());
  }

  private static VariableEntity entity(ValueTable table, String id) {
    return table.getVariableEntities().stream().filter(e -> e.getIdentifier().equals(id)).findFirst().get();
  }

  /**
   * A static table whose variables can be read as vectors, counting the value sets it is asked for.
   */
  private static class VectorTable extends StaticValueTable {

    private final boolean vectors;

    private int valueSetReads;

    VectorTable(StaticDatasource ds, String name, boolean vectors) {
      super(ds, name, IDS);
      this.vectors = vectors;
    }

    @Override
    public ValueSet getValueSet(VariableEntity entity) {
      valueSetReads++;
      return super.getValueSet(entity);
    }

    @Override
    public VariableValueSource getVariableValueSource(String variableName) {
      VariableValueSource source = super.getVariableValueSource(variableName);
      if(!vectors) return source;
      return new AbstractVariableValueSourceWrapper(source) {
        @Override
        public boolean supportVectorSource() {
          return true;
        }

        @Override
        public VectorSource asVectorSource() {
          return new VectorSource() {
            @Override
            public ValueType getValueType() {
              return source.getValueType();
            }

            @Override
            public Iterable<Value> getValues(Iterable<VariableEntity> entities) {
              return ImmutableList.copyOf(entities).stream()
                  .map(e -> source.getValue(new ValueSetBean(VectorTable.this, e))).collect(Collectors.toList());
            }
          };
        }
      };
    }
  }
}
