-- This view is for ad hoc queries on particular terms.
CREATE OR REPLACE VIEW term_relations AS 
SELECT
  subject.node_id AS node_id,
  subject.uid AS node_uid,
  subject.label AS node_label,
  predicate.node_id AS predicate_id,
  predicate.uid AS predicate_uid,
  predicate.label AS predicate_label,
  target.node_id AS object_id,
  target.uid AS object_uid,
  target.label AS object_label,
  link.is_inferred AS is_inferrred
FROM
  link
  JOIN node subject ON (subject.node_id = link.node_id)
  JOIN node predicate ON (predicate.node_id = link.predicate_id)
  JOIN node target ON (target.node_id = link.object_id)
WHERE 
  link.combinator=''
;