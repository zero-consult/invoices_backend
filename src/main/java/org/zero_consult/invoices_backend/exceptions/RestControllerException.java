package org.zero_consult.invoices_backend.exceptions;

import org.springframework.http.HttpStatusCode;

public class RestControllerException extends RuntimeException {
    private final HttpStatusCode statusCode;

    public RestControllerException(HttpStatusCode statusCode, String message) {
        super(message);
        this.statusCode = statusCode;
    }

    public RestControllerException(HttpStatusCode statusCode, String message, Throwable cause) {
        super(message, cause);
        this.statusCode = statusCode;
    }

    public HttpStatusCode getStatusCode() {
        return statusCode;
    }
}
