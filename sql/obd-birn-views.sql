-- NEW

CREATE OR REPLACE VIEW is_organism AS
 SELECT DISTINCT
  instance_of_link.node_id
 FROM
  instance_of_link 
  INNER JOIN node AS c ON (instance_of_link.object_id=c.node_id)
 WHERE
  c.uid='birnlex_ubo:birnlex_2';

-- e.g. (human with PD) is_bearer_of (P)
UPDATE link SET reiflink_node_id=get_node_id('BIRN:generic_annotation') WHERE predicate_id=get_node_id('birnlex_ubo:birnlex_17') AND is_inferred='f' AND node_id IN (SELECT node_id FROM asserted_is_a_link WHERE object_id=get_node_id('birnlex_tax:birnlex_516')) AND object_id NOT IN (SELECT node_id FROM is_a_link WHERE object_id=get_node_id('NIF_OBI:birnlex_11013'));

CREATE OR REPLACE VIEW is_bearer_of AS SELECT * FROM link WHERE predicate_id IN (SELECT node_id FROM node WHERE uid='birnlex_ubo:birnlex_17');
SELECT realize_relation('OBO_REL:has_part');

CREATE OR REPLACE VIEW has_part_bearer_of AS
 SELECT DISTINCT
  has_part.node_id,
  get_node_id('OBO_REL:has_part_bearer_of') AS predicate_id,
  is_bearer_of.object_id
 FROM
  OBO_REL.has_part
  INNER JOIN is_bearer_of ON (has_part.object_id=is_bearer_of.node_id)
 WHERE
  is_bearer_of.is_inferred='f';

CREATE OR REPLACE VIEW reflexive_has_part_bearer_of AS
 SELECT * FROM has_part_bearer_of UNION
 SELECT DISTINCT
  is_bearer_of.node_id,
  get_node_id('OBO_REL:has_part_bearer_of') AS predicate_id,
  is_bearer_of.object_id
 FROM
   is_bearer_of
 WHERE
  is_bearer_of.is_inferred='f';

SELECT realize_relation('OBO_REL:part_of');

CREATE OR REPLACE VIEW inheres_in_part_of AS
 SELECT DISTINCT
  inheres_in.node_id,
  get_node_id('OBO_REL:inheres_in_part_of') AS predicate_id,
  part_of.object_id
 FROM
  OBO_REL.inheres_in
  INNER JOIN obo_rel.part_of ON (inheres_in.object_id=part_of.node_id)
 WHERE
  inheres_in.is_inferred='f';

SELECT realize_relation('OBO_REL:inheres_in');
SELECT realize_relation('OBO_REL:instance_of');

CREATE OR REPLACE VIEW inferred_inheres_in_with_types_c AS
 SELECT
  ni.object_id AS node_type_id,
  oi.object_id AS object_type_id,
  count(DISTINCT inh.node_id) AS num_instances
 FROM
  OBO_REL.inheres_in AS inh
  INNER JOIN OBO_REL.instance_of  AS ni ON (inh.node_id=ni.node_id)
  INNER JOIN OBO_REL.instance_of  AS oi ON (inh.object_id=oi.node_id)
 GROUP BY
  ni.object_id,
  oi.object_id;

CREATE OR REPLACE VIEW asserted_inheres_in_with_types_c AS
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

CREATE OR REPLACE VIEW nr_inheres_in_with_types_c AS
 SELECT
  ni.object_id AS node_type_id,
  oi.object_id AS object_type_id,
  count(DISTINCT inh.node_id) AS num_instances
 FROM
  OBO_REL.inheres_in AS inh
  INNER JOIN asserted_instance_of_link  AS ni ON (inh.node_id=ni.node_id)
  INNER JOIN nr_instance_of_link  AS oi ON (inh.object_id=oi.node_id)
 GROUP BY
  ni.object_id,
  oi.object_id;


SELECT realize_relation('OBO_REL:towards');

