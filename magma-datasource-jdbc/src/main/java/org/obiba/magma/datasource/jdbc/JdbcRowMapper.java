/*
 * Copyright (c) 2016 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.magma.datasource.jdbc;

import com.google.common.collect.Maps;
import org.obiba.magma.Value;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;

/**
 * Make a {@link Value} from each extracted data.
 */
public class JdbcRowMapper implements RowMapper<Map<String, Value>> {

  private JdbcValueTable table;

  public JdbcRowMapper(JdbcValueTable table) {
    this.table = table;
  }

  @Override
  public Map<String, Value> mapRow(ResultSet rs, int rowNum) throws SQLException {
    Map<String, Value> res = Maps.newHashMap();

    for (int i = 1; i <= rs.getMetaData().getColumnCount(); i++) {
      String variableName = table.getVariableName(rs.getMetaData().getColumnName(i));
      Value variableValue = SqlTypes.valueTypeFor(rs.getMetaData().getColumnType(i)).valueOf(rs.getObject(i));
      res.put(variableName, variableValue);
    }

    return res;
  }
}
