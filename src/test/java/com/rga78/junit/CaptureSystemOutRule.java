package com.rga78.junit;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

/**
 * 
 * Capture and suppress stdout unless the test fails.
 *
 */
public class CaptureSystemOutRule implements TestRule {

    private PrintStream originalSystemOut = System.out;
    private PrintStream originalSystemErr = System.err;
    
    private ByteArrayOutputStream capturedSystemOut;
    
    /**
     * @param base Represents the test to be run. Typically you'd wrap this 
     *             Statement with a new Statement that performs the before/after
     *             operations around a call to base.evaluate(), which executes
     *             the test.
     * @param description This can be used to obtain @annotation data from the
     *             test method.
     *
     * @return A Statement to be evaluated() by the test runner.
     */
    @Override
    public Statement apply( Statement base, Description description ) {
        return new CollectSystemOutStatement( base );
    }

    /**
     * Statement class - performs the before/after operations around a 
     * call to the base Statement's evaulate() method (which runs the test).
     */
    protected class CollectSystemOutStatement extends Statement {

        /**
         * A reference to the Statement that this statement wraps around.
         */
        private final Statement base;

        /**
         * CTOR.
         *
         * @param base The Statement that MyStatement wraps around.
         */
        public CollectSystemOutStatement(Statement base) {
            this.base = base;
        }

        /**
         * This method is called by the test runner in order to execute the test.
         *
         * Before/After logic is embedded here around a call to base.evaluate(),
         * which processes the Statement chain (for any other @Rules that have been
         * applied) until at last the text method is executed.
         *
         */
        @Override
        public void evaluate() throws Throwable {

            before();

            try {
                base.evaluate();
            } catch (Throwable t) {
                dumpStreams();
                throw t;
            } finally {
                after();
            }
        }

        /**
         * Capture stdout.
         */
        protected void before()  {
            capturedSystemOut = new ByteArrayOutputStream();
            System.setOut( new PrintStream(capturedSystemOut) );
            System.setErr( new PrintStream(capturedSystemOut) );
        }

        /**
         * Restore stdout.
         */
        protected void after() {
            System.setOut(originalSystemOut);
            System.setErr(originalSystemErr);
        }
        
        /**
         * Dump captured output to system out.
         */
        protected void dumpStreams() {
            after();
            System.out.print( capturedSystemOut.toString() );
        }
        
        
    }
}
