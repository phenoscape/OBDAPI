CREATE OR REPLACE VIEW omim_annotation_source AS
 SELECT
  *
 FROM
  node
 WHERE
  uid IN ('omim_phenotype_bbop','omim_phenotype_zfin','omim_phenotype_fb');

-- BEGIN MATERIALIZE
-- SELECT create_matview('omim_annotation_source');
-- CREATE INDEX omim_annotation_source_idx_id ON omim_annotation_source(node_id);
-- CREATE INDEX omim_annotation_source_idx_uid ON omim_annotation_source(uid);
-- CREATE INDEX omim_annotation_source_idx_id_uid ON omim_annotation_source(node_id,uid);
-- END MATERIALIZE

CREATE VIEW inf_gt_label AS
 SELECT 
  node_id AS gt_node_id,
  node_uid AS gt_uid,
  (object_label || ' variant ' || replace(node_uid,'OMIM:','')) AS gt_label
 FROM node_link_node_with_pred
 WHERE pred_uid='OBO_REL:variant_of'
  AND is_inferred='f'
  AND node_uid like 'OMIM:%'
  AND object_uid like 'NCBI_Gene:%';

-- UPDATE node SET label=(SELECT gt_label FROM inf_gt_label WHERE gt_label.gt_node_id=node_id) WHERE node.uid LIKE 'OMIM:%';

CREATE OR REPLACE VIEW omim_genotype AS
 SELECT
  *
 FROM
  node
 WHERE uid like 'OMIM:%';

-- BEGIN MATERIALIZE
-- SELECT create_matview('omim_genotype');
-- CREATE INDEX omim_genotype_idx_id ON omim_genotype(node_id);
-- CREATE INDEX omim_genotype_idx_uid ON omim_genotype(uid);
-- END MATERIALIZE

CREATE OR REPLACE VIEW omim_genotype_annotation_summary AS
 SELECT
  omim_genotype.uid, 
  omim_genotype.label, 
  aet.*
 FROM 
  annotated_entity_total_annotations_by_annotsrc  AS aet
  INNER JOIN omim_genotype ON (aet.annotated_entity_id = omim_genotype.node_id);

CREATE OR REPLACE VIEW total_bbop AS
 SELECT
  aet.*
 FROM 
  annotated_entity_total_annotations_by_annotsrc AS aet
  INNER JOIN omim_annotation_source AS src ON (aet.source_id = src.node_id)
 WHERE
  src.uid='omim_phenotype_bbop';

CREATE OR REPLACE VIEW total_zfin AS
 SELECT
  aet.*
 FROM 
  annotated_entity_total_annotations_by_annotsrc AS aet
  INNER JOIN omim_annotation_source AS src ON (aet.source_id = src.node_id)
 WHERE
  src.uid='omim_phenotype_zfin';

CREATE OR REPLACE VIEW total_fb AS
 SELECT
  aet.*
 FROM 
  annotated_entity_total_annotations_by_annotsrc AS aet
  INNER JOIN omim_annotation_source AS src ON (aet.source_id = src.node_id)
 WHERE
  src.uid='omim_phenotype_fb';


CREATE OR REPLACE VIEW omim_genotype_annotation_summary_table AS
 SELECT
  omim_genotype.node_id, 
  omim_genotype.uid, 
  gt_label AS label, 
  aet_bbop.total_annotations AS bbop_annots,
  aet_zfin.total_annotations AS zfin_annots,
  aet_fb.total_annotations AS fb_annots
 FROM 
  omim_genotype
  INNER JOIN inf_gt_label ON (omim_genotype.node_id=gt_node_id)
  LEFT OUTER JOIN total_bbop AS aet_bbop ON (aet_bbop.annotated_entity_id=omim_genotype.node_id)
  LEFT OUTER JOIN total_zfin AS aet_zfin ON (aet_zfin.annotated_entity_id=omim_genotype.node_id)
  LEFT OUTER JOIN total_fb AS aet_fb ON (aet_fb.annotated_entity_id=omim_genotype.node_id);



CREATE OR REPLACE VIEW omim_genotype_annotsrc_coverage AS
 SELECT
  omim_genotype.uid, 
  omim_genotype.label, 
  aet.*
 FROM 
  annotated_entity_total_annotation_nodes_by_annotsrc AS aet
  INNER JOIN omim_genotype ON (aet.annotated_entity_id = omim_genotype.node_id);

CREATE OR REPLACE VIEW omim_annotator_congruence_by_entity AS
 SELECT
  base_src.uid AS base_src_uid,
  target_src.uid AS target_src_uid,
  aec.* 
 FROM 
  annotated_entity_congruence_between_annotsrc_pair AS aec 
  INNER JOIN omim_annotation_source AS base_src ON (base_src.node_id=aec.base_source_id)
  INNER JOIN omim_annotation_source AS target_src ON (target_src.node_id=aec.target_source_id);



 