package com.rga78.proc;

import java.io.OutputStream;
import java.io.PrintStream;
import java.util.Observable;
import java.util.Observer;

/**
 * OutputObserver for ProcessHelper that logs the output via Log.log.
 */
public class StreamPiper implements Observer {
    
    /**
     * The output stream.
     */
    private PrintStream printStream;
    
    /**
     * CTOR.
     */
    public StreamPiper(OutputStream outputStream) {
        printStream = new PrintStream(outputStream);
    }

    /**
     * Write the line to the printStream.
     */
    @Override
    public void update(Observable o, Object line) {
        printStream.println(line);
    }

}
