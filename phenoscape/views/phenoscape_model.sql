CREATE TABLE smart_node_label AS
SELECT
    node_id,
    uid,
    simple_label(node_id) AS simple_label,
    semantic_label(node_id) AS semantic_label
FROM
    node
;
CREATE INDEX smart_node_label_node_id_index ON smart_node_label(node_id);
CREATE INDEX smart_node_label_uid_index ON smart_node_label(uid);
CREATE INDEX smart_node_label_simple_label_index ON smart_node_label(simple_label);
CREATE INDEX smart_node_label_semantic_label_index ON smart_node_label(semantic_label);

CREATE TABLE quality_to_attribute AS
SELECT
  quality.node_id AS quality_node_id,
  attribute.node_id AS attribute_node_id
FROM
  node quality
  JOIN link attribute_to_slim ON (attribute_to_slim.predicate_id = (SELECT node_id from node where uid = 'oboInOwl:inSubset') AND attribute_to_slim.object_id = (SELECT node_id from node where uid = 'character_slim') AND is_inferred = false)
  JOIN link quality_is_a ON (quality_is_a.predicate_id = (SELECT node_id from node where uid = 'OBO_REL:is_a') AND quality_is_a.node_id = quality.node_id)
  JOIN node attribute ON (attribute.node_id = attribute_to_slim.node_id AND attribute.node_id = quality_is_a.object_id)
;

CREATE TABLE phenotype AS 
SELECT DISTINCT
  phenotype.node_id AS node_id,
  phenotype.uid, 
  phenotype.label, 
  entity.node_id AS entity_node_id,
  entity.uid AS entity_uid,
  entity.label AS entity_label,
  quality.node_id AS quality_node_id,
  quality.uid AS quality_uid,
  quality.label AS quality_label,
  related_entity.node_id AS related_entity_node_id,
  related_entity.uid AS related_entity_uid,
  related_entity.label AS related_entity_label
FROM 
  node phenotype
  JOIN link is_a_link ON (is_a_link.is_inferred = false AND phenotype.node_id = is_a_link.node_id AND is_a_link.predicate_id = (SELECT node.node_id FROM node WHERE node.uid='OBO_REL:is_a'))
  JOIN node quality ON (quality.node_id = is_a_link.object_id)
  JOIN link inheres_in_link ON (inheres_in_link.is_inferred = false AND phenotype.node_id = inheres_in_link.node_id AND inheres_in_link.predicate_id = (SELECT node.node_id FROM node WHERE node.uid='OBO_REL:inheres_in'))
  JOIN node entity ON (entity.node_id = inheres_in_link.object_id)
  LEFT JOIN link towards_link ON (phenotype.node_id=towards_link.node_id AND towards_link.is_inferred = false AND towards_link.predicate_id = (SELECT node.node_id FROM node WHERE node.uid='OBO_REL:towards'))
  LEFT JOIN node related_entity ON (related_entity.node_id = towards_link.object_id)
;
CREATE INDEX phenotype_node_id_index ON phenotype(node_id);
CREATE INDEX phenotype_uid_index ON phenotype(uid);
CREATE INDEX phenotype_entity_node_id_index ON phenotype(entity_node_id);
CREATE INDEX phenotype_entity_uid_index ON phenotype(entity_uid);
CREATE INDEX phenotype_entity_label_index ON phenotype(entity_label);
CREATE INDEX phenotype_quality_node_id_index ON phenotype(quality_node_id);
CREATE INDEX phenotype_quality_uid_index ON phenotype(quality_uid);
CREATE INDEX phenotype_quality_label_index ON phenotype(quality_label);
CREATE INDEX phenotype_related_entity_node_id_index ON phenotype(related_entity_node_id);
CREATE INDEX phenotype_related_entity_uid_index ON phenotype(related_entity_uid);
CREATE INDEX phenotype_related_entity_label_index ON phenotype(related_entity_label);


