/*
 * Copyright (c) 2017 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.magma.filter;

import org.obiba.magma.ValueSet;
import org.obiba.magma.VariableValueSource;

import com.thoughtworks.xstream.annotations.XStreamAlias;

@XStreamAlias("excludeAll")
public class ExcludeAllFilter<T> extends AbstractFilter<T> {

  private ExcludeAllFilter() {
    // Force clients to use the builder.
  }

  @Override
  protected Boolean runFilter(T item) {
    return Boolean.TRUE;
  }

  public static class Builder {

    public static Builder newFilter() {
      return new Builder();
    }

    public ExcludeAllFilter<ValueSet> buildForValueSet() {
      ExcludeAllFilter<ValueSet> filter = new ExcludeAllFilter<>();
      filter.setType(Type.EXCLUDE);
      return filter;
    }

    public ExcludeAllFilter<VariableValueSource> buildForVariableValueSource() {
      ExcludeAllFilter<VariableValueSource> filter = new ExcludeAllFilter<>();
      filter.setType(Type.EXCLUDE);
      return filter;
    }

  }
}
