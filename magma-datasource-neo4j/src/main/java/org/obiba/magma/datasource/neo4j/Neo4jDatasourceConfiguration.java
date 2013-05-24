/*
 * Copyright (c) 2013 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.magma.datasource.neo4j;

import org.neo4j.kernel.EmbeddedGraphDatabase;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.auditing.IsNewAwareAuditingHandler;
import org.springframework.data.neo4j.config.EnableNeo4jRepositories;
import org.springframework.data.neo4j.config.Neo4jConfiguration;
import org.springframework.data.neo4j.lifecycle.AuditingEventListener;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Configuration
@EnableTransactionManagement
@EnableNeo4jRepositories("org.obiba.magma.datasource.neo4j.repository")
public class Neo4jDatasourceConfiguration extends Neo4jConfiguration {

  @Bean(destroyMethod = "shutdown")
  public EmbeddedGraphDatabase graphDatabaseService() {
    //TODO extract graph storeDir
    return new EmbeddedGraphDatabase("target/data/neo4j.db");
  }

  @Bean
  public AuditingEventListener auditingEventListener() throws Exception {
    return new AuditingEventListener(new IsNewAwareAuditingHandler<Object>(isNewStrategyFactory()));
  }
}