CREATE TABLE taxon AS 
SELECT DISTINCT
  taxon.node_id AS node_id,
  taxon.uid AS uid,
  taxon.label AS label,
  rank.node_id AS rank_node_id,
  rank.uid AS rank_uid,
  rank.label AS rank_label,
  CAST (tagval.val AS BOOLEAN) AS is_extinct,
  parent_taxon.node_id AS parent_node_id,
  parent_taxon.uid AS parent_uid,
  parent_taxon.label AS parent_label,
  parent_rank.node_id AS parent_rank_node_id,
  parent_rank.uid AS parent_rank_uid,
  parent_rank.label AS parent_rank_label,
  CAST (parent_tagval.val AS BOOLEAN) AS parent_is_extinct,
  family_taxon.node_id AS family_node_id,
  family_taxon.uid AS family_uid,
  family_taxon.label AS family_label,
  CAST (family_tagval.val AS BOOLEAN) AS family_is_extinct,
  order_taxon.node_id AS order_node_id,
  order_taxon.uid AS order_uid,
  order_taxon.label AS order_label,
  CAST (order_tagval.val AS BOOLEAN) AS order_is_extinct
FROM
  node taxon
  JOIN node taxonomy ON (taxonomy.uid = 'teleost-taxonomy' AND taxon.source_id = taxonomy.node_id)
  LEFT JOIN link has_rank_link ON (has_rank_link.node_id = taxon.node_id AND has_rank_link.predicate_id = (SELECT node.node_id FROM node WHERE node.uid='has_rank') AND has_rank_link.is_inferred = FALSE)
  LEFT JOIN node rank ON (rank.node_id = has_rank_link.object_id)
  LEFT JOIN tagval ON (tagval.node_id = taxon.node_id AND tagval.tag_id = (SELECT node.node_id FROM node WHERE node.uid='is_extinct'))
  LEFT JOIN link parent_link ON (parent_link.node_id = taxon.node_id AND parent_link.is_inferred = FALSE AND parent_link.predicate_id = (SELECT node.node_id FROM node WHERE node.uid='OBO_REL:is_a'))
  LEFT JOIN node parent_taxon ON (parent_taxon.node_id = parent_link.object_id)
  LEFT JOIN link parent_has_rank_link ON (parent_has_rank_link.node_id = parent_taxon.node_id AND parent_has_rank_link.predicate_id = (SELECT node.node_id FROM node WHERE node.uid='has_rank') AND parent_has_rank_link.is_inferred = FALSE)
  LEFT JOIN node parent_rank ON (parent_rank.node_id = parent_has_rank_link.object_id)
  LEFT JOIN tagval parent_tagval ON (parent_tagval.node_id = parent_taxon.node_id AND parent_tagval.tag_id = (SELECT node.node_id FROM node WHERE node.uid='is_extinct'))
  LEFT JOIN link family_link ON (family_link.node_id = taxon.node_id AND family_link.predicate_id = (SELECT node.node_id FROM node WHERE node.uid='OBO_REL:is_a') AND family_link.object_id IN (SELECT family.node_id FROM node family JOIN link family_has_rank_link ON (family_has_rank_link.node_id = family.node_id AND family_has_rank_link.predicate_id = (SELECT node.node_id FROM node WHERE node.uid='has_rank') AND family_has_rank_link.is_inferred = FALSE AND family_has_rank_link.object_id = (SELECT node.node_id FROM node WHERE node.uid='TAXRANK:0000004'))))
  LEFT JOIN node family_taxon ON (family_taxon.node_id = family_link.object_id)
  LEFT JOIN tagval family_tagval ON (family_tagval.node_id = family_taxon.node_id AND family_tagval.tag_id = (SELECT node.node_id FROM node WHERE node.uid='is_extinct'))
  LEFT JOIN link order_taxon_link ON (order_taxon_link.node_id = taxon.node_id AND order_taxon_link.predicate_id = (SELECT node.node_id FROM node WHERE node.uid='OBO_REL:is_a') AND order_taxon_link.object_id IN (SELECT order_taxon.node_id FROM node order_taxon JOIN link order_taxon_has_rank_link ON (order_taxon_has_rank_link.node_id = order_taxon.node_id AND order_taxon_has_rank_link.predicate_id = (SELECT node.node_id FROM node WHERE node.uid='has_rank') AND order_taxon_has_rank_link.is_inferred = FALSE AND order_taxon_has_rank_link.object_id = (SELECT node.node_id FROM node WHERE node.uid='TAXRANK:0000003'))))
  LEFT JOIN node order_taxon ON (order_taxon.node_id = order_taxon_link.object_id)
  LEFT JOIN tagval order_tagval ON (order_tagval.node_id = order_taxon.node_id AND order_tagval.tag_id = (SELECT node.node_id FROM node WHERE node.uid='is_extinct'))
