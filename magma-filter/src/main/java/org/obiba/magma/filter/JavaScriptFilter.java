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

import javax.validation.constraints.NotNull;

import org.obiba.magma.Initialisable;
import org.obiba.magma.Value;
import org.obiba.magma.ValueSet;
import org.obiba.magma.js.JavascriptValueSource;
import org.obiba.magma.type.BooleanType;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamOmitField;

@XStreamAlias("script")
public class JavaScriptFilter extends AbstractFilter<ValueSet> implements Initialisable {

  private static final String SCRIPT_NAME = "JAVASCRIPT_FILTER_SCRIPT";

  private final String javascript;

  @XStreamOmitField
  private JavascriptValueSource javascriptSource;

  @XStreamOmitField
  private boolean initialised;

  JavaScriptFilter(@NotNull String javascript) {
    //noinspection ConstantConditions
    if(javascript == null) throw new IllegalArgumentException("The argument [javascript] cannot be null.");
    this.javascript = javascript;
    initialise();
  }

  @Override
  public synchronized void initialise() {
    if(initialised) return;
    javascriptSource = new JavascriptValueSource(BooleanType.get(), javascript);
    javascriptSource.setScriptName(SCRIPT_NAME);
    javascriptSource.initialise();
    initialised = true;
  }

  @edu.umd.cs.findbugs.annotations.SuppressWarnings(value = "NP_BOOLEAN_RETURN_NULL",
      justification = "Clients expect null as a valid value. It's possible for JavaScript to return null.")
  @Override
  protected Boolean runFilter(ValueSet item) {
    initialise();
    Value value = javascriptSource.getValue(item);
    // JavaScript can return null.
    return value.isNull() || value.equals(BooleanType.get().nullValue()) //
        ? null  //
        : (Boolean) value.getValue();
  }

  @SuppressWarnings("ParameterHidesMemberVariable")
  public static class Builder extends AbstractFilter.Builder {

    private String javascript;

    public static Builder newFilter() {
      return new Builder();
    }

    public Builder javascript(String javascript) {
      this.javascript = javascript;
      return this;
    }

    public JavaScriptFilter build() {
      JavaScriptFilter filter = new JavaScriptFilter(javascript);
      filter.setType(type);
      return filter;
    }

    @Override
    public Builder exclude() {
      super.exclude();
      return this;
    }

    @Override
    public Builder include() {
      super.include();
      return this;
    }
  }

  @Override
  public String toString() {
    return SCRIPT_NAME + "[" + javascript + "]";
  }
}
