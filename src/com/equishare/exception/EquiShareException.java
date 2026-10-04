package com.equishare.exception;

/**
 * Base custom runtime exception for EquiShare Pro application.
 */
public class EquiShareException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public EquiShareException(String message) {
        super(message);
    }

    public EquiShareException(String message, Throwable cause) {
        super(message, cause);
    }
}
