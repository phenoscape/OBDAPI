UPDATE link SET predicate_id = $is_a WHERE predicate_id IN (SELECT node_id FROM node WHERE uid='is_a');
