package org.obiba.magma.support;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;

import org.obiba.magma.AbstractAttributeAware;
import org.obiba.magma.Attribute;
import org.obiba.magma.Datasource;
import org.obiba.magma.NoSuchValueTableException;
import org.obiba.magma.Value;
import org.obiba.magma.ValueTable;
import org.obiba.magma.ValueTableWriter;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.LinkedListMultimap;
import com.google.common.collect.ListMultimap;

public abstract class AbstractDatasource extends AbstractAttributeAware implements Datasource {

  private String name;

  private String type;

  private Set<ValueTable> valueTables = new LinkedHashSet<ValueTable>(100);

  private ListMultimap<String, Attribute> attributes = LinkedListMultimap.create();

  protected AbstractDatasource(String name, String type) {
    if(name == null) throw new NullPointerException("name cannot be null");
    if(type == null) throw new NullPointerException("type cannot be null");
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
  public boolean hasValueTable(String name) {
    for(ValueTable vt : getValueTables()) {
      if(vt.getName().equals(name)) {
        return true;
      }
    }
    return false;
  }

  @Override
  public ValueTable getValueTable(final String name) throws NoSuchValueTableException {
    try {
      return Iterables.find(getValueTables(), new Predicate<ValueTable>() {
        @Override
        public boolean apply(ValueTable input) {
          return name.equals(input.getName());
        }
      });
    } catch(NoSuchElementException e) {
      throw new NoSuchValueTableException(getName(), name);
    }
  }

  @Override
  public void initialise() {
    List<DatasourceParsingException> parsingErrors = new ArrayList<DatasourceParsingException>();
    onInitialise();
    for(String valueTable : getValueTableNames()) {
      ValueTable vt = initialiseValueTable(valueTable);
      try {
        Initialisables.initialise(vt);
        addValueTable(vt);
      } catch(DatasourceParsingException pe) {
        parsingErrors.add((DatasourceParsingException) pe);
      }
    }
    if(parsingErrors.size() > 0) {
      DatasourceParsingException parent = new DatasourceParsingException("Errors while parsing tables of datasource: " + getName(), //
      "DatasourceDefinitionErrors", getName());
      parent.setChildren(parsingErrors);
      throw parent;
    }
  }

  @Override
  public void dispose() {
    Disposables.dispose(getValueTables());
    onDispose();
  }

  protected void addValueTable(ValueTable vt) {
    valueTables.add(vt);
  }

  @Override
  public ValueTableWriter createWriter(String tableName, String entityType) {
    throw new UnsupportedOperationException("createWriter() is not supported by datasource of type " + getType());
  }

  @Override
  public void setAttributeValue(String name, Value value) {
    Attribute attribute = Attribute.Builder.newAttribute(name).withValue(value).build();

    List<Attribute> attributesForName = getInstanceAttributes().get(name);
    if(!attributesForName.isEmpty()) {
      attributesForName.set(0, attribute);
    } else {
      attributesForName.add(attribute);
    }
  }

  @Override
  protected ListMultimap<String, Attribute> getInstanceAttributes() {
    return attributes;
  }

  protected void onInitialise() {

  }

  protected void onDispose() {

  }

  protected abstract Set<String> getValueTableNames();

  protected abstract ValueTable initialiseValueTable(String tableName);

}