;
CREATE INDEX taxon_node_id_index ON taxon(node_id);
CREATE INDEX taxon_uid_index ON taxon(uid);
CREATE INDEX taxon_label_index ON taxon(label);
CREATE INDEX taxon_is_extinct_index ON taxon(is_extinct);
CREATE INDEX taxon_parent_node_id_index ON taxon(parent_node_id);
CREATE INDEX taxon_family_node_id_index ON taxon(family_node_id);
CREATE INDEX taxon_family_uid_index ON taxon(family_uid);
CREATE INDEX taxon_order_node_id_index ON taxon(order_node_id);
CREATE INDEX taxon_order_uid_index ON taxon(order_uid);


CREATE TABLE character AS
SELECT DISTINCT
  character.node_id,
  character.uid,
  character.label,
  has_number_link.val AS character_number,
  has_comment_link.val AS comment
FROM 
  node character
  JOIN link ON (link.node_id = character.node_id AND link.predicate_id = (SELECT node.node_id FROM node WHERE node.uid='OBO_REL:instance_of') AND link.object_id = (SELECT node.node_id FROM node WHERE node.uid='cdao:Character'))
  LEFT JOIN tagval has_number_link ON (has_number_link.tag_id = (SELECT node.node_id FROM node WHERE node.uid='PHENOSCAPE:has_number') AND has_number_link.node_id = character.node_id)
  LEFT JOIN tagval has_comment_link ON (has_comment_link.tag_id = (SELECT node.node_id FROM node WHERE node.uid='PHENOSCAPE:has_comment') AND has_comment_link.node_id = character.node_id)
;
CREATE INDEX character_node_id_index ON character(node_id);
CREATE INDEX character_uid_index ON character(uid);
CREATE INDEX character_label_index ON character(label);


CREATE TABLE otu AS
SELECT DISTINCT 
  otu.node_id,
  otu.uid,
  otu.label,
  has_taxon_link.object_id AS taxon_node_id,
  has_comment_link.val AS comment
FROM
  node otu 
  JOIN link instance_of_link ON (instance_of_link.node_id = otu.node_id AND instance_of_link.predicate_id = (SELECT node.node_id FROM node WHERE node.uid='OBO_REL:instance_of') AND instance_of_link.object_id = (SELECT node.node_id FROM node WHERE node.uid='cdao:TU'))
  LEFT JOIN link has_taxon_link ON (has_taxon_link.node_id = otu.node_id AND has_taxon_link.predicate_id = (SELECT node.node_id FROM node WHERE node.uid='PHENOSCAPE:has_taxon') AND has_taxon_link.is_inferred = false)
  LEFT JOIN tagval has_comment_link ON (has_comment_link.tag_id = (SELECT node.node_id FROM node WHERE node.uid='PHENOSCAPE:has_comment') AND has_comment_link.node_id = otu.node_id)
;
CREATE INDEX otu_node_id_index ON otu(node_id);
CREATE INDEX otu_uid_index ON otu(uid);
CREATE INDEX otu_label_index ON otu(label);
CREATE INDEX otu_taxon_node_id_index ON otu(taxon_node_id);


CREATE TABLE state AS
SELECT DISTINCT
  state.node_id,
  state.uid,
  state.label,
  has_datum_link.node_id AS character_node_id,
  has_comment_link.val AS comment
