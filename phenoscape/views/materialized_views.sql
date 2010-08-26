
CREATE OR REPLACE VIEW phenotype AS 
SELECT DISTINCT
  phenotype.node_id AS node_id,
  phenotype.uid, 
  phenotype.label, 
  inheres_in_link.object_id AS entity_node_id,
  is_a_link.object_id AS quality_node_id,
  towards_link.object_id AS related_entity_node_id
FROM 
  node phenotype
  JOIN node towards ON (towards.uid = 'OBO_REL:towards')
  LEFT OUTER JOIN link towards_link ON (phenotype.node_id=towards_link.node_id AND towards_link.is_inferred = false AND towards_link.predicate_id = towards.node_id)
  JOIN node is_a_rel ON (is_a_rel.uid = 'OBO_REL:is_a')
  JOIN link is_a_link ON (is_a_link.is_inferred = false AND phenotype.node_id = is_a_link.node_id AND is_a_link.predicate_id = is_a_rel.node_id)
  JOIN node inheres_in ON (inheres_in.uid = 'OBO_REL:inheres_in')
  JOIN link inheres_in_link ON (inheres_in_link.is_inferred = false AND phenotype.node_id = inheres_in_link.node_id AND inheres_in.node_id = inheres_in_link.predicate_id)
;
SELECT create_matview('phenotype');
CREATE INDEX phenotype_node_id_index ON phenotype(node_id);
CREATE INDEX phenotype_uid_index ON phenotype(uid);
CREATE INDEX phenotype_entity_node_id_index ON phenotype(entity_node_id);
CREATE INDEX phenotype_quality_node_id_index ON phenotype(quality_node_id);
CREATE INDEX phenotype_related_entity_node_id_index ON phenotype(related_entity_node_id);


-- Need to implement a test to ensure that all is_extinct values can be cast to boolean
CREATE OR REPLACE VIEW taxon AS 
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
SELECT create_matview('taxon');
CREATE INDEX taxon_node_id_index ON taxon(node_id);
CREATE INDEX taxon_uid_index ON taxon(uid);
CREATE INDEX taxon_is_extinct_index ON taxon(is_extinct);
CREATE INDEX taxon_parent_node_id_index ON taxon(parent_node_id);
CREATE INDEX taxon_family_node_id_index ON taxon(family_node_id);
CREATE INDEX taxon_family_uid_index ON taxon(family_uid);
CREATE INDEX taxon_order_node_id_index ON taxon(order_node_id);
CREATE INDEX taxon_order_uid_index ON taxon(order_uid);


CREATE OR REPLACE VIEW taxon_annotation AS 
SELECT DISTINCT
  exhibits_link.node_id AS taxon_node_id,
  exhibits_link.object_id AS phenotype_node_id,
  exhibits_link.is_inferred,
  has_datum_link.node_id AS character_node_id,
  has_state_link_to_state.object_id AS state_node_id,
  has_publication_link.object_id AS publication_node_id,
  has_otu_link.object_id AS otu_node_id
FROM
  link exhibits_link
  LEFT JOIN node reiflink ON (reiflink.node_id = exhibits_link.reiflink_node_id)
  LEFT JOIN link posited_by_link ON (posited_by_link.node_id = reiflink.node_id AND posited_by_link.predicate_id = (SELECT node.node_id FROM node WHERE node.uid='posited_by') AND posited_by_link.is_inferred = false)
  LEFT JOIN link has_otu_link ON (has_otu_link.node_id = reiflink.node_id AND has_otu_link.predicate_id = (SELECT node.node_id FROM node WHERE node.uid='PHENOSCAPE:asserted_for_otu') AND has_otu_link.is_inferred = false)
  LEFT JOIN link has_publication_link ON (has_publication_link.node_id = posited_by_link.object_id AND has_publication_link.predicate_id = (SELECT node.node_id FROM node WHERE node.uid='PHENOSCAPE:has_publication') AND has_publication_link.is_inferred = false)
  LEFT JOIN link has_state_link_to_datum ON (has_state_link_to_datum.node_id = reiflink.node_id AND has_state_link_to_datum.predicate_id = (SELECT node.node_id FROM node WHERE node.uid='cdao:has_State') AND has_state_link_to_datum.is_inferred = false)
  LEFT JOIN link has_state_link_to_state ON (has_state_link_to_state.node_id = has_state_link_to_datum.object_id AND has_state_link_to_state.predicate_id = (SELECT node.node_id FROM node WHERE node.uid='cdao:has_State') AND has_state_link_to_state.is_inferred = false)
  LEFT JOIN link has_datum_link ON (has_datum_link.predicate_id = (SELECT node.node_id FROM node WHERE node.uid='cdao:has_Datum') AND has_datum_link.object_id = has_state_link_to_state.object_id AND has_datum_link.is_inferred = false)
