package com.surfapi.coll;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.json.simple.JSONObject;
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
    
    /**
     * 
     */
    @Test
    public void testPick() {
        
        JSONObject obj1 = new JSONObject();
        obj1.put("key1", 1);
        obj1.put("key2", 2);
        obj1.put("key3", 3);
        obj1.put("key4", 4);
        
        JSONObject obj2 = new JSONObject( Cawls.pick(obj1, Arrays.asList("key2", "key4") ) );
        
        assertTrue( obj2.containsKey("key2") );
        assertTrue( obj2.containsKey("key4") );
        assertFalse( obj2.containsKey("key1") );
        assertFalse( obj2.containsKey("key3") );
        
        assertEquals(2, obj2.get("key2"));
        assertEquals(4, obj2.get("key4"));
        
        obj2.put("key4", 5);
        assertEquals(5, obj2.get("key4"));
        
        // verify obj1 remains unchanged.
        assertEquals(4, obj1.get("key4"));   
        assertTrue( obj1.containsKey("key2") );
        assertTrue( obj1.containsKey("key4") );
        assertTrue( obj1.containsKey("key1") );
        assertTrue( obj1.containsKey("key3") );
    }
    
    /**
     * 
     */
    @Test
    public void testUniqueForField() {
        
        Map<String, String> obj1 = new MapBuilder<String, String>()
                                            .append("id", "blah")
                                            .append("name", "george costanza");
        Map<String, String> obj2 = new MapBuilder<String, String>()
                                            .append("id", "blah")
                                             .append("jerry", "seinfeld");
        Map<String, String> obj3 = new MapBuilder<String, String>()
                                            .append("id", "blah2")
                                            .append("larry", "david");
        
        Collection<Map> collection = new ListBuilder<Map>().append(obj1)
                                                            .append(obj2)
                                                            .append(obj3);
                                       
        List<Map> unique = Cawls.uniqueForField( collection, "id" );
        assertEquals(2,  unique.size() );
        
        assertSame(obj1, Cawls.findFirst(unique, new MapBuilder().append("id", "blah")));
        assertSame(obj3, Cawls.findFirst(unique, new MapBuilder().append("id", "blah2")));
        assertNull(Cawls.findFirst(unique, new MapBuilder().append("jerry", "seinfeld")));
    }
    
    /**
     * 
     */
    @Test
    public void testUniqueForFieldObject() {
        
        Map<String, String> obj1 = new MapBuilder<String, String>()
                                            .append("id", "blah")
                                            .append("name", "george costanza");
        Map<String, String> obj1b = new MapBuilder<String, String>()
                                            .append("id", "blah")
                                            .append("name", "george costanza");
        
        Map<String, Object> obj2 = new MapBuilder<String, Object>()
                                            .append("id", "blah")
                                            .append("jerry", "seinfeld")
                                            .append("george", obj1);
        Map<String, Object> obj3 = new MapBuilder<String, Object>()
                                            .append("id", "blah2")
                                            .append("larry", "david")
                                            .append("george", obj1b);
        
        Collection<Map> collection = new ListBuilder<Map>().append(obj2)
                                                            .append(obj3);
                                       
        List<Map> unique = Cawls.uniqueForField( collection, "george" );
        assertEquals(1,  unique.size() );
        
        assertNotNull(Cawls.findFirst(unique, new MapBuilder().append("jerry", "seinfeld")));
    }
    
    /**
     * 
     */
    @Test
    public void testFirstNotEmpty() {
        assertEquals( "1", Cawls.firstNotEmpty("1", null) );
        assertEquals( "1", Cawls.firstNotEmpty(null, "1") );
        assertEquals( "1", Cawls.firstNotEmpty("", "1") );
        assertEquals( "1", Cawls.firstNotEmpty(null, "", "1") );
        assertEquals( "1", Cawls.firstNotEmpty(null, "", "1", null) );
        assertEquals( "1", Cawls.firstNotEmpty("", "", "1") );
        assertEquals( "1", Cawls.firstNotEmpty(null, null, "1") );
        assertEquals( "1", Cawls.firstNotEmpty(null, null, "1") );
    }

}
