/*
 * Copyright (c) 2019 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.magma.support;

import org.junit.Test;

import static org.fest.assertions.api.Assertions.assertThat;

public class MagmaEngineTableResolverTest {

  @Test
  public void testValueOfQualifiedTableName() throws Exception {
    MagmaEngineTableResolver resolver = MagmaEngineTableResolver.valueOf("ironman.Participant");
    assertThat(resolver.getDatasourceName()).isEqualTo("ironman");
    assertThat(resolver.getTableName()).isEqualTo("Participant");
  }

  @Test
  public void testValueOfTableName() throws Exception {
    MagmaEngineTableResolver resolver = MagmaEngineTableResolver.valueOf("Participant");
    assertThat(resolver.getDatasourceName()).isNull();
    assertThat(resolver.getTableName()).isEqualTo("Participant");
  }

}
