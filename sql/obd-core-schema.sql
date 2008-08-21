-- $Id: obd-core-schema.sql 153 2008-06-04 20:00:34Z cjm $ --
-- $Revision: 153 $ --
-- $Date: 2008-06-04 13:00:34 -0700 (Wed, 04 Jun 2008) $ --

CREATE TABLE obd_schema_metadata (
        schema_release VARCHAR,
        schema_revision VARCHAR,
        schema_version_date VARCHAR,
        notes VARCHAR
);
INSERT INTO obd_schema_metadata (schema_release,schema_revision,schema_version_date) VALUES ('1.1','$Revision: 153 $','$Date: 2008-06-04 13:00:34 -0700 (Wed, 04 Jun 2008) $');

CREATE TYPE t_metatype AS ENUM ('C','I','R');

CREATE SEQUENCE node_node_uid_seq;
CREATE TABLE node (
	node_id SERIAL PRIMARY KEY,
	uid VARCHAR NOT NULL DEFAULT '__internal:_' || nextval('node_node_uid_seq'),
	label VARCHAR,
	uri VARCHAR,
        metatype        CHAR(1),
--        metatype        t_metatype,
	is_anonymous BOOLEAN NOT NULL DEFAULT 'f',
	is_transitive BOOLEAN NOT NULL DEFAULT 'f',
	is_obsolete BOOLEAN NOT NULL DEFAULT 'f',
	is_reiflink BOOLEAN NOT NULL DEFAULT 'f',
	is_metadata BOOLEAN NOT NULL DEFAULT 'f',

	UNIQUE(uid)
);
ALTER TABLE node add source_id INTEGER REFERENCES node(node_id) ON DELETE CASCADE;
ALTER TABLE node add loaded_from_id INTEGER REFERENCES node(node_id) ON DELETE CASCADE;

COMMENT ON TABLE node IS 'An element of a graph. A graph consists of a collection of nodes n1, n2, ... and a collection of edges between nodes. Nodes can represent Instances (eg a particular organ), Relations (eg part_of), and Types (classes) (eg the type "Lung"). RDFMap: typically corresponds to a resource';
COMMENT ON COLUMN node.uid IS 'A unique identifier for this node. Must be EITHER a valid URI OR a valid OBO ID. The latter is of bipartite form IDSPACE:LOCALID';
COMMENT ON COLUMN node.uri IS 'See W3 specs for definition of URI. URIs are optional. Can be generated automaticallt from uid';
COMMENT ON COLUMN node.label IS 'A piece of text intended for display for humans. Also known as "preferred term" in skos, "name" in obo. Also used for the canonical symbol for an entity';
COMMENT ON COLUMN node.metatype IS 'Enumeration that determines the semantics of the node. Choices are: I=instance (aka token, individual, particular). R=relation (aka linktype, relationship type. roughly equivalent to slots, owl properties, DL role names). C=class (universal, type, pattern, term). Annotations (propositions) are of type I';
COMMENT ON COLUMN node.is_anonymous IS 'true if the node represents an entity that does not have a stable persistent identifier. The node only makes sense in the context of the surrounding graph. An example would be a post-composed expression combining classes that themselves have stable IDs. RDFMap: corresponds to bNodes';
COMMENT ON COLUMN node.is_transitive IS 'X R Y and Y R Z => X R Z. Should only be used when metatype=R';
COMMENT ON COLUMN node.is_obsolete IS 'True if node represents a historical record of what was once considered a valid instance, type or relation. Note that this changes the semantics of the node';
COMMENT ON COLUMN node.source_id IS 'A node representing an information resource in which the node was originally inserted. Equivalent to OBO-Namespace';
COMMENT ON COLUMN node.is_reiflink IS 'true if this node identifies an instance of a statement or link. If this is true, there should be a row R in the link table with R.reiflink_node_id equal to the primary key of this row. RDF Closest Equivalent: reification - the subject, predicate and object triples are implicit AND/OR named graphs. At this stage the mapping is unclear. May be better to treat as a proposition in IKL';

CREATE INDEX node_source_indx ON node(source_id);
CREATE INDEX node_label_indx ON node(label);
CREATE INDEX node_metatype_transitive_indx ON node(metatype,is_transitive);

CREATE TYPE t_combinator AS ENUM ('','I','U');

