package org.obd.query;

import java.util.Collection;

import org.obd.model.CompositionalDescription;
import org.obd.model.Graph;
import org.obd.model.Node;
import org.obd.model.Statement;
import org.obd.query.LabelQueryTerm.AliasType;
import org.obd.query.Shard.EntailmentUse;
import org.obd.query.Shard.GraphExpansionAlgorithm;
import org.obd.query.exception.ShardExecutionException;

public interface AnnotationRepository {
	

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

}
