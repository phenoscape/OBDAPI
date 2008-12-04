package org.obd.model.bridge;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.LinkedList;

import org.bbop.dataadapter.DataAdapterException;
import org.obd.model.Graph;
import org.obd.model.LinkStatement;
import org.obd.model.LiteralStatement;
import org.obd.model.Node;
import org.obd.model.NodeAlias;
import org.obd.model.Statement;
import org.obd.model.Node.Metatype;
import org.obd.model.vocabulary.TermVocabulary;
import org.obd.query.AtomicQueryTerm;
import org.obd.query.ComparisonQueryTerm;
import org.obd.query.LabelQueryTerm;
import org.obd.query.LinkQueryTerm;
import org.obd.query.QueryTerm;
import org.obd.query.SubsetQueryTerm;
import org.obd.query.ComparisonQueryTerm.Operator;
import org.obd.query.LabelQueryTerm.AliasType;
import org.obd.query.QueryTerm.Aspect;
import org.obo.annotation.datamodel.Annotation;
import org.obo.annotation.datamodel.impl.AnnotationImpl;

import org.obo.dataadapter.OBOAdapter;
import org.obo.dataadapter.OBOFileAdapter;
import org.obo.dataadapter.OBOSerializationEngine;
import org.obo.datamodel.Dbxref;
import org.obo.datamodel.DefinedObject;
import org.obo.datamodel.IdentifiedObject;
import org.obo.datamodel.Instance;
import org.obo.datamodel.Link;
import org.obo.datamodel.LinkedObject;
import org.obo.datamodel.Namespace;
import org.obo.datamodel.OBOClass;
import org.obo.datamodel.OBOObject;
import org.obo.datamodel.OBOProperty;
import org.obo.datamodel.OBORestriction;
import org.obo.datamodel.OBOSession;
import org.obo.datamodel.ObjectFactory;
import org.obo.datamodel.PropertyValue;
import org.obo.datamodel.Synonym;
import org.obo.datamodel.SynonymedObject;
import org.obo.datamodel.impl.DanglingLinkImpl;
import org.obo.datamodel.impl.OBOSessionImpl;
import org.obo.datamodel.impl.SynonymImpl;
import org.obo.filters.CategorySearchCriterion;
import org.obo.filters.CompoundFilter;
import org.obo.filters.CompoundFilterImpl;
import org.obo.filters.EqualsComparison;
import org.obo.filters.Filter;
import org.obo.filters.IsCompleteLinkCriterion;
import org.obo.filters.LinkFilter;
import org.obo.filters.LinkFilterFactory;
import org.obo.filters.ObjectFilter;
import org.obo.filters.ObjectFilterFactory;
import org.obo.util.AnnotationUtil;
import org.obo.util.FilterUtil;
import org.obo.util.TermUtil;
import org.purl.obo.vocab.RelationVocabulary;

public class OBOBridge {
	
	static TermVocabulary tvocab = new TermVocabulary();
	static RelationVocabulary rvocab = new RelationVocabulary();

	
	public static String namespace2src(Namespace ns) {
		if (ns == null)
			return null;
		String src = ns.getID();
		return src;
	}
	
	public static Node namespace2srcNode(Namespace ns) {
		Node n = new Node();
		n.setId(namespace2src(ns));
		return n;
	}
	
	public static Graph session2graph(OBOSession session) {
		Graph g = new Graph();
		for (IdentifiedObject io :session.getObjects()) {
			Node n = obj2node(io);
			if (n != null)
				g.addNode(n);
		}
		return g;
	}
	
	public static Node obj2nodeBasic(IdentifiedObject io) {
		if (io == null)
			return null;
		if (io.isBuiltIn())
			return null;
		Node node = new Node();
		node.setId(io.getID());
		String id = node.getId();
		node.setLabel(io.getName());
		return node;
	}

	// in org.obo.model, Namespace is not an identified object
	public static Node obj2node(Namespace ns) {
		Node node = new Node();
		node.setId(ns.getID());
		String id = node.getId();
		node.setMetatype(Metatype.INSTANCE);
		addLinkStatement(node,rvocab.instance_of(),tvocab.SUBSET());
		return node;
	}

