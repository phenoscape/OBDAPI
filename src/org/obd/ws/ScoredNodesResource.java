

package org.obd.ws;

import java.util.Collection;
import java.util.List;

import org.obd.model.Graph;
import org.obd.model.Node;
import org.obd.model.Statement;
import org.obd.model.stats.ScoredNode;
import org.restlet.Context;
import org.restlet.data.MediaType;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.resource.Representation;
import org.restlet.resource.StringRepresentation;
import org.restlet.resource.Variant;

/**
 * Resource for a collection of node-score pairs
 * 
 * @author cjm
 */
public class ScoredNodesResource extends NodeResource {
	

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
    public ScoredNodesResource(Context context, Request request, Response response) {
        super(context, request, response);
        getVariants().clear();
        if (getNode() != null) {
            getVariants().add(new Variant(MediaType.TEXT_HTML));
        }
    }
    
    protected List<ScoredNode> getScoredNodes() {
       Collection<Statement> stmts;
        
       List<ScoredNode> scoredNodes = getShard().getSimilarNodes(getNodeId());
          return scoredNodes;
    }

    @Override
    public Representation getRepresentation(Variant variant) {
        Representation result = null;

        List<ScoredNode> scoredNodes = getScoredNodes();
         
        if (format == null) {
        	format = "";
        }

    	Graph g = new Graph();
          if (format.equals("json")) {
 //       	result = new StringRepresentation(OBDJSONBridge.toJSON(scoredNodes).toString());
  //      	return result;

        }
        else if (format.equals("owl")) {
 //       	result = new StringRepresentation(OWLBridge.toOWLString(scoredNodes));
  //      	return result;
        }
        else if (format.equals("obo")) {
 //       	result = new StringRepresentation(OBOBridge.toOBOString(scoredNodes));
  //      	return result;
        }
        else {
        	//if (variant.getMediaType().equals(MediaType.TEXT_HTML)) {
        	StringBuilder sb = new StringBuilder();
        	sb.append("<pre>");
        	sb.append("------------\n");
        	sb.append("Statements\n");
        	sb.append("------------\n\n");
          	 
        	for (ScoredNode sn : scoredNodes) {
        		Node n = getShard().getNode(sn.getNodeId());
        		sb.append(sn.getScore()+" :: "+href(sn.getNodeId()) + " " + n.getLabel() +" "+ " source: "+n.getSourceId());
        		sb.append("\n");
             }
        	sb.append(hrefToOtherFormats("/nodes/"+getNodeId()+"/blast/"));
        	 
         	sb.append("</pre>");
         	result = new StringRepresentation(sb, MediaType.TEXT_HTML);
        }

        return result;
    }
    


}
