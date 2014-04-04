
package com.surfapi.json;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.json.simple.JSONValue;

/**
 * Pretty-print JSON.
 * 
 * Usage: new JSONTrace(Map or List).toString()
 * or: JSONTrace.prettyPrint(Map or List).
 * 
 * TODO: comparators for ordering keys
 */
public class JSONTrace {
    
    /**
     * @return Pretty print the given JSON object 
     */
    public static String prettyPrint(Map map) {
        return new JSONTrace(map).toString();
    }
    
    /**
     * @return Pretty print the given JSON array.
     */
    public static String prettyPrint(List list) {
        return new JSONTrace(list).toString();
    }

    private String tab = " ";

    private String newline = "\n";

    private Map map;

    private List list;

//    /**
//     * Hacky comparator to show the relavant Node attributes first.
//     */
//    private static final Comparator nodeAttrCompator = new Comparator() {
//        public int compare(Object o1, Object o2) {
//            String s1 = massageIt(o1);
//            String s2 = massageIt(o2);
//            return s1.compareTo(s2);
//        }
//
//        private String massageIt(Object o) {
//            String s = (String)o;
//            String orderingPrefix = "";
//            if (s.equals("uuid")) {
//                orderingPrefix = "    ";
//            } else if (s.equals("type")) {
//                orderingPrefix = "   ";
//            } else if (s.equals("name")) {
//                orderingPrefix = "  ";
//            } else if (s.equals("description")) {
//                orderingPrefix = " ";
//            } else {
//                return s;
//            }
//            return orderingPrefix + s;
//        }
//    };


    /**
     * The given map of JSON object data will be pretty-printed when this.toString() is called.
     */
    public JSONTrace(Map map) {
        this.map = map;
    }


    /**
     * The given list of JSON array data will be pretty-printed when this.toString() is called.
     */
    public JSONTrace(List list) {
        this.list = list;
    }


    /**
     * Set the delimeters to use when pretty printing.
     */
    public JSONTrace setDelims(String tab, String newline) {
        this.tab = tab;
        this.newline = newline;
        return this;
    }


    /**
     * Pretty print the JSON provided to the CTOR.
     */
    public String toString() {
        if (map != null) {
            return prettyPrintMap(map);
        } else {
            return prettyPrintList(list);
        }
    }


    /**
     * Pretty print the given JSON object, with nice indenting and such.
     *
     * @return String of pretty printed JSON data.
     */
    private String prettyPrintMap(Map jsonMap) {
        return prettyPrintMap(jsonMap, 0);
    }


    /**
     * Pretty print the given JSON object, with nice indenting and such.
     *
     * @return String of pretty printed JSON data.
     */
    private String prettyPrintMap(Map jsonMap, int indentLevel) {

        if (jsonMap == null) {
            return "null";
        } else if (jsonMap.isEmpty()) {
            return "{}";
        }

        StringBuffer sb = new StringBuffer();
        sb.append("{" + newline);

        String objTab = "";
        String attrTab = tab;
        for (int i=0; i < indentLevel; ++i) {
            objTab += tab;
            attrTab += tab;
        }

        Set keys = getKeySet(jsonMap);

        String delim = "";
        for (Object key : keys) {

            sb.append(delim);
            sb.append(attrTab + "\"" + String.valueOf(key) + "\": ");  // Print key.

            Object value = jsonMap.get(key);

            sb.append(prettyPrintValue(value,indentLevel));
            delim = "," + newline;
        }

        sb.append(newline + objTab + "}");

        return sb.toString();
    }


    /**
     * Specially-sort the Map keys if it's a Node type.
     */
    private Set getKeySet(Map jsonMap) {
        return jsonMap.keySet();
    }


    /**
     * Pretty print the given JSON array, with nice indenting and such.
     *
     * @return String of pretty printed JSON data.
     */
    private String prettyPrintList(List jsonList) {
        return prettyPrintList(jsonList, 0);
    }


    /**
     * Pretty print the given JSON list, with nice indenting and such.
     *
     * @return String of pretty printed JSON data.
     */
    private String prettyPrintList(List jsonList, int indentLevel) {

        if (jsonList == null) {
            return "null";
        } else if (jsonList.isEmpty()) {
            return "[]";
        }

        StringBuffer sb = new StringBuffer();
        sb.append("[" + newline);

        String objTab = "";
        String attrTab = tab;
        for (int i=0; i < indentLevel; ++i) {
            objTab += tab;
            attrTab += tab;
        }

        String delim = "" ;
        for (Object value : jsonList) {

            sb.append(delim);
            sb.append(attrTab);

            sb.append(prettyPrintValue(value,indentLevel));

            delim = "," + newline;
        }

        sb.append(newline + objTab + "]");

        return sb.toString();
    }


    /**
     * Helper for prettyPrint methods.
     *
     * @return String of pretty printed JSON value.
     */
    private String prettyPrintValue(Object value, int indentLevel) {
        if (value instanceof Map) {
            return prettyPrintMap((Map)value,indentLevel+1);
        } else if (value instanceof List) {
            return prettyPrintList((List)value,indentLevel+1);
        } else if (value instanceof String) {
            return "\"" + escape((String)value) + "\"";
        } else {
            return String.valueOf(value);
        }
    }
    
    /**
     * TODO: use StringEscapeUtils (commons.lang3)?
     * 
     * @return the given string with quotes and control chars escaped.
     */
    private String escape(String s) {
        // return s.replace("\\", "\\\\").replace("\"", "\\\"");
        return JSONValue.escape(s);
    }
    
    

}





