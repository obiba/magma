/*
 * Copyright (c) 2011 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.magma.datasource.hibernate.domain;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

@Entity
@Table(name = "datasource", uniqueConstraints = { @UniqueConstraint(columnNames = "name") })
public class DatasourceState extends AbstractAttributeAwareEntity implements Timestamped {

  private static final long serialVersionUID = 1L;

  @Column(nullable = false)
  private String name;

  public DatasourceState() {
  }

  public DatasourceState(String name) {
    this.name = name;
  }

  public String getName() {
    return name;
  }

}
