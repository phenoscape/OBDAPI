package org.obd.query;

import java.util.Collection;

import org.obd.model.Node;
import org.obd.model.Statement;
import org.obd.query.LabelQueryTerm.AliasType;

public interface SearchableRepository {
	/**
	 * finds all matching nodes
	 * <p>
	 * as {@link #getNodesBySearch(String, org.obd.query.ComparisonQueryTerm.Operator)}
	 * defaults to Operator.MATCHES
	 * 
	 * @param searchTerm
	 * @return all matching Nodes
	 * @throws Exception 
	 */
	public Collection<Node> getNodesBySearch(String searchTerm) throws Exception;
	

	/**
	 * finds all nodes that have an identifier, label or alias matching the searchTerm
	 * 
	 * @param searchTerm
	 * @param operator
	 * @return all matching Nodes
	 * @throws Exception 
	 */
	public Collection<Node> getNodesBySearch(String searchTerm,	ComparisonQueryTerm.Operator operator) throws Exception;

	/**
	 * finds all nodes that have an identifier, label or alias matching the searchTerm having the specified source
	 * 
	 * @param searchTerm
	 * @param operator
	 * @param source
	 * @return all matching Nodes
	 * @throws Exception 
	 */
	public Collection<Node> getNodesBySearch(String searchTerm, ComparisonQueryTerm.Operator operator, String source, AliasType at) throws Exception;
	

}
