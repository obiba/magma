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

import java.util.HashSet;
import java.util.Set;

import org.obiba.magma.Variable;
import org.obiba.magma.VariableValueSource;
import org.obiba.magma.VariableValueSourceFactory;

import com.google.common.collect.ImmutableSet;

/**
 * Creates a {@code Set} of custom derived variables.
 * @see JavascriptVariableValueSource
 */
public class JavascriptVariableValueSourceFactory implements VariableValueSourceFactory {

  private Set<Variable> variables;

  public Set<VariableValueSource> createSources() {
    if(variables != null && variables.size() > 0) {
      return createSourcesFromVariables();
    } else {
      return ImmutableSet.of();
    }
  }

  private Set<VariableValueSource> createSourcesFromVariables() {
    ImmutableSet.Builder<VariableValueSource> sources = new ImmutableSet.Builder<VariableValueSource>();
    for(Variable variable : variables) {
      sources.add(new JavascriptVariableValueSource(variable));
    }
    return sources.build();
  }

  public void setVariables(Set<Variable> variables) {
    this.variables = new HashSet<Variable>();
    if(variables != null) {
      this.variables.addAll(variables);
    }
  }

}
