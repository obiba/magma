package org.obiba.magma.datasource.neo4j.domain;

import org.springframework.data.neo4j.annotation.GraphId;

public abstract class AbstractNode {

  @GraphId
  protected Long nodeId;

}
