package com.surfapi.json;

import org.json.simple.JSONObject;

/**
 * For easier building of JSONObjects.
 */
public class JSONObjectBuilder extends JSONObject {
    
    /**
     * Add the given key->value to the map.
     * 
     * @return this
     */
    public JSONObjectBuilder append(String key, Object value) {
        put(key, value);
        return this;
    }

}
