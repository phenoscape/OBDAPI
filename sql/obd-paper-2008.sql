
--
CREATE OR REPLACE VIEW gene_node AS
 SELECT 
  *
 FROM
  node
 WHERE node_id IN (SELECT is_a_link.node_id FROM is_a_link INNER JOIN node AS gene_type ON (is_a_link.object_id=gene_type.node_id) WHERE gene_type.uid='SO:0000704' AND is_inferred='f');

-- BEGIN MATERIALIZE
-- SELECT create_matview('gene_node');
-- CREATE UNIQUE INDEX gene_node_idx_id ON gene_node(node_id);
-- CREATE UNIQUE INDEX gene_node_idx_uid ON gene_node(uid);
-- CREATE INDEX gene_node_idx_label ON gene_node(label);
-- END MATERIALIZE

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
  *,
  split_part(uid,'/',1) AS allele
 FROM
  node
 WHERE uid like 'OMIM:%';

-- BEGIN MATERIALIZE
-- SELECT create_matview('omim_genotype');
-- CREATE INDEX omim_genotype_idx_id ON omim_genotype(node_id);
-- CREATE INDEX omim_genotype_idx_uid ON omim_genotype(uid);
-- CREATE INDEX omim_genotype_idx_label ON omim_genotype(label);
-- END MATERIALIZE

CREATE OR REPLACE VIEW omim_allele AS
 SELECT
  omim_genotype.node_id,
  allele,
  concat(gt_label || ' ') AS gt_labels
 FROM
  omim_genotype
  INNER JOIN inf_gt_label ON (node_id=gt_node_id)
 GROUP BY omim_genotype.node_id,allele;



CREATE OR REPLACE VIEW omim_genotype_gene AS
 SELECT
  og.*,
  l2n.object_id AS gene_id,
  l2n.object_uid AS gene_uid,
  l2n.object_label AS gene_label
 FROM
  omim_genotype AS og
  INNER JOIN link_to_node AS l2n USING (node_id)
 WHERE
  l2n.predicate_id IN (SELECT node_id FROM node WHERE uid='OBO_REL:variant_of')
  AND l2n.object_uid like 'NCBI_Gene:%';

CREATE OR REPLACE VIEW omim_gene AS
 SELECT DISTINCT
  l2n.object_id AS node_id,
  l2n.object_uid AS uid,
  l2n.object_label AS label
 FROM
  omim_genotype AS og
  INNER JOIN link_to_node AS l2n USING (node_id)
 WHERE
  l2n.predicate_id IN (SELECT node_id FROM node WHERE uid='OBO_REL:variant_of')
  AND l2n.object_uid like 'NCBI_Gene:%';

-- BEGIN MATERIALIZE
-- SELECT create_matview('omim_gene');
-- CREATE INDEX omim_gene_idx_id ON omim_gene(node_id);
-- CREATE INDEX omim_gene_idx_uid ON omim_gene(uid);
-- CREATE INDEX omim_gene_idx_label ON omim_gene(label);
-- END MATERIALIZE

CREATE OR REPLACE VIEW omim_genotype_annotation_summary AS
 SELECT
  omim_genotype.uid, 
  omim_genotype.label, 
  aet.*
 FROM 
  annotated_entity_total_annotations_by_annotsrc  AS aet
  INNER JOIN omim_genotype ON (aet.annotated_entity_id = omim_genotype.node_id);

CREATE OR REPLACE VIEW omim_gene_annotation_summary AS
 SELECT DISTINCT
  ogg.gene_uid, 
  ogg.gene_label, 
  aet.*
 FROM 
  annotated_entity_total_annotations_by_annotsrc  AS aet
  INNER JOIN omim_genotype_gene AS ogg ON (aet.annotated_entity_id = ogg.gene_id);

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

CREATE OR REPLACE VIEW omim_genotype_annotation_summary_by_allele_table AS
 SELECT
  omim_allele.allele, 
  gt_labels,
  SUM(aet_bbop.total_annotations) AS bbop_annots,
  SUM(aet_zfin.total_annotations) AS zfin_annots,
  SUM(aet_fb.total_annotations) AS fb_annots
 FROM 
  omim_allele
  LEFT OUTER JOIN total_bbop AS aet_bbop ON (aet_bbop.annotated_entity_id=omim_allele.node_id)
  LEFT OUTER JOIN total_zfin AS aet_zfin ON (aet_zfin.annotated_entity_id=omim_allele.node_id)
  LEFT OUTER JOIN total_fb AS aet_fb ON (aet_fb.annotated_entity_id=omim_allele.node_id)
 GROUP BY
  omim_allele.allele, 
  gt_labels;
  
