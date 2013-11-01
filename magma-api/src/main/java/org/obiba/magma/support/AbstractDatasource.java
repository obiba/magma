package org.obiba.magma.support;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;

import javax.annotation.Nonnull;

import org.obiba.magma.AbstractAttributeAware;
import org.obiba.magma.Attribute;
import org.obiba.magma.Datasource;
import org.obiba.magma.NoSuchValueTableException;
import org.obiba.magma.Timestamps;
import org.obiba.magma.Value;
import org.obiba.magma.ValueTable;
import org.obiba.magma.ValueTableWriter;
import org.obiba.magma.type.DateTimeType;

import com.google.common.base.Preconditions;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.LinkedListMultimap;
import com.google.common.collect.ListMultimap;

public abstract class AbstractDatasource extends AbstractAttributeAware implements Datasource {

  private final String name;

  private final String type;

  private final Set<ValueTable> valueTables = new LinkedHashSet<ValueTable>(100);

  private final ListMultimap<String, Attribute> attributes = LinkedListMultimap.create();

  protected AbstractDatasource(@Nonnull String name, @Nonnull String type) {
    Preconditions.checkNotNull(name, "name cannot be null");
    Preconditions.checkNotNull(type, "type cannot be null");
    this.name = name;
    this.type = type;
  }

  @Override
  public String getName() {
    return name;
  }

  @Override
  public String getType() {
    return type;
  }

  @Override
  public Set<ValueTable> getValueTables() {
    return Collections.unmodifiableSet(valueTables);
  }

  @Override
  public boolean hasValueTable(String tableName) {
    for(ValueTable vt : getValueTables()) {
      if(vt.getName().equals(tableName)) {
        return true;
      }
    }
    return false;
  }

  @Override
  public boolean hasEntities() {
    for(ValueTable vt : getValueTables()) {
      if (!vt.getVariableEntities().isEmpty()) return true;
    }
    return false;
  }

  @Override
  public ValueTable getValueTable(final String tableName) throws NoSuchValueTableException {
    try {
      return Iterables.find(getValueTables(), new Predicate<ValueTable>() {
        @Override
        public boolean apply(ValueTable input) {
          return tableName.equals(input.getName());
        }
      });
    } catch(NoSuchElementException e) {
      throw new NoSuchValueTableException(getName(), tableName);
    }
  }

  @Override
  public void initialise() {
    Collection<DatasourceParsingException> parsingErrors = new ArrayList<DatasourceParsingException>();
    onInitialise();
    for(String valueTable : getValueTableNames()) {
      ValueTable vt = initialiseValueTable(valueTable);
      try {
        Initialisables.initialise(vt);
        addValueTable(vt);
      } catch(DatasourceParsingException pe) {
        parsingErrors.add(pe);
      }
    }
    if(parsingErrors.size() > 0) {
      DatasourceParsingException parent = new DatasourceParsingException(
          "Errors while parsing tables of datasource: " + getName(), "DatasourceDefinitionErrors", getName());
      parent.setChildren(parsingErrors);
      throw parent;
    }
  }

  @Override
  public void dispose() {
    Disposables.dispose(getValueTables());
    onDispose();
  }

  @Nonnull
  @Override
  public ValueTableWriter createWriter(@Nonnull String tableName, @Nonnull String entityType) {
    throw new UnsupportedOperationException("createWriter() is not supported by datasource of type " + getType());
  }

  @Override
  public void setAttributeValue(String name, Value value) {
    Attribute attribute = Attribute.Builder.newAttribute(name).withValue(value).build();

    List<Attribute> attributesForName = getInstanceAttributes().get(name);
    if(attributesForName.isEmpty()) {
      attributesForName.add(attribute);
    } else {
      attributesForName.set(0, attribute);
    }
  }

  @Override
  public boolean canDropTable(String tableName) {
    return false;
  }

  @Override
  public void dropTable(@Nonnull String tableName) {
    throw new UnsupportedOperationException("cannot drop table");
  }

  @Override
  public boolean canDrop() {
    return false;
  }

  @Override
  public void drop() {
    throw new UnsupportedOperationException("cannot drop datasource");
  }

  @Override
  public Timestamps getTimestamps() {
    return new UnionTimestamps(getValueTables());
  }

  @Override
  protected ListMultimap<String, Attribute> getInstanceAttributes() {
    return attributes;
  }

  protected void addValueTable(ValueTable vt) {
    valueTables.add(vt);
  }

  protected void removeValueTable(String tableName) {
    ValueTable toRemove = getValueTable(tableName);
    valueTables.remove(toRemove);
    Disposables.dispose(toRemove);
  }

  @SuppressWarnings("NoopMethodInAbstractClass")
  protected void onInitialise() {

  }

  @SuppressWarnings("NoopMethodInAbstractClass")
  protected void onDispose() {

  }

  protected abstract Set<String> getValueTableNames();

  protected abstract ValueTable initialiseValueTable(String tableName);

}
