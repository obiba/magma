package org.obiba.magma.filter;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.obiba.magma.VariableValueSource;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;
import com.thoughtworks.xstream.annotations.XStreamOmitField;

@XStreamAlias("variableName")
public class VariableNameFilter extends AbstractFilter<VariableValueSource> {

  @XStreamAsAttribute
  private String prefix;

  @XStreamAsAttribute
  private String match;

  @XStreamOmitField
  private Pattern matchPattern;

  VariableNameFilter(String prefix, String match) {
    validateArguments(prefix, match);
    this.prefix = prefix;
    this.match = match;
    if(match != null) matchPattern = Pattern.compile(match);
  }

  private void validateArguments(String prefix, String match) {
    if(prefix == null && match == null) throw new IllegalArgumentException("The arguments [prefix] and [match] cannot both be null.");
    if(prefix != null && match != null) throw new IllegalArgumentException("The arguments [prefix] and [match] cannot both have values.");
  }

  @Override
  protected boolean runFilter(VariableValueSource item) {
    if(prefix != null) {
      if(item.getVariable().getName().startsWith(prefix)) return true;
    } else if(match != null) {
      Matcher matcher = matchPattern.matcher(item.getVariable().getName());
      if(matcher.matches()) return true;
    }

    return false;
  }

  private Object readResolve() {
    validateArguments(prefix, match);
    validateType();
    return this;
  }

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
      filter.validateType();
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
