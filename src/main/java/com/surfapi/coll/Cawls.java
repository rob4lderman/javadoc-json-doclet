package com.surfapi.coll;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;


/**
 * 
 */
public class Cawls {

    /**
     * Map the collection of strings to ints.  Any strings that fail
     * to parse as ints will be mapped to 0.
     * 
     * @return the collection of ints.
     */
    public static Iterable<Integer> toInts(String[] strs) {
        List<Integer> ints = new ArrayList<Integer>();
        
        for (String str : strs) {
            try {
                ints.add( Integer.parseInt( str ) );
            } catch ( NumberFormatException nfe ) {
                ints.add( 0 );
            }
        }
        
        return ints;
    }

    /**
     * Compare each int in the two collections in order.  The number of elements compared
     * is equal to the shorter list.
     * 
     * @return (-) if ints1 < ints2
     *          0  if ints1 == ints2
     *         (+) if ints1 > ints2
     */
    public static int compareInts(Iterable<Integer> ints1, Iterable<Integer> ints2) {
        Iterator<Integer> iter1 = ints1.iterator();
        Iterator<Integer> iter2 = ints2.iterator();
        
        while (iter1.hasNext() && iter2.hasNext()) {
            int rc = iter1.next().compareTo(iter2.next());
            if (rc != 0) {
                return rc;
            }
        }
        
        // They must be equal.
        return 0;
    }

    /**
     * 
     * @return the first Map in the given collection that matches all the key-value
     *         pairs in the given filter map.
     */
    public static Map findFirst(Collection<Map> collection, Map filter) {
        for (Map model : collection) {
            if ( model.entrySet().containsAll( filter.entrySet() ) ) {
                return model;
            }
        }
        return null;
    }

}
