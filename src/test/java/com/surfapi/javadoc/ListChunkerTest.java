package com.surfapi.javadoc;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.List;

import org.junit.Test;

public class ListChunkerTest {

    @Test
    public void test() throws Exception {
        
        List<String> theList = new JavadocProcess(null).listJavaFileNames(new File("src") );
        assertTrue( theList.size() > 3 );
        
        int theList_i = 0;
        
        for (List<String> subList : new ListChunker<String>(theList, 3)) {
            for (String str : subList) {
                assertEquals( theList.get(theList_i++), str );
            }
        }
    }
}
