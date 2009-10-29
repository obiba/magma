package org.obiba.meta.support;

import java.util.List;
import java.util.Set;

import org.obiba.meta.Collection;
import org.obiba.meta.CollectionConnector;
import org.obiba.meta.NoSuchVariableException;
import org.obiba.meta.ValueSetReference;
import org.obiba.meta.VariableValueSource;

import com.google.common.collect.ImmutableSet;

public class CollectionBean implements Collection {

  private String name;

  private List<CollectionConnector> connectors;

  public List<CollectionConnector> getConnectors() {
    return connectors;
  }

  public void setConnectors(List<CollectionConnector> connectors) {
    this.connectors = connectors;
  }

  @Override
  public Set<String> getEntityTypes() {
    ImmutableSet.Builder<String> builder = ImmutableSet.builder();
    for(CollectionConnector connector : getConnectors()) {
      builder.add(connector.getEntityType());
    }
    return builder.build();
  }

  @Override
  public String getName() {
    return name;
  }

  @Override
  public Set<ValueSetReference> getValueSetReferences(String entityType) {
    ImmutableSet.Builder<ValueSetReference> builder = ImmutableSet.builder();
    for(CollectionConnector connector : getConnectors()) {
      if(connector.isForEntityType(entityType)) {
        builder.addAll(connector.getValueSetReferences());
      }
    }
    return builder.build();
  }

  @Override
  public Set<VariableValueSource> getVariableValueSources(String entityType) {
    ImmutableSet.Builder<VariableValueSource> builder = ImmutableSet.builder();
    for(CollectionConnector connector : getConnectors()) {
      if(connector.isForEntityType(entityType)) {
        builder.addAll(connector.getVariableValueSources());
      }
    }
    return builder.build();
  }

  @Override
  public VariableValueSource getVariableValueSource(String entityType, String variableName) {
    for(CollectionConnector connector : getConnectors()) {
      if(connector.isForEntityType(entityType)) {
        VariableValueSource source = connector.getVariableValueSource(variableName);
        if(source != null) {
          return source;
        }
      }
    }
    throw new NoSuchVariableException(getName(), variableName);
  }

}
