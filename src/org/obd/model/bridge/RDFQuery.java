package org.obd.model.bridge;

import java.io.UnsupportedEncodingException;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import org.obd.query.AnnotationLinkQueryTerm;
import org.obd.query.AtomicQueryTerm;
import org.obd.query.ComparisonQueryTerm;
import org.obd.query.ExistentialQueryTerm;
import org.obd.query.LabelQueryTerm;
import org.obd.query.LinkQueryTerm;
import org.obd.query.QueryTerm;
import org.obd.query.ComparisonQueryTerm.Operator;
import org.obd.query.LabelQueryTerm.AliasType;
import org.obd.query.impl.RDFShard;
import org.purl.obo.vocab.RelationVocabulary;

import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.vocabulary.OWL;
import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.vocabulary.RDFS;


/**
 * Lightweight query object for representing a query over an RDF model that can be translated to SPARQL.
 * 
 * Has methods for translating from an OBD query object
 * 
 * <a href="http://www.bioontology.org/wiki/index.php/OBD:SPARQL-Examples">SPARQL Examples<a>
 * 
 * @author cjm
 * @see org.obd.test.RDFQueryTest
 * 
 */
public class RDFQuery {

	/**
	 * RDF variable
	 * @author cjm
	 *
	 */
	public class RDFVar {
		private Resource resource;
		private String name;
		
		

		public RDFVar() {
			super();
		}
		
		

		public RDFVar(Resource resource) {
			super();
			this.resource = resource;
		}

		public Resource getResource() {
			return resource;
		}

		public void setResource(Resource resource) {
			this.resource = resource;
		}
		public void setResource(String resource) {
			setResource(createResource(resource));
		}
		

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		public String toString() {
			if (resource == null) {
				return "?"+name;
			}
			else {
				return "<"+resource.toString()+">";
			}
		}

	}

	public class Triple {
		private RDFVar subject;
		private RDFVar predicate;
		private RDFVar object;
		public Triple(RDFVar subject, RDFVar predicate, RDFVar object) {
			super();
			this.subject = subject;
			this.predicate = predicate;
			this.object = object;
		}
		public RDFVar getObject() {
			return object;
		}
		public RDFVar getPredicate() {
			return predicate;
		}
		public RDFVar getSubject() {
			return subject;
		}
		public String toString() {
			return 
			subject.toString()+" "+
			predicate.toString()+" "+
			object.toString();
		}
	}

	protected Collection<Triple> triples = new HashSet<Triple>();
	protected RDFShard rdfShard;
	protected Map<RDFVar,String> varMap = new HashMap<RDFVar,String>();
	//protected Collection<Set<RDFVar>> varSets = new HashSet<Set<RDFVar>>();
	protected int refNum = 0;
	protected RDFVar selectVar;
	protected RelationVocabulary rv = new RelationVocabulary();

	public RDFQuery(QueryTerm qt) {
		selectVar = makeVar();
		translate(qt, selectVar);
	}

	public RDFQuery() {
		// TODO Auto-generated constructor stub
	}
	
	

	public RDFShard getRdfShard() {
		if (rdfShard == null)
			rdfShard = new RDFShard();
		return rdfShard;
	}

	public void setRdfShard(RDFShard rdfShard) {
		this.rdfShard = rdfShard;
	}
	
	

	public RDFVar getSelectVar() {
		return selectVar;
	}

	public void setSelectVar(RDFVar selectVar) {
		this.selectVar = selectVar;
	}

	public void addRegexFilter(RDFVar var, String s) {

	}
	
	protected void addVar(RDFVar v) {
		refNum++;
		varMap.put(v, "x_"+refNum);
		v.setName(varMap.get(v)); // TODO - DRY
		/*
		Set<RDFVar> varSet = new HashSet<RDFVar>();
		varSet.add(v);
		varSets.add(varSet); // new singleton
		*/
	}
	
	protected RDFVar makeVar() {
		RDFVar v = new RDFVar();
		addVar(v);
		return v;
	}

	protected void unifyVars(RDFVar v1, RDFVar v2) {
		String ref1 = varMap.get(v1);
		String ref2 = varMap.get(v2);
		refNum++;
		String newRef = "x_"+refNum;
		for (RDFVar v : varMap.keySet()) {
			if (varMap.get(v) == ref1 || varMap.get(v) == ref2) {
				varMap.put(v,newRef);
			}
			v.setName(varMap.get(v));
		}
	}
	

	public void addTriple(RDFVar su, RDFVar pr, RDFVar ob) {
		triples.add(new Triple(su,pr,ob));
	}

