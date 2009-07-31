-- CREATE SCHEMA obd_core_view;
-- SET search_path TO obd_core_view,public;

-- ************************************************************
-- SPECIFICIY VIEWS
-- ************************************************************
-- obd-core is hyper-normalized / ultra-generic
-- the views below define a more specific schema, layered on the core

-- %% node %%

-- basic categories
CREATE OR REPLACE VIEW class_node AS SELECT * FROM node WHERE metatype='C';
COMMENT ON VIEW class_node IS 'A graph node representing a Class. Examples: a GO class; a post-composed phenotype; an internal class';

CREATE OR REPLACE VIEW relation_node AS SELECT * FROM node WHERE metatype='R';
COMMENT ON VIEW relation_node IS 'A graph node representing a Relation. Examples: OBO_REL:part_of';

CREATE OR REPLACE VIEW transitive_relation_node AS SELECT * FROM node WHERE metatype='R' AND is_transitive='t';
COMMENT ON VIEW transitive_relation_node IS 'A relation node that is transitive. Examples: part_of, is_a. Counter-examples: has_participant';

-- BEGIN MATERIALIZE
-- SELECT create_matview('transitive_relation_node');
-- CREATE INDEX transitive_relation_node_idx_node ON transitive_relation_node(node_id);
-- CREATE INDEX transitive_relation_node_idx_node_uid ON transitive_relation_node(node_id,uid);
-- END MATERIALIZE

CREATE OR REPLACE VIEW metadata_relation_node AS SELECT * FROM node WHERE metatype='R' AND is_metadata='t';
COMMENT ON VIEW metadata_relation_node IS 'A relation node that is for metadata relations (annotationProperties in OWL)';

CREATE OR REPLACE VIEW instance_node AS SELECT * FROM node WHERE metatype='I';
COMMENT ON VIEW instance_node IS 'A graph node representing a Instance. Examples: an instance of an Annotation; an instance of a Person';

CREATE OR REPLACE VIEW source_node AS SELECT * FROM node 
 WHERE node_id IN (SELECT DISTINCT source_id FROM node UNION SELECT DISTINCT source_id FROM link);
COMMENT ON VIEW source_node IS 'A graph node representing a Source. Example: cell ontology';

CREATE OR REPLACE VIEW reif_node AS SELECT * FROM node WHERE is_reiflink='t';
COMMENT ON VIEW reif_node IS 'A node that is reified - ie for which a
corresponding link exists. The node acts as proxy-ID for the
link. Join via link.reiflink_node_id. Uses: If additional data is to
be attached to a link - ie metadata, attribution info - then a
reif_node is created, and the metadata is attached to the reif_node';

-- obsoletion-related
CREATE OR REPLACE VIEW obsolete_node AS SELECT * FROM node WHERE is_obsolete='t';
COMMENT ON VIEW obsolete_node IS 'A graph node containing obsolete data. Contrast: valid_node. Obsolete nodes may be mapped to valid_nodes using...';

CREATE OR REPLACE VIEW obsolete_class_node AS SELECT * FROM class_node WHERE is_obsolete='t';
COMMENT ON VIEW obsolete_class_node IS 'A class node that is obsolete';

CREATE OR REPLACE VIEW obsolete_instance_node AS SELECT * FROM instance_node WHERE is_obsolete='t';
COMMENT ON VIEW obsolete_instance_node IS 'An instance node that is obsolete';

CREATE OR REPLACE VIEW obsolete_relation_node AS SELECT * FROM relation_node WHERE is_obsolete='t';
COMMENT ON VIEW obsolete_relation_node IS 'A relation node that is obsolete';

CREATE OR REPLACE VIEW valid_node AS SELECT * FROM node WHERE is_obsolete='f';
COMMENT ON VIEW valid_node IS 'A graph node that contains current data; not obsolete';

CREATE OR REPLACE VIEW valid_class_node AS SELECT * FROM class_node WHERE is_obsolete='f';
COMMENT ON VIEW valid_class_node IS 'A class node that is current; not obsolete';

CREATE OR REPLACE VIEW valid_instance_node AS SELECT * FROM instance_node WHERE is_obsolete='f';
COMMENT ON VIEW valid_instance_node IS 'An instance node that is current; not obsolete';

CREATE OR REPLACE VIEW valid_relation_node AS SELECT * FROM relation_node WHERE is_obsolete='f';
COMMENT ON VIEW valid_relation_node IS 'A relation  node that is current; not obsolete';


-- %% link %%

CREATE OR REPLACE VIEW revlink AS 
 SELECT
  link_id,
  reiflink_node_id,
  object_id     AS node_id,
  predicate_id,
  node_id       AS object_id,
  when_id,
  is_inferred,
  is_negated,
  combinator,
  source_id
 FROM link;
COMMENT ON VIEW revlink IS 'A link in the reverse direction. Example_of_use:
SELECT * FROM node INNER JOIN revlink USING (node_id). May be useful
for inverse DAG traversal. Has no real-world semantics';

CREATE OR REPLACE VIEW inferred_link AS SELECT * FROM link WHERE is_inferred='t';
COMMENT ON VIEW inferred_link IS 'A link that has been inferred;
inferred links are created by deductive reasoning. Examples: X part_of
Z because X is_a Y and Y part_of Z';

CREATE OR REPLACE VIEW inferred_irreflexive_link AS SELECT * FROM inferred_link WHERE node_id != object_id;

CREATE OR REPLACE VIEW asserted_link AS SELECT * FROM link WHERE is_inferred='f';
COMMENT ON VIEW asserted_link IS 'A link that has been asserted; converse of inferred_link';

CREATE OR REPLACE VIEW intersection_link AS SELECT * FROM link WHERE combinator='I';
COMMENT ON VIEW intersection_link IS 'A link that forms part of an
intersection equivalence declaration. All intersection_links for a
node N are combined together, and the combination of links defines
N. Examples: the node representing "nucleus of astrocyte" may be
formed of an intersection_link to nucleus and an intersection_link to
part_of astrocyte';

CREATE OR REPLACE VIEW union_link AS SELECT * FROM link WHERE combinator='U';
COMMENT ON VIEW union_link IS 'A link that forms part of an
union equivalence declaration. All union_links for a node N are
combined together, and the combination of links defines N. Examples:
the node representing "thymine or guanine" may be formed of a
union_link to guanine and an union_link to thymine';


CREATE OR REPLACE VIEW standard_link AS SELECT * FROM link WHERE combinator='';
COMMENT ON VIEW standard_link IS 'A link without union or intersection conditions';

--CREATE OR REPLACE VIEW nc_link AS SELECT * FROM link WHERE combinator='';
--COMMENT ON VIEW nc_link IS 'A link stating a condition that is
--necessary for the node. These are "normal" DAG links, such as cell
--nucleus part_of cell (which is necessarily true for all cell nuclei)';

--CREATE OR REPLACE VIEW inheritable_link AS SELECT * FROM link WHERE combinator!='U' AND is_metadata='f'; -- AND is_negated='f';
CREATE OR REPLACE VIEW inheritable_link AS SELECT link.* FROM link WHERE 
link.combinator!='U' AND link.is_metadata='f' AND link.is_negated='f' AND 
link.reiflink_node_id IS NULL;

CREATE OR REPLACE VIEW reified_link AS SELECT * FROM link WHERE reiflink_node_id IS NOT NULL;
COMMENT ON VIEW reified_link IS 'A link that has a link pointing to it';

-- instance_of
CREATE OR REPLACE VIEW instance_of_relation AS SELECT * FROM relation_node WHERE uid='OBO_REL:instance_of';
COMMENT ON VIEW instance_of_relation IS 'A relation node for the OBO_REL relation "instance_of"';

CREATE OR REPLACE VIEW instantiation_link AS
 SELECT link.* 
 FROM link INNER JOIN instance_of_relation ON (predicate_id=instance_of_relation.node_id);
COMMENT ON VIEW instantiation_link IS 'A link of type "instance_of" between an instance and a class';
-- DUPLICATE; TODO: dereprecate one

CREATE OR REPLACE VIEW instance_of_link AS
 SELECT link.*
 FROM link INNER JOIN instance_of_relation ON (predicate_id=instance_of_relation.node_id)
 WHERE combinator='';

CREATE OR REPLACE VIEW asserted_instance_of_link AS
 SELECT link.*
 FROM link INNER JOIN instance_of_relation ON (predicate_id=instance_of_relation.node_id)
 WHERE combinator='' AND is_inferred='f';

CREATE OR REPLACE VIEW asserted_instantiation_link AS SELECT * FROM instantiation_link WHERE is_inferred='f';
COMMENT ON VIEW asserted_instantiation_link IS 'An instantiation link that is asserted (not implied/inferred)';

CREATE OR REPLACE VIEW implied_instantiation_link AS SELECT * FROM instantiation_link WHERE is_inferred='t';
COMMENT ON VIEW implied_instantiation_link IS 'An instantiation link that is inferred (not asserted directly)';

-- is_a
CREATE OR REPLACE VIEW is_a_relation AS SELECT * FROM relation_node WHERE uid='OBO_REL:is_a';
COMMENT ON VIEW is_a_relation IS 'relation_node for the OBO_REL relation "is_a"';

CREATE OR REPLACE VIEW non_is_a_relation AS SELECT * FROM relation_node WHERE uid!='OBO_REL:is_a';
COMMENT ON VIEW non_is_a_relation IS 'relation_node NOT corresponding to the OBO_REL relation "is_a"';


CREATE OR REPLACE VIEW is_a_link AS
 SELECT link.*
 FROM link INNER JOIN is_a_relation ON (predicate_id=is_a_relation.node_id);

COMMENT ON VIEW is_a_link IS 'A link of type "is_a"';

-- BEGIN MATERIALIZE
-- SELECT create_matview('is_a_link');
-- CREATE INDEX is_a_link_idx_node ON is_a_link(node_id);
-- CREATE INDEX is_a_link_idx_object ON is_a_link(object_id);
-- CREATE INDEX is_a_link_idx_node_object ON is_a_link(node_id,object_id);
-- END MATERIALIZE

CREATE OR REPLACE VIEW subrelation_link AS
 SELECT link.*
 FROM link INNER JOIN is_a_relation ON (link.predicate_id=is_a_relation.node_id)
  WHERE EXISTS (SELECT node.node_id FROM node WHERE node.metatype='R' AND node.node_id=link.node_id)
   AND link.node_id != link.object_id
   AND link.object_id NOT IN (SELECT node_id FROM node WHERE uid='OBO_REL:relationship');

COMMENT ON VIEW subrelation_link IS 'A subrelation-link between two relation nodes. Example regional_part_of and part_of. Includes inferred sub-relations but not reflexive ones';

-- BEGIN MATERIALIZE
-- SELECT create_matview('subrelation_link');
-- CREATE INDEX subrelation_link_idx_node ON subrelation_link(node_id);
-- CREATE INDEX subrelation_link_idx_object ON subrelation_link(object_id);
-- CREATE INDEX subrelation_link_idx_node_object ON subrelation_link(node_id,object_id);
-- END MATERIALIZE



CREATE OR REPLACE VIEW is_a_basic_link AS
 SELECT link.*
 FROM link INNER JOIN is_a_relation ON (predicate_id=is_a_relation.node_id)
 WHERE combinator='';

COMMENT ON VIEW is_a_basic_link IS 'A link of type "is_a" that is not a combinator/union';


CREATE OR REPLACE VIEW non_is_a_link AS
 SELECT link.*
 FROM link
 WHERE predicate_id NOT IN (SELECT node_id FROM is_a_relation);
-- WHERE NOT EXISTS (SELECT * FROM is_a_relation AS i WHERE i.node_id=link.predicate_id);

COMMENT ON VIEW non_is_a_link IS 'A link of type that is not "is_a"';

-- BEGIN MATERIALIZE
-- SELECT create_matview('non_is_a_link');
-- CREATE INDEX non_is_a_link_idx_node ON non_is_a_link(node_id);
-- CREATE INDEX non_is_a_link_idx_object ON non_is_a_link(object_id);
-- CREATE INDEX non_is_a_link_idx_node_object ON non_is_a_link(node_id,object_id);
-- END MATERIALIZE


CREATE OR REPLACE VIEW non_is_a_basic_link AS
 SELECT link.*
 FROM link
 WHERE combinator=''
  AND predicate_id NOT IN (SELECT node_id FROM is_a_relation);
COMMENT ON VIEW non_is_a_basic_link IS 'A link of type that is not "is_a", and not a combinator/union';

CREATE OR REPLACE VIEW non_is_a_basic_class_link AS
 SELECT link.*
 FROM non_is_a_basic_link AS link
  INNER JOIN node ON (link.node_id=node.node_id)
  INNER JOIN node AS o ON (link.object_id=o.node_id)
 WHERE node.metatype='C' AND o.metatype='C';

CREATE OR REPLACE VIEW asserted_is_a_link AS SELECT * FROM is_a_link WHERE is_inferred='f';
COMMENT ON VIEW asserted_is_a_link IS 'An is_a_link that is not inferred (ie it is asserted)';

CREATE OR REPLACE VIEW implied_is_a_link AS SELECT * FROM is_a_link WHERE is_inferred='t';
COMMENT ON VIEW implied_is_a_link IS 'An is_a_link that is inferred (ie it is not directly asserted)';

-- non-generic link, but still useful
CREATE OR REPLACE VIEW in_organism_relation AS SELECT * FROM relation_node WHERE uid='OBO_REL:in_organism';
COMMENT ON VIEW in_organism_relation IS 'relation_node for the OBO_REL relation "in_organism"';

CREATE OR REPLACE VIEW in_organism_link AS
 SELECT link.*
 FROM link INNER JOIN in_organism_relation ON (predicate_id=in_organism_relation.node_id);

COMMENT ON VIEW in_organism_link IS 'A link of type "in_organism"';

CREATE OR REPLACE VIEW axis_pair_link AS
 SELECT
  i.object_id AS is_a_node_id,
  i.is_inferred AS is_a_is_inferred,
  i.link_id AS is_a_link_id,
  i.combinator AS is_a_combinator,
  x.*
 FROM
  is_a_link AS i
  INNER JOIN non_is_a_link AS x USING (node_id);

