package com.surfapi.javadoc;

import java.io.File;
import java.io.FileOutputStream;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;

import com.surfapi.log.Log;

/**
 * Main class for converting javadoc into JSON.
 * 
 * Usage: Main <src-jar-file>.
 */
public class Main {
    
    /**
     * 
     */
    private static void usage(String err) {
        if (!StringUtils.isEmpty(err)) {
            Log.log(" !! ERROR: " + err);
        }
        Log.log("");
        Log.log("Usage: java -jar " + getThisJarFile().getName() + " <src-jar-file> <output-file-name>");
        Log.log("");
        
        System.exit(-1);
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
     * @param src-jar-file The jar file containing the src code to parse for javadoc.
     */
    public static void main(String[] args) throws Exception {
        
        if (args.length < 2) {
            usage(null);
        }
        
        File jarFile = new File(args[0]);
        if (! jarFile.exists()) {
            usage("src jar file " + jarFile.getAbsolutePath() + " does not exist");
        }
        
        FileOutputStream outputStream = new FileOutputStream( new File(args[1]) );
        
        // returns the directory in which the jar was extracted.
        // JavadocProcess will process all *.java files under that dir.
        File extractDir = new ExtractSrcJar(jarFile).extract();
        
        // Run javadoc against the extracted src.
        // Pair<List<String>,List<String>> out = new JavadocProcess( extractDir ).run();
        // // Write the output to stdout.
        // IOUtils.writeLines( out.getLeft(), "\n", System.out );
        
        // Send output directly to output stream, otherwise you risk running OutOfMemory.
        new JavadocProcess( extractDir )
                .setDocletPath( Main.getThisJarFile().getAbsolutePath())
                .run(outputStream);
        
        // Delete the directory recursively
        FileUtils.deleteDirectory( extractDir );
    }

}