CREATE TABLE link (
	link_id SERIAL PRIMARY KEY,
	reiflink_node_id INTEGER,
	FOREIGN KEY (reiflink_node_id) REFERENCES node(node_id) ON DELETE CASCADE,
	node_id INTEGER NOT NULL,
	FOREIGN KEY (node_id) REFERENCES node(node_id) ON DELETE CASCADE,
	predicate_id INTEGER NOT NULL,
	FOREIGN KEY (predicate_id) REFERENCES node(node_id) ON DELETE CASCADE,
	object_id INTEGER NOT NULL,
	FOREIGN KEY (object_id) REFERENCES node(node_id) ON DELETE CASCADE,
	when_id INTEGER,
	FOREIGN KEY (when_id) REFERENCES node(node_id) ON DELETE CASCADE,
	is_metadata BOOLEAN NOT NULL DEFAULT 'f',
	is_inferred BOOLEAN NOT NULL DEFAULT 'f',
	is_instantiation BOOLEAN NOT NULL DEFAULT 'f',
	is_negated BOOLEAN NOT NULL DEFAULT 'f',
	applies_to_all BOOLEAN NOT NULL DEFAULT 't',
        object_quantifier_some BOOLEAN NOT NULL DEFAULT 't',
        object_quantifier_only BOOLEAN NOT NULL DEFAULT 'f',
	combina            tor CHAR(1) NOT NULL DEFAULT '',
--	combinator t_combinator NOT NULL DEFAULT '',
        source_id INTEGER REFERENCES node(node_id) ON DELETE CASCADE,
        loaded_from_id INTEGER REFERENCES node(node_id) ON DELETE CASCADE,
	is_obsolete BOOLEAN NOT NULL DEFAULT 'f',
---	is_redundant BOOLEAN NOT NULL DEFAULT 'f',
---	inference_type_id INTEGER NOT NULL,
---	FOREIGN KEY (inference_type_id) REFERENCES node(node_id) ON DELETE CASCADE,

        --- should reiflink_node_id also be part of key? multiple statements. YES; but this means we can have multi values for NULL
	CONSTRAINT link_unique_c UNIQUE(reiflink_node_id, is_negated, node_id, predicate_id, object_id, when_id, combinator, source_id)
);
COMMENT ON TABLE link IS 'An element of a graph. Edges link nodes. Edges have labels (predicates). The semantics of the link are dependent on the values of particular columns. ';
COMMENT ON COLUMN link.reiflink_node_id IS 'A reference to a node that represents a statement or other narrative entity asserting this link. If this reference is NULL then the reification context is considered to be the whole database. All Annotations (sensu biomedical curation) are narrative entities positing links. RDF: if this is non-NULL, there is an implicit SPO reification quad';
COMMENT ON COLUMN link.node_id IS 'The entity for which the link directly applies. Can refer to a C, R or I. RDF: subject';
COMMENT ON COLUMN link.node_id IS 'The relation that holds between the node and the object. Must refer to a R node. RDF: predicate';
COMMENT ON COLUMN link.object_id IS 'The entity for which the node stands in some relation R to (where R is the predicate_id). Any node metatype. RDF: object';
COMMENT ON COLUMN link.when_id IS 'some relations are time-indexed; this points to a time point or interval instance for when the link statement holds. Note this is real time, not database time. RDF: does not correpond. If this column is non-null, special transforms must be applied to turn the OBD graph into an RDF graph';
COMMENT ON COLUMN link.is_metadata IS 'True if the link is for node metadata. Metadata applies to the node itself and not the entity represented by the node. Equivalent to annotation properties in OWL';
COMMENT ON COLUMN link.is_inferred IS 'True if this link is implied/entailed via some other link. TODO: reasoning provenance chain';
COMMENT ON COLUMN link.is_instantiation IS 'True if this link is a relation of type OBO_REL:instance_of (or rdf:type). Note this is redundant, but it can speed queries';
COMMENT ON COLUMN link.is_negated IS 'True if the relationship between node and object does NOT hold';
COMMENT ON COLUMN link.applies_to_all IS 'True if the link holds for all instances of the node. Meaningful for class_nodes only. True by default (this is standard ALL-SOME semantics)';
COMMENT ON COLUMN link.object_quantifier_some IS 'True if the link holds for some instance of the class referenced in object_id. Meaningful for class_nodes only. True by default (corresponds to existential restrictions in OWL)';
COMMENT ON COLUMN link.object_quantifier_only IS 'True if the link holds only for instances of the class references in object_id. Meaningful for class_nodes only. False by default (corresponds to universal restrictions in OWL)';
COMMENT ON COLUMN link.combinator IS '
A means of logically grouping links from the same node. If this is non-blank, the semantics of the link table is changed. 
one of:
   I - equivalent to intersection
   U - equivalent to union
   blank - disjunctive
