
package org.obd.ws;


import org.restlet.Context;
import org.restlet.data.MediaType;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.resource.Representation;
import org.restlet.resource.StringRepresentation;
import org.restlet.resource.Variant;

/**
 * Resource for a top level page
 */
public class HomeResource extends OBDResource {


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
	public HomeResource(Context context, Request request, Response response) {
		super(context, request, response);
		getVariants().add(new Variant(MediaType.TEXT_PLAIN));
	}


	@Override
	public Representation getRepresentation(Variant variant) {
		Representation result = null;

		StringBuilder sb = new StringBuilder();
		sb.append("<pre>");
		sb.append("------------\n");
		sb.append("OBD\n");
		sb.append("------------\n\n");
		sb.append("This is the RESTful interface to an OBD shard\n");
		sb.append("All resources in this shard should be accessible via a URL\n");
		sb.append("For metadata on this shard see: "+example("/html/metadata/")+" (slow)\n");
		sb.append("\n");
		sb.append("\n");

		sb.append("Use Cases:\n");
		sb.append("~~~~~~~~~~\n\n");
		sb.append("  <a href='/usecases/index.html'>Use Cases</a> \n");
		sb.append("\n");
		sb.append("\n");

		sb.append("GET Patterns Implemented:\n");
		sb.append("~~~~~~~~~~~~~~~~~~~~\n\n");

		sb.append("  {format}/nodes/{id}\n");
		sb.append("     A Node : can be class instance or relation\n");
		sb.append("     Example: "+example("/html/nodes/CL:0000148")+"\n");
		sb.append("     Example: "+example("/json/nodes/MP:0001306")+"\n");
		sb.append("     Example: "+example("/json/nodes/entrezgene:2138")+"\n");
		sb.append("     Example: "+example("/obdxml/nodes/WB%3AWBGene00001377")+"\n");
		sb.append("     Example: "+example("/json/nodes/OMIM:601653.0008")+"\n");
		sb.append("     Example: "+example("/html/nodes/PATO:0000963^OBO_REL:inheres_in(FMA:58238)")+"\n");
		sb.append("     Example: "+example("/owl/nodes/CL:0000100")+"\n");
		sb.append("     TODO: concatenate multiple using +\n");
		sb.append("\n");

		sb.append("  {format}/nodes/{id}/statements\n");
		sb.append("     All statements ABOUT a node\n");
		sb.append("     Synonymous with: {format}/nodes/{id}/statement/about\n");
		sb.append("     Example: "+example("/html/nodes/CL:0000148/statements")+"\n");
		sb.append("     Example: "+example("/json/nodes/MP:0001306/statements")+"\n");
		sb.append("     Example: "+example("/owl/nodes/MP:0001306/statements")+"\n");
		sb.append("     Example: "+example("/obo/nodes/MP:0001306/statements")+"\n");
		sb.append("     Example: "+example("/json/nodes/OMIM:601653.0008/statements")+"\n");
		sb.append("     Example: "+example("/json/nodes/OWB%3AWBGene00001377/statements")+"\n");

		sb.append("\n");


		sb.append("  {format}/nodes/{id}/statements/{aspect}/\n");
		sb.append("  {format}/nodes/{id}/statements/{aspect}/{relation}\n");
		sb.append("     All statements concerning a node in some way, optionally filtered by relation\n");
		sb.append("     Aspect = about OR to OR all\n");
		sb.append("     Example: "+example("/html/nodes/CL:0000148/statements/about")+"\n");
		sb.append("     Example: "+example("/html/nodes/CL:0000148/statements/to")+"\n");
		sb.append("     Example: "+example("/html/nodes/CL:0000148/statements/annotations")+"\n");
		sb.append("     Example: "+example("/html/nodes/PATO:0000963^OBO_REL:inheres_in(FMA:58238)/statements/to")+"\n");
		sb.append("     Example: "+example("/html/nodes/CL:0000148/statements/to/inheres_in")+"\n");
		sb.append("     Example: "+example("/html/nodes/CL:0000148/statements/about/OBO_REL:is_a")+"\n");
		sb.append("     Example: "+example("/html/nodes/CL:0000148/statements/all")+"\n");
		sb.append("     Example: "+example("/json/nodes/entrezgene:2138/statements/all")+"\n");
		sb.append("     Example: "+example("/json/nodes/MP:0005099/statements/all")+"\n");
		sb.append("     Example: "+example("/json/nodes/MP:0002877/statements/annotations")+"\n");
		sb.append("     Example: "+example("/obo/nodes/PATO:0000963^OBO_REL:inheres_in(FMA:58238)/statements/to")+"\n");
		sb.append("     Example: "+example("/html/nodes/omim_phenotype_fb/statements/source/")+"\n");
		sb.append("     Example: "+example("/obo/nodes/CL:0000100/statements/annotations")+"\n");

		sb.append("\n");

		sb.append("  {format}/nodes/{id}/graph\n");
		sb.append("  {format}/nodes/{id}/graph/{aspect}\n");
		sb.append("     Graph around node; deductive closure to roots\n");
		sb.append("     Example: "+example("/json/nodes/MP:0001306/graph")+"\n");
		sb.append("     Example: "+example("/html/nodes/MP:0001306/graph")+"\n");
		sb.append("     Example: "+example("/obo/nodes/MP:0001306/graph")+"\n");
		sb.append("     Example: "+example("/html/nodes/MP:0001306/graph")+"\n");
		sb.append("     Example: "+example("/html/nodes/ZFIN:ZDB-GENO-980202-1557/graph")+"\n");
		sb.append("     Example: "+example("/html/nodes/MP:0001306/graph")+"\n");
		sb.append("     Example: "+example("/html/nodes/Sox10%3Ctm3%28Sox8%29Weg%3E%2FSox10%3Ctm3%28Sox8%29Weg%3E/graph")+"\n");
		sb.append("     Example: "+example("/html/nodes/CL:0000100/graph/annotations")+"\n");
		sb.append("\n");
		sb.append("  {format}/nodes/{id}/description\n");
		sb.append("     Composite description (class expression, post-composition)\n");
		sb.append("     Example: "+example("/json/nodes/MP:0001306/description")+"\n");
		sb.append("     Example: "+example("/obo/nodes/MP:0001306/description")+"\n");
		sb.append("     Example: "+example("/owl/nodes/MP:0001306/description")+"\n");
		sb.append("     Example: "+example("/html/nodes/MP:0004176/description")+"\n");
		sb.append("     Example: "+example("/html/nodes/PATO:0000963^OBO_REL:inheres_in(FMA:58238)/description")+"\n");
		sb.append("     Example: "+example("/obo/nodes/PATO:0000963^OBO_REL:inheres_in(FMA:58238)/description")+"\n");
		sb.append("     Example: "+example("/owl/nodes/PATO:0000963^OBO_REL:inheres_in(FMA:58238)/description")+"\n");
		sb.append("\n");

		sb.append("  {format}/nodes/{id}/blast\n");
		sb.append("     Find similar annotated entity nodes, based on annotations\n");
		sb.append("     Example: "+example("/json/nodes/OMIM:601653.0008/blast")+"\n");
		sb.append("     Example: "+example("/json/nodes/ZFIN:ZDB-GENO-980410-322/blast")+"\n");
		sb.append("     Example: "+example("/json/nodes/ZFIN:ZDB-GENO-980202-1557/blast")+" -- what is similar to eya1?\n");
		sb.append("     Example: "+example("/json/nodes/Sox10%3Ctm3%28Sox8%29Weg%3E%2FSox10%3Ctm3%28Sox8%29Weg%3E/blast")+" -- what is similar to mouse SOX10?\n");

		sb.append("\n");

		sb.append("  {format}/nodes/{id}/annotation-graph\n");
		sb.append("     Graph around annotations to node\n");             
		sb.append("     TODO\n");
		sb.append("     Example: "+example("/json/nodes/MP:0001306/annotation-graph")+"\n");
		sb.append("\n");

		sb.append("  {format}/search/{searchTerm}\n");
		sb.append("     Matching nodes (defaults to exact match)\n");
		sb.append("     Example: "+example("/html/search/matches/eye")+"\n");
		sb.append("     Example: "+example("/html/search/matches/Eye")+"\n");
		sb.append("     Example: "+example("/html/search/matches/eya1")+"\n");
		sb.append("     Example: "+example("/html/search/matches/EYA1")+"\n");
		sb.append("     Example: "+example("/html/search/matches/EYA1+SHH")+"\n"); // TODO: quote phrases
		sb.append("     Example: "+example("/html/search/matches/cell")+"\n");
		sb.append("\n");

		sb.append("  {format}/search/{comparisonOperator}/{searchTerm}\n");
		sb.append("     Matching nodes; operator = starts_with OR contains\n");
		sb.append("     Example: "+example("/html/search/contains/melanocyte")+"\n");
		sb.append("     Example: "+example("/json/search/contains/melanocyte")+"\n");
		sb.append("     Example: "+example("/obo/search/contains/leukocyte")+"\n");
		sb.append("     Example: "+example("/owl/search/contains/melanocyte")+"\n");
		sb.append("     Example: "+example("/html/search/starts_with/eye")+"\n");
		sb.append("     Example: "+example("/obo/search/starts_with/eye")+"\n");
		sb.append("     Example: "+example("/html/search/contains/eye")+"\n");
		sb.append("     Example: "+example("/html/search/starts_with/eya")+"\n");
		sb.append("     Example: "+example("/html/search/starts_with/OMIM:601653")+"\n");
		sb.append("\n");

		sb.append("  {format}/search/[{aspect}]{comparisonOperator}/{searchTerm}   -- TODO; eg search by synonym\n");
		sb.append("     TODO\n");
		sb.append("\n");

		sb.append("  {format}/nodes/{id1+id2+...+idN} -- TODO\n");
		sb.append("     A list of nodes -- TODO\n");
		sb.append("     Example: "+example("/html/nodes/CL:0000148+CL:0000541")+"\n");
		sb.append("     Example: "+example("/json/nodes/MP:0001306+FMA:58241")+"\n");
		sb.append("\n");
		sb.append("  {format}/nodes/{id1+id2+...+idN}/statements -- TODO\n");
		sb.append("     Statements involving the union of above IDs\n");
		sb.append("     Example: "+example("/html/nodes/CL:0000148+CL:0000541/statements")+"\n");
		sb.append("     Example: "+example("/json/nodes/MP:0001306+FMA:58241/statements")+"\n");
		sb.append("\n");
		sb.append("  {format}/nodes/{id1+id2+...+idN}/graph -- TODO\n");
		sb.append("     Graph involving the union of above IDs\n");
		sb.append("\n");
		sb.append("  {format}/nodes/{id1^id2^...^idN}/graph -- TODO\n");
		sb.append("     Graph involving the union of above IDs\n");
		sb.append("\n");
		sb.append("  {format}/nodes/{id1+id2+...+idN}/annotations -- TODO\n");
		sb.append("     Annotations to the union of above IDs\n");
		sb.append("\n");
		sb.append("  {format}/nodes/{id1^id2^...^idN}/annotations -- TODO\n");
		sb.append("     Annotations to (entities with one of all of?) intersection of above IDs\n");
		sb.append("\n");


		sb.append("  / - this page\n");
		sb.append("  {format}/metadata\n");
		sb.append("  {format}/source/{id}   -- TODO\n");
		sb.append("  {format}/source/{id}/nodes   -- TODO\n");
		sb.append("  {format}/source/{id}/graph   -- TODO\n");
		sb.append("  {format}/source/{id}/summary   -- TODO\n");
		sb.append("  {format}/relation/{id}   -- TODO\n");
		sb.append("  {format}/nodes-by-query/{obdQuery}   -- TODO\n");
		sb.append("  {format}/nodes-by-query/{obdQuery}/graph   -- TODO\n");
		sb.append("  {format}/hits-by-co-annotated/{id}   -- TODO\n");
		sb.append("  {format}/hits-by-semantic-similarity/{id}   -- TODO\n");
		sb.append("\n");

		sb.append("\n");

		sb.append("Formats:\n");
		sb.append("~~~~~~~~~~~~~~~~~~~~\n\n");
		sb.append(" - html : ultra basic information on resource\n");
		sb.append(" - json : OBD-JSON (not finalized)\n");
		sb.append(" - owl : [partial implementation]\n");
		sb.append(" - obo : [partial implementation] \n");
		sb.append(" - obdxml : TODO - next \n");
		sb.append(" - tab : tabular \n");
		sb.append("\n");
		sb.append("\n");

		sb.append("Controlling number of results:\n");
		sb.append("~~~~~~~~~~~~~~~~~~~~~~~~~~~~\n\n");
		sb.append(" [TODO]\n");
		sb.append("Fetching counts:\n");
		sb.append("  Append /count -- EXAMPLE: /json/nodes/CL:0000000/statements/count:\n");
		sb.append("Use query params for limits/cursors:\n");
		sb.append("    ?limit=100:\n");
		sb.append("    ?from=200&limit=100:\n");
		sb.append("\n");

		sb.append("PUT Patterns:\n");
		sb.append("~~~~~~~~~~~~~~~~~~~~\n\n");
		sb.append(" class enrichment TODO\n");
		sb.append(" upload TODO\n");
		sb.append(" mapping TODO\n");
		sb.append("\n");


		sb.append("<pre>");
		result = new StringRepresentation(sb, MediaType.TEXT_HTML);
		return result;
	}

	public String example(String path) {
		return "<a href=\""+path+"\">"+path+"</a>";
	}

}
