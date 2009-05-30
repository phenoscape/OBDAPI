SELECT realize_relation('OBO_REL:is_a');
SELECT realize_relation('OBO_REL:towards');
SELECT realize_relation('reciprocal_of');
SELECT realize_relation('OBO_REL:results_in_complete_development_of');
SELECT realize_relation('OBO_REL:inheres_in');
SELECT realize_relation('increased_in_magnitude_relative_to');
SELECT realize_relation('decreased_in_magnitude_relative_to');
SELECT realize_class('PATO:0000070');

CREATE OR REPLACE VIEW develops_into AS
 SELECT
  object_id AS node_id,
  get_node_id('develops_into') AS predicate_id,
  node_id AS object_id
 FROM
  asserted__.develops_from;

CREATE OR REPLACE VIEW is_a_inheres_in_progenitor_of AS
 SELECT DISTINCT
  inh.node_id AS node_id,
  get_node_id('OBO_REL:is_a'),
  (SELECT store_genus_differentium(is_a.object_id,
                                   get_node_id('inheres_in_progenitor_of'),
                                   develops_into.object_id,
                                  CAST(NULL AS INT))) AS object_id
 FROM
 asserted_obo_rel.inheres_in AS inh
 INNER JOIN develops_into ON (inh.object_id=develops_into.node_id)
 INNER JOIN asserted_obo_rel.is_a ON (inh.node_id=is_a.node_id);

CREATE OR REPLACE VIEW inheres_in_progenitor_of AS
 SELECT DISTINCT
  inh.node_id,
  get_node_id('inheres_in_progenitor_of') AS predicate_id,
  develops_into.object_id
 FROM
  asserted_obo_rel.inheres_in AS inh
 INNER JOIN develops_into ON (inh.object_id=develops_into.node_id);


CREATE OR REPLACE VIEW inheres_in_derivative_of AS
 SELECT
  inheres_in.node_id,
  get_node_id('inheres_in_derivative_of') AS predicate_id,
  develops_from.object_id
 FROM
  obo_rel.inheres_in
  INNER JOIN _.develops_from ON (inheres_in.object_id=develops_from.node_id);

CREATE OR REPLACE VIEW is_a_inheres_in_part_of AS
 SELECT DISTINCT
  inh.node_id AS node_id,
  get_node_id('OBO_REL:is_a'),
  (SELECT store_genus_differentium(is_a.object_id,
                                   get_node_id('OBO_REL:inheres_in_part_of'),
                                   part_of.object_id,
                                  CAST(NULL AS INT))) AS object_id
 FROM
 asserted_obo_rel.inheres_in AS inh
 INNER JOIN part_of ON (inh.object_id=part_of.node_id)
 INNER JOIN asserted_obo_rel.is_a ON (inh.node_id=is_a.node_id)
WHERE
 part_of.object_id IN (SELECT * FROM node WHERE uid LIKE 'UBERON:%');



CREATE OR REPLACE VIEW quality_bearer_df_anatomy AS
SELECT DISTINCT
 node_label(genus_link.object_id) AS q,
 node_label(inh.object_id) AS p,
 node_label(rd.object_id) AS a
FROM
 asserted_obo_rel.inheres_in AS inh
 INNER JOIN asserted_obo_rel.results_in_complete_development_of AS rd ON (inh.object_id=rd.node_id)
 INNER JOIN genus_link ON (inh.node_id=genus_link.node_id);

CREATE OR REPLACE VIEW quality_bearer_anatomy AS
SELECT DISTINCT
 node_label(genus_link.object_id) AS q,
 node_label(inh.object_id) AS p,
 node_label(rd.object_id) AS a
FROM
 asserted_obo_rel.inheres_in AS inh
 INNER JOIN differentium_link AS rd ON (inh.object_id=rd.node_id)
 INNER JOIN genus_link ON (inh.node_id=genus_link.node_id);

