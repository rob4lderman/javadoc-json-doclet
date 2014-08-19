
package com.rga78.javadoc;


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import java.io.File;
import java.util.Arrays;

import org.apache.commons.lang3.StringUtils;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.integration.junit4.JUnit4Mockery;
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
import com.sun.javadoc.MethodDoc;

/**
 *
 */
public class JsonDocletTest {

    /**
     * The expected number of javadoc elements processed from src/test/java/com/rga78/javadoc/test.
     * (This number is used in several tests to verify the doclet.
     */
    public static final int ExpectedTestJavadocSize = 39;
 
    /**
     * Capture and suppress stdout unless the test fails.
     */
    @Rule
    public CaptureSystemOutRule systemOutRule  = new CaptureSystemOutRule( );
    
    /**
     *
     */
    @Test
    public void test() throws Exception {

        File sourcePath = new File("src/test/java");
        
        StreamCollector streamCollector = new StreamCollector();

        ProcessHelper javadocProcess = new SimpleJavadocProcess()
                                                    .setDocletClass( JsonDoclet.class )
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
        assertEquals( ExpectedTestJavadocSize, doc.size() );
        assertEquals( "package", ((JSONObject)doc.get(doc.size()-1)).get("metaType"));
        assertEquals( "com.rga78.javadoc.test", ((JSONObject)doc.get(doc.size()-1)).get("name"));
    }
    
    /**
     * 
     */
    @Test
    public void testGetInheritedCommentText() throws Exception {
        
        Mockery mockery = new JUnit4Mockery();
        
        final MethodDoc methodDoc1 = mockery.mock(MethodDoc.class, "methodDoc1");
        final MethodDoc methodDoc2 = mockery.mock(MethodDoc.class, "methodDoc2"); // parent
        
        mockery.checking(new Expectations() {
            {
                oneOf(methodDoc1).overriddenMethod();
                will(returnValue(methodDoc2));
                
                oneOf(methodDoc2).overriddenMethod();
                will(returnValue(null));
                
                oneOf(methodDoc1).commentText();
                will(returnValue("methodDoc1 commentText: {@inheritDoc}"));
                
                oneOf(methodDoc2).commentText();
                will(returnValue("methodDoc2 commentText"));
            }
        });

        
        assertEquals( "methodDoc1 commentText: methodDoc2 commentText",
                      new JsonDoclet(null).getInheritedCommentText(methodDoc1, null) );

    }
    
    /**
     * 
     */
    @Test
    public void testGetInheritedCommentTextFromSpecifiedByMethod() throws Exception {
        
        Mockery mockery = new JUnit4Mockery();
        
        final MethodDoc methodDoc1 = mockery.mock(MethodDoc.class, "methodDoc1");
        final MethodDoc methodDoc2 = mockery.mock(MethodDoc.class, "methodDoc2"); // parent
        final MethodDoc specifiedByMethodDoc = mockery.mock(MethodDoc.class, "methodDoc3"); // parent
        
        mockery.checking(new Expectations() {
            {
                oneOf(methodDoc1).overriddenMethod();
                will(returnValue(methodDoc2));
                
                oneOf(methodDoc2).overriddenMethod();
                will(returnValue(null));
                
                oneOf(methodDoc1).commentText();
                will(returnValue("methodDoc1 commentText: {@inheritDoc}"));
                
                oneOf(methodDoc2).commentText();
                will(returnValue("methodDoc2 commentText: {@inheritDoc}"));
                
                oneOf(specifiedByMethodDoc).commentText();
                will(returnValue("specifiedByMethodDoc commentText"));
            }
        });

        
        assertEquals( "methodDoc1 commentText: methodDoc2 commentText: specifiedByMethodDoc commentText",
                      new JsonDoclet(null).getInheritedCommentText(methodDoc1, specifiedByMethodDoc) );

    }

}


