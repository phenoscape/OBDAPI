-- These may need to use "select distinct" to handle duplicate equivalent assertions that are possible in the Knowledgebase - this has been a problem with some nodes in TTO.  Perhaps creation of equivalent link rows should be prevented in OBD?


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
  parent_link.object_id AS parent_node_id
FROM
  node taxon
  -- this is sufficient only if there are only taxon nodes in the TTO
  JOIN node taxonomy ON (taxonomy.uid = 'teleost-taxonomy' AND taxon.source_id = taxonomy.node_id)
  JOIN node has_rank_rel ON (has_rank_rel.uid = 'has_rank')
  LEFT OUTER JOIN link has_rank_link ON (has_rank_link.node_id = taxon.node_id AND has_rank_link.predicate_id = has_rank_rel.node_id AND has_rank_link.is_inferred = FALSE)
  LEFT OUTER JOIN node rank ON (rank.node_id = has_rank_link.object_id)
  JOIN node is_extinct_rel ON (is_extinct_rel.uid = 'is_extinct')
  LEFT OUTER JOIN tagval ON (tagval.node_id = taxon.node_id AND tagval.tag_id = is_extinct_rel.node_id)
  JOIN node is_a_rel ON (is_a_rel.uid = 'OBO_REL:is_a')
  LEFT OUTER JOIN link parent_link ON (parent_link.node_id = taxon.node_id AND parent_link.is_inferred = FALSE AND parent_link.predicate_id = is_a_rel.node_id)
;
SELECT create_matview('taxon');
CREATE INDEX taxon_node_id_index ON taxon(node_id);
CREATE INDEX taxon_uid_index ON taxon(uid);
CREATE INDEX taxon_is_extinct_index ON taxon(label);


-- Some joins may need to be changed to left joins to support inferred annotations
CREATE OR REPLACE VIEW annotation AS 
SELECT DISTINCT
  exhibits_link.node_id AS taxon_node_id,
  exhibits_link.object_id AS phenotype_node_id,
  exhibits_link.is_inferred,
  has_datum_link.node_id AS character_node_id,
  has_state_link_to_state.object_id AS state_node_id,
  has_publication_link.object_id AS publication_node_id
FROM
  link exhibits_link
  JOIN node exhibits ON (exhibits.uid = 'PHENOSCAPE:exhibits' AND exhibits_link.predicate_id = exhibits.node_id)
  JOIN node reiflink ON (reiflink.node_id = exhibits_link.reiflink_node_id)
  JOIN node posited_by ON (posited_by.uid = 'posited_by')
  JOIN link posited_by_link ON (posited_by_link.node_id = reiflink.node_id AND posited_by_link.predicate_id = posited_by.node_id)
  JOIN node has_publication ON (has_publication.uid = 'PHENOSCAPE:has_publication')
  JOIN link has_publication_link ON (has_publication_link.node_id = posited_by_link.object_id AND has_publication_link.predicate_id = has_publication.node_id)
  JOIN node has_state ON (has_state.uid = 'cdao:has_State')
  JOIN link has_state_link_to_datum ON (has_state_link_to_datum.node_id = reiflink.node_id AND has_state_link_to_datum.predicate_id = has_state.node_id)
  JOIN link has_state_link_to_state ON (has_state_link_to_state.node_id = has_state_link_to_datum.object_id AND has_state_link_to_state.predicate_id = has_state.node_id)
  JOIN node has_datum ON (has_datum.uid = 'cdao:has_Datum')
  JOIN link has_datum_link ON (has_datum_link.predicate_id = has_datum.node_id AND has_datum_link.object_id = has_state_link_to_state.object_id)
;
SELECT create_matview('annotation');
CREATE INDEX annotation_taxon_node_id_index ON annotation(taxon_node_id);
CREATE INDEX annotation_phenotype_node_id_index ON annotation(phenotype_node_id);
CREATE INDEX annotation_publication_node_id_index ON annotation(publication_node_id);
CREATE INDEX annotation_is_inferred_index ON annotation(is_inferred);


