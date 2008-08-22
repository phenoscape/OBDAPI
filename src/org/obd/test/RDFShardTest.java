package org.obd.test;

import java.util.Collection;

import org.obd.model.Graph;
import org.obd.model.Statement;
import org.obd.model.bridge.RDFQuery;
import org.obd.query.LinkQueryTerm;
import org.obd.query.impl.RDFShard;

import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.RDFNode;

/**
 * tests bridge capabilities for exporting annotations as OWL
 * @author cjm
 *
 */
public class RDFShardTest extends AbstractOBDTest {

	RDFShard rdfShard = 
		new RDFShard();

	public RDFShardTest(String n) {
		super(n);
	}
	


	/**
	 * copies links from core test Shard into a temporary rdf shard, then queries it
	 */
	public void testWriteRDF() {

		//Graph graph = shard.getAnnotationGraphAroundNode("CL:0000148", null, null);
		Collection<Statement> stmts = shard.getStatementsByNode(NEURON);
		Graph graph = new Graph(stmts);
		System.out.println("** to:");
		Collection<Statement> toStmts = shard.getStatementsForTarget(NEURON);
		graph.addStatements(toStmts);
		rdfShard.putGraph(graph);

		//RDFQuery q = new RDFQuery(new LinkQueryTerm(NEURON));
		RDFQuery q = new RDFQuery(new LinkQueryTerm(NEUROBLAST));
		//RDFQuery q = new RDFQuery(new LinkQueryTerm(null, null, NEUROBLAST));
		String sparql = q.toSPARQL();
		String sv = q.getSelectVar().getName();
		executeSparql(sparql,new String[] {sv });
		//executeSparql("SELECT ?x ?y {?x <http://www.w3.org/2000/01/rdf-schema#subClassOf>  ?y}",new String[] {"x","y"});
		//executeSparql("SELECT ?x ?r ?y {?x <http://www.w3.org/2000/01/rdf-schema#subClassOf>  ?z . ?z <http://www.w3.org/2002/07/owl#onProperty> ?r . ?z <http://www.w3.org/2002/07/owl#someValuesFrom> ?y}",new String[] {"x","r","y"});
		//executeSparql("SELECT ?x ?r ?y {?x <http://www.w3.org/2000/01/rdf-schema#subClassOf>  ?z . ?z <http://www.w3.org/2002/07/owl#onProperty> ?r . ?z <http://www.w3.org/2002/07/owl#someValuesFrom> ?y}",new String[] {"x","r","y"});
		Collection<Statement> stmts2 = rdfShard.getStatementsByNode(MELANOCYTE);
		for (Statement s : stmts2) {
			System.out.println(s);
		}

		OntModel m = rdfShard.getModel();

		m.write(System.out);
		}

		protected void executeSparql(String sparql, String[] svs) {
			System.out.println(sparql);
			Query query = QueryFactory.create(sparql);
			QueryExecution qe = QueryExecutionFactory.create(query, rdfShard.getModel());
			ResultSet results = qe.execSelect();
			// Model resultModel = qexec.execConstruct() ;
			for ( ; results.hasNext() ; )
			{
				QuerySolution soln = results.nextSolution() ;
				System.out.println("result="+soln);
				for (String sv : svs) {
					RDFNode x = soln.get(sv) ;       // Get a result variable by name.
					System.out.println(sv+" = "+x);
				}


			}
		}

	}