CREATE OR REPLACE VIEW asserted_towards_with_types_c AS
 SELECT
  ni.object_id AS node_type_id,
  oi.object_id AS object_type_id,
  count(DISTINCT towards.node_id) AS num_instances
 FROM
  OBO_REL.towards AS towards
  INNER JOIN asserted_OBO_REL.instance_of  AS ni ON (towards.node_id=ni.node_id)
  INNER JOIN asserted_OBO_REL.instance_of  AS oi ON (towards.object_id=oi.node_id)
 GROUP BY
  ni.object_id,
  oi.object_id;

CREATE OR REPLACE VIEW asserted_part_of_types_c AS
 SELECT
  ni.object_id AS node_type_id,
  oi.object_id AS object_type_id,
  count(DISTINCT part_of.node_id) AS num_instances
 FROM
  OBO_REL.part_of AS part_of
  INNER JOIN asserted_OBO_REL.instance_of  AS ni ON (part_of.node_id=ni.node_id)
  INNER JOIN asserted_OBO_REL.instance_of  AS oi ON (part_of.object_id=oi.node_id)
 WHERE
  part_of.object_id NOT IN (SELECT node_id FROM is_organism)
 GROUP BY
  ni.object_id,
  oi.object_id;


-- conservative
SELECT 
   store_genus_differentium(node_type_id,get_node_id('OBO_REL:part_of'),object_type_id,get_node_id('BIRN:generic_annotation'))
 FROM asserted_part_of_types_c;
-- should reason after this..

SELECT 
   store_genus_differentium(node_type_id,get_node_id('OBO_REL:inheres_in'),object_type_id,get_node_id('BIRN:generic_annotation'))
 FROM asserted_inheres_in_with_types_c;

SELECT 
   store_genus_differentium(node_type_id,get_node_id('OBO_REL:inheres_in'),object_type_id,get_node_id('BIRN:generic_annotation'))
 FROM nr_inheres_in_with_types_c;

SELECT 
   store_genus_differentium(node_type_id,get_node_id('OBO_REL:towards'),object_type_id,get_node_id('BIRN:generic_annotation'))
 FROM asserted_towards_with_types_c;

SELECT 
   store_genus_differentium(node_id,get_node_id('OBO_REL:inheres_in_part_of'),object_id,get_node_id('BIRN:generic_annotation'))
 FROM inheres_in_part_of;

SELECT realize_relation('OBO_REL:inheres_in_part_of');

-- obd-reasoner.pl --skip intersections --view inheres_in_link_from_has_quality --view has_population_of --rule none -d $* ;\


CREATE OR REPLACE VIEW exemplifies AS
 SELECT DISTINCT
  hpbo.node_id,
  get_node_id('OBO_REL:exemplifies') AS predicate_id,
  io.object_id  
 FROM
  reflexive_has_part_bearer_of AS hpbo
  INNER JOIN nr_instance_of_link AS io ON (hpbo.object_id=io.node_id)
 WHERE
  hpbo.node_id IN (SELECT node_id FROM is_organism);


-- delete from link where predicate_id=get_node_id('OBO_REL:exemplifies');
-- INSERT INTO LINK (reiflink_node_id,node_id,predicate_id,object_id) (SELECT get_node_id('BIRN:generic_annotation'), node_id, predicate_id, object_id FROM has_part_bearer_of);
-- SELECT reify_links_by_predicate('OBO_REL:exemplifies','BIRN:generic_annotation');
INSERT INTO LINK (reiflink_node_id,node_id,predicate_id,object_id) (SELECT get_node_id('BIRN:generic_annotation'), node_id, predicate_id, object_id FROM exemplifies);


CREATE OR REPLACE VIEW instance_class_sim AS
 SELECT 
  ss.*,
  inode.uid AS i_uid,
  inode.label AS i_label,
  cnode.uid AS c_uid,
  cnode.label AS c_label
 FROM 
  node_pair_annotation_similarity_score AS ss
  INNER JOIN node AS inode ON (ss.node1_id=inode.node_id)
  INNER JOIN node AS cnode ON (ss.node2_id=cnode.node_id)
 WHERE
  inode.metatype='I' AND
  cnode.metatype='C';
-- SELECT * FROM instance_class_sim ORDER BY basic_score DESC;

