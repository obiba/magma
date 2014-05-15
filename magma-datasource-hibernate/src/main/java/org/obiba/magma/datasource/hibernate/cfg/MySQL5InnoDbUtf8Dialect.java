/*
 * Copyright (c) 2014 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.magma.datasource.hibernate.cfg;

import org.hibernate.dialect.MySQL5InnoDBDialect;

public class MySQL5InnoDbUtf8Dialect extends MySQL5InnoDBDialect {

  @Override
  public String getTableTypeString() {
    return super.getTableTypeString() + " DEFAULT CHARACTER SET utf8 COLLATE utf8_bin";
  }

}
