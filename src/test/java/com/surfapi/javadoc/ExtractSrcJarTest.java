package com.surfapi.javadoc;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.junit.Test;

import com.surfapi.proc.ProcessHelper;

/**
 * 
 */
public class ExtractSrcJarTest {
    
    
    /**
     * 
     */
    @Test
    public void testExtractJar() throws Exception {
        
        // First make the jarfile.
        File jarFile = createTestJar("test.jar");
        jarFile.deleteOnExit();
        
        ExtractSrcJar extractSrcJar = new ExtractSrcJar(jarFile);
        
        // returns the directory in which the jar was extracted.
        // JavadocProcess will process all *.java files under that dir.
        File extractDir = extractSrcJar.extract();
        
        // Run javadoc against the extracted src
        Pair<List<String>,List<String>> out = new JavadocProcessForTesting( extractDir ).run();
        
        JSONArray doc = (JSONArray) new JSONParser().parse( "[" + StringUtils.join( out.getLeft(), "" ) + "]" );
        
        // The package is added last.
        assertFalse( doc.isEmpty() );
        assertEquals( "package", ((JSONObject)doc.get(doc.size()-1)).get("metaType"));
        assertEquals( "com.surfapi.test", ((JSONObject)doc.get(doc.size()-1)).get("name"));
        
        // for manual debugging...
        // FileUtils.write( new File("testExtractJar.out"), StringUtils.join( out.getLeft(), "\n") );
        
        // Delete directory recursively
        FileUtils.deleteDirectory( extractDir );
    }
    
    /**
     * 
     */
    @Test
    public void testExtractTar() throws Exception {
        
        // First make the jarfile.
        File tarFile = createTestTar("test.tar");
        tarFile.deleteOnExit();
        
        ExtractSrcJar extractSrcJar = new ExtractSrcJar(tarFile);
        
        // returns the directory in which the jar was extracted.
        // JavadocProcess will process all *.java files under that dir.
        File extractDir = extractSrcJar.extract();
        
        // Run javadoc against the 
        Pair<List<String>,List<String>> out = new JavadocProcessForTesting( extractDir ).run();
        
        JSONArray doc = (JSONArray) new JSONParser().parse( "[" + StringUtils.join( out.getLeft(), "" ) + "]" );
        
        // The package is added last.
        assertFalse( doc.isEmpty() );
        assertEquals( "package", ((JSONObject)doc.get(doc.size()-1)).get("metaType"));
        assertEquals( "com.surfapi.test", ((JSONObject)doc.get(doc.size()-1)).get("name"));
        
        // For manual debugging...
        // FileUtils.write( new File("testExtractTar.out"), StringUtils.join( out.getLeft(), "\n") );
        
        // Delete the directory recursively
        FileUtils.deleteDirectory( extractDir );
                
    }
    
    private File createTestJar(String jarFileName) throws IOException, InterruptedException {
        
        Process p = new ProcessBuilder("jar", "-cf", jarFileName, "-C", "src/test/java", "com/surfapi/test").start();
        new ProcessHelper(p).waitFor();
        
        return new File(jarFileName);
    }
    
    private File createTestTar(String tarFileName) throws IOException, InterruptedException {
        
        Process p = new ProcessBuilder("tar", "-cf", tarFileName, "src/test/java/com/surfapi/test").start();
        new ProcessHelper(p).waitFor();
        
        return new File(tarFileName);
    }

}