-- **
-- SELECT create_matview('axis_pair_link');
-- CREATE INDEX axis_pair_link_idx_node_id ON axis_pair_link(node_id);
-- CREATE INDEX axis_pair_link_idx_node_object_id ON axis_pair_link(node_id,object_id);
-- CREATE INDEX axis_pair_link_idx_node_isa_object_id ON axis_pair_link(node_id,is_a_node_id,object_id);
-- **

-- todo: generalise to any irreflexive relation
CREATE OR REPLACE VIEW implied_equivalent_class_link AS 
 SELECT 
  x.node_id AS node1_id,
  y.node_id AS node2_id
 FROM is_a_link AS x INNER JOIN is_a_link AS y ON (x.object_id=y.node_id AND y.object_id=x.node_id)
 WHERE x.node_id != x.object_id;


CREATE OR REPLACE VIEW genus_link AS
 SELECT link.*
 FROM link INNER JOIN is_a_relation ON (predicate_id=is_a_relation.node_id)
 WHERE combinator='I';
COMMENT ON VIEW genus_link IS 'A link between a class_node and its
genus_node (a component of a logical definition). The genus link is an
is_a link to the parent class that is refined by some differentia. A
logical def can be written as "a C is_a G that D", where C is the
class being defined, G is the genus, and D are the discriminating
features (differentia). Genus-diff definitions are stored as
intersection links; the genus is any link that goes to an is_a
parent. Good genus-differentia definitions have a single genus and 1
or more differentia, although if an intersection consists of >1 is_a
link then all will be treated as genus';

CREATE OR REPLACE VIEW differentium_link AS
 SELECT link.*
 FROM link
 WHERE combinator='I'
  AND predicate_id NOT IN (SELECT node_id FROM is_a_relation);
COMMENT ON VIEW differentium_link IS 'A link between a class_node and its
differentium (a component of a logical definition). The differentia
are links to other classes or nodes that serve to differentiae this
class from its genus';

CREATE OR REPLACE VIEW uniq_differentium_link AS
 SELECT DISTINCT
  node_id,
  predicate_id,
  object_id
 FROM differentium_link;



CREATE OR REPLACE VIEW genus_link_to_node AS
 SELECT genus_link.*,
        obj.uid AS object_uid,
        obj.label AS object_label,
        obj.source_id AS object_source_id,
        obj.metatype AS object_metatype
 FROM genus_link
  INNER JOIN node AS obj ON (genus_link.object_id=obj.node_id);
COMMENT ON VIEW genus_link_to_node IS 'A genus_link joined to the corresponding genus class_node';

CREATE OR REPLACE VIEW differentium_link_to_node AS
 SELECT uniq_differentium_link.*,
        obj.uid AS object_uid,
        obj.label AS object_label,
        obj.source_id AS object_source_id,
        obj.metatype AS object_metatype
 FROM uniq_differentium_link
  INNER JOIN node AS obj ON (uniq_differentium_link.object_id=obj.node_id);
COMMENT ON VIEW differentium_link_to_node IS 'A differentium_link joined to the
corresponding differentia relatum class_node. Example: if the class is
defined as "a nucleus that is part_of an astrocyte", there would be a
differentium_link_to_node that joined both the differentium_link
(part_of) and the relatum class node (nucleus)';

CREATE OR REPLACE VIEW differentium_link_to_label AS
 SELECT 
  dl.*,
  pred.uid AS pred_uid,
  pred.label AS pred_label,
  pred.label || ' ' || dl.object_label AS differentium_label
 FROM 
  differentium_link_to_node AS dl
  INNER JOIN node AS pred ON (dl.predicate_id=pred.node_id);
COMMENT ON VIEW differentium_link_to_label IS 'A differentium_link
with a human-readable label for the link; substitutes the ID of the
predicate and the ID of the object/relatum with their labels. Example:
"part_of nucleus"';

-- TODO: change names, these are misleading..
-- useful for populating xps
-- e.g. given some E instances and some Q instances, with
-- instance-level inheres_in relations we make find all 
-- EQ combinations
CREATE OR REPLACE VIEW some_some_relation_by_asserted_instance_links AS 
 SELECT DISTINCT
  link.predicate_id,
  nt.object_id AS node_class_id,
  obt.object_id AS object_class_id
 FROM link 
  INNER JOIN asserted_instance_of_link AS nt ON (link.node_id=nt.node_id)
  INNER JOIN asserted_instance_of_link AS obt ON (link.object_id=obt.node_id);

-- ??
CREATE OR REPLACE VIEW some_some_relation_by_asserted_instances AS 
 SELECT DISTINCT
  link.predicate_id,
  nt.object_id AS node_class_id,
  obt.object_id AS object_class_id
 FROM link 
  INNER JOIN instance_of_link AS nt ON (link.node_id=nt.node_id)
  INNER JOIN instance_of_link AS obt ON (link.object_id=obt.node_id)
 WHERE
  EXISTS (SELECT * FROM asserted_instance_of_link AS c1 WHERE c1.object_id=nt.object_id)
 AND
  EXISTS (SELECT * FROM asserted_instance_of_link AS c2 WHERE c2.object_id=obt.object_id);


CREATE OR REPLACE VIEW some_some_relation_by_instance_links AS 
 SELECT DISTINCT
  link.predicate_id,
  nt.object_id AS node_class_id,
  obt.object_id AS object_class_id,
  nt.is_inferred AS is_inferred_node,
  obt.is_inferred AS is_inferred_object
 FROM link 
  INNER JOIN instance_of_link AS nt ON (link.node_id=nt.node_id)
  INNER JOIN instance_of_link AS obt ON (link.object_id=obt.node_id);


-- ************************************************************
-- CONSISTENCY CHECKING VIEWS
-- ************************************************************
-- CREATE SCHEMA obd_consistency_view;
-- SET search_path TO obd_consistency_view,obd_core_view,public;

CREATE OR REPLACE VIEW consecutive_link_pair AS
 SELECT
  link1.*,
  link1.object_id AS via_node_id,
  link2.predicate_id AS next_predicate_id,
  link2.object_id AS next_object_id,
  link2.is_inferred AS next_is_inferred
 FROM
  link AS link1
  INNER JOIN link AS link2 ON (link1.object_id=link2.node_id)
 WHERE
  link1.link_id != link2.link_id;

COMMENT ON VIEW consecutive_link_pair IS 'A pair of links in which the
object of one matches the node of another. For example, X is_a Y + Y
part_of Z. May include reflexive links';

CREATE OR REPLACE VIEW sibling_link_pair AS
 SELECT
  link1.*,
  link1.node_id AS sibling_node_id,
  link2.predicate_id AS sibling_predicate_id,
  link2.is_inferred AS sibling_is_inferred
 FROM
  link AS link1
  INNER JOIN link AS link2 ON (link1.object_id=link2.object_id)
 WHERE
  link1.link_id != link2.link_id;

COMMENT ON VIEW sibling_link_pair IS 'A pair of links that share
the same object_id';

CREATE OR REPLACE VIEW parent_link_pair AS
 SELECT
  link1.*,
  link2.predicate_id AS parent_predicate_id,
  link1.object_id AS parent_object_id,
  link2.is_inferred AS parent_is_inferred
 FROM
  link AS link1
  INNER JOIN link AS link2 ON (link1.node_id=link2.node_id)
 WHERE
  link1.link_id != link2.link_id;

COMMENT ON VIEW parent_link_pair IS 'A pair of links that share
the same node_id';


CREATE OR REPLACE VIEW cyclic_link_pair AS
 SELECT
  *
 FROM
  consecutive_link_pair
 WHERE
  node_id = next_object_id;

COMMENT ON VIEW cyclic_link_pair IS 'A consecutive_link_pair in which
the main link node equals the succeeding link object';

 
CREATE OR REPLACE VIEW cyclic_link_pair_with_nodes AS
 SELECT
  consecutive_link_pair.*,
  node.uid                      AS node_uid,
  node.label                    AS node_label,
  node.source_id                AS node_source_id,
  via_node.uid                  AS via_node_uid,
  via_node.label                AS via_node_label,
  via_node.source_id            AS via_node_source_id,
  object_node.uid               AS object_node_uid,
  object_node.label             AS object_node_label,
  object_node.source_id         AS object_node_source_id
 FROM
  consecutive_link_pair
  INNER JOIN node ON (consecutive_link_pair.node_id=node.node_id)
  INNER JOIN node AS via_node ON  (consecutive_link_pair.via_node_id=via_node.node_id)
  INNER JOIN node AS object_node ON  (consecutive_link_pair.next_object_id=object_node.node_id)
 WHERE
  node.node_id = next_object_id;

COMMENT ON VIEW cyclic_link_pair IS 'A cyclic_link_pair with node
information; a convenience view';

-- ************************************************************
-- GRAPH TOPOLOGY VIEWS
-- ************************************************************
-- CREATE SCHEMA obd_graph_topology_view;
-- SET search_path TO obd_graph_topology_view,obd_core_view,public;

CREATE OR REPLACE VIEW graph_root_node AS
 SELECT
  *
 FROM
  node
 WHERE NOT EXISTS (SELECT * FROM link WHERE link.node_id=node.node_id);

COMMENT ON VIEW graph_root_node IS 'A node that has no links from it';

CREATE OR REPLACE VIEW graph_root_class_node AS 
 SELECT * FROM graph_root_node WHERE metatype='C';

COMMENT ON VIEW graph_root_class_node IS 'A class_node that has no
links from it';

CREATE OR REPLACE VIEW graph_root_relation_node AS 
 SELECT * FROM graph_root_node WHERE metatype='R';

COMMENT ON VIEW graph_root_relation_node IS 'A relation_node that has no
links from it';

CREATE OR REPLACE VIEW graph_root_instance_node AS 
 SELECT * FROM graph_root_node WHERE metatype='I';

COMMENT ON VIEW graph_root_instance_node IS 'A instance_node that has no
links from it';

CREATE OR REPLACE VIEW graph_leaf_node AS
 SELECT
  *
 FROM
  node
 WHERE NOT EXISTS (SELECT * FROM link WHERE link.object_id=node.node_id);

COMMENT ON VIEW graph_leaf_node IS 'A node that has no links to it';

CREATE OR REPLACE VIEW graph_leaf_class_node AS 
 SELECT * FROM graph_leaf_node WHERE metatype='C';

COMMENT ON VIEW graph_lead_class_node IS 'A class_node that has no
links to it';

CREATE OR REPLACE VIEW graph_leaf_relation_node AS 
 SELECT * FROM graph_leaf_node WHERE metatype='R';

COMMENT ON VIEW graph_lead_class_node IS 'A relation_node that has no
links to it';

CREATE OR REPLACE VIEW graph_leaf_instance_node AS 
 SELECT * FROM graph_leaf_node WHERE metatype='I';

COMMENT ON VIEW graph_lead_instance_node IS 'An instance_node that has no
links to it';

CREATE OR REPLACE VIEW graph_root_id_by_relation AS
 SELECT DISTINCT
  nodepred.node_id,
  nodepred.predicate_id,
  nodepred.source_id AS link_source_id
 FROM
  link AS nodepred
  LEFT OUTER JOIN link ON (nodepred.node_id=link.object_id)
 WHERE link.node_id IS NULL;

COMMENT ON VIEW graph_root_id_by_relation IS 'A node that has no links
from it of a certain edge relation type. NOTE: **this view is
currently too slow** materialize??';

CREATE OR REPLACE VIEW asserted_ancestor_link AS
 SELECT
  inf_link.node_id AS query_node_id,
  inf_link.predicate_id AS query_predicate_id,
  asserted_link.*
 FROM
  link AS inf_link
  INNER JOIN link AS asserted_link ON (inf_link.object_id=asserted_link.node_id)
 WHERE
  asserted_link.is_inferred='f';

-- Example: select node_label(node_id) AS node_label,node_uid(predicate_id) AS rel,node_label(object_id) AS obj_label from asserted_ancestor_link where query_node_id = 11695 and predicate_id=7159

COMMENT ON VIEW asserted_ancestor_link IS 'An asserted link that can
be reached via closure from query_node_id. Can also be restricted such
that predicate_id is predicate of interest (R). In this case
query_predicate_id should equal is_a OR R - there should always be a
reflexive is_a relation realized for every node';


CREATE OR REPLACE VIEW asserted_ancestor_link_plus AS
 SELECT
  aal.*,
  ll.predicate_id AS ext_predicate_id,
  ll.object_id AS ext_object_id
 FROM
  asserted_ancestor_link AS aal
  INNER JOIN link AS ll ON (aal.object_id=ll.node_id);

CREATE OR REPLACE VIEW asserted_ancestor_link_minus AS
 SELECT
  aal.*,
  ll.predicate_id AS ext_predicate_id,
  ll.object_id AS ext_node_id
 FROM
  asserted_ancestor_link AS aal
  INNER JOIN is_a_link AS ll ON (aal.object_id=ll.object_id);

-- non-redundant version. slow!
CREATE OR REPLACE VIEW asserted_ancestor_link_nr AS
 SELECT
  aal.*
 FROM
  asserted_ancestor_link_minus AS aal
  LEFT JOIN link AS rl ON (rl.node_id=aal.node_id AND rl.predicate_id=aal.predicate_id AND rl.object_id=aal.ext_node_id AND rl.object_id!=aal.object_id)
 WHERE rl.link_id IS NULL;
  


-- ************************************************************
-- Joins
-- ************************************************************
-- CREATE SCHEMA obd_prejoins_view;
-- SET search_path TO obd_prejoins_view,obd_core_view,public;

CREATE OR REPLACE VIEW relation_use_by_source AS
 SELECT DISTINCT
  predicate_id, source_id
 FROM link;

-- %% node * link %%

CREATE OR REPLACE VIEW node_link AS
 SELECT
        link.*,
        node.uid AS node_uid,
        node.label AS node_label,
        node.source_id AS node_source_id,
        node.metatype AS node_metatype,
        node.is_anonymous AS node_is_anonymous
 FROM link INNER JOIN node USING (node_id);
