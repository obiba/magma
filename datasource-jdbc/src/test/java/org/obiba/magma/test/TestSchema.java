/**
 * 
 */
package org.obiba.magma.test;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface TestSchema {

  public String dataSourceBean() default "dataSource";

  public String schemaLocation() default "";

  public String beforeSchema() default "";

  public String afterSchema() default "";
}