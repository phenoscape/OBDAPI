CREATE OR REPLACE VIEW phenotype AS 
SELECT
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
  JOIN is_a_link ON (is_a_link.is_inferred = false AND phenotype.node_id = is_a_link.node_id)
  JOIN node inheres_in ON (inheres_in.uid = 'OBO_REL:inheres_in')
  JOIN link inheres_in_link ON (inheres_in_link.is_inferred = false AND phenotype.node_id = inheres_in_link.node_id AND inheres_in.node_id = inheres_in_link.predicate_id)
;
