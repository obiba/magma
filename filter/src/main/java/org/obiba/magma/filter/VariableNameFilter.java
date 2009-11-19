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

  public VariableNameFilter(String prefix, String match) {
    this.prefix = prefix;
    this.match = match;
    if(match != null) matchPattern = Pattern.compile(match);
  }

  @Override
  boolean runFilter(VariableValueSource item) {
    if(prefix != null) {
      if(item.getVariable().getName().startsWith(prefix)) return true;
    } else if(match != null) {
      Matcher matcher = matchPattern.matcher(item.getVariable().getName());
      if(matcher.matches()) return true;
    }

    return false;
  }
}
