/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.meta;

/**
 *
 */
public interface IVariable {

  public static class Builder {

    private Variable variable = new Variable();

    public Builder(String name, ValueType type) {
      variable.name = name;
      variable.valueType = type;
    }

    public static Builder newVariable(String name, ValueType type) {
      return new Builder(name, type);
    }

    public IVariable build() {
      return variable;
    }
  }

  public String getName();

  public String getEntityType();

  public ValueType getValueType();

  /**
   * The SI code of the measurement unit if applicable.
   * @return unit
   */
  public String getUnit();

  /**
   * The IANA mime-type of binary data if applicable.
   * @return the IANA mime-type
   */
  public String getMimeType();

  public String getAttribute(String name);

  /**
   * Used when this variable value is a pointer to another {@code IVariableEntity}. The value is considered to point to
   * the referenced entity's {@code identifier}.
   * 
   * @return the {@code entityType} that this value points to, this method returns null when the variable doesn't point
   * to another entity.
   */
  public String getReferencedEntityType();

}