	public static Node obj2node(IdentifiedObject io) {
		if (io == null)
			return null;
		if (io.isBuiltIn())
			return null;
		TermVocabulary vocab = new TermVocabulary();

		Node node = new Node();
		node.setId(io.getID());
		String id = node.getId();
		node.setLabel(io.getName());
		String srcId = namespace2src(io.getNamespace());
		node.setSourceId(srcId);
		node.setAnonymous(io.isAnonymous());
		for (Link link : ((LinkedObject)io).getParents() ) {
			node.addStatement(link2statement(link));
		}
		if (io instanceof OBOObject) {

			OBOObject obj = (OBOObject) io;
			if (obj.getSynonyms() != null) {
				for (Synonym s : obj.getSynonyms()) {
					LiteralStatement ls = new NodeAlias();
					ls.setNodeId(node.getId());
					ls.setRelationId(vocab.HAS_SYNONYM());
					ls.setValue(s.getText());
					ls.setSourceId(srcId);
					node.addStatement(ls);
				}
			}
			String def = obj.getDefinition();
			if (def != null && !def.equals("")) {
				LiteralStatement ls = new LiteralStatement();
				ls.setNodeId(node.getId());
				ls.setValue(def);
				ls.setRelationId(vocab.HAS_DEFINITION());
				ls.setSourceId(srcId);
				node.addStatement(ls);

			}
		}
		for (PropertyValue pv : io.getPropertyValues()) {
			LinkStatement ls = new LinkStatement(id,
					pv.getProperty(),
					pv.getValue());
			ls.setSourceId(srcId);
			node.addStatement(ls);

		}
		return node;
	}
	
	public static Statement link2statement(Link link) {
		LinkStatement s = new LinkStatement();
		s.setNodeId(link.getChild().getID());
		s.setRelationId(link.getType().getID());
		s.setTargetId(link.getParent().getID());
		s.setInferred(link.isImplied());
		if (link instanceof OBORestriction) {
			OBORestriction rlink = (OBORestriction)link;
			s.setAppliesToAllInstancesOf(true);
			if (rlink.completes()) {
				s.setIntersectionSemantics(true);
			}
		}
		String src = namespace2src(link.getNamespace());
		if (src == null)
			src = namespace2src(link.getChild().getNamespace());
		if (src != null)
			s.setSourceId(src);
		return s;
	}
	
	public static LinkStatement xref2statement(IdentifiedObject io, Dbxref x) {
		TermVocabulary vocab = new TermVocabulary();
		LinkStatement s = new LinkStatement();
		s.setNodeId(io.getID());
		s.setRelationId(vocab.HAS_DBXREF());
		s.setTargetId(x.getDatabase() + ":" + x.getDatabaseID());
		return s;
	}

	


	public static OBOSession graph2obosession(Graph graph) {
		OBOSession session = new OBOSessionImpl();
		ObjectFactory of = session.getObjectFactory();
		for (Node n : graph.getNodes()) {
			IdentifiedObject io = of.createObject(n.getId(), 
					OBOClass.OBO_CLASS, n.isAnonymous());
			if (n.getLabel() != null)
				io.setName(n.getLabel());
			session.addObject(io);
		}
		for (Statement s : graph.getAllStatements()){
			addStatementToSession(session, of, s);	
		}

		return session;
	}
	
