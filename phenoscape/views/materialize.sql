SELECT create_matview('phenotype');
CREATE INDEX phenotype_node_id_index ON phenotype(node_id);
CREATE INDEX phenotype_quality_node_id_index ON phenotype(quality_node_id);
CREATE INDEX phenotype_related_entity_node_id_index ON phenotype(related_entity_node_id);

SELECT create_matview('taxon');
CREATE INDEX taxon_node_id_index ON taxon(node_id);
CREATE INDEX taxon_uid_index ON taxon(uid);

SELECT create_matview('annotation');
CREATE INDEX annotation_taxon_node_id_index ON annotation(taxon_node_id);
CREATE INDEX annotation_phenotype_node_id_index ON annotation(phenotype_node_id);
CREATE INDEX annotation_publication_node_id_index ON annotation(publication_node_id);
CREATE INDEX annotation_is_inferred_index ON annotation(is_inferred);

SELECT create_matview('queryable_annotation');
CREATE INDEX queryable_annotation_taxon_node_id_index ON queryable_annotation(taxon_node_id);
CREATE INDEX queryable_annotation_phenotype_node_id_index ON queryable_annotation(phenotype_node_id);
CREATE INDEX queryable_annotation_publication_node_id_index ON queryable_annotation(publication_node_id);
CREATE INDEX queryable_annotation_publication_uid_index ON queryable_annotation(publication_uid);
CREATE INDEX queryable_annotation_is_inferred_index ON queryable_annotation(is_inferred);
