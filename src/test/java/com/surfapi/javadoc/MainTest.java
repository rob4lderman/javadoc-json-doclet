package com.surfapi.javadoc;

import static org.junit.Assert.assertNotNull;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;

import org.apache.commons.io.filefilter.TrueFileFilter;
import org.json.simple.JSONArray;
import org.json.simple.parser.JSONParser;
import org.junit.Test;

import com.surfapi.coll.Cawls;
import com.surfapi.json.JSONObjectBuilder;

/**
 * 
 */
public class MainTest {

    @Test
    public void test() throws Exception {
        
        File outputFile = new File("target/com.surfapi_1.0.json" );
        new Main()
                .setDocletPath( JavadocProcessForTesting.buildTestDocletPath() )
                .setOutputStream( new FileOutputStream( outputFile ))
                .setDirFilter( TrueFileFilter.INSTANCE )
                .go( new String[] { "src/main/java/com/surfapi/proc", "src/test/java/com/surfapi/test" });
        
        // Verify at a minimum that it can be successfully parsed 
        JSONArray doc = (JSONArray) new JSONParser().parse( new FileReader( outputFile ) );
        
        // Verify the two packages are in there.
        assertNotNull( Cawls.findFirst( doc, new JSONObjectBuilder().append( "name", "com.surfapi.proc" ) ) );
        assertNotNull( Cawls.findFirst( doc, new JSONObjectBuilder().append( "name", "com.surfapi.test" ) ) );
    }

    /**
     * 
     */
    @Test
    public void testOnJdk16() throws Exception {
        
        File outputFile = new File("target/java-sdk_1.6.json" );
        new Main()
                .setDocletPath( JavadocProcessForTesting.buildTestDocletPath() )
                .setOutputStream( new FileOutputStream( outputFile ))
                .go( new String[] { "/fox/tmp/javadoc/jdk6.src/jdk/src/share/classes/java/lang",
                                    "/fox/tmp/javadoc/jdk6.src/jdk/src/share/classes/java/net",
                                    "/fox/tmp/javadoc/jdk6.src/jdk/src/share/classes/java/util",
                                    "/fox/tmp/javadoc/jdk6.src/jdk/src/share/classes/java/io"
                                  });
        
        // Verify at a minimum that it can be successfully parsed 
        JSONArray doc = (JSONArray) new JSONParser().parse( new FileReader( outputFile ) );
        
        // Verify the java.lang package is in there.
        assertNotNull( Cawls.findFirst( doc, new JSONObjectBuilder().append( "name", "java.lang" ) ) );
        assertNotNull( Cawls.findFirst( doc, new JSONObjectBuilder().append( "name", "java.net" ) ) );
        assertNotNull( Cawls.findFirst( doc, new JSONObjectBuilder().append( "name", "java.util" ) ) );
        assertNotNull( Cawls.findFirst( doc, new JSONObjectBuilder().append( "name", "java.util.concurrent" ) ) );
        assertNotNull( Cawls.findFirst( doc, new JSONObjectBuilder().append( "name", "java.io" ) ) );
        
    }
}
