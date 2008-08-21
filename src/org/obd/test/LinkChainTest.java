package org.obd.test;

import java.util.Collection;

import org.obd.model.LinkStatement;
import org.obd.query.LinkQueryTerm;
import org.obd.query.QueryTerm.Aspect;
import org.obd.query.impl.OBDSQLShard;

@Deprecated
public class LinkChainTest extends AbstractOBDTest {

	public LinkChainTest(String n) {
		super(n);
	}

	public void xxx() {
		String INNER_LINK = "link2";
		String rel1 = "OBO_REL:variant_of";
		String rel2 = "OBO_REL:influences";
		//String newRel = "OBO_REL:implicated_in";
		String newRel = "OBO_REL:influences";
		
		LinkQueryTerm qt = new LinkQueryTerm(null, rel1, null);
		LinkQueryTerm iqt = new LinkQueryTerm(null,rel2, null);
		qt.setAspect(Aspect.SELF);
		iqt.setQueryAlias(INNER_LINK);
		qt.setNode(iqt);
		iqt.setAspect(Aspect.SELF);
		OBDSQLShard obdsql = (OBDSQLShard)shard;
		Collection<LinkStatement> stmts = obdsql.getImpliedLinkStatementsByQuery(qt, newRel, INNER_LINK);
		for (LinkStatement s : stmts) {
			//obdsql.putStatement(s);
		}

	}
	
	public void xxxRealize() {
		String INNER_LINK = "link2";
		String rel1 = "OBO_REL:variant_of";
		String rel2 = "OBO_REL:influences";
		//String newRel = "OBO_REL:implicated_in";
		String newRel = "OBO_REL:influences";
		OBDSQLShard obdsql = (OBDSQLShard)shard;
		obdsql.realizeLinkChain(newRel,rel1, rel2, true, false);
	}



}
