package org.obd.query;

import org.obd.model.InstanceQuantifier;
import org.obd.model.vocabulary.TermVocabulary;


/**
 * Base class for OBD query objects.
 * Examples:
 * <code>
 *    QueryTerm qt =
 *    new LinkStatementQueryTerm(partOf,
            new LinkStatementQueryTerm(developsFrom, 
                      new LiteralStatementQueryTerm(AliasType.NAME, "imaginal disc")));
      nodes = shard.getNodesByQuery(qt);
 * </code>
 * 
 * See <a href="http://martinfowler.com/eaaCatalog/queryObject.html">QueryObject</a> pattern
 * @author cjm
 * @see org.obd.model.Statement
 * @see org.obd.query.LinkQueryTerm
 * @see Shard#getLinkStatementsByQuery(QueryTerm)
 * @see Shard#getNodesByQuery(QueryTerm)
 *
 */
public abstract class QueryTerm {
	
	/**
	 * Determines how a nested query is joined
	 * 
	 * 
	 * 
	 * @author cjm
	 *
	 */
	public enum Aspect {SELF, RELATION, TARGET, SOURCE, POSITED_BY};

	protected QueryTerm node;
	protected QueryTerm relation;
	protected QueryTerm source; // source of axiom
	protected QueryTerm nodeSource; // source of the node itself
	protected QueryTerm positedBy;
	protected Boolean isInferred;
	protected Boolean isAnnotation;
	protected InstanceQuantifier instanceQuantifier;
	protected Aspect aspect = Aspect.SELF;
	protected String queryAlias;
	protected TermVocabulary termVocabulary = new TermVocabulary();

	
	public QueryTerm() {
		super();
	}
	
	public QueryTerm(QueryTerm relation, QueryTerm source, QueryTerm positedBy) {
		super();
		this.relation = relation;
		this.source = source;
		this.positedBy = positedBy;
	}
	public QueryTerm getPositedBy() {
		return positedBy;
	}
	public void setPositedBy(QueryTerm positedBy) {
		this.positedBy = positedBy;
	}
	public QueryTerm getSource() {
		return source;
	}
	public void setSource(QueryTerm source) {
		this.source = source;
	}
	public void setSource(String sourceAtom) {
		if (sourceAtom != null)
			this.source = new LabelQueryTerm(sourceAtom);
	}
	
	public QueryTerm getNodeSource() {
		return nodeSource;
	}
	public void setNodeSource(QueryTerm nodeSource) {
		this.nodeSource = nodeSource;
	}
	public void setNodeSource(String nodeSourceAtom) {
		if (nodeSourceAtom != null)
			this.nodeSource = new LabelQueryTerm(nodeSourceAtom);
	}

	public QueryTerm getNode() {
		return node;
	}
	public void setNode(QueryTerm node) {
		this.node = node;
	}
	public void setNode(String nodeAtom) {
		if (nodeAtom != null)
			this.node = new LabelQueryTerm(nodeAtom);
	}
	public QueryTerm getRelation() {
		return relation;
	}
	public void setRelation(QueryTerm relation) {
		this.relation = relation;
	}
	public void setRelation(String relationAtom) {
		if (relationAtom != null)
			this.relation = new LabelQueryTerm(relationAtom);
	}

	public Boolean isInferred() {
		return isInferred;
	}
	public void setInferred(Boolean isInferred) {
		this.isInferred = isInferred;
	}
	
	public Boolean getIsAnnotation() {
		return isAnnotation;
	}

	public void setIsAnnotation(Boolean isAnnotation) {
		this.isAnnotation = isAnnotation;
	}

	public InstanceQuantifier getInstanceQuantifier() {
		return instanceQuantifier;
	}

	public void setInstanceQuantifier(InstanceQuantifier instanceQuantifier) {
		this.instanceQuantifier = instanceQuantifier;
	}

	public Aspect getAspect() {
		return aspect;
	}
	public void setAspect(Aspect aspect) {
		this.aspect = aspect;
	}
	
	public String getQueryAlias() {
		return queryAlias;
	}

	/**
	 * Attaches a label to this part of the query such that it can be
	 * referred to later.
	 * This is also used in SQL generation - the specified alias is used
	 * as the base for creating unique table aliases
	 * @param queryAlias
	 */
	public void setQueryAlias(String queryAlias) {
		this.queryAlias = queryAlias;
	}
	
	public TermVocabulary getTermVocabulary() {
		return termVocabulary;
	}

	public void setTermVocabulary(TermVocabulary termVocabulary) {
		this.termVocabulary = termVocabulary;
	}

	public String toString() {
		String s = "";
		if (positedBy != null)
			s = s + "positedBy:"+positedBy.toString();
		if (isInferred() != null)
			s = s + "inferred:"+isInferred();
		return s;
	}

	/**
	 * returns the leaf node, presuming this query term has a single path-to-leaf.
	 * This is useful for when we have nested constructs such as
	 * Label(Comparison(=,val))
	 * And we want to get straight at 'val'
	 * @return  an atomic value
	 */
	public Object getAtomicValue() {
		return null;
	}
		
}
