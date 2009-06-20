CREATE TABLE phenotype_by_entity_character (
row_number SERIAL PRIMARY KEY,
phenotype_nid INTEGER, 
phenotype_uid VARCHAR, 
subject_nid INTEGER, 
subject_uid VARCHAR, 
subject_label VARCHAR, 
quality_nid INTEGER, 
quality_uid VARCHAR, 
quality_label VARCHAR, 
character_nid INTEGER, 
character_uid VARCHAR, 
character_label VARCHAR, 
entity_nid INTEGER, 
entity_uid VARCHAR, 
entity_label VARCHAR
);


COMMENT ON TABLE phenotype_by_entity_character IS 
'A static table to store every unique phenotype in the database with associations to the 
respective taxa, genes, qualities, characters, and anatomical entities Author: Cartik 
Date: June 20, 2009';