COMMENT ON VIEW node_link IS 'Link joined to node';

CREATE OR REPLACE VIEW instance_link AS
 SELECT * FROM node_link WHERE node_metatype='I';
COMMENT ON VIEW instance_link IS 'Link between an instance_node and some
other node. Examples: a link between two instances; a link between an
instance and a class';

CREATE OR REPLACE VIEW class_link AS
 SELECT * FROM node_link WHERE node_metatype='C';
COMMENT ON VIEW class_link IS 'Link between an class_node and some
other node. Examples: a class-class link (nucleus part_of cell); a
class-instance link (eg metadata)';

CREATE OR REPLACE VIEW relation_link AS
 SELECT * FROM node_link WHERE node_metatype='R';
COMMENT ON VIEW relation_link IS 'Link between an relation_node and some
other node. Examples: a relation to a super-relation';

-- %% link * node %%

CREATE OR REPLACE VIEW link_to_node AS
 SELECT
        node_link.*,
        obj.uid AS object_uid,
        obj.label AS object_label,
        obj.source_id AS object_source_id,
        obj.metatype AS object_metatype
 FROM node_link INNER JOIN node AS obj ON (node_link.object_id=obj.node_id);

COMMENT ON VIEW link_to_node IS 'Link joined to node via
link.object_id. Join: link * object.node. Example: querying this via the node_id for "Tenth
thoracic vertebra", then the query will return link_to_node rows for
"is_a Thoracic vertebrae" and "has_part Bony part of eleventh thoracic
vertebra"';

CREATE OR REPLACE VIEW link_to_instance AS
 SELECT * FROM link_to_node WHERE object_metatype='I';

COMMENT ON VIEW link_to_instance IS 'a link_to_node whete the node is
an instance';

CREATE OR REPLACE VIEW link_to_class AS
 SELECT * FROM link_to_node WHERE object_metatype='C';

COMMENT ON VIEW link_to_class IS 'a link_to_node whete the node is
a class';

CREATE OR REPLACE VIEW link_to_relation AS
 SELECT * FROM link_to_node WHERE object_metatype='R';

COMMENT ON VIEW link_to_relation IS 'a link_to_node whete the node is
a relation';


-- %% node * link * node (triple) %%

CREATE OR REPLACE VIEW node_link_node AS
 SELECT
        node_link.*,
        obj.uid AS object_uid,
        obj.label AS object_label,
        obj.source_id AS object_source_id,
        obj.metatype AS object_metatype,
        obj.is_anonymous AS object_is_anonymous
 FROM node_link INNER JOIN node AS obj ON (node_link.object_id=obj.node_id);

COMMENT ON VIEW node_link_node IS 'A join between two nodes via a
link. Join: link * node * object.node. This is a convenience view to avoid having to explicitly make
the join. Example: SELECT * FROM node_link_node WHERE
node_label="apoptosis"';



CREATE OR REPLACE VIEW instance_link_instance AS
 SELECT * FROM node_link_node
 WHERE node_metatype='I' AND object_metatype='I';

CREATE OR REPLACE VIEW instance_link_class AS
 SELECT * FROM node_link_node
 WHERE node_metatype='I' AND object_metatype='C';

CREATE OR REPLACE VIEW class_link_instance AS
 SELECT * FROM node_link_node
 WHERE node_metatype='C' AND object_metatype='I';

CREATE OR REPLACE VIEW class_link_class AS
 SELECT * FROM node_link_node
 WHERE node_metatype='C' AND object_metatype='C';

CREATE OR REPLACE VIEW link_with_pred AS
 SELECT link.*,
        pred.uid AS pred_uid,
        pred.label AS pred_label,
        pred.source_id AS pred_source_id,
        pred.metatype AS pred_metatype
 FROM link INNER JOIN node AS pred ON (predicate_id=pred.node_id);

COMMENT ON VIEW link_with_pred IS 'A join between a link and a
predicate node. link * predicate.node. Convenience view to avoid
having to explicitly make the join';

CREATE OR REPLACE VIEW link_with_pred_uid AS
 SELECT link.*,
        pred.uid AS pred_uid
 FROM link INNER JOIN node AS pred ON (predicate_id=pred.node_id);

CREATE OR REPLACE VIEW node_link_node_with_pred AS
 SELECT node_link_node.*,
        pred.uid AS pred_uid,
        pred.label AS pred_label,
        pred.source_id AS pred_source_id,
        pred.metatype AS pred_metatype
 FROM node_link_node INNER JOIN node AS pred ON (predicate_id=pred.node_id);

COMMENT ON VIEW node_link_node_with_pred IS 'A join between a subject
node, an object node and a predicate node via a link. Join: link *
node * object.node * predicate.node. Convenience view';

CREATE OR REPLACE VIEW node_link_node_with_pred_and_source AS
 SELECT link.*,
        source.uid AS source_uid,
        source.label AS source_label
 FROM node_link_node_with_pred AS link LEFT OUTER JOIN node AS source ON (link.source_id=source.node_id);


CREATE OR REPLACE VIEW node_with_source AS
 SELECT node.*,
        source.uid AS source_uid,
        source.label AS source_label
 FROM node LEFT OUTER JOIN node AS source ON (node.source_id=source.node_id);

CREATE OR REPLACE VIEW description_with_type AS
 SELECT
  description.*,
  type.uid AS type_uid,
  type.label AS type_label
 FROM
  description
  INNER JOIN node AS type ON (description.type_id=type.node_id);

CREATE OR REPLACE VIEW description_d AS
 SELECT descr.*,
        node.uid AS node_uid,
        node.label AS node_label
 FROM node 
  INNER JOIN description_with_type AS descr USING (node_id);

CREATE OR REPLACE VIEW alias_with_type AS
 SELECT
  alias.*,
  type.uid AS type_uid,
  type.label AS type_label
 FROM
  alias
  LEFT OUTER JOIN node AS type ON (alias.type_id=type.node_id);

CREATE OR REPLACE VIEW alias_d AS
 SELECT a.*,
        node.uid AS node_uid,
        node.label AS node_label
 FROM node 
  INNER JOIN alias_with_type AS a USING (node_id);

CREATE OR REPLACE VIEW tagval_with_type AS
 SELECT
  tagval.*,
  type.uid AS type_uid,
  type.label AS type_label
 FROM
  tagval
  LEFT OUTER JOIN node AS type ON (tagval.type_id=type.node_id);

CREATE OR REPLACE VIEW tagval_d AS
 SELECT a.*,
        node.uid AS node_uid,
        node.label AS node_label
 FROM node 
  INNER JOIN tagval_with_type AS a USING (node_id);

CREATE OR REPLACE VIEW node_subset_d AS
 SELECT *
 FROM node_link_node_with_pred
 WHERE pred_uid='oboInOwl:inSubset';


-- ************************************************************
-- OBO METAMODEL
-- ************************************************************
-- CREATE SCHEMA obd_obo_metamodel_view;
-- SET search_path TO obd_obo_metamodel_view,obd_prejoins_view,obd_core_view,public;

CREATE OR REPLACE VIEW in_subset_link AS
 SELECT
  link.link_id,
  link.node_id          AS node_id,
  link.object_id        AS subset_node_id
 FROM
  link_with_pred AS link
 WHERE
  pred_uid='oboInOwl:inSubset'
  AND is_inferred='f'; -- should not be necessary

COMMENT ON VIEW in_subset_link IS 'holds if node_id belongs to the subset_node_id';

CREATE OR REPLACE VIEW xref_link AS
 SELECT
  link.link_id,
  link.node_id          AS node_id,
  link.object_id        AS xref_id
 FROM
  link_with_pred AS link
 WHERE
  pred_uid='oboInOwl:hasDbXref';

CREATE OR REPLACE VIEW node_label AS
 SELECT
  node_id,
  label
 FROM
  node
 UNION
 SELECT
  node_id,
  label
 FROM
  alias;

CREATE OR REPLACE VIEW node_literal AS
 SELECT
  node_id,
  type_id AS predicate_id,
  label AS val
 FROM
  alias
 UNION
 SELECT
  node_id,
  type_id AS predicate_id,
  label AS val
 FROM
  description
 UNION
 SELECT
  node_id,
  tag_id AS predicate_id,
  val
 FROM
  tagval;

CREATE OR REPLACE VIEW node_literal_with_pred AS
 SELECT
  node_id,
  type_id AS predicate_id,
  'onoInOwl:hasSynonym' AS predicate_uid,
  label AS val
 FROM
  alias
 UNION
 SELECT
  node_id,
  type_id AS predicate_id,
  'oboInOwl:hasDefinition' AS predicate_uid,
  label AS val
 FROM
  description
 UNION
 SELECT
  node_id,
  tag_id AS predicate_id,
  pred.uid AS predicate_uid,
  val
 FROM
  tagval INNER JOIN node AS pred USING(node_id);

-- ************************************************************
-- ANNOTATION MODEL
-- ************************************************************
-- CREATE SCHEMA obd_annotation_view;
-- SET search_path TO obd_annotation_view,obd_prejoins_view, obd_obo_metamodel_view,obd_core_view,public;

-- reification is not propagated so we use this
-- NOTE: currently propagates over ALL relations.. TODO fix
CREATE OR REPLACE VIEW implied_annotation_link AS
  SELECT DISTINCT
   alink.link_id,
   alink.node_id,
   alink.predicate_id,
   alink.object_id AS asserted_object_id,
   alink.source_id,
   ilink.predicate_id AS chaining_predicate_id,
   ilink.is_inferred,
   ilink.object_id
  FROM
   reified_link AS alink
   INNER JOIN link AS ilink ON (alink.object_id=ilink.node_id)
  WHERE
   ilink.is_metadata='f';
--  WHERE
--   alink.is_inferred='f'; -- non-asserted annotation 

COMMENT ON VIEW implied_annotation_link IS 'A link between an annotated
entity and the closure of the node used to annotate it. The annotated
entity (node_id) is asserted to hold some relation to
asserted_object_id, which in turn holds some relation to object_id
(possibly implied). For example
(pmid123,describes,Eye,develops_from,ectoderm) or
(proteinA,localised_to,nucleolus,part_of,cell)';

-- BEGIN MATERIALIZE
-- SELECT create_matview('implied_annotation_link');
-- CREATE INDEX implied_annotation_link_idx_node_id ON implied_annotation_link(node_id);
-- CREATE INDEX implied_annotation_link_idx_object_id ON implied_annotation_link(object_id);
-- CREATE INDEX implied_annotation_link_idx_node_object_id ON implied_annotation_link(node_id,object_id);
-- CREATE INDEX implied_annotation_link_idx_source_id ON implied_annotation_link(source_id);
-- CREATE INDEX implied_annotation_link_idx_node_object_source_id ON implied_annotation_link(node_id,object_id,source_id);
-- END MATERIALIZE

CREATE OR REPLACE VIEW annotation_xp AS
  SELECT
   alink.link_id,
   alink.node_id,
   alink.predicate_id,
   alink.object_id AS asserted_object_id,
   alink.source_id,
   apl.predicate_id AS xp_predicate_id,
   apl.is_inferred,
   apl.object_id,
   apl.combinator
  FROM
   reified_link AS alink
   INNER JOIN intersection_link AS apl ON (alink.object_id=apl.node_id)
  WHERE
   apl.is_inferred='f';

COMMENT ON VIEW annotation_xp IS 'A link between an annotated entity
and the xp of the object directly annotated to. E.g. if g1 is
annotated to LeftEye-small then this would yield two rows g1-LeftEye,
g1-small';

-- SELECT create_matview('annotation_xp');
-- CREATE INDEX annotation_xp_idx_link_id ON annotation_xp(link_id);
-- CREATE INDEX annotation_xp_idx_node_id ON annotation_xp(node_id);
-- CREATE INDEX annotation_xp_idx_object_id ON annotation_xp(object_id);
-- CREATE INDEX annotation_xp_idx_node_object_id ON annotation_xp(node_id,object_id);
-- CREATE INDEX annotation_xp_idx_source_id ON annotation_xp(source_id);
-- CREATE INDEX annotation_xp_idx_node_object_source_id ON annotation_xp(node_id,object_id,source_id);
-- END MATERIALIZE


CREATE OR REPLACE VIEW implied_annotation_xp AS
  SELECT
   alink.link_id,
   alink.node_id,
   alink.predicate_id,
   alink.object_id AS asserted_object_id,
   alink.source_id,
   apl.predicate_id AS xp_predicate_id,
   apl.is_inferred,
   apl.object_id,
   apl.is_a_node_id,
   apl.is_a_is_inferred
  FROM
   reified_link AS alink
   INNER JOIN axis_pair_link AS apl ON (alink.object_id=apl.node_id);

COMMENT ON VIEW implied_annotation_xp IS 'A link between an annotated
entity and the implied pairwise product of the annotation. E.g. if g1
is annotated to LeftEye-small then the implied xp would be
{small,size,morphology} x {LeftEye,Eye,SenseOrgan,..}';

-- implied_annotation_xp too large to materialize

CREATE OR REPLACE VIEW count_of_annotated_entity AS
 SELECT 
  count(DISTINCT node_id) AS total
 FROM
  link -- used to be asserted...
 WHERE
  reiflink_node_id IS NOT NULL;

-- BEGIN MATERIALIZE
-- SELECT create_matview('count_of_annotated_entity');
-- END MATERIALIZE


CREATE OR REPLACE VIEW implied_annotation_link_count_by_object AS
 SELECT
  object_id AS node_id,
  COUNT(DISTINCT node_id) AS total
 FROM
  implied_annotation_link
 GROUP BY
  object_id;

COMMENT ON VIEW implied_annotation_link_count_by_object IS 'number of annotated entities annotated to a class node. E.g. ial(femur,100) means there are 100 genes or similar objects annotated to femur. TODO: rename? sounds like it is counting links..';

-- BEGIN MATERIALIZE
-- SELECT create_matview('implied_annotation_link_count_by_object');
-- CREATE INDEX implied_annotation_link_count_by_object_idx_node ON implied_annotation_link_count_by_object(node_id);
-- CREATE INDEX implied_annotation_link_count_by_object_idx_node_total ON implied_annotation_link_count_by_object(node_id,total);
-- END MATERIALIZE

