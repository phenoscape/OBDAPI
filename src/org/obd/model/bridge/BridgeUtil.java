package org.obd.model.bridge;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

public class BridgeUtil {


	
	public static String readFileAsString(File file) throws java.io.IOException{
        StringBuffer fileData = new StringBuffer(1000);
        BufferedReader reader = new BufferedReader(
                new FileReader(file));
        char[] buf = new char[1024];
        int numRead=0;
        while((numRead=reader.read(buf)) != -1){
            fileData.append(buf, 0, numRead);
        }
        reader.close();
        return fileData.toString();
    }
}
