/*
 * Copyright (c) 2016 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.magma.datasource.r;

import com.google.common.base.Strings;
import org.obiba.magma.*;
import org.obiba.magma.support.AbstractValueTable;
import org.obiba.magma.support.VariableEntityProvider;
import org.rosuda.REngine.REXP;

import java.util.Set;

/**
 * A {@link org.obiba.magma.ValueTable} built on a R data.frame.
 */
public class RValueTable extends AbstractValueTable {

  private static final String PARTICIPANT = "Participant";

  private final REXP rexp;

  private final String idColumn;

  public RValueTable(Datasource datasource, String name, REXP rexp) {
    this(datasource, name, rexp, PARTICIPANT, "id");
    ((RDatasource) datasource).setValueTable(this);
  }

  public RValueTable(Datasource datasource, String name, REXP rexp, String entityType, String idColumn) {
    super(datasource, name);
    this.rexp = rexp;
    this.idColumn = idColumn;
    setVariableEntityProvider(new RVariableEntityProvider(entityType));
  }

  @Override
  public ValueSet getValueSet(VariableEntity entity) throws NoSuchValueSetException {
    return null;
  }

  @Override
  public Timestamps getTimestamps() {
    return null;
  }

  private class RVariableEntityProvider implements VariableEntityProvider {

    private final String entityType;

    private RVariableEntityProvider(String entityType) {
      this.entityType = Strings.isNullOrEmpty(entityType) ? PARTICIPANT : entityType;
    }

    @Override
    public String getEntityType() {
      return entityType;
    }

    @Override
    public boolean isForEntityType(String entityType) {
      return this.entityType.equals(entityType);
    }

    @Override
    public Set<VariableEntity> getVariableEntities() {
      return null;
    }
  }
}
