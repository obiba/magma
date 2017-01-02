/*
 * Copyright (c) 2017 OBiBa. All rights reserved.
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

public class MagmaEngineVariableResolverTest {

  @Test
  public void testValueOfQualifiedTableName() throws Exception {
    MagmaEngineVariableResolver resolver = MagmaEngineVariableResolver.valueOf("ironman.Participant:");
    assertThat(resolver.getDatasourceName()).isEqualTo("ironman");
    assertThat(resolver.getTableName()).isEqualTo("Participant");
    assertThat(resolver.getVariableName()).isNull();
  }

  @Test
  public void testValueOfVariableName() throws Exception {
    MagmaEngineVariableResolver resolver = MagmaEngineVariableResolver.valueOf("SMOKER_STATUS");
    assertThat(resolver.getDatasourceName()).isNull();
    assertThat(resolver.getTableName()).isNull();
    assertThat(resolver.getVariableName()).isEqualTo("SMOKER_STATUS");
  }

  @Test
  public void testValueOfTableAndVariableName() throws Exception {
    MagmaEngineVariableResolver resolver = MagmaEngineVariableResolver.valueOf("Participant:SMOKER_STATUS");
    assertThat(resolver.getDatasourceName()).isNull();
    assertThat(resolver.getTableName()).isEqualTo("Participant");
    assertThat(resolver.getVariableName()).isEqualTo("SMOKER_STATUS");
  }

  @Test
  public void testValueOfDatasourceAndTableAndVariableName() throws Exception {
    MagmaEngineVariableResolver resolver = MagmaEngineVariableResolver.valueOf("ironman.Participant:SMOKER_STATUS");
    assertThat(resolver.getDatasourceName()).isEqualTo("ironman");
    assertThat(resolver.getTableName()).isEqualTo("Participant");
    assertThat(resolver.getVariableName()).isEqualTo("SMOKER_STATUS");
  }
}
