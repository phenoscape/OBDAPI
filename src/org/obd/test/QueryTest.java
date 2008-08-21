package org.obd.test;


import java.sql.SQLException;

import org.bbop.rdbms.RelationalQuery;
import org.obd.model.CompositionalDescription;
import org.obd.model.Graph;
import org.obd.model.LinkStatement;
import org.obd.model.Statement;
import org.obd.query.AnnotationLinkQueryTerm;
import org.obd.query.BooleanQueryTerm;
import org.obd.query.ComparisonQueryTerm;
import org.obd.query.CompositionalDescriptionQueryTerm;
import org.obd.query.LabelQueryTerm;
import org.obd.query.LinkQueryTerm;
import org.obd.query.LiteralQueryTerm;
import org.obd.query.QueryTerm;
import org.obd.query.SourceQueryTerm;
import org.obd.query.BooleanQueryTerm.BooleanOperator;
import org.obd.query.ComparisonQueryTerm.Operator;
import org.obd.query.LabelQueryTerm.AliasType;
import org.obd.query.QueryTerm.Aspect;
import org.obd.query.impl.OBDSQLShard;

/**
 * tests for ability to query by arbitrarily complex and nested {@link QueryTerm} objects
 * @author cjm
 *
 */
public class QueryTest extends AbstractOBDTest {


	public QueryTest(String n) {
		super(n);
	}


	// TODO: refactor into separate tests
	public void testQuery() throws SQLException, ClassNotFoundException {

		/**
		 * core set of relations to use to test
		 */
		String isA = "OBO_REL:is_a";
		String partOf = "OBO_REL:part_of";
		String developsFrom = "develops_from";

		/**
		 * querying for literals: e.g. synonyms, descriptions, properties
		 */
		if (true) {
			//			LiteralQueryTerm lqt =
			//				new LiteralQueryTerm(new ComparisonQueryTerm(Operator.CONTAINS,
			//						"breast cancer"));
			QueryTerm lqt = 
				new LabelQueryTerm(AliasType.PRIMARY_NAME,
						new ComparisonQueryTerm(Operator.CONTAINS,
						"breast cancer"));
			runNodeQuery(lqt);
		}
		if (true) {
			runLiteralQuery(new LabelQueryTerm("MP:0008246"));
		}

		/*
		 * find all entities that develops from
		 * the visual primordium
		 */
		if (true) {
			runNodeQuery(new LinkQueryTerm("develops_from", "FBbt:00001059"));
		}

		/*
		 * find all entities that are part of something that develops from
		 * the visual primordium
		 */
		if (true) {
			runNodeQuery(new LinkQueryTerm("part_of", 
					new LinkQueryTerm("develops_from", "FBbt:00001059")));
		}

		if (true) {
			LabelQueryTerm lqt =
				new LabelQueryTerm(AliasType.PRIMARY_NAME,
						new ComparisonQueryTerm(Operator.CONTAINS,
						"eye"));
			SourceQueryTerm sq = new SourceQueryTerm("cell");
			BooleanQueryTerm bq = new BooleanQueryTerm( 
					BooleanOperator.AND,
					lqt,
					sq);
			runNodeQuery(bq, "CL:0000287");
		}

		/**
		 * two ways of filtering by source
		 */
		if (true) {
			LabelQueryTerm lqt =
				new LabelQueryTerm(AliasType.PRIMARY_NAME,
						new ComparisonQueryTerm(Operator.CONTAINS,
						"eye"));
			lqt.setNodeSource("cell");
			runNodeQuery(lqt, "CL:0000287");
		}

		/**
		 * which is the source of the COI?
		 * 
		 * Label(COI)/SOURCE
		 */
		if (true) {
			QueryTerm srcqt = new LabelQueryTerm("FMA:7088");
			srcqt.setAspect(Aspect.SOURCE);
			runNodeQuery(srcqt);
		}

		/**
		 * complex boolean queries
		 */
		if (true) {

			/**
			 * all classes that are subsumed (subclass/isa children) of
			 * heart OR kidney
			 */
			QueryTerm heartOrKidneyQuery =
				new BooleanQueryTerm( 
						BooleanOperator.OR,
						new LinkQueryTerm(isA, "FMA:7088"),
						new LinkQueryTerm(isA, "FMA:7203"));

			/*

		 QueryTerm heartOrKidneyQuery =
			new BooleanQueryTerm( 
					BooleanOperator.OR,
					new LinkQueryTerm(isA, 
							new LabelQueryTerm(AliasType.PRIMARY_NAME,"heart")),
					new LinkQueryTerm(isA, 
							new LabelQueryTerm(AliasType.PRIMARY_NAME,"kidney")));
			 */

			runNodeQuery(heartOrKidneyQuery, "FMA:7205"); // left kidney

			QueryTerm sizeOfHeartOrKidneyQuery =
				new BooleanQueryTerm(
						BooleanOperator.AND,
						new LinkQueryTerm(isA,"PATO:0000117"),
						new LinkQueryTerm(heartOrKidneyQuery));

			runNodeQuery(new AnnotationLinkQueryTerm(sizeOfHeartOrKidneyQuery));
		}

		/*
		 * all entities that are part of the nucleus
		 */
		if (true) {
			LinkQueryTerm partOfNucleusQuery = new LinkQueryTerm(partOf, 
					new LabelQueryTerm(AliasType.ANY_LABEL, "nucleus"));

			// find the entities themselves
			runNodeQuery(partOfNucleusQuery);

			// anything annotated to part-of-nucleus
			runLinkQuery(new AnnotationLinkQueryTerm(partOfNucleusQuery));
		}

		/*
		 * label queries:
		 * find all entities with a primary name "eya1"
		 * (e.g. the eyes absent gene)
		 * 
		 */
		if (true) {
			LabelQueryTerm labelQuery =
				new LabelQueryTerm(AliasType.PRIMARY_NAME,
				"eya1");

			// matching entities
			runNodeQuery(labelQuery,"ZFIN:ZDB-GENE-990712-18");		
		}

		if (true) {
			runNodeQuery(new LabelQueryTerm(AliasType.ALTERNATE_LABEL,
					new ComparisonQueryTerm(Operator.STARTS_WITH,
					"apoptosis")));

			runNodeQuery(new LinkQueryTerm(partOf, "GO:0005634"));
		}

		if (true) {
			/*
			 * find all entities that are part of something that develops from
			 * the visual primordium
			 */
			runNodeQuery(new LinkQueryTerm("part_of", 
					new LinkQueryTerm("develops_from", "FBbt:00001059")));
		}
		/*
		 * all entities that are undifferentiated and trace their
		 * developmental lineage back to a neurectodermal cell
		 */
		QueryTerm cellQuery =
			new BooleanQueryTerm( 
					BooleanOperator.AND,
					new LinkQueryTerm(isA, "CL:0000055"), // non-terminally differentiated cell
					new LinkQueryTerm(developsFrom, "CL:0000133") // neurectodermal cell
			);
		runNodeQuery(cellQuery);

		/*
		 * all annotations to entities that are undifferentiated and trace their
		 * developmental lineage back to a neurectodermal cell
		 */
		runNodeQuery(new AnnotationLinkQueryTerm(cellQuery));

		runLinkQuery(new LinkQueryTerm(partOf, "GO:0031595")); // nuclear proteasome complex

		// annotations
		AnnotationLinkQueryTerm oq = new AnnotationLinkQueryTerm("FMA:58865"); // anterior segment of eyeball
		runLinkQuery(oq);


		/**
		 * Finding asserted links in which the link node is subsumed by the COI
		 * 
		 * Link(inf=f,node=Link(target=COI))
		 * 
		 * link(A,B),implied_link(A,C)
		 * 
		 * similar semantics to annotation query
		 */
		if (true) {
			LinkQueryTerm iq = new LinkQueryTerm();
			iq.setTarget("FMA:7088"); // Heart
			iq.setQueryAlias("link_to_coi");
			LinkQueryTerm q1 = new LinkQueryTerm();
			q1.setInferred(false);
			q1.setNode(iq);
			Statement match = new LinkStatement("FMA:9290", "OBO_REL:is_a", "FMA:9721");
			runLinkQuery(q1, match);
		}


		/**
		 * Finding asserted links in which the link target subsumes the COI
		 * [atypical use case]
		 * 
		 * Link(inf=f,node=Link(node=COI)/TARGET)
		 * 
		 * The aspect (here /TARGET) indicates the context of the
		 * link query term is such that the target of the link should be used in
		 * the join to the outer query
		 * 
		 * link(A,B),link(C,B)
		 */
		if (true) {
			LinkQueryTerm iq = new LinkQueryTerm();
			iq.setNode("MP:0008324"); // abnormal melanotroph morphology
			//iq.setTarget("MP:0008324"); // abnormal melanotroph morphology
			iq.setAspect(Aspect.TARGET); 
			iq.setQueryAlias("x");
			LinkQueryTerm q = new LinkQueryTerm();
			q.setInferred(false);
			q.setNode(iq);
			runLinkQuery(q, 
					new LinkStatement("CL:0000151", "OBO_REL:is_a", "CL:0000144"));
		}

		/**
		 * CompositionalDescription objects can be transformed into query objects
		 * 
		 */
		if (true) {		
			String id = "MP:0008246";
			LinkQueryTerm lq = 
				new LinkQueryTerm();
			lq.setNode(id);
			lq.setInferred(false);
			Graph g = 
				runLinkQuery(lq);
			if (g != null) {
				CompositionalDescription desc = g.getCompositionalDescription(id);
				CompositionalDescriptionQueryTerm dq = 
					new CompositionalDescriptionQueryTerm(desc);
				runNodeQuery(new AnnotationLinkQueryTerm(dq));
			}
		}



	}

