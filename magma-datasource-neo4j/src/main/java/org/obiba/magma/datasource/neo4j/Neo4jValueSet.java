package org.obiba.magma.datasource.neo4j;

import javax.annotation.Nonnull;

import org.obiba.magma.Timestamps;
import org.obiba.magma.Value;
import org.obiba.magma.ValueTable;
import org.obiba.magma.VariableEntity;
import org.obiba.magma.datasource.neo4j.domain.Timestamped;
import org.obiba.magma.support.ValueSetBean;
import org.obiba.magma.type.DateTimeType;
import org.springframework.util.Assert;

public class Neo4jValueSet extends ValueSetBean {

  private final Timestamps timestamps;

  public Neo4jValueSet(@Nonnull ValueTable table, @Nonnull VariableEntity entity,
      @Nonnull final Timestamped valueSetNode) {
    super(table, entity);
    Assert.notNull(valueSetNode, "valueSetNode cannot be null");
    timestamps = new Timestamps() {

      @Nonnull
      @Override
      public Value getLastUpdate() {
        return DateTimeType.get().valueOf(valueSetNode.getUpdated());
      }

      @Nonnull
      @Override
      public Value getCreated() {
        return DateTimeType.get().valueOf(valueSetNode.getCreated());
      }
    };
  }

  @Override
  public Timestamps getTimestamps() {
    return timestamps;
  }
}
