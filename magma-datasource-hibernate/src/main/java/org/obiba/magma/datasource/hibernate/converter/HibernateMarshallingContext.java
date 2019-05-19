/*
 * Copyright (c) 2019 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.magma.datasource.hibernate.converter;

import javax.annotation.Nullable;

import org.hibernate.SessionFactory;
import org.obiba.magma.AttributeAwareBuilder;
import org.obiba.magma.datasource.hibernate.domain.AbstractAttributeAwareEntity;
import org.obiba.magma.datasource.hibernate.domain.DatasourceState;
import org.obiba.magma.datasource.hibernate.domain.ValueSetState;
import org.obiba.magma.datasource.hibernate.domain.ValueTableState;
import org.obiba.magma.datasource.hibernate.domain.VariableState;

public class HibernateMarshallingContext {

  private DatasourceState datasourceState;

  private ValueTableState valueTable;

  private ValueSetState valueSet;

  private VariableState variable;

  private AttributeAwareBuilder<?> attributeAwareBuilder;

  private AbstractAttributeAwareEntity attributeAwareEntity;

  private SessionFactory sessionFactory;

  public static HibernateMarshallingContext create(SessionFactory sessionFactory, DatasourceState datasourceState) {
    return create(sessionFactory, datasourceState, null);
  }

  public static HibernateMarshallingContext create(SessionFactory sessionFactory, DatasourceState datasourceState,
      @Nullable ValueTableState valueTable) {
    if(sessionFactory == null) throw new IllegalArgumentException("sessionFactory cannot be null");
    if(datasourceState == null) throw new IllegalArgumentException("datasourceState cannot be null");

    HibernateMarshallingContext context = new HibernateMarshallingContext();
    context.sessionFactory = sessionFactory;
    context.datasourceState = datasourceState;
    context.valueTable = valueTable;
    return context;
  }

  public DatasourceState getDatasourceState() {
    return datasourceState;
  }

  public ValueTableState getValueTable() {
    return valueTable;
  }

  public SessionFactory getSessionFactory() {
    return sessionFactory;
  }

  public ValueSetState getValueSet() {
    return valueSet;
  }

  public void setValueSet(ValueSetState valueSet) {
    this.valueSet = valueSet;
  }

  public VariableState getVariable() {
    return variable;
  }

  public void setVariable(VariableState variable) {
    this.variable = variable;
    attributeAwareEntity = variable;
  }

  public void setAttributeAwareEntity(AbstractAttributeAwareEntity adaptable) {
    attributeAwareEntity = adaptable;
  }

  public AbstractAttributeAwareEntity getAttributeAwareEntity() {
    return attributeAwareEntity;
  }

  public AttributeAwareBuilder<?> getAttributeAwareBuilder() {
    return attributeAwareBuilder;
  }

  public void setAttributeAwareBuilder(AttributeAwareBuilder<?> attributeAwareBuilder) {
    this.attributeAwareBuilder = attributeAwareBuilder;
  }

}