WHERE
  exhibits_link.predicate_id = (SELECT node.node_id FROM node WHERE node.uid='PHENOSCAPE:exhibits')
;
SELECT create_matview('taxon_annotation');
CREATE INDEX taxon_annotation_taxon_node_id_index ON taxon_annotation(taxon_node_id);
CREATE INDEX taxon_annotation_phenotype_node_id_index ON taxon_annotation(phenotype_node_id);
CREATE INDEX taxon_annotation_publication_node_id_index ON taxon_annotation(publication_node_id);
CREATE INDEX taxon_annotation_is_inferred_index ON taxon_annotation(is_inferred);


CREATE OR REPLACE VIEW queryable_taxon_annotation AS 
SELECT
  taxon.node_id AS taxon_node_id,
  taxon.uid AS taxon_uid,
  taxon.label AS taxon_label,
  taxon.rank_node_id AS taxon_rank_node_id,
  taxon.rank_uid AS taxon_rank_uid,
  taxon.rank_label AS taxon_rank_label,
  taxon.is_extinct AS taxon_is_extinct,
  taxon.family_node_id AS taxon_family_node_id,
  taxon.family_uid AS taxon_family_uid,
  taxon.family_label AS taxon_family_label,
  taxon.family_is_extinct AS taxon_family_is_extinct,
  taxon.order_node_id AS taxon_order_node_id,
  taxon.order_uid AS taxon_order_uid,
  taxon.order_label AS taxon_order_label,
  taxon.order_is_extinct AS taxon_order_is_extinct,
  taxon_annotation.phenotype_node_id,
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
  publication.label AS publication_label,
  has_number_link.val AS character_number,
  character.node_id AS character_node_id,
  character.label AS character_label,
  state.node_id AS state_node_id,
  state.label AS state_label,
  otu.node_id AS otu_node_id,
  otu.uid AS otu_uid,
  otu.label AS otu_label,
  taxon_annotation.is_inferred
FROM
  taxon_annotation
  JOIN taxon ON (taxon_annotation.taxon_node_id = taxon.node_id)
  JOIN phenotype ON (taxon_annotation.phenotype_node_id = phenotype.node_id)
  JOIN node entity ON (phenotype.entity_node_id = entity.node_id)
  JOIN node quality ON (phenotype.quality_node_id = quality.node_id)
  LEFT JOIN node related_entity ON (phenotype.related_entity_node_id = related_entity.node_id)
  LEFT JOIN node publication ON (taxon_annotation.publication_node_id = publication.node_id)
  LEFT JOIN node character ON (taxon_annotation.character_node_id = character.node_id)
  LEFT JOIN node state ON (taxon_annotation.state_node_id = state.node_id)
  LEFT JOIN tagval has_number_link ON (has_number_link.tag_id = (SELECT node.node_id FROM node WHERE node.uid='PHENOSCAPE:has_number') AND has_number_link.node_id = taxon_annotation.character_node_id)
  LEFT JOIN node otu ON (taxon_annotation.otu_node_id = otu.node_id)
