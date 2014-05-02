package com.surfapi.proc;

import java.io.IOException;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

/**
 * Contains info related to the javadoc process that failed.
 */
public class ProcessException extends IOException {
    
    /**
     * CTOR.
     */
    public ProcessException(ProcessHelper processHelper) {
        this( processHelper.getProcess(), processHelper.getDescription(), processHelper.getStdoutNow(), processHelper.getStderrNow() );
    }
    
    /**
     * CTOR.
     */
    public ProcessException( Process p, String description, List<String> stdout, List<String> stderr) {
        super( "Process failed with exitValue " + p.exitValue()
               + "; Description: " + description
               + "; =====stdout================================================\n" 
               + StringUtils.join( stdout, "\n" )
               + "; =====stderr================================================\n" 
               + StringUtils.join( stderr, "\n" ) );
    }

}