CREATE OR REPLACE VIEW instance_class_xp_sim AS
 SELECT 
  ss.*,
  inode.uid AS i_uid,
  inode.label AS i_label,
  cnode.uid AS c_uid,
  cnode.label AS c_label
 FROM 
  node_pair_annotation_xp_intersection_with_total AS ss
  INNER JOIN node AS inode ON (ss.node1_id=inode.node_id)
  INNER JOIN node AS cnode ON (ss.node2_id=cnode.node_id)
 WHERE
  inode.metatype='I' AND
  cnode.metatype='C';



CREATE OR REPLACE VIEW model_disease_sim AS
 SELECT 
  ss.*,
  mnode.uid AS i_uid,
  mnode.label AS i_label,
  dnode.uid AS c_uid,
  dnode.label AS c_label
 FROM 
  node_pair_annotation_similarity_score AS ss
  INNER JOIN node AS mnode ON (ss.node1_id=mnode.node_id)
  INNER JOIN node AS dnode ON (ss.node2_id=dnode.node_id)
 WHERE
  mnode.node_id IN (SELECT node_id FROM is_organism) AND
  dnode.node_id IN (SELECT node_id FROM foo);

-- UPDATE node SET label=node_auto_label(node_id) WHERE label IS NULL AND node_id IN (SELECT node_id FROM genus_link);


-- everything after here may apply to old data version


-- execute this AFTER data population
-- SELECT realize_all_relations();
SELECT realize_class('BIRN_PDPO:Phenotype');
SELECT realize_relation('BIRN_PDPO:has_quality');
SELECT realize_relation('OBO_REL:exemplifies');
SELECT realize_relation('OBO_REL:instance_of');
SELECT realize_relation('BIRN_PDPO:bears');
SELECT realize_relation('BIRN_PDPO:has_diagnosis');
SELECT realize_class('sao:sao2254405550');

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

CREATE OR REPLACE VIEW is_population AS
 SELECT DISTINCT
  instance_of_link.node_id
 FROM
  instance_of_link 
  INNER JOIN node AS c ON (instance_of_link.object_id=c.node_id)
 WHERE
  c.label='Population'; -- use SAO ID instead?

CREATE OR REPLACE VIEW is_quality AS
 SELECT DISTINCT
  instance_of_link.node_id
 FROM
  instance_of_link 
  INNER JOIN node AS c ON (instance_of_link.object_id=c.node_id)
 WHERE
  c.uid='snap:Quality' OR c.uid='BIRN_PDPO:Phenotype';



CREATE OR REPLACE VIEW is_human AS
 SELECT DISTINCT
  instance_of_link.node_id
 FROM
  instance_of_link 
  INNER JOIN node AS c ON (instance_of_link.object_id=c.node_id)
 WHERE
  c.uid='birnlex_tax:birnlex_516';

CREATE OR REPLACE VIEW is_animal_model AS
 SELECT DISTINCT
  instance_of_link.node_id
 FROM
  instance_of_link 
  INNER JOIN node AS c ON (instance_of_link.object_id=c.node_id)
 WHERE
  c.uid='BIRN_PDPO:Class_30';



CREATE OR REPLACE VIEW has_population_of AS
 SELECT DISTINCT
  pow.object_id AS node_id,
  get_node_id('OBO_REL:has_population_of') AS predicate_id,
  pop.node_id AS object_id
 FROM
  link AS pop
  INNER JOIN link AS pow ON (pop.object_id=pow.node_id)
  INNER JOIN node AS pred ON (pop.predicate_id=pred.node_id AND pow.predicate_id=pred.node_id)
  INNER JOIN is_population ON (pop.object_id=is_population.node_id)
 WHERE
  pred.uid='OBO_REL:part_of';

