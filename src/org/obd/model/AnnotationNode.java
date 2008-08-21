package org.obd.model;

import java.util.Collection;

/**
 * @deprecated
 * @author cjm
 *
 */
public class AnnotationNode extends Node {
	Collection<Statement> posits;

	public Collection<Statement> getPosits() {
		return posits;
	}

	public void setPosits(Collection<Statement> posits) {
		this.posits = posits;
	}
	
	
}
