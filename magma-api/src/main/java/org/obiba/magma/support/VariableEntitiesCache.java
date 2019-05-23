/*
 * Copyright (c) 2019 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.magma.support;

import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.util.Set;

import org.obiba.magma.Value;
import org.obiba.magma.VariableEntity;

public class VariableEntitiesCache implements Serializable {

  private static final long serialVersionUID = 69918333951801112L;

  private List<VariableEntity> entities;

  private long lastUpdate;

  public VariableEntitiesCache(List<VariableEntity> entities, Value lastUpdate) {
    this(entities, ((Date)lastUpdate.getValue()).getTime());
  }

  public VariableEntitiesCache(List<VariableEntity> entities, long lastUpdate) {
    this.entities = entities;
    this.lastUpdate = lastUpdate;
  }

  public boolean isUpToDate(Value updated) {
    return lastUpdate == ((Date)updated.getValue()).getTime();
  }

  public List<VariableEntity> getEntities() {
    return entities;
  }
}
