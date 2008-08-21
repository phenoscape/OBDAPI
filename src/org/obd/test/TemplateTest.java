package org.obd.test;


import java.io.File;
import java.io.FileWriter;
import java.util.Map;
import java.util.TreeMap;

import junit.framework.TestCase;

import org.restlet.data.MediaType;
import org.restlet.ext.freemarker.TemplateRepresentation;

import freemarker.template.Configuration;

/**
 * Unit test for the FreeMarker extension.
 * 
 * @author Jerome Louvel (contact@noelios.com)
 */
public class TemplateTest extends TestCase {
    public static void main(String[] args) {
        try {
            new TemplateTest().testTemplate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void testTemplate() throws Exception {
    	// Create a temporary directory for the tests
    	File testDir = new File(System.getProperty("java.io.tmpdir"),
    	"FreeMarkerTestCase");
    	testDir.mkdir();

    	// Create a temporary template file
    	File testFile = File.createTempFile("test", ".ftl", testDir);
    	FileWriter fw = new FileWriter(testFile);
    	fw.write("Value=${value}");
    	fw.close();

    	Configuration fmc = new Configuration();
    	fmc. setDirectoryForTemplateLoading(testDir);
    	Map<String, Object> map = new TreeMap<String, Object>();
    	map.put("value", "myValue");

    	String result = new TemplateRepresentation(testFile.getName(), fmc,
    			map, MediaType.TEXT_PLAIN).getText();
    	assertEquals("Value=myValue", result);

    	// Clean-up
    	testFile.delete();
    	testDir.delete();

    	//String path = "test_resources/test.ftl";
    	String path = "bin/org/obd/ws/pages/UseCase1.ftl";
    	fmc = new Configuration();
    	//fmc.setDirectoryForTemplateLoading("/Users/cjm/Eclipse/workspace/OBDAPI/bin/");
    	//fmc.setDirectoryForTemplateLoading(testDir);
    	map = new TreeMap<String, Object>();
    	TreeMap<String, Object> imap = new TreeMap<String, Object>();
       	imap.put("k", "myValue");
       	map.put("kv", "k");
       	map.put("h", imap);

    	result = new TemplateRepresentation(path, fmc,
    			map, MediaType.TEXT_PLAIN).getText();
    	System.out.println(result);
    	//assertEquals("Value=myValue", result);


    }

}