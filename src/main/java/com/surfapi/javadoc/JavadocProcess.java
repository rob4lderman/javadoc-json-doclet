package com.surfapi.javadoc;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.regex.Pattern;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import com.surfapi.proc.ProcessException;
import com.surfapi.proc.ProcessHelper;

/**
 * Runs javadoc against all *.java files in the given baseDir, using our custom MyDoclet.
 *
 */
public class JavadocProcess {

    /**
     * The base package dir.  This dir plus all subdirs are supplied as packages to
     * the javadoc command.
     */
    private File baseDir;
    
    /**
     * The -docletpath parm for the javadoc process.
     */
    private String docletPath;
    
    /**
     * 
     * @param baseDir - The base package dir. This points to the directory of your base package.
     *                  E.g if your package is "com.abc.foo", the baseDir is "/path/to/com".
     */
	public JavadocProcess(File baseDir) {
		this.baseDir = baseDir;
	}

    /**
     * Inject the docletPath to use for this process.
     * 
     * @return this
     */
    public JavadocProcess setDocletPath(String docletPath) {
        this.docletPath = docletPath;
        return this;
    }
    
    /**
     * @return the classpath (-docletpath) for the custom doclet.
     */
    protected String getDocletPath() {
        return docletPath;
    }
    
	/**
	 * 
	 * @return a Pair containing stdout and stderr from the javadoc process.
	 */
	public Pair<List<String>,List<String>> run() throws IOException, InterruptedException, ExecutionException {

	    List<String> stdout = new ArrayList<String>();
	    List<String> stderr = new ArrayList<String>();
	    
        // The doclet writes a bunch of JSONObjects to the stream. 
        // Need to wrap those objects in array notation [] in order to simplify parsing.
	    stdout.add("["); 
	    
	    for ( List<String> javaFileNames : new ListChunker<String>( listJavaFileNames(baseDir), 250 ) ) {
	    
	        ProcessHelper process = new ProcessHelper( buildJavadocProcess(javaFileNames) );
	        process.waitFor();
		
	        if (process.exitValue() != 0) {
	            throw new ProcessException( process );
	        }
	        
	        stdout.addAll( process.getStdout() );
	        stderr.addAll( process.getStderr() );
	    }
	    
	    stdout.add("]");
		
        return new ImmutablePair<List<String>, List<String>>(stdout, stderr);
	}
	
	
	/**
     * 
     * @param outputStream Output is written to this stream.
     * 
     * @return a Pair containing stdout and stderr from the javadoc process.
     */
    public void run(OutputStream outputStream) throws IOException, InterruptedException, ExecutionException {

        // The doclet writes a bunch of JSONObjects to the stream. 
        // Need to wrap those objects in array notation [] in order to simplify parsing.
        IOUtils.write("[\n",outputStream,"ISO-8859-1");
        
        for ( List<String> javaFileNames : new ListChunker<String>( listJavaFileNames(baseDir), 300 ) ) {
        
            ProcessHelper process = new ProcessHelper( buildJavadocProcess(javaFileNames) );
            process.waitFor();
        
            if (process.exitValue() != 0) {
                throw new ProcessException( process );
            }
            
            IOUtils.writeLines( removeWarnings( process.getStdout() ), "\n", outputStream, "ISO-8859-1");
            // IOUtils.writeLines( process.getStderr(), "\n", outputStream, "ISO-8859-1");
        }
        
        IOUtils.write("\n]",outputStream,"ISO-8859-1");
    }
    
    /**
     * 
     */
	protected List<String> removeWarnings(List<String> list) {
	    
	    Pattern patter = Pattern.compile("^\\d+\\s+warnings$");
	    
	    List<String> retMe = new ArrayList<String>();
	    for (String s : list) {
	        if (!patter.matcher(s).matches()) {
	            retMe.add(s);
	        }
	    }
       
        return retMe;
    }

    /**
     * Why not use com.sun.tools.javadoc.Main?  
     * 
     * Per http://docs.oracle.com/javase/7/docs/technotes/guides/javadoc/standard-doclet.html:
     * 
     * "The disadvantages of calling main are: (1) It can only be called once per run -- for 1.2.x or 1.3.x, 
     * use java.lang.Runtime.exec("javadoc ...") if more than one call is needed, (2) it exits using System.exit(), 
     * which exits the entire program, and (3) an exit code is not returned."
     * 
     * TODO: maybe it's still useful for getting the classpath right?  I could package a separate jar for
     *       it, with all its dependencies baked in, and invoke using java -jar instead of javadoc.
     *
     * @param javaFileNames The list of java files to process
     * 
     * @return the javadoc Process
     */
    protected Process buildJavadocProcess(List<String> javaFileNames) throws IOException {
        return new ProcessBuilder( buildCommand2(javaFileNames) ).start();
    }
    
    /**
     * 
     * @param javaFileNames The list of java files to process
     * 
     * @return the javadoc command, with java file names as args  
     *         Assume baseDir points at a directory that contains java files somewhere underneath it.
     */
    protected List<String> buildCommand2( List<String> javaFileNames ) throws IOException {

        List<String> command = new ArrayList<String>();

        command.addAll( Arrays.asList( new String[] { "javadoc", 
                                              "-docletpath",
                                              getDocletPath(),
                                              "-doclet",
                                              MyDoclet.class.getCanonicalName(),
                                              "-quiet" } ) );

        command.addAll( javaFileNames );
        
        return command;
    }


    /**
     * Find all *.java files in the given baseDir
     *
     * @return The Collection of *.java file names.
     */
    protected List<String> listJavaFileNames(File baseDir) throws IOException {
        List<String> fileNames = new ArrayList<String>();
        for (File file : FileUtils.listFiles(baseDir, new String[] { "java"}, true)) {
            fileNames.add( file.getCanonicalPath() );
        }
        return fileNames;
    }

}