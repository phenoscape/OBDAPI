package org.obd.model;

/**
 * A {@link LinkStatement} denoting equivalence
 * <p>
 * Here "equivalence" is inclusive of
 * <ul>
 *  <li>owl:sameAs
 *  <li>owl:equivalentClass
 *  <li>owl:equivalentProperty
 * </ul>
 * @author cjm
 *
 */
public class EquivalenceStatement extends LinkStatement {

	public EquivalenceStatement(String su, String rel, String ob) {
		super(su, rel, ob);
	}

	/**
	 * true  if is owl:equivalentClass semantics
	 */ 
	public boolean isEquivalentClass() {
		return this.isAppliesToAllInstancesOf();
	}
}
