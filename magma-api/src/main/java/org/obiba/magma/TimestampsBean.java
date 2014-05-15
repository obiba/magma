/*
 * Copyright (c) 2014 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.magma;

public class TimestampsBean implements Timestamps {

  private final Value created;

  private final Value lastUpdate;

  public TimestampsBean(Value created, Value lastUpdate) {
    this.created = created;
    this.lastUpdate = lastUpdate;
  }

  @Override
  public Value getCreated() {
    return created;
  }

  @Override
  public Value getLastUpdate() {
    return lastUpdate;
  }
}
