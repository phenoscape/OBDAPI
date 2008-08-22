package org.obd.query;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.obd.model.CompositionalDescription;
import org.obd.model.Graph;
import org.obd.model.LinkStatement;
import org.obd.model.LiteralStatement;
import org.obd.model.Node;
import org.obd.model.NodeSet;
import org.obd.model.Statement;
import org.obd.model.rule.InferenceRule;
import org.obd.model.stats.AggregateStatisticCollection;
import org.obd.model.stats.SimilarityPair;
import org.obd.model.stats.ScoredNode;
import org.obd.model.stats.AggregateStatistic.AggregateType;
import org.obd.query.LabelQueryTerm.AliasType;
import org.obd.query.exception.ShardExecutionException;


/**
 * Interface for accessing a repository/repositories of ontology-based annotations
 * <p>
 * Significant implementations:
 * <ul>
 *  <li> {@link org.obd.query.impl.OBDSQLShard} </li> -- database access
 *  <li> {@link org.obd.query.impl.OBOSessionShard} </li> -- in-memory access (persistence via obof or owl files)
 *  <li> {@link org.obd.query.impl.MultiShard} </li> -- mediator access to >1 shard
 *  <li> {@link org.obd.query.impl.RDFShard} </li> -- wraps an OBD triplestore (IN PROGRESS)
 * </ul>
 * 
 * The methods here present essentially two methods of access:
 * <ul>
 *  <li> advanced query based access, via construction of {@link QueryTerm} objects
 *  <li> simple canned template based access
 * </ul>
 * 
 * The latter method presents convenience methods such as
 *  {@link org.obd.query.Shard#getAnnotationStatementsForNode()}
 * which are themselves typically implemented behind the scenes as wrapper calls using the appropriate 
 * query object
 * 
 * 
 * @author cjm
 *
 */
