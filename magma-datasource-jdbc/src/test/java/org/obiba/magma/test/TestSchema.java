/**
 *
 */
package org.obiba.magma.test;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface TestSchema {

  String dataSourceBean() default "dataSource";

  String schemaLocation() default "";

  String beforeSchema() default "";

  String afterSchema() default "";
}