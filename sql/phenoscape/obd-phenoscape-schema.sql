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
count VARCHAR, 
entity_nid INTEGER, 
entity_uid VARCHAR, 
entity_label VARCHAR, 
related_entity_nid INTEGER, 
related_entity_uid VARCHAR, 
related_entity_label VARCHAR, 
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
COMMENT ON COLUMN phenotype_by_entity_character.count IS 
'The count of the ENTITY that is associated with the PHENOTYPE';
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
COMMENT ON COLUMN phenotype_by_entity_character.related_entity_nid IS 
'The node id of the RELATED ANATOMICAL ENTITY as stored in the NODE table';
COMMENT ON COLUMN phenotype_by_entity_character.related_entity_uid IS 
'The actual uid of the RELATED ANATOMICAL ENTITY. Eq: TAO:0001510';
COMMENT ON COLUMN phenotype_by_entity_character.related_entity_label IS 
'The actual label or name of the RELATED ANATOMICAL ENTITY. Eg: "basihyal cartilage"';
COMMENT ON COLUMN phenotype_by_entity_character.reif_id IS 
'The node id of the SUBJECT -> PHENOTYPE assertion as stored in the LINK table. This is 
the link to the publication, character number, character text, state text, comments etc 
(METADATA) that is associated with every SUBJECT -> PHENOTYPE assertion in the database';


CREATE INDEX entity_character_index ON phenotype_by_entity_character(entity_nid, character_nid);
CREATE INDEX subject_index ON phenotype_by_entity_character(subject_nid);
CREATE INDEX entity_index ON phenotype_by_entity_character(entity_nid);
CREATE INDEX phenotype_nid_index ON phenotype_by_entity_character(phenotype_nid);

COMMENT ON INDEX entity_character_index IS 
'An index for ENTITY - CHARACTER combinations in the phenotype_with_entity_character table';

COMMENT ON INDEX subject_index IS 
'An index for SUBJECT column in the phenotype_with_entity_character table'; 

COMMENT ON INDEX entity_index IS 
'An index for ENTITY column in the phenotype_with_entity_character table'; 

COMMENT ON INDEX phenotype_nid_index IS 
'An index for the PHENOTYPE node id column in the phenotype_with_entity_character table';

CREATE TABLE taxon_phenotype_metadata (
row_number SERIAL PRIMARY KEY,
reif_id INTEGER, 
phenotype_uid VARCHAR, 
taxon_uid VARCHAR, 
taxon_label VARCHAR, 
entity_uid VARCHAR, 
entity_label VARCHAR, 
quality_uid VARCHAR, 
quality_label VARCHAR, 
publication VARCHAR, 
character_text VARCHAR, 
character_comment VARCHAR, 
character_number VARCHAR, 
state_text VARCHAR, 
state_comment VARCHAR, 
curators VARCHAR
);

COMMENT ON TABLE taxon_phenotype_metadata IS 
'A data warehouse table that stores the metadata associated with every <TAXON><PHENOTYPE> assertion.';

COMMENT ON TABLE taxon_phenotype_metadata IS 
'A table that stores the metadata associated with every <TAXON><PHENOTYPE> assertion. 
These include the name of the publication, the names of the curators, the character 
and state text from the publication that has been curated, the character number from
the matrix, and the free textcomments from the curators about the character and the state.
In addition, we also store the taxon, entity and quality associated with the phenotype.';

COMMENT ON COLUMN taxon_phenotype_metadata.reif_id IS 
'The node id of the SUBJECT -> PHENOTYPE assertion as stored in the LINK table. This is 
the link to the publication, character number, character text, state text, comments etc 
(METADATA) that is associated with every SUBJECT -> PHENOTYPE assertion in the database';

COMMENT ON COLUMN taxon_phenotype_metadata.phenotype_uid IS 
'The actual uid of the PHENOTYPE [Eg. "PATO:0000052^OBO_REL:inheres_in(TAO:0001274)"]';

COMMENT ON COLUMN taxon_phenotype_metadata.taxon_uid IS 
'The actual uid of the TAXON. Eg. "TTO:1005925"';

COMMENT ON COLUMN taxon_phenotype_metadata.taxon_label IS 
'The actual label (name) of the TAXON. Eg. "Parapimelodus valenciennis"';

COMMENT ON COLUMN taxon_phenotype_metadata.entity_uid IS 
'The actual uid of the ENTITY. Eg. "TAO:0001274"';

COMMENT ON COLUMN taxon_phenotype_metadata.entity_label IS 
'The actual label (name) of the ENTITY. Eg. "coronomeckelian"';

