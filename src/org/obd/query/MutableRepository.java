package org.obd.query;

import java.util.Collection;

import org.obd.model.CompositionalDescription;
import org.obd.model.Graph;
import org.obd.model.Node;
import org.obd.model.Statement;
import org.obd.query.LabelQueryTerm.AliasType;
import org.obd.query.exception.ShardExecutionException;

public interface MutableRepository {
	/**
	 * copies an entire set of nodes and statements into this shard
	 * <p>
	 * Must have write access
	 * @param graphToInsert
	 */
	public void putGraph(Graph graphToInsert);

	public void putNode(Node n) ;
	public void putStatement(Statement s);

	/**
	 * Removes the specified source from the Shard.
	 * Warning: not guaranteed to work transactionally
	 * @param srcId
	 */
	public void removeSource(String srcId);
	
	/**
	 * removes a node from the Shard store.
	 * Depending on the shard implementation, this may have a cascading effect
	 * @param nodeId
	 * @throws ShardExecutionException
	 */
	public void removeNode(String nid) throws ShardExecutionException ;


	/**
	 * Merges links to two identifiers.
	 * It is common that there may be two IDs for the same entity, typically across ID spaces. This method merges links from
	 * one of those ID spaces into the other.
	 * @param fromIdSpace -- e.g. NCBI_Gene
	 * @param toIdSpace -- e.g. ZFIN
	 * @throws ShardExecutionException
	 */
	public void mergeIdentifierByIDSpaces(String fromIdSpace, String toIdSpace) throws ShardExecutionException;

	/**
	 * copies a CompositionalDescription into this shard
	 * @param desc
	 */
	public void putCompositionalDescription(CompositionalDescription desc);

	/**
	 * removes all statements with matching su-rel-ob from shard
	 * @param su
	 * @param rel
	 * @param ob
	 * @throws ShardExecutionException 
	 */
	public void removeMatchingStatements(String su, String rel, String ob) throws ShardExecutionException;

	/**
	 * performs {@link #getGraph()} on sourceShard, then {@link #putGraph(Graph)}
	 * on this shard
	 * @param sourceShard
	 */
	public void transferFrom(Shard sourceShard);

}
