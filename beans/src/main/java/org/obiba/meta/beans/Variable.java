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

import org.obiba.meta.IVariable;
import org.obiba.meta.ValueType;

/**
 *
 */
public class Variable implements IVariable {

  private String name;

  private ValueType dataType;

  public Variable(ValueType dataType, String name) {
    this.dataType = dataType;
    this.name = name;
  }

  public ValueType getValueType() {
    return dataType;
  }

  public String getEntityType() {
    return null;
  }

  public String getMimeType() {
    return null;
  }

  public String getName() {
    return name;
  }

  public String getUnit() {
    return null;
  }

}
