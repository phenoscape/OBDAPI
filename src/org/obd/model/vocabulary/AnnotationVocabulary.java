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

public class AnnotationVocabulary {

	public AnnotationVocabulary() {
		super();
	}

	public String HAS_PROVENANCE() {
		return id("has_data_source");
	}
	public String HAS_DATE() {
		return id("date");
	}
	public String ASSIGNED_BY() {
		return id("assigned_by");
	}
	public String WITH() {
		return id("with");
	}
	public String HAS_EVIDENCE() {
		return id("has_evidence");
	}


	private String id(String local) {
		return "oban:"+local;
	}


}
