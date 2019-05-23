/*
 * Copyright (c) 2019 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.magma.beans;

import com.google.common.collect.ImmutableList;
import org.obiba.magma.VariableEntity;
import org.obiba.magma.support.AbstractVariableEntityProvider;
import org.obiba.magma.support.VariableEntityBean;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.PropertyAccessorFactory;

import javax.validation.constraints.NotNull;
import java.util.List;

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
  public List<VariableEntity> getVariableEntities() {
    ImmutableList.Builder<VariableEntity> builder = new ImmutableList.Builder<>();
    for (Object bean : loadBeans()) {
      BeanWrapper bw = PropertyAccessorFactory.forBeanPropertyAccess(bean);

      Object entityId = bw.getPropertyValue(entityIdentifierPropertyPath);
      if (entityId == null) {
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
