package com.rga78.javadoc;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.junit.Rule;
import org.junit.Test;

import com.rga78.junit.CaptureSystemOutRule;
import com.rga78.log.Log;
import com.rga78.proc.ProcessHelper;
import com.rga78.proc.ProcessHelper.Stream;
import com.rga78.proc.StreamCollector;

/**
 * 
 */
public class SimpleJavadocProcessTest {
    
    /**
     * Capture and suppress stdout unless the test fails.
     */
    @Rule
    public CaptureSystemOutRule systemOutRule  = new CaptureSystemOutRule( );
    
    /**
     * 
     */
    @Test
    public void testBuildCommand() throws Exception {
        
        File sourcePath = new File("src/test/java");
        SimpleJavadocProcess javadocProcess = new SimpleJavadocProcess()
                                                    .setSourcePath( sourcePath )
                                                    .setPackages( Arrays.asList( "com.rga78.javadoc.test", "com.rga78.coll" ) );
        
        List<String> expectedCommand = new ArrayList<String>( Arrays.asList( new String[] { "javadoc", 
                                                                                            "-docletpath",
                                                                                            SimpleJavadocProcess.buildMavenDocletPath(),
                                                                                            "-doclet",
                                                                                            JsonDoclet.class.getCanonicalName(),
                                                                                            "",
                                                                                            "-J-Xms1024m",
                                                                                            "-J-Xmx4096m",
                                                                                            "-sourcepath",
                                                                                            sourcePath.getCanonicalPath(),
                                                                                            "com.rga78.javadoc.test",
                                                                                            "com.rga78.coll"
                                                                                          } ) );
        assertEquals(expectedCommand, javadocProcess.buildCommand());
    }


    /**
     * 
     */
    @Test
    public void testBuildCommandWithSubpackages() throws Exception {
        
        File sourcePath = new File("src/test/java");
        SimpleJavadocProcess javadocProcess = new SimpleJavadocProcess()
                                                    .setSourcePath( sourcePath )
                                                    .setSubpackages( Arrays.asList( "com.rga78.javadoc.test", "com.rga78.coll" ) );
        
        List<String> expectedCommand = new ArrayList<String>( Arrays.asList( new String[] { "javadoc", 
                                                                                            "-docletpath",
                                                                                            SimpleJavadocProcess.buildMavenDocletPath(),
                                                                                            "-doclet",
                                                                                            JsonDoclet.class.getCanonicalName(),
                                                                                            "",
                                                                                            "-J-Xms1024m",
                                                                                            "-J-Xmx4096m",
                                                                                            "-sourcepath",
                                                                                            sourcePath.getCanonicalPath(),
                                                                                            "-subpackages",
                                                                                            "com.rga78.javadoc.test",
                                                                                            "-subpackages",
                                                                                            "com.rga78.coll"
                                                                                          } ) );
        assertEquals(expectedCommand, javadocProcess.buildCommand());
    }

    /**
     * 
     */
    @Test
    public void testJavadoc() throws Exception {
        
        
        File sourcePath = new File("src/test/java");

        StreamCollector streamCollector = new StreamCollector();
        
        ProcessHelper javadocProcess = new SimpleJavadocProcess()
                                                    .setSourcePath( sourcePath )
                                                    .setPackages( Arrays.asList( "com.rga78.javadoc.test" ) )
                                                    .setQuiet(true)
                                                    .buildProcessHelper()
                                                    .addObserver( Stream.STDOUT, streamCollector )
                                                    .spawnStreamReaders()
                                                    .waitFor();

        // For manual verification...
        // FileUtils.write( new File("test.out"), StringUtils.join( out.getLeft(), "\n") );
        Log.trace(this, "test: ", streamCollector.getOutput());
        
        JSONArray doc = (JSONArray) new JSONParser().parse( "[" + StringUtils.join(streamCollector.getOutput(), "" ) + "]" );
        
        // The package is added last.
        assertFalse( doc.isEmpty() );
        assertEquals( JsonDocletTest.ExpectedTestJavadocSize, doc.size() );
        assertEquals( "package", ((JSONObject)doc.get(doc.size()-1)).get("metaType"));
        assertEquals( "com.rga78.javadoc.test", ((JSONObject)doc.get(doc.size()-1)).get("name"));
    }
    

    /**
     * 
     */
    // TODO: @Test
    public void testJavadocWithSubpackages() throws Exception {

    }

    
}

