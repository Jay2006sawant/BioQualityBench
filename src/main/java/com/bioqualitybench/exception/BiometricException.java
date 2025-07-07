package com.bioqualitybench.exception;

/**
 * Checked exception for SDK failures, timeouts, or unusable biometric samples.
 */
public class BiometricException extends Exception {

    public BiometricException(String message) {
        super(message);
    }

    public BiometricException(String message, Throwable cause) {
        super(message, cause);
    }
}