CREATE OR REPLACE VIEW omim_genotype_annotsrc_coverage AS
 SELECT
  omim_genotype.uid, 
  omim_genotype.label, 
  aet.*
 FROM 
  annotated_entity_total_annotation_nodes_by_annotsrc AS aet
  INNER JOIN omim_genotype ON (aet.annotated_entity_id = omim_genotype.node_id);

CREATE OR REPLACE VIEW omim_gene_annotsrc_coverage AS
 SELECT DISTINCT
  ogg.uid, 
  ogg.label, 
  aet.*
 FROM 
  annotated_entity_total_annotation_nodes_by_annotsrc AS aet
  INNER JOIN omim_gene AS ogg ON (aet.annotated_entity_id = ogg.node_id);

CREATE OR REPLACE VIEW omim_annotator_pairwise_congruence_by_entity AS
 SELECT
  base_src.uid AS base_src_uid,
  target_src.uid AS target_src_uid,
  aec.* 
 FROM 
  annotated_entity_congruence_between_annotsrc_pair AS aec 
  INNER JOIN omim_annotation_source AS base_src ON (base_src.node_id=aec.base_source_id)
  INNER JOIN omim_annotation_source AS target_src ON (target_src.node_id=aec.target_source_id);

CREATE OR REPLACE VIEW omim_annotator_pairwise_congruence_by_gene AS
 SELECT DISTINCT
  ogg.uid, 
  ogg.label, 
  base_src.uid AS base_src_uid,
  target_src.uid AS target_src_uid,
  aec.* 
 FROM 
  annotated_entity_congruence_between_annotsrc_pair AS aec
  INNER JOIN omim_gene AS ogg ON (aec.annotated_entity_id = ogg.node_id)
  INNER JOIN omim_annotation_source AS base_src ON (base_src.node_id=aec.base_source_id)
  INNER JOIN omim_annotation_source AS target_src ON (target_src.node_id=aec.target_source_id);

 CREATE OR REPLACE VIEW omim_annotator_global_congruence_by_gene AS
 SELECT DISTINCT
  ogg.uid, 
  ogg.label, 
  src.uid AS src_uid,
  aec.* 
 FROM 
  annotated_entity_congruence_by_annotsrc AS aec
  INNER JOIN omim_gene AS ogg ON (aec.annotated_entity_id = ogg.node_id)
  INNER JOIN omim_annotation_source AS src ON (src.node_id=aec.source_id);

CREATE OR REPLACE VIEW avg_information_content AS 
 SELECT avg(shannon_information) AS avg_information_content
 FROM class_node_entropy_by_evidence;

CREATE OR REPLACE VIEW avg_information_content_by_annotsrc AS 
 SELECT 
  aic.source_id,
  avg(shannon_information) AS avg_information_content
 FROM 
  annotation_with_information_content AS aic
 GROUP BY
  aic.source_id;

CREATE OR REPLACE VIEW avg_information_content_by_annotated_entity AS 
 SELECT 
  aic.node_id AS annotated_entity_id,
  avg(shannon_information) AS avg_information_content
 FROM 
  annotation_with_information_content AS aic
 GROUP BY
  aic.node_id;

CREATE OR REPLACE VIEW unique_annotation_with_information_content AS
 SELECT DISTINCT
  node_id,
  object_id,
  annotated_entity_count,
  shannon_information
 FROM
  annotation_with_information_content;

CREATE OR REPLACE VIEW avg_unique_information_content_by_annotated_entity AS 
 SELECT 
  aic.node_id AS annotated_entity_id,
  avg(shannon_information) AS avg_information_content
 FROM 
  unique_annotation_with_information_content AS aic
 GROUP BY
  aic.node_id;

CREATE OR REPLACE VIEW avg_unique_information_content_by_annotsrc_and_annotated_entity AS 
 SELECT 
  aic.source_id,
  aic.node_id AS annotated_entity_id,
  avg(shannon_information) AS avg_information_content
 FROM 
  unique_annotation_with_information_content AS aic
 GROUP BY
  aic.source_id,
  aic.node_id;

