-- execute this AFTER data population
-- SELECT realize_all_relations();



CREATE OR REPLACE VIEW phenotype_of AS
 SELECT
  ph.uid,
  ph.label,
  ph.source_id AS node_source_id,
  ph.metatype,
  inh.*
 FROM
  BIRN_PDPO.phenotype AS ph
  INNER JOIN OBO_REL.inheres_in AS inh USING(node_id);


CREATE OR REPLACE VIEW inheres_in_population_with AS
 SELECT
  inh.node_id,
  po.object_id,
  popn.node_id AS population_id
 FROM 
  OBO_REL.inheres_in AS inh
  INNER JOIN sao.sao2254405550 AS popn ON (inh.object_id=popn.node_id)
  INNER JOIN OBO_REL.part_of AS po ON (inh.object_id=po.node_id);

CREATE OR REPLACE VIEW inheres_in_with_types AS
 SELECT
  ni.object_id AS node_type_id,
  oi.object_id AS object_type_id,
  inh.*
 FROM
  OBO_REL.inheres_in AS inh
  INNER JOIN asserted_OBO_REL.instance_of  AS ni ON (inh.node_id=ni.node_id)
  INNER JOIN asserted_OBO_REL.instance_of  AS oi ON (inh.object_id=oi.node_id);

CREATE OR REPLACE VIEW inheres_in_with_types_c AS
 SELECT
  ni.object_id AS node_type_id,
  oi.object_id AS object_type_id,
  count(DISTINCT inh.node_id) AS num_instances
 FROM
  OBO_REL.inheres_in AS inh
  INNER JOIN asserted_OBO_REL.instance_of  AS ni ON (inh.node_id=ni.node_id)
  INNER JOIN asserted_OBO_REL.instance_of  AS oi ON (inh.object_id=oi.node_id)
 GROUP BY
  ni.object_id,
  oi.object_id;

CREATE OR REPLACE VIEW has_part_with_types_c AS
 SELECT
  ni.object_id AS node_type_id,
  oi.object_id AS object_type_id,
  count(DISTINCT inh.node_id) AS num_instances
 FROM
  OBO_REL.has_part AS inh
  INNER JOIN asserted_OBO_REL.instance_of  AS ni ON (inh.node_id=ni.node_id)
  INNER JOIN asserted_OBO_REL.instance_of  AS oi ON (inh.object_id=oi.node_id)
 GROUP BY
  ni.object_id,
  oi.object_id;

-- note that in the above asserted instance_of is used: for a wider variety
-- consider inferred
CREATE OR REPLACE FUNCTION create_birn_xps() RETURNS SETOF RECORD AS
$$
 SELECT store_genus_differentium(node_type_id,get_node_id('OBO_REL:inheres_in'),object_type_id) FROM inheres_in_with_types_c
 UNION
 SELECT store_genus_differentium(node_type_id,get_node_id('OBO_REL:has_part'),object_type_id) FROM has_part_with_types_c
$$ LANGUAGE 'sql';

-- select reify_links_by_predicate('BIRN_PDPO:bears','BIRN:generic_annotation');

-- local closed-world axioms
CREATE SCHEMA cwaxiom;


CREATE OR REPLACE VIEW phenotype_born_by AS
 SELECT
  ph.uid,
  ph.label,
  ph.source_id AS node_source_id,
  ph.metatype,
  ibb.*
 FROM
  BIRN_PDPO.phenotype AS ph INNER JOIN BIRN_PDPO.is_born_by AS ibb ON (ph.node_id=ibb.node_id);


CREATE OR REPLACE VIEW cwaxiom.unsat_all_phenotype_born_by_something AS
 SELECT
  ph.*
 FROM
  BIRN_PDPO.phenotype AS ph
 WHERE NOT EXISTS (SELECT * FROM BIRN_PDPO.is_born_by AS ibb WHERE ibb.node_id=ph.node_id);

SELECT realize_class('birnlex_ubo:birnlex_2');

CREATE OR REPLACE VIEW cwaxiom.unsat_all_phenotype_born_by_some_organism AS
 SELECT
  ph.*
 FROM
  BIRN_PDPO.phenotype AS ph
 WHERE NOT EXISTS (SELECT * FROM BIRN_PDPO.is_born_by AS ibb WHERE ibb.node_id=ph.node_id AND birnlex_ubo.birnlex_2(ibb.object_id));
