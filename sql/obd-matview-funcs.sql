-- snapshot MVs only supported. See
-- http://www.jonathangardner.net/tech/w/PostgreSQL/Materialized_Views

CREATE OR REPLACE VIEW pgdb_size AS
 SELECT pg_database.datname,pg_database_size(pg_database.datname), pg_size_pretty(pg_database_size(pg_database.datname)) AS size FROM pg_database ORDER BY pg_database_size(pg_database.datname);

--CREATE SCHEMA mvutil;
--SET search_path TO public, mvutil;

CREATE TABLE matviews (
  mv_name NAME NOT NULL PRIMARY KEY,
  v_name NAME NOT NULL,
  v_sql TEXT,
  last_refresh TIMESTAMP WITH TIME ZONE
);

-- @@ create_matview(tableName,viewName)
CREATE OR REPLACE FUNCTION create_matview(NAME, NAME)
RETURNS VOID
SECURITY DEFINER
LANGUAGE plpgsql AS $$
DECLARE
    matview ALIAS FOR $1;
    view_name ALIAS FOR $2;
    view_sql TEXT;
    entry matviews%ROWTYPE;
BEGIN
    SELECT * INTO entry FROM matviews WHERE mv_name = matview;

    IF FOUND THEN
        RAISE EXCEPTION 'Materialized view ''%'' already exists.',
          matview;
    END IF;

    SELECT definition INTO view_sql FROM pg_views WHERE viewname=view_name;

    EXECUTE 'REVOKE ALL ON ' || view_name || ' FROM PUBLIC'; 

    EXECUTE 'GRANT SELECT ON ' || view_name || ' TO PUBLIC';

    EXECUTE 'CREATE TABLE ' || matview || ' AS SELECT * FROM ' || view_name;

    EXECUTE 'REVOKE ALL ON ' || matview || ' FROM PUBLIC';

    EXECUTE 'GRANT SELECT ON ' || matview || ' TO PUBLIC';

    INSERT INTO matviews (mv_name, v_name, v_sql, last_refresh)
      VALUES (matview, view_name, view_sql, CURRENT_TIMESTAMP); 
    
    RETURN;
END
$$;

CREATE OR REPLACE FUNCTION create_matview(NAME)
RETURNS VOID
SECURITY DEFINER
LANGUAGE plpgsql AS $$
DECLARE
    view_name ALIAS FOR $1;
    view_sql TEXT;
    entry matviews%ROWTYPE;
BEGIN
    RAISE NOTICE 'materializing %', view_name ;
    SELECT * INTO entry FROM matviews WHERE mv_name = view_name;

    IF FOUND THEN
        RAISE EXCEPTION 'Materialized view ''%'' already exists.',
          matview;
    END IF;

    SELECT definition INTO view_sql FROM pg_views WHERE viewname=view_name;

    EXECUTE 'REVOKE ALL ON ' || view_name || ' FROM PUBLIC'; 

    EXECUTE 'GRANT SELECT ON ' || view_name || ' TO PUBLIC';

    EXECUTE 'DROP VIEW ' || view_name || ' CASCADE'; -- up to user to recreate dependents

    RAISE NOTICE 'creating table %', view_name ;
    EXECUTE 'CREATE TABLE ' || view_name || ' AS ' || view_sql;
    RAISE NOTICE 'created table %', view_name ;

    EXECUTE 'REVOKE ALL ON ' || view_name || ' FROM PUBLIC';

    EXECUTE 'GRANT SELECT ON ' || view_name || ' TO PUBLIC';

    INSERT INTO matviews (mv_name, v_name, v_sql, last_refresh)
      VALUES (view_name, view_name, view_sql, CURRENT_TIMESTAMP); 
    
    RETURN;
END
$$;

-- todo: recreate view? or do en masse
CREATE OR REPLACE FUNCTION drop_matview(NAME,BOOLEAN) RETURNS VOID
SECURITY DEFINER
LANGUAGE plpgsql AS $$
DECLARE
    matview ALIAS FOR $1;
    casc    ALIAS FOR $2;
    entry matviews%ROWTYPE;
BEGIN

    SELECT * INTO entry FROM matviews WHERE mv_name = matview;

    IF NOT FOUND THEN
        RAISE EXCEPTION 'Materialized view % does not exist.', matview;
    END IF;

    IF casc THEN    
      RAISE NOTICE 'dropping %', matview;
      EXECUTE 'DROP TABLE ' || matview || ' CASCADE';
      RAISE NOTICE 'dropped %', matview;
    ELSE
      EXECUTE 'DROP TABLE ' || matview;
    END IF;
    DELETE FROM matviews WHERE mv_name=matview;

    RETURN;
END
$$;

CREATE OR REPLACE FUNCTION drop_matview(NAME) RETURNS VOID
LANGUAGE sql AS $$
  SELECT drop_matview($1,'f')
$$;

CREATE OR REPLACE FUNCTION drop_all_matviews() RETURNS VOID
SECURITY DEFINER
LANGUAGE plpgsql AS $$
DECLARE
    n VARCHAR;
BEGIN

    FOR n IN SELECT mv_name FROM matviews LOOP
      PERFORM drop_matview(n,'t');
    END LOOP;

    RETURN;
END
$$;

CREATE OR REPLACE FUNCTION refresh_matview(name) RETURNS VOID
SECURITY DEFINER
LANGUAGE plpgsql AS $$
DECLARE 
    matview ALIAS FOR $1;
    entry matviews%ROWTYPE;
BEGIN

    SELECT * INTO entry FROM matviews WHERE mv_name = matview;

    IF NOT FOUND THEN
        RAISE EXCEPTION 'Materialized view % does not exist.', matview;
    END IF;

    EXECUTE 'DELETE FROM ' || matview;
    EXECUTE 'INSERT INTO ' || matview
        || ' SELECT * FROM ' || entry.v_name;

    UPDATE matviews
        SET last_refresh=CURRENT_TIMESTAMP
        WHERE mv_name=matview;

    RETURN;
END
$$;

-- place all MVs in separate schema
CREATE SCHEMA mv;
SET search_path TO mv, public;
