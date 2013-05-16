package org.obiba.magma.datasource.neo4j.converter;

public interface Neo4jConverter<TNode, TMagmaObject> {

  TNode marshal(TMagmaObject magmaObject, Neo4jMarshallingContext context);

  TMagmaObject unmarshal(TNode node, Neo4jMarshallingContext context);

}
