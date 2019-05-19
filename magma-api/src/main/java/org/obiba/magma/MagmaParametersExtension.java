/*
 * Copyright (c) 2019 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.magma;

import com.google.common.collect.Maps;

import java.util.Map;

public class MagmaParametersExtension implements MagmaEngineExtension {

  private static final long serialVersionUID = -6089345777332195129L;

  private transient Map<String, Object> parameters;

  public MagmaParametersExtension() {
  }

  public MagmaParametersExtension(Map<String, Object> parameters) {
    this.parameters = parameters;
  }

  @Override
  public String getName() {
    return "magma-parameters";
  }

  @Override
  public void initialise() {

  }

  public Map<String, Object> getParameters() {
    return parameters == null ? parameters = Maps.newHashMap() : parameters;
  }

  public void setParameter(String name, Object value) {
    getParameters().put(name, value);
  }

  public boolean hasParameter(String name) {
    return getParameters().containsKey(name);
  }

  public Object getParameter(String name) {
    return getParameters().get(name);
  }

  public String getParameterString(String name) {
    return hasParameter(name) ? getParameters().get(name).toString() : null;
  }

  public Long getParameterLong(String name) {
    return hasParameter(name) ? (Long) getParameters().get(name) : null;
  }

  public Integer getParameterInteger(String name) {
    return hasParameter(name) ? (Integer) getParameters().get(name) : null;
  }

  public Double getParameterDouble(String name) {
    return hasParameter(name) ? (Double) getParameters().get(name) : null;
  }

  public Boolean getParameterBoolean(String name) {
    return hasParameter(name) ? (Boolean) getParameters().get(name) : null;
  }

}
