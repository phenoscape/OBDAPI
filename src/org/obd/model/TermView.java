package org.obd.model;

import java.util.Collection;
import java.util.HashSet;

import org.obd.model.vocabulary.TermVocabulary;

/**
 *
 * Provides a terminological view over a Graph
 * <p>
 * this is essentially a facade that hides the terminology metamodel
 * 
 * NOTE: may move to separate package
* @author cjm
  */
public class TermView {

	private Graph graph;
	private TermVocabulary vocab = new TermVocabulary();

	public TermView() {
		super();
	}

	public TermView(Graph graph) {
		super();
		this.graph = graph;
	}

	public Graph getGraph() {
		return graph;
	}

	public void setGraph(Graph graph) {
		this.graph = graph;
	}
	
	public Collection<LiteralStatement> getTextualDefinitionStatements(String id) {
		Collection<LiteralStatement> stmts = new HashSet<LiteralStatement>();
		for (LiteralStatement s : graph.getAllLiteralStatementsForNode(id)) {
			if (s.getRelationId().equals(vocab.HAS_DEFINITION()))
				stmts.add(s);
		}
		return stmts;
	}
	public Collection<LinkStatement> getSubsetStatements(String id) {
		Collection<LinkStatement> stmts = new HashSet<LinkStatement>();
		for (LinkStatement s : graph.getAllLinkStatementsForNode(id)) {
			if (s.getRelationId().equals(vocab.IN_SUBSET()))
				stmts.add(s);
		}
		return stmts;
	}
	public Collection<LinkStatement> getDbXrefStatements(String id) {
		Collection<LinkStatement> stmts = new HashSet<LinkStatement>();
		for (LinkStatement s : graph.getAllLinkStatementsForNode(id)) {
			if (s.getRelationId().equals(vocab.HAS_DBXREF()))
				stmts.add(s);
		}
		return stmts;
	}
	public Collection<LiteralStatement> getSynonymStatements(String id) {
		Collection<LiteralStatement> stmts = new HashSet<LiteralStatement>();
		for (LiteralStatement s : graph.getAllLiteralStatementsForNode(id)) {
			if (vocab.isSynonym(s.getRelationId()))
				stmts.add(s);
		}
		return stmts;
	}
	
}
