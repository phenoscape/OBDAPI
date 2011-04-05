CREATE OR REPLACE VIEW absent_over_develops_from AS
SELECT 
    exhibits.node_id,
    exhibits.predicate_id,
    store_genus_differentium(is_absent.object_id, inheres_in.predicate_id, develops_from.node_id, NULL) AS object_id
FROM
    link exhibits
    JOIN link is_absent ON (is_absent.node_id = exhibits.object_id AND is_absent.predicate_id = (SELECT node_id FROM node WHERE uid = 'OBO_REL:is_a') AND is_absent.object_id = (SELECT node_id FROM node where uid = 'PATO:0000462'))
    JOIN link inheres_in ON (inheres_in.node_id = exhibits.object_id AND inheres_in.predicate_id = (SELECT node_id FROM node WHERE uid = 'OBO_REL:inheres_in'))
    JOIN link develops_from ON (develops_from.predicate_id = (SELECT node_id FROM node where uid = 'develops_from') AND develops_from.object_id = inheres_in.object_id)
WHERE
    exhibits.predicate_id = (SELECT node_id FROM node WHERE uid = 'PHENOSCAPE:exhibits')
;
