
package org.obd.ws;


import java.util.Map;
import java.util.Set;

import org.obd.model.Graph;
import org.obd.model.LinkStatement;
import org.obd.model.LiteralStatement;
import org.obd.model.Node;
import org.obd.model.Statement;
import org.obd.model.vocabulary.TermVocabulary;
import org.obd.query.Shard;
import org.purl.obo.vocab.RelationVocabulary;
import org.restlet.Context;
import org.restlet.data.Form;
import org.restlet.data.MediaType;
import org.restlet.data.Reference;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.ext.freemarker.TemplateRepresentation;
import org.restlet.resource.Representation;
import org.restlet.resource.Resource;
import org.restlet.resource.Variant;

import freemarker.cache.ClassTemplateLoader;
import freemarker.ext.beans.BeansWrapper;
import freemarker.template.Configuration;

/**
 * Root class for all OBD resources
 */
public abstract class OBDResource extends Resource {

	protected Form form;
	protected String queryString;
	protected int limit;
	protected int from;
	protected Set<String> imports;
	protected RelationVocabulary rvocab = new RelationVocabulary();
	protected TermVocabulary tvocab = new TermVocabulary();

	public OBDResource(Context context, Request request, Response response) {
		super(context, request, response);
		getVariants().add(new Variant(MediaType.TEXT_XML));

		queryString = request.getResourceRef().getQuery();
		form = request.getResourceRef().getQueryAsForm();
		String limitVal = form.getFirstValue("limit");
		if (limitVal != null)
			limit = Integer.parseInt(limitVal);
		String fromVal = form.getFirstValue("from");
		if (fromVal != null)
			limit = Integer.parseInt(fromVal);


	}

	/**
	 * Returns the parent OBDRestApplication.
	 * 
	 * @return the parent OBDRestApplication.
	 */
	public OBDRestApplication getOBDRestApplication() {
		return (OBDRestApplication) getContext().getAttributes().get(OBDRestApplication.KEY);
	}

	/**
	 * Returns the database container.
	 * 
	 * @return the database container.
	 */
	public Shard getShard() {
		return getOBDRestApplication().getShard();
	}


	public String href(Node node) {
		String id = node.getId();
		return href(id);
	}
	public String href(String id) {
		String eid = Reference.encode(id);
		return "<a href=\"/html/nodes/"+eid+"\">"+id+"</a>";
	}
	public String href(String path, String id) {
		return "<a href=\"/html/"+path+"\">"+id+"</a>";
	}
	public String hrefNodesBySearch(String n) {
		return "<a href=\"/html/nodes-by-search/"+n+"\">"+n+"</a>";
	}
	public String hrefStatementsFor(String id) {
		String eid = Reference.encode(id);

		return "[ <a href=\"/html/nodes/"+eid+"/statements/about\">about</a> | " +
		"<a href=\"/html/nodes/"+eid+"/statements/to\">to</a> | " +
		"<a href=\"/html/nodes/"+eid+"/statements/annotations\">annotations</a> | " +
		"<a href=\"/html/nodes/"+eid+"/statements/all\">all</a> ]";
	}

	public String hrefDescriptionFor(String id) {
		return "<a href=\"/html/nodes/"+id+"/description\">"+id+"</a>";
	}

	public String hrefStatement(Statement s) {
		return  hrefStatement(new Graph(), s);
	}

	public String hrefStatement(Graph g, Statement s) {
		StringBuilder sb = new StringBuilder();


		sb.append(href(s.getNodeId()));
		Node n = g.getNode(s.getNodeId());
		if (n != null && n.getLabel() != null)
			sb.append(" \""+n.getLabel()+"\"");
		sb.append(" --[ ");
		sb.append(href(s.getRelationId()));
		sb.append("]--> ");
		n = g.getNode(s.getTargetId());
		if (n != null && n.getLabel() != null)
			sb.append(" \""+n.getLabel()+"\"");
		if (s instanceof LinkStatement) {
			sb.append(href(s.getTargetId()));
			LinkStatement ls = (LinkStatement)s;
			if (ls.isIntersectionSemantics()) {
				sb.append(" [n+s condition element]");    			
			}
		}
		if (s instanceof LiteralStatement) {
			sb.append(" "+((LiteralStatement)s).getSValue());
		}
		if (s.isInferred())
			sb.append(" [Implied]");
		if (s.getSourceId() != null)
			sb.append(" [source:"+href(s.getSourceId())+"]");
		if (s.getPositedByNodeId() != null)
			sb.append(" [positedBy:"+href(s.getPositedByNodeId())+"]");
		for (Statement ss : s.getSubStatements())
			sb.append(" <<"+hrefStatement(g,ss)+">> ");

		return sb.toString();
	}

	public String hrefToOtherFormats(String res) {
		String[] fmts = {"json","html","obo","owl","obdxml"};
		StringBuilder sb = new StringBuilder();
		int i=0;
		for (String fmt : fmts) {
			if (i>0) {
				sb.append(" | ");
			}
			sb.append("<a href=\"/"+fmt+res+"\">"+fmt+"</a>");
			i++;
		}
		return "\nRelated resources: [ " + sb + " ]\n";
	}


	public String hrefGraph(String id) {
		return "<a href=\"/html/nodes/"+id+"/graph\">"+id+"</a>";
	}

	public Representation getTemplateRepresentation(String view, Map map) {
		// TODO: make this configurable
		// semi-configurable now. Can be overridden in subclass of OBDRestApplication
		String path = getOBDRestApplication().getPagesDirectoryRelativePath();
		System.out.println("path="+path);
		return getTemplateRepresentation(view, map, path);
	}
	public Representation getTemplateRepresentation(String view, Map map, String pathBase) {
		Representation result = null;
		try {
			Configuration fmc = new Configuration();
			fmc.setTemplateLoader(new ClassTemplateLoader(this.getClass(),pathBase));
			BeansWrapper wrapper = new BeansWrapper();
			// wrapper.setMethodsShadowItems(false);
			wrapper.setExposureLevel(BeansWrapper.EXPOSE_ALL);
			fmc.setObjectWrapper(wrapper);
			map.put("baseUri", this.getRequest().getResourceRef(). getParentRef().toString());

			result = new TemplateRepresentation(view+".ftl", fmc,
					map, MediaType.TEXT_HTML);
		} catch (Exception ex) {
			ex.printStackTrace();
			result = null;
		}
		return result;
	}



}
