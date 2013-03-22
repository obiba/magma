/*
 * Copyright (c) 2013 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.magma.test;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import javax.annotation.Nonnull;

/**
 *
 */
public class TestUtils {

  private TestUtils() {}

  @Nonnull
  public static File getFileFromResource(String path) {
    try {
      URL resource = TestUtils.class.getClassLoader().getResource(path);
      URI uri = resource == null ? null : resource.toURI();
      if(uri == null) throw new IllegalArgumentException("Cannot find file at " + path);
      return new File(uri);
    } catch(URISyntaxException e) {
      throw new IllegalArgumentException("Cannot find file at " + path);
    }
  }

}
