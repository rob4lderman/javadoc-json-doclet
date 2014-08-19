package com.rga78.proc;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Observer;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * A little wrapper around a forked Process.  It sets up separate threads
 * to read stdout/stderr from the Process, to avoid hanging the Process.
 * 
 * When you fork a process from Java it sets up pipes between the Java process
 * and the forked process for stdout/stderr.  The pipes have a limited buffer
 * capacity and if this capacity is reached, then the forked process may hang
 * waiting for the Java process to read some data off the pipe.  That's why 
 * you need separate threads reading the stdout/stderr pipes from the Java
 * side while another thread calls, e.g, Process.waitFor.
 * 
 */
public class ProcessHelper<T extends ProcessHelper> {
    
    /**
     * Output stream identifier
     */
    public enum Stream {
        STDOUT,
        STDERR;
    }
    
    /**
     * The process.
     */
    private Process process;

    /**
     * Output observers are registered with the ProcessHelper and are notified
     * whenever the process writes output to stdout/stderr.
     */
    private Collection<Observer> stdoutObservers = new ArrayList<Observer>();
    
    /**
     * Output observers are registered with the ProcessHelper and are notified
     * whenever the process writes output to stdout/stderr.
     */
    private Collection<Observer> stderrObservers = new ArrayList<Observer>();
    
    /**
     * ExecutorService for creating separate threads to read the process's stdout/stderr streams.
     * TODO: inject ExecutorService?
     */
    private ExecutorService executorService = Executors.newCachedThreadPool();
    
    /**
     * The stdout stream.  Represented as a Future because the stream is read
     * asynchronously by a separate thread.
     * 
     * The stdout stream is returned as a List<String> - one string per line.
     */
    private Future<List<String>> stdout;
    
    /**
     * The stderr stream.  Represented as a Future because the stream is read
     * asynchronously by a separate thread.
     * 
     * The stderr stream is returned as a List<String> - one string per line.
     */
    private Future<List<String>> stderr;
    
    /**
     * A short description of the process, mainly for debugging purposes.
     */
    private String description;
    
    /**
     * CTOR.  
     * 
     * @param process - the already-started process.
     */
    public ProcessHelper(Process process) {
        this.process = process;
    }
    
    /**
     * Add an output observer for the given output stream (STDOUT or STDERR).
     * 
     * Note: observers should be added *BEFORE* calling spawnStreamReaders.
     * 
     * @param stream the output stream to observe (Stream.STDOUT, Stream.STDERR)
     * @param observer the observer
     * 
     * @return this
     */
    public T addObserver(Stream stream, Observer observer) {
        if (observer == null) {
            // ignore
        } else if (stream == Stream.STDOUT) {
            stdoutObservers.add(observer);
        } else {
            stderrObservers.add(observer);
        }
        return (T) this;
    }
    
    /**
     * Add the given output observer to both the STDOUT and STDERR streams.
     * 
     * Note: observers should be added *BEFORE* calling spawnStreamReaders.
     * 
     * @param observer the observer
     * 
     * @return this
     */
    public T addObserver(Observer observer) {
        addObserver(Stream.STDOUT, observer);
        return addObserver(Stream.STDERR, observer);
    }
    
    /**
     * Set the description
     * 
     * @return this
     */
    public T setDescription(String description) {
        this.description = description;
        return (T) this;
    }
    
    /**
     * @return process description
     */
    public String getDescription() {
        return description;
    }
    
    /**
     * Redirect to the given output stream
     */
    public T pipeTo(Stream stream, OutputStream outputStream) {
        return addObserver( stream, new StreamPiper(outputStream) );
    }
    
    /**
     * @return the executorService, for spawning threads to read stdout/stderr.
     */
    protected ExecutorService getExecutorService() {
        return executorService;
    }
    
    /**
     * @return this
     */
    protected T shutdownExecutor() {
        
        // Shutdown the executor threads.  
        // If we don't do this, then the JVM may not exit right
        // away because of cached (non-daemon) threads in the pool.  Eventually 
        // the cached threads time out and terminate from inactivity, which then
        // allows the JVM to exit, but that could take 20 or 30 seconds or so.
        getExecutorService().shutdown();
        
        return (T) this;
    }
    
