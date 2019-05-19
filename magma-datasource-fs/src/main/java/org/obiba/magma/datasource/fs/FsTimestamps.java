/*
 * Copyright (c) 2019 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.magma.datasource.fs;

import java.util.Date;

import javax.validation.constraints.NotNull;

import org.obiba.magma.Timestamps;
import org.obiba.magma.Value;
import org.obiba.magma.type.DateTimeType;

import de.schlichtherle.io.File;

public class FsTimestamps implements Timestamps {

  @NotNull
  private final File valueTableDirectory;

  public FsTimestamps(@NotNull File valueTableDirectory) {
    this.valueTableDirectory = valueTableDirectory;
  }

  @NotNull
  @Override
  public Value getCreated() {
    // Not currently possible to read a file creation timestamp. Coming in JDK 7 NIO.
    return DateTimeType.get().nullValue();
  }

  @NotNull
  @Override
  public Value getLastUpdate() {
    return valueTableDirectory.exists()
        ? DateTimeType.get().valueOf(new Date(valueTableDirectory.lastModified()))
        : DateTimeType.get().nullValue();
  }
}
