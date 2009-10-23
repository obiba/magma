/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.meta.beans;

import org.obiba.meta.IVariableEntity;

/**
 * An implementation of {@link IVariableEntity} for beans that do not directly implement the {@code IVariableEntity}
 * interface. This allows using any bean type within the variable framework.
 */
public abstract class VariableEntityBeanAdaptor<T> implements IVariableEntity {

  /** The adapted bean instance. Its type does not implement {@code IVariableEntity} */
  protected T adaptedBean;

  protected VariableEntityBeanAdaptor(T adaptedBean) {
    this.adaptedBean = adaptedBean;
  }

  public abstract String getIdentifier();

  public abstract String getType();

  public T getAdaptedBean() {
    return adaptedBean;
  }

}
