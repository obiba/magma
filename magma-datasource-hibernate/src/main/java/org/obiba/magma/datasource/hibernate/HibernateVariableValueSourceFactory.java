/*
 * Copyright (c) 2017 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.magma.datasource.hibernate;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.hibernate.FetchMode;
import org.hibernate.Session;
import org.obiba.core.service.SortingClause;
import org.obiba.core.service.impl.hibernate.AssociationCriteria;
import org.obiba.core.service.impl.hibernate.AssociationCriteria.Operation;
import org.obiba.magma.VariableValueSource;
import org.obiba.magma.VariableValueSourceFactory;
import org.obiba.magma.datasource.hibernate.domain.VariableState;

class HibernateVariableValueSourceFactory implements VariableValueSourceFactory {

//  private static final Logger log = LoggerFactory.getLogger(HibernateVariableValueSourceFactory.class);

  private final HibernateValueTable valueTable;

  HibernateVariableValueSourceFactory(HibernateValueTable valueTable) {
    if(valueTable == null) throw new IllegalArgumentException("valueTable cannot be null");
    this.valueTable = valueTable;
  }

  @Override
  public Set<VariableValueSource> createSources() {
    Set<VariableValueSource> sources = new LinkedHashSet<>();
    @SuppressWarnings("unchecked")
    Iterable<VariableState> variables = (List<VariableState>) AssociationCriteria
        .create(VariableState.class, getCurrentSession())
        .add("valueTable", Operation.eq, valueTable.getValueTableState()) //
        .addSortingClauses(SortingClause.create("id")) //
        .getCriteria().setFetchMode("categories", FetchMode.JOIN).list();
    for(VariableState v : variables) {
      sources.add(createSource(v));
    }
    return sources;
  }

  VariableValueSource createSource(VariableState variableState) {
    return createSource(variableState, true);
  }

  VariableValueSource createSource(VariableState variableState, boolean unmarshall) {
    return new HibernateVariableValueSource(valueTable, variableState, unmarshall);
  }

  private Session getCurrentSession() {
    return valueTable.getDatasource().getSessionFactory().getCurrentSession();
  }

}