-- <org> bears <ph> inh <a> hp <pop> hp <towards>
-- <q> inh <pop>
--    <=>
-- <ph> instance_of (<Q'> inh <A> tow <TOWARDS>)
-- TODO
-- select distinct node_label(org_id),node_label(ph_id),node_label(anat_id),node_label(pop_id),node_label(towards_id),node_label(pato_id) from org_bears_ph_inh_whole_pop_of_cc_quality;
CREATE OR REPLACE VIEW org_bears_ph_inh_whole_pop_of_cc_quality AS
 SELECT DISTINCT
  bears.node_id AS org_id,
  bears.object_id AS ph_id,
  inheres_in1.object_id AS anat_id,
  has_part1.object_id AS pop_id,
  has_part2.object_id AS towards_id,
  inheres_in2.node_id AS pato_id
 FROM
  birn_pdpo.bears AS bears
  INNER JOIN obo_rel.inheres_in AS inheres_in1 ON (bears.object_id=inheres_in1.node_id)
  INNER JOIN obo_rel.has_part AS has_part1 ON (inheres_in1.object_id=has_part1.node_id)
  INNER JOIN obo_rel.has_part AS has_part2 ON (has_part1.object_id=has_part2.node_id)
  INNER JOIN obo_rel.inheres_in AS inheres_in2 ON (has_part2.node_id=inheres_in2.object_id)
  INNER JOIN is_population ON (has_part1.object_id=is_population.node_id);
  
 
CREATE OR REPLACE VIEW org_bears_ph_inh_whole_pop_of_cc_quality_invq AS
 SELECT DISTINCT
  org_bears_ph_inh_whole_pop_of_cc_quality.*,
  recipq.object_id AS invpato_id
 FROM
   org_bears_ph_inh_whole_pop_of_cc_quality
   INNER JOIN asserted_instance_of_link AS inst ON (pato_id=inst.node_id)
   INNER JOIN link AS recipq ON (inst.object_id=recipq.node_id)
   INNER JOIN node AS pred ON (recipq.predicate_id=pred.node_id)
 WHERE
  pred.label='reciprocal_of'
  AND recipq.is_inferred='f';

CREATE OR REPLACE VIEW eq_instance_of_invq_link AS
 SELECT DISTINCT
  ph_id AS node_id,
  get_node_id('OBO_REL:instance_of') AS predicate_id,
  store_genus_differentium(store_genus_differentium(invpato_id,
                                                get_node_id('OBO_REL:towards'),
                                                ti.object_id,
                                                NULL),
                           get_node_id('OBO_REL:inheres_in'),
                           ai.object_id,
                           NULL) AS object_id
 FROM
  org_bears_ph_inh_whole_pop_of_cc_quality_invq AS x
  INNER JOIN asserted_instance_of_link AS ai ON (x.anat_id=ai.node_id)
  INNER JOIN asserted_instance_of_link AS ti ON (x.towards_id=ti.node_id);

-- <org> bears <ph> inh <a> hp <pop> hp <cc>
-- <q> inh <cc>
-- select distinct node_label(org_id),node_label(ph_id),node_label(anat_id),node_label(pop_id),node_label(towards_id),node_label(pato_id) from org_bears_ph_inh_whole_pop_of_cc_quality;
CREATE OR REPLACE VIEW org_bears_ph_inh_whole_pop_of_cc_quality2 AS
 SELECT DISTINCT
  bears.node_id AS org_id,
  bears.object_id AS ph_id,
  inheres_in1.object_id AS anat_id,
  has_part1.object_id AS pop_id,
  has_part2.object_id AS towards_id,
  inheres_in2.node_id AS pato_id
 FROM
  birn_pdpo.bears AS bears
  INNER JOIN obo_rel.inheres_in AS inheres_in1 ON (bears.object_id=inheres_in1.node_id)
  INNER JOIN obo_rel.has_part AS has_part1 ON (inheres_in1.object_id=has_part1.node_id)
  INNER JOIN obo_rel.has_part AS has_part2 ON (has_part1.object_id=has_part2.node_id)
  INNER JOIN obo_rel.inheres_in AS inheres_in2 ON (has_part2.object_id=inheres_in2.object_id)
  INNER JOIN is_population ON (has_part1.object_id=is_population.node_id);
   
-- <org> bears <ph> inh <pop> hp <cc>
-- <q> inh <pop>
-- select distinct node_label(org_id),node_label(ph_id),node_label(pop_id),node_label(subanat_id),node_label(pato_id) from org_bears_ph_inh_pop_of_cc_quality;
CREATE OR REPLACE VIEW org_bears_ph_inh_pop_of_cc_quality AS
 SELECT DISTINCT
  bears.node_id AS org_id,
  bears.object_id AS ph_id,
  inheres_in1.object_id AS pop_id,
  has_part.object_id AS subanat_id,
  inheres_in2.node_id AS pato_id
 FROM
  birn_pdpo.bears AS bears
  INNER JOIN obo_rel.inheres_in AS inheres_in1 ON (bears.object_id=inheres_in1.node_id)
  INNER JOIN obo_rel.has_part AS has_part ON (inheres_in1.object_id=has_part.node_id)
  INNER JOIN obo_rel.inheres_in AS inheres_in2 ON (has_part.node_id=inheres_in2.object_id)
  INNER JOIN instance_of_link ON (inheres_in2.node_id=instance_of_link.node_id)
 WHERE
  has_part.is_inferred='f'
  AND instance_of_link.object_id=get_node_id('PATO:0000001');

CREATE OR REPLACE VIEW eq_instance_of_link AS
 SELECT DISTINCT
  ph_id AS node_id,
  get_node_id('OBO_REL:instance_of') AS predicate_id,
  inst.object_id AS object_id
 FROM
  org_bears_ph_inh_pop_of_cc_quality as x
  INNER JOIN instance_of_link AS inst ON (x.pato_id=inst.node_id)
  INNER JOIN node AS eq_type ON (inst.object_id=eq_type.node_id)
 WHERE
  eq_type.uid like 'PATO:%^%';

--- <org> bears <ph> inh1 <anat> hp <cc> -inh2 <q>
CREATE OR REPLACE VIEW eq_instance_of_link_basic AS
 SELECT DISTINCT
  bears.object_id AS node_id,
  get_node_id('OBO_REL:instance_of') AS predicate_id,
  inst.object_id AS object_id
 FROM
  birn_pdpo.bears AS bears
  INNER JOIN obo_rel.inheres_in AS inheres_in1 ON (bears.object_id=inheres_in1.node_id)
  INNER JOIN obo_rel.has_part ON (inheres_in1.object_id=has_part.node_id)
  INNER JOIN obo_rel.inheres_in AS inheres_in2 ON (has_part.object_id=inheres_in2.object_id)
  INNER JOIN instance_of_link AS inst ON (inheres_in2.node_id=inst.node_id);

--- <org> bears <ph> inh1 <cc> -inh2 <q>
CREATE OR REPLACE VIEW eq_instance_of_link_more_basic AS
 SELECT DISTINCT
  bears.object_id AS node_id,
  get_node_id('OBO_REL:instance_of') AS predicate_id,
  inst.object_id AS object_id
 FROM
  birn_pdpo.bears AS bears
  INNER JOIN obo_rel.inheres_in  AS inheres_in1 ON (bears.object_id=inheres_in1.node_id)
  INNER JOIN obo_rel.inheres_in AS inheres_in2 ON (inheres_in1.object_id=inheres_in2.object_id)
  INNER JOIN instance_of_link AS inst ON (inheres_in2.node_id=inst.node_id);

-- TODO: NR
-- we want to add this as asserted
CREATE OR REPLACE VIEW inferred_exemplifies AS
 SELECT
  bears.node_id,
  get_node_id('OBO_REL:exemplifies') AS predicate_id,
  inst.object_id
 FROM
  asserted_birn_pdpo.bears
  INNER JOIN instance_of_link AS inst ON (bears.object_id=inst.node_id)
 WHERE
  bears.node_id IN (SELECT node_id FROM is_organism)
  AND
  bears.object_id IN (SELECT node_id FROM is_quality);

CREATE OR REPLACE VIEW nr_inferred_exemplifies AS
 SELECT
  bears.node_id,
  get_node_id('OBO_REL:exemplifies') AS predicate_id,
  inst.object_id
 FROM
  asserted_birn_pdpo.bears
  INNER JOIN instance_of_link AS inst ON (bears.object_id=inst.node_id)
 WHERE
  bears.node_id IN (SELECT node_id FROM is_organism)
  AND
  bears.object_id IN (SELECT node_id FROM is_quality)
  AND
  NOT EXISTS (SELECT * FROM instance_of_link AS instnr WHERE instnr.node_id=inst.node_id AND instnr.object_id IN (SELECT node_id FROM link WHERE object_id=inst.object_id AND link.node_id!=link.object_id));

-- NOTE: do this as an asserted link, for annotation
-- INSERT INTO link (node_id,predicate_id,object_id) SELECT node_id,predicate_id,object_id FROM nr_inferred_exemplifies;


-- changed to instance level only
CREATE OR REPLACE VIEW inheres_in_link_from_has_quality AS
 SELECT DISTINCT
  object_id AS node_id,
  get_node_id('OBO_REL:inheres_in') AS predicate_id,
  node_id AS object_id
 FROM
  birn_pdpo.has_quality
 WHERE
  has_quality.is_inferred='f'
  AND node_id IN (SELECT node_id FROM instance_node)
  AND object_id IN (SELECT node_id FROM instance_node);

 

CREATE OR REPLACE VIEW inheres_in_population_with AS
 SELECT DISTINCT
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

CREATE OR REPLACE VIEW inferred_inheres_in_with_types_c AS
 SELECT
  ni.object_id AS node_type_id,
  oi.object_id AS object_type_id,
  count(DISTINCT inh.node_id) AS num_instances
 FROM
  OBO_REL.inheres_in AS inh
  INNER JOIN OBO_REL.instance_of  AS ni ON (inh.node_id=ni.node_id)
  INNER JOIN OBO_REL.instance_of  AS oi ON (inh.object_id=oi.node_id)
 GROUP BY
  ni.object_id,
  oi.object_id;

CREATE OR REPLACE VIEW inferredR_inheres_in_with_types_c AS
 SELECT
  ni.object_id AS node_type_id,
  oi.object_id AS object_type_id,
  count(DISTINCT inh.node_id) AS num_instances
 FROM
  OBO_REL.inheres_in AS inh
  INNER JOIN asserted_OBO_REL.instance_of  AS ni ON (inh.node_id=ni.node_id)
  INNER JOIN OBO_REL.instance_of  AS oi ON (inh.object_id=oi.node_id)
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

CREATE OR REPLACE VIEW inferred_has_part_with_types_c AS
 SELECT
  ni.object_id AS node_type_id,
  oi.object_id AS object_type_id,
  count(DISTINCT inh.node_id) AS num_instances
 FROM
  OBO_REL.has_part AS inh
  INNER JOIN OBO_REL.instance_of  AS ni ON (inh.node_id=ni.node_id)
  INNER JOIN OBO_REL.instance_of  AS oi ON (inh.object_id=oi.node_id)
 GROUP BY
  ni.object_id,
  oi.object_id;

-- 
CREATE OR REPLACE VIEW inferredR_has_part_with_types_c AS
 SELECT
  ni.object_id AS node_type_id,
  oi.object_id AS object_type_id,
  count(DISTINCT inh.node_id) AS num_instances
 FROM
  OBO_REL.has_part AS inh
  INNER JOIN asserted_OBO_REL.instance_of  AS ni ON (inh.node_id=ni.node_id)
  INNER JOIN OBO_REL.instance_of  AS oi ON (inh.object_id=oi.node_id)
 GROUP BY
  ni.object_id,
  oi.object_id;

-- note that in the above asserted instance_of is used: for a wider variety
-- consider excluding certain ontologies - e.g. upper ontologies
-- don't make a recursive genus
CREATE OR REPLACE FUNCTION create_birn_xps(INT) RETURNS SETOF RECORD AS
$$
 SELECT store_genus_differentium(node_type_id,get_node_id('OBO_REL:inheres_in'),object_type_id,$1) FROM inferred_inheres_in_with_types_c WHERE node_type_id NOT IN (SELECT node_id FROM intersection_link)
 UNION
 SELECT store_genus_differentium(node_type_id,get_node_id('OBO_REL:has_part'),object_type_id,$1) FROM inferred_has_part_with_types_c WHERE node_type_id NOT IN (SELECT node_id FROM intersection_link)
$$ LANGUAGE 'sql';



-- store_genus_differentium(node_type_id,get_node_id('OBO_REL:inheres_in'),object_type_id,$1) FROM inferred_inheres_in_with_types_c WHERE node_type_id NOT IN (SELECT node_id FROM intersection_link)

-- DO THIS ONCE:
-- load generic annotation first
-- SELECT reify_links_by_predicate('BIRN_PDPO:bears','BIRN:generic_annotation');
-- THIS ONE BETTER:
-- SELECT reify_links_by_predicate('OBO_REL:exemplifies','BIRN:generic_annotation');

-- experimental:
-- SELECT reify_links_by_predicate('BIRN_PDPO:bears','BIRN:generic_annotation');
-- this is better:
-- UPDATE link SET reiflink_node_id=get_node_id('BIRN:generic_annotation') WHERE predicate_id=get_node_id('BIRN_PDPO:has_quality') AND is_inferred='f' AND node_id IN (SELECT node_id FROM is_a_link WHERE object_id=get_node_id('http://purl.org/nbirn/birnlex/ontology/BIRNLex-OBI-proxy.owl#birnlex_11013'));
-- Yikes! inverse links at class level. e.g PD has alpha-syn agg inh PD
-- DELETE FROM link WHERE predicate_id=get_node_id('OBO_REL:inheres_in') AND is_inferred='t' AND object_id IN (SELECT node_id FROM is_a_link WHERE object_id=get_node_id('http://purl.org/nbirn/birnlex/ontology/BIRNLex-OBI-proxy.owl#birnlex_11013'));

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

--- disease to phenotype correlations

CREATE OR REPLACE VIEW has_model AS
  SELECT DISTINCT
   bears.node_id,
   has_diagnosis.object_id
  FROM
   birn_pdpo.bears
   INNER JOIN birn_pdpo.has_diagnosis ON (bears.object_id=has_diagnosis.node_id)
  WHERE
   has_diagnosis.object_id IN (SELECT node_id FROM is_animal_model);
--   bears.node_id IN (SELECT node_id FROM is_animal_model);
--   bears.node_id NOT IN (SELECT node_id FROM is_human);

CREATE OR REPLACE VIEW has_model_type AS
  SELECT DISTINCT
   bears.node_id,
   inst.object_id
  FROM
   birn_pdpo.bears
   INNER JOIN birn_pdpo.has_diagnosis ON (bears.object_id=has_diagnosis.node_id)
   INNER JOIN asserted_instance_of_link AS inst ON (has_diagnosis.object_id=inst.node_id)
  WHERE
   bears.node_id NOT IN (SELECT node_id FROM is_human);

CREATE OR REPLACE VIEW has_disease_type AS
  SELECT DISTINCT
   bears.node_id,
   inst.object_id
  FROM
   birn_pdpo.bears
   INNER JOIN birn_pdpo.has_diagnosis ON (bears.object_id=has_diagnosis.node_id)
   INNER JOIN asserted_instance_of_link AS inst ON (has_diagnosis.object_id=inst.node_id)
  WHERE
   bears.node_id IN (SELECT node_id FROM is_human);

CREATE OR REPLACE VIEW animal_model_to_disease_correlation AS
 SELECT DISTINCT
  max_ic,
  has_model.object_id AS model_id,
  has_disease_type.object_id AS disease_id
 FROM 
  node_pair_annotation_match_max_entropy AS npme
  INNER JOIN has_model ON (npme.node1_id=has_model.node_id)
  INNER JOIN has_disease_type ON (npme.node2_id=has_disease_type.node_id);

-- todo
CREATE OR REPLACE VIEW animal_model_to_disease_correlation_with_best_match AS
 SELECT
  max_ic,
  get_best_match(model_id,disease_id) AS match_id,
  node_label(model_id) AS model,
  node_label(disease_id) AS disease
 FROM
  animal_model_to_disease_correlation
 WHERE max_ic > 0 
 ORDER BY disease,max_ic DESC, model;


-- select link_info(link_id) from link where node_id in (SELECT node_id from asserted_is_a_link where object_id=get_node_id('http://purl.org/nbirn/birnlex/ontology/BIRNLex-Investigation.owl#birnlex_2087')) and combinator='' and is_inferred='f';

-- we need to promote disease phenotypes to class expressions
--- UPDATE link set combinator='I' where node_id in (SELECT node_id from asserted_is_a_link where object_id=get_node_id('http://purl.org/nbirn/birnlex/ontology/BIRNLex-Investigation.owl#birnlex_2087')) and combinator='' and is_inferred='f';

