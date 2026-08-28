# Magma JDBC Datasource

Exposes a relational database as a Magma datasource, either by mapping existing tables or by
managing its own metadata tables.

## Supported Databases

The datasource generates its DDL through [Liquibase](https://www.liquibase.org/), so the dialect is
resolved from the JDBC connection at runtime. The following engines are covered by the test suite:

| Database  | Notes                                                    |
|-----------|----------------------------------------------------------|
| MySQL     | tables are created with `ENGINE=InnoDB`, `BLOB` columns are widened to `LONGBLOB` |
| MariaDB   | same handling as MySQL                                    |
| PostgreSQL| `OID` columns are rewritten to `BYTEA`                     |
| SQL Server|                                                           |
| H2        | 2.x, in-memory or file based                               |

Dialect-specific SQL rewriting lives in `org.obiba.magma.datasource.jdbc.support`, as Liquibase
`SqlVisitor` implementations registered in `JdbcDatasource.ChangeDatabaseCallback`. H2 and SQL Server
need none.

## JDBC Drivers

Magma does **not** ship JDBC drivers: they are declared at `test` scope here and must be provided by
the embedding application. For H2:

```xml
<dependency>
  <groupId>com.h2database</groupId>
  <artifactId>h2</artifactId>
  <version>2.4.240</version>
</dependency>
```

## Usage

```java
BasicDataSource dataSource = new BasicDataSource();
dataSource.setDriverClassName("org.h2.Driver");
// in-memory, kept alive while the application holds no connection
dataSource.setUrl("jdbc:h2:mem:mydb;DB_CLOSE_DELAY=-1");
// or file based: dataSource.setUrl("jdbc:h2:file:/path/to/mydb");
dataSource.setUsername("sa");
dataSource.setPassword("");

JdbcDatasource datasource = new JdbcDatasource("my-datasource", dataSource,
    JdbcDatasourceSettings.newSettings("Participant").build());
datasource.initialise();
```

Use H2 in its regular mode. A compatibility mode such as `MODE=MySQL` changes identifier casing and
quoting semantics and is neither needed nor tested.

## Tests

```
mvn -pl magma-datasource-jdbc test
```

`JdbcDatasourceTest` runs against an in-memory H2 database, so no external server is required. The
schema scripts it drives through `@TestSchema` live in `src/test/resources/org/obiba/magma/datasource/jdbc`
and are H2 flavoured — `schema-notables.sql` tears the database down with `DROP ALL OBJECTS`.

To run the suite against another engine, point the `dataSource` bean in `test-spring-context.xml` at
it (commented-out MySQL and PostgreSQL examples are in that file) and adjust the teardown statement,
which is engine specific.
