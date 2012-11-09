package org.obiba.magma.datasource.hibernate.domain;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

@Entity
@Table(name = "value_set", uniqueConstraints = { @UniqueConstraint(columnNames = { "value_table_id", "variable_entity_id" }) })
public class ValueSetState extends AbstractTimestampedEntity {

  private static final Logger log = LoggerFactory.getLogger(ValueSetState.class);

  private static final long serialVersionUID = 1L;

  @ManyToOne(optional = false)
  @JoinColumn(name = "value_table_id")
  private ValueTableState valueTable;

  @ManyToOne(optional = false)
  @JoinColumn(name = "variable_entity_id")
  private VariableEntityState variableEntity;

  @OneToMany(cascade = CascadeType.ALL, mappedBy = "id.valueSet", orphanRemoval = true)
  private Set<ValueSetValue> values;

  private transient Map<String, ValueSetValue> valueMap;

  public ValueSetState() {
    super();
  }

  public ValueSetState(ValueTableState valueTable, VariableEntityState variableEntity) {
    super();
    this.valueTable = valueTable;
    this.variableEntity = variableEntity;
  }

  public VariableEntityState getVariableEntity() {
    return variableEntity;
  }

  public ValueTableState getValueTable() {
    return valueTable;
  }

  public Set<ValueSetValue> getValues() {
    return values != null ? values : (values = Sets.newLinkedHashSet());
  }

  public synchronized Map<String, ValueSetValue> getValueMap() {
    return valueMap != null ? valueMap : valuesAsMap();
  }

  private synchronized Map<String, ValueSetValue> valuesAsMap() {
    if(valueMap == null) {
      Map<String, ValueSetValue> valueMap = Maps.newHashMap();
      for(ValueSetValue vsv : getValues()) {
        log.info("{}={}", vsv.getVariable().getName(), vsv.getValue().toString());
        valueMap.put(vsv.getVariable().getName(), vsv);
      }
      this.valueMap = Collections.unmodifiableMap(valueMap);
    }
    return valueMap;
  }

}
