SELECT enforce_link_metadata();

-- replicated from core-views
CREATE OR REPLACE VIEW instance_of_link AS
 SELECT link.*
 FROM link INNER JOIN instance_of_relation ON (predicate_id=instance_of_relation.node_id)
 WHERE combinator='';

-- BEGIN MATERIALIZE
-- SELECT create_matview('instance_of_link');
-- CREATE INDEX instance_of_link_idx_node ON instance_of_link(node_id);
-- CREATE INDEX instance_of_link_idx_object ON instance_of_link(object_id);
-- CREATE INDEX instance_of_link_idx_node_object ON instance_of_link(node_id,object_id);
-- END MATERIALIZE

-- instance <xRy> instantiating <XRY>
CREATE OR REPLACE VIEW instance_link_class_pair AS
 SELECT
  io.node_id,
  link.predicate_id,
  link.object_id,
  io.object_id AS node_class_id,
  yio.object_id AS object_class_id
 FROM
  instance_of_link AS io
  INNER JOIN link ON (io.node_id=link.node_id)
  INNER JOIN instance_of_link AS yio ON (link.object_id=yio.node_id)
 WHERE
  link.is_metadata='f';


-- BEGIN MATERIALIZE
-- SELECT create_matview('instance_link_class_pair');
-- CREATE INDEX instance_link_class_pair_ix1 ON instance_link_class_pair(node_id);
-- CREATE INDEX instance_link_class_pair_ix2 ON instance_link_class_pair(predicate_id);
-- CREATE INDEX instance_link_class_pair_ix3 ON instance_link_class_pair(object_id);
-- CREATE INDEX instance_link_class_pair_ix6 ON instance_link_class_pair(node_class_id,object_class_id);
-- CREATE INDEX instance_link_class_pair_ix7 ON instance_link_class_pair(node_class_id,predicate_id,object_class_id);
-- END MATERIALIZE

-- instance <xRy> instantiating <XRY>, non-redundant
-- we exclude classes such as (size of (spinal_cord)) of (spinal_cord)
CREATE OR REPLACE VIEW instance_link_class_pair_nr AS
 SELECT *
 FROM instance_link_class_pair AS il
  WHERE
   NOT EXISTS (
    SELECT * FROM link AS rl WHERE il.node_class_id=rl.node_id AND il.object_class_id=rl.object_id
   )
   AND
   NOT EXISTS (
    SELECT * FROM link AS rl WHERE il.object_class_id=rl.node_id AND il.node_class_id=rl.object_id
   );


-- BEGIN MATERIALIZE
-- SELECT create_matview('instance_link_class_pair_nr');
-- CREATE INDEX instance_link_class_pair_nr_ix1 ON instance_link_class_pair_nr(node_id);
-- CREATE INDEX instance_link_class_pair_nr_ix2 ON instance_link_class_pair_nr(predicate_id);
-- CREATE INDEX instance_link_class_pair_nr_ix3 ON instance_link_class_pair_nr(object_id);
-- CREATE INDEX instance_link_class_pair_nr_ix6 ON instance_link_class_pair_nr(node_class_id,object_class_id);
-- CREATE INDEX instance_link_class_pair_nr_ix7 ON instance_link_class_pair_nr(node_class_id,predicate_id,object_class_id);
-- END MATERIALIZE


-- size ~1m

-- <x1 r y1>,<x2 r y2>,<X,Y>
-- such that X(x1),X(x2),Y(y1),Y(y2)
-- Large join.
-- It is recommended you constrain this with at least a predicate
CREATE OR REPLACE VIEW instance_common_ancestor_tuple AS
 SELECT
  il1.node_id,
  il1.object_id,
  il2.node_id AS node2_id,
  il2.object_id AS object2_id,
  il1.node_class_id,
  il1.predicate_id,
  il1.object_class_id
 FROM
  instance_link_class_pair_nr AS il1
  INNER JOIN instance_link_class_pair_nr AS il2 USING(node_class_id,predicate_id,object_class_id);


-- <x1 r y1>,<x2 r y2>,<X,Y>
-- such that X(x1),X(x2),Y(y1),Y(y2)
-- and there is no more specific <X,Y> that satisfies this
CREATE OR REPLACE VIEW instance_LCA_tuple AS
 SELECT
  *
 FROM
  instance_common_ancestor_tuple AS ica
 WHERE
  NOT EXISTS (
    SELECT *
    FROM
      instance_common_ancestor_tuple AS nrica,
      link AS rl,
      link AS rl2
    WHERE ica.node_id = nrica.node_id AND   -- same instance
      ica.object_id = nrica.object_id AND
      ica.node2_id = nrica.node2_id AND   -- same instance
      ica.object2_id = nrica.object2_id AND
      nrica.node_class_id = rl.node_id AND    -- X redundant with X'
      ica.node_class_id = rl.object_id AND -- Y redundant with Y'
      nrica.object_class_id = rl2.node_id AND
      ica.object_class_id = rl2.object_id AND
      (ica.node_class_id != nrica.node_class_id OR ica.object_class_id != nrica.object_class_id)); -- not equal

-- DELETE FROM node WHERE node_id IN (SELECT node_id FROM intersection_link WHERE object_id=get_node_id('NIF_Investigation:birnlex_2087'));



CREATE OR REPLACE VIEW self_referential_class_expr AS
 SELECT
  dl.node_id
 FROM
  intersection_link AS dl,
   intersection_link AS xdl,
   link AS rl
 WHERE
  dl.node_id = xdl.node_id AND
  xdl.object_id = rl.node_id AND
  rl.object_id=dl.object_id AND
  dl.link_id != xdl.link_id;
  

-- equivalencies throw NR check
-- delete from node as n where uid like '_:%' and not exists (select * from link where link.object_id=n.node_id);
-- delete from node as n where uid like '_:%' and not exists (select * from asserted_link as link where link.object_id=n.node_id);

-- after creation, below
delete from node as n where uid like '%^%' and node_id in (select node_id from sameas);
delete from node as n where uid like '_:%' and node_id in (select node_id from sameas);


-- NEW

CREATE OR REPLACE VIEW is_organism AS
 SELECT DISTINCT
  instance_of_link.node_id
 FROM
  instance_of_link 
  INNER JOIN node AS c ON (instance_of_link.object_id=c.node_id)
 WHERE
  c.uid='birnlex_ubo:birnlex_2';

CREATE OR REPLACE VIEW is_disease_class AS
 SELECT DISTINCT node_id
 FROM
  is_a_link WHERE object_id=get_node_id('NIF_OBI:birnlex_11013');

CREATE OR REPLACE VIEW is_a_human AS
 SELECT DISTINCT node_id
 FROM
   asserted_is_a_link WHERE object_id=get_node_id('birnlex_tax:birnlex_516');

CREATE OR REPLACE VIEW is_bearer_of_relation_node AS 
 SELECT *
 FROM node WHERE uid='birnlex_ubo:birnlex_17';

CREATE OR REPLACE VIEW is_bearer_of AS SELECT * FROM link WHERE predicate_id IN (SELECT node_id FROM is_bearer_of_relation_node);