;
SELECT create_matview('queryable_taxon_annotation');
CREATE INDEX queryable_taxon_annotation_taxon_node_id_index ON queryable_taxon_annotation(taxon_node_id);
CREATE INDEX queryable_taxon_annotation_taxon_uid_index ON queryable_taxon_annotation(taxon_uid);
CREATE INDEX queryable_taxon_annotation_taxon_label_index ON queryable_taxon_annotation(taxon_label);
CREATE INDEX queryable_taxon_annotation_taxon_family_node_id_index ON queryable_taxon_annotation(taxon_family_node_id);
CREATE INDEX queryable_taxon_annotation_taxon_family_uid_index ON queryable_taxon_annotation(taxon_family_uid);
CREATE INDEX queryable_taxon_annotation_taxon_family_label_index ON queryable_taxon_annotation(taxon_family_label);
CREATE INDEX queryable_taxon_annotation_taxon_order_node_id_index ON queryable_taxon_annotation(taxon_order_node_id);
CREATE INDEX queryable_taxon_annotation_taxon_order_uid_index ON queryable_taxon_annotation(taxon_order_uid);
CREATE INDEX queryable_taxon_annotation_taxon_order_label_index ON queryable_taxon_annotation(taxon_order_label);
CREATE INDEX queryable_taxon_annotation_phenotype_node_id_index ON queryable_taxon_annotation(phenotype_node_id);
CREATE INDEX queryable_taxon_annotation_entity_node_id_index ON queryable_taxon_annotation(entity_node_id);
CREATE INDEX queryable_taxon_annotation_entity_uid_index ON queryable_taxon_annotation(entity_uid);
CREATE INDEX queryable_taxon_annotation_entity_label_index ON queryable_taxon_annotation(entity_label);
CREATE INDEX queryable_taxon_annotation_quality_node_id_index ON queryable_taxon_annotation(quality_node_id);
CREATE INDEX queryable_taxon_annotation_quality_uid_index ON queryable_taxon_annotation(quality_uid);
CREATE INDEX queryable_taxon_annotation_quality_label_index ON queryable_taxon_annotation(quality_label);
CREATE INDEX queryable_taxon_annotation_related_entity_node_id_index ON queryable_taxon_annotation(related_entity_node_id);
CREATE INDEX queryable_taxon_annotation_related_entity_uid_index ON queryable_taxon_annotation(related_entity_uid);
CREATE INDEX queryable_taxon_annotation_related_entity_label_index ON queryable_taxon_annotation(related_entity_label);
CREATE INDEX queryable_taxon_annotation_publication_node_id_index ON queryable_taxon_annotation(publication_node_id);
CREATE INDEX queryable_taxon_annotation_publication_uid_index ON queryable_taxon_annotation(publication_uid);
CREATE INDEX queryable_taxon_annotation_is_inferred_index ON queryable_taxon_annotation(is_inferred);


--TODO include family and order?
-- CREATE OR REPLACE VIEW distinct_taxon_annotation AS 
-- SELECT DISTINCT
--   queryable_taxon_annotation.taxon_node_id,
--   queryable_taxon_annotation.taxon_uid,
--   queryable_taxon_annotation.taxon_label,
--   queryable_taxon_annotation.taxon_rank_node_id,
--   queryable_taxon_annotation.taxon_rank_uid,
--   queryable_taxon_annotation.taxon_rank_label,
--   queryable_taxon_annotation.taxon_is_extinct,
--   queryable_taxon_annotation.phenotype_node_id,
--   queryable_taxon_annotation.phenotype_uid,
--   queryable_taxon_annotation.phenotype_label,
--   queryable_taxon_annotation.entity_node_id,
--   queryable_taxon_annotation.entity_uid,
--   queryable_taxon_annotation.entity_label,
--   queryable_taxon_annotation.quality_node_id,
--   queryable_taxon_annotation.quality_uid,
--   queryable_taxon_annotation.quality_label,
--   queryable_taxon_annotation.related_entity_node_id,
--   queryable_taxon_annotation.related_entity_uid,
--   queryable_taxon_annotation.related_entity_label
-- FROM
--   queryable_taxon_annotation
-- ;
-- SELECT create_matview('distinct_taxon_annotation');
-- CREATE INDEX distinct_taxon_annotation_taxon_node_id_index ON distinct_taxon_annotation(taxon_node_id);
-- CREATE INDEX distinct_taxon_annotation_taxon_uid_index ON distinct_taxon_annotation(taxon_uid);
-- CREATE INDEX distinct_taxon_annotation_taxon_label_index ON distinct_taxon_annotation(taxon_label);
-- CREATE INDEX distinct_taxon_annotation_phenotype_node_id_index ON distinct_taxon_annotation(phenotype_node_id);
-- CREATE INDEX distinct_taxon_annotation_entity_node_id_index ON distinct_taxon_annotation(entity_node_id);
-- CREATE INDEX distinct_taxon_annotation_entity_uid_index ON distinct_taxon_annotation(entity_uid);
-- CREATE INDEX distinct_taxon_annotation_entity_label_index ON distinct_taxon_annotation(entity_label);
-- CREATE INDEX distinct_taxon_annotation_quality_node_id_index ON distinct_taxon_annotation(quality_node_id);
-- CREATE INDEX distinct_taxon_annotation_quality_uid_index ON distinct_taxon_annotation(quality_uid);
-- CREATE INDEX distinct_taxon_annotation_quality_label_index ON distinct_taxon_annotation(quality_label);
-- CREATE INDEX distinct_taxon_annotation_related_entity_node_id_index ON distinct_taxon_annotation(related_entity_node_id);
-- CREATE INDEX distinct_taxon_annotation_related_entity_uid_index ON distinct_taxon_annotation(related_entity_uid);
-- CREATE INDEX distinct_taxon_annotation_related_entity_label_index ON distinct_taxon_annotation(related_entity_label);


