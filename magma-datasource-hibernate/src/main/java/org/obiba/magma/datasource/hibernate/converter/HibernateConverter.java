/*
 * Copyright (c) 2019 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.magma.datasource.hibernate.converter;

public interface HibernateConverter<T, E> {

  T marshal(E magmaObject, HibernateMarshallingContext context);

  E unmarshal(T jpaObject, HibernateMarshallingContext context);

}
