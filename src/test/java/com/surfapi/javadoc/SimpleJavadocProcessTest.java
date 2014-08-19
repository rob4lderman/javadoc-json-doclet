package com.surfapi.javadoc;

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

import com.surfapi.junit.CaptureSystemOutRule;
import com.surfapi.log.Log;
import com.surfapi.proc.ProcessHelper;
import com.surfapi.proc.ProcessHelper.Stream;
import com.surfapi.proc.StreamCollector;

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
                                                    .setPackages( Arrays.asList( "com.surfapi.test", "com.surfapi.coll" ) );
        
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
                                                                                            "com.surfapi.test",
                                                                                            "com.surfapi.coll"
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
                                                    .setSubpackages( Arrays.asList( "com.surfapi.test", "com.surfapi.coll" ) );
        
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
                                                                                            "com.surfapi.test",
                                                                                            "-subpackages",
                                                                                            "com.surfapi.coll"
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
                                                    .setPackages( Arrays.asList( "com.surfapi.test" ) )
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
        assertEquals( "com.surfapi.test", ((JSONObject)doc.get(doc.size()-1)).get("name"));
    }
    

    /**
     * 
     */
    // TODO: @Test
    public void testJavadocWithSubpackages() throws Exception {

    }

    
}

