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

import org.springframework.data.neo4j.annotation.NodeEntity;
import org.springframework.data.neo4j.annotation.RelatedTo;

import static org.neo4j.graphdb.Direction.OUTGOING;

@NodeEntity
public abstract class AbstractValueAwareNode extends AbstractTimestampedGraphItem {

  @RelatedTo(type = "HAS_VALUE", direction = OUTGOING)
  private ValueNode value;

  public ValueNode getValue() {
    return value;
  }

  public void setValue(ValueNode value) {
    this.value = value;
  }
}
