package com.surfapi.javadoc;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import com.surfapi.proc.ProcessException;
import com.surfapi.proc.ProcessHelper;

/**
 * Java source will most likely be submitted to the site packaged up in a jar file.
 * 
 * We need to extract the source from the jar.  That's what this class does.
 * 
 * Usage: new ExtractSrcJar(jarFile).extract();
 */
public class ExtractSrcJar {

    /**
     * The jar file to extract.
     */
    private File jarFile;
    
    /**
     * CTOR.
     *  
     * @param jarFile The jarfile to extract.
     */
    public ExtractSrcJar(File jarFile) {
        this.jarFile = jarFile;
    }

    /**
     * Extract the contents of the jar file.
     * 
     * @return the extraction directory.
     */
    public File extract() throws IOException, InterruptedException {
        
        File extractDir = makeExtractDir();

        ProcessHelper process = new ProcessHelper( buildJarProcess( extractDir ) ).waitFor();
        
        if (process.exitValue() != 0) {
            throw new ProcessException( process );
        }
        
        return extractDir;
    }
    
    /**
     * @return the jar process.
     */
    protected Process buildJarProcess( File extractDir ) throws IOException {
        return new ProcessBuilder( buildCommand() ).directory(extractDir).start();
    }
    
    /**
     * Note: could use unzip instead of jar.
     * 
     * @return the jar -xf <jarFile>
     */
    protected List<String> buildCommand() throws IOException {
        if (jarFile.getName().endsWith(".tar")) {
            return Arrays.asList( "tar", "-xf", jarFile.getCanonicalPath().replaceFirst("C:","") );
        } else {
            return Arrays.asList( "jar", "-xf", jarFile.getCanonicalPath() );
        }
    }
    
    /**
     * @return a newly created directory to use for the extract.
     */
    protected File makeExtractDir() throws IOException {
        File extractDir;
        
        do {
            // Keep trying until we find a dir name that doesn't already exist.
            // (This should pretty much always work the first time since we're using UUIDs).
            extractDir = new File( System.getProperty("java.io.tmpdir") + File.separator + UUID.randomUUID().toString() );
        } while (extractDir.exists() );
        
        if ( ! extractDir.mkdirs() ) {
            throw new IOException("Failed to make extractDir: " + extractDir.getAbsolutePath() );
        }

        return extractDir;
    }
}
