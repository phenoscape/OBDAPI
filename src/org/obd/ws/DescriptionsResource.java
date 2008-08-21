

package org.obd.ws;

import java.util.Collection;
import java.util.TreeMap;

import org.obd.model.CompositionalDescription;
import org.obd.model.Graph;
import org.obd.model.Node;
import org.obd.model.Statement;
import org.obd.model.TermView;
import org.obd.model.CompositionalDescription.Predicate;
import org.obd.model.bridge.OBDJSONBridge;
import org.obd.model.bridge.OBDXMLBridge;
import org.obd.model.bridge.OBOBridge;
import org.obd.model.bridge.OWLBridge;
import org.obd.query.LinkQueryTerm;
import org.obd.query.impl.OBOSessionShard;
import org.obo.datamodel.OBOSession;
import org.restlet.Context;
import org.restlet.data.MediaType;
import org.restlet.data.Reference;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.resource.Representation;
import org.restlet.resource.StringRepresentation;
import org.restlet.resource.Variant;

/**
 * Resource for a {@link CompositionalDescription} collection
 * 
 * @author cjm
 */
public class DescriptionsResource extends NodeResource {
	

    /**
     * Constructor.
     * 
     * @param context
     *            The parent context.
     * @param request
     *            The request to handle.
     * @param response
     *            The response to return.
     */
    public DescriptionsResource(Context context, Request request, Response response) {
        super(context, request, response);
    }

    @Override
    public Representation getRepresentation(Variant variant) {
    	Representation result = null;

    	LinkQueryTerm dqt = new LinkQueryTerm();
    	dqt.setDescriptionLink(true);
    	Collection<Statement> statements = getShard().getStatementsByQuery(dqt);
    	// TODO: expose option for full traversal
    	Graph g = new Graph(statements);
		if (true) {
			String[] nids = g.getReferencedNodeIds();
			for (String nid : nids) {
				Node n = getShard().getNode(nid);
				g.addNode(n);
			}
		}

 
    	if (format == null) {
    		format = "";
    	}
    	if (format.equals("json")) {
    		result = new StringRepresentation(OBDJSONBridge.toJSON(g).toString());
    		return result;

    	}
    	else if (format.equals("obo")) {
    		result = new StringRepresentation(OBOBridge.toOBOString(g));
    		return result;
    	}
    	else if (format.equals("obdxml")) {
    		result = new StringRepresentation(OBDXMLBridge.toXML(g));
    		return result;
    	}
    	else if (format.equals("owl")) {
    		result = new StringRepresentation(OWLBridge.toOWLString(g));
    		return result;
    	}
		else if (format.equals("view")) {
			TreeMap<String, Object> map = new TreeMap<String, Object>();
			TermView termview = new TermView(g);
			g.nestStatementsUnderNodes();
			map.put("graph", g);
			map.put("termview", termview);
			return getTemplateRepresentation("templates/DescriptionsView",map);
		}

    	else {
    		//if (variant.getMediaType().equals(MediaType.TEXT_HTML)) {
    		StringBuilder sb = new StringBuilder();
    		sb.append("<pre>");
    		sb.append("------------\n");
    		sb.append("Description\n");
    		sb.append("------------\n\n");
    		//sb.append(toHTML(g));
    		sb.append(hrefToOtherFormats("/nodes/"+getNodeId()+"/description"));
    		sb.append("</pre>");
    		result = new StringRepresentation(sb, MediaType.TEXT_HTML);
    	}

    	return result;
    }
    
    public String toHTML(CompositionalDescription desc) {
     	StringBuilder sb = new StringBuilder();
     	if (desc == null)
     		return "";
		if (desc.isAtomic())
			return href(desc.getNodeId());
		
     	sb.append(desc.getPredicate().toString());
      	sb.append("( ");
      	if (desc.getPredicate().equals(Predicate.RESTRICTION)) {
            	sb.append(desc.getRelationId());
         	sb.append(" SOME ");
         	
      	}
      	for (CompositionalDescription arg : desc.getArguments()) {
      		sb.append(toHTML(arg));
      		sb.append(" ");
      	}
      	sb.append(" )");
      	return sb.toString();   	
    }
    
     @Override
    public void handleGet() {
        // Make sure that the Uri ends with a "/" without changing the query.
        Reference ref = getRequest().getResourceRef();
        if (!ref.getPath().endsWith("/")) {
            ref.setPath(ref.getPath() + "/");
            getResponse().redirectPermanent(ref);
        } else {
            super.handleGet();
        }
    }

}
