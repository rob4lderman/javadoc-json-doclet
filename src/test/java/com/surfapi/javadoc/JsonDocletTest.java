
package com.surfapi.javadoc;


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import java.io.File;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.junit.Test;

/**
 *
 */
public class JsonDocletTest {

 
    /**
     *
     */
    @Test
    public void test() throws Exception {

        // assertEquals("C:\\easy\\mysandbox\\javadoc-parser", System.getProperty("user.dir") );

        Pair<List<String>,List<String>> out = new JavadocProcessForTesting( new File("src/test/java/com/surfapi/test") ).run();
        
        // For manual verification...
        // FileUtils.write( new File("test.out"), StringUtils.join( out.getLeft(), "\n") );
        
        JSONArray doc = (JSONArray) new JSONParser().parse( "[" + StringUtils.join( out.getLeft(), "" ) + "]" );
        
        // The package is added last.
        assertFalse( doc.isEmpty() );
        assertEquals( 27, doc.size() );
        assertEquals( "package", ((JSONObject)doc.get(doc.size()-1)).get("metaType"));
        assertEquals( "com.surfapi.test", ((JSONObject)doc.get(doc.size()-1)).get("name"));
    }
    


}
