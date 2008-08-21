package org.obd.test;

import java.util.Collection;

import junit.framework.Test;
import junit.framework.TestSuite;

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
public class RDFShardPutTest extends PutExternalAnnotationsTest {


	public RDFShardPutTest(String n) {
		super(n);
	}
	
	public void setUp() throws Exception {
		System.out.println("Setting up: " + this);
		//getShard();
		shard = new RDFShard();
		initLogger();
	}

	public static void addTests(TestSuite suite) {
		suite.addTest(new RDFShardPutTest("testWriteRDF"));
	}
	
	public static Test suite() {
		TestSuite suite = new TestSuite();
		addTests(suite);
		return suite;
	}


	/**
	 * copies links from core test Shard into a temporary rdf shard, then queries it
	 * @throws Exception 
	 */
	public void testWriteRDF() throws Exception {

		putGeneTest();

		String sox10 = "NCBI_Gene:140616";
		//RDFQuery q = new RDFQuery(new LinkQueryTerm(NEURON));
		LinkQueryTerm qt = new LinkQueryTerm(sox10);
		
		RDFQuery q = new RDFQuery(qt);
		//RDFQuery q = new RDFQuery(new LinkQueryTerm(null, null, NEUROBLAST));
		String sparql = q.toSPARQL();
		String sv = q.getSelectVar().getName();
		executeSparql(sparql,new String[] {sv });
		//executeSparql("SELECT ?x ?y {?x <http://www.w3.org/2000/01/rdf-schema#subClassOf>  ?y}",new String[] {"x","y"});
		//executeSparql("SELECT ?x ?r ?y {?x <http://www.w3.org/2000/01/rdf-schema#subClassOf>  ?z . ?z <http://www.w3.org/2002/07/owl#onProperty> ?r . ?z <http://www.w3.org/2002/07/owl#someValuesFrom> ?y}",new String[] {"x","r","y"});
		//executeSparql("SELECT ?x ?r ?y {?x <http://www.w3.org/2000/01/rdf-schema#subClassOf>  ?z . ?z <http://www.w3.org/2002/07/owl#onProperty> ?r . ?z <http://www.w3.org/2002/07/owl#someValuesFrom> ?y}",new String[] {"x","r","y"});
		Collection<Statement> stmts = shard.getStatementsForNode(sox10);
		for (Statement s : stmts) {
			System.out.println(s);
		}

		RDFShard rdfShard = (RDFShard)shard;
		OntModel m = rdfShard.getModel();

		m.write(System.out);
	}

		protected void executeSparql(String sparql, String[] svs) {
			System.out.println(sparql);
			Query query = QueryFactory.create(sparql);
			RDFShard rdfShard = (RDFShard)shard;

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
