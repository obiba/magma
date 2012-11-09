/*******************************************************************************
 * Copyright (c) 2012 OBiBa. All rights reserved.
 *  
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *  
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.magma.datasource.hibernate.converter;

import java.io.File;
import java.io.Serializable;

import org.json.JSONException;
import org.json.JSONObject;
import org.obiba.magma.MagmaRuntimeException;
import org.obiba.magma.Value;
import org.obiba.magma.ValueLoader;
import org.obiba.magma.ValueLoaderFactory;
import org.obiba.magma.support.BinaryValueFileHelper;
import org.obiba.magma.type.BinaryType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 */
public class HibernateValueLoaderFactory implements ValueLoaderFactory {

  private final File tableRoot;

  public HibernateValueLoaderFactory(File tableRoot) {
    this.tableRoot = tableRoot;
  }

  @Override
  public ValueLoader create(Value valueRef, Integer occurrence) {
    return new HibernateBinaryValueLoader(tableRoot, valueRef, occurrence);
  }

  private static final class HibernateBinaryValueLoader implements ValueLoader, Serializable {

    private static final long serialVersionUID = -1878370804810810064L;

    private static final Logger log = LoggerFactory.getLogger(HibernateBinaryValueLoader.class);

    private final File tableRoot;

    private final Value valueRef;

    private final Integer occurrence;

    private byte[] value;

    public HibernateBinaryValueLoader(File tableRoot, Value valueRef, Integer occurrence) {
      super();
      this.valueRef = valueRef;
      this.occurrence = occurrence;
      this.tableRoot = tableRoot;
    }

    @Override
    public boolean isNull() {
      return valueRef == null || valueRef.isNull();
    }

    @Override
    public Object getValue() {
      if(value == null) {
        if(valueRef.getValueType().equals(BinaryType.get())) {
          // legacy
          value = (byte[]) valueRef.getValue();
        } else {
          try {
            String path;
            if(valueRef.toString().startsWith("{")) {
              JSONObject properties = new JSONObject(valueRef.toString());
              path = properties.getString("path");
            } else {
              path = valueRef.toString();
            }
            value = BinaryValueFileHelper.readValue(tableRoot, path);
          } catch(JSONException e) {
            log.error("Failed loading JSON binary value reference", e);
            throw new MagmaRuntimeException(e);
          }
        }
      }
      return value;
    }
  }

}
