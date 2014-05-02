package com.surfapi.test;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.List;

public class DemoJavadocSubClass extends DemoJavadoc<List> {

    /**
     * Overridden method.
     */
    @Override
    public void someAbstractMethod(String[] strs) {
        // TODO Auto-generated method stub
        
    }
    
    @Override
    public InputStream parse(URL url, List<List> y) throws IOException, IllegalArgumentException {
        return null;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public String call() throws Exception {
        // TODO Auto-generated method stub
        return null;
    }

}
