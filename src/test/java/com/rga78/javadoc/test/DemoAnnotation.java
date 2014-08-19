package com.rga78.javadoc.test;

import java.lang.annotation.Documented;

/**
 * DemoAnnotation is a custom annotation type.
 * 
 * @author Rob
 */
@Documented
public @interface DemoAnnotation {

    /**
     * Javadoc comment for ze author.
     * 
     * @return returns clause - the author name.
     */
    String author();
    int currentRevision() default 1;
    String lastModified() default "N/A";
    // Note use of array
    String[] reviewers() default { "onereview", "tworeview" };
}
