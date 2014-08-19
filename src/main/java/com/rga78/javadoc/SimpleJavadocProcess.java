package com.rga78.javadoc;


import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;

import com.rga78.log.Log;
import com.rga78.proc.ProcessException;
import com.rga78.proc.ProcessHelper;

/**
 * Runs javadoc using our custom doclet.  
 * Uses JsonDoclet by default.  
 * Use setDocletClass(Class docletClazz) to specify a different doclet.
 *
 * javadoc \
 *      -doclet com.rga78.javadoc.JsonDoclet \
 *      -docletpath "target/classes;$dp" \
 *      -J-Xms1024m \
 *      -J-Xmx4096m \
 *      -sourcepath /fox/tmp/javadoc/src-jdk7   \
 *      [ -subpackages javax  | <package-list> ]
 *
 */
public class SimpleJavadocProcess {

    /**
     * The base package dir.  This dir plus all subdirs are supplied as packages to
     * the javadoc command.
     */
    private File sourcePath;
    
    /**
     * The mongo URI
     */
    private String mongoUri;

    /**
     * The libraryId (mongodb collection name).
     */
    private String libraryId;
    
    /**
     * List of -subpackages args (all subpackages beneath the arg are processed).
     */
    private List<String> subpackages = new ArrayList<String>();

    /**
     * List of packages to process.
     */
    private List<String> packages = new ArrayList<String>();
    
    /**
     * The doclet class
     */
    private Class<?> docletClazz = JsonDoclet.class;
    
    /**
     * The -quiet flag
     */
    private boolean quiet = false;

    /**
     * @return the classpath (-docletpath) for the custom doclet.
     */
    protected String getDocletPath() throws IOException {
        return buildMavenDocletPath(); 
    }
    
    /**
     * Build a doclet path that contains the classes from this project along
     * with all dependency jars.  Dependency jars are copied into target/dependency
     * under mvn package.
     * 
     * @return the classpath (-docletpath) for the custom doclet.
     */
    public static String buildMavenDocletPath() throws IOException {
        
        File dependencyDir = new File("./target/dependency");
        Collection<File> jarFiles = FileUtils.listFiles(dependencyDir, new String[] { "jar"}, false);
        List<String> jarFileNames = mapToFileNames(jarFiles);
        
        String jarFileClassPath = StringUtils.join(jarFileNames, File.pathSeparator);
        
        return "./target/classes" 
                + File.pathSeparator
                + jarFileClassPath;
    }
    
    /**
     * 
     * @return The names of the given collection of Files.
     */
    public static List<String> mapToFileNames(Collection<File> files) throws IOException {
        List<String> fileNames  = new ArrayList<String>();
        for (File file : files) {
            fileNames.add(file.getCanonicalPath());
        }
        return fileNames;
    }

    /**
     * @return this
     */
    public SimpleJavadocProcess setSourcePath(File sourcePath) {
        this.sourcePath = sourcePath;
        return this;
    }
    
    /**
     * @return the sourcepath
     */
    public File getSourcePath() {
        return sourcePath;
    }

    /**
     * @return this
     */
    public SimpleJavadocProcess setSubpackages(List<String> subpackages) {
        this.subpackages.addAll( subpackages );
        return this;
    }

    /**
     * @return the list of -subpackages
     */
    public List<String> getSubpackages() {
        return subpackages;
    }

    /**
     * @return this
     */
    public SimpleJavadocProcess setPackages(List<String> packages) {
        this.packages.addAll( packages );
        return this;
    }

    /**
     * @return the list of packages
     */
    public List<String> getPackages() {
        return packages;
    }

    /**
     * @return this
     */
    public SimpleJavadocProcess setDocletClass(Class<?> docletClazz) {
        this.docletClazz = docletClazz;
        return this;
    }
    
    /**
     * @return the doclet class
     */
    public Class<?> getDocletClass() {
        return docletClazz;
    }
    
    /**
     * @return this
     */
    public SimpleJavadocProcess setQuiet(boolean quiet) {
        this.quiet = quiet;
        return this;
    }
    
    /**
     * @return "-quiet" if set, otherwise ""
     */
    public String getQuietOption() {
        return (quiet) ? "-quiet" : "";
    }
    
    /**
     * Build Runnable work for spawning and waiting for the javadoc process.
     * 
     * @return A Runnable that will spawn and wait for the javadoc process.
     */
    protected Callable<ProcessHelper> buildJavadocProcessRunnable( ) {
        
        return new Callable<ProcessHelper>() {
            public ProcessHelper call() {
                
                ProcessHelper processHelper = null;
                try {
                    
                    String processDescription = "javadoc against sourcepath: " +  getSourcePath().getCanonicalPath();
                    Log.info(this, "run: " + processDescription);
                    
                    processHelper = buildProcessHelper().setDescription(processDescription)
                                                        .spawnStreamReaders()
                                                        .waitFor();

                    if (processHelper.exitValue() != 0) {
                        Log.error(this, "run: " + new ProcessException( processHelper ));
                    }
                    
                } catch (Exception e) {
                    Log.error(this, "run: " + e);
                }
                
                return processHelper;
            }
        };
    }

    /**
     * Run the javadoc command.
     */
    public ProcessHelper run() throws IOException, InterruptedException, ExecutionException, Exception {
        return buildJavadocProcessRunnable().call();
    }
    
    /**
     * @return a ProcessHelper wrapped around the javadoc process.
     */
    public ProcessHelper buildProcessHelper() throws IOException {
        return new ProcessHelper( buildJavadocProcess() );
    }

    /**
     *
     * @param javaFileNames The list of java files to process
     * 
     * @return the javadoc Process
     */
    protected Process buildJavadocProcess() throws IOException {
        return new ProcessBuilder( buildCommand() ).start();
    }
    
    /**
     * @return the javadoc command
     */
    protected List<String> buildCommand() throws IOException {

        List<String> command = new ArrayList<String>();

        command.addAll( Arrays.asList( new String[] { "javadoc", 
                                                      "-docletpath",
                                                      getDocletPath(),
                                                      "-doclet",
                                                      getDocletClass().getCanonicalName(),
                                                      getQuietOption(),
                                                      "-J-Xms1024m",
                                                      "-J-Xmx4096m",
                                                      "-sourcepath",
                                                      getSourcePath().getCanonicalPath()
                                                    } ) );

        command.addAll( buildSubpackagesCommandArgs() );
        command.addAll( getPackages() );
        
        return command;
    }
    

    /**
     * @return -subpackages <subpkg1> -subpackages <subpkg2> ...
     */
    protected List<String> buildSubpackagesCommandArgs() {
        List<String> retMe = new ArrayList<String>();

        for (String subpackage : getSubpackages() ) {
            retMe.add( "-subpackages" );
            retMe.add( subpackage );
        }

        return retMe;
    }

}


