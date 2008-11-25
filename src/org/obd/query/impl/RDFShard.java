package org.obd.query.impl;

import java.io.UnsupportedEncodingException;
import java.util.Collection;

import org.obd.model.CompositionalDescription;
import org.obd.model.Graph;
import org.obd.model.LinkStatement;
import org.obd.model.LiteralStatement;
import org.obd.model.Node;

import org.obd.model.Statement;
import org.obd.model.stats.AggregateStatisticCollection;
import org.obd.model.stats.AggregateStatistic.AggregateType;
import org.obd.query.AtomicQueryTerm;
import org.obd.query.ComparisonQueryTerm;
import org.obd.query.LinkQueryTerm;
import org.obd.query.QueryTerm;
import org.obd.query.Shard;
import org.obd.query.ComparisonQueryTerm.Operator;
import org.obd.query.LabelQueryTerm.AliasType;
import org.obd.query.exception.ShardExecutionException;
import org.obd.query.impl.RDFShard.ModelingStrategy.InvalidModelinStrategyException;
import org.obo.owl.dataadapter.OWLAdapter;
import org.purl.obo.vocab.RelationVocabulary;

import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.ontology.AllValuesFromRestriction;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.Restriction;
import com.hp.hpl.jena.ontology.SomeValuesFromRestriction;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.ReifiedStatement;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.sparql.algebra.Algebra;
import com.hp.hpl.jena.sparql.algebra.Op;
import com.hp.hpl.jena.sparql.algebra.op.OpBGP;
import com.hp.hpl.jena.sparql.algebra.op.OpFilter;
import com.hp.hpl.jena.sparql.core.BasicPattern;
import com.hp.hpl.jena.sparql.core.Var;
import com.hp.hpl.jena.sparql.engine.QueryIterator;
import com.hp.hpl.jena.sparql.expr.E_LessThan;
import com.hp.hpl.jena.sparql.expr.Expr;
import com.hp.hpl.jena.sparql.expr.ExprVar;
import com.hp.hpl.jena.sparql.expr.NodeValue;
import com.hp.hpl.jena.vocabulary.RDFS;

/**
 * Implements a Shard by wrapping an RDF Store, using Jena.
 * This class should not be exposed in the API; use the Shard interface.
 * 
 * An RDFShard works by taking Shard method calls and {@link QueryTerm} objects and translating
 * these to SPARQL queries - see {@link RDFQuery}
 * 
 * This means that clients have different options for accessing an OBD DataStore; they can use the
 * OBDAPI stack (java API calls on Shard objects, or RESTful URLs for accessing OBDXML) or they can
 * use the semantic web stack (Jena API calls or SPARQL queries, for example via a Joseki-hosted
 * SPARQL endpoint). This is analagous to the situation with the {@link org.obd.query.impl.OBDSQLShard},
 * where SQL can be used. 
 *  
 * @author cjm
 *
 */
public class RDFShard extends AbstractShard implements Shard {

	/**
	 * An RDFShard can implement a variety of modeling strategies. See 
	 * 
	 * @author cjm
	 *
	 */
	public class ModelingStrategy {
		private boolean modelTBoxInABox = false;
		private boolean modelAnnotationsUsingReification = true;
		private boolean alwaysAssertReifiedStatement = true;
		private boolean modelAnnotationsAsTriples = false;
		private boolean modelClassAnnotationsAsAnonymousIndividuals = true;
		private boolean modelClassAnnotationsAsAxioms = true;
		private boolean modelClassAnnotationsAsOWLDescriptions = false;

		/**
		 * Using this mapping, all ontology relations are treated as simple triples;
		 * this means OWL axioms such as HeartVentricle SubClassOf (part_of Heart) are
		 * treated as triples: HeartVentricle part_f Heart
		 * 
		 * Background:
		 * Normally, each subClassOf OWL axiom  is converted to a single RDF triple, but 
		 * other relations follow a different pattern. OWL has no notion of �edges� in an ontology graph as such. 
		 * Instead, edges such as �HeartVentricle part_of Heart� (taken from the adult mouse anatomy ontology)
		 *  are treated as subclass axioms between (a) a named class and (b) an owl description .
		 *  Here the description is an owl:Restriction representing the class of all things that are part of the heart.
		 *  When we convert a part_of edge to RDF via OWL we end up with 4 triples. In turtle syntax:
		 *
		 * <code>
		 * HeartVentricle rdfs:subClassOf 
		 *  [a owl:Restriction ; 
		 *    onProperty: ro:part_of ;
		 *    someValuesFrom: Heart ]
		 * </code>
		 * Note the part in []s denotes an RDF bNode. The Restriction is (typically) unnamed. 
		 *
		 * The low level RDF details are hidden from us if we use a Jena ontModel;
		 * however, the details are not hidden at the SPARQL level. This means that
		 * if this pattern is not used, SPARQL queries must be expanded to use
		 * the full OWL-in-RDF encoding
		 * 
		 * 
		 *
		 * @return
		 */
		public boolean isModelTBoxInABox() {
			return modelTBoxInABox;
		}

