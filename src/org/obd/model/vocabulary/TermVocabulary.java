/**
 * For retrieving identifiers for terminology metamodel.
 * @see <a href="http://www.bioontology.org/wiki/index.php/OboInOwl:Main_Page">OboInOwl</a>
 * <p>
 * The primary purpose of this class is to make the oboInOwl vocabulary visible at
 * the java level for the purposes of compile-time code checking, and to integrate the
 * ontology documentation with the java documentation
 * <p>
 * Note: method accessors may change from uppercase to lowercase convention, to be
 * consistent with the vocabulary itself
 * <p>
 * @author cjm
 *
 */
package org.obd.model.vocabulary;

public class TermVocabulary {

	public TermVocabulary() {
		super();
	}
	
	public String HAS_DEFINITION() {
		return id("hasDefinition");
	}
	public String HAS_SYNONYM() {
		return id("hasSynonym");
	}
	public String HAS_EXACT_SYNONYM() {
		return id("hasExactSynonym");
	}
	public String HAS_NARROW_SYNONYM() {
		return id("hasNarrowSynonym");
	}
	public String HAS_BROAD_SYNONYM() {
		return id("hasBroadSynonym");
	}
	public String HAS_RELATED_SYNONYM() {
		return id("hasRelatedSynonym");
	}
	public String IN_SUBSET() {
		return id("inSubset");
	}
	
	public String SUBSET() {
		return id("Subset");
	}
	public String HAS_DBXREF() {
		return id("hasDbXref");
	}
	
	private String id(String local) {
		return "oboInOwl:"+local;
	}

	public boolean isSynonym(String id) {
		if (id.equals(HAS_SYNONYM()))
			return true;
		if (id.equals(HAS_EXACT_SYNONYM()))
			return true;
		if (id.equals(HAS_NARROW_SYNONYM()))
			return true;
		if (id.equals(HAS_BROAD_SYNONYM()))
			return true;
		if (id.equals(HAS_RELATED_SYNONYM()))
			return true;
		return false;
	}
}
