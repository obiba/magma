/*
 * Copyright (c) 2016 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.magma.beans;

import java.util.Set;

import javax.validation.constraints.NotNull;

import org.obiba.magma.VariableEntity;
import org.obiba.magma.support.AbstractVariableEntityProvider;
import org.obiba.magma.support.VariableEntityBean;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.PropertyAccessorFactory;

import com.google.common.collect.ImmutableSet;

public abstract class AbstractBeanVariableEntityProvider<T> extends AbstractVariableEntityProvider {

  private final String entityIdentifierPropertyPath;

  public AbstractBeanVariableEntityProvider(String entityType, String entityIdentifierPropertyPath) {
    super(entityType);
    this.entityIdentifierPropertyPath = entityIdentifierPropertyPath;
  }

  public String getEntityIdentifierPropertyPath() {
    return entityIdentifierPropertyPath;
  }

  @NotNull
  @Override
  public Set<VariableEntity> getVariableEntities() {
    ImmutableSet.Builder<VariableEntity> builder = new ImmutableSet.Builder<>();
    for(Object bean : loadBeans()) {
      BeanWrapper bw = PropertyAccessorFactory.forBeanPropertyAccess(bean);

      Object entityId = bw.getPropertyValue(entityIdentifierPropertyPath);
      if(entityId == null) {
        throw new NullPointerException("entity identifier cannot be null");
      }
      builder.add(new VariableEntityBean(getEntityType(), entityId.toString()));
    }
    return builder.build();
  }

  /**
   * @return
   */
  protected abstract Iterable<T> loadBeans();

}
