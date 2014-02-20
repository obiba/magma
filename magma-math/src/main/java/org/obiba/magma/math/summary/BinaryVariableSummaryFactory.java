package org.obiba.magma.math.summary;

import javax.validation.constraints.NotNull;

import org.obiba.magma.ValueSource;
import org.obiba.magma.ValueTable;
import org.obiba.magma.Variable;

public class BinaryVariableSummaryFactory extends AbstractVariableSummaryFactory<BinaryVariableSummary> {

  private Integer offset;

  private Integer limit;

  @NotNull
  @Override
  public String getCacheKey() {
    return getCacheKey(getVariable(), getTable(), offset, limit);
  }

  @SuppressWarnings("PMD.ExcessiveParameterList")
  public static String getCacheKey(Variable variable, ValueTable table, Integer offset, Integer limit) {
    String key = variable.getVariableReference(table);
    if(offset != null) key += ";o=" + offset;
    if(limit != null) key += ";l=" + limit;
    return key;
  }

  @NotNull
  @Override
  public BinaryVariableSummary getSummary() {
    return new BinaryVariableSummary.Builder(getVariable()) //
        .filter(offset, limit) //
        .addTable(getTable(), getValueSource()) //
        .build();
  }

  public Integer getOffset() {
    return offset;
  }

  public void setOffset(Integer offset) {
    this.offset = offset;
  }

  public Integer getLimit() {
    return limit;
  }

  public void setLimit(Integer limit) {
    this.limit = limit;
  }

  @SuppressWarnings("ParameterHidesMemberVariable")
  public static class Builder {

    private final BinaryVariableSummaryFactory factory = new BinaryVariableSummaryFactory();

    public Builder variable(Variable variable) {
      factory.setVariable(variable);
      return this;
    }

    public Builder table(ValueTable table) {
      factory.setTable(table);
      return this;
    }

    public Builder valueSource(ValueSource valueSource) {
      factory.setValueSource(valueSource);
      return this;
    }

    public Builder offset(Integer offset) {
      factory.offset = offset;
      return this;
    }

    public Builder limit(Integer limit) {
      factory.limit = limit;
      return this;
    }

    public BinaryVariableSummaryFactory build() {
      return factory;
    }
  }

}
