-- Need to change is_extinct to boolean (it is text now)
CREATE OR REPLACE VIEW taxon AS 
SELECT DISTINCT
  taxon.node_id AS node_id,
  taxon.uid AS uid,
  taxon.label AS label,
  has_rank_link.object_id AS rank_node_id,
  tagval.val AS is_extinct,
  parent_link.object_id AS parent_node_id
FROM
  node taxon
  JOIN node chordata ON (chordata.uid = 'TTO:1')
  JOIN is_a_link ON (is_a_link.node_id = taxon.node_id AND is_a_link.object_id = chordata.node_id)
  JOIN node has_rank_rel ON (has_rank_rel.uid = 'has_rank')
  LEFT OUTER JOIN link has_rank_link ON (has_rank_link.node_id = taxon.node_id AND has_rank_link.predicate_id = has_rank_rel.node_id AND has_rank_link.is_inferred = FALSE)
  JOIN node is_extinct_rel ON (is_extinct_rel.uid = 'is_extinct')
  LEFT OUTER JOIN tagval ON (tagval.node_id = taxon.node_id AND tagval.tag_id = is_extinct_rel.node_id)
  LEFT OUTER JOIN is_a_link parent_link ON (parent_link.node_id = taxon.node_id AND parent_link.is_inferred = FALSE)
;