CREATE OR REPLACE VIEW quality_bearer_process_anatomy AS
SELECT DISTINCT
 node_label(genus_link.object_id) AS q,
 node_uid(inh.object_id) AS pid,
 node_label(inh.object_id) AS p,
 node_label(rd.object_id) AS a
FROM
 asserted_obo_rel.inheres_in AS inh
 INNER JOIN differentium_link AS rd ON (inh.object_id=rd.node_id)
 INNER JOIN genus_link ON (inh.node_id=genus_link.node_id)
WHERE
 node_uid(inh.object_id) LIKE 'GO:%';

CREATE VIEW link_inferred_from_bp_xp_anat AS
 SELECT
  CAST('t' AS BOOLEAN) AS is_inferred,
  inh.node_id AS node_id,
  (SELECT node_id FROM is_a_relation) AS predicate_id,
  (SELECT store_genus_differentium((select node_id FROM node WHERE uid='PATO:0000001'),
                                  inh.predicate_id,
                                  rd.object_id,
                                  CAST(NULL AS INT))) AS object_id
 FROM
 asserted_obo_rel.inheres_in AS inh
 INNER JOIN differentium_link AS rd ON (inh.object_id=rd.node_id)
WHERE
 node_uid(inh.object_id) LIKE 'GO:%';

COMMENT ON VIEW link_inferred_from_bp_xp_anat IS 'Creates a link to
grouping phenotype for process phenotypes, where a link in
bp_xp_uberon exists. is_a(Phen,quality that inh(Anat)) <-
inh(Phen,Proc),diff(Proc,_,Anat). Realize this link by passing the
argument view=link_inferred_from_bp_xp_anat to obd-reasoner';

-- INSERT INTO link (is_inferred,node_id,predicate_id,object_id) SELECT * FROM link_inferred_from_bp_xp_anat;


CREATE OR REPLACE VIEW absent_eq AS
 SELECT DISTINCT
  inheres_in.node_id,
  inheres_in.object_id AS inheres_in_id,
  is_a_link.object_id AS is_a_id
 FROM
  obo_rel.inheres_in
  INNER JOIN is_a_link ON (is_a_link.node_id=inheres_in.node_id)
 WHERE
  is_a_link.is_inferred='f' AND
  is_a_link.object_id IN (SELECT node_id FROM node WHERE uid='PATO:0000462');

CREATE OR REPLACE VIEW reciprocal_is_a_absent_eq_link AS
 SELECT DISTINCT
  absent_eq.node_id AS node_id,
  get_node_id('OBO_REL:is_a') AS predicate_id,
  store_genus_differentium(get_node_id('PATO:0001999'),
                           get_node_id('OBO_REL:towards'),
                           absent_eq.inheres_in_id,
                           NULL) AS object_id
 FROM
  absent_eq;
COMMENT ON VIEW reciprocal_is_a_absent_eq_link IS 'is_a(Phen, lacks_parts that towards E) <- is_a(Phen,absent),inh(Phen,E)';

-- obd-reasoner.pl --skip rules  --skip intersections --verbose -d 'dbi:Pg:dbname=obdp;host=spade.lbl.gov' --view reciprocal_is_a_absent_eq_link

CREATE OR REPLACE VIEW towards_xp_uberon AS
 SELECT 
  genus_link.object_id AS quality_id,
  towards.object_id AS towards_id,
  isa.object_id     AS uberon_id
 FROM
   asserted_obo_rel.towards 
   INNER JOIN genus_link ON (towards.node_id=genus_link.node_id)
   INNER JOIN asserted_is_a_link AS isa ON (towards.object_id=isa.node_id)
 WHERE 
   isa.node_id NOT IN (SELECT node_id FROM node_with_source WHERE source_uid='uberon') AND
   isa.object_id IN (SELECT node_id FROM node_with_source WHERE source_uid='uberon') AND
   towards.combinator='I';

COMMENT ON VIEW towards_xp_uberon IS 'Q that towards E2 : for uberon only';


