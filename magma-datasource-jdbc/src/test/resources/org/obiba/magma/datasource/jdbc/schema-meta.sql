--mysql table names are case sensitive on Linux
DROP TABLE IF EXISTS "value_tables";
DROP TABLE IF EXISTS "variables";
DROP TABLE IF EXISTS "variable_attributes";
DROP TABLE IF EXISTS "categories";
DROP TABLE IF EXISTS "category_attributes";
DROP TABLE IF EXISTS BONE_DENSITY;
DROP TABLE IF EXISTS MY_TABLE;
DROP TABLE IF EXISTS "mydatasourcenodb_MY_TABLE";

create table "value_tables"("datasource" varchar(255) not null, "name" varchar(255) not null, "entity_type" varchar(255) not null, "created" timestamp, "updated" timestamp, "sql_name" varchar(255) not null, primary key("datasource", "name"));
create table "variables"("datasource" varchar(255) not null, "value_table" varchar(255) not null, "name" varchar(255) not null, "value_type" varchar(255), "ref_entity_type" varchar(255), "mime_type" varchar(255), "units" varchar(255), "is_repeatable" boolean not null, "occurrence_group" varchar(255), "index" int, "sql_name" varchar(255) not null, primary key("datasource", "value_table", "name"));
create table "variable_attributes"("datasource" varchar(255) not null, "value_table" varchar(255) not null, "variable" varchar(255) not null, "name" varchar(255) not null, "locale" varchar(20) not null, "value" varchar(255), primary key("datasource", "value_table", "variable", "name", "locale"));
create table "categories"("datasource" varchar(255) not null, "value_table" varchar(255) not null, "variable" varchar(255), "name" varchar(255) not null, "missing" boolean not null, primary key("value_table", "variable", "name"));
create table "category_attributes"("datasource" varchar(255) not null, "value_table" varchar(255) not null, "variable" varchar(255) not null, "category" varchar(255) not null, "name" varchar(255) not null, "locale" varchar(20) not null, "value" varchar(255), primary key("datasource", "value_table", "variable", "category", "name", "locale"));

create table bone_density(part_id varchar(25) not null, visit_id varchar(25) not null, bd integer, bd_2 integer, primary key(part_id));
