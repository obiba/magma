/*
 * Copyright (c) 2017 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.magma.datasource.limesurvey;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;

import javax.validation.constraints.NotNull;

import org.obiba.magma.Value;
import org.obiba.magma.ValueTable;
import org.obiba.magma.ValueType;
import org.obiba.magma.VariableEntity;
import org.obiba.magma.support.ValueSetBean;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ResultSetExtractor;

import com.google.common.collect.Maps;

class LimesurveyValueSet extends ValueSetBean {

  private Map<String, Object> cache;

  LimesurveyValueSet(ValueTable table, VariableEntity entity) {
    super(table, entity);
  }

  Value getValue(ValueType type, String field) {
    loadValues();
    Object object = cache.get(field);
    return type.valueOf("".equals(object) ? null : object);
  }

  private synchronized void loadValues() {
    if(cache == null) {
      LimesurveyValueTable limeValueTable = getValueTable();
      cache = Maps.newHashMap();
      String id = getVariableEntity().getIdentifier();
      getValueTable().getDatasource().getJdbcTemplate()
          .query("SELECT * FROM " + limeValueTable.quoteAndPrefix("survey_" + limeValueTable.getSid()) +
              " WHERE token = ?", new Object[] { id }, new ResultSetExtractor<Void>() {
            @Override
            public Void extractData(ResultSet rs) throws SQLException, DataAccessException {
              if(rs.next()) {
                for(int i = 1; i <= rs.getMetaData().getColumnCount(); i++) {
                  String column = rs.getMetaData().getColumnName(i);
                  Object object = rs.getObject(i);
                  cache.put(column, object);
                }
              }
              return null;
            }
          });
    }
  }

  @NotNull
  @Override
  public LimesurveyValueTable getValueTable() {
    return (LimesurveyValueTable) super.getValueTable();
  }

}