FROM
  node state
  JOIN link instance_of_link ON (instance_of_link.node_id = state.node_id AND instance_of_link.predicate_id = (SELECT node.node_id FROM node WHERE node.uid='OBO_REL:instance_of') AND instance_of_link.object_id = (SELECT node.node_id FROM node WHERE node.uid='cdao:CharacterStateDomain'))
  LEFT JOIN link has_datum_link ON (has_datum_link.predicate_id = (SELECT node.node_id FROM node WHERE node.uid='cdao:has_Datum') AND has_datum_link.object_id = state.node_id AND has_datum_link.is_inferred = false)
  LEFT JOIN tagval has_comment_link ON (has_comment_link.tag_id = (SELECT node.node_id FROM node WHERE node.uid='PHENOSCAPE:has_comment') AND has_comment_link.node_id = state.node_id)
;
CREATE INDEX state_node_id_index ON state(node_id);
CREATE INDEX state_uid_index ON state(uid);
CREATE INDEX state_label_index ON state(label);
CREATE INDEX state_character_node_id_index ON state(character_node_id);


CREATE TABLE taxon_annotation AS
SELECT DISTINCT
  exhibits_link.node_id AS taxon_node_id,
  exhibits_link.object_id AS phenotype_node_id
FROM
  link exhibits_link
  JOIN phenotype ON (phenotype.node_id = exhibits_link.object_id)
  JOIN taxon ON (taxon.node_id = exhibits_link.node_id)
WHERE
  exhibits_link.predicate_id = (SELECT node.node_id FROM node WHERE node.uid='PHENOSCAPE:exhibits')
;
ALTER TABLE taxon_annotation ADD COLUMN annotation_id SERIAL;
CREATE INDEX taxon_annotation_id_index ON taxon_annotation(annotation_id);
CREATE INDEX taxon_annotation_taxon_node_id_index ON taxon_annotation(taxon_node_id);
CREATE INDEX taxon_annotation_phenotype_node_id_index ON taxon_annotation(phenotype_node_id);


CREATE TABLE annotation_source AS
SELECT DISTINCT
  taxon_annotation.annotation_id,
  has_datum_link.node_id AS character_node_id,
  has_state_link_to_state.object_id AS state_node_id,
  has_publication_link.object_id AS publication_node_id,
  has_otu_link.object_id AS otu_node_id
FROM
  node reiflink
  JOIN link exhibits_link ON (reiflink.node_id = exhibits_link.reiflink_node_id AND exhibits_link.predicate_id = (SELECT node.node_id FROM node WHERE node.uid='PHENOSCAPE:exhibits'))
  JOIN taxon_annotation ON (taxon_annotation.taxon_node_id = exhibits_link.node_id AND taxon_annotation.phenotype_node_id = exhibits_link.object_id)
  JOIN link posited_by_link ON (posited_by_link.node_id = reiflink.node_id AND posited_by_link.predicate_id = (SELECT node.node_id FROM node WHERE node.uid='posited_by') AND posited_by_link.is_inferred = false)
  JOIN link has_publication_link ON (has_publication_link.node_id = posited_by_link.object_id AND has_publication_link.predicate_id = (SELECT node.node_id FROM node WHERE node.uid='PHENOSCAPE:has_publication') AND has_publication_link.is_inferred = false)
  JOIN link has_state_link_to_datum ON (has_state_link_to_datum.node_id = reiflink.node_id AND has_state_link_to_datum.predicate_id = (SELECT node.node_id FROM node WHERE node.uid='cdao:has_State') AND has_state_link_to_datum.is_inferred = false)
   JOIN link has_state_link_to_state ON (has_state_link_to_state.node_id = has_state_link_to_datum.object_id AND has_state_link_to_state.predicate_id = (SELECT node.node_id FROM node WHERE node.uid='cdao:has_State') AND has_state_link_to_state.is_inferred = false)
   JOIN link has_datum_link ON (has_datum_link.predicate_id = (SELECT node.node_id FROM node WHERE node.uid='cdao:has_Datum') AND has_datum_link.object_id = has_state_link_to_state.object_id AND has_datum_link.is_inferred = false)
   JOIN link has_otu_link ON (has_otu_link.node_id = reiflink.node_id AND has_otu_link.predicate_id = (SELECT node.node_id FROM node WHERE node.uid='PHENOSCAPE:asserted_for_otu') AND has_otu_link.is_inferred = false)
