-- rule: ?pa is_a absent; ?pb is_a absent; ?pa inheres_in ?a; ?pb inheres_in ?b; ?b develops_from ?a -> ?pa is_a ?pb

CREATE OR REPLACE VIEW absent_over_develops_from AS
SELECT 
    phenotypeA.node_id,
    (SELECT node_id FROM node WHERE uid = 'OBO_REL:is_a') AS predicate_id,
    phenotypeB.node_id AS object_id
FROM
    node phenotypeA
    JOIN link a_is_a ON (a_is_a.node_id = phenotypeA.node_id AND a_is_a.predicate_id = (SELECT node_id FROM node WHERE uid = 'OBO_REL:is_a') AND a_is_a.object_id = (SELECT node_id FROM node where uid = 'PATO:0000462') AND a_is_a.is_inferred = false)
    JOIN link b_is_a ON (b_is_a.predicate_id = (SELECT node_id FROM node WHERE uid = 'OBO_REL:is_a') AND b_is_a.object_id = (SELECT node_id FROM node where uid = 'PATO:0000462') AND b_is_a.is_inferred = false)
    JOIN node phenotypeB ON (phenotypeB.node_id = b_is_a.node_id)
    JOIN link a_inheres_in ON (a_inheres_in.node_id = phenotypeA.node_id AND a_inheres_in.predicate_id = (SELECT node_id FROM node WHERE uid = 'OBO_REL:inheres_in') and a_inheres_in.is_inferred = false)
    JOIN node entityA ON (entityA.node_id = a_inheres_in.object_id)
    JOIN link b_inheres_in ON (b_inheres_in.node_id = phenotypeB.node_id AND b_inheres_in.predicate_id = (SELECT node_id FROM node WHERE uid = 'OBO_REL:inheres_in') and b_inheres_in.is_inferred = false)
    JOIN node entityB ON (entityB.node_id = b_inheres_in.object_id)
    JOIN link develops_from ON (develops_from.node_id = entityB.node_id AND develops_from.object_id = entityA.node_id AND develops_from.predicate_id = (SELECT node_id FROM node where uid = 'develops_from'))
;
