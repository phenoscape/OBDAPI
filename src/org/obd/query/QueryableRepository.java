package org.obd.query;

import java.util.Collection;

import org.obd.model.Graph;
import org.obd.model.LinkStatement;
import org.obd.model.LiteralStatement;
import org.obd.model.Node;
import org.obd.model.Statement;
import org.obd.query.Shard.EntailmentUse;
import org.obd.query.Shard.GraphTranslation;

public interface QueryableRepository {


	/**
	 * given a query, find matching nodes from shard
	 * 
	 * @param queryTerm
	 * @return non-null Collection of any kind of Statement
	 * @see org.obd.test.QueryTest
	 */
	public Collection<Node> getNodesByQuery(QueryTerm queryTerm);

	/**
	 * given a query, find matching statements from shard
	 * <p>
	 * Example:
	 * 		LinkQueryTerm partOfNucleusQuery = new LinkQueryTerm(partOf, 
	 *			new LabelQueryTerm(AliasType.ANY_LABEL, "nucleus"));
	 *		stmts = shard.getStatementsByQuery(new AnnotationLinkQueryTerm(partOfNucleusQuery));
	 * 
	 * @param queryTerm
	 * @return non-null Collection of any kind of Statement
	 * @see org.obd.test.QueryTest
	 */
	public Collection<Statement> getStatementsByQuery(QueryTerm queryTerm);
	
	/**
	 * As {@link #getStatementsByQuery(QueryTerm)}, but only fetch statements
	 * linking two nodes
	 * @param queryTerm
	 * @return
	 */
	
	public Collection<LinkStatement> getLinkStatementsByQuery(QueryTerm queryTerm);
	/**
	 * As {@link #getStatementsByQuery(QueryTerm)}, but only fetch statements
	 * linking a node with a literal value
	 * @param queryTerm
	 * @return
	 */	
	public Collection<LiteralStatement> getLiteralStatementsByQuery(QueryTerm queryTerm);
	
	public Graph getGraphByQuery(QueryTerm queryTerm, 
			EntailmentUse entailment, GraphTranslation gea);
  

}