    /**
     * Spawn threads (via the ExecutorService) to read stdout/stderr of the process.
     * 
     * @return this
     */
    public T spawnStreamReaders() {
        
        if (stdout != null) {
            // We already spawned them.
            return (T) this;
        }
        
        // Start threads for reading stdout and stderr.
        // Note: this must be done on separate threads otherwise the process may block
        // waiting for this guy to read some output.
        stdout = getExecutorService().submit( new Callable<List<String>>() {
            public List<String> call() {
                return loadStreamUnchecked(Stream.STDOUT, getProcess().getInputStream());
            }
        });

        stderr = getExecutorService().submit( new Callable<List<String>>() {
            public List<String> call() {
                return loadStreamUnchecked(Stream.STDERR, getProcess().getErrorStream());
            }
        });
        
        return (T) this;
    }
    
    /**
     * Start separate threads for reading stdout/stderr of the Process, 
     * then call process.waitFor.
     * 
     * @return this
     */
    public T waitFor() throws InterruptedException {
        
        spawnStreamReaders();

        getProcess().waitFor();
        
        shutdownExecutor();
        
        return (T) this;
    }
    
    /**
     * Destroy the process (via process.destroy()), then wait for it to terminate.
     * 
     * @return this
     */
    public T destroyAndWaitFor() throws InterruptedException {
        
        spawnStreamReaders();
        
        getProcess().destroy();
        getProcess().waitFor();
        
        shutdownExecutor();

        return (T) this;
    }
    
    /**
     * @return stdout data
     */
    public List<String> getStdout() throws ExecutionException, InterruptedException {
        return stdout.get();
    }
    
    /**
     * @return stderr data
     */
    public List<String> getStderr() throws ExecutionException, InterruptedException {
        return stderr.get();
    }
    
    /**
     * @return all output, stdout followed by stderr
     */
    public List<String> getOutput() throws ExecutionException, InterruptedException {
        List<String> retMe = new ArrayList<String>( getStdout() );
        retMe.addAll( getStderr() );
        return retMe;
    }
    
    
    /**
     * @return stdout data, without waiting. If an exception occurs then the exception message is returned.
     */
    public List<String> getStdoutNow() {
        if (stdout.isDone()) {
            try {
                return stdout.get();
            } catch (Exception e) {
                return Arrays.asList( "stdout could not be retrieved due to exception", e.getMessage() );
            } 
        }
        return Arrays.asList("stdout could not be retrieved because the process is not done");
    }
    
    /**
     * @return stderr data, without waiting. If an exception occurs then the exception message is returned.
     */
    public List<String> getStderrNow() {
        if (stderr.isDone()) {
            try {
                return stderr.get();
            } catch (Exception e) {
                return Arrays.asList( "stderr could not be retrieved due to exception", e.getMessage() );
            } 
        }
        return Arrays.asList("stderr could not be retrieved because the process is not done");
    }
    
    /**
     * @return process.exitValue.
     */
    public int exitValue() {
        return getProcess().exitValue();
    }
    
    /**
     * @return the wrapped process object
     */
    public Process getProcess() {
        return process;
    }
    
    /**
     * @return The contents of the InputStream
     *
     * @throws RuntimeException if an IOException occurs.
     */
    protected List<String> loadStreamUnchecked(Stream stream, InputStream is) {
        try {
            // return IOUtils.readLines(is, "ISO-8859-1");
            return loadStream(stream, is);
        } catch (IOException ioe) {
            throw new RuntimeException(ioe);
        }
    }

    /**
     * Read the contents of the given inputstream.
     * 
     * Each line is passed to the output observers.
     * 
     * The last 1000 lines are saved in a list and returned.
     *
     * @return The last 1000 lines of the InputStream.
     */
    protected List<String> loadStream(Stream stream, InputStream is) throws IOException {
        List<String> retMe = new LimitedQueue<String>(1000);
        BufferedReader br = new BufferedReader(new InputStreamReader( is ) );     // stdout
        String line;
        while ((line = br.readLine()) != null) {
            retMe.add(line);
            notifyObservers(stream, line);
        } 
        return retMe;
    }
    
    /**
     * Notify output observers when a new line of output is read from the process's
     * stdout or stderr streams.
     */
    protected void notifyObservers(Stream stream, String line) {
        Collection<Observer> observers = (stream == Stream.STDOUT) ? stdoutObservers : stderrObservers;
        for (Observer observer : observers) {
            observer.update(null, line);
        }
    }


}

/**
 * Fixed-size circular FIFO queue.
 * 
 */
class LimitedQueue<E> extends LinkedList<E> {

    private final int limit;

    public LimitedQueue(int limit) {
        this.limit = limit;
    }

    @Override
    public boolean add(E o) {
        super.add(o);    // added to tail.
        
        while (size() > limit) { 
            remove(); // removed from head
        } 

        return true;
    }
}
