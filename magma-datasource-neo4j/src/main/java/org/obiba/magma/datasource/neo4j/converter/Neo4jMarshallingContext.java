package org.obiba.magma.datasource.neo4j.converter;

import org.obiba.magma.datasource.neo4j.domain.DatasourceNode;
import org.obiba.magma.datasource.neo4j.domain.ValueSetNode;
import org.obiba.magma.datasource.neo4j.domain.ValueTableNode;
import org.obiba.magma.datasource.neo4j.domain.VariableNode;

public class Neo4jMarshallingContext {

  private DatasourceNode datasource;

  private ValueTableNode valueTable;

  private ValueSetNode valueSet;

  private VariableNode variable;

  public static Neo4jMarshallingContext create(DatasourceNode datasource) {
    return create(datasource, null);
  }

  public static Neo4jMarshallingContext create(DatasourceNode datasource, ValueTableNode valueTable) {
    Neo4jMarshallingContext context = new Neo4jMarshallingContext();
    context.datasource = datasource;
    context.valueTable = valueTable;
    return context;
  }

  public DatasourceNode getDatasource() {
    return datasource;
  }

  public ValueTableNode getValueTable() {
    return valueTable;
  }

  public ValueSetNode getValueSet() {
    return valueSet;
  }

  public VariableNode getVariable() {
    return variable;
  }
}
