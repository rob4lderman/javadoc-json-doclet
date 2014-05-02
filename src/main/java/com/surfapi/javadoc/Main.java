package com.surfapi.javadoc;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.Arrays;

import org.apache.commons.io.IOUtils;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.commons.lang3.StringUtils;

import com.surfapi.log.Log;

/**
 * Main class for generating JSON javadoc.
 * 
 * Usage: Main <output-file> <src-dir> ... 
 */
public class Main {
    
    /**
     * @param src-dir The dir containing the src code to parse for javadoc.
     */
    public static void main(String[] args) throws Exception {
        new Main()
            .validateArgs(args)
            .setOutputStream( new FileOutputStream(args[0]) )
            .setDocletPath( Main.getThisJarFile().getAbsolutePath() )
            .go( Arrays.copyOfRange( args, 1, args.length ) );
    }

    /**
     * @return The jar file that this class is executing within
     */
    public static File getThisJarFile() {
        return new File(Main.class.getProtectionDomain()
                                        .getCodeSource()
                                        .getLocation()
                                        .getPath());
    }

    /**
     * The stream to which to write the JSON output
     */
    private OutputStream outputStream;

    /**
     * The doclet path used by the JavadocProcess.  It's a variable field
     * because the path is different between the real/test environments.
     */
    private String docletPath;
    
    /**
     * A filter for filtering OUT the subdirs that we DON'T want to process.
     * By default it filters out all subdirs under any directory named "test"
     */
    private IOFileFilter subdirFilter = new FilterOutTest();
    
    /**
     * @throws RuntimeException prints the given err, usage, and then throws
     */
    private void usage(String err) {
        if (!StringUtils.isEmpty(err)) {
            Log.log(" !! ERROR: " + err);
        }
        Log.log("");
        Log.log("Usage: Main <output-file> <src-dir> ...");
        Log.log("");
        
        throw new RuntimeException("ERROR: usage. " + err);
    }


    /**
     * @throws RuntimeException if the args are not valid.
     */
    protected Main validateArgs(String[] args) {
        if (args.length < 2) {
            usage(null);
        } else if ( new File(args[0]).isDirectory() ) {
            usage("Must specify output file as first arg");
        }
        return this;
    }

    /**
     *
     * @param dirs A list of dirs to process
     */
    protected void go(String[] dirNames) throws Exception {

        // The doclet writes a bunch of JSONObjects to the stream. 
        // Need to wrap those objects in array notation [] in order to simplify parsing.
        IOUtils.write("[\n",outputStream,"ISO-8859-1");

        // Put ',' between the process outputs (we're writing a big huge JSON array to
        // the file). First iteration prefixes the JavadocProcess output with "".
        String delim = "";
        
        // Process each directory.
        for (String srcDirName : dirNames) {

            File srcDir = new File(srcDirName);
            if (! srcDir.exists() || !srcDir.isDirectory()) {
                Log.log(this, "go: src directory " + srcDir.getAbsolutePath() + " does not exist");

            } else {
        
                IOUtils.write(delim,outputStream,"ISO-8859-1");

                // Send output directly to output stream, otherwise you risk running OutOfMemory.
                new JavadocProcess( srcDir )
                        .setDocletPath( getDocletPath() )
                        .setDirFilter( subdirFilter )
                        .run(outputStream);

                // 2nd..n iterations prefix the output with ','
                delim = ",";
            }
        }
        
        // The doclet writes a bunch of JSONObjects to the stream. 
        // Need to wrap those objects in array notation [] in order to simplify parsing.
        IOUtils.write("\n]",outputStream,"ISO-8859-1");
    }

    /**
     *
     */
    protected Main setOutputStream(OutputStream outputStream) {
        this.outputStream = outputStream;
        return this;
    }

    /**
     *
     */
    public Main setDocletPath(String docletPath) {
        this.docletPath = docletPath;
        return this;
    }
    
    /**
     * 
     */
    public String getDocletPath() {
        return docletPath;
    }

    /**
     * @param subdirFilter 
     * @return this
     */
    protected Main setDirFilter(IOFileFilter subdirFilter) {
        this.subdirFilter = subdirFilter;
        return this;
    }

}
