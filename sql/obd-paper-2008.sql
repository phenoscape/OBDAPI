
CREATE OR REPLACE VIEW influences_inheres_in AS
 SELECT
  a.link_id,
  a.node_id,
  a.predicate_id,
  a.source_id,
  a.combinator,
  a.is_inferred,
  a.is_obsolete,
  a.reiflink_node_id,
  a.is_negated,
  a.applies_to_all,
  a.object_quantifier_some,
  a.object_quantifier_only,
  i.object_id
 FROM
  link AS a
  INNER JOIN link AS i ON (a.object_id=i.node_id)
 WHERE
  a.predicate_id IN (SELECT node_id FROM node WHERE uid='OBO_REL:influences') AND
  i.predicate_id IN (SELECT node_id FROM node WHERE uid='OBO_REL:inheres_in') AND
  a.reiflink_node_id IS NOT NULL AND
  i.is_inferred=false;



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
-- CREATE UNIQUE INDEX gene_node_idx_id_uid ON gene_node(node_id,uid);
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

CREATE OR REPLACE VIEW inf_gt_label AS
 SELECT DISTINCT
  node_id AS gt_node_id,
  node_uid AS gt_uid,
  (object_label || ' variant ' || replace(node_uid,'OMIM:','')) AS gt_label
 FROM node_link_node_with_pred
 WHERE pred_uid='OBO_REL:variant_of'
  AND is_inferred='f'
  AND node_uid like 'OMIM:%'
  AND object_uid like 'NCBI_Gene:%'
  AND object_label is not null;

-- UPDATE node SET label=(SELECT gt_label FROM inf_gt_label WHERE inf_gt_label.gt_node_id=node_id) WHERE node.uid LIKE 'OMIM:%';




CREATE OR REPLACE VIEW omim_genotype_gene AS
 SELECT
  og.*,
  split_part(og.uid,'/',1) AS allele,
  l2n.object_id AS gene_id,
  l2n.object_uid AS gene_uid,
  l2n.object_label AS gene_label
 FROM
  node AS og
  INNER JOIN link_to_node AS l2n USING (node_id)
 WHERE
  og.uid LIKE 'OMIM:%' AND
  l2n.predicate_id IN (SELECT node_id FROM node WHERE uid='OBO_REL:variant_of')
  AND l2n.object_uid like 'NCBI_Gene:%';

-- BEGIN MATERIALIZE
-- SELECT create_matview('omim_genotype_gene');
-- CREATE INDEX omim_genotype_gene_idx_id ON omim_genotype_gene(node_id);
-- CREATE INDEX omim_genotype_gene_idx_uid ON omim_genotype_gene(uid);
-- CREATE INDEX omim_genotype_gene_idx_label ON omim_genotype_gene(label);
-- CREATE INDEX omim_genotype_gene_idx_gene_id ON omim_genotype_gene(gene_id);
-- CREATE UNIQUE INDEX omim_genotype_gg_idx_gene_id ON omim_genotype_gene(node_id,gene_id);
-- END MATERIALIZE

CREATE OR REPLACE VIEW omim_genotype AS
 SELECT DISTINCT
  node_id,
  uid,
  label,
  allele
 FROM
  omim_genotype_gene;


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

CREATE OR REPLACE VIEW omim_annotator_pairwise_congruence_by_omim_gene AS
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

 CREATE OR REPLACE VIEW omim_annotator_global_congruence_by_omim_gene AS
 SELECT DISTINCT
  ogg.uid, 
  ogg.label, 
  src.uid AS src_uid,
  aec.* 
 FROM 
  annotated_entity_congruence_by_annotsrc AS aec
  INNER JOIN omim_gene AS ogg ON (aec.annotated_entity_id = ogg.node_id)
  INNER JOIN omim_annotation_source AS src ON (src.node_id=aec.source_id);

CREATE OR REPLACE VIEW avg_information_content_by_omim_gene AS 
 SELECT 
  g.uid,
  g.label,
  aic.*
 FROM 
  avg_information_content_by_annotated_entity AS aic
  INNER JOIN omim_gene AS g ON (aic.annotated_entity_id=g.node_id);

COMMENT ON VIEW avg_information_content_by_omim_gene IS 'Average IC of
each annotation for each OMIM gene. Formally: avg({ic(EQ) :
annotation(g) }). This means that if there are multiple redundant
annotations to the exact same EQ description, the numbers *may* be
skewed towards this description. For example, 3 annotation sources may
all record "small eye". If this EQ has a low IC, then the average will
be lowered, because this is counted 3 times.';

CREATE OR REPLACE VIEW avg_unique_information_content_by_omim_gene AS 
 SELECT 
  g.uid,
  g.label,
  aic.*
 FROM 
  avg_unique_information_content_by_annotated_entity AS aic
  INNER JOIN omim_gene AS g ON (aic.annotated_entity_id=g.node_id);

COMMENT ON VIEW avg_unique_information_content_by_omim_gene IS
'Average IC of each *distinct* EQ description for each OMIM
gene. Formally: avg({DISTINCT ic(EQ) : annotation(g) }). This means
that if there are multiple redundant annotations to the exact same EQ
description, the numbers will *not* be skewed towards this description
(however, this is not a guarantee there will be no annotation bias).';


