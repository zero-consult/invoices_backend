package org.zero_consult.invoices_backend.exceptions;

public class ServiceUnavailableException extends Exception {
    public ServiceUnavailableException(String message) {
        super(message);
    }

    public ServiceUnavailableException(String message, Throwable cause) {
        super(message, cause);
    }

}
