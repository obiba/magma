/*
 * Copyright (c) 2013 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.magma.datasource.neo4j.support;

import org.springframework.data.neo4j.support.Neo4jTemplate;

/**
 * Strategy for obtaining the {@code Neo4jTemplate} instance.
 */
public interface Neo4jTemplateProvider {

  Neo4jTemplate getNeo4jTemplate();

}