CREATE OR REPLACE VIEW podocyte_IC AS
 select uid,label,get_information_content(node_id) as ic from node where node_id in (select object_id from node_link where node_uid='ZFA:0000151' or node_uid = 'ZFA:0009285' or node_uid='ZFA:0000150') and uid like 'ZFA%' order by ic;

CREATE OR REPLACE VIEW mislocalised_posteriorly_IC AS
 select uid,label,get_information_content(node_id) as ic from node where node_id in (select object_id from node_link where node_uid='PATO:0001922') and uid like 'PATO:%' order by ic;

CREATE OR REPLACE VIEW rectum_IC AS
 select uid,label,get_information_content(node_id) as ic from node where node_id in (select object_id from node_link where node_uid='PATO:0001922') and uid like 'PATO:%' order by ic;

SELECT realize_relation('OBO_REL:in_organism');
SELECT realize_relation('OBO_REL:variant_of');

CREATE OR REPLACE VIEW distinct_descriptions_for_all_omim AS
 select count(distinct object_id) from omim_genotype_gene as ogg inner join reified_link as al on (ogg.node_id=al.node_id);

CREATE OR REPLACE VIEW distinct_annotations_for_all_omim AS
 select count(distinct link_id) from omim_genotype_gene as ogg inner join reified_link as al on (ogg.node_id=al.node_id);

CREATE OR REPLACE VIEW distinct_uses_of_description_for_all_omim AS 
 select 
  al.object_id AS description_id,
  count(distinct al.node_id) AS num_genotypes
 from omim_genotype_gene as ogg inner join reified_link as al on (ogg.node_id=al.node_id) group by al.object_id;

CREATE OR REPLACE VIEW description_breakdown_for_all_omim AS
 select 
  s.uid,
  count(distinct al.object_id) AS num_descriptions
 from omim_genotype_gene as ogg inner join reified_link as al on (ogg.node_id=al.node_id)
 inner join differentium_link AS dl ON (al.object_id=dl.node_id)
 inner join node AS n ON (dl.object_id=n.node_id)
 left outer join node as s on (n.source_id=s.node_id)
 group by s.uid;

CREATE OR REPLACE VIEW description_breakdown_for_all_omim2 AS
 select 
  substring(n.uid,1,2) As code,
  count(distinct al.object_id) AS num_descriptions
 from omim_genotype_gene as ogg inner join reified_link as al on (ogg.node_id=al.node_id)
 inner join differentium_link AS dl ON (al.object_id=dl.node_id)
 inner join node AS n ON (dl.object_id=n.node_id)
 group by substring(n.uid,1,2);


CREATE OR REPLACE VIEW gene_genotype_phenotype AS
 SELECT
  ogg.*,
  ial.object_id AS phenotype_id
 FROM
  omim_genotype_gene AS ogg
  INNER JOIN implied_annotation_link AS ial ON (ogg.node_id=ial.node_id);

CREATE OR REPLACE VIEW gene_phenotype_gt_count AS
 SELECT
  gene_id,
  phenotype_id,
  count(DISTINCT node_id) AS gt_count
 FROM
  gene_genotype_phenotype AS ggp
 GROUP BY gene_id,   phenotype_id;

CREATE OR REPLACE VIEW gene_gt_count AS
 SELECT
  gene_id,
  count(DISTINCT node_id) AS gt_count
 FROM
  omim_genotype_gene
 GROUP BY gene_id;

CREATE OR REPLACE VIEW phenotype_in_all_genotypes_of_gene AS
 SELECT
  gene_id,
  phenotype_id,
  gc.gt_count
 FROM
  gene_phenotype_gt_count AS gpc
  INNER JOIN gene_gt_count AS gc USING (gene_id)
 WHERE gpc.gt_count = gc.gt_count;

CREATE OR REPLACE VIEW phenotype_in_all_genotypes_of_gene_with_ic AS
 SELECT 
  pg.*,
  ic.shannon_information AS ic
 FROM phenotype_in_all_genotypes_of_gene AS pg
  INNER JOIN class_node_entropy_by_evidence AS ic ON (pg.phenotype_id=ic.node_id);

CREATE OR REPLACE VIEW phenotype_in_all_genotypes_of_gene_with_ic AS
 SELECT 
  pg.*,
  ic.shannon_information AS ic
 FROM phenotype_in_all_genotypes_of_gene AS pg
  INNER JOIN class_node_entropy_by_evidence AS ic ON (pg.phenotype_id=ic.node_id);

CREATE OR REPLACE VIEW phenotype_in_all_genotypes_of_gene_max_ic AS
 SELECT 
  gene_id,
  max(ic) AS max_ic
 FROM phenotype_in_all_genotypes_of_gene_with_ic AS pg
 GROUP BY
  gene_id;

