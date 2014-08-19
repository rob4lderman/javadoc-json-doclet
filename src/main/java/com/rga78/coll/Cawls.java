package com.rga78.coll;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;


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
        for (Map model : safeIterable(collection)) {
            if ( containsAll(model, filter ) ) {
                return model;
            }
        }
        return null;
    }
    
    /**
     * @return a subset of elements from the collection that match the given filter.
     */
    public static List<Map> findAll(Collection<Map> collection, Map filter) {
        List<Map> retMe = new ArrayList<Map>();
        for (Map model : safeIterable(collection)) {
            if ( containsAll(model, filter ) ) {
                retMe.add(model);
            }
        }
        
        return retMe;
    }

    /**
     * Cuz entrySet().containsAll() doesn't work for some implementations of Map
     * (ahem, mongodb BasicDBObject...)
     *
     * TODO: test
     *
     * @return true if the given map contains all the entries from the given "subset" map.
     */
    public static boolean containsAll(Map map, Map subset) {
        for (Map.Entry subsetEntry : (Set<Map.Entry>)subset.entrySet() ) {
            Object subsetVal = subsetEntry.getValue();
            Object mapVal = map.get(subsetEntry.getKey());

            if ( ! ObjectUtils.equals( subsetVal, mapVal ) ) {
                // Check if they're both Maps -- if so do an extra check of each entry.
                if ((mapVal instanceof Map) 
                        && (subsetVal instanceof Map) 
                        && Cawls.mapEquals( (Map) mapVal, (Map) subsetVal ) )  {
                    // they're equal.
                } else {
                    return false;
                }
            }
        }

        return true;
    }

    /**
     * Can't rely on the typical Map.equals() implementation (map.entrySet().equals( map2.entrySet() )
     * because some implementations of Map don't work properly (ahem.. mongodb BasicDBObject).
     *
     * TODO: test
     *
     * @return true if the two maps are equals.
     */
    public static boolean mapEquals(Map map1, Map map2) {
        for (Map.Entry map1Entry : (Set<Map.Entry>)map1.entrySet() ) {
            Object map1Val = map1Entry.getValue();
            Object map2Val = map2.get(map1Entry.getKey());

            if ( ! ObjectUtils.equals( map1Val, map2Val ) ) {
                // Check if they're both Maps -- if so do an extra check of each entry.
                if ((map1Val instanceof Map) 
                        && (map2Val instanceof Map) 
                        && Cawls.mapEquals( (Map) map1Val, (Map) map2Val) )  {
                    // they're equal.
                } else {
                    return false;
                }
            }
        }

        return true;
    }

    /**
     * 
     */
    public static boolean isSubset_xx(Map map, Map subset) {
        return map.entrySet().containsAll( subset.entrySet() );
    }
    
    /**
     * @return a new Map containing the given subset of keys from the given Map.
     */
    public static Map pick(Map obj, Collection<String> keys) {
        Map retMe = new HashMap(obj);
        retMe.keySet().retainAll(keys);
        return retMe;
    }

    /**
     * @return Pluck a single field from each of the Maps in the givne Collection and return
     *         the values in a List.
     */
    public static List<?> pluck(Collection<Map> objs, String key) {
        List<Object> retMe = new ArrayList<Object>();
        for (Map obj : safeIterable(objs)) {
            retMe.add( obj.get(key) );
        }
        return retMe;
    }

    
    /**
     * @return the given iterable, if not null; otherwise an empty list.
     */
    public static <T> Iterable<T> safeIterable(Iterable<T> iterable) {
        return (iterable != null) ? iterable : Collections.EMPTY_LIST;
    }
    
    /**
     * @return the given iterable, if not null; otherwise an empty list.
     */
    public static <T> Iterable<T> safeIterable(T[] iterable) {
        return (iterable != null) ? Arrays.asList(iterable) : Collections.EMPTY_LIST;
    }
    
    /**
     * 
     * @return A sublist of objs where each object has a unique value for the given key.
     *         Dups are omitted.  In the event there are dups, the first obj found
     *         (via the collection's iterator) is the one included in the result.
     */
    public static List<Map> uniqueForField(Collection<Map> objs, String key) {
        
        List<Map> retMe = new ArrayList<Map>();
        Set uniques = new HashSet();
        
        for ( Map obj : objs ) {
            if (uniques.add( obj.get(key) )) {
                retMe.add(obj);
            }
        }

        return retMe;
    }

    /**
     * @return the last item in the list, or null if the list is null or empty.
     */
    public static <T> T getLast(List<T> list) {
        return (list == null || list.size() == 0) ? null : list.get( list.size() - 1 );
    }

    /**
     * @return true if the given array is null or empty.
     */
    public static <T> boolean isEmpty(T[] arr) {
        return (arr == null || arr.length == 0);
    }

    /**
     * @return the first non-empty string, or null if they're all empty
     */
    public static String firstNotEmpty(String... strs) {
        for (String str : strs) {
            if (!StringUtils.isEmpty(str)) {
                return str;
            }
        }
        return null;
    }
}
