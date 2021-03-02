/*
 * Copyright (c) 2019 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.magma.views.support;

import java.util.HashSet;
import java.util.Set;

import org.obiba.magma.Datasource;
import org.obiba.magma.DatasourceTransformer;
import org.obiba.magma.views.View;
import org.obiba.magma.views.ViewAwareDatasource;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;

public class ViewAwareDatasourceTransformer implements DatasourceTransformer {
  //
  // Instance Variables
  //

  // TODO: this is no longer used. It must not be removed because some configuration files may still refer to this
  // property.
  @Deprecated
  @SuppressWarnings("FieldCanBeLocal")
  private String name;

  private Set<View> views;

  //
  // DatasourceTransformer Methods
  //

  @Override
  public Datasource transform(Datasource datasource) {
    if(views != null) {
      return new ViewAwareDatasource(datasource, views);
    }
    return datasource;
  }

  //
  // Methods
  //
  @Deprecated
  public void setName(String name) {
    this.name = name;
  }

  public void setViews(Set<View> views) {
    this.views = new HashSet<>();
    if(views != null) {
      this.views.addAll(views);
    }
  }

  public Set<View> getViews() {
    return ImmutableSet.copyOf(Iterables.filter(views, View.class));
  }
}