		public void setModelTBoxInABox(boolean useTBoxInABoxPattern) {
			this.modelTBoxInABox = useTBoxInABoxPattern;
		}

		/**
		 * if this is true then any reified statement
		 * ?st subject ?su
		 * ?st predicate ?pr
		 * ?st object ?ob
		 * 
		 * is always accompanied by a triple
		 * ?su ?pr ?ob
		 * 
		 * if false, then the accompanying triple is optional
		 * @return
		 */
		public boolean isAlwaysAssertReifiedStatement() {
			return alwaysAssertReifiedStatement;
		}

		public void setAlwaysAssertReifiedStatement(boolean alwaysAssertReifiedStatement) {
			this.alwaysAssertReifiedStatement = alwaysAssertReifiedStatement;
		}

		/**
		 * if true, a simple triple pattern is always used for annotations,
		 * even class-class annotations.
		 * This can lead to OWL-Full
		 * @return
		 */
		public boolean isModelAnnotationsAsTriples() {
			return modelAnnotationsAsTriples;
		}

		public void setModelAnnotationsAsTriples(boolean modelAnnotationsAsTriples) {
			this.modelAnnotationsAsTriples = modelAnnotationsAsTriples;
		}

		/**
		 * if true, provenance metadata is attached to annotation links via reification.
		 * If false, named graphs are used
		 * @return
		 */
		public boolean isModelAnnotationsUsingReification() {
			return modelAnnotationsUsingReification;
		}

		public void setModelAnnotationsUsingReification(
				boolean modelAnnotationsUsingReification) {
			this.modelAnnotationsUsingReification = modelAnnotationsUsingReification;
		}

		public boolean isModelClassAnnotationsAsAnonymousIndividuals() {
			return modelClassAnnotationsAsAnonymousIndividuals;
		}

		public void setModelClassAnnotationsAsAnonymousIndividuals(
				boolean modelClassAnnotationsAsAnonymousIndividuals) {
			this.modelClassAnnotationsAsAnonymousIndividuals = modelClassAnnotationsAsAnonymousIndividuals;
		}

		public boolean isModelClassAnnotationsAsAxioms() {
			return modelClassAnnotationsAsAxioms;
		}

		public void setModelClassAnnotationsAsAxioms(
				boolean modelClassAnnotationsAsAxioms) {
			this.modelClassAnnotationsAsAxioms = modelClassAnnotationsAsAxioms;
		}

		public boolean isModelClassAnnotationsAsOWLDescriptions() {
			return modelClassAnnotationsAsOWLDescriptions;
		}

		public void setModelClassAnnotationsAsOWLDescriptions(
				boolean modelClassAnnotationsAsOWLDescriptions) {
			this.modelClassAnnotationsAsOWLDescriptions = modelClassAnnotationsAsOWLDescriptions;
		}

		public class InvalidModelinStrategyException extends Exception {

			public InvalidModelinStrategyException(String string) {
				// TODO Auto-generated constructor stub
			}

		}

		private void validate() throws InvalidModelinStrategyException {
			if (isModelClassAnnotationsAsOWLDescriptions() &&
					isModelAnnotationsUsingReification())
				throw new InvalidModelinStrategyException("isModelClassAnnotationsAsOWLDescriptions incompatible with isModelAnnotationsUsingReification");
		}

	}

	private OntModel model = ModelFactory.createOntologyModel();
	private RelationVocabulary relVocab = new RelationVocabulary();
	private String BASE = "http://purl.org/obo/owl#" ; 
	private OWLAdapter owlAdapter = new OWLAdapter(); // TODO - temporary - for ID to URI
	private ModelingStrategy modelingStrategy = new ModelingStrategy();


	public OntModel getModel() {
		return model;
	}

	public void setModel(OntModel model) {
		this.model = model;
	}

	public void tq(QueryTerm qt, BasicPattern bp) {
		if (qt instanceof LinkQueryTerm) {
			LinkQueryTerm lqt = (LinkQueryTerm)qt;
			Triple t = new Triple(null, null, null);
			//tq
		}
	}

	public void test() {


		BasicPattern bp = new BasicPattern() ;
		Var var_x = Var.alloc("x") ;

		Var var_z = Var.alloc("z") ;



		// ---- Build expression
		com.hp.hpl.jena.graph.Node uri = 
			com.hp.hpl.jena.graph.Node.createURI(BASE+"p");
		bp.add(new Triple(var_x,  uri, var_z)) ;

		Op op = new OpBGP(bp) ;

		Expr expr = new E_LessThan(new ExprVar(var_z), NodeValue.makeNodeInteger(2)) ;

		op = OpFilter.filter(expr, op) ;

		QueryIterator qi = Algebra.exec(op, model);
	}

