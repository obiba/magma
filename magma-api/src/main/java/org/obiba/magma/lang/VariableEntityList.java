package org.obiba.magma.lang;

import com.google.common.collect.Sets;
import org.obiba.magma.VariableEntity;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * A list of unique values, just like a set but with list interface.
 */
public class VariableEntityList extends ArrayList<VariableEntity> {

  private static final long serialVersionUID = -4415279469444174L;

  private Set<String> ids = Sets.newHashSet();

  @Override
  public boolean add(VariableEntity entity) {
    if (contains(entity))
      return false;
    boolean added = super.add(entity);
    if (added) ids.add(entity.getIdentifier());
    return added;
  }

  @Override
  public boolean addAll(Collection<? extends VariableEntity> collection) {
    List<VariableEntity> entities = collection.stream().distinct().filter(ve -> !contains(ve)).collect(Collectors.toList());
    ids.addAll(entities.stream().map(VariableEntity::getIdentifier).collect(Collectors.toList()));
    return super.addAll(entities);
  }

  @Override
  public boolean contains(Object o) {
    if (!(o instanceof VariableEntity)) return false;
    if (ids.isEmpty()) return false;
    return ids.contains(((VariableEntity)o).getIdentifier());
  }

  @Override
  public boolean remove(Object o) {
    if (!(o instanceof VariableEntity)) return false;
    boolean removed = super.remove(o);
    if (removed) ids.remove(((VariableEntity)o).getIdentifier());
    return removed;
  }

  @Override
  public void clear() {
    ids.clear();
    super.clear();
  }
}