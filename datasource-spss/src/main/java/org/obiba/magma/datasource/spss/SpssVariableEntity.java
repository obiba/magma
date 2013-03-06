/*
 * Copyright (c) 2012 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.magma.datasource.spss;

import org.obiba.magma.support.VariableEntityBean;

@edu.umd.cs.findbugs.annotations.SuppressWarnings("EQ_DOESNT_OVERRIDE_EQUALS")
public class SpssVariableEntity extends VariableEntityBean {

  private final int variableIndex;

  public SpssVariableEntity(String entityType, String entityIdentifier, int variableIndex) {
    super(entityType, entityIdentifier);
    this.variableIndex = variableIndex;
  }

  public int getVariableIndex() {
    return variableIndex;
  }

}
