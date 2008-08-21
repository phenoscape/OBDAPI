package org.obd.test;


import java.util.TreeMap;

import junit.framework.TestSuite;

import org.obd.model.Graph;
import org.obd.model.Node;
import org.obd.query.AnnotationLinkQueryTerm;
import org.restlet.data.MediaType;
import org.restlet.ext.freemarker.TemplateRepresentation;

import freemarker.ext.beans.BeansWrapper;
import freemarker.template.Configuration;

/**
 * Unit test for the FreeMarker extension.
 * 
 * @author Jerome Louvel (contact@noelios.com)
 */
public class FreeMarkerTest extends AbstractOBDTest {
    public FreeMarkerTest(String n) {
		super(n);
		// TODO Auto-generated constructor stub
	}

	public static void addTests(TestSuite suite) {
		suite.addTest(new FreeMarkerTest("testTemplate"));
	}
	
	public void testTemplate() throws Exception {
		//AnnotationLinkQueryTerm q = new AnnotationLinkQueryTerm("MP:0001306");
		AnnotationLinkQueryTerm q = new AnnotationLinkQueryTerm("CL:0000148");
		Graph graph = shard.getGraphByQuery(q, null, null);

		String path = "bin/org/obd/ws/presentation/AnnotationView.ftl";
		Configuration fmc = new Configuration();
		BeansWrapper wrapper = new BeansWrapper();
        // wrapper.setMethodsShadowItems(false);
        wrapper.setExposureLevel(BeansWrapper.EXPOSE_ALL);
        fmc.setObjectWrapper(wrapper);
		//fmc.setDirectoryForTemplateLoading("/Users/cjm/Eclipse/workspace/OBDAPI/bin/");
		//fmc.setDirectoryForTemplateLoading(testDir);
		TreeMap<String, Object> map = new TreeMap<String, Object>();
		TreeMap<String, Object> labelMap = new TreeMap<String, Object>();
		map.put("statements", graph.getAllStatements());
		for (Node n : graph.getNodes()) {
			labelMap.put(n.getId(),n.getLabel());
		}
		map.put("graph", graph);
		map.put("labels", labelMap);

		String result = new TemplateRepresentation(path, fmc,
				map, MediaType.TEXT_PLAIN).getText();
		System.out.println(result);
		assertEquals("Value=myValue", result);


	}

}