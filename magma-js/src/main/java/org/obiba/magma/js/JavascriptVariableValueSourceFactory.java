/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.magma.js;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;

import org.obiba.magma.ValueTable;
import org.obiba.magma.Variable;
import org.obiba.magma.VariableValueSource;
import org.obiba.magma.VariableValueSourceFactory;

import com.google.common.collect.ImmutableSet;

/**
 * Creates a {@code Set} of custom derived variables.
 *
 * @see JavascriptVariableValueSource
 * @see SameAsVariableValueSource
 */
public class JavascriptVariableValueSourceFactory implements VariableValueSourceFactory {

  private final Collection<Variable> variables = new LinkedHashSet<>();

  private ValueTable valueTable;

  @Override
  public Set<VariableValueSource> createSources() {
    return variables.size() > 0 ? createSourcesFromVariables() : ImmutableSet.<VariableValueSource>of();
  }

  private Set<VariableValueSource> createSourcesFromVariables() {
    ImmutableSet.Builder<VariableValueSource> sources = new ImmutableSet.Builder<>();
    for(Variable variable : variables) {
      if(variable.hasAttribute(SameAsVariableValueSource.SAME_AS_ATTRIBUTE_NAME)) {
        sources.add(new SameAsVariableValueSource(variable, valueTable));
      } else {
        sources.add(new JavascriptVariableValueSource(variable, valueTable));
      }
    }
    return sources.build();
  }

  public void setVariables(Collection<Variable> variables) {
    this.variables.clear();
    if(variables != null) {
      this.variables.addAll(variables);
    }
  }

  public void setValueTable(ValueTable valueTable) {
    this.valueTable = valueTable;
  }
}