-- Some joins may need to be changed to left joins to support inferred annotations
-- why does this have more rows than annotation???
CREATE OR REPLACE VIEW queryable_annotation AS 
SELECT
  taxon.node_id AS taxon_node_id,
  taxon.uid AS taxon_uid,
  taxon.label AS taxon_label,
  rank.node_id AS taxon_rank_node_id,
  rank.uid AS taxon_rank_uid,
  rank.label AS taxon_rank_label,
  taxon.is_extinct AS taxon_is_extinct,
  annotation.phenotype_node_id,
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
  annotation.is_inferred
FROM
  annotation
  JOIN taxon ON (annotation.taxon_node_id = taxon.node_id)
  LEFT OUTER JOIN node rank ON (rank.node_id = taxon.rank_node_id)
  JOIN phenotype ON (annotation.phenotype_node_id = phenotype.node_id)
  JOIN node entity ON (phenotype.entity_node_id = entity.node_id)
  JOIN node quality ON (phenotype.quality_node_id = quality.node_id)
  LEFT OUTER JOIN node related_entity ON (phenotype.related_entity_node_id = related_entity.node_id)
  JOIN node publication ON (annotation.publication_node_id = publication.node_id)
  JOIN node character ON (annotation.character_node_id = character.node_id)
  JOIN node state ON (annotation.state_node_id = state.node_id)
  JOIN node has_number_rel ON (has_number_rel.uid = 'PHENOSCAPE:has_number')
  JOIN tagval has_number_link ON (has_number_link.tag_id = has_number_rel.node_id AND has_number_link.node_id = annotation.character_node_id)
;
SELECT create_matview('queryable_annotation');
CREATE INDEX queryable_annotation_taxon_node_id_index ON queryable_annotation(taxon_node_id);
CREATE INDEX queryable_annotation_taxon_uid_index ON queryable_annotation(taxon_uid);
CREATE INDEX queryable_annotation_taxon_label_index ON queryable_annotation(taxon_label);
CREATE INDEX queryable_annotation_phenotype_node_id_index ON queryable_annotation(phenotype_node_id);
CREATE INDEX queryable_annotation_entity_node_id_index ON queryable_annotation(entity_node_id);
CREATE INDEX queryable_annotation_entity_uid_index ON queryable_annotation(entity_uid);
CREATE INDEX queryable_annotation_entity_label_index ON queryable_annotation(entity_label);
CREATE INDEX queryable_annotation_quality_node_id_index ON queryable_annotation(quality_node_id);
CREATE INDEX queryable_annotation_quality_uid_index ON queryable_annotation(quality_uid);
CREATE INDEX queryable_annotation_quality_label_index ON queryable_annotation(quality_label);
CREATE INDEX queryable_annotation_related_entity_node_id_index ON queryable_annotation(related_entity_node_id);
CREATE INDEX queryable_annotation_related_entity_uid_index ON queryable_annotation(related_entity_uid);
CREATE INDEX queryable_annotation_related_entity_label_index ON queryable_annotation(related_entity_label);
CREATE INDEX queryable_annotation_publication_node_id_index ON queryable_annotation(publication_node_id);
CREATE INDEX queryable_annotation_publication_uid_index ON queryable_annotation(publication_uid);
CREATE INDEX queryable_annotation_is_inferred_index ON queryable_annotation(is_inferred);


CREATE OR REPLACE VIEW distinct_annotation AS 
SELECT DISTINCT
  queryable_annotation.taxon_node_id,
  queryable_annotation.taxon_uid,
  queryable_annotation.taxon_label,
  queryable_annotation.taxon_rank_node_id,
  queryable_annotation.taxon_rank_uid,
  queryable_annotation.taxon_rank_label,
  queryable_annotation.taxon_is_extinct,
  queryable_annotation.phenotype_node_id,
  queryable_annotation.phenotype_uid,
  queryable_annotation.phenotype_label,
  queryable_annotation.entity_node_id,
  queryable_annotation.entity_uid,
  queryable_annotation.entity_label,
  queryable_annotation.quality_node_id,
  queryable_annotation.quality_uid,
  queryable_annotation.quality_label,
  queryable_annotation.related_entity_node_id,
  queryable_annotation.related_entity_uid,
  queryable_annotation.related_entity_label
FROM
  queryable_annotation
