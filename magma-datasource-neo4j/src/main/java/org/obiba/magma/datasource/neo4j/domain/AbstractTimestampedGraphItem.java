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

import java.util.Date;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

public abstract class AbstractTimestampedGraphItem extends AbstractGraphItem implements Timestamped {

  @CreatedDate
  private long createdDate;

  @LastModifiedDate
  private long lastModifiedDate;

  public long getCreatedDate() {
    return createdDate;
  }

  public void setCreatedDate(long createdDate) {
    this.createdDate = createdDate;
  }

  public long getLastModifiedDate() {
    return lastModifiedDate;
  }

  public void setLastModifiedDate(long lastModifiedDate) {
    this.lastModifiedDate = lastModifiedDate;
  }

  @Override
  public Date getCreated() {
    return new Date(createdDate);
  }

  @Override
  public Date getUpdated() {
    return new Date(lastModifiedDate);
  }

}
