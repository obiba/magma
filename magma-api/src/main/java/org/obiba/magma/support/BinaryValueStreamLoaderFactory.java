/*
 * Copyright (c) 2016 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.magma.support;

import java.io.File;

import javax.annotation.Nullable;

import org.obiba.magma.Value;
import org.obiba.magma.ValueLoader;
import org.obiba.magma.ValueLoaderFactory;

/**
 * Given a string reference (url or file path), creates the corresponding {@link ValueLoader}.
 */
public class BinaryValueStreamLoaderFactory implements ValueLoaderFactory {

  private final File parent;

  public BinaryValueStreamLoaderFactory(@Nullable File parent) {
    this.parent = parent;
  }

  @SuppressWarnings("ConstantConditions")
  @Override
  public ValueLoader create(Value valueRef, @Nullable Integer occurrence) {
    if(valueRef.isNull()) return null;
    String strValue = valueRef.toString();
    return strValue.startsWith("http://")
        ? new BinaryValueURLLoader(strValue)
        : new BinaryValueFileLoader(parent, strValue);
  }

}
