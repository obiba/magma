/*
 * Copyright (c) 2013 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.magma.datasource.neo4j.converter;

import javax.annotation.Nonnull;

public interface Neo4jConverter<TNode, TMagmaObject> {

  TNode marshal(@Nonnull TMagmaObject magmaObject, @Nonnull Neo4jMarshallingContext context);

  TMagmaObject unmarshal(@Nonnull TNode node, @Nonnull Neo4jMarshallingContext context);

}
