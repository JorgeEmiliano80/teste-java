package com.jorge.portfolio.common.response;

import java.time.Instant;
import java.util.Map;

public record ErrorResponse(
        Instant timestamp,
        int status,
        String error,
        String message,
        String path,
        Map<String, String> fieldErrors
) {
    public static ErrorResponse of(
            int status,
            String error,
            String message,
            String path
    ) {
        return new ErrorResponse(
                Instant.now(),
                status,
                error,
                message,
                path,
                Map.of()
        );
    }

    public static ErrorResponse withFieldErrors(
            int status,
            String error,
            String message,
            String path,
            Map<String, String> fieldErrors
    ) {
        return new ErrorResponse(
                Instant.now(),
                status,
                error,
                message,
                path,
                fieldErrors == null ? Map.of() : fieldErrors
        );
    }
}