	public com.hp.hpl.jena.graph.Node getJenaNode(String id) {
		return com.hp.hpl.jena.graph.Node.createURI(BASE+id);
	}

	public Collection<Node> getAnnotatedEntitiesBelowNodeSet(
			Collection<String> ids, EntailmentUse entailment,
			GraphTranslation gea) {
		// TODO Auto-generated method stub
		return null;
	}

	public int getAnnotatedNodeCount() {
		// TODO Auto-generated method stub
		return 0;
	}

	public Graph getAnnotationGraphAroundNode(String id,
			EntailmentUse entailment, GraphTranslation gea) {
		// TODO Auto-generated method stub
		return null;
	}

	public Collection<Statement> getAnnotationStatementsForAnnotatedEntity(
			String id, EntailmentUse entailment,
			GraphTranslation strategy) {
		// TODO Auto-generated method stub
		return null;
	}

	public Collection<Statement> getAnnotationStatementsForNode(String id,
			EntailmentUse entailment, GraphTranslation strategy) {
		// TODO Auto-generated method stub
		return null;
	}

	public CompositionalDescription getCompositionalDescription(String id,
			boolean traverseNamedClasses) {
		// TODO Auto-generated method stub
		return null;
	}

	public Graph getGraphByQuery(QueryTerm queryTerm, EntailmentUse entailment,
			GraphTranslation gea) {
		// TODO Auto-generated method stub
		return null;
	}

	public Integer getLinkAggregateQueryResults(QueryTerm queryTerm,
			AggregateType aggType) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	public Node getNode(String id) {
		// TODO Auto-generated method stub
		return null;
	}

	public Integer getNodeAggregateQueryResults(QueryTerm queryTerm,
			AggregateType aggType) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	public Collection<Node> getNodes() {
		// TODO Auto-generated method stub
		return null;
	}

	public Collection<Node> getNodesBelowNodeSet(Collection<String> ids,
			EntailmentUse entailment, GraphTranslation gea) {
		// TODO Auto-generated method stub
		return null;
	}

	public Collection<Node> getNodesByQuery(QueryTerm queryTerm) {
		// TODO Auto-generated method stub
		return null;
	}

	public Collection<Node> getNodesBySearch(String searchTerm,	Operator operator, String source, AliasType at) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	public Collection<Node> getNodesBySource(String sourceId) {
		// TODO Auto-generated method stub
		return null;
	}

	public Collection<Node> getSourceNodes() {
		// TODO Auto-generated method stub
		return null;
	}

	public Collection<Statement> getStatements(String sourceId) {
		// TODO Auto-generated method stub
		return null;
	}

	public Collection<Statement> getStatements(String nodeId,
			String relationId, String targetId, String sourceId,
			Boolean useImplied, Boolean isReified) {
		// TODO Auto-generated method stub
		return null;
	}

	public Collection<Statement> getStatementsByQuery(QueryTerm queryTerm) {
		// TODO Auto-generated method stub
		return null;
	}

	public Collection<Statement> getStatementsByNode(String id) {
		// TODO Auto-generated method stub
		return null;
	}

	public Collection<Statement> getStatementsForNodeWithSource(String id,
			String sourceId) {
		// TODO Auto-generated method stub
		return null;
	}

	public Collection<Statement> getStatementsForTarget(String targetId) {
		// TODO Auto-generated method stub
		return null;
	}

	public Collection<Statement> getStatementsForTargetWithSource(
			String targetId, String sourceId) {
		// TODO Auto-generated method stub
		return null;
	}

	public AggregateStatisticCollection getSummaryStatistics() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.obd.query.Shard#putGraph(org.obd.model.Graph)
	 * 
	 * given an OBD Graph, translate all statements to triples
	 * and add them to the current Jena model
	 */
	public void putGraph(Graph obdGraph) {
		for (Statement s : obdGraph.getAllStatements()) {
			//System.out.println("putting: "+s);
			putStatementToJena(s);
		}
	}

