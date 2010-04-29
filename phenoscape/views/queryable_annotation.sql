-- Some joins may need to be changed to left joins to support inferred annotations
CREATE OR REPLACE VIEW queryable_annotation AS 
SELECT
  taxon.node_id AS taxon_node_id,
  taxon.uid AS taxon_uid,
  taxon.label AS taxon_label,
  rank.node_id AS taxon_rank_node_id,
  rank.uid AS taxon_rank_uid,
  rank.label AS taxon_rank_label,
  taxon.is_extinct AS taxon_is_extinct,
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
  annotation.character_text,
  annotation.state_text,
  annotation.is_inferred
FROM
  annotation
  JOIN taxon ON (annotation.subject_node_id = taxon.node_id)
  LEFT OUTER JOIN node rank ON (rank.node_id = taxon.rank_node_id)
  JOIN phenotype ON (annotation.phenotype_node_id = phenotype.node_id)
  JOIN node entity ON (phenotype.entity_node_id = entity.node_id)
  JOIN node quality ON (phenotype.quality_node_id = quality.node_id)
  LEFT OUTER JOIN node related_entity ON (phenotype.related_entity_node_id = related_entity.node_id)
  JOIN node publication ON (annotation.publication_node_id = publication.node_id)
  ;