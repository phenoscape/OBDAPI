package org.obd.query;

import java.util.Collection;

import org.obd.model.LiteralStatement;
import org.obd.model.Node;
import org.obd.model.Statement;

public interface BasicRepository {
	/**
	 * @param  id      unique global identifier for node. E.g. purl URI or CL:0000148
	 * @return Node with matching ID
	 */
	public Node getNode(String id);


	/**
	 * fetches all nodes in this shard
	 * <p>
	 * caution : may return large numbers of nodes.
	 * This method call is useful for dumps or cloning shards
	 * 
	 * @return all nodes in shard
	 */
	public Collection<Node> getNodes();

	/**
	 * fetches all statements about an entity, identified by some node ID
	 * <p>
	 * Here "about" corresponds to "subject" in RDF terminology. Each Statement
	 * has a subject -- what the statement is about.
	 * @param id   the unique global identifier for the node that the statement is about
	 * @return
	 */
	public Collection<Statement> getStatementsByNode(String id);
	
	/**
	 * fetches all statements that reference a particular entity
	 * <p>
	 * Note: may be upgraded to return LinkStatements
	 * @param targetId
	 * @return
	 */
	public Collection<Statement> getStatementsForTarget(String targetId);


	
	
}
