package org.obd.parser;

import org.obd.model.LinkStatement;
import org.obd.model.Node;
import org.obd.model.Node.Metatype;

/**
 * Parses simple stanza-based metadata files
 * 
 * @author cjm
 *
 */
public class OBDSimpleMetadataParser extends StanzaInfoParser {

	String src = "obd";
	public OBDSimpleMetadataParser() {
		super();
	}
	public OBDSimpleMetadataParser(String path) {
		super(path);
	}

	public  void translateStanzas() {
		for (Stanza stanza : stanzas) {
			String id = stanza.getId();
			if (id == null) {
				System.err.println("no id: "+stanza);
				continue;
			}
			Node srcNode = addNode(id);
			srcNode.setMetatype(Metatype.INSTANCE);
			srcNode.setSourceId(src);
			LinkStatement instOfLink = new LinkStatement(id,"OBO_REL:instance_of","oboMetamodel:LogicalResource");
			instOfLink.setSourceId(src);
			srcNode.addStatement(instOfLink);
			for (String tag : stanza.getTags()) {
				String val = stanza.getTagVal(tag);

				if (tag.equals("title")) {
					srcNode.setLabel(val);
				}
				else if (tag.equals("is_obsolete")) {
					if (val.equals("true"))
						srcNode.setObsolete(true);
				}
				else {
					String[] valParts = val.split("\\|");
					if (valParts.length == 2) {
						Node ref = addNode(valParts[1]);
						ref.setLabel(valParts[0]);
						addLink(id, tag, ref.getId(), src);
					}
					else {
						if (isLiteral(tag))
							addLiteral(id, tag, val, "xsd:string", src);
						else
							addLink(id, tag, val, src);
					}
				}
			}
		}
	
	}
	
	public boolean isLiteral(String tag) {
		if (tag.equals("namespace"))
			return false;
		return true;
	}
}
