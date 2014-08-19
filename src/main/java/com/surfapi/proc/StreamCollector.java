package com.surfapi.proc;

import java.util.ArrayList;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

/**
 * OutputObserver for ProcessHelper that collects all output into a List<String>
 */
public class StreamCollector implements Observer {
    
    /**
     * The output stream.
     */
    private List<String> output = new ArrayList<String>();
    
    /**
     * Write the line to the printStream.
     */
    @Override
    public void update(Observable o, Object line) {
        output.add((String)line);
    }

    /**
     * @return the collected output.
     */
    public List<String> getOutput() {
        return output;
    }
}
