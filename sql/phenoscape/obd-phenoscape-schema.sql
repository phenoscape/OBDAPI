CREATE TABLE dw_taxon_phenotype_metadata (
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

COMMENT ON TABLE dw_taxon_phenotype_metadata IS 
'A table that stores the metadata associated with every <TAXON><PHENOTYPE> assertion. 
These include the name of the publication, the names of the curators, the character 
and state text from the publication that has been curated, the character number from
the matrix, and the free textcomments from the curators about the character and the state.
In addition, we also store the taxon, entity and quality associated with the phenotype.';

COMMENT ON COLUMN dw_taxon_phenotype_metadata.reif_id IS 
'The node id of the SUBJECT -> PHENOTYPE assertion as stored in the LINK table. This is 
the link to the publication, character number, character text, state text, comments etc 
(METADATA) that is associated with every SUBJECT -> PHENOTYPE assertion in the database';

COMMENT ON COLUMN dw_taxon_phenotype_metadata.phenotype_uid IS 
'The actual uid of the PHENOTYPE [Eg. "PATO:0000052^OBO_REL:inheres_in(TAO:0001274)"]';

COMMENT ON COLUMN dw_taxon_phenotype_metadata.taxon_uid IS 
'The actual uid of the TAXON. Eg. "TTO:1005925"';

COMMENT ON COLUMN dw_taxon_phenotype_metadata.taxon_label IS 
'The actual label (name) of the TAXON. Eg. "Parapimelodus valenciennis"';

COMMENT ON COLUMN dw_taxon_phenotype_metadata.entity_uid IS 
'The actual uid of the ENTITY. Eg. "TAO:0001274"';

COMMENT ON COLUMN dw_taxon_phenotype_metadata.entity_label IS 
'The actual label (name) of the ENTITY. Eg. "coronomeckelian"';

COMMENT ON COLUMN dw_taxon_phenotype_metadata.quality_uid IS 
'The actual uid of the QUALITY. Eg. "PATO:0000052"';

COMMENT ON COLUMN dw_taxon_phenotype_metadata.quality_label IS 
'The actual label (name) of the QUALITY. Eg. "shape"';

COMMENT ON COLUMN dw_taxon_phenotype_metadata.publication IS 
'The citation info of the publication the Taxon to Phenotype
assertion is taken from. Eg. "de Pinna MCC. 1993. Higher-level 
Phylogeny of Siluriformes (Teleostei: Ostariophysi), with a 
New Classification of the Order. New York: City University of New York."';

COMMENT ON COLUMN dw_taxon_phenotype_metadata.character_text IS
'The actual text from the publication about the character, which has been 
curated as this Taxon to Phenotype assertion. Eg: "Coronoid bone in catfishes"';

COMMENT ON COLUMN dw_taxon_phenotype_metadata.character_comment IS
'Comments from the curators on the curated character. Eg: "JGL thinks de Pinna 
means coronomeckelian bone"';

COMMENT ON COLUMN dw_taxon_phenotype_metadata.character_number IS 
'The number assigned to this character in the character-state
data matrix. Eg: "42"';

COMMENT ON COLUMN dw_taxon_phenotype_metadata.state_text IS
'The actual text from the publication about the state, which has been 
curated as this Taxon to Phenotype assertion. Eg: "primitively an 
inconspicuous bone on the lateral profile of the lower jaw, and its dorsal margin 
does not reach the dorsal limit of the coronoid process"';

COMMENT ON COLUMN dw_taxon_phenotype_metadata.state_comment IS 
'Comments from the curators on the curated state. Eg: "= coronomeckelian bone"';

COMMENT ON COLUMN dw_taxon_phenotype_metadata.curators IS 
'The names of the curators who entered this Taxon - Phenotype assertion.
Eg: "John G. Lundberg; Mark Sabaj Pérez"';

CREATE INDEX dw_reif_index ON dw_taxon_phenotype_metadata(reif_id);

COMMENT ON INDEX reif_index IS 
'An index for REIF ID column in the dw_taxon_phenotype_metadata table'; 

CREATE INDEX dw_publication_index ON dw_taxon_phenotype_metadata(publication);

COMMENT ON INDEX publication_index IS 
'An index for the PUBLICATION column in the dw_taxon_phenotype_metadata table';

CREATE TABLE dw_gene_table (
gene_nid INTEGER PRIMARY KEY, 
gene_uid VARCHAR, 
gene_label VARCHAR 
);

COMMENT ON TABLE dw_gene_table IS 
'A table to store information about all the genes with annotations in the Phenoscape knowledgebase'; 

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
'A table to store the aliases of all the genes with annotations in the Phenoscape knowledgebase'; 

COMMENT ON COLUMN dw_gene_alias_table.alias IS 
'Alias of the gene';

CREATE INDEX dw_gene_alias_index ON dw_gene_alias_table(alias);

CREATE TABLE dw_genotype_table (
genotype_nid INTEGER PRIMARY KEY, 
genotype_uid VARCHAR, 
genotype_label VARCHAR 
);

COMMENT ON TABLE dw_genotype_table IS 
'Table to store genotypes that are associated with phenotypes in the Phenoscape knowledgebase';

COMMENT ON COLUMN dw_genotype_table.genotype_nid IS 
'Primary key (node Id) of the genotype';

COMMENT ON COLUMN dw_genotype_table.genotype_uid IS 
'ZFIN ID of the genotype';

COMMENT ON COLUMN dw_genotype_table.genotype_label IS 
'Label of the genotype';

CREATE TABLE dw_gene_genotype_phenotype_table (
gene_nid INTEGER REFERENCES dw_gene_table(gene_nid) ON DELETE CASCADE, 
genotype_nid INTEGER REFERENCES dw_genotype_table(genotype_nid) ON DELETE CASCADE, 
phenotype_nid INTEGER REFERENCES dw_phenotype_table(phenotype_nid) ON DELETE CASCADE 
);

CREATE TABLE dw_phenotype_table (
phenotype_nid INTEGER PRIMARY KEY, 
phenotype_uid VARCHAR, 
count_text VARCHAR, 
inheres_in_entity_nid INTEGER FOREIGN KEY REFERENCES dw_entity_table(entity_nid), 
towards_entity_nid INTEGER FOREIGN KEY REFERENCES dw_entity_table(entity_nid), 
is_a_quality_nid INTEGER FOREIGN KEY REFERENCES dw_quality_table(quality_nid), 
character_nid INTEGER FOREIGN KEY REFERENCES dw_quality_table(quality_nid)
);

CREATE TABLE dw_taxon_table (
taxon_nid INTEGER PRIMARY KEY, 
taxon_uid VARCHAR, 
taxon_label VARCHAR, 
taxon_rank VARCHAR 
);

COMMENT ON TABLE dw_taxon_table IS 
'A table to store all the evolutionary taxa that exhibit phenotypes';

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
taxon_nid INTEGER REFERENCES dw_taxon_table(taxon_nid), 
alias VARCHAR 
);

