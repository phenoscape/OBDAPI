package org.obd.query;

/**
 * A {@link QueryTerm} that matches any node that is not the target of a LinkStatement 
 * by some relation
 * @author cjm
 *
 */
public class RootQueryTerm extends QueryTerm {
	protected QueryTerm rootSource;

	public RootQueryTerm(String relationAtom, QueryTerm rootSource) {
		super();
		//this.relation = new AtomicQueryTerm(relation);
		this.relation = new LabelQueryTerm(relationAtom);
		this.rootSource = rootSource;
	}

	public RootQueryTerm(String relationAtom, String rootSource) {
		super();
		this.relation = new LabelQueryTerm(relationAtom);
		this.rootSource = new LabelQueryTerm(rootSource);
	}
	
	public RootQueryTerm() {
		super();
	}
	
	public QueryTerm getRootSource() {
		return rootSource;
	}

	public void setRootSource(QueryTerm rootSource) {
		this.rootSource = rootSource;
	}
	public void setRootSource(String rootSource) {
		this.rootSource = new LabelQueryTerm(rootSource);
	}

	public String toString() {
		String s = "Link[ ";
		if (node != null) {
			s = s + "from: "+getNode()+" ";
		}
		if (relation != null)
			s =s +"rel: "+relation+" ";
		if (rootSource != null)
			s = s + "inSource: "+rootSource.toString()+" ";
		s = s+"]";
		if (aspect != null)
			s=s+"/"+aspect+" ";
		return s;
	}



}