	private static void addStatementToSession(OBOSession session, ObjectFactory of, Statement s) {
		LinkedObject child = id2oboObject(session, s.getNodeId(),OBOClass.OBO_CLASS);
		System.out.println(s.getRelationId());
		LinkedObject type = id2oboObject(session, s.getRelationId(),OBOClass.OBO_PROPERTY);
		Link link;
		session.addObject(child);
		if (s instanceof LinkStatement) {
			LinkStatement ls = (LinkStatement)s;
			TermUtil.castToClass(child);
			if (!(type instanceof OBOProperty)) {
				//TermUtil.castToProperty(type);
				type = (OBOProperty)of.createObject(s.getRelationId(),
						OBOClass.OBO_PROPERTY, false);
			}
			
			LinkedObject parent = id2oboObject(session, s.getTargetId(),OBOClass.OBO_CLASS);
			link =
				of.createOBORestriction(child, (OBOProperty)type,
						parent, s.isInferred());
			if (ls.isIntersectionSemantics())
				((OBORestriction)link).setCompletes(true);
			//System.out.println(link);
			child.addParent(link);
			if (s.getPositedByNodeId() != null) {
				Instance annotInst = 
					(Instance)
					of.createObject(s.getPositedByNodeId(), 
							OBOClass.OBO_INSTANCE, 
							false);
				id2oboObject(session, s.getPositedByNodeId(),OBOClass.OBO_CLASS);
				AnnotationImpl annot = new AnnotationImpl(annotInst, link);
				session.addObject(annot);
				for (Statement ss : s.getSubStatements()) {
					addStatementToSession(session, of, ss);
				}
			
			}
		}
		else if (s instanceof LiteralStatement) {
			LiteralStatement ls = (LiteralStatement)s;
			Object v = ls.getValue();
			if (ls.isAlias()) {
				Synonym syn = new SynonymImpl(v.toString());
				((SynonymedObject)child).addSynonym(syn);
			}
			else if (ls.isTextDescription()) {
				((DefinedObject)child).setDefinition(v.toString());
			}
			else {
				// TODO
			}
		}
	}

	public static String toOBOString(Collection<Statement> stmts) {
		Graph g = new Graph(stmts);
		return toOBOString(g);
	}
	public static CharSequence toOBOString(Node node) {
		Graph g = new Graph();
		g.addNode(node);
		return toOBOString(g);
	}

