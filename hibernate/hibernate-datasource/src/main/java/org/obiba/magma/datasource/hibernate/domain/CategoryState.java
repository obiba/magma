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

@Entity
@Table(name = "category")
public class CategoryState extends AbstractAttributeAwareEntity implements Timestamped {

  private static final long serialVersionUID = 1L;

  @Column(nullable = false)
  private String name;

  private String code;

  @Column(nullable = false)
  private boolean missing;

  public CategoryState() {

  }

  public CategoryState(String name, String code, boolean missing) {
    this.name = name;
    this.code = code;
    this.missing = missing;
  }

  public String getCode() {
    return code;
  }

  public void setCode(String code) {
    this.code = code;
  }

  public void setMissing(Boolean missing) {
    this.missing = missing;
  }

  public String getName() {
    return name;
  }

  public boolean isMissing() {
    return missing;
  }

}
