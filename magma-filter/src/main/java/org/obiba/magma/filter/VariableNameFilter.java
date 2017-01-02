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

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.obiba.magma.Initialisable;
import org.obiba.magma.Variable;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;
import com.thoughtworks.xstream.annotations.XStreamOmitField;

@XStreamAlias("variableName")
public class VariableNameFilter extends AbstractFilter<Variable> implements Initialisable {

  @XStreamAsAttribute
  private final String prefix;

  @XStreamAsAttribute
  private final String match;

  @XStreamOmitField
  private Pattern matchPattern;

  @XStreamOmitField
  private boolean initialised;

  VariableNameFilter(String prefix, String match) {
    this.prefix = prefix;
    this.match = match;
    initialise();
  }

  @Override
  public void initialise() {
    if(initialised) return;
    if(prefix == null && match == null) {
      throw new IllegalArgumentException("The arguments [prefix] and [match] cannot both be null.");
    }
    if(prefix != null && match != null) {
      throw new IllegalArgumentException("The arguments [prefix] and [match] cannot both have values.");
    }
    if(match != null) matchPattern = Pattern.compile(match);
    initialised = true;
  }

  @Override
  protected Boolean runFilter(Variable item) {
    initialise();
    if(prefix != null) {
      if(item.getName().startsWith(prefix)) return Boolean.TRUE;
    } else if(match != null) {
      Matcher matcher = matchPattern.matcher(item.getName());
      if(matcher.matches()) return Boolean.TRUE;
    }
    return Boolean.FALSE;
  }

  @SuppressWarnings("ParameterHidesMemberVariable")
  public static class Builder extends AbstractFilter.Builder {

    private String prefix;

    private String match;

    public static Builder newFilter() {
      return new Builder();
    }

    public Builder prefix(String prefix) {
      this.prefix = prefix;
      return this;
    }

    public Builder match(String match) {
      this.match = match;
      return this;
    }

    public VariableNameFilter build() {
      VariableNameFilter filter = new VariableNameFilter(prefix, match);
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

}
