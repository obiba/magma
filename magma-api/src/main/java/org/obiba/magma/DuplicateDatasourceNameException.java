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

public class DuplicateDatasourceNameException extends MagmaRuntimeException {

  private static final long serialVersionUID = 2416927981990092192L;

  private final Datasource existing;

  private final Datasource duplicate;

  public DuplicateDatasourceNameException(Datasource existing, Datasource duplicate) {
    super("Datasource with name '" + existing.getName() + "' already exists in MagmaEngine.");
    this.existing = existing;
    this.duplicate = duplicate;
  }

  public Datasource getExisting() {
    return existing;
  }

  public Datasource getDuplicate() {
    return duplicate;
  }
}