	public static String toOBOString(Graph graph) {
		OBOSession session = graph2obosession(graph);
		String oboString = null;
		File tempFile;
		try {
			tempFile = File.createTempFile("graph","obo");
			OBOFileAdapter.OBOAdapterConfiguration writeConfig =
				new OBOFileAdapter.OBOAdapterConfiguration();
			writeConfig.setBasicSave(false);
			writeConfig.setAllowDangling(true);
			writeConfig.setWritePath(tempFile.toString());
			writeConfig.setSerializer("OBO_1_2");
			OBOFileAdapter adapter = new OBOFileAdapter();
			OBOSerializationEngine.FilteredPath path = 
				new OBOSerializationEngine.FilteredPath();
			path.setPath(tempFile.toString());
			writeConfig.getSaveRecords().add(path);
			//path.setSaveImplied(true); TODO - make optional
			path.setAllowDangling(true);
			//path.setRealizeImpliedLinks(true); 
			adapter.doOperation(OBOAdapter.WRITE_ONTOLOGY, writeConfig, session);

			oboString =  BridgeUtil.readFileAsString(tempFile);
			tempFile.delete();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (DataAdapterException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return oboString;
			
	}
	
	public static LinkedObject id2oboObject(OBOSession session, String id, OBOClass ct) {
		IdentifiedObject io = session.getObject(id);
		if (io == null) {
//			io = session.getObjectFactory().createDanglingObject(id, isProperty);
			io = session.getObjectFactory().createObject(id, ct, false);
		}
		return (LinkedObject)io;

	}
	
	public static Graph getAnnotationGraph(OBOSession session) {
		Collection<Annotation> annots = AnnotationUtil.getAnnotations(session);
		
		Graph g = new Graph();
		for (Annotation annot : annots) {
			 Statement s = OBOBridge.annotation2statement(annot);
			 g.addStatement(s);
			 LinkedObject obj = annot.getObject();
			 if (obj.isAnonymous() || obj.getNamespace() == null) {
				 Node n = obj2node(obj);
				 g.addNode(n);
			 }
		}
		return g;
		
	}
	
	public static Statement annotation2statement(Annotation a) {
		Link link = a.getPositedLink();
		/*
		Link link = 
			new DanglingLinkImpl(
					a.getSubject() == null ? "" : a.getSubject().getID(),
							a.getRelationship() == null ? "" :a.getRelationship().getID(),
									a.getObject() == null ? "" : a.getObject().getID());
									*/
		Statement s = OBOBridge.link2statement(link);
		Node aNode = obj2node(a);
		for (Statement ss : aNode.getStatements()) {
			s.addSubStatement(ss);
		}
		//s.setSubStatements(aNode.getStatements());
		//s.setPositedByNodeId(a.getID());
		return s;
	}
	
	public static Filter query2filter(QueryTerm q) {
		LinkFilterFactory lff = new LinkFilterFactory();

		ObjectFilterFactory off = new ObjectFilterFactory();
		
		if (q instanceof SubsetQueryTerm) {
			LinkedList<Filter<Link>> linkFilters = new LinkedList<Filter<Link>>();

			SubsetQueryTerm sqt = (SubsetQueryTerm)q;
			if (sqt.getTarget() != null) {
				LinkFilter lfilter = (LinkFilter)lff.createNewFilter();
				String subsetId = (String)sqt.getAtomicValue(); // assumes simple structure
				ObjectFilter ofilter = (ObjectFilter)query2filter(sqt.getTarget());
				CategorySearchCriterion c = new CategorySearchCriterion();
				ofilter.setCriterion(c);
				ofilter.setValue(subsetId);
				linkFilters.add(lfilter);
			}
			Filter<Link> out = FilterUtil.mergeFilters(linkFilters);
			return out;
		}
		else if (q instanceof LinkQueryTerm) {
			ObjectFilter ofilter;
			LinkQueryTerm lqt = (LinkQueryTerm)q;
			int oboAspect;
			Aspect aspect = lqt.getAspect();
			Collection<LinkFilter> linkFilters = new LinkedList<LinkFilter>();
			if (lqt.getNode() != null) {
				LinkFilter lfilter = (LinkFilter)lff.createNewFilter();
				ofilter = (ObjectFilter)query2filter(lqt.getNode());
				lfilter.setAspect(LinkFilter.CHILD);
				lfilter.setFilter(ofilter);
				linkFilters.add(lfilter);
				
			}
			if (lqt.getTarget() != null) {
				LinkFilter lfilter = (LinkFilter)lff.createNewFilter();
				ofilter = (ObjectFilter)query2filter(lqt.getTarget());
				lfilter.setAspect(LinkFilter.PARENT);
				lfilter.setFilter(ofilter);
				linkFilters.add(lfilter);
			}
			if (lqt.isDescriptionLink()) {
				LinkFilter lfilter = (LinkFilter)lff.createNewFilter();
				ofilter = (ObjectFilter)off.createNewFilter();
				ofilter.setCriterion(new IsCompleteLinkCriterion());
				lfilter.setAspect(LinkFilter.SELF);
				lfilter.setFilter(ofilter);
				linkFilters.add(lfilter);		
			}
	
			Filter filter;
			if (linkFilters.size() == 1) {
				filter = linkFilters.iterator().next();
			}
			else {
				CompoundFilter cf = new CompoundFilterImpl(CompoundFilter.AND);
				for (LinkFilter lfilter : linkFilters)
					cf.addFilter(lfilter);
				filter = cf;
			}
			/*
			if (aspect.equals(Aspect.SELF))
				oboAspect = LinkFilter.SELF;
			else if (aspect.equals(Aspect.TARGET))
				oboAspect = LinkFilter.PARENT;
			else
				oboAspect = LinkFilter.SELF;
			lfilter.setAspect(oboAspect);
			*/
			
			return filter;
		}
		else if (q instanceof LabelQueryTerm) {
			LabelQueryTerm lqt = (LabelQueryTerm)q;
			AliasType alias = lqt.getAliasType();
			return query2filter(lqt.getValue());
		}
		else if (q instanceof ComparisonQueryTerm) {
			ComparisonQueryTerm cqt = (ComparisonQueryTerm)q;
			ObjectFilter ofilter = (ObjectFilter)off.createNewFilter();
			if (cqt.getOperator().equals(Operator.EQUAL_TO)) {
				EqualsComparison c = new EqualsComparison();
				ofilter.setComparison(c);
				QueryTerm v = cqt.getValue();
				if (v instanceof AtomicQueryTerm) {
					ofilter.setValue(((AtomicQueryTerm)v).getSValue());
				}
				else {
					
				}
			}
			return ofilter;
		}
		else {
			
		}
		return null;
	}
	
	private static void addLinkStatement(Node node, String rel, String target) {
		LinkStatement ls = new LinkStatement();
		ls.setNodeId(node.getId());
		ls.setRelationId(rel);
		ls.setTargetId(target);
		node.addStatement(ls);
	}


}
