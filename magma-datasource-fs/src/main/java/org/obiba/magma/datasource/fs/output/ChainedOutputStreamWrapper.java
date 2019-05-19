/*
 * Copyright (c) 2019 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.magma.datasource.fs.output;

import java.io.OutputStream;
import java.util.Arrays;
import java.util.List;

import org.obiba.magma.datasource.fs.OutputStreamWrapper;

import de.schlichtherle.io.File;

public class ChainedOutputStreamWrapper implements OutputStreamWrapper {

  List<OutputStreamWrapper> factories;

  public ChainedOutputStreamWrapper(List<OutputStreamWrapper> factories) {
    this.factories = factories;
  }

  public ChainedOutputStreamWrapper(OutputStreamWrapper... factories) {
    this(Arrays.asList(factories));
  }

  @Override
  public OutputStream wrap(OutputStream os, File file) {
    OutputStream wrapped = os;
    for(OutputStreamWrapper factory : factories) {
      wrapped = factory.wrap(wrapped, file);
    }
    return wrapped;
  }
}