CREATE OR REPLACE VIEW implied_annotation_link_count_by_node AS
 SELECT
  node_id AS node_id,
  COUNT(DISTINCT object_id) AS total
 FROM
  implied_annotation_link
 GROUP BY
  node_id;

COMMENT ON VIEW implied_annotation_link_count_by_node IS 'number of nodes (eg classes) used to annotate a node (eg gene)';

-- BEGIN MATERIALIZE
-- SELECT create_matview('implied_annotation_link_count_by_node');
-- CREATE INDEX implied_annotation_link_count_by_node_idx_node ON implied_annotation_link_count_by_node(node_id);
-- CREATE INDEX implied_annotation_link_count_by_node_idx_node_total ON implied_annotation_link_count_by_node(node_id,total);
-- END MATERIALIZE

CREATE OR REPLACE VIEW implied_annotation_link_with_total AS
 SELECT DISTINCT
  ial.*,
  ialc.total AS total -- total number of genes etc 
 FROM
  implied_annotation_link AS ial
  INNER JOIN implied_annotation_link_count_by_object AS ialc ON (ial.object_id=ialc.node_id);

COMMENT ON VIEW implied_annotation_link_with_total IS
'implied_annotation_link adorned with the total number of annotated entities annotated to the object_id class';
 
-- BEGIN MATERIALIZE
-- SELECT create_matview('implied_annotation_link_with_total');
-- CREATE INDEX implied_annotation_link_with_total_idx_node_object_total ON implied_annotation_link_with_total(node_id,object_id,total);
-- CREATE INDEX implied_annotation_link_with_total_idx_node_object ON implied_annotation_link_with_total(node_id,object_id);
-- CREATE INDEX implied_annotation_link_with_total_idx_object ON implied_annotation_link_with_total(object_id);
-- END MATERIALIZE

CREATE OR REPLACE VIEW implied_annotation_link_with_prob AS
 SELECT DISTINCT
  ialt.*,
  CAST(total AS FLOAT) / (SELECT total FROM count_of_annotated_entity) AS p
 FROM
  implied_annotation_link_with_total AS ialt;


COMMENT ON VIEW implied_annotation_link_with_prob IS
'implied_annotation_link adorned with p(c), where c is the object_id class.';
 
-- BEGIN MATERIALIZE
-- SELECT create_matview('implied_annotation_link_with_prob');
-- CREATE INDEX implied_annotation_link_with_total_idx_node_object_prob ON implied_annotation_link_with_prob(node_id,object_id,p);
-- CREATE INDEX implied_annotation_link_with_prob_idx_node_object ON implied_annotation_link_with_prob(node_id,object_id);
-- CREATE INDEX implied_annotation_link_with_prob_idx_object ON implied_annotation_link_with_prob(object_id);
-- END MATERIALIZE

CREATE OR REPLACE VIEW implied_annotation_link_with_object AS
 SELECT
  ial.*,
  n.uid AS object_uid,
  n.label AS object_label,
  n.source_id AS object_source_id
 FROM
  implied_annotation_link AS ial
  INNER JOIN node AS n ON (ial.object_id=n.node_id);


-- link where relation=posits; from annotation to statement
CREATE OR REPLACE VIEW posits_link AS
 SELECT pl.* 
 FROM link_with_pred_uid AS pl
 WHERE pred_uid='oban:posits';

COMMENT ON VIEW posits_link IS 'holds if node_id is a annotation node asserting reiflink_node_id';

CREATE OR REPLACE VIEW link_to_link AS
 SELECT 
        link_in.node_id         AS in_node_id,
        link_in.predicate_id    AS in_predicate_id,
        linked_link.*
 FROM link AS link_in INNER JOIN link AS linked_link ON (link_in.object_id = linked_link.reiflink_node_id);

-- instance of an annotation. TODO: treat all reiflinks as annotations?
CREATE OR REPLACE VIEW annotation_node AS
 SELECT * FROM node 
 WHERE node_id IN 
        (SELECT instance_of_link.node_id 
         FROM instance_of_link INNER JOIN node AS c ON (c.node_id=instance_of_link.object_id) 
         WHERE c.uid='oban:Annotation');

CREATE OR REPLACE VIEW annotated_entity AS
 SELECT * FROM node
 WHERE node_id IN (SELECT DISTINCT node_id FROM link WHERE reiflink_node_id IS NOT NULL);

CREATE OR REPLACE VIEW is_annotated_entity AS
 SELECT DISTINCT node_id FROM link WHERE reiflink_node_id IS NOT NULL;

-- example: select * from  annotation_count_by_annotated_entity where annotation_count > 20;
CREATE OR REPLACE VIEW annotation_count_by_annotated_entity AS
 SELECT
  ae.uid,
  ae.label,
  count(link.*) AS annotation_count
 FROM 
        link
        INNER JOIN node AS ae ON (link.node_id=ae.node_id)
 WHERE
  reiflink_node_id IS NOT NULL
 GROUP BY
  ae.uid,
  ae.label;

CREATE OR REPLACE VIEW annotation_count_by_annotated_entity_and_source AS
 SELECT
  source.uid   AS source_uid,
  source.label AS source_label,
  ae.uid,
  ae.label,
  count(link.*) AS annotation_count
 FROM 
        link
        INNER JOIN node AS source ON (link.source_id=source.node_id)
        INNER JOIN node AS ae ON (link.node_id=ae.node_id)
 WHERE
  reiflink_node_id IS NOT NULL
 GROUP BY
  source.uid,
  source.label,
  ae.uid,
  ae.label;



-- instance of an annotation and the statement posited in the annotation
CREATE OR REPLACE VIEW annotation_node_J_link AS
 SELECT
  n.*,
  link.node_id AS subject_id
 FROM annotation_node AS n
  INNER JOIN link ON (n.node_id=link.reiflink_node_id);
 

--CREATE OR REPLACE VIEW composite_annotation_node AS
-- SELECT * FROM annotation_node x y z;

-- an annotation is an entity that posits something
CREATE OR REPLACE VIEW posited_link AS
 SELECT link.*,
        pl.node_id AS annotation_node_id
 FROM posits_link AS pl
  INNER JOIN link ON (pl.object_id=link.reiflink_node_id);
 
CREATE OR REPLACE VIEW statement_node AS
 SELECT
  node.*,
  link.node_id AS subject_id,
  link.predicate_id,
  link.object_id,
  link.is_inferred,
  link.combinator
 FROM
  node
  INNER JOIN link ON (link.reiflink_node_id=node.node_id);

CREATE OR REPLACE VIEW node_j_source AS 
 SELECT node.*,source.label AS source_label, source.uid AS source_uid
 FROM node LEFT OUTER JOIN node AS source ON (node.source_id=source.node_id);


CREATE OR REPLACE VIEW link_J_predicate AS 
 SELECT link.*, 
        node.uid AS predicate_uid,
        node.label AS predicate_label,
        node.source_id AS predicate_source_id
 FROM link INNER JOIN node ON (predicate_id=node.node_id);

CREATE OR REPLACE VIEW annotation_evidence AS 
 SELECT 
        annotation_node.*,
        has_evidence.object_id  AS evidence_id,
        has_evidence.is_inferred
 FROM           annotation_node
  INNER JOIN    link_J_predicate AS has_evidence USING (node_id)
 WHERE
  has_evidence.predicate_uid='oban:has_evidence';

-- annotation aggregate queries

CREATE OR REPLACE VIEW annotated_entity_count_by_source AS 
 SELECT 
  source.uid,
  source.label, 
        count(annotated_entity.node_id) AS annotated_entity_count
 FROM annotated_entity LEFT OUTER JOIN node AS source ON (annotated_entity.source_id=source.node_id)
 GROUP BY source.uid,source.label;

CREATE OR REPLACE VIEW annotation_count_by_annotated_entity_source AS 
 SELECT source.uid                              AS source_uid,
        source.label                            AS source_label, 
        count(reified_link.link_id)             AS annotation_count,
        count(DISTINCT reified_link.object_id)  AS distinct_annotation_class_count
 FROM reified_link
  INNER JOIN node AS annotated_entity     ON (reified_link.node_id=annotated_entity.node_id)
  INNER JOIN node AS source               ON (annotated_entity.source_id=source.node_id)
 GROUP BY 
        source.uid,
        source.label;

CREATE OR REPLACE VIEW annotation_count_by_class AS 
 SELECT link.object_id AS node_id,
        count(reified_link.link_id) AS annotation_count
 FROM reified_link
      INNER JOIN link ON (reified_link.object_id=link.node_id)
 WHERE
  reified_link.is_inferred='f'
 GROUP BY link.object_id;

CREATE OR REPLACE VIEW implied_counts_by_class_and_annotated_entity_source AS 
 SELECT source.uid                              AS source_uid,
        source.label                            AS source_label, 
        alink.object_id                         AS node_id,
        count(alink.link_id)                    AS annotation_count,
        count(DISTINCT alink.node_id)           AS annotated_entity_count
 FROM implied_annotation_link AS alink
  INNER JOIN node AS annotated_entity     ON (alink.node_id=annotated_entity.node_id)
  INNER JOIN node AS source               ON (annotated_entity.source_id=source.node_id)
 GROUP BY 
        source.uid,
        source.label,
        alink.object_id;

CREATE OR REPLACE VIEW asserted_counts_by_class_and_annotated_entity_source AS 
 SELECT source.uid                              AS source_uid,
        source.label                            AS source_label, 
        alink.object_id                         AS node_id,
        count(alink.link_id)                    AS annotation_count,
        count(DISTINCT alink.node_id)           AS annotated_entity_count
 FROM reified_link AS alink
  INNER JOIN node AS annotated_entity     ON (alink.node_id=annotated_entity.node_id)
  INNER JOIN node AS source               ON (annotated_entity.source_id=source.node_id)
 GROUP BY 
        source.uid,
        source.label,
        alink.object_id;

-- move to statistical views?
CREATE OR REPLACE VIEW class_probability_by_annotated_entity_source_using_asserted AS
 SELECT 
  cbc.source_uid,
  cbc.annotated_entity_count AS annotated_entity_count_by_class,
  c.annotated_entity_count AS total_annotated_entities,
  cbc.annotated_entity_count / c.annotated_entity_count AS p
 FROM 
  asserted_counts_by_class_and_annotated_entity_source AS cbc
  INNER JOIN annotated_entity_count_by_source AS c USING (source_uid);

CREATE OR REPLACE VIEW class_probability_by_annotated_entity_source_using_implied AS
 SELECT 
  cbc.source_uid,
  cbc.annotated_entity_count AS annotated_entity_count_by_class,
  c.annotated_entity_count AS total_annotated_entities,
  cbc.annotated_entity_count / c.annotated_entity_count AS p
 FROM 
  implied_counts_by_class_and_annotated_entity_source AS cbc
  INNER JOIN annotated_entity_count_by_source AS c USING (source_uid);

CREATE OR REPLACE VIEW annotation_count_by_annotated_entity_and_annotation_source AS 
 SELECT annotated_entity.uid                              AS annotated_entity_uid,
        annotated_entity.label                            AS annotated_entity_label, 
        annotation_source.uid                   AS annotation_source_uid,
        annotation_source.label                 AS annotation_source_label, 
        count(reified_link.link_id)             AS annotation_count,
        count(DISTINCT reified_link.object_id)  AS distinct_annotation_class_count
 FROM reified_link
  INNER JOIN node AS annotated_entity     ON (reified_link.node_id=annotated_entity.node_id)
  INNER JOIN node AS annotation_source ON (reified_link.source_id=annotation_source.node_id)
 GROUP BY 
        annotated_entity.uid,
        annotated_entity.label,
        annotation_source.uid,                   
        annotation_source.label;

CREATE OR REPLACE VIEW annotation_count_by_source AS 
 SELECT source.uid,source.label, count(link.link_id) AS link_count
 FROM link LEFT OUTER JOIN node AS source ON (link.source_id=source.node_id)
 WHERE reiflink_node_id IS NOT NULL
 GROUP BY source.uid,source.label;

CREATE OR REPLACE VIEW annotation_count_by_annotated_entity AS 
 SELECT source.uid,source.label, count(link.link_id) AS link_count
 FROM link LEFT OUTER JOIN node AS source ON (link.source_id=source.node_id)
 WHERE reiflink_node_id IS NOT NULL
 GROUP BY source.uid,source.label;

COMMENT ON VIEW annotation_count_by_source IS 'Number of reified
links (annotations) grouped by source';

CREATE OR REPLACE VIEW annotation_count_by_annotation_source AS
 SELECT annotation_source.uid                   AS annotation_source_uid,
        annotation_source.label                 AS annotation_source_label, 
        count(reified_link.link_id)             AS annotation_count,
        count(DISTINCT reified_link.node_id)    AS annotated_entity_count
 FROM reified_link
  INNER JOIN node AS annotation_source ON (reified_link.source_id=annotation_source.node_id)
 GROUP BY 
        annotation_source.uid,                   
        annotation_source.label;

CREATE OR REPLACE VIEW annotation_count_by_annotation_source_and_class_source AS
 SELECT annotation_source.uid                   AS annotation_source_uid,
        annotation_source.label                 AS annotation_source_label, 
        annotation_class_source.uid             AS annotation_class_source_uid,
        annotation_class_source.label           AS annotation_class_source_label,
        count(reified_link.link_id)             AS annotation_count,
        count(DISTINCT reified_link.node_id)    AS annotated_entity_count
 FROM reified_link
  INNER JOIN node AS annotation_source ON (reified_link.source_id=annotation_source.node_id)
  INNER JOIN node AS annotation_class  ON (reified_link.object_id=annotation_class.node_id)
  INNER JOIN node AS annotation_class_source ON (annotation_class.source_id=annotation_class_source.node_id)
 GROUP BY 
        annotation_source.uid,                   
        annotation_source.label, 
        annotation_class_source.uid,
        annotation_class_source.label;

