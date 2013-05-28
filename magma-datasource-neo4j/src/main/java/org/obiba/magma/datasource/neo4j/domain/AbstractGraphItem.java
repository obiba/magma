/*
 * Copyright (c) 2013 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.magma.datasource.neo4j.domain;

import org.springframework.data.neo4j.annotation.GraphId;

public abstract class AbstractGraphItem {

  @GraphId
  protected Long graphId;

  public Long getGraphId() {
    return graphId;
  }

  public void setGraphId(Long graphId) {
    this.graphId = graphId;
  }

  @Override
  public boolean equals(Object o) {
    if(this == o) return true;
    if(o == null || getClass() != o.getClass()) return false;

    AbstractGraphItem node = (AbstractGraphItem) o;
    if(graphId == null) return super.equals(o);
    return graphId.equals(node.graphId);

  }

  @Override
  public int hashCode() {
    return graphId == null ? super.hashCode() : graphId.hashCode();
  }
}