	public void translate(QueryTerm qt,RDFVar joinVar) {

		if (qt == null) {
			return;
		}
		else if (qt instanceof ComparisonQueryTerm) {
			ComparisonQueryTerm cqt = (ComparisonQueryTerm)qt;
			Operator op = cqt.getOperator();
			QueryTerm comparedToQt = cqt.getValue();
			if (comparedToQt instanceof AtomicQueryTerm) {
				AtomicQueryTerm atomQt = (AtomicQueryTerm)comparedToQt;
				Class datatypeClass = atomQt.getDatatype();
				Object atom = atomQt.getValue();

				if (joinVar != null) {
					if (op.equals(Operator.STARTS_WITH))
						addRegexFilter(joinVar, atom+".*"); 
					else if (op.equals(Operator.CONTAINS))
						addRegexFilter(joinVar, atom+".*"); 
					else if (op.equals(Operator.MATCHES))
						addRegexFilter(joinVar, atom+".*"); 
					else if (op.equals(Operator.EQUAL_TO)) {
						joinVar.setResource(atom.toString()); // TODO: numbers
					}
					else 
						addRegexFilter(joinVar, atom+".*"); 
				}		

			}
		}
		else if (qt instanceof LabelQueryTerm) {
			LabelQueryTerm cqt = (LabelQueryTerm)qt;
			AliasType alias = cqt.getAliasType();
			if (alias.equals(AliasType.ID)) {
				translate(cqt.getValue(), joinVar); // passthru
			}
			else {

			}
		}
		else if (qt instanceof AnnotationLinkQueryTerm) {
			AnnotationLinkQueryTerm origAnnotQt = (AnnotationLinkQueryTerm)qt;

			/*
			 * We want to return annotations of R(X,Y) if R'(Y,Z),
			 * where Z is the class of interest and X is the annotated entity.
			 * R(X,Y) is asserted.
			 * We do this with a nested link query
			 * newqt = Link(inf=f,positedBy=NOT_NULL,target=Link(target=COI))
			 */
			LinkQueryTerm ontolQt = new LinkQueryTerm(origAnnotQt.getTarget());
			LinkQueryTerm transformedAnnotQt =
				new LinkQueryTerm(ontolQt);

			transformedAnnotQt.setSource(origAnnotQt.getSource());
			transformedAnnotQt.setNode(origAnnotQt.getNode());
			transformedAnnotQt.setRelation(origAnnotQt.getRelation()); // R
			QueryTerm ontolRel = origAnnotQt.getOntologyRelation();
			ontolQt.setRelation(ontolRel); // R'
			//ontolQt.setQueryAlias(IMPLIED_ANNOTATION_LINK_ALIAS);
			transformedAnnotQt.setQueryAlias(origAnnotQt.getQueryAlias());
			transformedAnnotQt.setPositedBy(new ExistentialQueryTerm());
			transformedAnnotQt.setInferred(false);
			//Logger.getLogger("org.obd").fine("annotq="+transformedAnnotQt.toString());			
			translate(transformedAnnotQt,joinVar);
		}
		else if (qt instanceof LinkQueryTerm) {
			RDFVar su = makeVar();
			RDFVar pr = makeVar();
			RDFVar ob = makeVar();

			//su.unifyWith(joinVar);
			unifyVars(su,joinVar);

			LinkQueryTerm lqt = (LinkQueryTerm) qt;
			translate(lqt.getNode(),su);
			translate(lqt.getRelation(),pr);
			translate(lqt.getTarget(),ob);
			if (qt.getPositedBy() != null) {
				// TODO - current triplestore lacks reified links
				if (false) {
					// TODO: switch for named graphs. For now, reification only
					RDFVar s = makeVar();
					translate(qt.getPositedBy(),s);
					addTriple(s, new RDFVar(RDF.subject),su);
					addTriple(s, new RDFVar(RDF.predicate),pr);
					addTriple(s, new RDFVar(RDF.object),ob);
				}
			}
			boolean isSimpleTriple = false;
			if (pr.getResource() != null && pr.getResource().equals(RDFS.subClassOf)) {
				isSimpleTriple = true;
			}
			if (lqt.getInstanceQuantifier() == null) {
				isSimpleTriple = true;
			}
			
			// TODO: union of both?
			if (isSimpleTriple) {
				addTriple(su, pr, ob);
			}
			else {
				RDFVar restr = makeVar();
				addTriple(su, new RDFVar(RDFS.subClassOf), restr);
				addTriple(restr, new RDFVar(OWL.onProperty), pr);
				if (lqt.getInstanceQuantifier().isUniversal())
					addTriple(restr, new RDFVar(OWL.allValuesFrom), ob);
				else
					addTriple(restr, new RDFVar(OWL.someValuesFrom), ob);
			}

		}
		else {

		}


	}

	public String toSPARQL() {
		String sparql =
			"SELECT " + selectVar.toString() + 
			" WHERE { " + toSPARQL(triples) + "}";
		return sparql;
	}

	public String toSPARQL(Collection<Triple> conjs) {
		StringBuffer sb = new StringBuffer();
		int n = 0;
		for (Triple t : conjs) {
			if (n>0)
				sb.append(" . ");
			sb.append(t.toString());
			n++;

		}
		return sb.toString();
	}

	private Resource createResource(String id) {
		try {
			return getRdfShard().createResource(id);
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		} // TODO
	}


}
