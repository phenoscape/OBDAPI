CREATE OR REPLACE VIEW nknode AS
 SELECT
  node.uid AS subj,
  node.metatype,
  node.label,
  s.uid AS src
 FROM
  node INNER JOIN node AS s ON (s.node_id=node.source_id);

CREATE OR REPLACE VIEW nklink AS
 SELECT
  r.uid AS rel,
  node.uid AS subj,
  t.uid AS targ,
  src.uid AS src,
  reif.uid AS reif,
  link.is_metadata,
  link.is_inferred,
  link.is_instantiation,
  link.is_negated,
  link.applies_to_all,
  link.object_quantifier_some,
  link.object_quantifier_only,
  link.combinator,
  link.is_obsolete
 FROM
  link
  INNER JOIN node ON (link.node_id=node.node_id)
  INNER JOIN node AS r ON (link.predicate_id=r.node_id)
  LEFT OUTER JOIN node AS src ON (link.source_id=src.node_id)
  LEFT OUTER JOIN node AS reif ON (link.reiflink_node_id=reif.node_id)
  INNER JOIN node AS t ON (link.object_id=t.node_id);

-- BEGIN MATERIALIZE
-- SELECT create_matview('slink');
-- CREATE INDEX vlink_r ON vlink(rel);
-- CREATE INDEX vlink_s ON vlink(subj);
-- CREATE INDEX vlink_t ON vlink(targ);
-- CREATE INDEX vlink_st ON vlink(subj,targ);
-- CREATE INDEX vlink_rst ON vlink(rel,subj,targ);
-- END MATERIALIZE

CREATE OR REPLACE VIEW nkliteral AS
 SELECT
  node.uid AS subj,
  r.uid AS rel,
  val
 FROM
  node_literal_with_pred AS nl
  INNER JOIN node ON (nl.node_id=node.node_id)
  INNER JOIN node AS r ON (nl.predicate_id=r.node_id);
