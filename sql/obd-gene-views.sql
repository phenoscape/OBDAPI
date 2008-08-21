CREATE OR REPLACE VIEW gene_node AS
 SELECT
  *
 FROM
  node
 WHERE node_id IN (SELECT isa.node_id FROM is_a_link AS isa INNER JOIN node AS t ON (t.node_id=isa.object_id));

-- BEGIN MATERIALIZE
SELECT create_matview('gene_node');
CREATE INDEX gene_node_idx_id ON gene_node(node_id);
CREATE INDEX gene_node_idx_uid ON gene_node(uid);
CREATE INDEX gene_node_idx_label ON gene_node(label);
-- END MATERIALIZE


CREATE OR REPLACE VIEW gene_implied_annotation_link AS
 SELECT
  gn.uid AS uid,
  gn.source_id AS node_source_id,
  gn.label AS label,
  gn.metatype,
  ial.*
 FROM
  implied_annotation_link AS ial 
  INNER JOIN gene_node AS gn USING (node_id);

-- BEGIN MATERIALIZE
SELECT create_matview('gene_implied_annotation_link');
CREATE INDEX gene_implied_annotation_link_idx_node_id ON gene_implied_annotation_link(node_id);
CREATE INDEX gene_implied_annotation_link_idx_object_id ON gene_implied_annotation_link(object_id);
-- END MATERIALIZE



-- CONVENIENCE VIEW
-- EXAMPLE: select object_uid,object_label,count(distinct node_id) from gene_implied_annotation_link_to_node where object_label='Eye' group by object_uid,object_label; 
CREATE OR REPLACE VIEW gene_implied_annotation_link_to_node AS
 SELECT
  tn.uid AS object_uid,
  tn.source_id AS object_source_id,
  tn.label AS object_label,
  tn.metatype AS object_metatype,
  ial.*
 FROM
 gene_implied_annotation_link AS ial
 INNER JOIN node AS tn ON (ial.object_id=tn.node_id);

-- this view appears slow..
CREATE OR REPLACE VIEW gene_node_pair_annotation_similarity_score AS
 SELECT
  gn1.uid AS node1_uid,
  gn1.label AS node1_label,
  gn1.source_id AS node1_source_id,
  gn1.metatype AS node1_metatype,
  gn2.uid AS node2_uid,
  gn2.label AS node2_label,
  gn2.source_id AS node2_source_id,
  gn2.metatype AS node2_metatype,
  npss.*
 FROM
  node_pair_annotation_similarity_score AS npss
  INNER JOIN gene_node AS gn1 ON (gn1.node_id=npss.node1_id)
  INNER JOIN gene_node AS gn2 ON (gn2.node_id=npss.node2_id)
 WHERE
  npss.total_nodes_in_intersection > 0;

-- BEGIN MATERIALIZE
-- SELECT create_matview('gene_node_pair_annotation_similarity_score');
-- CREATE INDEX gene_node_pair_annotation_similarity_score_idx_ids ON gene_node_pair_annotation_similarity_score(node1_id,node2_id);
-- END MATERIALIZE


CREATE OR REPLACE VIEW count_of_annotated_gene AS
 SELECT 
  count(DISTINCT node_id) AS total
 FROM
  link INNER JOIN gene_node USING (node_id)
 WHERE
  reiflink_node_id IS NOT NULL;

-- BEGIN MATERIALIZE
SELECT create_matview('count_of_annotated_gene');
-- END MATERIALIZE

CREATE OR REPLACE VIEW count_of_annotated_gene_by_class_node AS
 SELECT 
  ial.object_id                     AS node_id,
  CAST(NULL AS INT)                 AS evidence_id,
  count(DISTINCT ial.node_id)         AS total
 FROM
  gene_implied_annotation_link AS ial
 GROUP BY ial.object_id;

-- BEGIN MATERIALIZE
SELECT create_matview('count_of_annotated_gene_by_class_node');
-- END MATERIALIZE

 
CREATE OR REPLACE VIEW class_node_entropy_against_geneset AS
 SELECT 
  node_id,
  tbc.total                                          AS annotated_entity_count,
  - log(cast(tbc.total AS float) / t.total) / log(2) AS shannon_information
 FROM
  count_of_annotated_gene_by_class_node                    AS tbc,
  count_of_annotated_gene                                  AS t;

-- BEGIN MATERIALIZE
SELECT create_matview('class_node_entropy_against_geneset');
CREATE UNIQUE INDEX class_node_entropy_against_geneset_idx_node_id ON class_node_entropy_against_geneset(node_id);
CREATE INDEX class_node_entropy_against_geneset_idx_info ON class_node_entropy_against_geneset(shannon_information);
CREATE UNIQUE INDEX class_node_entropy_against_geneset_idx_node_id_info ON class_node_entropy_against_geneset(node_id,shannon_information);
-- END MATERIALIZE

CREATE OR REPLACE VIEW gene_implied_annotation_link_with_entropy AS
 SELECT
  e.shannon_information,
  e.annotated_entity_count,
  ial.*
 FROM
  gene_implied_annotation_link AS ial
  INNER JOIN class_node_entropy_against_geneset AS e ON (ial.object_id=e.node_id);

-- BEGIN MATERIALIZE
SELECT create_matview('gene_implied_annotation_link_with_entropy');
CREATE INDEX gene_implied_annotation_link_with_entropy_idx_node_id ON gene_implied_annotation_link_with_entropy(node_id);
CREATE INDEX gene_implied_annotation_link_with_entropy_idx_object_id ON gene_implied_annotation_link_with_entropy(object_id);
CREATE INDEX gene_implied_annotation_link_with_entropy_idx_node_object_id ON gene_implied_annotation_link_with_entropy(node_id,object_id);
CREATE INDEX gene_implied_annotation_link_with_entropy_idx_node_object_id_ic ON gene_implied_annotation_link_with_entropy(node_id,object_id,shannon_information);
-- END MATERIALIZE

CREATE OR REPLACE VIEW gene_node_pair_intersection_with_entropy AS
 SELECT
  ial1.node_id AS node1_id,
  ial2.node_id AS node2_id,
  ial1.object_id,
  ial1.shannon_information,
  ial1.annotated_entity_count
 FROM 
  gene_implied_annotation_link_with_entropy AS ial1,
  gene_implied_annotation_link_with_entropy AS ial2
 WHERE
  ial1.object_id = ial2.object_id; -- both annotations agree


CREATE OR REPLACE VIEW class_node_entropy_against_geneset_with_auto_label AS
 SELECT 
  *,
  node_auto_label(node_id)      AS label
 FROM
  class_node_entropy_against_geneset;


