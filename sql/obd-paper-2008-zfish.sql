CREATE OR REPLACE VIEW zfish_variant_of AS
 SELECT DISTINCT
  variant_of.*
 FROM obo_rel.variant_of INNER JOIN obo_rel.in_organism ON (variant_of.object_id=in_organism.node_id)
 WHERE in_organism.object_id=get_node_id('NCBITaxon:7955');

-- BEGIN MATERIALIZE
-- SELECT create_matview('zfish_variant_of');
-- CREATE INDEX zfish_variant_of_ix1 ON zfish_variant_of(node_id);
-- CREATE INDEX zfish_variant_of_ix2 ON zfish_variant_of(object_id);
-- CREATE UNIQUE INDEX zfish_variant_of_ix3 ON zfish_variant_of(node_id,object_id);
-- END MATERIALIZE

CREATE OR REPLACE VIEW zfish_genotype_gene AS
 SELECT 
  gt.uid,
  gt.label,
  g.node_id AS gene_id,
  g.uid AS gene_uid,
  g.label AS gene_label,
  zfish_variant_of.*
 FROM
  zfish_variant_of INNER JOIN node AS g ON (zfish_variant_of.object_id=g.node_id)
  INNER JOIN node AS gt ON (zfish_variant_of.node_id=gt.node_id);

-- BEGIN MATERIALIZE
-- SELECT create_matview('zfish_genotype_gene');
-- CREATE INDEX zfish_genotype_gene_ix1 ON zfish_genotype_gene(node_id);
-- CREATE INDEX zfish_genotype_gene_ix2 ON zfish_genotype_gene(gene_id);
-- CREATE UNIQUE INDEX zfish_genotype_gene_ix3 ON zfish_genotype_gene(node_id,gene_id);
-- END MATERIALIZE

CREATE OR REPLACE VIEW zfish_gene AS
 SELECT DISTINCT
  g.*
 FROM
  zfish_variant_of INNER JOIN node AS g ON (zfish_variant_of.object_id=g.node_id);

CREATE OR REPLACE FUNCTION populate_cached_pairwise_similarity_for_zfish_genes() RETURNS VOID
 AS
$$
 DECLARE
  v_node1_id  INTEGER;
  v_node2_id  INTEGER;
 BEGIN
  FOR v_node1_id IN SELECT DISTINCT node_id FROM zfish_gene LOOP
   RAISE NOTICE 'in %',v_node1_id;
   FOR v_node2_id IN SELECT DISTINCT node_id FROM zfish_gene LOOP
    INSERT INTO cached_pairwise_similarity SELECT * FROM node_pair_annotation_similarity_score WHERE node1_id=v_node1_id AND node2_id=v_node2_id;
   END LOOP;
  END LOOP;
 END
$$ LANGUAGE 'plpgsql';


CREATE OR REPLACE FUNCTION populate_cached_pairwise_similarity_for_zfish_genotypes() RETURNS VOID
 AS
$$
 DECLARE
  v_node1_id  INTEGER;
  v_node2_id  INTEGER;
 BEGIN
  FOR v_node1_id IN SELECT DISTINCT node_id FROM zfish_variant_of LOOP
   RAISE NOTICE 'in %',v_node1_id;
   FOR v_node2_id IN SELECT DISTINCT node_id FROM zfish_variant_of LOOP
    INSERT INTO cached_pairwise_similarity  SELECT * FROM node_pair_annotation_similarity_score WHERE node1_id=v_node1_id AND node2_id=v_node2_id;
   END LOOP;
  END LOOP;
 END
$$ LANGUAGE 'plpgsql';

CREATE OR REPLACE VIEW zfish_intergene_genotype_similarity AS
 SELECT
  ogg.gene_id,
  ogg.gene_uid,
  ogg.gene_label,
  AVG(basic_score) AS avg_basic_score,
  MAX(basic_score) AS max_basic_score,
  COUNT(DISTINCT ogg.node_id) AS num_genotypes
 FROM
  zfish_genotype_gene AS ogg
  INNER JOIN zfish_genotype_gene AS ogg2 ON (ogg.gene_id!=ogg2.gene_id)
  INNER JOIN cached_pairwise_similarity AS cps ON (ogg.node_id = cps.node1_id AND ogg2.node_id=cps.node2_id AND node1_id != node2_id)
 GROUP BY
  ogg.gene_id,
  ogg.gene_uid,
  ogg.gene_label;