;
CREATE INDEX annotation_source_annotation_id_index ON annotation_source(annotation_id);
CREATE INDEX annotation_source_character_node_id_index ON annotation_source(character_node_id);
CREATE INDEX annotation_source_state_node_id_index ON annotation_source(state_node_id);
CREATE INDEX annotation_source_publication_node_id_index ON annotation_source(publication_node_id);
CREATE INDEX annotation_source_otu_node_id_index ON annotation_source(otu_node_id);


CREATE TABLE asserted_taxon_annotation AS
SELECT DISTINCT
  *
FROM
  taxon_annotation
WHERE
  annotation_id in (select annotation_id from annotation_source)
;
CREATE INDEX asserted_taxon_annotation_id_index ON asserted_taxon_annotation(annotation_id);
CREATE INDEX asserted_taxon_annotation_taxon_node_id_index ON asserted_taxon_annotation(taxon_node_id);
CREATE INDEX asserted_taxon_annotation_phenotype_node_id_index ON asserted_taxon_annotation(phenotype_node_id);


CREATE TABLE filtered_taxon_annotation AS
SELECT DISTINCT
  taxon_annotation.taxon_node_id,
  taxon_annotation.phenotype_node_id,
  taxon_annotation.annotation_id
FROM
  taxon_annotation
JOIN asserted_taxon_annotation ON (asserted_taxon_annotation.phenotype_node_id = taxon_annotation.phenotype_node_id)
JOIN link taxon_is_a ON (taxon_is_a.predicate_id = (SELECT node_id FROM node where uid = 'OBO_REL:is_a') AND taxon_is_a.node_id = taxon_annotation.taxon_node_id AND taxon_is_a.object_id = asserted_taxon_annotation.taxon_node_id)
;
CREATE INDEX filtered_taxon_annotation_id_index ON filtered_taxon_annotation(annotation_id);
CREATE INDEX filtered_taxon_annotation_taxon_node_id_index ON filtered_taxon_annotation(taxon_node_id);
CREATE INDEX filtered_taxon_annotation_phenotype_node_id_index ON filtered_taxon_annotation(phenotype_node_id);


CREATE OR REPLACE VIEW gene AS 
SELECT DISTINCT
  gene.node_id AS node_id,
  gene.uid AS uid,
  gene.label AS label,
  alias.label AS full_name
FROM
  node gene
  JOIN link instance_of_link ON (instance_of_link.node_id = gene.node_id AND instance_of_link.predicate_id = (SELECT node.node_id FROM node WHERE node.uid='OBO_REL:instance_of') AND instance_of_link.object_id = (SELECT node.node_id FROM node WHERE node.uid='SO:0000704'))
  -- synonym types are not properly loaded by OBD at the moment
  --LEFT JOIN alias ON (gene.node_id = alias.node_id AND alias.type_id = (SELECT node.node_id FROM node WHERE node.uid='FULLNAME'))
  LEFT JOIN alias ON (gene.node_id = alias.node_id)
;
SELECT create_matview('gene');
CREATE INDEX gene_node_id_index ON gene(node_id);
CREATE INDEX gene_uid_index ON gene(uid);


CREATE OR REPLACE VIEW gene_annotation AS 
SELECT DISTINCT
  influences_link.node_id AS genotype_node_id,
  type_link.object_id AS type_node_id,
  gene_link.object_id AS gene_node_id,
  influences_link.object_id AS phenotype_node_id,
  posited_by_link.object_id AS publication_node_id