public interface Shard extends BasicRepository, 
  QueryableRepository, SearchableRepository, MutableRepository, AnalysisCapableRepository {

	// TODO: move
	public enum EntailmentUse {
		USE_ASSERTED,
		USE_IMPLIED,
		FULL_TRANSITIVE_CLOSURE
	}
	public enum GraphExpansionAlgorithm {
		MINIMAL,
		INCLUDE_SUBGRAPH
	}


	/**
	 * fetches all nodes in the given source
	 * @return 
	 */
	public Collection<Node> getNodesBySource(String sourceId);

	
	/**
	 * finds all nodes that are not the {@link LinkStatement#getTarget()} target of some {@link LinkStatement}
	 * @param sourceId
	 * @return
	 */
	public Collection<Node> getRootNodes(String sourceId);

	/**
	 * finds all nodes that are not the {@link org.obd.model.LinkStatement#getTarget()} 
	 * target of some {@link org.obd.model.LinkStatement}
	 * via the specified relation
	 * @param sourceId
	 * @return
	 */
	public Collection<Node> getRootNodes(String sourceId, String relationId);

	/**
	 * finds all nodes that are used as the source of some {@link Statement}
	 * @return all Nodes used as a source
	 */
	public Collection<Node> getSourceNodes();

	/**
	 * fins all nodes that are used as the source of some link}
	 * @return all nodes used as a source
	 */
	public Collection<Node> getLinkStatementSourceNodes();
	
	public Collection<Node> getNodeSourceNodes();
	
	/**
	 * finds all Nodes that are beneath each of the nodes with given ids
	 * 
	 * @param ids
	 * @param entailment
	 * @param gea
	 * @return 
	 */
	public Collection<Node> getNodesBelowNodeSet(Collection<String> ids,
			EntailmentUse entailment,
			GraphExpansionAlgorithm gea);



	/**
	 * fetch annotated entities that have at least one annotation to each node
	 * in the given set.
	 * if id in ids, and x annotated to id, then include x
	 * 
	 * @param ids
	 * @param entailment
	 * @param gea
	 * @return all annotated entities that have at least one annotation to each node
	 * in the given set
	 */
	public Collection<Node> getAnnotatedEntitiesBelowNodeSet(Collection<String> ids,
			EntailmentUse entailment,
			GraphExpansionAlgorithm gea);


	/**
	 * fetches all statements from a particular source. All statements, whether
	 * LinkStatements or LiteralStatements, should have some source. This may
	 * be identical to the source of the node, or it may be separate
	 * (for example, it is valid for a non_OMIM source to annotate an OMIM ID).
	 * <p>
	 * Caution: may return multiple results
	 * 
	 * @param sourceId    a unique global identifier for the source. E.g. OMIM
	 * @return all statements that have this source
	 */
	public Collection<Statement> getStatements(String sourceId);

	public Collection<LiteralStatement> getLiteralStatementsByNode(String nodeID);
	
	public Collection<LiteralStatement> getLiteralStatementsByNode(String nodeID, String relationID);
	/**
	 * given an annotated entity, return all annotation
	 * statements
	 * <p>
	 * 
	 * 
	 * @param id  For example: a gene ID or patient ID
	 * @param entailment
	 * @param strategy
	 * @return given an annotated entity (eg a genotype node), return all annotation
	 * statements
	 */
	public Collection<Statement> getAnnotationStatementsForAnnotatedEntity(String id, 
			EntailmentUse entailment, GraphExpansionAlgorithm strategy);

	/**
	 * given an node, return all annotation statements
	 * 
	 * @param id   For example: an ontology class unique ID
	 * @param entailment
	 * @param strategy
	 * @return given an node, return all annotation
	 * statements
	 */
	public Collection<Statement> getAnnotationStatementsForNode(String id, 
			EntailmentUse entailment, GraphExpansionAlgorithm strategy);

	/**
	 * Given a node (eg a class in an ontology), return a graph centered
	 * around this node, including annotation to nodes subsumed by this node
	 * 
	 * @param id For example: an ontology class unique ID
	 * @param entailment
	 * @param gea
	 * @return given a node (eg a class in an ontology), return a graph centered
	 * around this node, including annotation to nodes subsumed by this node
	 */
	public Graph getAnnotationGraphAroundNode(String id, 
			EntailmentUse entailment, GraphExpansionAlgorithm gea);

 

	/**
	 * as getStatementsForNode(id), possibly filtering based on whether the statement is asserted or inferred
	 * @param id
	 * @param isInferred - if explicitly set to false, obly return asserted
	 * @return
	 */
	public Collection<Statement> getStatementsForNode(String id, Boolean isInferred);

	/**
	 * fetches all statements about an entity from a particular source
	 * 
	 * @param id   the unique global identifier for the node that the statement is about
	 * @param sourceId   unique global id for the statement source
	 * @return
	 */
	public Collection<Statement> getStatementsForNodeWithSource(String id, String sourceId);

	
	/**
	 * as getStatementsForTarget(id), possibly filtering based on whether the statement is asserted or inferred
	 * @param targetId
	 * @param isInferred - if explicitly set to false, obly return asserted
	 * @return
	 */
	public Collection<Statement> getStatementsForTarget(String targetId, Boolean isInferred);

	/**
	 * fetches all statements that reference a particular entity, where the statements
	 * come from a particular source
	 * @param targetId
	 * @param sourceId
	 * @return
	 */
	public Collection<Statement> getStatementsForTargetWithSource(String targetId, String sourceId);

	/**
	 * general purpose statement query - parameters may be null, in which case they
	 * match anything
	 * @param nodeId
	 * @param relationId
	 * @param targetId
	 * @param sourceId
	 * @param useImplied
	 * @param isReified
	 * @return
	 */
	public Collection<Statement> getStatements(String nodeId, 
			String relationId, String targetId, String sourceId,
			Boolean useImplied, Boolean isReified);


	/**
	 * Given a node identifier, retrieve the class expression defining this node
	 * @param id
	 * @param traverseNamedClasses
	 * @return null if none found
	 */
	// TODO: return multiple?
	public CompositionalDescription getCompositionalDescription(String id, 
			boolean traverseNamedClasses);




	/**
	 * Perform an aggregate query. Works as a normal query, but performs an
	 * aggregate function 9eg COUNT) on the result
	 * @param queryTerm
	 * @param aggType
	 * @return
	 * @throws Exception
	 */
	public Integer getNodeAggregateQueryResults(QueryTerm queryTerm,
			AggregateType aggType) throws Exception;

	public Integer getLinkAggregateQueryResults(QueryTerm queryTerm,
			AggregateType aggType) throws Exception;


	/**
	 * fetches total number of annotated entities in shard.
	 * An annotated entity is any entity that is represented by a node
	 * that is the target of an annotation statement
	 * 
	 * @return total number of annotated entities
	 */
	public int getAnnotatedNodeCount();
	
	/**
	 * fetches entire contents of shard as a graph. Can be be used in
	 * transfers.
	 * <p>
	 * Caution: may return large graphs
	 * @return
	 */
	public Graph getGraph();
	



	/**
	 * Most shards provide access to the deductive closure of all statements.
	 * Future versions will allow more detailed introspection to determine completeness
	 * w.r.t different language sub-fragments
	 * 
	 * @return whether the shard includes *any* entailed statements
	 */
	public Boolean includesEntailedStatements();

	/**
	 * Given a node identifier, map it to a subset (ontology view).
	 * 
	 * @param id            node to be mapped
	 * @param subsetId      identifier of subset
	 * @return closure of id &isin; subset
	 */
	public Set<String> mapNodeToSubset(String id, String subsetId);
	
	/**
	 * EXPERIMENTAL:
	 * given a query over some ontology, find annotations to classes subsumed by that
	 * query. Result is a map between classes that match the query and the direct
	 * annotation itself
	 * @param queryTerm
	 * @return
	 */
	public Map<String,Collection<LinkStatement>> getAnnotationStatementMapByQuery(QueryTerm queryTerm);
	
	
	/**
	 * Given a set of annotations and a subset/view over an ontology(s), map all
	 * annotations to the subset and then count the number of DISTINCT annotated
	 * entities related to the mapped class. The deductive closure is used here.
	 * <p>
	 * Formally:
	 * count(n) = |e| where e R n
	 * 
	 * Here, e R n denotes that there is some relation R between e and n that
	 * can be deduced from some annotation, plus the axioms in the ontology.
	 * The value of R is by default left unbound; it can be set by passing in
	 * the appropriate annotation query object
	 * 
	 * For example, if the annotations contained:
	 * 
	 *   genotype1   size of Eyeball
	 *   genotype2   thickness of Wall of eyeball
	 *   genotype2   color of Iris
	 *   
	 * Then the count for "Eyeball" would be 2, because both genotypes are related to
	 * "Eyeball" via some chain of relations (specific relations can be selected using
	 * methods on the annotation query. In the example above, part_of is included)
	 * 
	 * Note that if the subset contains the class "Organ", then these annotated
	 * entities would also count for this class
	 * 
	 * @param aqt           query for fetching annotations. If null will select all
	 * @param subsetId      identifier for the subset/view to map to
	 * @return              counts broken down by class node used to annotate
	 * @throws ShardExecutionException 
	 */
	public Map<Node, Integer> getAnnotatedEntityCountBySubset(AnnotationLinkQueryTerm aqt, 
			String subsetId) throws ShardExecutionException;


	/**
	 * called when shard is no longer required
	 */
	public void disconnect();


	public Map<String, NodeSet> createNodeSetMap(Collection<LinkStatement> homolLinks);


	/**
	 * iteratively build graph starting from seeds
	 * @param seedIds
	 * @param relationIds
	 * @return
	 */
	public Graph getGraphFromSeeds(Collection<String> seedIds, Collection<String> relationIds);


	/**
	 * starting from seed, iteratively build a transitive closure, adding links to the graph object.
	 * 
	 * @param graph - all links are added to this graph
	 * @param seedId - initial starting node
	 * @param relIds - relations to traverse, in a subject->target direction
	 * @param reverseRelIds - relations to traverse, in a target->subject direction
	 */
	public void simpleClosureOver(Graph graph, String seedId, Collection<String> relIds, 
			Collection<String> reverseRelIds);

	/**
	 * given a collection of inference rules, realize the statements that are entailed by those rules
	 * @param rules
	 */
	public void realizeRules(Collection<InferenceRule> rules);
	

	/**
	 * get all links (implied+asserted) in which ids are subjects
	 * @param ids
	 * @param relId - may be null, in which case all relations are used
	 * @return
	 */
	public Collection<LinkStatement> getClosure(Collection<String> ids, String relId);




}