CREATE OR REPLACE VIEW subsumed_annotation_pair AS
 SELECT
  sl.link_id AS subsumed_link_id,
  sl.predicate_uid AS subsumed_link_predicate_id,
  al1.node_id AS a1_node_id,
  al2.node_id AS a2_node_id,
  al1.predicate_id AS a1_predicate_id,
  al2.predicate_id AS a2_predicate_id
  al1.object_id AS a1_object_id,
  al2.object_id AS a2_object_id,
  al1.reiflink_node_id AS a1_reiflink_node_id
  al2.reiflink_node_id AS a2_reiflink_node_id
 FROM
  al1.predicate_id AS a1_predicate_id,
  reif_link AS al1,
  reif_link AS al2,
  link AS sl
 WHERE
  al1.object_id=sl.node_id
  AND al2.object_id=sl.object_id
  AND al1.is_inferred='f'
  AND al2.is_inferred='f';


-- ************************************************************
-- AGGREGATE QUERIES
-- ************************************************************
-- CREATE SCHEMA obd_aggregate_view;
-- SET search_path TO obd_aggregate_view,obd_annotation_view,obd_prejoins_view, obd_obo_metamodel_view,obd_core_view,public;

CREATE OR REPLACE VIEW statement_count_by_source AS 
 SELECT source.uid,source.label, count(sn.node_id) AS statement_count
 FROM statement_node AS sn
  INNER JOIN node AS source ON (sn.source_id=source.node_id)
 GROUP BY source.uid,source.label;

CREATE OR REPLACE VIEW node_count_by_source AS 
 SELECT source.uid,source.label, count(node.node_id) AS node_count
 FROM node LEFT OUTER JOIN node AS source ON (node.source_id=source.node_id)
 GROUP BY source.uid,source.label;

CREATE OR REPLACE VIEW node_count_by_source_id AS 
 SELECT node.source_id, count(node.node_id) AS node_count
 FROM node
 GROUP BY node.source_id;

CREATE OR REPLACE VIEW link_count_by_source AS 
 SELECT source.uid,source.label, count(link.link_id) AS link_count
 FROM link LEFT OUTER JOIN node AS source ON (link.source_id=source.node_id)
 GROUP BY source.uid,source.label;

CREATE OR REPLACE VIEW link_count_by_source_id AS 
 SELECT link.source_id, count(link.link_id) AS link_count
 FROM link
 GROUP BY source_id;

CREATE OR REPLACE VIEW asserted_link_count_by_source_id AS 
 SELECT link.source_id, count(link.link_id) AS link_count
 FROM link
 WHERE is_inferred='f'
 GROUP BY source_id;

CREATE OR REPLACE VIEW reified_link_count_by_source_id AS 
 SELECT link.source_id, count(link.link_id) AS link_count
 FROM link
 WHERE reiflink_node_id IS NOT NULL
 GROUP BY source_id;


CREATE OR REPLACE VIEW link_count_by_predicate_and_source AS 
 SELECT source.uid,source.label, pred.uid AS pred_uid, pred.label AS pred_label,count(link.link_id) AS link_count
 FROM link LEFT OUTER JOIN node AS source ON (link.source_id=source.node_id)
           INNER JOIN node AS pred ON (pred.node_id=link.predicate_id)
 GROUP BY source.uid,source.label,pred.uid,pred.label;

CREATE OR REPLACE VIEW link_count_by_node_source_id AS 
 SELECT node.source_id, count(link.link_id) AS link_count
 FROM link 
        INNER JOIN node USING (node_id) 
 GROUP BY node.source_id;

CREATE OR REPLACE VIEW asserted_link_count_by_node_source_id AS 
 SELECT node.source_id, count(link.link_id) AS link_count
 FROM link 
        INNER JOIN node USING (node_id) 
 WHERE is_inferred='f'
 GROUP BY node.source_id;

CREATE OR REPLACE VIEW implied_link_count_by_node_source_id AS 
 SELECT node.source_id, count(link.link_id) AS link_count
 FROM link 
        INNER JOIN node USING (node_id) 
 WHERE is_inferred='t'
 GROUP BY node.source_id;

CREATE OR REPLACE VIEW reified_link_count_by_node_source_id AS 
 SELECT node.source_id, count(link.link_id) AS link_count
 FROM link 
        INNER JOIN node USING (node_id) 
 WHERE reiflink_node_id IS NOT NULL
 GROUP BY node.source_id;

CREATE OR REPLACE VIEW link_count_by_node_source AS 
 SELECT source.uid,source.label, count(link.link_id) AS link_count
 FROM link 
        INNER JOIN node USING (node_id) 
        LEFT OUTER JOIN node AS source ON (node.source_id=source.node_id)
 GROUP BY source.uid,source.label;

CREATE OR REPLACE VIEW link_count_by_node_and_object_source AS 
 SELECT node_source.uid AS node_source_uid,
        node_source.label AS node_source_label,
        obj_source.uid AS obj_source_uid,
        obj_source.label AS obj_source_label,
        count(link.link_id) AS link_count
 FROM   link 
        INNER JOIN node USING (node_id) 
        LEFT OUTER JOIN node AS node_source ON (node.source_id=node_source.node_id)
        INNER JOIN node AS obj ON (link.object_id=obj.node_id) 
        LEFT OUTER JOIN node AS obj_source ON (obj.source_id=obj_source.node_id)
 GROUP BY node_source_uid,
          node_source_label,
          obj_source_uid,
          obj_source_label;

CREATE OR REPLACE VIEW inter_ontology_linkcount AS 
 SELECT * 
 FROM link_count_by_node_and_object_source 
 WHERE node_source_uid != obj_source_uid;

CREATE OR REPLACE VIEW link_count_by_relation_and_node_source AS 
 SELECT pred.uid AS relation_uid,pred.label AS relation_label,source.uid,source.label, count(link.link_id) AS link_count
 FROM link LEFT OUTER JOIN node AS source ON (link.source_id=source.node_id)
        INNER JOIN node AS pred ON (link.predicate_id=pred.node_id)
 GROUP BY pred.uid,pred.label,source.uid,source.label;

CREATE OR REPLACE VIEW link_count_by_metatype_and_source AS 
 SELECT source.uid,source.label, is_inferred, combinator, count(link.link_id) AS link_count
 FROM link 
        LEFT OUTER JOIN node AS source ON (link.source_id=source.node_id)
 GROUP BY source.uid,source.label, is_inferred, combinator;

CREATE OR REPLACE VIEW link_count_by_metatype_and_node_source AS 
 SELECT source.uid,source.label, is_inferred, combinator, count(link.link_id) AS link_count
 FROM link 
        INNER JOIN node USING (node_id) 
        LEFT OUTER JOIN node AS source ON (node.source_id=source.node_id)
 GROUP BY source.uid,source.label, is_inferred, combinator;

CREATE OR REPLACE VIEW implied_link_count_by_node_source AS 
 SELECT source.uid,source.label, count(link.link_id) AS link_count
 FROM link 
        INNER JOIN node USING (node_id) 
        LEFT OUTER JOIN node AS source ON (node.source_id=source.node_id)
 WHERE link.is_inferred='t'
 GROUP BY source.uid,source.label;

CREATE OR REPLACE VIEW instance_count_by_class AS
 SELECT
  object_id AS class_node_id,
  count(DISTINCT node_id) AS instance_count
 FROM
  instance_of_link
 GROUP BY
  object_id;

CREATE OR REPLACE VIEW asserted_instance_count_by_class AS
 SELECT
  object_id AS class_node_id,
  count(DISTINCT node_id) AS instance_count
 FROM
  asserted_instance_of_link
 GROUP BY
  object_id;

CREATE OR REPLACE VIEW instance_count_by_class_all AS
 SELECT DISTINCT
  ic.class_node_id,
  aic.instance_count AS asserted_instance_count,
  ic.instance_count AS instance_count
 FROM
  instance_count_by_class AS ic
  LEFT OUTER JOIN asserted_instance_count_by_class AS aic USING (class_node_id);

CREATE OR REPLACE VIEW relation_summary AS 
 SELECT 
  pred.uid AS relation_uid,
  pred.label AS relation_label,
  count(link.link_id) AS link_count,
  count(DISTINCT link.node_id) AS node_count,
  count(DISTINCT link.object_id) AS obj_count,
  count(DISTINCT link.source_id) AS source_count
 FROM link 
  INNER JOIN node AS pred ON (link.predicate_id=pred.node_id)
 GROUP BY 
  pred.uid,
  pred.label;

CREATE OR REPLACE VIEW instance_level_relation_summary AS 
 SELECT 
  pred.uid AS relation_uid,
  pred.label AS relation_label,
  count(link.link_id) AS link_count,
  count(DISTINCT link.node_id) AS node_count,
  count(DISTINCT link.object_id) AS obj_count,
  count(DISTINCT link.source_id) AS source_count
 FROM link 
  INNER JOIN node AS pred ON (link.predicate_id=pred.node_id)
  INNER JOIN node AS n ON (link.node_id=n.node_id)
  INNER JOIN node AS ob ON (link.object_id=ob.node_id)
 WHERE
  n.metatype='I'
  AND
  ob.metatype='I'
 GROUP BY 
  pred.uid,
  pred.label;

CREATE OR REPLACE VIEW instance_level_relation_class_summary AS 
 SELECT 
  pred.uid AS relation_uid,
  pred.label AS relation_label,
  count(link.link_id) AS link_count,
  count(DISTINCT nt.object_id) AS node_class_count,
  count(DISTINCT obt.object_id) AS obj_class_count,
  count(DISTINCT link.source_id) AS source_count
 FROM link 
  INNER JOIN node AS pred ON (link.predicate_id=pred.node_id)
  INNER JOIN instance_of_link AS nt ON (link.node_id=nt.node_id)
  INNER JOIN instance_of_link AS obt ON (link.object_id=obt.node_id)
 GROUP BY 
  pred.uid,
  pred.label;

CREATE OR REPLACE VIEW node_count_by_metatype_and_node_source AS 
 SELECT node.metatype,source.uid,source.label, count(node.node_id) AS node_count
 FROM node LEFT OUTER JOIN node AS source ON (node.source_id=source.node_id)
 GROUP BY node.metatype,source.uid,source.label;

CREATE OR REPLACE VIEW annotated_entity_count_by_source AS 
 SELECT source.uid,source.label, count(DISTINCT node.node_id) AS annotated_entity_count
 FROM link 
        INNER JOIN node USING (node_id) 
        LEFT OUTER JOIN node AS source ON (node.source_id=source.node_id)
 WHERE
  reiflink_node_id IS NOT NULL
 GROUP BY source.uid,source.label;

CREATE OR REPLACE VIEW annotated_entity_count_by_source_id AS 
 SELECT node.source_id, count(DISTINCT node.node_id) AS annotated_entity_count
 FROM link 
      INNER JOIN node USING (node_id) 
 WHERE
  reiflink_node_id IS NOT NULL
 GROUP BY node.source_id;

CREATE OR REPLACE VIEW source_summary AS 
 SELECT 
        source.uid,source.label, 
        nc.node_count,
        alc.link_count AS asserted_link_count_in_source,
        lc.link_count AS link_count_in_source,
        rlc.link_count AS annotation_link_count_in_source,
        alcn.link_count AS asserted_link_count_by_node_source,
        lcn.link_count AS link_count_by_node_source,
        ilcn.link_count AS implied_link_count_by_node_source,
        rlcn.link_count AS reified_link_count_by_node_source,
        aec.annotated_entity_count
 FROM
  node AS source
  INNER JOIN node_count_by_source_id AS nc ON (nc.source_id=source.node_id)
  LEFT OUTER JOIN link_count_by_source_id AS lc ON (lc.source_id=source.node_id)
  LEFT OUTER JOIN asserted_link_count_by_source_id AS alc ON (alc.source_id=source.node_id)
  LEFT OUTER JOIN reified_link_count_by_source_id AS rlc ON (rlc.source_id=source.node_id)
  LEFT OUTER JOIN link_count_by_node_source_id AS lcn ON (lcn.source_id=source.node_id)
  LEFT OUTER JOIN implied_link_count_by_node_source_id AS ilcn ON (ilcn.source_id=source.node_id)
  LEFT OUTER JOIN asserted_link_count_by_node_source_id AS alcn ON (alcn.source_id=source.node_id)
  LEFT OUTER JOIN reified_link_count_by_node_source_id AS rlcn ON (rlcn.source_id=source.node_id)
  LEFT OUTER JOIN annotated_entity_count_by_source_id AS aec ON (aec.source_id=source.node_id);

COMMENT ON VIEW source_summary IS 'Summary statistics for all nodes and links in database:
<pre>
uid                                | node source identifier. Example: fma
label                              | node source label. Often not populated.
node_count                         | Number of nodes belonging to that source. Note that each annotation (reified) statement is counted as a node
asserted_link_count_in_source      | Non-implied links belonging to that source. Note that links may sometimes not be attributed to the same source as the node.
link_count_in_source               | Total links (asserted+implied). See above.
annotation_link_count_in_source    | Number of annotation link statements. Annotation links are those with reiflink_node_id not null.
asserted_link_count_by_node_source | Number of non-implied links that have a node from source as the subject/source. Includes cross-ontology links, annotation links.
link_count_by_node_source          | As above, including implied links (ie all the way to the root)
reified_link_count_by_node_source  | As above, but only counting those with reiflink_node_id not null
annotated_entity_count             | Number of nodes from this source that have annotation link statements emanating from them
</pre>
Example:
<pre>
uid                                | ZFIN  - all nodes coming from ZFIN.org
label                              | ""    - we should really have a label here like "zebrafish information netword database"
node_count                         | 54622 - genes, genotypes, publications, experiments; but not classes - they go in ZFA
asserted_link_count_in_source      | 189789 - in this case these will be a mixture of G-P annotation links, as well as genotype-gene links, annot-pub links etc
link_count_in_source               | 235041 - above including implied links. Not much higher: we expect this to be proportionally higher for ontology sources eg ZFA
annotation_link_count_in_source    | 80816  - here, both genotype-phenotype and promoted gene-phenotype links
asserted_link_count_by_node_source | 228275 - non-implied links that emanate from a ZFIN node
link_count_by_node_source          | 1003067 - includes implied
reified_link_count_by_node_source  | 80816  - note that this matches annotatuon_link_count_in_source. only ZFIN nodes (genotypes etc) have annotations from ZFIN (contrast with OMIM)
annotated_entity_count             | 11610 - nodes in the ZFIN IDspace that have annotations. genes + genotypes
</pre>
';

