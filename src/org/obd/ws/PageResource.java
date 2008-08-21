
package org.obd.ws;


import java.util.TreeMap;

import org.restlet.Context;
import org.restlet.data.MediaType;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.ext.freemarker.TemplateRepresentation;
import org.restlet.resource.Representation;
import org.restlet.resource.Variant;

import freemarker.template.Configuration;

/**
 * Resource for a page (fetched from src)
 */
public class PageResource extends OBDResource {

     private String page;


	/**
     * Constructor.
     * 
     * @param context
     *                The parent context.
     * @param request
     *                The request to handle.
     * @param response
     *                The response to return.
     */
    public PageResource(Context context, Request request, Response response) {
        super(context, request, response);
        this.page = (String) request.getAttributes().get("page");
        if (request.getAttributes().get("usecase") != null &&
        		!request.getAttributes().get("usecase").equals("")) {
        	page = "UseCase"+request.getAttributes().get("usecase");
        }
         if (page != null) {
            getVariants().add(new Variant(MediaType.TEXT_PLAIN));
        }
    }

    @Override
    public Representation getRepresentation(Variant variant) {
    	Representation result = null;
  		TreeMap<String, Object> map = new TreeMap<String, Object>();
		map.put("baseUri", this.getRequest().getResourceRef(). getParentRef().toString());
    	return getTemplateRepresentation(page,map);
    	/*
    	try {
      		String path = "bin/org/obd/ws/pages/"+page+".ftl";
    		Configuration fmc = new Configuration();
    		TreeMap<String, Object> map = new TreeMap<String, Object>();
    		map.put("baseUri", this.getRequest().getResourceRef(). getParentRef().toString());

    		result = new TemplateRepresentation(path, fmc,
    				map, MediaType.TEXT_HTML);
    	} catch (Exception ex) {
    		result = null;
    	}
    	return result;
    	*/
    }
    

}
