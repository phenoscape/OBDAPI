-- This view is for ad hoc queries on particular terms.
CREATE OR REPLACE VIEW term_relations AS 
SELECT
  subject.uid AS subject_uid,
  subject.label AS subject_label,
  predicate.uid AS predicate_uid,
  predicate.label AS predicate_label,
  target.uid AS target_uid,
  target.label AS target_label,
  link.is_inferred AS is_inferrred
FROM
  link
  JOIN node subject ON (subject.node_id = link.node_id)
  JOIN node predicate ON (predicate.node_id = link.predicate_id)
  JOIN node target ON (target.node_id = link.object_id)
  ;