FROM
  link influences_link
  JOIN link type_link ON (type_link.node_id = influences_link.node_id AND type_link.predicate_id = (SELECT node.node_id FROM node WHERE node.uid='OBO_REL:instance_of') AND type_link.is_inferred = false)
  JOIN link gene_link ON (gene_link.node_id = influences_link.node_id AND gene_link.predicate_id = (SELECT node.node_id FROM node WHERE node.uid='OBO_REL:variant_of'))
  JOIN node reiflink ON (reiflink.node_id = influences_link.reiflink_node_id)
  JOIN link posited_by_link ON (posited_by_link.node_id = reiflink.node_id AND posited_by_link.predicate_id = (SELECT node.node_id FROM node WHERE node.uid='posited_by'))
WHERE
  influences_link.predicate_id = (SELECT node.node_id FROM node WHERE node.uid='OBO_REL:influences')
;
SELECT create_matview('gene_annotation');
CREATE INDEX gene_annotation_genotype_node_id_index ON gene_annotation(genotype_node_id);
CREATE INDEX gene_annotation_type_node_id_index ON gene_annotation(type_node_id);
CREATE INDEX gene_annotation_gene_node_id_index ON gene_annotation(gene_node_id);
CREATE INDEX gene_annotation_phenotype_node_id_index ON gene_annotation(phenotype_node_id);
CREATE INDEX gene_annotation_publication_node_id_index ON gene_annotation(publication_node_id);


CREATE OR REPLACE VIEW queryable_gene_annotation AS 
SELECT
  genotype.node_id AS genotype_node_id,
  genotype.uid AS genotype_uid,
  genotype.label AS genotype_label,
  type.node_id AS type_node_id,
  type.uid AS type_uid,
  type.label AS type_label,
  gene.node_id AS gene_node_id,
  gene.uid AS gene_uid,
  gene.label AS gene_label,
  gene.full_name AS gene_full_name,
  gene_annotation.phenotype_node_id,
  phenotype.uid AS phenotype_uid,
  phenotype.label AS phenotype_label,
  entity.node_id AS entity_node_id,
  entity.uid AS entity_uid,
  entity.label AS entity_label,
  quality.node_id AS quality_node_id,
  quality.uid AS quality_uid,
  quality.label AS quality_label,
  related_entity.node_id AS related_entity_node_id,
  related_entity.uid AS related_entity_uid,
  related_entity.label AS related_entity_label,
  publication.node_id AS publication_node_id,
  publication.uid AS publication_uid,
  publication.label AS publication_label
FROM
  gene_annotation
  JOIN node genotype ON (gene_annotation.genotype_node_id = genotype.node_id)
  JOIN node type ON (gene_annotation.type_node_id = type.node_id)
  JOIN gene ON (gene_annotation.gene_node_id = gene.node_id)
  JOIN phenotype ON (gene_annotation.phenotype_node_id = phenotype.node_id)
  JOIN node entity ON (phenotype.entity_node_id = entity.node_id)
  JOIN node quality ON (phenotype.quality_node_id = quality.node_id)
  LEFT OUTER JOIN node related_entity ON (phenotype.related_entity_node_id = related_entity.node_id)
  JOIN node publication ON (gene_annotation.publication_node_id = publication.node_id)
