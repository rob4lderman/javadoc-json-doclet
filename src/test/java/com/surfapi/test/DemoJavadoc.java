package com.surfapi.test;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.List;
import java.util.concurrent.Callable;

// Here are all the tags we need to use:
// REF: http://docs.oracle.com/javase/7/docs/technotes/tools/windows/javadoc.html#link
//@author  1.0
//{@code}  1.5
//{@docRoot}   1.3
//@deprecated  1.0
//@exception   1.0
//{@inheritDoc}    1.4
//{@link}  1.2
//{@linkplain} 1.4
//{@literal}   1.5
//@param   1.0
//@return  1.0
//@see 1.0
//@serial  1.2
//@serialData  1.2
//@serialField 1.2
//@since   1.1
//@throws  1.2
//{@value} 1.4
//@version

/**
* A test file used for parsing its javadoc comments.
*
* This file was copied from {@link it.sauronsoftware.feed4j.FeedParser FeedParser}.
* This file links to {@link com.surfapi.test.DemoAnnotation}.
* It also links in plaint text: {@linkplain com.surfapi.test.DemoJavadocException}
*
* And here is some {@literal literal text like <hello> surrounded by brackets}
*
* And here is some malicious scripting: <script>window.alert("hello from DemoJavadoc.commentText")</script>
*
* Here's some <code>code within a code tag </code>.
*
* Compared to the @{code at-code tag, with malicious script: <script>window.alert("hello from DemoJavadoc.commentText.@code")</script>}
* 
* @param <T> A generic type parm T, extends List
* 
* @author Rob Alderman
* @since 1.0
* @version 1.0
*
* @see <a href="http://docs.oracle.com/javase/7/docs/technotes/tools/windows/javadoc.html#link">Javadoc Reference Guide</a>
* @see com.surfapi.javadoc.JsonDoclet
* @see com.surfapi.javadoc.JsonDoclet#processType
* @see java.net.URL#equals The URL.equals method
*
*/
@DemoAnnotation( author = "Rob Alderman" )
public abstract class DemoJavadoc<T extends List> implements Callable<String>, DemoInterface {

    /**
     * A private member.
     */
    private String x;
    
    /**
     * A protected member String array.
     */
    protected volatile transient String[] y;
    
    /**
     * A static final member named z.
     */
    public static final String z = "ZZZ";
    
    /**
     * CTOR. default.
     */
    public DemoJavadoc() {}

    /**
     * CTOR.
     * 
     * @param b     Some {@link java.lang.String String} parameter named b. 
     */
    public DemoJavadoc(String b) {
        this.x = b;
    }
    
    /**
     * 
     * @param nope this parm doesn't exist!
     * @param demoJavdoc oh man! i misspelled demoJavadoc!
     * 
     * @throws DemoJavadocException for whatever reason
     * @throws NullPointerException if you gimme a null
     */
    public DemoAnnotation getAnnotation(DemoJavadoc demoJavadoc) throws DemoJavadocException {
        return null;
    }
    
    /**
     * This is an abstract method.
     */
    public abstract void someAbstractMethod(String[] strs);
    
    /**
     * This is a static method.
     */
    public static int someStaticMethod() { return 0; }

    /**
     * This is the first sentence of the javadoc description for method parse(URL).
     * This is the second sentence of the javadoc. For more
     * info on URLs go {@link java.net.URL here}.  How about a 4th sentence? 
     * 
     * @param url   The first parm is a feed URL.<script>window.alert("hello from parse.url @param");</script>
     *              The description for the url parm has two lines.
     * @param y     An List param named y.
     *             
     * @return An input stream for the feed <script>window.alert("hello from parse.url @return ");</script>
     * 
     * @throws IOException
     *             I/O error during conetnts retrieving.
     *
     * @deprecated
     */
    public InputStream parse(URL url, @DemoAnnotation(author="Rob") List<T> y) throws IOException, IllegalArgumentException {
       
        if (x.equals("blah")) {
            throw new IOException("blah");
        } 
        
        return null;
    }
    
    /**
     * @param classLevelType Is of the class-level generic type T.
     * @param methodLevelType Is of the method-level generic type E.
     * @param <E> A generic type parm E
     * 
     * @return E some object of type E
     */
    public <E> E methodWithTypes(T[] classLevelType, E methodLevelType) {
        return methodLevelType;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String call() throws Exception {
        // TODO Auto-generated method stub
        return null;
    }
    
    /**
     * Javadoc for non-static inner class.
     */
    public class NonStaticInnerClass {
        /**
         * This here is a method within the inner class, the non-static one.
         */
        public void innerClassMethodBlah() { }
    }
    
    /**
     * Javadoc for static inner class.
     */
    public static class StaticInnerClass {
        
    }

}
