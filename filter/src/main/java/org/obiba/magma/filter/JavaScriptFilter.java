package org.obiba.magma.filter;

import org.obiba.magma.ValueSet;

import com.thoughtworks.xstream.annotations.XStreamAlias;

@XStreamAlias("script")
public class JavaScriptFilter extends AbstractFilter<ValueSet> {

  private String javascript;

  @Override
  boolean runFilter(ValueSet item) {
    // TODO Auto-generated method stub
    return false;
  }
}
