package com.surfapi.test;

import java.util.Map;

/**
 * Subclass of subclass of DemoJavadoc
 * 
 * Inherits all methods.
 */
public class DemoJavadocSubClass2 extends DemoJavadocSubClass {


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


}
