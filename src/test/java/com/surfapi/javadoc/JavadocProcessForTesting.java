package com.surfapi.javadoc;

import java.io.File;

import org.apache.commons.io.filefilter.TrueFileFilter;

public class JavadocProcessForTesting extends JavadocProcess {

    /**
     * 
     * @param baseDir - The base package dir. This points to the directory of your base package.
     *                  E.g if your package is "com.abc.foo", the baseDir is "/path/to/com".
     */
    public JavadocProcessForTesting(File baseDir) {
        super(baseDir);
        setDocletPath( buildTestDocletPath() );
        setDirFilter( TrueFileFilter.INSTANCE );
    }
    
    
    /**
     * Build a doclet path for use during unit testing.
     * 
     * @return the classpath (-docletpath) for the custom doclet.
     */
    protected static String buildTestDocletPath() {
        String mavenProjectDir = System.getProperty("user.dir");
        return mavenProjectDir + "/target/classes"
                + File.pathSeparator
                + mavenProjectDir + "/docletpath/json-simple-1.1.1.jar";
    }
    
}