	public void putNode(Node n) {
		try {
			String label = n.getLabel();
			Resource rdfSu = createResource(n.getId());

			if (label != null) {
				Literal lit = model.createTypedLiteral(label);
				com.hp.hpl.jena.rdf.model.Statement rdfStmt = model.createLiteralStatement(rdfSu, RDFS.label, lit);
				if (rdfStmt != null)
					model.add(rdfStmt);

			}
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	public void putStatement(Statement s) {
		putStatementToJena(s);
	}

	private com.hp.hpl.jena.rdf.model.Statement putStatementToJena(Statement s) {
		return putStatementToJena(null,s);
	}
	private com.hp.hpl.jena.rdf.model.Statement putStatementToJena(Resource rdfSu, Statement s) {
		String su = s.getNodeId();
		String rel = s.getRelationId();
		String ob = s.getTargetId();
		try {
			if (rdfSu == null)
				rdfSu = createResource(su);
			Property rdfProp = createProperty(rel);
			com.hp.hpl.jena.rdf.model.Statement rdfStmt = null;

			if (s.isNegated()) {
				// TODO
			}


			if (s.isAppliesToAllInstancesOf() && !modelingStrategy.isModelTBoxInABox() &&
					!rdfProp.equals(RDFS.subClassOf)) {
				Resource rdfOb;
				rdfOb = createResource(ob);
				if (s.isExistential()) {
					SomeValuesFromRestriction restr = model.createSomeValuesFromRestriction(null, rdfProp, rdfOb);
					rdfStmt = model.createStatement(rdfSu,RDFS.subClassOf,restr);
					//model.add(rdfStmt);
					addSubStatements(s,rdfStmt);
				}
				if (s.isUniversal()) {
					AllValuesFromRestriction restr = model.createAllValuesFromRestriction(null, rdfProp, rdfOb);
					rdfStmt = model.createStatement(rdfSu,RDFS.subClassOf,restr);
					//model.add(rdfStmt);
					addSubStatements(s,rdfStmt);
				}

			}
			else {
				// simple triple
				if (s instanceof LiteralStatement) {
					// TODO

					LiteralStatement ls = (LiteralStatement) s;
					Literal lit = model.createTypedLiteral(ls.getSValue());
					//model.addLiteral(rdfSu, rdfProp, ls.getValue());
					//rdfStmt = 
					//	model.createLiteralStatement(rdfSu, rdfProp, lit);
					//addSubStatements(s,rdfStmt);


				}
				else {
					Resource rdfOb = createResource(ob);
					rdfStmt = 
						model.createStatement(rdfSu, rdfProp, rdfOb);
					addSubStatements(s,rdfStmt);
				}
			}
			if (rdfStmt != null) {
				System.out.println(rdfStmt);
				model.add(rdfStmt);
			}
			return rdfStmt;
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null; // TODO
		}

	}

	private void addSubStatements(Statement s, com.hp.hpl.jena.rdf.model.Statement rdfStmt) {
		if (s.getSubStatements().size() > 0) {
			ReifiedStatement reifRdfStmt = model.createReifiedStatement(rdfStmt);
			for (Statement ss: s.getSubStatements()) {
				putStatementToJena(reifRdfStmt, ss);
			}
		}


	}

	private void jenaModelToOBDGraph(Model model, Graph g) {
		StmtIterator it = model.listStatements();
		while (it.hasNext()) {
			com.hp.hpl.jena.rdf.model.Statement triple = it.nextStatement();
			String su = getId(triple.getSubject());
			String rel = getId(triple.getPredicate());
			String ob = getId(triple.getObject());
			Statement s;
			s = new LinkStatement(su,rel,ob);
			if (triple.isReified()) {
				// TODO
			}
			g.addStatement(s);
		}
	}

	private String getId(RDFNode node) {
		return node.toString();
	}

	public Resource createResource(String id) throws UnsupportedEncodingException {
		if (id.equals(relVocab.is_a())) {
			// TODO - DRY - repeated below. 
			return RDFS.subClassOf;
		}
		return model.createResource(owlAdapter.getURI(id).toString()); // TODO
	}

	private Property createProperty(String rel) throws UnsupportedEncodingException {
		if (rel.equals(relVocab.is_a())) {
			return RDFS.subClassOf;
		}
		return model.createProperty(owlAdapter.getURI(rel).toString()); // TODO
	}

	private String convertIdentifierToURIString(String id) throws UnsupportedEncodingException {
		if (id.equals(relVocab.is_a())) {
			return RDFS.subClassOf.toString();
		}
		return owlAdapter.getURI(id).toString();
	}

	public Collection<Node> getLinkStatementSourceNodes() {
		System.err.println("ERROR: getLinkStatementSourceNodes() not yet implemented.");
		return null;
	}

	public Collection<Node> getNodeSourceNodes() {
		System.err.println("ERROR: getNodeSourceNodes() not yet implemented.");
		return null;
	}


	public void removeNode(String nid) throws ShardExecutionException {
		// TODO Auto-generated method stub

	}

	@Override
	public Collection<Statement> getStatementsWithSearchTerm(String node, 
			String relation, String target, String source,
			Boolean useImplied, Boolean isReified) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Collection<Node> getNodesForSearchTermByLabel(String searchTerm) {
		// TODO Auto-generated method stub
		return null;
	}



}



