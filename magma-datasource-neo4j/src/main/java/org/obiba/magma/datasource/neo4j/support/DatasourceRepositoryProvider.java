package org.obiba.magma.datasource.neo4j.support;

import org.obiba.magma.datasource.neo4j.repository.DatasourceRepository;

public interface DatasourceRepositoryProvider {

  DatasourceRepository getDatasourceRepository();

}