COMMENT ON COLUMN taxon_phenotype_metadata.quality_uid IS 
'The actual uid of the QUALITY. Eg. "PATO:0000052"';

COMMENT ON COLUMN taxon_phenotype_metadata.quality_label IS 
'The actual label (name) of the QUALITY. Eg. "shape"';

COMMENT ON COLUMN taxon_phenotype_metadata.publication IS 
'The citation info of the publication the Taxon to Phenotype
assertion is taken from. Eg. "de Pinna MCC. 1993. Higher-level 
Phylogeny of Siluriformes (Teleostei: Ostariophysi), with a 
New Classification of the Order. New York: City University of New York."';

COMMENT ON COLUMN taxon_phenotype_metadata.character_text IS
'The actual text from the publication about the character, which has been 
curated as this Taxon to Phenotype assertion. Eg: "Coronoid bone in catfishes"';

COMMENT ON COLUMN taxon_phenotype_metadata.character_comment IS
'Comments from the curators on the curated character. Eg: "JGL thinks de Pinna 
means coronomeckelian bone"';

COMMENT ON COLUMN taxon_phenotype_metadata.character_number IS 
'The number assigned to this character in the character-state
data matrix. Eg: "42"';

COMMENT ON COLUMN taxon_phenotype_metadata.state_text IS
'The actual text from the publication about the state, which has been 
curated as this Taxon to Phenotype assertion. Eg: "primitively an 
inconspicuous bone on the lateral profile of the lower jaw, and its dorsal margin 
does not reach the dorsal limit of the coronoid process"';

COMMENT ON COLUMN taxon_phenotype_metadata.state_comment IS 
'Comments from the curators on the curated state. Eg: "= coronomeckelian bone"';

COMMENT ON COLUMN taxon_phenotype_metadata.curators IS 
'The names of the curators who entered this Taxon - Phenotype assertion.
Eg: "John G. Lundberg; Mark Sabaj Pérez"';

CREATE INDEX reif_index ON taxon_phenotype_metadata(reif_id);

COMMENT ON INDEX reif_index IS 
'An index for REIF ID column in the taxon_phenotype_metadata table'; 

CREATE INDEX publication_index ON taxon_phenotype_metadata(publication);

COMMENT ON INDEX publication_index IS 
'An index for the PUBLICATION column in the taxon_phenotype_metadata table';

CREATE TABLE phenotype_inheres_in_part_of_entity (
phenotype_nid INTEGER, 
entity_nid INTEGER, 
entity_uid VARCHAR, 
entity_label VARCHAR
);

COMMENT ON TABLE phenotype_inheres_in_part_of_entity IS 
'A look up table that stores the tuples of the "one to many" relation 
"OBO_REL:inheres_in_part_of" relation between PHENOTYPES and ENTITIES';

COMMENT ON COLUMN phenotype_inheres_in_part_of_entity.entity_nid IS 
'The node id of the ANATOMICAL ENTITY as stored in the NODE table';
COMMENT ON COLUMN phenotype_inheres_in_part_of_entity.entity_uid IS 
'The actual uid of the ANATOMICAL ENTITY. Eq: TAO:0001510';
COMMENT ON COLUMN phenotype_inheres_in_part_of_entity.entity_label IS 
'The actual label or name of the ANATOMICAL ENTITY. Eg: "basihyal cartilage"';

CREATE INDEX entity_uid_index_in_inheres_in_table ON phenotype_inheres_in_part_of_entity(entity_uid);
CREATE INDEX phenotype_nid_index_in_inheres_in_table ON phenotype_inheres_in_part_of_entity(phenotype_nid);

COMMENT ON INDEX entity_uid_index_in_inheres_in_table IS 'An index on the "entity_uid" column in the 
phenotype_inheres_in_part_of_entity table';

COMMENT ON INDEX phenotype_nid_index_in_inheres_in_table IS 'An index on the "phenotype_nid" column in the 
phenotype_inheres_in_part_of_entity table';

--DATA WAREHOUSE TABLES DEFINED AS PART OF NORMALIZATION PROCESS 

CREATE TABLE dw_gene_table (
gene_nid INTEGER PRIMARY KEY, 
gene_uid VARCHAR, 
gene_label VARCHAR 
);

COMMENT ON TABLE dw_gene_table IS 
'A data warehouse table to store information about all the genes with annotations in the Phenoscape knowledgebase'; 

COMMENT ON COLUMN dw_gene_table.gene_nid IS 
'Node id of the gene';

COMMENT ON COLUMN dw_gene_table.gene_uid IS 
'ZFIN ID of the gene';

