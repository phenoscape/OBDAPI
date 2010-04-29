-- Some joins may need to be changed to left joins to support inferred annotations
CREATE OR REPLACE VIEW annotation AS 
SELECT
  exhibits_link.node_id AS subject_node_id,
  exhibits_link.object_id AS phenotype_node_id,
  exhibits_link.is_inferred,
  character_text.label AS character_text,
  state_text.label AS state_text,
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
  JOIN node state_text ON (state_text.node_id = has_state_link_to_state.object_id)
  JOIN node has_datum ON (has_datum.uid = 'cdao:has_Datum')
  JOIN link has_datum_link ON (has_datum_link.predicate_id = has_datum.node_id AND has_datum_link.object_id = state_text.node_id)
  JOIN node character_text ON (character_text.node_id = has_datum_link.node_id)
;