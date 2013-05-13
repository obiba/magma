package org.obiba.magma.datasource.neo4j.repository;

import org.obiba.magma.datasource.neo4j.domain.DatasourceNode;
import org.springframework.data.neo4j.repository.GraphRepository;
import org.springframework.data.neo4j.repository.RelationshipOperationsRepository;

public interface DatasourceRepository
    extends GraphRepository<DatasourceNode>, RelationshipOperationsRepository<DatasourceNode> {

  DatasourceNode findByName(String name);

}
