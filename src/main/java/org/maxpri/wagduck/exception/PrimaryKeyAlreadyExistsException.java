package org.maxpri.wagduck.exception;

public class PrimaryKeyAlreadyExistsException extends RuntimeException {
    public PrimaryKeyAlreadyExistsException(String message) {
        super(message);
    }
}
