-- BEGIN MATERIALIZE
-- SELECT create_matview('reified_link');
-- CREATE INDEX reified_link_idx_node ON transitive_relation_node(node_id);
-- CREATE INDEX transitive_relation_node_idx_node_uid ON transitive_relation_node(node_id,uid);
-- END MATERIALIZE

CREATE OR REPLACE VIEW asserted_ancestor_link_proper AS
 SELECT
  inf_link.node_id AS query_node_id,
  inf_link.predicate_id AS query_predicate_id,
  asserted_link.*
 FROM
  link AS inf_link
  INNER JOIN link AS asserted_link ON (inf_link.object_id=asserted_link.node_id)
 WHERE
  asserted_link.is_inferred='f';

COMMENT ON VIEW asserted_ancestor_link_proper IS 'A more efficient
version of asserted_ancestor_link, but excludes the reflexive
case. Can be used if all reflexive links are precomputed';

CREATE OR REPLACE VIEW asserted_ancestor_link AS
 SELECT
  *
 FROM
  asserted_ancestor_link_proper
 UNION
 SELECT
  node_id AS query_node_id,
  predicate_id AS query_predicate_id,
  asserted_link.*
 FROM
  link AS asserted_link
 WHERE
  asserted_link.is_inferred='f';

COMMENT ON VIEW asserted_ancestor_link IS 'An asserted link
that can be reached via closure from query_node_id';


CREATE OR REPLACE VIEW old___instance_least_common_ancestor_tuple_nr AS
 SELECT DISTINCT
  class_id,
  predicate_id,
  class2_id
 FROM instance_least_common_ancestor_tuple AS xp
  WHERE NOT EXISTS (
   SELECT * FROM link WHERE node_id=class_id AND xp.predicate_id=link.predicate_id 
  )
  OR NOT EXISTS (
   SELECT * FROM link INNER JOIN inverse_of ON (link.predicate_id=inverse_of.predicate_id)
    WHERE node_id=class2_id AND xp.predicate_id=inverse_of.inverse_predicate_id 
  );
   
CREATE OR REPLACE VIEW old2__instance_least_common_ancestor_tuple_nr AS
 SELECT DISTINCT
  class_id,
  predicate_id,
  class2_id
 FROM instance_least_common_ancestor_tuple AS xp
  WHERE NOT EXISTS (
   SELECT * FROM link WHERE link.node_id=class_id AND link.object_id =class2_id
  )
  OR NOT EXISTS (
   SELECT * FROM link WHERE link.node_id=class_id AND link.object_id =class2_id
  );
   



-- for any instance-level relationship <x r y> we
-- find the set of classes X and Y that x and y instantiates
CREATE OR REPLACE VIEW instance_common_ancestor_tuple AS
 SELECT
  io.node_id AS instance_id,
  io.object_id AS class_id,
  link.predicate_id,
  xio.node_id AS instance2_id,
  xio.object_id AS class2_id
 FROM
  instance_of_link AS io
  INNER JOIN link ON (io.node_id=link.node_id)
  INNER JOIN instance_of_link AS xio ON (link.object_id=xio.node_id)
 WHERE
  link.is_metadata='f';
--- size ~440k rows



-- common ancestors minus classes like (agg^inh(alsyn=)) x inh alsyn'
CREATE OR REPLACE VIEW instance_common_ancestor_tuple_nr AS
 SELECT DISTINCT
  *
 FROM instance_common_ancestor_tuple AS xp
  WHERE NOT EXISTS (
   SELECT * FROM link WHERE link.node_id=class_id AND link.object_id=class2_id
  )
  OR NOT EXISTS (
   SELECT * FROM link WHERE link.node_id=class2_id AND link.object_id=class_id
  );



-- for any instance-level relationship <x r y> we
-- find the set of classes X and Y that x and y instantiates
-- such that there is no more specific pair <X' Y'> that is subsumed by <X Y>
CREATE OR REPLACE VIEW instance_least_common_ancestor_tuple AS
 SELECT
  nrica.*
 FROM 
  instance_common_ancestor_tuple AS nrica
 WHERE NOT EXISTS (
  SELECT * 
  FROM instance_common_ancestor_tuple AS ica,
       link AS rl,
       link AS rl2
  WHERE ica.instance_id = nrica.instance_id AND
        ica.instance2_id = nrica.instance2_id AND
        ica.class_id = rl.node_id AND
        nrica.class_id = rl.object_id AND
        ica.class2_id = rl2.node_id AND
        nrica.class2_id = rl2.object_id AND
        (ica.class_id != nrica.class_id OR ica.class2_id != nrica.class2_id));

-- for any instance-level relationship <x r y> we
-- find the set of classes X and Y that x and y instantiates
-- such that there is no more specific pair <X' Y'> that is subsumed by <X Y>
-- AND exclude <<X Y> X> style expressions
CREATE OR REPLACE VIEW instance_least_common_ancestor_tuple_nr AS
 SELECT
  nrica.*
 FROM 
  instance_common_ancestor_tuple_nr AS nrica
 WHERE NOT EXISTS (
  SELECT * 
  FROM instance_common_ancestor_tuple_nr AS ica,
       link AS rl,
       link AS rl2
  WHERE ica.instance_id = nrica.instance_id AND
        ica.instance2_id = nrica.instance2_id AND
        ica.class_id = rl.node_id AND
        nrica.class_id = rl.object_id AND
        ica.class2_id = rl2.node_id AND
        nrica.class2_id = rl2.object_id AND
        (ica.class_id != nrica.class_id OR ica.class2_id != nrica.class2_id));

-- BEGIN MATERIALIZE
-- SELECT create_matview('instance_least_common_ancestor_tuple_nr');
-- CREATE INDEX instance_least_common_ancestor_tuple_nr_ix1 ON instance_least_common_ancestor_tuple_nr(class_id,predicate_id,class2_id);
-- END MATERIALIZE

-- the follow extracts the relevant <x r y> classes

CREATE OR REPLACE VIEW instance_least_common_ancestor_tuple_classes AS
 SELECT DISTINCT
  class_id,
  predicate_id,
  class2_id
 FROM instance_least_common_ancestor_tuple;

CREATE OR REPLACE VIEW instance_least_common_ancestor_tuple_classes_nr AS
 SELECT DISTINCT
  class_id,
  predicate_id,
  class2_id
 FROM instance_least_common_ancestor_tuple_nr;

-- remove cases of foo^inheres_in(e&has_quality(foo'))
-- where foo' and foo stand in some relationship;
--
-- better to catch this earlier...?
CREATE OR REPLACE VIEW xp_class_nr AS 
 SELECT DISTINCT
  *
 FROM 
  instance_least_common_ancestor_tuple_classes AS xp
 WHERE
  NOT EXISTS (
   SELECT * FROM link 
     WHERE (xp.class_id=link.node_id AND xp.class2_id=link.object_id) OR
           (xp.class_id=link.object_id AND xp.class2_id=link.node_id)
  );

-- SELECT store_genus_differentium(class_id,predicate_id,class2_id, get_node_id('BIRN:generic_annotation')) FROM instance_least_common_ancestor_tuple_classes_nr;
