-- relies on relation realization

SELECT realize_relation('OBO_REL:inheres_in');

--

CREATE OR REPLACE VIEW homologous_to_symm AS
 SELECT DISTINCT
  link_id,
  reiflink_node_id,
  node_id,
  predicate_id,
  object_id
 FROM
  obo_rel.homologous_to
 UNION
 SELECT DISTINCT
  link_id,
  reiflink_node_id,
  node_id		 AS object_id,
  predicate_id,
  object_id		 AS node_id
 FROM
  obo_rel.homologous_to;

-- BEGIN MATERIALIZE
-- SELECT create_matview('homologous_to_symm');
-- CREATE INDEX homologous_to_symm_idx_pair ON homologous_to_symm(node_id,object_id);
-- CREATE INDEX homologous_to_symm_idx_trip ON homologous_to_symm(link_id,node_id,object_id);
-- END MATERIALIZE


-- BEGIN MATERIALIZE
-- SELECT create_matview('genus_differentium');
-- CREATE INDEX genus_differentium_idx_all ON genus_differentium(node_id,genus_id,object_id);
-- END MATERIALIZE

-- Example: select node_info(node_taxon_id),node_info(node_anat_id),node_info(object_taxon_id),node_info(object_anat_id) from homologous_to_exp_symm;
CREATE OR REPLACE VIEW homologous_to_exp_symm AS
 SELECT
  h.*,
  gd1.object_id AS node_taxon_id,
  gd1.genus_id AS node_anat_id,
  gd2.object_id AS object_taxon_id,
  gd2.genus_id AS object_anat_id
 FROM
  homologous_to_symm AS h,
  genus_differentium AS gd1,
  genus_differentium AS gd2
 WHERE
  h.node_id=gd1.node_id AND
  h.object_id=gd2.node_id;

CREATE OR REPLACE VIEW taxon_eq AS
 SELECT
  l.*,
  gd.genus_id,
  gd.predicate_id AS xp_predicate_id,
  gd.object_id AS xp_object_id
 FROM
  reified_link AS l
  INNER JOIN genus_differentium AS gd ON (gd.node_id=l.object_id);

CREATE OR REPLACE VIEW taxon_anat AS
 SELECT
  l.*,
  inheres_in.object_id AS anat_id
 FROM
  reified_link AS l
  INNER JOIN obo_rel.inheres_in ON (l.object_id=inheres_in.node_id);

CREATE OR REPLACE VIEW homology_anat_effect AS
 SELECT
  h.*,
  ta.node_id AS affected_taxon_id
 FROM
  taxon_anat AS ta
  INNER JOIN homologous_to_exp_symm AS h ON (h.node_anat_id=ta.anat_id);
  
CREATE OR REPLACE VIEW homology_anat_effect_intax AS
 SELECT
  *
 FROM 
  homology_anat_effect
 WHERE EXISTS (SELECT * FROM obo_rel.is_a WHERE is_a.node_id=affected_taxon_id AND is_a.object_id=node_taxon_id);

CREATE OR REPLACE VIEW homology_anat_effect_pair AS
 SELECT
  h.*,
  ta1.node_id AS node_affected_taxon_id,
  ta2.node_id AS object_affected_taxon_id
 FROM
  taxon_anat AS ta1,
  taxon_anat AS ta2,
  homologous_to_exp_symm AS h
 WHERE
  h.node_anat_id=ta1.anat_id AND 
  h.object_anat_id=ta2.anat_id;

