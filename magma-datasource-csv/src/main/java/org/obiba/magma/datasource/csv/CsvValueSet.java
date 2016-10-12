/*
 * Copyright (c) 2016 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.magma.datasource.csv;

import java.io.File;
import java.util.Map;

import javax.annotation.Nullable;

import org.obiba.magma.MagmaRuntimeException;
import org.obiba.magma.Value;
import org.obiba.magma.ValueLoaderFactory;
import org.obiba.magma.Variable;
import org.obiba.magma.VariableEntity;
import org.obiba.magma.support.BinaryValueStreamLoaderFactory;
import org.obiba.magma.support.DatasourceParsingException;
import org.obiba.magma.support.ValueSetBean;
import org.obiba.magma.type.BinaryType;

public class CsvValueSet extends ValueSetBean {

//  private static final Logger log = LoggerFactory.getLogger(CsvValueSet.class);

  private final Map<String, Integer> headerMap;

  private final String[] line;

  //
  public CsvValueSet(CsvValueTable table, VariableEntity entity, Map<String, Integer> headerMap, String[] line) {
    super(table, entity);
    this.headerMap = headerMap;
    this.line = line;
  }

  public Value getValue(Variable variable) {
    Value value = variable.getValueType().nullValue();
    if (line == null || line.length == 0) return value;

    Integer pos = headerMap.get(variable.getName());

    if(pos != null && pos < line.length) {
      String strValue = line[pos];
      if(strValue.length() > 0) {
        try {
          value = getValue(variable, strValue);
        } catch(MagmaRuntimeException e) {
          throw new DatasourceParsingException(
              "Unable to get value for entity " + getVariableEntity().getIdentifier() + " and variable " +
                  variable.getName() + ": " + e.getMessage(), e, "CsvUnableToGetVariableValueForEntity",
              getVariableEntity().getIdentifier(), variable.getName(), e.getMessage());
        }
      }
    }
    return value;
  }

  private Value getValue(Variable variable, String strValue) {
    return variable.getValueType().equals(BinaryType.get()) //
        ? getBinaryValue(variable, strValue) //
        : getAnyTypeValue(variable, strValue);
  }

  private Value getAnyTypeValue(Variable variable, String strValue) {
    return variable.isRepeatable() //
        ? variable.getValueType().sequenceOf(strValue) //
        : variable.getValueType().valueOf(strValue);
  }

  private Value getBinaryValue(Variable variable, String strValue) {
    ValueLoaderFactory factory = new BinaryValueStreamLoaderFactory(getParentFile());
    return variable.isRepeatable() //
        ? BinaryType.get().sequenceOfReferences(factory, strValue) //
        : BinaryType.get().valueOfReference(factory, strValue);
  }

  @Nullable
  private File getParentFile() {
    return ((CsvValueTable) getValueTable()).getParentFile();
  }

}
