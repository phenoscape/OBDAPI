CREATE TABLE phenotype_by_entity_character (
row_number SERIAL PRIMARY KEY,
phenotype_nid INTEGER, 
phenotype_uid VARCHAR, 
subject_nid INTEGER, 
subject_uid VARCHAR, 
subject_label VARCHAR, 
gene_or_taxon VARCHAR, 
quality_nid INTEGER, 
quality_uid VARCHAR, 
quality_label VARCHAR, 
character_nid INTEGER, 
character_uid VARCHAR, 
character_label VARCHAR, 
entity_nid INTEGER, 
entity_uid VARCHAR, 
entity_label VARCHAR, 
reif_id INTEGER
);


COMMENT ON TABLE phenotype_by_entity_character IS 
'A static table to store every unique phenotype in the database with associations to the 
respective taxa, genes, qualities, characters, and anatomical entities Author: Cartik 
Date: June 20, 2009';

COMMENT ON COLUMN phenotype_by_entity_character.phenotype_nid IS 
'The stored node id of the PHENOTYPE from the NODE table';
COMMENT ON COLUMN phenotype_by_entity_character.phenotype_uid IS 
'The actual uid of the PHENOTYPE [Eg. PATO:0004067^OBO_REL:inheres_in(TAO:0001510)]';
COMMENT ON COLUMN phenotype_by_entity_character.subj_nid IS 
'The stored node id of the SUBJECT from the NODE table. The SUBJECT may be a species 
such as "Danio rerio" or a gene such as "edar"';
COMMENT ON COLUMN phenotype_by_entity_character.subject_uid IS 
'The actual uid of the SUBJECT. The uid may be the uid of a species Eg: TTO:302 or of 
a gene such as "ZFIN:ZDB-GENE-050107-6"';
COMMENT ON COLUMN phenotype_by_entity_character.subject_label IS 
'The actual label (name) of the SUBJECT. The label may be the label or name of a 
species such as "Danio rerio" or a gene such as "edar"';
COMMENT ON COLUMN phenotype_by_entity_character.gene_or_taxon IS 
'A flag field which indicates whether the SUBJECT is a gene or species. It holds 
one of two possible values viz. "T" when the SUBJECT is a species or "G" when the SUBJECT is a gene';
COMMENT ON COLUMN phenotype_by_entity_character.quality_nid IS 
'The node id of the QUALITY as stored in the NODE TABLE';
COMMENT ON COLUMN phenotype_by_entity_character.quality_uid IS 
'The actual uid of the QUALITY. Eg: PATO:0000467';
COMMENT ON COLUMN phenotype_by_entity_character.quality_label IS 
'The actual label or name of the QUALITY. Eg: "present"';
COMMENT ON COLUMN phenotype_by_entity_character.character_nid IS 
'The node id of the CHARACTER as stored in the NODE table';
COMMENT ON COLUMN phenotype_by_entity_character.character_uid IS 
'The actual uid of the CHARACTER. Eq: PATO:0000070';
COMMENT ON COLUMN phenotype_by_entity_character.character_label IS 
'The actual label or name of the CHARACTER. Eg: "count"';
COMMENT ON COLUMN phenotype_by_entity_character.character_nid IS 
'The node id of the CHARACTER as stored in the NODE table';
COMMENT ON COLUMN phenotype_by_entity_character.character_uid IS 
'The actual uid of the CHARACTER. Eq: PATO:0000070';
COMMENT ON COLUMN phenotype_by_entity_character.character_label IS 
'The actual label or name of the CHARACTER. Eg: "count"';
COMMENT ON COLUMN phenotype_by_entity_character.entity_nid IS 
'The node id of the ANATOMICAL ENTITY as stored in the NODE table';
COMMENT ON COLUMN phenotype_by_entity_character.entity_uid IS 
'The actual uid of the ANATOMICAL ENTITY. Eq: TAO:0001510';
COMMENT ON COLUMN phenotype_by_entity_character.entity_label IS 
'The actual label or name of the ANATOMICAL ENTITY. Eg: "basihyal cartilage"';
COMMENT ON COLUMN phenotype_by_entity_character.reif_id IS 
'The node id of the SUBJECT -> PHENOTYPE assertion as stored in the LINK table. This is 
the link to the publication, character number, character text, state text, comments etc 
(METADATA) that is associated with every SUBJECT -> PHENOTYPE assertion in the database';


CREATE INDEX entity_character_index ON phenotype_by_entity_character(entity_nid, character_nid);
CREATE INDEX subject_index ON phenotype_by_entity_character(subject_nid);

COMMENT ON INDEX entity_character_index IS 
'An index for ENTITY - CHARACTER combinations in the phenotype_with_entity_character table';

COMMENT ON INDEX subject_index IS 
'An index for SUBJECT column in the phenotype_with_entity_character table'; 