each set of links is grouped by combinator and treated as a distinct set,
so it is possible to have all of:
 a collection of necessary conditions ("")
 a collection of necessary+sufficient conditions by set-intersection (I)
 a collection of necessary+sufficient conditions by set-union (U)
specified directly for a node
   ##
for other combinations, use equivalentClass link to anon class. In the OBO API, I is set if link.isComplete() is true';
COMMENT ON COLUMN link.source_id IS 'A node representing an information resource in which the link was originally asserted. Equivalent to OBO-Namespace. TODO: formalize relationship to RDF named graphs';


CREATE INDEX link_predicate_indx ON link(predicate_id);
CREATE INDEX link_object_indx ON link(object_id);
CREATE INDEX link_node_indx ON link(node_id);
CREATE INDEX link_source_indx ON link(source_id);
CREATE INDEX link_node_predicate_indx ON link(node_id,predicate_id);
CREATE INDEX link_predicate_object_indx ON link(predicate_id,object_id);
CREATE INDEX link_reiflink_node_indx ON link(reiflink_node_id);
CREATE INDEX link_is_inferred_reiflink_node_indx ON link(is_inferred,reiflink_node_id);
CREATE INDEX link_is_node_inferred_reiflink_node_indx ON link(node_id,is_inferred,reiflink_node_id);
CREATE INDEX link_triple_indx ON link(node_id,predicate_id,object_id);
CREATE INDEX link_node_object_indx ON link(node_id,object_id);
CREATE INDEX link_triple_comb_indx ON link(node_id,predicate_id,object_id,combinator);
CREATE INDEX link_triple_link_indx ON link(link_id,node_id,predicate_id,object_id);

CREATE TABLE link_cardinality (
	link_id INTEGER NOT NULL,
	FOREIGN KEY (link_id) REFERENCES link(link_id) ON DELETE CASCADE,
        object_cardinality INTEGER,
        object_min_cardinality INTEGER,
        object_max_cardinality INTEGER,

	CONSTRAINT link_cardinality_unique_c UNIQUE(link_id)
);

COMMENT ON TABLE link_cardinality IS 'Links that reference classes can
have cardinality constraints corresponding to quantitative
restrictions on the number of instances of that class. Equivalent to
OWL cardinality restrictions. Each link row can have at most one row
in the link_cardinality table';


CREATE TABLE link_argument (
        link_argument_id        SERIAL PRIMARY KEY,
	link_id INTEGER NOT NULL,
	FOREIGN KEY (link_id) REFERENCES link(link_id) ON DELETE CASCADE,
	predicate_id INTEGER NOT NULL,
	FOREIGN KEY (predicate_id) REFERENCES node(node_id) ON DELETE CASCADE,
	object_id INTEGER NOT NULL,
	FOREIGN KEY (object_id) REFERENCES node(node_id) ON DELETE CASCADE
);

COMMENT ON TABLE link_argument IS 'n-ary relations (not involving time)';

CREATE TABLE link_inference (
        link_inference_id        SERIAL PRIMARY KEY,
	link_id INTEGER NOT NULL,
	FOREIGN KEY (link_id) REFERENCES link(link_id) ON DELETE CASCADE,
	inferred_from_link_id INTEGER NOT NULL,
	FOREIGN KEY (inferred_from_link_id) REFERENCES link(link_id) ON DELETE CASCADE,
	type_id INTEGER,
	FOREIGN KEY (type_id) REFERENCES node (node_id) ON DELETE CASCADE
);

COMMENT ON TABLE link_inference IS 'A dependency relation between an
inferred link and the links it was inferred from. Can be used for
inference provenance';

COMMENT ON COLUMN link_inference.link_id IS 'A link that has been
inferred by some deductive process';

COMMENT ON COLUMN link_inference.inferred_from_link_id IS 'The link
that was used to support an inferred link. May itself be inferred, or
asserted. Note that cascading deletes do not propagate, unless a
trigger is added such that any inferred link must have a corresponding
link_inference or be deleted.';

COMMENT ON COLUMN link_inference.link_id IS 'An optional inference
type; may come from an ontology of deductive operations';