-- BEGIN MATERIALIZE
-- SELECT create_matview('zfish_intergene_genotype_similarity');
-- CREATE INDEX zfish_intergene_genotype_similarity_ix2 ON zfish_intergene_genotype_similarity(gene_id);
-- CREATE INDEX zfish_intergene_genotype_similarity_ix3 ON zfish_intergene_genotype_similarity(max_basic_score);
-- END MATERIALIZE



CREATE OR REPLACE VIEW zfish_intergene_genotype_similarity2 AS
 SELECT
  ogg.gene_id,
  ogg.gene_uid,
  ogg.gene_label,
  ogg2.gene_id AS gene2_id,
  ogg2.gene_uid AS gene2_uid,
  ogg2.gene_label AS gene2_label,
  AVG(basic_score) AS avg_basic_score,
  MAX(basic_score) AS max_basic_score,
  AVG(total_nodes_in_intersection) AS avg_total_nodes_in_intersection,
  COUNT(DISTINCT ogg.node_id) AS num_genotypes,
  COUNT(DISTINCT ogg2.node_id) AS num_genotypes2
 FROM
  zfish_genotype_gene AS ogg
  INNER JOIN zfish_genotype_gene AS ogg2 ON (ogg.gene_id != ogg2.gene_id)
  INNER JOIN cached_pairwise_similarity AS cps ON (ogg.node_id = cps.node1_id AND ogg2.node_id=cps.node2_id AND node1_id!=node2_id)
 GROUP BY
  ogg.gene_id,
  ogg.gene_uid,
  ogg.gene_label,
  ogg2.gene_id,
  ogg2.gene_uid,
  ogg2.gene_label;

-- BEGIN MATERIALIZE
-- SELECT create_matview('zfish_intergene_genotype_similarity2');
-- CREATE INDEX zfish_intergene_genotype_similarity2_ix1 ON zfish_intergene_genotype_similarity2(gene_id);
-- CREATE INDEX zfish_intergene_genotype_similarity2_ix2 ON zfish_intergene_genotype_similarity2(gene2_id);
-- CREATE INDEX zfish_intergene_genotype_similarity2_ix3 ON zfish_intergene_genotype_similarity2(max_basic_score);
-- CREATE UNIQUE INDEX zfish_intergene_genotype_similarity2_ix4 ON zfish_intergene_genotype_similarity2(gene_id, gene2_id, max_basic_score);
-- END MATERIALIZE

CREATE OR REPLACE VIEW most_similar_zfish_intergene_pair AS
 SELECT
  ogg.gene_id,
  ogg.gene_uid,
  ogg.gene_label,
  ogg2.gene_id AS gene2_id,
  ogg2.gene_uid AS gene2_uid,
  ogg2.gene_label AS gene2_label,
  max(basic_score) AS max_basic_score,
  avg(basic_score) AS avg_basic_score
 FROM
  zfish_genotype_gene AS ogg
  INNER JOIN zfish_genotype_gene AS ogg2 ON (ogg.gene_id!=ogg2.gene_id)
  INNER JOIN cached_pairwise_similarity AS cps ON (ogg.node_id = cps.node1_id AND ogg2.node_id=cps.node2_id)
 GROUP BY
  ogg.gene_id,
  ogg.gene_uid,
  ogg.gene_label,
  ogg2.gene_id,
  ogg2.gene_uid,
  ogg2.gene_label;

CREATE OR REPLACE VIEW most_similar_zfish_intergene_genotype_pair AS
 SELECT
  ogg.gene_id,
  ogg.gene_uid,
  ogg.gene_label,
  ogg2.gene_id AS gene2_id,
  ogg2.gene_uid AS gene2_uid,
  ogg2.gene_label AS gene2_label,
  ogg.uid AS uid1,    
  ogg.label AS label1,    
  ogg2.uid AS uid2,    
  ogg2.label AS label2  
 FROM
  zfish_genotype_gene AS ogg
  INNER JOIN zfish_genotype_gene AS ogg2 ON (ogg.gene_id!=ogg2.gene_id)
  INNER JOIN cached_pairwise_similarity AS cps ON (ogg.node_id = cps.node1_id AND ogg2.node_id=cps.node2_id)
 WHERE basic_score IN (SELECT max(avg_basic_score) FROM zfish_intergene_genotype_similarity2 WHERE num_genotypes > 6 AND num_genotypes2 > 6);


