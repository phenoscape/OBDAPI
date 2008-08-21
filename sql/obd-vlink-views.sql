CREATE OR REPLACE VIEW linkstmt AS
 SELECT
  r.uid AS rel,
  node.uid AS subj,
  t.uid AS targ,
  src.uid AS src,
  reif.uid AS reif,
  is_metadata,
  is_inferred,
  is_instantiation,
  is_negated,
  applies_to_all,
  object_quantifier_some,
  object_quantifier_only,
  combinator,
  is_obsolete
 FROM
  link
  INNER JOIN node ON (link.node_id=node.node_id)
  INNER JOIN node AS r ON (link.predicate_id=r.node_id)
  LEFT OUTER node AS src ON (link.source_id=src.node_id)
  LEFT OUTER JOIN node AS reif ON (link.reiflink_node_id=reif.node_id)
  INNER JOIN node AS t ON (link.object_id=t.node_id);

-- BEGIN MATERIALIZE
SELECT create_matview('vlink');
CREATE INDEX vlink_r ON vlink(rel);
CREATE INDEX vlink_s ON vlink(subj);
CREATE INDEX vlink_t ON vlink(targ);
CREATE INDEX vlink_st ON vlink(subj,targ);
CREATE INDEX vlink_rst ON vlink(rel,subj,targ);
-- END MATERIALIZE
