package org.obiba.magma.datasource.limesurvey;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

import org.obiba.magma.Datasource;
import org.obiba.magma.Initialisable;
import org.obiba.magma.NoSuchValueSetException;
import org.obiba.magma.Timestamps;
import org.obiba.magma.Value;
import org.obiba.magma.ValueSet;
import org.obiba.magma.ValueType;
import org.obiba.magma.Variable;
import org.obiba.magma.VariableEntity;
import org.obiba.magma.VariableValueSource;
import org.obiba.magma.VectorSource;
import org.obiba.magma.support.AbstractValueTable;
import org.obiba.magma.support.AbstractVariableEntityProvider;
import org.obiba.magma.support.VariableEntityProvider;
import org.springframework.jdbc.core.JdbcTemplate;

public class LimesurveyValueTable extends AbstractValueTable {

  public LimesurveyValueTable(Datasource datasource, String name) {
    super(datasource, name);
  }

  public LimesurveyValueTable(Datasource datasource, String name, VariableEntityProvider variableEntityProvider) {
    super(datasource, name, variableEntityProvider);
  }

  @Override
  public void initialise() {
    super.initialise();
    initialiseVariableValueSources();
  }

  private void initialiseVariableValueSources() {
    JdbcTemplate jdbcTemplate = ((LimesurveyDatasource) getDatasource()).getJdbcTemplate();
  }

  @Override
  public ValueSet getValueSet(VariableEntity entity) throws NoSuchValueSetException {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Timestamps getTimestamps() {
    // TODO Auto-generated method stub
    return null;
  }

  class LimesurveyVariableEntityProvider extends AbstractVariableEntityProvider implements Initialisable {

    private Set<VariableEntity> entities = new LinkedHashSet<VariableEntity>();

    protected LimesurveyVariableEntityProvider(String entityType) {
      super(entityType);
    }

    @Override
    public void initialise() {
      // TODO Auto-generated method stub

    }

    @Override
    public Set<VariableEntity> getVariableEntities() {
      return Collections.unmodifiableSet(entities);
    }

  }

  class LimesurveyVariableValueSource implements VariableValueSource {

    private Variable variable;

    public LimesurveyVariableValueSource(Variable variable) {
      this.variable = variable;
    }

    @Override
    public ValueType getValueType() {
      // TODO Auto-generated method stub
      return null;
    }

    @Override
    public Value getValue(ValueSet valueSet) {
      // TODO Auto-generated method stub
      return null;
    }

    @Override
    public VectorSource asVectorSource() {
      // TODO Auto-generated method stub
      return null;
    }

    @Override
    public Variable getVariable() {
      return variable;
    }

  }

}