CREATE OR REPLACE VIEW 
 
CREATE OR REPLACE VIEW node_count_by_document_source AS 
 SELECT source.uid,source.label, count(node.node_id) AS node_count
 FROM node 
  INNER JOIN node_audit USING (node_id)
  INNER JOIN node AS source ON (node_audit.source_id=source.node_id)
 GROUP BY source.uid,source.label;



-- todo
-- so far only shows meaningful results for class AE nodes
CREATE OR REPLACE VIEW annotated_entity_count_by_type AS 
 SELECT type_node.uid, type_node.label, count(DISTINCT link.node_id) AS annotated_entity_count
 FROM link
        INNER JOIN is_a_link USING (node_id)
        INNER JOIN node AS type_node ON (is_a_link.object_id=type_node.node_id)
 WHERE
  link.reiflink_node_id IS NOT NULL
 GROUP BY type_node.uid, type_node.label;

--- per node-level aggregates:

CREATE OR REPLACE VIEW link_count_by_node AS
 SELECT
  node.uid                      AS node_uid,
  node.label                    AS node_label,
  node.source_id                AS node_source_id,
  count(DISTINCT link_id)       AS link_count
 FROM node INNER JOIN link USING (node_id)
 GROUP BY
  node.uid,
  node.label,
  node.source_id;

CREATE OR REPLACE VIEW asserted_link_count_by_node AS
 SELECT
  node.uid                      AS node_uid,
  node.label                    AS node_label,
  node.source_id                AS node_source_id,
  count(DISTINCT link_id)       AS link_count
 FROM node INNER JOIN asserted_link USING (node_id)
 GROUP BY
  node.uid,
  node.label,
  node.source_id;

CREATE OR REPLACE VIEW linked_object_count_by_node AS
 SELECT
  node.uid                      AS node_uid,
  node.label                    AS node_label,
  node.source_id                AS node_source_id,
  count(DISTINCT link.object_id) AS link_count
 FROM node INNER JOIN link USING (node_id)
 GROUP BY
  node.uid,
  node.label,
  node.source_id;

CREATE OR REPLACE VIEW asserted_linked_object_count_by_node AS
 SELECT
  node.uid                      AS node_uid,
  node.label                    AS node_label,
  node.source_id                AS node_source_id,
  count(DISTINCT asserted_link.object_id) AS link_count
 FROM node INNER JOIN asserted_link USING (node_id)
 GROUP BY
  node.uid,
  node.label,
  node.source_id;

CREATE OR REPLACE VIEW link_count_by_object_node AS
 SELECT
  obj.uid AS object_uid,
  obj.label AS object_label,
  obj.source_id AS object_source_id,
  count(DISTINCT link_id)       AS link_count
 FROM node AS obj INNER JOIN link ON (obj.node_id=link.object_id)
 GROUP BY
  obj.uid,
  obj.label,
  obj.source_id;

CREATE OR REPLACE VIEW asserted_link_count_by_object_node AS
 SELECT
  obj.uid AS object_uid,
  obj.label AS object_label,
  obj.source_id AS object_source_id,
  count(DISTINCT link_id)       AS link_count
 FROM node AS obj INNER JOIN asserted_link ON (obj.node_id=asserted_link.object_id)
 GROUP BY
  obj.uid,
  obj.label,
  obj.source_id;


-- ************************************************************
-- INFORMATION CONTENT
-- ************************************************************
-- CREATE SCHEMA obd_statistical_view;
-- SET search_path TO obd_statistical_view,obd_aggregate_view,obd_annotation_view,obd_prejoins_view, obd_obo_metamodel_view,obd_core_view,public;

CREATE OR REPLACE VIEW implied_annotation_link_to_node AS
 SELECT
  implied_annotation_link.*,
  node.uid AS object_uid
 FROM
  implied_annotation_link INNER JOIN node ON (object_id=node.node_id);

CREATE OR REPLACE VIEW co_annotated_to_pair AS
 SELECT 
  link1.object_id AS object1_id,
  link2.object_id AS object2_id,
  COUNT(DISTINCT node_id) AS total_entities_annotated_to_both
 FROM
  implied_annotation_link AS link1
  INNER JOIN implied_annotation_link AS link2 USING (node_id)
 WHERE 
  NOT EXISTS (SELECT node_id,object_id FROM link AS rl WHERE rl.node_id=link1.object_id AND rl.object_id=link2.object_id)
 AND
  NOT EXISTS (SELECT node_id,object_id FROM link AS rl WHERE rl.node_id=link2.object_id AND rl.object_id=link1.object_id)
 GROUP BY object1_id, object2_id;

COMMENT ON VIEW co_annotated_to_pair IS 'a pair of nodes that have
been used to annotate the same entity; for example,
(Eye,photoreceptor) if a gene affects both; (Shh,Heart), if a document
is coannotated with both';

CREATE OR REPLACE VIEW annotated_entity_count_by_node AS
 SELECT
  ial.object_id AS node_id,
  count(distinct ial.node_id) AS annotated_entity_count
 FROM
  implied_annotation_link AS ial
 GROUP BY ial.object_id;

CREATE OR REPLACE VIEW co_annotated_to_pair_with_score AS
 SELECT
  *,
  total_entities_annotated_to_both / cast(annotated_entity_count AS FLOAT) AS object2_score
 FROM 
  co_annotated_to_pair AS pair
  INNER JOIN annotated_entity_count_by_node AS aec ON (aec.node_id=pair.object2_id)
 WHERE
  annotated_entity_count > 0;


-- todo: need to ensure comparing like with like
CREATE OR REPLACE VIEW node_p_value AS
 SELECT
  ial.object_id AS node_id,
  count(distinct ial.node_id) / (SELECT total FROM count_of_annotated_entity)
 FROM
  implied_annotation_link AS ial;

CREATE OR REPLACE VIEW co_annotated_to_pair_with_node AS
 SELECT 
  node1.uid AS node1_uid,
  node1.label AS node1_label,
  node1.source_id AS node1_source_id,
  node2.uid AS node2_uid,
  node2.label AS node2_label,
  node2.source_id AS node2_source_id,
  cap.*
 FROM
  co_annotated_to_pair AS cap
  INNER JOIN node AS node1 ON (cap.object1_id=node1.node_id)
  INNER JOIN node AS node2 ON (cap.object2_id=node2.node_id);

COMMENT ON VIEW co_annotated_to_pair_with_node IS 'As
co_annotated_to_pair, with an extra join to get details of the node
pair';

CREATE OR REPLACE VIEW annotated_entity_total_annotation_nodes AS
 SELECT
  baselink.node_id AS annotated_entity_id,
  count(DISTINCT baselink.object_id) AS total_annotation_nodes
 FROM
  implied_annotation_link AS baselink
 GROUP BY
  baselink.node_id;

-- BEGIN MATERIALIZE
-- SELECT create_matview('annotated_entity_total_annotation_nodes');
-- CREATE INDEX annotated_entity_total_annotation_nodes_idx_ae ON annotated_entity_total_annotation_nodes(annotated_entity_id);
-- CREATE INDEX annotated_entity_total_annotation_nodes_idx_ae_total ON annotated_entity_total_annotation_nodes(annotated_entity_id,total_annotation_nodes);
-- END MATERIALIZE


-- Example: SELECT node_label(is_a_node_id),node_label(object_id) from node_pair_annotation_xp_intersection where node1_id = 532850 and node2_id=239699;
CREATE OR REPLACE VIEW node_pair_annotation_xp_intersection AS
 SELECT DISTINCT
  iax1.node_id AS node1_id,
  iax2.node_id AS node2_id,
  iax1.is_a_node_id,
  iax1.object_id
 FROM 
  implied_annotation_xp AS iax1 INNER JOIN
  implied_annotation_xp AS iax2 USING (is_a_node_id,object_id);

COMMENT ON VIEW node_pair_annotation_xp_intersection IS 'Given two
nodes (e.g. genes) node1 and node2, what are the annotation class
nodes in common, split on an axis? Uses both N+S relations and normal
relations. For example, if g1 is annotated to thick/cranial-nerve-VIII
and g2 is annotated to misshapen-cranial-nerve-VII then the xp in
common will be {shape, quality} x { cranial-nerve, nerve,
organsim-part, ...}';


CREATE OR REPLACE VIEW node_pair_annotation_xp_intersection_with_total AS
 SELECT 
  npax.*,
  axis1c.total AS is_a_node_total,
  axis2c.total AS object_node_total
 FROM
  node_pair_annotation_xp_intersection AS npax
  INNER JOIN implied_annotation_link_count_by_object AS axis1c ON (axis1c.node_id=npax.is_a_node_id)
  INNER JOIN implied_annotation_link_count_by_object AS axis2c ON (axis2c.node_id=npax.object_id);

COMMENT ON VIEW node_pair_annotation_xp_intersection_with_total IS 'as
node_pair_annotation_xp_intersection but include the totals for each
axis term (but not the totals for the combination)';

CREATE OR REPLACE VIEW node_pair_annotation_xp_intersection_with_stats1 AS
 SELECT 
  npaxt.*,
  CAST(is_a_node_total AS FLOAT) / total AS is_a_node_p,
  CAST(object_node_total AS FLOAT) / total AS object_node_p
 FROM
  node_pair_annotation_xp_intersection_with_total AS npaxt,
  count_of_annotated_entity;

COMMENT ON VIEW node_pair_annotation_xp_intersection_with_stats1 IS
'intermediate view used to construct
node_pair_annotation_xp_intersection_with_stats';

-- Example: SELECT node_label(is_a_node_id),node_label(object_id) from node_pair_annotation_xp_intersection_with_stats where node1_id = 532850 and node2_id=239699;
CREATE OR REPLACE VIEW node_pair_annotation_xp_intersection_with_stats AS
 SELECT 
  npaxt.*,
  is_a_node_p * object_node_p AS p
 FROM
  node_pair_annotation_xp_intersection_with_stats1 AS npaxt;

COMMENT ON VIEW node_pair_annotation_xp_intersection_with_stats IS 'As
 node_pair_annotation_xp_intersection, but include the p values for
 each axis, and the product of those p values. Note that the product
 is not necessarily an accurate measure of probability, as the two
 axes will probably not be independent';

CREATE OR REPLACE VIEW node_pair_annotation_intersection AS
 SELECT DISTINCT
  ial1.node_id AS node1_id,
  ial2.node_id AS node2_id,
  ial1.object_id
 FROM 
  implied_annotation_link AS ial1,
  implied_annotation_link AS ial2
 WHERE
  ial1.object_id = ial2.object_id; -- both annotations agree

CREATE OR REPLACE VIEW node_pair_annotation_intersection_nr AS
 SELECT DISTINCT
  *
 FROM 
  node_pair_annotation_intersection AS npai
 WHERE
  NOT EXISTS (SELECT *
              FROM node_pair_annotation_intersection AS npai_r ,
                 link
              WHERE 
                npai_r.node1_id=npai.node1_id AND
                npai_r.node2_id=npai.node2_id AND
                npai_r.object_id != npai.object_id AND
                npai_r.object_id=link.node_id AND 
                npai.object_id=link.object_id);
  

CREATE OR REPLACE VIEW node_pair_intersection_with_total AS
 SELECT DISTINCT
  ial1.node_id AS node1_id,
  ial2.node_id AS node2_id,
  ial1.object_id,
  ial1.total
 FROM 
  implied_annotation_link_with_total AS ial1,
  implied_annotation_link_with_total AS ial2
 WHERE
  ial1.object_id = ial2.object_id; -- both annotations agree

CREATE OR REPLACE VIEW node_pair_annotation_intersection_count AS
 SELECT
  ial1.node_id AS node1_id,
  ial2.node_id AS node2_id,
  count(DISTINCT ial1.object_id) AS total_nodes_in_intersection
 FROM 
  implied_annotation_link AS ial1,
  implied_annotation_link AS ial2
 WHERE
  ial1.object_id = ial2.object_id -- both annotations agree
 GROUP BY
  ial1.node_id,
  ial2.node_id;

COMMENT ON VIEW node_pair_annotation_intersection_count IS 'Formally: | a*(g1) ^ a*(g2) |. For any
two nodes (e.g. two gene nodes), what is the total number of
annotation nodes in common? implied_annotation_link is used here.';

CREATE OR REPLACE VIEW node_pair_annotation_intersection_count_nonzero AS
 SELECT * FROM node_pair_annotation_intersection_count
 WHERE total_nodes_in_intersection > 0;
-- can we create a matview out of this? possible with obdphuman

-- TODO CREATE UNIQUE INDEX count_of_annotated_entity_by_class_node_and_evidence_idx_1 ON count_of_annotated_entity_by_class_node_and_evidence(node_id);



CREATE OR REPLACE VIEW node_pair_annotation_union1 AS
 SELECT DISTINCT
  ial1.node_id AS node1_id,
  n2.node_id AS node2_id,
  ial1.object_id
 FROM 
  node AS n2,
  implied_annotation_link AS ial1;

COMMENT ON VIEW node_pair_annotation_union1 IS 'INTERNAL
VIEW: For any two nodes n1 and n2 (e.g. two gene nodes), what are the
implied annotations for n1. Warning: large cartesian product unless
constrained. This view is used internally in the implementation of
node_pair_annotation_union';


CREATE OR REPLACE VIEW node_pair_annotation_union2 AS
 SELECT DISTINCT
  n1.node_id AS node1_id,
  ial2.node_id AS node2_id,
  ial2.object_id
 FROM 
  node AS n1,
  implied_annotation_link AS ial2;

COMMENT ON VIEW node_pair_annotation_union2 IS 'INTERNAL
VIEW: For any two nodes n1 and n2 (e.g. two gene nodes), what are the
implied annotations for n2. Warning: large cartesian product unless
constrained. This view is used internally in the implementation of
node_pair_annotation_union';

