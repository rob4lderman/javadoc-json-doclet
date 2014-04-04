package com.surfapi.proc;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.apache.commons.io.IOUtils;

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
     * The process.
     */
    private Process process;
    
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
     * CTOR.  
     * 
     * @param process - the already-started process.
     */
    public ProcessHelper(Process process) {
        this.process = process;
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
                return loadStreamUnchecked(process.getInputStream());
            }
        });

        stderr = getExecutorService().submit( new Callable<List<String>>() {
            public List<String> call() {
                return loadStreamUnchecked(process.getErrorStream());
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

        process.waitFor();
        
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
        
        process.destroy();
        process.waitFor();
        
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
        return process.exitValue();
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
    protected List<String> loadStreamUnchecked(InputStream is) {
        try {
            return IOUtils.readLines(is, "ISO-8859-1");
            // return loadStream(is);
        } catch (IOException ioe) {
            throw new RuntimeException(ioe);
        }
    }

    /**
     * Read the entire contents of the given inputstream and return as a String.
     *
     * @return The contents of the InputStream.
     */
    protected List<String> loadStream(InputStream is) throws IOException {
        List<String> retMe = new ArrayList<String>();
        BufferedReader br = new BufferedReader(new InputStreamReader( is ) );     // stdout
        String line;
        while ((line = br.readLine()) != null) {
            retMe.add(line);
        } 
        return retMe;
    }


}
