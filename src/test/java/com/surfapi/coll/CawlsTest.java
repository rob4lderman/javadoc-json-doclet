package com.surfapi.coll;

import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;

import java.util.Collection;
import java.util.Map;

import org.junit.Test;

/**
 * 
 */
public class CawlsTest {
    
    @Test
    public void testFindFirst() {
        
        Map<String, String> findMe1 = new MapBuilder<String, String>()
                                            .append("george", "costanza")
                                            .append("jerry", "seinfeld");
        Map<String, String> findMe2 = new MapBuilder<String, String>()
                                            .append("larry", "david")
                                             .append("jerry", "seinfeld");
        
        Collection<Map> collection = new ListBuilder<Map>().append( new MapBuilder<String, String>()
                                                                            .append("key1", "value1")
                                                                            .append("key2", "value2"))
                                                            .append(findMe1)
                                                            .append(findMe2);
                                       
        
        assertSame(findMe1, Cawls.findFirst(collection, new MapBuilder().append("jerry", "seinfeld")));
        assertSame(findMe2, Cawls.findFirst(collection, new MapBuilder().append("jerry", "seinfeld")
                                                                        .append("larry", "david")));
        assertNull(Cawls.findFirst(collection, new MapBuilder().append("jerry", "seinfeldxx")));
    }

}