CREATE OR REPLACE VIEW node_pair_annotation_union AS
 SELECT
  *
 FROM node_pair_annotation_union1
 UNION
 SELECT
  *
 FROM node_pair_annotation_union2;

COMMENT ON VIEW node_pair_annotation_union1 IS 'For any two nodes n1
and n2 (e.g. two gene nodes), what are the implied annotations for
either n1 or n2. Warning: large cartesian product unless
constrained.';

CREATE OR REPLACE VIEW node_pair_annotation_union_count AS
 SELECT
  node1_id,
  node2_id,
  count(DISTINCT object_id) AS total_nodes_in_union
 FROM 
  node_pair_annotation_union
 GROUP BY
  node1_id,
  node2_id;

COMMENT ON VIEW node_pair_annotation_union1 IS 'For any two nodes n1
and n2 (e.g. two gene nodes), what are the total number of distinct
implied annotation nodes for either n1 or n2. Warning: large cartesian
product unless constrained.';

CREATE OR REPLACE VIEW node_pair_annotation_similarity_score_old AS
 SELECT
  nii.node1_id,
  nii.node2_id,
  total_nodes_in_intersection,
  total_nodes_in_union,
  CAST(total_nodes_in_intersection AS FLOAT) / total_nodes_in_union AS basic_score
 FROM 
  node_pair_annotation_intersection_count AS nii INNER JOIN
  node_pair_annotation_union_count AS niu USING (node1_id,node2_id)
 WHERE
  total_nodes_in_intersection > 0;

CREATE OR REPLACE VIEW node_pair_annotation_similarity_score AS
 SELECT
  nii.node1_id,
   nii.node2_id,
  n1t.total_annotation_nodes AS node1_total_nodes,
  n2t.total_annotation_nodes AS node2_total_nodes,
  total_nodes_in_intersection,
  CAST(total_nodes_in_intersection AS FLOAT) / ((n1t.total_annotation_nodes + n2t.total_annotation_nodes) - total_nodes_in_intersection) AS basic_score
 FROM 
  node_pair_annotation_intersection_count AS nii 
  INNER JOIN annotated_entity_total_annotation_nodes AS n1t ON (nii.node1_id=n1t.annotated_entity_id)
  INNER JOIN annotated_entity_total_annotation_nodes AS n2t ON (nii.node2_id=n2t.annotated_entity_id)
 WHERE
  total_nodes_in_intersection > 0;

COMMENT ON VIEW node_pair_annotation_similarity_score IS
'annotation overlap between two nodes as a ratio of intersection / union. This is an approaximation, it actually calculates
| nodes in intersection | / ( | nodes in set 1| + | nodes in set 2|). If we double this we get the Dice distance. 
Tversky ratio = f( set1 ^ set 2) / ( f (set1 ^ set2) + alpha * f(set1 - set2) + beta * f(set2 - set1) ). if alpha = beta = 0.5 and f is the cardinality function, then this is S_dice
 ';


CREATE OR REPLACE VIEW annotated_entity_total_annotations_by_annotsrc AS
 SELECT
  node_id AS annotated_entity_id,
  source_id,
  count(DISTINCT link_id) AS total_annotations
 FROM
  reified_link
 GROUP BY
  node_id,
  source_id;

COMMENT ON VIEW
annotated_entity_total_annotations_by_annotsrc IS
'number of annotations for a particular annotated
entity (eg a genotype) and annotation source (eg ZFIN).';

CREATE OR REPLACE VIEW annotated_entity_total_annotation_nodes_by_annotsrc AS
 SELECT
  baselink.node_id AS annotated_entity_id,
  baselink.source_id,
  count(DISTINCT baselink.object_id) AS total_annotation_nodes
 FROM
  implied_annotation_link AS baselink
 GROUP BY
  baselink.node_id,
  baselink.source_id;

COMMENT ON VIEW
annotated_entity_total_annotation_nodes_by_annotsrc IS
'number of nodes colored by annotation for a particular annotated
entity (eg a genotype) and annotation source (eg ZFIN).';

CREATE OR REPLACE VIEW annotated_entity_union_annotations_between_annotsrc_pair AS
 SELECT
  baselink.node_id AS annotated_entity_id,
  baselink.source_id AS base_source_id,
  targetlink.source_id AS target_source_id,
  count(DISTINCT node.node_id) AS total_nodes_in_union
 FROM 
  implied_annotation_link AS baselink,
  implied_annotation_link AS targetlink,
  node
 WHERE
  baselink.node_id = targetlink.node_id   -- both annotations are of the same subject entity
  AND
      (node.node_id = targetlink.object_id OR
       node.node_id = baselink.object_id)
 GROUP BY
  baselink.node_id,
  baselink.source_id,
  targetlink.source_id;

COMMENT ON VIEW annotated_entity_union_annotations_between_annotsrc_pair IS 
'
annotated_entity_union_annotations_between_annotsrc_pair(ae,s1,s2) = 
|implied_annotations(a1,s1) UNION implied_annotations(a1,s2)|

number colored nodes in union for two annotation sources and an
annotated entity. For example, one gene and two independent sources of annotation
on that gene
';

CREATE OR REPLACE VIEW annotated_entity_shared_annotations_between_annotsrc_pair AS
 SELECT
  baselink.node_id AS annotated_entity_id,
  baselink.source_id AS base_source_id,
  targetlink.source_id AS target_source_id,
  count(DISTINCT baselink.object_id) AS total_nodes_in_common
 FROM 
  implied_annotation_link AS baselink,
  implied_annotation_link AS targetlink
 WHERE
  baselink.node_id = targetlink.node_id   -- both annotations are of the same subject entity
  AND
  baselink.object_id = targetlink.object_id -- both annotations agree
 GROUP BY
  baselink.node_id,
  baselink.source_id,
  targetlink.source_id;

COMMENT ON VIEW annotated_entity_shared_annotations_between_annotsrc_pair IS 
'
annotated_entity_shared_annotations_between_annotsrc_pair(ae,s1,s2) = 
|implied_annotations(a1,s1) ^ implied_annotations(a1,s2)|

number colored nodes in common for two annotation sources and an
annotated entity. For example, one gene and two independent sources of annotation
on that gene
';


CREATE OR REPLACE VIEW annotated_entity_congruence_between_annotsrc_pair AS
 SELECT
  sharednodes.annotated_entity_id,
  sharednodes.base_source_id,
  sharednodes.target_source_id,
  sharednodes.total_nodes_in_common,
  totalnodes.total_nodes_in_union,
  CAST(total_nodes_in_common AS FLOAT) / total_nodes_in_union AS congruence
 FROM
             annotated_entity_shared_annotations_between_annotsrc_pair    AS sharednodes
  INNER JOIN annotated_entity_union_annotations_between_annotsrc_pair      AS totalnodes
   ON       (sharednodes.annotated_entity_id = totalnodes.annotated_entity_id AND
             sharednodes.base_source_id = totalnodes.base_source_id AND 
             sharednodes.target_source_id = totalnodes.target_source_id);

COMMENT ON VIEW annotated_entity_congruence_between_annotsrc_pair IS
'congruence for an annotated_entity and two source, base (s1) and
target (s2) is calculated as the proportion of (inferred) nodes in
common divided by the proportion of nodes in the union of both
annotation sets:

  congruence(ae,s1,s2) = |annotnodes*(ae,s1) ^ annotnodes*(ae,s2)| / |annotnodes*(ae,s1) UNION annotnodes*(ae,s2)|

Here, annotnodes*(ae,s) is the set of classes used to annotated ae by
s, together with all classes that are inferred to be parents of those
classes.

For example, if ae is a gene, s1 may annotate {T-cell,astrocyte}, and
s2 may annotate {lymphocyte,interneuron}. The inferred nodes in common
are {lymphocyte,nerve cell,cell}. The inferred nodesin the union are
{T-cell,astrocyte,glial cell,neuron,lymphocyte,nerve cell,cell}. Thus
the congruence is 3/7.

We can visualize this is graph coloring, with nodes in one set and its
deductive closure colored one way, the other annotation set colored
the other way, with some color mixing indicating the intersection.
';

CREATE OR REPLACE VIEW annotated_entity_congruence_by_annotsrc AS
 SELECT
  ial.node_id AS annotated_entity_id,
  ial.source_id,
  ialc.total                    AS total_nodes,
  COUNT(DISTINCT ial.object_id) AS total_nodes_in_src,
  CAST(COUNT(DISTINCT ial.object_id) AS FLOAT) / ialc.total AS congruence
 FROM
             implied_annotation_link AS ial
  INNER JOIN implied_annotation_link_count_by_node AS ialc USING (node_id)

 GROUP BY
  ial.node_id,
  ial.source_id,
  ialc.total;
 
COMMENT ON VIEW annotated_entity_congruence_by_annotsrc IS 
'congruence for an annotated_entity and a source vs all other sources (including itself).
Faster than comparing between pairs. 

  congruence(ae,s) = |annotnodes*(ae,s)| / |annotnodes*(ae)|
';

CREATE OR REPLACE VIEW avg_annotated_entity_congruence_between_annotsrc_pair AS
 SELECT
  base_source_id,
  target_source_id,
  count(DISTINCT annotated_entity_id) AS annotated_entity_count,
  avg(congruence) AS avg_congruence
 FROM
  annotated_entity_congruence_between_annotsrc_pair
 GROUP BY
  base_source_id,
  target_source_id;

COMMENT ON VIEW avg_annotated_entity_congruence_between_annotsrc_pair IS
'The average of @annotated_entity_congruence_between_annotsrc_pair@ across annotated entities.

Please see the docs for @annotated_entity_congruence_between_annotsrc_pair@. Recall that this measure is for a particular annotated entity ae:

  congruence(ae,s1,s2) = |annotnodes*(ae,s1) ^ annotnodes*(ae,s2)| / |annotnodes*(ae,s1) UNION annotnodes*(ae,s2)|

This gives the average over all annotated entities
';

-- EXAMPLE: select node_uid(node_id),node_uid(grouping_predicate_id),node_uid(annotgroup_node_id),node_uid(object_id),node_label(object_id) from implied_annotation_link_to_annotgroup where node_uid(annotgroup_node_id) like 'entrez%'
CREATE OR REPLACE VIEW implied_annotation_link_to_annotgroup AS
  SELECT
   link.object_id AS annotgroup_node_id, -- eg Gene, if the AE is a genotype
   link.predicate_id AS grouping_predicate_id,
   ial.node_id,
   ial.predicate_id,
   ial.object_id
  FROM
   implied_annotation_link AS ial
   INNER JOIN link ON (ial.node_id=link.node_id)
  WHERE
   link.reiflink_node_id IS NULL
   AND
   link.is_inferred='f';

CREATE OR REPLACE VIEW implied_annotation_link_to_annotgroup_level2 AS
  SELECT
   link2.object_id AS annotgroup_node_id, -- eg Gene, if the AE is a genotype
   link2.predicate_id AS grouping_predicate_id,
   link1.object_id AS annotgroup1_node_id, -- eg Gene, if the AE is a genotype
   link1.predicate_id AS grouping1_predicate_id,
   ial.node_id,
   ial.predicate_id,
   ial.object_id
  FROM
   implied_annotation_link AS ial
   INNER JOIN link AS link1 ON (ial.node_id=link1.node_id)
   INNER JOIN link AS link2 ON (link1.object_id=link2.node_id)
  WHERE
   link1.reiflink_node_id IS NULL
   AND
   link2.reiflink_node_id IS NULL;

COMMENT ON VIEW implied_annotation_link_to_annotgroup_level2 IS '2-level structure; for example annotations to genotypes to genes to homology groups EXAMPLE: select node_uid(node_id),node_uid(grouping_predicate_id) as p,node_uid(annotgroup_node_id),node_uid(object_id),node_label(object_id) from implied_annotation_link_to_annotgroup_level2 where node_label(object_id) = ''Kidney'' and node_uid(annotgroup1_node_id) like ''entrez%'' and node_uid(grouping_predicate_id)=''OBO_REL:descended_from''';


CREATE OR REPLACE VIEW count_of_annotated_entity_by_class_node_and_evidence AS
 SELECT 
  ial.object_id                     AS node_id,
  CAST(NULL AS INT)                 AS evidence_id,
  count(DISTINCT ial.node_id)         AS total
 FROM
  implied_annotation_link AS ial
 GROUP BY ial.object_id;

-- BEGIN MATERIALIZE
-- SELECT create_matview('count_of_annotated_entity_by_class_node_and_evidence');
-- CREATE UNIQUE INDEX count_of_annotated_entity_by_class_node_and_evidence_idx_1 ON count_of_annotated_entity_by_class_node_and_evidence(node_id);
-- CREATE UNIQUE INDEX count_of_annotated_entity_by_class_node_and_evidence_idx_2 ON count_of_annotated_entity_by_class_node_and_evidence(node_id,count);
-- END MATERIALIZE

CREATE OR REPLACE VIEW class_node_entropy_by_evidence AS
 SELECT 
  node_id,
--  evidence_id,
  tbc.total                                          AS annotated_entity_count,
  - log(cast(tbc.total AS float) / t.total) / log(2) AS shannon_information
 FROM
  count_of_annotated_entity_by_class_node_and_evidence  AS tbc,
  count_of_annotated_entity                             AS t;

COMMENT ON VIEW class_node_entropy_by_evidence IS 'I(Cn) = -log2
p(Cn). Based on total annotated entities in database. Evidence not yet
implemented';

-- BEGIN MATERIALIZE
-- SELECT create_matview('class_node_entropy_by_evidence');
-- CREATE UNIQUE INDEX class_node_entropy_by_evidence_idx_node_id ON class_node_entropy_by_evidence(node_id);
-- CREATE INDEX class_node_entropy_by_evidence_idx_info ON class_node_entropy_by_evidence(shannon_information);
-- CREATE UNIQUE INDEX class_node_entropy_by_evidence_idx_node_id_info ON class_node_entropy_by_evidence(node_id,shannon_information);
-- END MATERIALIZE

CREATE OR REPLACE VIEW implied_annotation_link_with_entropy AS
 SELECT
  e.shannon_information,
  e.annotated_entity_count,
  ial.*
 FROM
  implied_annotation_link AS ial
  INNER JOIN class_node_entropy_by_evidence AS e ON (ial.object_id=e.node_id);