COMMENT ON TABLE dw_taxon_alias_table IS 
'A table to store all the alternative names (synonyms) of evolutionary taxa';

COMMENT ON COLUMN dw_taxon_alias_table.alias IS 
'The alias of the evolutionary taxon';

CREATE TABLE dw_taxon_phenotype_table(
taxon_nid INTEGER REFERENCES dw_taxon_table(taxon_nid) ON DELETE CASCADE, 
phenotype_nid INTEGER REFERENCES dw_phenotype_table(phenotype_nid) ON DELETE CASCADE,  
reif_id INTEGER   
);

CREATE TABLE dw_entity_table (
entity_nid INTEGER PRIMARY KEY, 
entity_uid VARCHAR, 
entity_label VARCHAR 
);

CREATE TABLE dw_quality_table (
quality_nid INTEGER PRIMARY KEY, 
quality_uid VARCHAR, 
quality_label VARCHAR, 
value_for_character_uid VARCHAR, 
value_for_character_label VARCHAR 
);

CREATE TABLE dw_entity_part_of_entity_table (
comp_entity_nid INTEGER, 
aggr_entity_nid INTEGER 
);

CREATE TABLE dw_taxon_is_a_taxon_table (
subtaxon_nid INTEGER REFERENCES dw_taxon_table(taxon_nid), 
supertaxon_nid INTEGER REFERENCES dw_taxon_table(taxon_nid) 
);

CREATE TABLE dw_entity_is_a_entity_table (
subentity_nid INTEGER, 
superentity_nid INTEGER 
);