COMMENT ON COLUMN dw_gene_table.gene_label IS 
'Label of the gene.';

CREATE INDEX dw_gene_label_index ON dw_gene_table(gene_label);

CREATE TABLE dw_gene_alias_table(
gene_nid INTEGER REFERENCES dw_gene_table(gene_nid) ON DELETE CASCADE, 
alias VARCHAR  
);

COMMENT ON TABLE dw_gene_alias_table IS 
'A data warehouse table to store the aliases of all the genes with annotations in the Phenoscape knowledgebase'; 

COMMENT ON COLUMN dw_gene_alias_table.alias IS 
'Alias of the gene';

CREATE INDEX dw_gene_alias_index ON dw_gene_alias_table(alias);

CREATE TABLE dw_genotype_table (
genotype_nid INTEGER PRIMARY KEY, 
genotype_uid VARCHAR, 
genotype_label VARCHAR 
);

COMMENT ON TABLE dw_genotype_table IS 
'Data warehouse table to store genotypes that are associated with phenotypes in the Phenoscape knowledgebase';

COMMENT ON COLUMN dw_genotype_table.genotype_nid IS 
'Primary key (node Id) of the genotype';

COMMENT ON COLUMN dw_genotype_table.genotype_uid IS 
'ZFIN ID of the genotype';

COMMENT ON COLUMN dw_genotype_table.genotype_label IS 
'Label of the genotype';

CREATE TABLE dw_taxon_table (
taxon_nid INTEGER PRIMARY KEY, 
taxon_uid VARCHAR, 
taxon_label VARCHAR, 
taxon_rank VARCHAR 
);

COMMENT ON TABLE dw_taxon_table IS 
'A data warehouse table to store all the evolutionary taxa from TTO that exhibit phenotypes';

COMMENT ON COLUMN dw_taxon_table.taxon_nid IS 
'The node id of the taxon (primary key)';

COMMENT ON COLUMN dw_taxon_table.taxon_uid IS 
'The TTO id of the taxon';

COMMENT ON COLUMN dw_taxon_table.taxon_label IS 
'The valid name of the taxon';

COMMENT ON COLUMN dw_taxon_table.taxon_rank IS 
'The rank of the taxon in the Linnaean classification taxonomy';

CREATE INDEX dw_taxon_label_index ON dw_taxon_table(taxon_label);

CREATE TABLE dw_taxon_alias_table(
taxon_nid INTEGER REFERENCES dw_taxon_table(taxon_nid) ON DELETE CASCADE, 
alias VARCHAR 
);

COMMENT ON TABLE dw_taxon_alias_table IS 
'A data warehouse table to store all the alternative names (synonyms) of evolutionary taxa';

CREATE TABLE dw_taxon_is_a_taxon_table (
subtaxon_nid INTEGER REFERENCES dw_taxon_table(taxon_nid) ON DELETE CASCADE, 
supertaxon_nid INTEGER REFERENCES dw_taxon_table(taxon_nid) ON DELETE CASCADE
);

COMMENT ON TABLE dw_taxon_is_a_taxon_table IS 
'This table stores subsumption relationships between taxa';

CREATE INDEX supertaxon_index ON dw_taxon_is_a_taxon_table(supertaxon_nid);

CREATE TABLE dw_entity_table (
entity_nid INTEGER PRIMARY KEY, 
entity_uid VARCHAR, 
entity_label VARCHAR 
);

COMMENT ON TABLE dw_entity_table IS 
'A data warehouse table to store information about entities from the TAO';

COMMENT ON COLUMN dw_entity_table.entity_nid IS 
'Node id of the entity from Phenoscape KB'; 

COMMENT ON COLUMN dw_entity_table.entity_uid IS 
'TAO id of the entity';

COMMENT ON COLUMN dw_entity_table.entity_label IS 
'Label of the entity'; 

CREATE TABLE dw_entity_is_a_entity_table (
subentity_nid INTEGER, 
superentity_nid INTEGER  
);

COMMENT ON TABLE dw_entity_is_a_entity_table IS 
'Data warehouse table to hold subsumption relationships between entities';

CREATE INDEX superentity_index ON dw_entity_is_a_entity_table(subentity_nid); 

CREATE TABLE dw_entity_part_of_entity_table (
comp_entity_nid INTEGER, 
aggr_entity_nid INTEGER 
);

COMMENT ON TABLE dw_entity_part_of_entity_table IS 
'A data warehouse table to store partonomy relationships between entities'; 

CREATE INDEX aggr_entity_index ON dw_entity_part_of_entity_table(aggr_entity_nid);

