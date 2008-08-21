package org.obd.model;

import org.obd.model.vocabulary.TermVocabulary;

/**
 * A {@link LiteralStatement} used to denote an alternate label or synonym for an entity
 * @author cjm
 *
 */
public class NodeAlias extends LiteralStatement {
	public static TermVocabulary tv = new TermVocabulary();
	public enum Scope { BROAD, NARROW, RELATED, EXACT }
	
	public String categoryId;
	public Scope scope;
	
	public NodeAlias(String id, String syn) {
		super(id,syn);
	}
	public NodeAlias() {
		// TODO Auto-generated constructor stub
	}
	public String getCategoryId() {
		return categoryId;
	}
	public void setCategoryId(String categoryId) {
		this.categoryId = categoryId;
	}
	public Scope getScope() {
		return scope;
	}
	public void setScope(Scope scope) {
		this.scope = scope;
	}
	public String getRelationId() {
		if (scope != null) {
			if (scope.equals(scope.EXACT))
				return tv.HAS_EXACT_SYNONYM();
			if (scope.equals(scope.NARROW))
				return tv.HAS_NARROW_SYNONYM();
			if (scope.equals(scope.BROAD))
				return tv.HAS_BROAD_SYNONYM();
		}
		return tv.HAS_RELATED_SYNONYM();

	}
	
	public boolean isAlias() {
		return true;
	}

}