;
SELECT create_matview('distinct_annotation');
CREATE INDEX distinct_annotation_taxon_node_id_index ON distinct_annotation(taxon_node_id);
CREATE INDEX distinct_annotation_taxon_uid_index ON distinct_annotation(taxon_uid);
CREATE INDEX distinct_annotation_taxon_label_index ON distinct_annotation(taxon_label);
CREATE INDEX distinct_annotation_phenotype_node_id_index ON distinct_annotation(phenotype_node_id);
CREATE INDEX distinct_annotation_entity_node_id_index ON distinct_annotation(entity_node_id);
CREATE INDEX distinct_annotation_entity_uid_index ON distinct_annotation(entity_uid);
CREATE INDEX distinct_annotation_entity_label_index ON distinct_annotation(entity_label);
CREATE INDEX distinct_annotation_quality_node_id_index ON distinct_annotation(quality_node_id);
CREATE INDEX distinct_annotation_quality_uid_index ON distinct_annotation(quality_uid);
CREATE INDEX distinct_annotation_quality_label_index ON distinct_annotation(quality_label);
CREATE INDEX distinct_annotation_related_entity_node_id_index ON distinct_annotation(related_entity_node_id);
CREATE INDEX distinct_annotation_related_entity_uid_index ON distinct_annotation(related_entity_uid);
CREATE INDEX distinct_annotation_related_entity_label_index ON distinct_annotation(related_entity_label);


CREATE OR REPLACE VIEW asserted_distinct_annotation AS 
SELECT DISTINCT
  queryable_annotation.taxon_node_id,
  queryable_annotation.taxon_uid,
  queryable_annotation.taxon_label,
  queryable_annotation.taxon_rank_node_id,
  queryable_annotation.taxon_rank_uid,
  queryable_annotation.taxon_rank_label,
  queryable_annotation.taxon_is_extinct,
  queryable_annotation.phenotype_node_id,
  queryable_annotation.phenotype_uid,
  queryable_annotation.phenotype_label,
  queryable_annotation.entity_node_id,
  queryable_annotation.entity_uid,
  queryable_annotation.entity_label,
  queryable_annotation.quality_node_id,
  queryable_annotation.quality_uid,
  queryable_annotation.quality_label,
  queryable_annotation.related_entity_node_id,
  queryable_annotation.related_entity_uid,
  queryable_annotation.related_entity_label
FROM
  queryable_annotation
WHERE
  queryable_annotation.is_inferred = FALSE
;
SELECT create_matview('asserted_distinct_annotation');
CREATE INDEX asserted_distinct_annotation_taxon_node_id_index ON asserted_distinct_annotation(taxon_node_id);
CREATE INDEX asserted_distinct_annotation_taxon_uid_index ON asserted_distinct_annotation(taxon_uid);
CREATE INDEX asserted_distinct_annotation_taxon_label_index ON asserted_distinct_annotation(taxon_label);
CREATE INDEX asserted_distinct_annotation_phenotype_node_id_index ON asserted_distinct_annotation(phenotype_node_id);
CREATE INDEX asserted_distinct_annotation_entity_node_id_index ON asserted_distinct_annotation(entity_node_id);
CREATE INDEX asserted_distinct_annotation_entity_uid_index ON asserted_distinct_annotation(entity_uid);
CREATE INDEX asserted_distinct_annotation_entity_label_index ON asserted_distinct_annotation(entity_label);
CREATE INDEX asserted_distinct_annotation_quality_node_id_index ON asserted_distinct_annotation(quality_node_id);
CREATE INDEX asserted_distinct_annotation_quality_uid_index ON asserted_distinct_annotation(quality_uid);
CREATE INDEX asserted_distinct_annotation_quality_label_index ON asserted_distinct_annotation(quality_label);
CREATE INDEX asserted_distinct_annotation_related_entity_node_id_index ON asserted_distinct_annotation(related_entity_node_id);
CREATE INDEX asserted_distinct_annotation_related_entity_uid_index ON asserted_distinct_annotation(related_entity_uid);
CREATE INDEX asserted_distinct_annotation_related_entity_label_index ON asserted_distinct_annotation(related_entity_label);
