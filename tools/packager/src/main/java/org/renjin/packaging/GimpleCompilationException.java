package org.renjin.packaging;

public class GimpleCompilationException extends BuildException {
    public GimpleCompilationException(String message) {
        super(message);
    }

    public GimpleCompilationException(String message, Exception cause) {
        super(message, cause);
    }
}
