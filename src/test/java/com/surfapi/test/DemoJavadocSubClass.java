package com.surfapi.test;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.net.URL;
import java.util.List;

/**
 * Try inheriting doc here: {@inheritDoc}
 */
public class DemoJavadocSubClass extends DemoJavadoc<List> implements Serializable {

    /**
     * Overridden method, inherit doc: {@inheritDoc}
     * 
     * @param strs inherit doc: {@inheritDoc}
     * 
     * @return inherit doc: {@inheritDoc}
     */
    @Override
    public int someAbstractMethod(String[] strs) {
        return 0;
    }
    
    @Override
    public InputStream parse(URL url, List<List> y) throws IOException, IllegalArgumentException {
        return null;
    }
    
    /**
     * This method has an inheritDoc tag, right here: {@inheritDoc}
     */
    @Override
    public String call() throws Exception {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void interfaceMethod(String parm1) {
        // TODO Auto-generated method stub
        
    }

}
