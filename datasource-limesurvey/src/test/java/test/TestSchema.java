package test;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

//TODO REFACTOR, also used in JDBC DATASOURCE
@Retention(RetentionPolicy.RUNTIME)
public @interface TestSchema {

  public String dataSourceBean() default "dataSource";

  public String schemaLocation() default "";

  public String beforeSchema() default "";

  public String afterSchema() default "";
}