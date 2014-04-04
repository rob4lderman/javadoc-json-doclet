package com.surfapi.javadoc;

import java.util.ArrayList;
import java.util.List;

/**
 * Utility class for chunking up a really long List into a set of sublists.
 * 
 * This is useful for when you've got a ton of *.java files to run thru javadoc
 * but you can't specify all of them on the javadoc command at once because
 * you'll overrun the command length limit.
 */
public class ListChunker<T> extends ArrayList<List<T>> {
    
    public ListChunker(List<T> list, int chunkSize) {
        for (int i=0; i < list.size(); i += chunkSize) {
            int endIndex = (i+chunkSize < list.size()) ? (i + chunkSize) : list.size();
            super.add( list.subList(i, endIndex) );
        }
    }
}