--TODO include family and order?
-- CREATE OR REPLACE VIEW asserted_distinct_taxon_annotation AS 
-- SELECT DISTINCT
--   queryable_taxon_annotation.taxon_node_id,
--   queryable_taxon_annotation.taxon_uid,
--   queryable_taxon_annotation.taxon_label,
--   queryable_taxon_annotation.taxon_rank_node_id,
--   queryable_taxon_annotation.taxon_rank_uid,
--   queryable_taxon_annotation.taxon_rank_label,
--   queryable_taxon_annotation.taxon_is_extinct,
--   queryable_taxon_annotation.phenotype_node_id,
--   queryable_taxon_annotation.phenotype_uid,
--   queryable_taxon_annotation.phenotype_label,
--   queryable_taxon_annotation.entity_node_id,
--   queryable_taxon_annotation.entity_uid,
--   queryable_taxon_annotation.entity_label,
--   queryable_taxon_annotation.quality_node_id,
--   queryable_taxon_annotation.quality_uid,
--   queryable_taxon_annotation.quality_label,
--   queryable_taxon_annotation.related_entity_node_id,
--   queryable_taxon_annotation.related_entity_uid,
--   queryable_taxon_annotation.related_entity_label
-- FROM
--   queryable_taxon_annotation
-- WHERE
--   queryable_taxon_annotation.is_inferred = FALSE
-- ;
-- SELECT create_matview('asserted_distinct_taxon_annotation');
-- CREATE INDEX asserted_distinct_taxon_annotation_taxon_node_id_index ON asserted_distinct_taxon_annotation(taxon_node_id);
-- CREATE INDEX asserted_distinct_taxon_annotation_taxon_uid_index ON asserted_distinct_taxon_annotation(taxon_uid);
-- CREATE INDEX asserted_distinct_taxon_annotation_taxon_label_index ON asserted_distinct_taxon_annotation(taxon_label);
-- CREATE INDEX asserted_distinct_taxon_annotation_phenotype_node_id_index ON asserted_distinct_taxon_annotation(phenotype_node_id);
-- CREATE INDEX asserted_distinct_taxon_annotation_entity_node_id_index ON asserted_distinct_taxon_annotation(entity_node_id);
-- CREATE INDEX asserted_distinct_taxon_annotation_entity_uid_index ON asserted_distinct_taxon_annotation(entity_uid);
-- CREATE INDEX asserted_distinct_taxon_annotation_entity_label_index ON asserted_distinct_taxon_annotation(entity_label);
-- CREATE INDEX asserted_distinct_taxon_annotation_quality_node_id_index ON asserted_distinct_taxon_annotation(quality_node_id);
-- CREATE INDEX asserted_distinct_taxon_annotation_quality_uid_index ON asserted_distinct_taxon_annotation(quality_uid);
-- CREATE INDEX asserted_distinct_taxon_annotation_quality_label_index ON asserted_distinct_taxon_annotation(quality_label);
-- CREATE INDEX asserted_distinct_taxon_annotation_related_entity_node_id_index ON asserted_distinct_taxon_annotation(related_entity_node_id);
-- CREATE INDEX asserted_distinct_taxon_annotation_related_entity_uid_index ON asserted_distinct_taxon_annotation(related_entity_uid);
-- CREATE INDEX asserted_distinct_taxon_annotation_related_entity_label_index ON asserted_distinct_taxon_annotation(related_entity_label);


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