SELECT
  store_genus_differentium(quality_id,
                           get_node_id('OBO_REL:towards'),
                           uberon_id,
                           NULL)
  FROM
   towards_xp_uberon;


CREATE OR REPLACE VIEW inh_p_anat AS
SELECT * 
FROM
 link AS inh
 INNER JOIN link AS rd ON (inh.object_id=rd.node_id)
WHERE
 node_uid(inh.predicate_id) = 'OBO_REL:inheres_in'
 AND node_uid(rd.predicate_id) = 'OBO_REL:results_in_development_of';

CREATE OR REPLACE VIEW phenotype_incmag AS
 SELECT
  genus_link.*
 FROM
  genus_link INNER JOIN asserted__.increased_in_magnitude_relative_to AS im ON genus_link.object_id=im.node_id;

-- BEGIN MATERIALIZE
-- SELECT create_matview('phenotype_incmag');
-- CREATE INDEX phenotype_incmag_ix1 ON phenotype_incmag(node_id);
-- CREATE INDEX phenotype_incmag_ix2 ON phenotype_incmag(object_id);
-- CREATE INDEX phenotype_incmag_ix3 ON phenotype_incmag(node_id,object_id);
-- END MATERIALIZE

CREATE OR REPLACE VIEW phenotype_decmag AS
 SELECT
  genus_link.*
 FROM
  genus_link INNER JOIN asserted__.decreased_in_magnitude_relative_to AS im ON genus_link.object_id=im.node_id;

-- BEGIN MATERIALIZE
-- SELECT create_matview('phenotype_decmag');
-- CREATE INDEX phenotype_decmag_ix1 ON phenotype_decmag(node_id);
-- CREATE INDEX phenotype_decmag_ix2 ON phenotype_decmag(object_id);
-- CREATE INDEX phenotype_decmag_ix3 ON phenotype_decmag(node_id,object_id);
-- END MATERIALIZE

CREATE OR REPLACE VIEW phenotype_incmag_p AS
 SELECT
  pm.*,
  isa.object_id AS determinable_id
 FROM
  phenotype_incmag AS pm
  INNER JOIN asserted_is_a_link AS isa ON (pm.object_id=isa.node_id);

CREATE OR REPLACE VIEW phenotype_decmag_p AS
 SELECT
  pm.*,
  isa.object_id AS determinable_id
 FROM
  phenotype_decmag AS pm
  INNER JOIN asserted_is_a_link AS isa ON (pm.object_id=isa.node_id);



CREATE OR REPLACE VIEW pheno_magpair AS
 SELECT
  im.node_id AS pheno_incmag_id,
  dm.node_id AS pheno_decmag_id,
  im.determinable_id AS quality_id,
  ime.object_id AS entity_id
 FROM
  phenotype_incmag_p AS im
  INNER JOIN phenotype_decmag_p AS dm ON (im.determinable_id=dm.determinable_id)
  INNER JOIN differentium_link AS ime ON (ime.node_id=im.node_id)
  INNER JOIN differentium_link AS dme ON (dme.node_id=dm.node_id)
 WHERE
  ime.object_id=dme.object_id;

CREATE OR REPLACE VIEW invmag_annotpair AS
 SELECT
  mp.*,
  a1.node_id AS a1_id,
  a2.node_id AS a2_id
 FROM
  pheno_magpair AS mp
  INNER JOIN reified_link AS a1 ON (a1.object_id=mp.pheno_incmag_id)
  INNER JOIN reified_link AS a2 ON (a2.object_id=mp.pheno_incmag_id);

-- BEGIN MATERIALIZE
-- SELECT create_matview('invmag_annotpair');
-- CREATE INDEX phenotype_decmag_ix1 ON phenotype_decmag(node_id);
-- CREATE INDEX phenotype_decmag_ix2 ON phenotype_decmag(object_id);
-- CREATE INDEX phenotype_decmag_ix3 ON phenotype_decmag(node_id,object_id);
-- END MATERIALIZE
