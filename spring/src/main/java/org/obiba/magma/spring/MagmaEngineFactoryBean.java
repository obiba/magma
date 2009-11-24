/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.magma.spring;

import java.util.Set;

import org.obiba.magma.MagmaEngine;
import org.obiba.magma.MagmaEngineExtension;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;

/**
 *
 */
public class MagmaEngineFactoryBean implements FactoryBean, InitializingBean, DisposableBean {

  private Set<MagmaEngineExtension> extensions;

  public Object getObject() throws Exception {
    return MagmaEngine.get();
  }

  public Class<?> getObjectType() {
    return MagmaEngine.class;
  }

  public boolean isSingleton() {
    return true;
  }

  public void afterPropertiesSet() throws Exception {
    MagmaEngine engine = new MagmaEngine();
    for(MagmaEngineExtension extension : extensions) {
      engine.extend(extension);
    }
  }

  public void destroy() throws Exception {
    MagmaEngine.get().shutdown();
  }

  public void setExtensions(Set<MagmaEngineExtension> extensions) {
    this.extensions = extensions;
  }

}