	public Graph runNodeQuery(QueryTerm qt) throws SQLException, ClassNotFoundException {
		return runNodeQuery(qt,null);
	}

	public Graph runNodeQuery(QueryTerm qt, String nid) throws SQLException, ClassNotFoundException {
		System.out.println("running Q: "+qt.toString()+" Expect: "+nid);
		OBDSQLShard s = new OBDSQLShard();
		RelationalQuery rq = s.translateQueryForNode(qt);
		System.out.println(rq.toString());
		for (Object v : rq.getPlaceHolderVals())
			System.out.println(" "+v);
		return null;
	}


	public Graph runLinkQuery(QueryTerm qt) throws SQLException, ClassNotFoundException {
		return runLinkQuery(qt, null);
	}

	public Graph runLinkQuery(QueryTerm qt, Statement stmt) throws SQLException, ClassNotFoundException {
		System.out.println("running Q: "+qt.toString());
		//OBDSQLShard s = new OBDSQLShard();
		OBDSQLShard s = (OBDSQLShard)shard;
		RelationalQuery rq = s.translateQueryForLinkStatement(qt);
		System.out.println(rq.toString());
		for (Object v : rq.getPlaceHolderVals())
			System.out.println(" "+v);
		return null;

	}
	public Graph runLiteralQuery(QueryTerm qt) throws SQLException, ClassNotFoundException {
		System.out.println("running Q: "+qt.toString());
		OBDSQLShard s = (OBDSQLShard)shard;
		RelationalQuery rq = s.translateQueryForLiteral(qt);
		System.out.println(rq.toString());
		for (Object v : rq.getPlaceHolderVals())
			System.out.println(" "+v);
		return null;
	}

}
