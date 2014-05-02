package com.surfapi.coll;

import java.util.ArrayList;

public class ListBuilder<T> extends ArrayList<T> {
    
    public ListBuilder<T> append(T obj) {
        add(obj);
        return this;
    }
    
   
}