CREATE OR REPLACE VIEW phenotype_in_all_genotypes_of_gene_with_max_ic AS
 SELECT 
  pgmax.gene_id,
  pgic.phenotype_id,
  pgic.ic,
  pgic.gt_count
 FROM 
   phenotype_in_all_genotypes_of_gene_max_ic AS pgmax
   INNER JOIN phenotype_in_all_genotypes_of_gene_with_ic AS pgic ON (pgmax.gene_id=pgic.gene_id AND pgmax.max_ic=pgic.ic);

CREATE OR REPLACE VIEW phenotype_in_all_genotypes_of_gene_with_max_ic_nr AS
 SELECT
  *
 FROM
   phenotype_in_all_genotypes_of_gene_with_max_ic AS pg
 WHERE 
  phenotype_id NOT IN 
   (SELECT 
     pg2.phenotype_id
    FROM
     phenotype_in_all_genotypes_of_gene_with_max_ic AS pg2
     INNER JOIN inferred_irreflexive_link AS il ON (pg2.phenotype_id=il.object_id)
    WHERE il.node_id=pg.phenotype_id AND pg.gene_id=pg2.gene_id);







CREATE OR REPLACE VIEW zebrafish_gene AS
 SELECT
  'ZFIN'::varchar AS src,
  *
 FROM
  node
 WHERE
  uid LIKE 'ZFIN:ZDB-GENE%';

CREATE OR REPLACE VIEW mouse_gene AS
 SELECT
  'MGI'::varchar AS src,
  *
 FROM
  node
 WHERE
  node_id IN (SELECT node_id FROM asserted_is_a_link WHERE object_id = get_node_id('SO:0000704')) AND
  uid LIKE 'MGI:%';

CREATE OR REPLACE VIEW gene AS
 SELECT
  *
 FROM
  node
 WHERE
  node_id IN (SELECT node_id FROM asserted_is_a_link WHERE object_id = get_node_id('SO:0000704'));

CREATE OR REPLACE VIEW genotype AS
 SELECT
  *
 FROM
  node
 WHERE
  node_id IN (SELECT node_id FROM asserted_is_a_link WHERE object_id = get_node_id('SO:0001027'));

CREATE OR REPLACE VIEW mouse_genotype AS
 SELECT
  'MGI'::varchar AS src ,
  *
 FROM
  node
 WHERE
  node_id IN (SELECT node_id FROM asserted_is_a_link WHERE object_id = get_node_id('SO:0001027')) AND
  uid LIKE 'MGI:%';


CREATE OR REPLACE VIEW fm_mgi AS
  SELECT DISTINCT f.uid as f, a.uid as a 
  FROM implied_annotation_link AS ial INNER JOIN mouse_gene AS f ON (f.node_id=ial.node_id);

CREATE OR REPLACE VIEW fm_zfin AS
  SELECT DISTINCT f.uid as f, a.uid as a 
  FROM implied_annotation_link AS ial INNER JOIN node AS f ON (f.node_id=ial.node_id) INNER JOIN node AS a ON (a.node_id=ial.object_id) 
  WHERE f.uid LIKE 'ZFIN:ZDB-GENE-%';

CREATE OR REPLACE VIEW fm_human AS
  SELECT DISTINCT f.uid as f, a.uid as a 
  FROM implied_annotation_link AS ial INNER JOIN node AS f ON (f.node_id=ial.node_id) INNER JOIN node AS a ON (a.node_id=ial.object_id) 
  WHERE f.uid IN (SELECT uid FROM omim_gene);

CREATE OR REPLACE VIEW fm_allg AS
  SELECT DISTINCT f.uid as f, a.uid as a 
  FROM implied_annotation_link AS ial INNER JOIN gene_node AS f ON (f.node_id=ial.node_id) INNER JOIN node AS a ON (a.node_id=ial.object_id); 

CREATE OR REPLACE VIEW fm_allg_direct AS
  SELECT DISTINCT f.uid as f, a.uid as a 
  FROM reified_link AS ial INNER JOIN gene_node AS f ON (f.node_id=ial.node_id) INNER JOIN node AS a ON (a.node_id=ial.object_id);

CREATE OR REPLACE VIEW uid_label_genes AS
  SELECT DISTINCT uid,label FROM gene_node
  WHERE label IS NOT NULL;

CREATE OR REPLACE VIEW fm_closure AS
  SELECT DISTINCT f.uid as f, a.uid as a 
  FROM inferred_link AS l INNER JOIN node AS f ON (f.node_id=l.node_id) INNER JOIN node AS a ON (a.node_id=l.object_id) 
  WHERE f.node_id IN (SELECT object_id FROM reified_link);

CREATE OR REPLACE VIEW fm_all AS
  SELECT DISTINCT f.uid as f, a.uid as a 
  FROM implied_annotation_link AS ial INNER JOIN node AS f ON (f.node_id=ial.node_id) INNER JOIN node AS a ON (a.node_id=ial.object_id); 

CREATE OR REPLACE VIEW fm_all_direct AS
  SELECT DISTINCT f.uid as f, a.uid as a 
  FROM reified_link AS ial INNER JOIN node AS f ON (f.node_id=ial.node_id) INNER JOIN node AS a ON (a.node_id=ial.object_id); 


 
