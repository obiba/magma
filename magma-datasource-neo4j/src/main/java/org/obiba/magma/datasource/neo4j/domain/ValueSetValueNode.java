package org.obiba.magma.datasource.neo4j.domain;

import org.springframework.data.neo4j.annotation.NodeEntity;

@NodeEntity
public class ValueSetValueNode extends AbstractTimestampedGraphItem {

  private ValueSetNode valueSet;

  private VariableNode variable;

  private ValueNode value;

}