CREATE TABLE dw_quality_table (
quality_nid INTEGER PRIMARY KEY, 
quality_uid VARCHAR, 
quality_label VARCHAR  
);

COMMENT ON TABLE dw_quality_table IS 
'A data warehouse table to store quality terms defined in PATO';

CREATE TABLE dw_phenotype_table (
phenotype_nid INTEGER PRIMARY KEY, 
phenotype_uid VARCHAR, 
count_text VARCHAR, 
inheres_in_entity_nid INTEGER, 
towards_entity_nid INTEGER, 
is_a_quality_nid INTEGER, 
character_nid INTEGER REFERENCES dw_quality_table(quality_nid)
);

COMMENT ON TABLE dw_phenotype_table IS 
'Data warehouse table to store information about phenotypes exhibited by evolutionary taxa and genes-genotypes';

COMMENT ON COLUMN dw_phenotype_table.phenotype_nid IS 
'Node id of the phenotype from the Phenoscape KB';

COMMENT ON COLUMN dw_phenotype_table.phenotype_uid IS 
'Post composed UID of the phenotype'; 

COMMENT ON COLUMN dw_phenotype_table.count_text IS 
'Text field to store numerical counts that are associated with the phenotype';

COMMENT ON COLUMN dw_phenotype_table.inheres_in_entity_nid IS 
'The entity that is primarily associated with the phenotype';

COMMENT ON COLUMN dw_phenotype_table.towards_entity_nid IS 
'The relational second entity that may be associated with the phenotype when a relational quality is involved';

COMMENT ON COLUMN dw_phenotype_table.is_a_quality_nid IS 
'The quality that is associated with the phenotype';

COMMENT ON COLUMN dw_phenotype_table.character_nid IS 
'The character for which the quality is a value';

CREATE INDEX dw_phenotype_entity_index ON dw_phenotype_table(inheres_in_entity_nid);

CREATE INDEX dw_phenotype_entity_character_index ON dw_phenotype_table(inheres_in_entity_nid, character_nid);

CREATE TABLE dw_gene_genotype_phenotype_table (
gene_nid INTEGER REFERENCES dw_gene_table(gene_nid) ON DELETE CASCADE, 
genotype_nid INTEGER REFERENCES dw_genotype_table(genotype_nid) ON DELETE CASCADE, 
phenotype_nid INTEGER REFERENCES dw_phenotype_table(phenotype_nid) ON DELETE CASCADE 
);

COMMENT ON TABLE dw_gene_genotype_phenotype_table IS 
'Data warehouse table storing instances of the ternary relationship between gene, genotype, and phenotype';

CREATE INDEX dw_ggp_gene_index ON dw_gene_genotype_phenotype_table(gene_nid);

CREATE INDEX dw_ggp_phenotype_index ON dw_gene_genotype_phenotype_table(phenotype_nid);

CREATE TABLE dw_taxon_phenotype_table(
taxon_nid INTEGER REFERENCES dw_taxon_table(taxon_nid) ON DELETE CASCADE, 
phenotype_nid INTEGER REFERENCES dw_phenotype_table(phenotype_nid) ON DELETE CASCADE,  
reif_id INTEGER   
);

COMMENT ON TABLE dw_taxon_phenotype_table IS 
'Data warehouse table to store direct taxon - phenotype assertions in Phenoscape KB';

COMMENT ON COLUMN dw_taxon_phenotype_table.reif_id IS 
'The reif id which stores the provenance information of the taxon - phenotype assertion';

CREATE INDEX dw_tp_taxon_index ON dw_taxon_phenotype_table(taxon_nid);

CREATE INDEX dw_tp_reifid_index ON dw_taxon_phenotype_table(reif_id);

CREATE TABLE dw_publication_table(
publication TEXT PRIMARY KEY, 
reference_type VARCHAR, 
authors TEXT, 
title TEXT, 
secondary_title TEXT, 
volume VARCHAR, 
pages VARCHAR, 
keywords TEXT, 
publication_year VARCHAR, 
abstract TEXT
);

COMMENT ON TABLE dw_publication_table IS 
'Data warehouse table to store information about publications';

CREATE TABLE dw_publication_reif_id_table(
publication VARCHAR REFERENCES dw_publication_table(publication), 
reif_id INTEGER
);

COMMENT ON TABLE dw_publication_reif_id_table IS 
'Data warehouse table to store relation instances between publication and reif ids, or indirectly
publications and taxon-phenotype assertions';

CREATE INDEX dw_publication_index ON dw_publication_reif_id_table(publication);

CREATE INDEX dw_publication_reif_id_index ON dw_publication_reif_id_table(reif_id);