;
SELECT create_matview('queryable_gene_annotation');
CREATE INDEX queryable_gene_annotation_genotype_node_id_index ON queryable_gene_annotation(genotype_node_id);
CREATE INDEX queryable_gene_annotation_genotype_uid_index ON queryable_gene_annotation(genotype_uid);
CREATE INDEX queryable_gene_annotation_type_node_id_index ON queryable_gene_annotation(type_node_id);
CREATE INDEX queryable_gene_annotation_type_uid_index ON queryable_gene_annotation(type_uid);
CREATE INDEX queryable_gene_annotation_gene_node_id_index ON queryable_gene_annotation(gene_node_id);
CREATE INDEX queryable_gene_annotation_gene_uid_index ON queryable_gene_annotation(gene_uid);
CREATE INDEX queryable_gene_annotation_gene_label_index ON queryable_gene_annotation(gene_label);
CREATE INDEX queryable_gene_annotation_phenotype_node_id_index ON queryable_gene_annotation(phenotype_node_id);
CREATE INDEX queryable_gene_annotation_entity_node_id_index ON queryable_gene_annotation(entity_node_id);
CREATE INDEX queryable_gene_annotation_entity_uid_index ON queryable_gene_annotation(entity_uid);
CREATE INDEX queryable_gene_annotation_entity_label_index ON queryable_gene_annotation(entity_label);
CREATE INDEX queryable_gene_annotation_quality_node_id_index ON queryable_gene_annotation(quality_node_id);
CREATE INDEX queryable_gene_annotation_quality_uid_index ON queryable_gene_annotation(quality_uid);
CREATE INDEX queryable_gene_annotation_quality_label_index ON queryable_gene_annotation(quality_label);
CREATE INDEX queryable_gene_annotation_related_entity_node_id_index ON queryable_gene_annotation(related_entity_node_id);
CREATE INDEX queryable_gene_annotation_related_entity_uid_index ON queryable_gene_annotation(related_entity_uid);
CREATE INDEX queryable_gene_annotation_related_entity_label_index ON queryable_gene_annotation(related_entity_label);
CREATE INDEX queryable_gene_annotation_publication_node_id_index ON queryable_gene_annotation(publication_node_id);
CREATE INDEX queryable_gene_annotation_publication_uid_index ON queryable_gene_annotation(publication_uid);
CREATE INDEX queryable_gene_annotation_publication_label_index ON queryable_gene_annotation(publication_label);


CREATE OR REPLACE VIEW distinct_gene_annotation AS 
SELECT DISTINCT
  queryable_gene_annotation.gene_node_id,
  queryable_gene_annotation.gene_uid,
  queryable_gene_annotation.gene_label,
  queryable_gene_annotation.gene_full_name,
  queryable_gene_annotation.phenotype_node_id,
  queryable_gene_annotation.phenotype_uid,
  queryable_gene_annotation.phenotype_label,
  queryable_gene_annotation.entity_node_id,
  queryable_gene_annotation.entity_uid,
  queryable_gene_annotation.entity_label,
  queryable_gene_annotation.quality_node_id,
  queryable_gene_annotation.quality_uid,
  queryable_gene_annotation.quality_label,
  queryable_gene_annotation.related_entity_node_id,
  queryable_gene_annotation.related_entity_uid,
  queryable_gene_annotation.related_entity_label
FROM
  queryable_gene_annotation
;
SELECT create_matview('distinct_gene_annotation');
CREATE INDEX distinct_gene_annotation_gene_node_id_index ON distinct_gene_annotation(gene_node_id);
CREATE INDEX distinct_gene_annotation_gene_uid_index ON distinct_gene_annotation(gene_uid);
CREATE INDEX distinct_gene_annotation_gene_label_index ON distinct_gene_annotation(gene_label);
CREATE INDEX distinct_gene_annotation_phenotype_node_id_index ON distinct_gene_annotation(phenotype_node_id);
CREATE INDEX distinct_gene_annotation_entity_node_id_index ON distinct_gene_annotation(entity_node_id);
CREATE INDEX distinct_gene_annotation_entity_uid_index ON distinct_gene_annotation(entity_uid);
CREATE INDEX distinct_gene_annotation_entity_label_index ON distinct_gene_annotation(entity_label);
CREATE INDEX distinct_gene_annotation_quality_node_id_index ON distinct_gene_annotation(quality_node_id);
CREATE INDEX distinct_gene_annotation_quality_uid_index ON distinct_gene_annotation(quality_uid);
CREATE INDEX distinct_gene_annotation_quality_label_index ON distinct_gene_annotation(quality_label);
CREATE INDEX distinct_gene_annotation_related_entity_node_id_index ON distinct_gene_annotation(related_entity_node_id);
CREATE INDEX distinct_gene_annotation_related_entity_uid_index ON distinct_gene_annotation(related_entity_uid);
CREATE INDEX distinct_gene_annotation_related_entity_label_index ON distinct_gene_annotation(related_entity_label);
