/*
 * Copyright (c) 2017 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.magma.datasource.hibernate.cfg;

import org.hibernate.AssertionFailure;
import org.hibernate.cfg.ImprovedNamingStrategy;
import org.hibernate.internal.util.StringHelper;

public class MagmaNamingStrategy extends ImprovedNamingStrategy {

  private static final long serialVersionUID = -7910076102639471779L;

  /**
   * Overridden to generate the column name: &lt;tableName&gt;_&lt;columnName&gt;
   */
  @Override
  public String foreignKeyColumnName(String propertyName, String propertyEntityName, String propertyTableName,
      String referencedColumnName) {
    String header = propertyName == null ? propertyTableName : StringHelper.unqualify(propertyName);
    if(header == null) throw new AssertionFailure("NamingStrategy not properly filled");
    return columnName(header) + "_" + referencedColumnName; // not used for backward compatibility
  }

}
