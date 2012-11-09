/*******************************************************************************
 * Copyright (c) 2012 OBiBa. All rights reserved.
 *  
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *  
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.magma.support;

import java.io.File;

import org.obiba.magma.Value;
import org.obiba.magma.ValueLoader;
import org.obiba.magma.ValueLoaderFactory;

/**
 * Given a string reference (url or file path), creates the corresponding {@link ValueLoader}.
 */
public class BinaryValueStreamLoaderFactory implements ValueLoaderFactory {

  private final File parent;

  public BinaryValueStreamLoaderFactory(File parent) {
    this.parent = parent;
  }

  @Override
  public ValueLoader create(Value valueRef, Integer occurrence) {
    String strValue = valueRef.toString();
    if(strValue.startsWith("http://")) {
      return new BinaryValueURLLoader(strValue);
    } else {
      return new BinaryValueFileLoader(parent, strValue);
    }
  }

}
