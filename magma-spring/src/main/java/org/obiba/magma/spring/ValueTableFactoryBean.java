/*
 * Copyright (c) 2019 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.magma.spring;

import org.obiba.magma.Datasource;
import org.obiba.magma.ValueTable;

public interface ValueTableFactoryBean {

  String getValueTableName();

  ValueTable buildValueTable(Datasource datasource);

}
