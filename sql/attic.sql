
CREATE OR REPLACE VIEW asserted_ancestor_link_proper AS
 SELECT
  inf_link.node_id AS query_node_id,
  inf_link.predicate_id AS query_predicate_id,
  asserted_link.*
 FROM
  link AS inf_link
  INNER JOIN link AS asserted_link ON (inf_link.object_id=asserted_link.node_id)
 WHERE
  asserted_link.is_inferred='f';

COMMENT ON VIEW asserted_ancestor_link_proper IS 'A more efficient
version of asserted_ancestor_link, but excludes the reflexive
case. Can be used if all reflexive links are precomputed';

CREATE OR REPLACE VIEW asserted_ancestor_link AS
 SELECT
  *
 FROM
  asserted_ancestor_link_proper
 UNION
 SELECT
  node_id AS query_node_id,
  predicate_id AS query_predicate_id,
  asserted_link.*
 FROM
  link AS asserted_link
 WHERE
  asserted_link.is_inferred='f';

COMMENT ON VIEW asserted_ancestor_link IS 'An asserted link
that can be reached via closure from query_node_id';
