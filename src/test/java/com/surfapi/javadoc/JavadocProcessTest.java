package com.surfapi.javadoc;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.io.filefilter.DirectoryFileFilter;
import org.json.simple.JSONArray;
import org.json.simple.parser.JSONParser;
import org.junit.Test;

/**
 * 
 */
public class JavadocProcessTest {


    /**
     * 
     */
    @Test
    public void testBuildCommand2() throws Exception {
        
        String pwd = System.getProperty("user.dir");
        
        File baseDir = new File("src/test/java/com/surfapi/test");
        JavadocProcess javadocProcess = new JavadocProcess(baseDir);
        
        List<String> expectedCommand = new ArrayList<String>( Arrays.asList( new String[] { "javadoc", 
                                                                     "-docletpath",
                                                                     javadocProcess.getDocletPath(),
                                                                     "-doclet",
                                                                     MyDoclet.class.getCanonicalName(),
                                                                     "-quiet" } ) );
        
        List<String> javaFileNames = javadocProcess.listJavaFileNames(baseDir);
        expectedCommand.addAll(javaFileNames);
        
        assertEquals(expectedCommand, javadocProcess.buildCommand2(javaFileNames));
    }
	
    /**
     * 
     */
    @Test
    public void testRemoveWarnings() throws Exception {
        
        List<String> list = Arrays.asList("hello",
                                          "23 warnings",
                                          "no warnings",
                                          "warnings",
                                          "0 warnings",
                                          "good bye");
        
        List<String> list2 = new JavadocProcess(null).removeWarnings(list);
        
        assertFalse(list2.contains("23 warnings"));
        assertFalse(list2.contains("0 warnings"));
        assertTrue(list2.contains("hello"));
        assertTrue(list2.contains("warnings"));
        assertTrue(list2.contains("no warnings"));
    }
    
    /**
     * 
     */
    // @Test
    public void testLotsOfFiles() throws Exception {
        
        // new JavadocProcessForTesting(new File("/fox/tmp/javadoc/jdk6.src")).run( new FileOutputStream("testLotsOfFiles.out") );
        
        processDir( new File("/fox/tmp/javadoc/jdk6.src/jdk/src/share/classes/java") );
        
    }
    
    /**
     * 
     */
    // @Test
    public void testJdk16() throws Exception {
        
        new JavadocProcessForTesting(new File("/fox/tmp/javadoc/jdk6.src")).run( new FileOutputStream("java-sdk_1.6.json") );
        // make sure it parses
        JSONArray objs = (JSONArray) new JSONParser().parse(new FileReader("java-sdk_1.6.json") );
        assertFalse(objs.isEmpty());
    }
    
    // @Test
    public void testLotsOfFiles2() throws Exception {
        File baseDir = new File("/fox/tmp/javadoc/jdk6.src/jdk/src/share/classes/java");
        
        // for (File dir : FileUtils.listFilesAndDirs(baseDir, DirectoryFileFilter.INSTANCE, FalseFileFilter.INSTANCE) ) {
        for (String dirName : baseDir.list( DirectoryFileFilter.INSTANCE) ) {
            File dir = new File(baseDir.getAbsolutePath() + File.separator + dirName);
            System.out.println("Testing " + dir.getAbsolutePath());
            processDir(dir);
           
        }
    }
    
    private void processDir(File baseDir) throws Exception {
        new JavadocProcessForTesting(baseDir).run( new FileOutputStream("testLotsOfFiles.out") );
        
        // make sure it parses
        JSONArray objs = (JSONArray) new JSONParser().parse(new FileReader("testLotsOfFiles.out") );
        assertFalse(objs.isEmpty());
    }

}
