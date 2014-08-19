package com.rga78.javadoc.test;

import java.io.Serializable;
import java.util.Map;

/**
 * Subclass of subclass of DemoJavadoc
 * 
 * Inherits all methods.
 */
public class DemoJavadocSubClass2 extends DemoJavadocSubClass implements Serializable {


    /**
     * A method with a complicated var-arg.
     */
    public String playingWithVarArgs(int blah, Map<DemoJavadoc, Map<String, DemoJavadoc>>... maps) {
        return "";
    }


    /**
     * A method with a simple var-arg.
     */
    public String simpleVarArg(int... ints) {
        return "";
    }

    /**
     * A method with a simple var-arg.
     */
    public String simpleVarArg(DemoJavadoc... docs) {
        return "";
    }
    
    /**
     * Testing what happens when a type isn't available to the javadoc tool.
     */
    // public String unknownType(com.surfapi.type.dont.exist.Huh huh);


}
