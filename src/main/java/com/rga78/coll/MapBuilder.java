package com.rga78.coll;

import java.util.HashMap;
import java.util.Map;

/**
 * For easier building of JSONObjects.
 */
public class MapBuilder<K,V> extends HashMap<K,V> implements Map<K,V> {
    
    /**
     * Add the given key->value to the map.
     * 
     * @return this
     */
    public MapBuilder<K,V> append(K key, V value) {
        put(key, value);
        return this;
    }

}