CREATE TABLE sameas (
        sameas_id        SERIAL PRIMARY KEY,
	node_id INTEGER NOT NULL,
	FOREIGN KEY (node_id) REFERENCES node(node_id) ON DELETE CASCADE,
	object_id INTEGER NOT NULL,
	FOREIGN KEY (object_id) REFERENCES node(node_id) ON DELETE CASCADE,
	is_inferred BOOLEAN NOT NULL DEFAULT 'f',
        source_id INTEGER REFERENCES node(node_id) ON DELETE CASCADE,
        loaded_from_id INTEGER REFERENCES node(node_id) ON DELETE CASCADE,

	CONSTRAINT sameas_unique_c UNIQUE(node_id, object_id, source_id)
 );
COMMENT ON TABLE sameas IS 'STATUS: under discussion. Should we use the generic link table? Currently, no; rationale: sameas is a metamodel relation and not a relation between distinct entities in reality';

CREATE TABLE tagval (
        tagval_id        SERIAL PRIMARY KEY,
	node_id INTEGER NOT NULL,
	FOREIGN KEY (node_id) REFERENCES node(node_id) ON DELETE CASCADE,
	tag_id INTEGER NOT NULL,
	FOREIGN KEY (tag_id) REFERENCES node (node_id) ON DELETE CASCADE,
	datatype_id INTEGER,
	FOREIGN KEY (datatype_id) REFERENCES node (node_id) ON DELETE CASCADE,
	val TEXT NOT NULL,
        source_id INTEGER REFERENCES node(node_id) ON DELETE CASCADE,
        loaded_from_id INTEGER REFERENCES node(node_id) ON DELETE CASCADE

);

COMMENT ON TABLE tagval IS 'Atomic attribute of an entity represented in a node. A pair (T,V) where T is a relation node and V is an atomic piece of data . RDF Equivalent: Triple between resource and a Literal. as opposed to RDF we do not treat literals as nodes in the graph';
COMMENT ON COLUMN tagval.node_id IS 'Node to which this tagval applies';
COMMENT ON COLUMN tagval.tag_id IS 'Relation that holds between node and literal value. This should always point to a relation_node';
COMMENT ON COLUMN tagval.datatype_id IS 'Literal datatype. This should always point to a node corresponding to an XMLSchema datatype. Examples: xsd:string, xsd:integer';
COMMENT ON COLUMN tagval.val IS 'Literal value. Always cast as TEXT. Implementation notes: A view layer utlising SQL types could be hidden underneath this for efficiency of querying';
COMMENT ON COLUMN tagval.source_id IS 'A node representing an information resource in which the tagval was originally asserted. Equivalent to OBO-Namespace. TODO: formalize relationship to RDF named graphs';

CREATE INDEX tagval_nt_index ON tagval(node_id, tag_id);
--CREATE INDEX tagval_label_indx ON tagval(val);

CREATE TABLE alias (
	alias_id SERIAL PRIMARY KEY,
	node_id INTEGER NOT NULL,
	FOREIGN KEY (node_id) REFERENCES node(node_id) ON DELETE CASCADE,
	scope VARCHAR,
	type_id INTEGER,
	FOREIGN KEY (type_id) REFERENCES node(node_id) ON DELETE CASCADE,
	label VARCHAR NOT NULL,
        source_id INTEGER REFERENCES node(node_id) ON DELETE CASCADE,
        loaded_from_id INTEGER REFERENCES node(node_id) ON DELETE CASCADE
);
COMMENT ON TABLE alias IS 'Alternate label for node. Intended for humans';
COMMENT ON COLUMN alias.node_id IS 'Node to which this alias applies';
COMMENT ON COLUMN alias.scope IS 'Context in which label is appropriate: oneof: broad, narrow, related, exact. Equivalent to OBO scope';
COMMENT ON COLUMN alias.type_id IS 'Type of synonym; examples: abbreviation, US-spelling, etc. Equilavent to OBO synonym type';
COMMENT ON COLUMN alias.label IS 'Alternate label for node';
COMMENT ON COLUMN alias.source_id IS 'A node representing an information resource in which the alias was originally asserted. Equivalent to OBO-Namespace. TODO: formalize relationship to RDF named graphs';

CREATE INDEX alias_nt_indx ON alias(node_id, type_id);
CREATE INDEX alias_label_indx ON alias(label);
CREATE INDEX alias_node_label_indx ON alias(node_id,label);

