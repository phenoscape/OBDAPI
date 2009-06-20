-- CREATE SCHEMA phenoscape;
CREATE OR REPLACE VIEW taxon_character_state_phenotype AS
 SELECT
 FROM
  link AS taxon_phenotype
  INNER JOIN link AS (

-- NEW VIEW CREATED BY CARTIK Date: June 20, 2009

CREATE OR REPLACE VIEW phenotype_with_subject_entity_quality_character AS
SELECT DISTINCT 
phenotype_node.node_id AS phenotype_nid, 
phenotype_node.uid AS phenotype, 
CASE WHEN gene_node.node_id IS NULL THEN taxon_node.node_id ELSE gene_node.node_id END AS subject_nid, 
CASE WHEN gene_node.uid IS NULL THEN taxon_node.uid ELSE gene_node.uid END AS subject_uid, 
CASE WHEN gene_node.label IS NULL THEN taxon_node.label ELSE gene_node.label END AS subject_label, 
quality_node.node_id AS quality_nid, 
quality_node.uid AS quality_uid,
quality_node.label AS quality_label, 
character_node.node_id AS character_nid, 
character_node.uid AS character_uid,
character_node.label AS character_label, 
entity_node.node_id AS entity_nid, 
entity_node.uid AS entity_uid,
entity_node.label AS entity_label
FROM 
node AS phenotype_node 
JOIN (link AS exhibits_link 
JOIN (node AS taxon_node 
LEFT OUTER JOIN (link AS has_allele_link 
JOIN node AS gene_node 
ON (has_allele_link.node_id = gene_node.node_id AND 
	has_allele_link.predicate_id = (SELECT node_id FROM node WHERE uid = 'PHENOSCAPE:has_allele'))) 
ON (taxon_node.node_id = has_allele_link.object_id)) 
ON (exhibits_link.node_id = taxon_node.node_id AND 
	exhibits_link.predicate_id = (SELECT node_id FROM node WHERE uid = 'PHENOSCAPE:exhibits'))) 
ON (exhibits_link.object_id = phenotype_node.node_id) 
JOIN (link AS is_a_link 
JOIN (node AS quality_node 
JOIN (link AS value_for_link 
JOIN node AS character_node 
ON (value_for_link.object_id = character_node.node_id AND 
	value_for_link.predicate_id = (SELECT node_id FROM node WHERE uid = 'PHENOSCAPE:value_for'))) 
ON (quality_node.node_id = value_for_link.node_id )) 
ON (is_a_link.object_id = quality_node.node_id AND 
	is_a_link.predicate_id = (SELECT node_id FROM node WHERE uid = 'OBO_REL:is_a')))
ON (phenotype_node.node_id = is_a_link.node_id) 	
JOIN (link AS inheres_in_link 
JOIN node AS entity_node 
ON (entity_node.node_id = inheres_in_link.object_id AND 
	inheres_in_link.predicate_id = (SELECT node_id FROM node WHERE uid = 'OBO_REL:inheres_in'))) 
ON (inheres_in_link.node_id = phenotype_node.node_id) 
WHERE
exhibits_link.is_inferred = 'f' AND 
is_a_link.is_inferred = 'f' AND 
inheres_in_link.is_inferred = 'f'; 

COMMENT ON VIEW phenotype_with_subject_entity_quality_character IS
'A View of all the phenotypes asserted into the database with their respective 
associations to the taxon or gene, quality, character, and anatomical entity. Author: Cartik
Date: 06-20-2009'; 