-- BEGIN MATERIALIZE
-- SELECT create_matview('implied_annotation_link_with_entropy');
-- CREATE INDEX implied_annotation_link_with_entropy_idx_node ON implied_annotation_link_with_entropy(node_id);
-- CREATE INDEX implied_annotation_link_with_entropy_idx_object ON implied_annotation_link_with_entropy(object_id);
-- CREATE INDEX implied_annotation_link_with_entropy_idx_node_object ON implied_annotation_link_with_entropy(node_id,object_id);
-- CREATE INDEX implied_annotation_link_with_entropy_idx_node_object_info ON implied_annotation_link_with_entropy(shannon_information,node_id,object_id);
-- END MATERIALIZE

CREATE OR REPLACE VIEW node_pair_annotation_match AS
 SELECT
  ial1.node_id AS node1_id,
  ial2.node_id AS node2_id,
  object_id AS match_id,
  shannon_information AS ic
 FROM 
  implied_annotation_link AS ial1
  INNER JOIN implied_annotation_link_with_entropy AS ial2 USING (object_id);

CREATE OR REPLACE VIEW node_pair_annotation_match_max_entropy AS
 SELECT
  ial1.node_id AS node1_id,
  ial2.node_id AS node2_id,
  max(DISTINCT shannon_information) AS max_ic
 FROM 
  implied_annotation_link AS ial1
  INNER JOIN implied_annotation_link_with_entropy AS ial2 USING (object_id)
 GROUP BY
  ial1.node_id,
  ial2.node_id;

CREATE OR REPLACE VIEW node_pair_annotation_match_having_max_entropy AS
 SELECT
  node1_id,
  node2_id,
  match_id,
  ic
 FROM 
  node_pair_annotation_match
  INNER JOIN node_pair_annotation_match_max_entropy USING(node1_id,node2_id)
 WHERE 
  ic >= max_ic;


CREATE OR REPLACE VIEW node_pair_annotation_match_sum_entropy AS
 SELECT
  ial1.node_id AS node1_id,
  ial2.node_id AS node2_id,
  sum(DISTINCT shannon_information) AS sum_ic
 FROM 
  implied_annotation_link AS ial1
  INNER JOIN implied_annotation_link_with_entropy AS ial2 USING (object_id)
 GROUP BY
  ial1.node_id,
  ial2.node_id;


CREATE OR REPLACE VIEW class_node_entropy_against_background_TODO AS
 SELECT 
  
  node_id,
--  evidence_id,
  tbc.total                                          AS annotated_entity_count,
  - log(cast(tbc.total AS float) / t.total) / log(2) AS shannon_information
 FROM
  class_count_by_annotated_entity  AS tbc,
  class_count_by_annotated_entity                  AS t;
COMMENT ON VIEW class_node_entropy_by_evidence IS 'I(Cn) = -log2 p(Cn)';

CREATE OR REPLACE VIEW class_node_entropy_by_evidence_with_auto_label AS
 SELECT 
  *,
  node_auto_label(node_id)      AS label
 FROM
  class_node_entropy_by_evidence;

CREATE OR REPLACE VIEW class_node_entropy_by_evidence_with_node_and_auto_label AS
 SELECT 
  class_node_entropy_by_evidence.*,
  node.uid      AS uid,
  node_auto_label(node_id)      AS label
 FROM
  class_node_entropy_by_evidence
  INNER JOIN node USING (node_id)
 ORDER BY shannon_information;

COMMENT ON VIEW
class_node_entropy_by_evidence_with_node_and_auto_label IS 'each class
that has an annotation (asserted and implied) together with the
information content of the class, based on the overall background set
of annotations. TODO: choice of background set. TODO: different
relations for deductive closure. USE NOTES: order by
shannon_information for most informative results';

CREATE OR REPLACE VIEW annotation_with_information_content AS
 SELECT
  asserted_link.*,
  annotated_entity_count,
  shannon_information
 FROM
  asserted_link
  INNER JOIN class_node_entropy_by_evidence AS cne ON (asserted_link.object_id = cne.node_id)
 WHERE
  reiflink_node_id IS NOT NULL;

COMMENT ON VIEW annotation_with_information_content IS 'Annotations
adorned with the information content; this means annotations can be
sorted by information';

-- BEGIN MATERIALIZE
-- SELECT create_matview('annotation_with_information_content');
-- CREATE INDEX annotation_with_information_content_idx_node ON annotation_with_information_content(node_id);
-- CREATE INDEX annotation_with_information_content_idx_object ON annotation_with_information_content(object_id);
-- CREATE INDEX annotation_with_information_content_idx_node_object ON annotation_with_information_content(node_id,object_id);
-- CREATE INDEX annotation_with_information_content_idx_node_object_info ON annotation_with_information_content(shannon_information,node_id,object_id);
-- END MATERIALIZE

CREATE OR REPLACE VIEW annotation_with_information_content_and_auto_label AS
 SELECT
  ai.*,
  node.uid      AS uid,
  node_auto_label(node.node_id)      AS label
 FROM
  annotation_with_information_content AS ai
  INNER JOIN node ON (ai.object_id=node.node_id)
 ORDER BY shannon_information;
  

CREATE OR REPLACE VIEW avg_annotation_information_content_by_annotated_source AS
 SELECT
  source.uid, source.label,
  avg(ai.shannon_information),
  sum(ai.annotated_entity_count)
 FROM 
  annotation_with_information_content AS ai
  INNER JOIN node ON (ai.node_id=node.node_id)
  INNER JOIN node AS source ON (node.source_id=source.node_id)
 GROUP BY source.uid,source.label;
  
CREATE OR REPLACE VIEW avg_annotation_information_content_by_annotation_source AS
 SELECT
  source.uid, source.label,
  avg(ai.shannon_information),
  sum(ai.annotated_entity_count)
 FROM 
  annotation_with_information_content AS ai
  INNER JOIN node AS source ON (ai.source_id=source.node_id)
 GROUP BY source.uid,source.label;

-- some redundancy follows...

CREATE OR REPLACE VIEW avg_information_content AS 
 SELECT avg(shannon_information) AS avg_information_content
 FROM class_node_entropy_by_evidence;

CREATE OR REPLACE VIEW stddev_information_content AS 
 SELECT stddev(shannon_information) AS stddev_information_content
 FROM class_node_entropy_by_evidence;

CREATE OR REPLACE VIEW avg_information_content_by_annotsrc AS 
 SELECT 
  aic.source_id,
  avg(shannon_information) AS avg_information_content
 FROM 
  annotation_with_information_content AS aic
 GROUP BY
  aic.source_id;

COMMENT ON VIEW avg_information_content_by_annotsrc IS 'avg( { IC(c) :
forall direct-annotation-to(c) }) by annotation-source. Be careful
interpreting these results. The IC is measured against the background
of the whole database. This means that species-centric annotation
sources will have an IC inversely proportional to the size of the
annotation set for that source. For example, if the majority of
annotations in the database are to human anatomical classes, then a
xenopus-specific annotation source will have a high IC because it uses
"rarer" classes.';

CREATE OR REPLACE VIEW avg_information_content_by_ontology AS 
 SELECT 
  n.source_id,
  avg(shannon_information) AS avg_information_content
 FROM 
  class_node_entropy_by_evidence AS e
  INNER JOIN node AS n ON (e.node_id=n.node_id)
 WHERE
  n.metatype='C'
 GROUP BY
  n.source_id;

COMMENT ON VIEW avg_information_content_by_ontology IS 'avg( {IC(c) :
forall c in O }). average information content of a class, broken down
by ontology.';

CREATE OR REPLACE VIEW dist_information_content_by_annotation AS 
 SELECT 
  CAST(shannon_information AS INT) AS ic_midpoint,
  COUNT(DISTINCT node_id) AS total_annotation_classes,
  COUNT(DISTINCT object_id) AS total_annotated_entities,
  COUNT(link_id) AS total_annotations
 FROM annotation_with_information_content
 GROUP BY
  CAST(shannon_information AS INT)
 ORDER BY
  CAST(shannon_information AS INT);

COMMENT ON VIEW dist_information_content_by_annotation IS
'distribution of information content. Each bin is an IC midpoint -
i.e. "3" is any IC between 2.5 and 3.5. There are 3 counts for each
bin: The total number of classes whose IC fall within that range; The
total number of annotated entities (e.g. genes) that have an
annotation to a class with an IC within that bin; The total number of
annotations within that bin. Be careful with the 2nd count: the same
annotated entity (e.g. gene) can be present in multiple bins - thus
the numbers do not sum to the total number of annotated entities. Note
that at the high end of the distribution the numbers may tail off more
dramatically for classes than annotations - this is because there may
be multiple redundant annotations to the same high-IC class. There is
a danger of annotation bias here, especially if there are "promoted"
annotations. total_annotation_classes is the safest number to
use. Note that for the counts we only consider direct/asserted
annotations. However, for the IC itself, implicit annotations are
used. E.g. IC("small organ") = -log(p(annot*("small organ"))), where
annot* includes "small heart" etc. However, if there are no direct
annotations to "small organ" then it will not be counted in the
histogram.';




CREATE OR REPLACE VIEW avg_information_content_by_annotated_entity AS 
 SELECT 
  aic.node_id AS annotated_entity_id,
  avg(shannon_information) AS avg_information_content
 FROM 
  annotation_with_information_content AS aic
 GROUP BY
  aic.node_id;

CREATE OR REPLACE VIEW avg_information_content_by_annotsrc_and_annotated_entity AS 
 SELECT 
  aic.source_id,
  aic.node_id AS annotated_entity_id,
  avg(shannon_information) AS avg_information_content
 FROM 
  annotation_with_information_content AS aic
 GROUP BY
  aic.source_id,
  aic.node_id;

CREATE OR REPLACE VIEW unique_annotation_with_information_content AS
 SELECT DISTINCT
  node_id,
  object_id,
  annotated_entity_count,
  shannon_information
 FROM
  annotation_with_information_content;

CREATE OR REPLACE VIEW avg_unique_information_content_by_annotated_entity AS 
 SELECT 
  aic.node_id AS annotated_entity_id,
  avg(shannon_information) AS avg_information_content
 FROM 
  unique_annotation_with_information_content AS aic
 GROUP BY
  aic.node_id;



-- ************************************************************
-- REASONER VIEWS
-- ************************************************************
CREATE OR REPLACE VIEW inferred_nr_subclass AS
 SELECT
  *
 FROM
  inferred_link AS il
 WHERE
  NOT EXISTS (SELECT *
              FROM consecutive_link_pair AS clp
              WHERE
               il.node_id=clp.node_id AND
               il.object_id=clp.next_object_id AND
               clp.via_node_id != clp.node_id AND
               clp.via_node_id != clp.next_object_id);

CREATE OR REPLACE VIEW nr_link AS
 SELECT
  *
 FROM
  link
 WHERE
  NOT EXISTS 
   (SELECT *
            FROM consecutive_link_pair AS clp
            WHERE
               link.node_id=clp.node_id AND
               link.object_id=clp.next_object_id AND
               clp.via_node_id != clp.node_id AND
               clp.via_node_id != clp.next_object_id);

-- todo: test for equivalencies
CREATE OR REPLACE VIEW nr_instance_of_link AS
 SELECT
  *
 FROM
  instance_of_link AS link
 WHERE
  NOT EXISTS 
   (SELECT *
            FROM consecutive_link_pair AS clp
            WHERE
               link.node_id=clp.node_id AND
               link.predicate_id=clp.predicate_id AND
               link.object_id=clp.next_object_id AND
               clp.via_node_id != clp.node_id AND
	       clp.next_predicate_id IN (SELECT node_id FROM is_a_relation) AND
               clp.via_node_id != clp.next_object_id);

CREATE OR REPLACE VIEW redundant_anonymous_node AS
 SELECT * 
 FROM node 
 WHERE is_anonymous=true AND 
  node_id IN (SELECT node_id FROM sameas) AND 
  NOT EXISTS (SELECT * FROM asserted_link WHERE object_id=node.node_id);


-- ************************************************************
-- OWL
-- ************************************************************
-- CREATE SCHEMA owl;
-- SET search_path TO owl,obd_prejoins_view, obd_obo_metamodel_view,obd_core_view,public;

CREATE OR REPLACE VIEW n_subClassOf_n AS
 SELECT
  link_id,
  node_id,
  OBJECT_id AS subClassOf_id,
  source_id
 FROM
  is_a_link;

CREATE OR REPLACE VIEW n_subClassOf_r AS
 SELECT
  link_id,
  node_id,
  link_id AS Restriction_id,
  source_id
 FROM
  non_is_a_basic_link;

CREATE OR REPLACE VIEW n_subClassOf_nr AS
 SELECT
  *
 FROM link
 UNION
 SELECT
  link_id,
  node_id,
  source_id,
  Restriction_id
 FROM n_subClassOf_r;

CREATE OR REPLACE VIEW subClassOf_nr AS
 SELECT
  node.uri,
  n_subClassOf_nr.*
 FROM
  n_subClassOf_nr
  INNER JOIN node USING (node_id);

CREATE OR REPLACE VIEW subClassOf AS
 SELECT
  subClassOf_nr.*,
  x  
 FROM
  subClassOf_nr
  INNER JOIN node USING (node_id);


-- ************************************************************
-- AUDIT INFO
-- ************************************************************
-- CREATE SCHEMA obd_audit_view;
-- SET search_path TO obd_audit_view,public;

CREATE OR REPLACE VIEW node_audit_summary AS
 SELECT
  max(loadtime)  AS max_loadtime,  
  min(loadtime)  AS min_loadtime
 FROM
  node_audit;

CREATE OR REPLACE VIEW node_max_loadtime_by_source AS 
 SELECT source.uid,source.label, max(loadtime) AS max_loadtime
 FROM node LEFT OUTER JOIN node AS source ON (node.source_id=source.node_id)
  INNER JOIN node_audit ON (node.node_id=node_audit.node_id)
 GROUP BY source.uid,source.label;