CREATE TABLE description (
	description_id SERIAL PRIMARY KEY,
	node_id INTEGER NOT NULL,
	FOREIGN KEY (node_id) REFERENCES node(node_id) ON DELETE CASCADE,
	scope VARCHAR, --?
	type_id INTEGER,
	FOREIGN KEY (type_id) REFERENCES node(node_id) ON DELETE CASCADE,
	label TEXT NOT NULL,
        source_id INTEGER REFERENCES node(node_id) ON DELETE CASCADE,
        loaded_from_id INTEGER REFERENCES node(node_id) ON DELETE CASCADE
);
COMMENT ON TABLE description IS 'Narrative text intended for humans describing what entity the node represents';
COMMENT ON COLUMN description.node_id IS 'Node to which this description applies';
COMMENT ON COLUMN description.scope IS 'Context in which label is appropriate: oneof: broad, narrow, related, exact. Equivalent to OBO scope';
COMMENT ON COLUMN description.type_id IS 'Type of synonym; examples: abbreviation, US-spelling, etc. Equilavent to OBO synonym type';
COMMENT ON COLUMN description.label IS 'Alternate label for node';
COMMENT ON COLUMN description.source_id IS 'A node representing an information resource in which the description was originally asserted. Equivalent to OBO-Namespace. TODO: formalize relationship to RDF named graphs';

CREATE INDEX description_nt_indx ON description(node_id,type_id);
-- CREATE INDEX description_label_indx ON description(label);  too big for index?
-- CREATE INDEX description_node_label_indx ON description(node_id,label);

CREATE TABLE node_xref (
	node_id INTEGER NOT NULL,
	FOREIGN KEY (node_id) REFERENCES node(node_id) ON DELETE CASCADE,
	xref_id INTEGER NOT NULL,
	FOREIGN KEY (xref_id) REFERENCES node(node_id) ON DELETE CASCADE,
	context_id INTEGER,
	FOREIGN KEY (context_id) REFERENCES node(node_id) ON DELETE CASCADE,

	UNIQUE(node_id, xref_id)
);

COMMENT ON TABLE node_xref IS 'Links between node identifiers. Use in
place of link table if the relationship is on the level of identifiers
rather than a link between the underlying entities denoted by the
nodes. STATUS: may be deprecated in favour of rdf:seeAlso links';

CREATE TABLE description_xref (
	description_id INTEGER NOT NULL,
	FOREIGN KEY (description_id) REFERENCES description(description_id) ON DELETE CASCADE,
	xref_id INTEGER NOT NULL,
	FOREIGN KEY (xref_id) REFERENCES node(node_id) ON DELETE CASCADE,
	context_id INTEGER,
	FOREIGN KEY (context_id) REFERENCES node(node_id) ON DELETE CASCADE,

	UNIQUE(description_id, xref_id)
);

CREATE TABLE node_audit (
	node_id INTEGER NOT NULL,
	FOREIGN KEY (node_id) REFERENCES node(node_id) ON DELETE CASCADE,
	infonode_id INTEGER,
	FOREIGN KEY (infonode_id) REFERENCES node(node_id) ON DELETE CASCADE,
        loadtime        TIMESTAMP default 'now',
        source_id INTEGER REFERENCES node(node_id) ON DELETE CASCADE
);
COMMENT ON TABLE node_audit IS 'An load/update operation on a node
table in this database. Note that this is essentially an internal
book-keeping table, using auto-tmestamps. Other time events, be it
events in nature (such as an occurrence of a biological process) or
events at the data level (eg update time of an annotation) are
considered part of the model, and are represented elsewhere. If the
distinction here seems subtle consider - OBD is a data warehouse; it
is necessary to track timestamps of curator edits independently of
database updates at any one nide';
COMMENT ON COLUMN node_audit.node_id IS 'Node for which this applies';
COMMENT ON COLUMN node_audit.infonode_id IS 'Reference to node representing transaction instance';
COMMENT ON COLUMN node_audit.source_id IS 'A node representing an information resource from which the transaction derives';

CREATE TABLE link_audit (
	link_id INTEGER NOT NULL,
	FOREIGN KEY (link_id) REFERENCES link(link_id) ON DELETE CASCADE,
	infonode_id INTEGER,
	FOREIGN KEY (infonode_id) REFERENCES node(node_id) ON DELETE CASCADE,
        loadtime        TIMESTAMP default 'now',
        source_id INTEGER REFERENCES node(node_id) ON DELETE CASCADE
);
COMMENT ON TABLE link_audit IS 'An load/update operation on a link table. See: node_audit';
COMMENT ON COLUMN link_audit.link_id IS 'Link for which this applies';
COMMENT ON COLUMN link_audit.infonode_id IS 'Reference to node representing transaction instance';
COMMENT ON COLUMN link_audit.source_id IS 'A node representing an information resource from which the transaction derives';

